import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Rules to refine results
 * 
 * @author Andrea Picciolo
 * @author Zelda Ailine Luconi
 */
public class Rules
{
    private static String FileFormat = "";

    // constants, error
    private static final String ERROR_SOUP = "Error connection with Jsoup for url, check if is not an encoding error ";
    private static final String FILE_EXIST = "File is open, close it and run agian";
    private static final String NOTWORKINGLINKS = "\n\n ----- BUT CHECK THE FOLLOWING LINKS (they did NOT worked) ----- \n\n";
    private static final String FILEEDITED = "\n\n ----- FILE EDITED with the following name  ----- \n\n";
    private static final String SUBSTITUTION_WORD = "$WORD";

    private List<String> NotWorkingLinks = new ArrayList<>();
    private static String WordToBeTagged = "";

    private SortedSet<String> results;
    private FileManager fileManager;
    private String subFolder;



    /**
     * Builder
     * 
     * @param subFolder
     */
    public Rules(String subFolder)
    {
	this.subFolder = subFolder;
	this.fileManager = new FileManager(subFolder);
    }



    /**
     * MaryTTS
     * 
     * @param filename
     * @param filepath
     * @param language
     * @param outputmary
     */
    public void tagWord(String filename, String filepath, String language, String outputmary)
    // were to find the csv, name of the file, language of the text, kind of output in Mary (check http://mary.dfki.de:59125/)
    {
	String FILE_NAME = filename + ".csv";
	String FILE_PATH = filepath;
	String sLOCALE = language;
	String sOUTPUT = outputmary;

	try
	{
	    PrintWriter pw = new PrintWriter(new File(FILE_NAME));
	    StringBuilder taggerframework = new StringBuilder();

	    // construct the framework (columns name)
	    taggerframework.append("tag rule");
	    taggerframework.append(',');
	    taggerframework.append("sentence");
	    taggerframework.append(',');
	    taggerframework.append("tag");
	    taggerframework.append(',');
	    taggerframework.append("words");
	    taggerframework.append(',');
	    taggerframework.append("complete tagged sentence");
	    taggerframework.append('\n');

	    // scan CSV
	    Scanner scanner = new Scanner(new File(FILE_PATH));
	    while (scanner.hasNext())
	    {
		WordToBeTagged = scanner.nextLine().toString().toLowerCase().replaceAll("[()?:!.;{}]+", " ").replace(",", ";");

		// low case in order to disambiguate SP and S
		// eliminate symbos and substitute spaces with +

		WordToBeTagged = WordToBeTagged.replaceAll(" ", "+");
		FileFormat = "http://mary.dfki.de:59125/process?INPUT_TEXT=" + "le+" + WordToBeTagged + "&INPUT_TYPE=TEXT&OUTPUT_TYPE=" + sOUTPUT + "&LOCALE=" + sLOCALE;
		// the article "le" used to disambiguate S and SP
		// call to the URL
		try
		{
		    Document d = Jsoup.connect(FileFormat).timeout(6000).get();

		    String DocToSting = d.toString();
		    // select the body
		    DocToSting = DocToSting.split("<s>")[1].split("</s>")[0];
		    // replace the tree structure with a one line string
		    DocToSting = DocToSting.replaceAll("[\r\n]+", " ").replace("  ", "");
		    // extract the structure " tags > word "
		    DocToSting = DocToSting.replaceAll("<t pos=", "");
		    DocToSting = DocToSting.replaceAll("</t>", ";");
		    // Separate the different structures " tags > word "
		    String[] words = DocToSting.split(";");

		    String rule = "";
		    ArrayList<String> taglist = new ArrayList<String>();
		    ArrayList<String> wordlist = new ArrayList<String>();

		    // use of int =1 to eliminate the article "le" used to disambiguate
		    for (int i = 1, l = words.length; i + 1 < l; i++)
		    {

			// for each structure " tags > word "
			rule = words[i].toString();
			// Select the word and the tag
			String Word = rule.split(">")[1];
			String Tag = rule.split(">")[0];

			// add them to the framework
			taglist.add(Tag);
			wordlist.add(Word);
			// normalize S, SP, A and V as a $, in order to consider the presence of variable structures
			taggerframework.append(taglist.toString().replace(",", " ").replace("[", "").replace("]", "").replace("V", "$").replace("^S", "$").replace("SP", "$").replaceAll("RD", "$").replace("^A", "$"));
			taggerframework.append(',');
			taggerframework.append(wordlist.toString().replace(",", " ").replace("[", "").replace("]", ""));
			taggerframework.append(',');
			taggerframework.append(Tag);
			taggerframework.append(',');
			taggerframework.append(Word);
			taggerframework.append(',');
			taggerframework.append(DocToSting.replaceAll(">", ":").replaceAll(";", " ").replace(" \"RD\":le", " "));
			taggerframework.append('\n');
		    }

		}

		catch (Exception e)
		{
		    System.out.println(ERROR_SOUP + FileFormat);
		    System.out.println(e);
		    this.NotWorkingLinks.add(FileFormat);

		}
	    }

	    scanner.close();
	    pw.write(taggerframework.toString());
	    pw.close();

	} catch (Exception e)
	{
	    System.out.println(FILE_EXIST);
	}
	System.out.println(FILEEDITED);
	System.out.println(FILE_NAME);
	System.out.println(NOTWORKINGLINKS + NotWorkingLinks);

    }



