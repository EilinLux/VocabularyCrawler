import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * SaveManager class manage reading and writing files
 * 
 * 
 */
public class FileManager
{
    // Log messages
    private static final String LOG_INDEX_FILE_LOADED = "LOG - Index file loaded. Size: ";
    private static final String LOG_INDEX_FILE_SAVED = "LOG - Index file saved. Size: ";
    private static final String LOG_FILE_FOUND = "LOG - File found and loaded: ";
    private static final String LOG_FILE_SAVED = "LOG - File saved successfully: ";
    private static final String LOG_NUMBER_RESULTS = " results found";
    private static final String LOG_OUTPUT_SEPARATOR = "\n\n ----- OUTPUT ----- \n\n";
    private static final String LOG_FILE_NAME_SAVED_AT = "Name of saved file: ";

    // Error messages
    private static final String ERR_INDEX_FILE_NOT_FOUND = "ERR - Impossible to load index file, creating empty index";
    private static final String ERR_FILE_NOT_FOUND = "ERR - File not found, attempting to download: ";
    private static final String ERR_FILE_NOT_SAVED = "ERR - Impossible to save: ";

    // Values for file I/O
    private static final String NEWLINE = "\n";
    private static final String FOLDER_SEPARATOR = "/";
    private static final String FOLDER_ROOT = "saves";
    private static final String FOLDER_DOMAINS = "domains";
    private static final String FOLDER_RESULTS = "results";
    private static final String FILE_NAME_INDEX = "index";
    private static final String FILE_FORMAT_INDEX = ".json";
    private static final String FILE_FORMAT_PAGE = ".html";
    private static final String FILE_FORMAT_RESULT = ".csv";
    private static final String FILE_FORMAT_TXT = ".txt";

    private String rootSaveFolder;



    /**
     * Builder
     * 
     * @param subFolder to set the save folder
     */
    public FileManager(String subFolder)
    {
	this.rootSaveFolder = FileManager.FOLDER_ROOT + FileManager.FOLDER_SEPARATOR + subFolder + FileManager.FOLDER_SEPARATOR;
    }



    /**
     * @return the save folder
     */
    public String getSaveFolder()
    {
	return this.rootSaveFolder;
    }



    /**
     * Find the page with a certain url
     * 
     * @param list of pages
     * @param url of the page
     * @return the page found, null if don't found
     */
    public Page getListHasUrl(List<Page> list, String url)
    {
	for (int i = 0; i < list.size(); i++)
	{
	    if (list.get(i).getUrl().equals(url))
	    {
		return list.get(i);
	    }
	}
	return null;
    }



    /**
     * Create a folder for the domains used by Downloader class
     * 
     * @param subFolderName
     */
    public void createSubFolderDownloader(String subFolderName)
    {
	new File(FileManager.FOLDER_ROOT + FileManager.FOLDER_SEPARATOR + FileManager.FOLDER_DOMAINS + FileManager.FOLDER_SEPARATOR + subFolderName).mkdirs();
    }



    /**
     * Create a folder for the results used by analyzer class
     * 
     * @param subFolderName
     */
    public void createSubFolderAnalyzer(String subFolderName)
    {
	new File(FileManager.FOLDER_ROOT + FileManager.FOLDER_SEPARATOR + FileManager.FOLDER_RESULTS + FileManager.FOLDER_SEPARATOR + subFolderName).mkdirs();
    }



    /**
     * Read the index saved into index.json file
     * 
     * @param folderName
     * @return the index as List<Page>
     */
    public List<Page> readIndexFile(String folderName)
    {
	String path = FileManager.FOLDER_ROOT + FileManager.FOLDER_SEPARATOR + FileManager.FOLDER_DOMAINS + FileManager.FOLDER_SEPARATOR + folderName + FileManager.FOLDER_SEPARATOR + FileManager.FILE_NAME_INDEX + FileManager.FILE_FORMAT_INDEX;
	List<Page> index = new ArrayList<Page>();
	try
	{

	    String jsonString = new String(Files.readAllBytes(Paths.get(path)));
	    Gson objGson = new GsonBuilder().setPrettyPrinting().create();
	    Type listType = new TypeToken<List<Page>>()
	    {
	    }.getType();
	    index = objGson.fromJson(jsonString, listType);
	    System.err.println(FileManager.LOG_INDEX_FILE_LOADED + index.size());
	} catch (IOException e)
	{
	    index = new ArrayList<Page>();
	    System.err.println(FileManager.ERR_INDEX_FILE_NOT_FOUND);
	}
	return index;
    }



    /**
     * Save the index to index.json file
     * 
     * @param index as List<Page>
     */
    public void writeIndexFile(List<Page> index, String folderName)
    {
	String path = FileManager.FOLDER_ROOT + FileManager.FOLDER_SEPARATOR + FileManager.FOLDER_DOMAINS + FileManager.FOLDER_SEPARATOR + folderName + FileManager.FOLDER_SEPARATOR + FileManager.FILE_NAME_INDEX + FileManager.FILE_FORMAT_INDEX;
	try
	{
	    Gson gsonItem = new Gson();
	    String jsonString = gsonItem.toJson(index);
	    System.err.println(FileManager.LOG_INDEX_FILE_SAVED + index.size());
	    try
	    {
		FileWriter indexFile = new FileWriter(path);
		indexFile.write(jsonString);
		indexFile.close();

	    } catch (Exception e)
	    {
	    }
	} catch (Exception e)
	{
	}

    }



