import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * TreeDownloader class, download a domain
 * 
 * @author Andrea Picciolo
 */
public class TreeDownloader
{
    private static final String LOG_DOWNLOAD_COMPLETE = "LOG - Download complete";
    private static final String LOG_DOWNLOAD_SUSPENDED = "LOG - Download suspended";
    private static final String LOG_NUMBER_DOWNLOADED_FILES = "LOG - file downloaded: ";
    private static final String LOG_TIME_ELAPSED = "LOG - %d files downloaded in %f secs, %f secs/page%n";

    private static final String ERR_DOWNLOAD_FAIL = "ERR - Download failed: ";
    private static final int MAX_INDEX_BLOCK_SAVER = 100;

    private List<Page> index;

    private int idNumber;
    private FileManager fileManager;
    private int sessionPageCounter;
    private int maxPagesDownload;
    private String domain;
    private long timeStart;



    /**
     * Builder
     * 
     * @param subFolder folder where to save datas
     */
    public TreeDownloader(String subFolder)
    {
	this.fileManager = new FileManager(subFolder);
    }



    /**
     * @Override
     */
    public void download(String url)
    {
	this.download(url, null, -1, this.findLastSavedIndex());
    }



    /**
     * @Override
     */
    public void download(String url, String urlLimit)
    {
	this.download(url, urlLimit, -1, this.findLastSavedIndex());
    }



    /**
     * @Override
     */
    public void download(String url, String urlLimit, int indexIdStartFrom)
    {
	this.download(url, urlLimit, -1, indexIdStartFrom);
    }



    /**
     * @Override
     */
    public void download(String url, int maxPagesDownload, int indexIdStartFrom)
    {
	this.download(url, null, maxPagesDownload, indexIdStartFrom);
    }



    /**
     * Download a domain, or a part of it
     * 
     * @param url: starting url
     * @param urlLimit: the part of the domain to download (optional for full site)
     * @param maxPagesDownload: max number of pages to download (optional, -1 for no limit)
     * @param startFrom: index to start from (optional, last downloaded page by default)
     */
    public void download(String url, String urlLimit, int maxPagesDownload, int indexIdToDownload)
    {
	// initialize
	this.domain = this.getDomain(url);
	if (this.index == null)
	{
	    this.index = fileManager.readIndexFile(this.domain);
	}
	this.maxPagesDownload = maxPagesDownload;
	this.timeStart = System.nanoTime();
	this.sessionPageCounter = 0;

	this.idNumber = this.index.size();

	// if the list is empty (not loaded from index loader) add the starting url to the list
	if (this.index.size() == 0)
	{
	    Page page = new Page(url, this.idNumber);
	    this.index.add(page);
	    this.idNumber++;
	}

	// create folder if don't exist
	this.fileManager.createSubFolderDownloader(this.domain);

	// iterate the index to scan all
	while (indexIdToDownload < this.index.size() && (this.sessionPageCounter < this.maxPagesDownload || this.maxPagesDownload == -1))
	{
	    if (!this.index.get(indexIdToDownload).getIsDownloaded())
		if (urlLimit == null)
		{
		    this.downloadAndScanPage(this.index.get(indexIdToDownload).getUrl());
		} else
		{
		    if (this.index.get(indexIdToDownload).getUrl().contains(urlLimit))
		    {
			this.downloadAndScanPage(this.index.get(indexIdToDownload).getUrl());
		    }
		}
	    indexIdToDownload++;
	    // update index each N cycle to don't lose partial job
	    if (this.sessionPageCounter % MAX_INDEX_BLOCK_SAVER == 0 && this.sessionPageCounter != 0)
	    {
		this.fileManager.writeIndexFile(this.index, this.domain);
		float elapsedTime = ((System.nanoTime() - this.timeStart)) / 1000000000;
		System.err.format(LOG_TIME_ELAPSED, this.sessionPageCounter, elapsedTime, elapsedTime / this.sessionPageCounter);
	    }
	}
	if (this.sessionPageCounter < this.maxPagesDownload || this.maxPagesDownload == -1)
	{
	    System.err.println(TreeDownloader.LOG_DOWNLOAD_COMPLETE);
	} else
	{
	    System.err.println(TreeDownloader.LOG_DOWNLOAD_SUSPENDED);
	}
	System.err.println(TreeDownloader.LOG_NUMBER_DOWNLOADED_FILES + this.sessionPageCounter);
	this.fileManager.writeIndexFile(this.index, this.domain);
    }



    /**
     * Find the domain of a certain url
     * 
     * @param url
     * @return domain
     */
    private String getDomain(String url)
    {
	String[] fragments = url.split("//")[1].split("/")[0].split("\\.");
	return fragments[1].concat("." + fragments[2]);
    }



    /**
     * Find the last downloaded page
     * 
     * @return index of the page
     */
    private int findLastSavedIndex()
    {
	if (this.index == null)
	{
	    this.index = fileManager.readIndexFile(this.domain);
	}
	int i = this.index.size();
	while (i > 0)
	{
	    i--;
	    if (this.index.get(i).getIsDownloaded())
	    {
		return i;
	    }
	}
	return i;
    }



    /**
     * Scan a single page
     * 
     * @param url of the page
     * @param domain
     */
    private void downloadAndScanPage(String url)
    {
	Document rawHtml = this.downloadRawHtml(url);
	if (rawHtml != null)
	{
	    this.findLinks(rawHtml);
	    this.sessionPageCounter++;
	}
    }



    /**
     * Find the links of a page and add into the collection
     * 
     * @param rawHtml HTML of the page
     * @param domain
     */
    private void findLinks(Document rawHtml)
    {
	Elements links = rawHtml.select("a[href]");
	for (Element link : links)
	{
	    // check the link belongs to domain
	    String linkString = link.attr("abs:href");
	    if (linkString.endsWith("/"))
	    {
		linkString = linkString.substring(0, linkString.length() - 1);
	    }
	    if (isIntoDomain(linkString))
	    {
		// if the link is unique, add to index
		if (this.fileManager.getListHasUrl(this.index, linkString) == null)
		{
		    Page page = new Page(linkString, this.idNumber);
		    this.index.add(page);
		    this.idNumber++;
		}
	    }
	}
    }



    /**
     * Try to download a page
     * 
     * @param url
     * @return the page ad document item is success, null if fail
     */
    private Document downloadRawHtml(String url)
    {
	try
	{
	    Document rawHtml = Jsoup.connect(url).get();
	    if (this.fileManager.writePageFile(url, rawHtml, this.index, this.domain))
	    {
		this.fileManager.getListHasUrl(this.index, url).setIsDownloaded();
		return rawHtml;
	    } else
	    {
		return null;
	    }
	} catch (Exception connectException)
	{
	    System.err.println(TreeDownloader.ERR_DOWNLOAD_FAIL + url);
	    return null;
	}
    }



    /**
     * Check if an url belongs to the domain
     * 
     * @param url
     * @param domain
     * @return true/false
     */
    private boolean isIntoDomain(String url)
    {
	try
	{
	    String target = url.split("//")[1];
	    target = target.split("/")[0];
	    return target.contains(this.domain);
	} catch (Exception e)
	{
	    return false;
	}
    }

}