    /**
     * Trasform a .csv file removing any result with redundant structure
     * 
     * @param fileIn
     * @param fileOut
     */
    public void patterWords(String fileIn, String fileOut)
    {
	this.results = new TreeSet<String>();
	TreeSet<String> structures = new TreeSet<String>();
	String[] fileResults = this.fileManager.readResultFile(fileIn, this.subFolder);
	// list of prepositions
	List<String> listOfPrepositions = Arrays.asList("di", "con", "della", "al", "alla", "e", "ed", "allo", "agli", "all'");
	// chiamata per l'output
	for (int i = 0; i < fileResults.length; i++)
	{
	    String[] splittedName = fileResults[i].split(" ");
	    String[] structureName = this.toStructure(splittedName, listOfPrepositions);
	    List<String> results = this.toResults(splittedName, structureName);
	    for (int j = 0; j < results.size(); j++)
	    {
		String structure = "";
		String[] splittedStructure = this.toStructure(results.get(j).split(" "), listOfPrepositions);
		for (int index = 0; index < splittedStructure.length; index++)
		{
		    structure += splittedStructure[index];
		}
		if (!structures.contains(structure))
		{
		    this.results.add(results.get(j));
		    structures.add(structure);
		}
	    }
	}
	this.fileManager.writeResultFile(fileOut, this.results, this.subFolder);
    }



    /**
     * Trasform a splitted string into the relative structure
     * 
     * @param splitted string
     * @param prepositions
     * @return structure
     */
    private String[] toStructure(String[] input, List<String> prepositions)
    {
	String[] output = new String[input.length];
	output[0] = input[0];
	for (int i = 1; i < input.length; i++)
	{
	    if (prepositions.contains(input[i]))
	    {
		output[i] = input[i];
	    } else
	    {
		output[i] = Rules.SUBSTITUTION_WORD;
	    }

	}
	return output;
    }



    /**
     * Generate all results from a splitted string
     * 
     * @param origin splitted string
     * @param transformed splitted string
     * @returnlist of results
     */
    private List<String> toResults(String[] origin, String[] transformed)
    {
	List<String> output = new ArrayList<String>();
	output.add(origin[0]);
	for (int i = 1; i < transformed.length; i++)
	{
	    if (transformed[i].equals(Rules.SUBSTITUTION_WORD))
	    {
		if (i == transformed.length - 1)
		{
		    output.add(this.generateSumString(origin, i));
		} else
		{
		    if (!transformed[i + 1].equals(Rules.SUBSTITUTION_WORD))
		    {
			output.add(this.generateSumString(origin, i));
		    }
		}
	    }
	}
	return output;
    }



    /**
     * Generate the string from the splitted string
     * 
     * @param origin
     * @param index
     * @return the string
     */
    private String generateSumString(String[] origin, int index)
    {
	String output = "";
	for (int i = 0; i <= index; i++)
	{
	    output += origin[i] + " ";
	}
	return output.trim();
    }



    /**
     * Merge more file results togedar
     * 
     * @param filesIn name of the file sources
     * @param fileOut name of the result
     */
    public void mergeResults(String[] filesIn, String fileOut)
    {
	this.results = new TreeSet<String>();
	for (int i = 0; i < filesIn.length; i++)
	{
	    String[] singleFileResults = this.fileManager.readResultFile(filesIn[i], this.subFolder);
	    for (int j = 0; j < singleFileResults.length; j++)
	    {
		this.results.add(singleFileResults[j]);
	    }
	}
	this.fileManager.writeResultFile(fileOut, this.results, this.subFolder);
    }



    /**
     * Remove all strings shorter than max
     * 
     * @param filesIn name of the file source
     * @param fileOut name of the result
     * @param max
     */
    public void cutShorterThan(String fileIn, String fileOut, int max)
    {
	this.results = new TreeSet<String>();
	String[] fileResults = this.fileManager.readResultFile(fileIn, this.subFolder);
	for (int i = 0; i < fileResults.length; i++)
	{
	    if (fileResults[i].length() > max)
	    {
		this.results.add(fileResults[i]);
	    }
	}
	this.fileManager.writeResultFile(fileOut, this.results, this.subFolder);
    }



    /**
     * Clear a string by white spaces on head and tail, and remove a preposition in head
     * 
     * @param fileIn
     * @param fileOut
     */
    public void clear(String fileIn, String fileOut)
    {
	this.results = new TreeSet<String>();
	String[] fileResults = this.fileManager.readResultFile(fileIn, this.subFolder);
	for (int i = 0; i < fileResults.length; i++)
	{
	    fileResults[i] = fileResults[i].trim();
	    this.results.add(this.removePrepositionOnHead(fileResults[i]));
	}
	this.fileManager.writeResultFile(fileOut, this.results, this.subFolder);
    }



    /**
     * Remove a preposition on head, if exist
     * 
     * @param input
     * @return
     */
    private String removePrepositionOnHead(String input)
    {
	String[] words = input.split(" ");
	String output = "";
	if (words[0].matches("i|il|lo|gli|le|la|l"))
	{
	    for (int i = 1; i < words.length; i++)
	    {
		output += " " + words[i];
	    }
	    return output.trim();
	} else
	{
	    return input;
	}
    }



    public void writeListFile(String fileIn)
    {
	this.results = new TreeSet<String>();
	String[] fileResults = this.fileManager.readResultFile(fileIn, this.subFolder);
	for (int i = 0; i < fileResults.length; i++)
	{
	    this.results.add(fileResults[i]);
	}
	this.fileManager.writeListFile(this.subFolder, this.results, this.subFolder);
    }
}