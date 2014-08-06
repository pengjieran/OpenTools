package arithmetic.shared;
/*
 * FileNames.java
 *
 * Created on February 21, 2002, 10:54 AM
 */

/**
 *
 * @author  James Louis
 */

import java.io.File;

/** The FileNames class contains functions for handling file names. Functions are
 * provided for deriving the names of the namesfile, datafile, and testfile. The
 * DATAFILE option is used for all derivations by determining the root and adding
 * different extensions. If the DATAFILE contains the suffix ".all", it is assumed
 * the datafile conntains all data. The user is allowed to selected a testfile
 * explicitely.
 * @author James Louis Ported to Java. 2/21/2002
 */
public class FileNames {

    /** TRUE if the testfile name should be automatically generated.
     */    
    private boolean suggestTestFile;
    
    /** TRUE if the testfile name is prompted from the user.
     */    
    private boolean testFilePrompted;
    
    /** Rootname of the file structure.
     */    
    private String rootName;
    
    /** Name of the datafile.
     */    
    private String dataFile;
    
    /** Name of the names/schema file.
     */    
    private String namesFile;
    
    /** Name of the test file.
     */    
    private String testFile;
    
    /** Stem name for output production.
     */    
    private String dumpStem;
    
    /** Instance of GetEnv used to retrieve environment variables.
     */    
    private GetEnv getEnv;
    
    /** Help string for datafile names.
     */    
    private String dataFileHelp = "Datafile containing instances.  A '.data' is "
    +"appended if no suffix is given.  A '.all' suffix implies that "
    +"there is no test set available";
    
    /** Help string for names/schema file names.
     */    
    private String namesFileHelp = "Namesfile describing how to parse the "
    +" datafile.  A '.names' is appended if no suffix is given.";
    
    /** Help string for testfile names.
     */    
    private String testFileHelp = "Testfile containing instances.  A '.test' is "
    +"appended if no suffix is given.";
    
    /** Help string for dumpfile names.
     */    
    private String dumpStemHelp = "Name of a file stem for dumping instances.";

    
    /** Creates a new instance of FileNames */
    public FileNames() {
        getEnv = new GetEnv();
        dataFile = getEnv.get_option_string_no_default("DATAFILE", dataFileHelp);
        suggestTestFile = true;
        testFilePrompted = false;
        if (dataFile.indexOf(".") > -1) {
            rootName = dataFile.substring(0,dataFile.indexOf("."));
            if (dataFile.substring(rootName.length()) == ".all")
                suggestTestFile = false;
        }
        else {
            rootName = dataFile;
            dataFile += ".data";
        }
        
        dumpStem = Globals.EMPTY_STRING;
        
        names_file(); // prompt for it now because it's related.

    }

    /** Returns the name of the datafile.
     * @return The name of the datafile.
     */    
    public String data_file() {
        return dataFile;
    }
    
    /** Returns the rootname of the files contianing data used for trainging.
     * @return The basename of the files used for training.
     */    
    public String root_name() {
        return rootName;
    }
    
    /** Returns the name of the namesfile used. If ".names" extension is missing it
     * is automatically added.
     *
     * @return The name of the namesfile.
     */    
    public String names_file() {
        if ((namesFile == Globals.EMPTY_STRING)||(namesFile == null)) {
            namesFile = getEnv.get_option_string("NAMESFILE", rootName + ".names",
            namesFileHelp, true);
            if (namesFile.indexOf(".") <= -1)
                namesFile += ".names";
        }
        return namesFile;
    }
    
    /** Returns the name of the testfile used for testing. If the name does not end with
     * ".test" it is automatically added.
     * @param abortOnEmpty If TRUE, this function will display an error message if there is no testfile
     * name specified.
     * @return The name of the testfile.
     */    
    public String test_file(boolean abortOnEmpty) {
        if (!testFilePrompted) {
            // NOTE: use " " instead of "" for the suggested test file name
            // to not suggest a test file.  Otherwise we'll be asking for a
            // nuisance option with no default value, which is illegal.
            String suggestedTestName;
            if(suggestTestFile) {
                suggestedTestName = rootName + ".test";
                File file = new File(suggestedTestName);
                if (!file.exists())
                    suggestedTestName = " ";
            }
            else
                suggestedTestName = " ";
            
            testFile = getEnv.get_option_string("TESTFILE", suggestedTestName,
            testFileHelp, true);
            if(testFile == " ")
                testFile = Globals.EMPTY_STRING;
            if (testFile != Globals.EMPTY_STRING && testFile.indexOf(".") <= -1)
                testFile += ".test";
            
            if(testFile != Globals.EMPTY_STRING && !suggestTestFile)
                Error.err("Warning: using DATAFILE with .all suffix.\n"
                +"   If the TESTFILE instances overlap, the error estimate"
                +" is invalid!\n");
            
            testFilePrompted = true;
        }
        
        if (testFile == Globals.EMPTY_STRING && abortOnEmpty)
            Error.fatalErr("FileNames.test_file: No TESTFILE specified");
        return testFile;
    }
    
    /** Returns the stem of the dumpfile name.
     * @return The stem of the dumpfile name.
     */    
    public String dump_stem() {
        if(dumpStem == Globals.EMPTY_STRING)
            dumpStem = getEnv.get_option_string_no_default("DUMPSTEM", dumpStemHelp);
        return dumpStem;
    }
    
    /** Strips all leading directories by searching for the last file separator
     * character.
     *
     * @param file The path of the file to be stripped.
     * @return The file name without directory path.
     */    
    public String base_name(String file) {
        int pos = file.lastIndexOf(System.getProperty("file.separator"));
        if (pos <= -1)
            return file;
        else
            return file.substring(pos + 1);
    }
    
    /** Returns the name of the testfile used for testing. If the name does not end with
     * ".test" it is automatically added.
     * @return The name of the testfile.
     */    
    public String test_file() {
        return test_file(true);
    }
    
}