    /**
     * Try to read the page stored in the file
     * 
     * @param url of the page
     * @param index list
     * @return return the page or null if fail
     */
    public Document readPageFile(String url, List<Page> index, String folderName)
    {
	String path = FileManager.FOLDER_ROOT + FileManager.FOLDER_SEPARATOR + FileManager.FOLDER_DOMAINS + FileManager.FOLDER_SEPARATOR + folderName + FileManager.FOLDER_SEPARATOR + this.getListHasUrl(index, url).getIndex() + FileManager.FILE_FORMAT_PAGE;
	try
	{
	    Document rawHtml = Jsoup.parse(new String(Files.readAllBytes(Paths.get(path))));
	    System.err.println(FileManager.LOG_FILE_FOUND + path);
	    return rawHtml;
	} catch (Exception fileLoadException)
	{
	    System.err.println(FileManager.ERR_FILE_NOT_FOUND + fileLoadException);
	    return null;
	}
    }



    /**
     * Save a download page to a file
     * 
     * @param url of the page
     * @param rawHtml the complete HTML of the page
     * @param index list
     * @return true if success, false if failure
     */
    public boolean writePageFile(String url, Document rawHtml, List<Page> index, String folderName)
    {
	String path = FileManager.FOLDER_ROOT + FileManager.FOLDER_SEPARATOR + FileManager.FOLDER_DOMAINS + FileManager.FOLDER_SEPARATOR + folderName + FileManager.FOLDER_SEPARATOR + this.getListHasUrl(index, url).getIndex() + FileManager.FILE_FORMAT_PAGE;
	try
	{
	    FileWriter pageFile = new FileWriter(path);
	    pageFile.write(rawHtml.toString());
	    pageFile.close();
	    System.err.println(FileManager.LOG_FILE_SAVED + url);
	    rawHtml = null;
	    return true;
	} catch (Exception fileWriteException)
	{
	    System.err.println(FileManager.ERR_FILE_NOT_SAVED + fileWriteException);
	    return false;
	}
    }



    /**
     * Read a result .csv file
     * 
     * @param fileName
     * @return array or rows
     */
    public String[] readResultFile(String fileName, String folderName)
    {
	String path = FileManager.FOLDER_ROOT + FileManager.FOLDER_SEPARATOR + FileManager.FOLDER_RESULTS + FileManager.FOLDER_SEPARATOR + folderName + FileManager.FOLDER_SEPARATOR + fileName + FileManager.FILE_FORMAT_RESULT;
	try
	{
	    String[] results = (new String(Files.readAllBytes(Paths.get(path)))).split(FileManager.NEWLINE);
	    System.err.println(FileManager.LOG_FILE_FOUND + path);
	    return results;
	} catch (Exception fileLoadException)
	{
	    System.err.println(FileManager.ERR_FILE_NOT_FOUND + fileLoadException);
	    return null;
	}
    }



    /**
     * Save a result file
     * 
     * @param resultFileName the file name
     * @param results the result collection to write
     */
    public void writeResultFile(String fileName, SortedSet<String> results, String folderName)
    {
	String path = FileManager.FOLDER_ROOT + FileManager.FOLDER_SEPARATOR + FileManager.FOLDER_RESULTS + FileManager.FOLDER_SEPARATOR + folderName + FileManager.FOLDER_SEPARATOR + fileName + FileManager.FILE_FORMAT_RESULT;

	try
	{
	    FileWriter resultFile = new FileWriter(path);
	    System.err.println(FileManager.LOG_OUTPUT_SEPARATOR);
	    Iterator<String> it = results.iterator();
	    while (it.hasNext())
	    {
		String element = it.next();
		System.out.println(element);
		resultFile.write(element + FileManager.NEWLINE);
	    }
	    resultFile.close();
	    System.err.println(results.size() + FileManager.LOG_NUMBER_RESULTS + NEWLINE + LOG_FILE_NAME_SAVED_AT + fileName + FileManager.FILE_FORMAT_RESULT);
	} catch (Exception e)
	{
	    System.err.println(FileManager.ERR_FILE_NOT_SAVED + e);
	}
    }



    public void writeListFile(String fileName, SortedSet<String> results, String folderName)
    {
	String path = FileManager.FOLDER_ROOT + FileManager.FOLDER_SEPARATOR + FileManager.FOLDER_RESULTS + FileManager.FOLDER_SEPARATOR + folderName + FileManager.FOLDER_SEPARATOR + fileName + FileManager.FILE_FORMAT_TXT;

	try
	{
	    FileWriter ListFile = new FileWriter(path);
	    System.err.println(FileManager.LOG_OUTPUT_SEPARATOR);
	    Iterator<String> it = results.iterator();
	    while (it.hasNext())
	    {
		String element = it.next();
		System.out.println(element);
		ListFile.write("\"" + element + "\"" + FileManager.NEWLINE);
	    }
	    ListFile.close();
	    System.err.println(results.size() + FileManager.LOG_NUMBER_RESULTS + NEWLINE + LOG_FILE_NAME_SAVED_AT + fileName + FileManager.FILE_FORMAT_TXT);
	} catch (Exception e)
	{
	    System.err.println(FileManager.ERR_FILE_NOT_SAVED + e);
	}
    }
}