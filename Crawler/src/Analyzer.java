import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * the class Analyser take as input a subfolder where information about web pages will be stored. it has two main ways to extract information: information from HTML and information from the URL.
 * 
 * 
 * 
 */
/**
 * 
 *
 */
public class Analyzer
{
    private static final String FILE_EXIST = "ERR - File is open, close it and run it agian";
    private static final String ALL_WORDS_AND_NUMBERS = "\\W";
    private static final String AFTER_DOMAIN = "([^,]*).html";
    private static final String FILE_NAME_DEFAULT = "result";
    private static final String FILE_NAME_SEPARATOR = "-";

    private List<Page> index;
    private SortedSet<String> results;
    private FileManager fileManager;
    private String searchName;



    /**
     * Analyzer class make a search in a download domain to produce a result file
     * 
     * @param subFolder
     */
    public Analyzer(String subFolder)
    {
	this.results = new TreeSet<String>();
	this.fileManager = new FileManager(subFolder);
	this.searchName = subFolder;
	this.fileManager.createSubFolderAnalyzer(subFolder);
    }



    /**
     * Override for method AnalyzeByTags that build automatically the file name
     * 
     * @param selectorTags HTML tags for search
     */
    public void AnalyzeByTags(String domain, String[] selectorTags)
    {
	String outputFileName = Analyzer.FILE_NAME_DEFAULT;
	for (int i = 0; i < selectorTags.length; i++)
	{
	    outputFileName += Analyzer.FILE_NAME_SEPARATOR + selectorTags[i];
	}
	this.AnalyzeByTags(domain, selectorTags, outputFileName);
    }



    /**
     * Scan the index to find all download pages and for each look for HTML tags
     * 
     * @param outputFileName output file name
     * @param selectorTags HTML tags for search
     */
    public void AnalyzeByTags(String domain, String[] selectorTags, String outputFileName)
    {
	// iterate the index to scan all
	this.index = this.fileManager.readIndexFile(domain);
	int i = 0;
	while (i < this.index.size())
	{
	    if (this.index.get(i).getIsDownloaded())
	    {
		this.findTargetsByTags(this.fileManager.readPageFile(this.index.get(i).getUrl(), this.index, domain), selectorTags);
	    }
	    i++;
	}
	this.fileManager.writeResultFile(outputFileName, this.results, this.searchName);
    }



    /**
     * Use the params to scan the HTML to find the required texts
     * 
     * @param rawHtml the HTML
     * @param selectorParams params for search
     */
    public void findTargetsByTags(Document rawHtml, String[] selectorParams)
    {
	// build the selector
	Elements selector = rawHtml.select(selectorParams[0]);
	for (int i = 1; i < selectorParams.length; i++)
	{
	    selector = selector.select(selectorParams[i]);
	}

	// find and add all results
	int i = 0;
	while (true)
	{
	    try
	    {
		String selected = selector.get(i).text();
		i++;
		this.results.add(selected);
	    } catch (Exception e)
	    {
		break;
	    }
	}
	rawHtml = null;
    }



    /**
     * Override for method urlSelection that build automatically the file name
     * 
     * @param selectorTags
     */
    public void urlFilter(String partOfUrlToBeElimitate)
    {
	String outputFileName = Analyzer.FILE_NAME_DEFAULT + Analyzer.FILE_NAME_SEPARATOR;
	String[] fragments = partOfUrlToBeElimitate.split("//");
	for (int i = 1; i < fragments.length; i++)
	{
	    outputFileName += fragments[i];
	}
	this.urlFilter(partOfUrlToBeElimitate, outputFileName.replace("/", "_"));
    }



    /**
     * Given URLs, urlFilter return in a file (resultFileName) parts of them, eliminating the what was insert as partOfUrlToBeElimitate
     * 
     * @param resultFileName, name the output file
     * @param partOfUrlToBeElimitate, part of the url that has to be eliminated to extract useful information
     */
    public void urlFilter(String urlHead, String outputFileName)
    {
	String[] fragments = urlHead.split("//")[1].split("/")[0].split("\\.");
	String domain = fragments[1].concat("." + fragments[2]);
	this.index = this.fileManager.readIndexFile(domain);

	// find the results
	for (int i = 0; i < this.index.size(); i++)
	{
	    String line = this.index.get(i).getUrl();
	    if (line.contains(urlHead))
	    {
		String urlTail = line.substring(urlHead.length());
		if (!urlTail.contains("/"))
		{
		    if (urlTail.contains("html"))
		    {
		    	urlTail = urlTail.split("html")[0];
		    	if (urlTail.contains("."))
			    {
		    		urlTail = urlTail.split("\\.")[0];
			    }
		    }
		    this.results.add(this.format(urlTail));
		}
	    }
	}
	this.fileManager.writeResultFile(outputFileName, this.results, this.searchName);
    }



    /**
     * Format a string
     * 
     * @param input string
     * @return output string
     */
    private String format(String input)
    {
	return input.replaceAll(Analyzer.ALL_WORDS_AND_NUMBERS, " ").toLowerCase();
    }



    /**
     * Given URLs, urlSelection  slices the urls (eliminating partOfUrlToBeElimitate) and returns a file (outputFileName)
     * 
     * @param resultFileName, output file name
     * @param partOfUrlToBeElimitate, part of the url that has to be eliminated to extract useful information
     */
    public void urlSelection(String partOfUrlToBeElimitate, String outputFileName)

    {
	String[] fragments = partOfUrlToBeElimitate.split("//")[1].split("/")[0].split("\\.");
	String domain = fragments[1].concat("." + fragments[2]);
	this.index = this.fileManager.readIndexFile(domain);

	// find and add all results
	for (int i = 0; i < this.index.size(); i++)
	{
	    try
	    {
		String line = this.index.get(i).getUrl();
		// find the value after the domain_path
		Pattern patternToCheck = Pattern.compile(partOfUrlToBeElimitate + Analyzer.AFTER_DOMAIN);
		Matcher matchedLine = patternToCheck.matcher(line);
		while (matchedLine.find())
		{

		    // save the new section
		    String section = matchedLine.group(1).replaceAll(Analyzer.ALL_WORDS_AND_NUMBERS, " ");
		    System.out.println(section);
		    this.results.add(section);
		    System.out.println("section");

		    // this.results.add(NEWLINE);

		}

	    } catch (Exception e)
	    {
		System.err.println(Analyzer.FILE_EXIST);
	    }

	}
	this.fileManager.writeResultFile(outputFileName, this.results, this.searchName);
    }
}
