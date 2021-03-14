
/**
 * Page class represent a single page of a domain
 * 
 * 
 */
public class Page
{
    private String url;
    private int idNumber;
    private boolean downloaded;



    /**
     * Builder
     * 
     * @param url the page url
     * @param index the id associated to the page (used for file name too)
     */
    public Page(String url, int index)
    {
	this.idNumber = index;
	this.url = url;
	this.downloaded = false;
    }



    /**
     * @return url
     */
    public String getUrl()
    {
	return this.url;
    }



    /**
     * @return id
     */
    public int getIndex()
    {
	return this.idNumber;
    }



    /**
     * @return true if the page is downloaded yet
     */
    public boolean getIsDownloaded()
    {
	return this.downloaded;
    }



    /**
     * set the page as downloaded
     */
    public void setIsDownloaded()
    {
	this.downloaded = true;
    }
}
