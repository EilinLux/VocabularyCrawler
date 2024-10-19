# Web Crawler for creating natural language corpora

This Java project implements a web crawler  to collect pages from a domain, useful also to create corpora of natural language expressions and/or vocabularies.

## Project Structure

- **Crawler:** Root directory for the project.
  - **.settings:** IDE settings files.
  - **bin:** Compiled Java class files (generated during build).
  - **saves:** Directory to store temporary crawl data (can be used for resuming crawls).
  - **domains/giallozafferano.it:** Domain-specific configuration and data.
  - **results/FoodNames:** Output directory where extracted data are stored.
  - **src:** Source code directory.
    - **Analyzer.java:**  Analyzes web page content to extract data.
    - **FileManager.java:** Handles file operations (reading, writing, saving, etc.).
    - **Main.java:**  Main entry point for the crawler application.
    - **Page.java:** Represents a web page with its URL and content.
    - **Rules.java:** Defines crawling rules and configurations.
    - **TreeDownloader.java:**  Handles the web crawling process (downloading pages, following links).
  - **.classpath:**  Eclipse project classpath file.
  - **.gitignore:**  Specifies files and directories to be ignored by Git.
  - **.project:**  Eclipse project file.
  - **pom.xml:**  Maven project file (for dependency management and build).
  - **.gitattributes:**  Git attributes file (for line ending configurations).

## Getting Started

### Prerequisites

- **Java Development Kit (JDK):**  Make sure you have a JDK installed (version 8 or higher).
- **Maven:**  Apache Maven is used for building the project and managing dependencies.

### Building the Project

1. **Clone the repository:**

   ```bash
   git clone [your-repository-url]
   cd web-crawler-recipe-names
   ```
Build with Maven:

   ```
  mvn clean install
  
   ```
This will compile the code and create a JAR file in the target directory.

Running the Crawler
Execute the JAR file:

   ```
  java -jar target/crawler-[version].jar

   ```

Replace [version] with the actual version number of the JAR file.
### Output:

The extracted data will be saved in the results/FoodNames directory

## Customization
* Crawling Rules: You can modify the crawling behavior by adjusting the rules in Rules.java.
* Domain-Specific Configuration: The domains/giallozafferano.it directory can be used to store domain-specific settings and data.
* Analyzer: The Analyzer.java file contains the logic for extracting data. You can modify this to extract other information or adapt it to different websites.

### Contributing
If you'd like to contribute to this project, please follow these steps:

 1. Fork the repository.
  2. Create a new branch for your feature or bug fix.
 3.  Make your changes and commit them.   
  4. Push your changes to your fork.
  5. Submit a pull request to the main repository.   
