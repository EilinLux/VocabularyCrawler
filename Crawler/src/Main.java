/**
 * Main class
 * 
 * 
 *  
 */
class Main
{
    TreeDownloader downloader;
    Analyzer analyzer;
    Rules rules;



    /**
     * DON'T MODIFY THIS METHOD
     */
    public static void main(String[] args)
    {
	new Main().define();
    }



    /**
     * DON'T MODIFY THIS METHOD
     */
    private void initialize(String subFolder)
    {
	this.downloader = new TreeDownloader(subFolder);
	this.analyzer = new Analyzer(subFolder);
	this.rules = new Rules(subFolder);
    }



    /**
     * MODIFY THIS CLASS:
     */
    private void define()
    {
	/**
	 * DON'T COMMENT OR MODIFY THIS LINE
	 * 
	 * Change the subfolder name, if you want
	 */
	this.initialize("FoodNames");

	/**
	 * Download a domain (ex. http://www.giallozafferano.it):
	 * 
	 * @param String an url of the domain to download (home page suggested)
	 * @param Int max page to download for this session, -1 for no max (optional)
	 * @param Int page number, from which you want to start to download, skip page that have been download before (optional)
	 */
	
	  // to download the whole domain 
	  // this.downloader.download("http://www.giallozafferano.it");

	  // to skip the first 150 pages and download other 200 
	  // this.downloader.download("http://www.giallozafferano.it", 200, 414);
	  
	  

	/**
	 * Analyse a domain folder (check saves\domains for the name, if you are not sure)
	 * building list of words, based on tags (analizeByTag) or urls (urlFileter)
	 * 
	 * @method analyzeByTag: extract text, based on the HTML structure
	 * @param tags.class HTML structure
	 * @param output file name (optional)
	 * 
	 * @method urlFileter: extract text, cutting off the url
	 * @param url selector url fragment to cut (if not given it used as output file name,)
	 * @param output file name (optional)
	 */

	
	this.analyzer.AnalyzeByTags("giallozafferano.it", new String[] { "article", "header", "h2", "a" }, "results_headers");
	//this.analyzer.AnalyzeByTags("recipes", new String[] { "h2.title-recipe", "a" },  "results_h2_title-recipe");
	
	
	// this.analyzer.urlSelection("https://ricette.giallozafferano.it/", "outputFileName_giallo_zafferano");
	// this.analyzer.urlFilter("https://ricette.giallozafferano.it/"); 
	
	// this.analyzer.urlSelection("https://www.giallozafferano.it/ricette-cat", "outputFileName_cat_giallo_zafferano");
	// this.analyzer.urlFilter("https://www.giallozafferano.it/ricette-cat/page"); //
	
	
	
	this.analyzer.urlFilter("http://ricette.giallozafferano.it/ricette-con", "ricette_con");
	// this.analyzer.urlSelection("recipesUrl", "https://blog.giallozafferano.it/tmm/tag/");

	/**
	 * Additional filters to refine results in saves/results/FoodName/
	 * 
	 * @method writeListFile: write a list in "<name>" and save as .txt
	 * @param input file names
	 */

	this.rules.writeListFile("ricette_con");
	
	/**
	 * NOT WORKING AT THE MOMENT (http://mary.dfki.de/ is down)
	 * Additional filters to refine results with http://mary.dfki.de/
	 * 
	 * @method tagWord: 
     * @param filename
     * @param filepath 
     * @param language
     * @param outputmary
     * 
	 */


	// this.rules.tagWord("saves/results/FoodName/Mary_results_h2_title-recipe", "saves/results/FoodName/FoodNames_l", "it", "PARTSOFSPEECH");
	// this.rules.patterWords("ricette_con", "Pattern_outputTest");
	// this.rules.cutShorterThan("outputFileName_giallo_zafferano", "short", 3);

	/**
	 * Additional filters to refine results in saves/results/FoodName/
	 * 
	 * @method mergeResults: merge results
	 * @param array of input file names
	 * @param output name
	 */
	// this.rules.mergeResults(new String[] { "result-1",  "result-2" }, "mergedResults");

    
    }

}
