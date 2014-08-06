package arithmetic.shared;
import java.io.Writer;

/** A base class for inducers. The main reason for this base class, which
 * we don't expect users to use much, is that ExternalInducers do not build a
 * data structure in memory, and thus we must provide a composite operation
 * train_and_test. The two derived classes that will actually be used by users
 * are Inducer and ExternalInducer.<P>
 *
 * @author James Louis	12/08/2000	Porting to Java.
 * @author Yeogirl Yun	7/4/95	Added copy constructor.
 * @author Ronny Kohavi	10/5/93	Initial revision (.c, .h) based on
 * Inducer.*
 */
abstract public class BaseInducer {
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int CONST_INDUCER         = Class_Id.INDUCER_ID_BASE +  1;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int TDDT_INDUCER          = Class_Id.INDUCER_ID_BASE +  2;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int TABLE_INDUCER         = Class_Id.INDUCER_ID_BASE +  3;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int C45_INDUCER           = Class_Id.INDUCER_ID_BASE +  4;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int FSS_INDUCER           = Class_Id.INDUCER_ID_BASE +  5;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int OODG_INDUCER          = Class_Id.INDUCER_ID_BASE +  6;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int NAIVE_BAYES_INDUCER   = Class_Id.INDUCER_ID_BASE +  7;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int NULL_INDUCER          = Class_Id.INDUCER_ID_BASE +  8;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int IB_INDUCER            = Class_Id.INDUCER_ID_BASE +  9;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int ONER_INDUCER          = Class_Id.INDUCER_ID_BASE + 10;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int DISC_TAB_INDUCER      = Class_Id.INDUCER_ID_BASE + 11;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int DISC_NB_INDUCER       = Class_Id.INDUCER_ID_BASE + 12;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int DF_INDUCER            = Class_Id.INDUCER_ID_BASE + 13;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int LIST_HOODG_INDUCER    = Class_Id.INDUCER_ID_BASE + 14;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int WINNOW_INDUCER        = Class_Id.INDUCER_ID_BASE + 15;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int ID3_INDUCER           = Class_Id.INDUCER_ID_BASE + 16;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int PERCEPTRON_INDUCER    = Class_Id.INDUCER_ID_BASE + 17;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int LAZY_DT_INDUCER       = Class_Id.INDUCER_ID_BASE + 18;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int HOODG_INDUCER         = Class_Id.INDUCER_ID_BASE + 19;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int PERF_EST_INDUCER      = Class_Id.INDUCER_ID_BASE + 20;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int BAGGING_INDUCER       = Class_Id.INDUCER_ID_BASE + 21;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int ENTROPY_ODG_INDUCER   = Class_Id.INDUCER_ID_BASE + 22;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int LIST_ODG_INDUCER      = Class_Id.INDUCER_ID_BASE + 23;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int C45R_INDUCER          = Class_Id.INDUCER_ID_BASE + 24;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int ORDER_FSS_INDUCER     = Class_Id.INDUCER_ID_BASE + 25;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int DISC_SEARCH_INDUCER   = Class_Id.INDUCER_ID_BASE + 26;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int C45AP_INDUCER         = Class_Id.INDUCER_ID_BASE + 27;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int CatDT_INDUCER         = Class_Id.INDUCER_ID_BASE + 28;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int CF_INDUCER            = Class_Id.INDUCER_ID_BASE + 29;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int TABLE_CAS_INDUCER     = Class_Id.INDUCER_ID_BASE + 30;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int WEIGHT_SEARCH_INDUCER = Class_Id.INDUCER_ID_BASE + 31;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int T2_INDUCER            = Class_Id.INDUCER_ID_BASE + 33;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int PROJECT_INDUCER       = Class_Id.INDUCER_ID_BASE + 32;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int STACKING_INDUCER      = Class_Id.INDUCER_ID_BASE + 34;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int SGI_DT_INDUCER        = Class_Id.INDUCER_ID_BASE + 35;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int FCF_INDUCER           = Class_Id.INDUCER_ID_BASE + 36;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int AM_INDUCER            = Class_Id.INDUCER_ID_BASE + 37;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int PEBLS_INDUCER         = Class_Id.INDUCER_ID_BASE + 38;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int RIPPER_INDUCER        = Class_Id.INDUCER_ID_BASE + 39;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int OC1_INDUCER           = Class_Id.INDUCER_ID_BASE + 40;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int COODG_INDUCER         = Class_Id.INDUCER_ID_BASE + 41;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int CN2_INDUCER           = Class_Id.INDUCER_ID_BASE + 42;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int AHA_IB_INDUCER        = Class_Id.INDUCER_ID_BASE + 43;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int CART_INDUCER          = Class_Id.INDUCER_ID_BASE + 44;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int C50_INDUCER           = Class_Id.INDUCER_ID_BASE + 45;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int ODT_INDUCER           = Class_Id.INDUCER_ID_BASE + 46;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int CLUSTER_INDUCER       = Class_Id.INDUCER_ID_BASE + 47;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int BOOSTER_INDUCER       = Class_Id.INDUCER_ID_BASE + 48;
    
    /** Class identifier used for class_id function.
     * @deprecated The class_id method should be replaced with Java's
     * instanceof operator.
     */
    public static int DDT_INDUCER           = Class_Id.INDUCER_ID_BASE + 49;
    
    /** Data set used for training this inducer.
     */
    protected InstanceList TS = null;
    
    /** A description of this inducer.
     */
    private String dscr;
    
    /** Logging options for this class.
     */
    protected LogOptions logOptions = new LogOptions();
    
    /** Instance of the GetEnv class used for accessing environment variables
     * and options.
     */    
    protected GetEnv getEnv = new GetEnv();;
    
    /** Sets the logging level for this object.
     * @param level	The new logging level.
     */
    public void set_log_level(int level){logOptions.set_log_level(level);}
    
    /** Returns the logging level for this object.
     * @return The logging level for this object.
     */
    public int  get_log_level(){return logOptions.get_log_level();}
    
    /** Sets the stream to which logging options are displayed.
     * @param strm	The stream to which logs will be written.
     */
    public void set_log_stream(Writer strm)
    {logOptions.set_log_stream(strm);}
    
    /** Returns the stream to which logs for this object are written.
     * @return The stream to which logs for this object are written.
     */
    public Writer get_log_stream(){return logOptions.get_log_stream();}
    
    /** Returns the LogOptions object for this object.
     * @return The LogOptions object for this object.
     */
    public LogOptions get_log_options(){return logOptions;}
    
    /** Sets the LogOptions object for this object.
     * @param opt	The new LogOptions object.
     */
    public void set_log_options(LogOptions opt)
    {logOptions.set_log_options(opt);}
    
    /** Sets the logging message prefix for this object.
     * @param file	The file name to be displayed in the prefix of log messages.
     * @param line	The line number to be displayed in the prefix of log messages.
     * @param lvl1 The log level of the statement being logged.
     * @param lvl2	The level of log messages being displayed.
     */
    public void set_log_prefixes(String file, int line,int lvl1, int lvl2)
    {logOptions.set_log_prefixes(file, line, lvl1, lvl2);}
    
    /** Constructs an inducer with the description specified.
     * @param description String describing the BaseInducer instance.
     */
    public BaseInducer(String description) {
        dscr = description;
        TS = null;
        logOptions = new LogOptions();
    }
    
    /** Copy constructor. The new inducer will have the same logging options
     * as the source inducer. The training data set(TS) is set to null.
     * @param source The original inducer that is being copied.
     */
    public BaseInducer(BaseInducer source) {
        dscr = source.dscr;
        TS = null;
        logOptions = new LogOptions();
    }
    
    /** Reads the training set from files. The default file extensions (.data,
     * .names) are used for the data and names files. This method should be called
     * before any other methods that requires the training set. This method also
     * overwrites old data if called more than once. The assign_data method is
     * called so subclasses that alter the assign_data method will also change
     * this method. Inducer.read_data() has the complexity of
     * InstanceList(String, String, String). Uses Globals.DEFAULT_TEST_EXT for the
     * extension of the test file and Globals.DEFAULT_DATA_EXT for the extension of
     * the data file.
     *
     * @param file The name of the names, data, and test files without the
     * the file extensions.
     * @param namesExtension	The extension used for the schema file.
     */
    public void read_data(String file,
    String namesExtension) {
        String dataExtension = Globals.DEFAULT_DATA_EXT;
        read_data(file,namesExtension,dataExtension);
    }
    
    /** Reads the training set from files. The default file extensions (.data,
     * .names) are used for the data and names files. This method should be called
     * before any other methods that requires the training set. This method also
     * overwrites old data if called more than once. The assign_data method is
     * called so subclasses that alter the assign_data method will also change
     * this method. Inducer.read_data() has the complexity of
     * InstanceList(String, String, String). Uses Globals.DEFAULT_TEST_EXT for the
     * extension of the test file.
     *
     * @param file The name of the names, data, and test files without the
     * the file extensions.
     * @param namesExtension	The extension used for the schema file.
     * @param dataExtension	The extension used for the data file.
     */
    public void read_data(String file,
    String namesExtension,
    String dataExtension) {
        InstanceList newTS = new InstanceList(file,namesExtension,dataExtension);
        assign_data(newTS);
    }
    
    /** Reads the training set from files. The default file extensions (.data,
     * .names) are used for the data and names files. This method should be called
     * before any other methods that requires the training set. This method also
     * overwrites old data if called more than once. The assign_data method is
     * called so subclasses that alter the assign_data method will also change
     * this method. Inducer.read_data() has the complexity of
     * InstanceList(String, String, String). Uses Globals.DEFAULT_TEST_EXT for the
     * extension of the test file, Globals.DEFAULT_DATA_EXT for the extension of
     * the data file, and Globals.DEFAULT_NAMES_EXT for the extension of the schema
     * file.
     *
     *
     * @param file The name of the names, data, and test files without the
     * the file extensions.
     */
    public void read_data(String file) {
        InstanceList newTS = new InstanceList(file);
        //      newTS.display(false);
        assign_data(newTS);
    }
    
    /** Sets the dataset for this inducer to the specified InstanceList. The
     * inducer takes ownership of the new training set, so the reference passed in
     * will be reset to null.
     * @param newTS The InstanceList containing the new training data set.
     * @return The previous training data set.
     */
    public InstanceList assign_data(InstanceList newTS) {
        InstanceList oldTS = TS;
        TS = newTS;
        newTS = null;
        //DBG(OK());
        return oldTS;
    }
    
    /** Returns an instance list corresponding to the training data set.
     * @return The current training data set.
     */
    public InstanceList instance_list() {
        has_data();   //checks that TS is allocated
        return TS;
    }
    
    /** Returns true if and only if the class has a valid training
     * data set.
     * @return True if there is a valid training set, False otherwise.
     */
    public boolean has_data() {
        return has_data(true);
    }
    /** Returns true if and only if the inducer has been assigned a data set.
     * @param fatalOnFalse If set to true, this method will display an error
     * message if there is not a valid training data set.
     * @return True if there is a valid training set, False otherwise.
     */
    public boolean has_data(boolean fatalOnFalse) {
        if(fatalOnFalse && TS == null)
            Error.err("BaseInducer.has_data: Training data has not been set"
            +" -->fatal_error");
        return TS != null;
    }
    
    /** Returns the class id of this of this inducer.
     * @deprecated This method should be replaced with Java's instanceof operator.
     * @return Integer assigned to this inducer.
     */
    abstract public int class_id();
    //This was the code that was here
    //public int class_id()
    //{return 0;  /*(????)*/ }
    
    /** Returns the description of this inducer.
     * @return A reference to the String containing the description of this
     * inducer.
     */
    public String description(){return dscr;}
    
    /** Releases and returns the instance list. The training data set will now be
     * set to null.
     * @return The current training data set.
     */
    public InstanceList release_data() {
        InstanceList instList = TS;
        TS = null;
        return instList;
    }
    
    /** Trains and tests the inducer on the given data sets. This class is
     * overridden in subclasses to the specific induction algorithm.
     * @param trainingSet The data set used to train this inducer.
     * @param testSet     The data set used to test this inducer.
     * @return The probability of incorrect test responses. Possible values
     * are 0.0 to 1.0.
     */
    
    abstract public double train_and_test(InstanceList trainingSet,
    InstanceList testSet);
    //   public double train_and_test(InstanceList trainingSet,
    //                             InstanceList testSet){return 0.0;}
    
    
    /** Trains and measures performance on the inducer. Can not be done on a
     * BaseInducer. This method first checks if testing can be done before
     * attempting to do so. For a BaseInducer this function always displays
     * an error message.
     * @param trainingSet The data set used to train this inducer.
     * @param testSet     The data set used to test this inducer.
     * @return A CatTestResult class containing the results after perfecting.
     */
    public CatTestResult train_and_perf(InstanceList trainingSet,
    InstanceList testSet) {
        if(supports_full_testing())
            Error.err("BaseInducer::train_and_perf: inducer supports full"
            +" testing but this function was never defined"
            +" -->fatal_error");
        else
            Error.err("BaseInducer::train_and_perf: not supported for this"
            +" inducer -->fatal_error");
        return null;
    }
    
    /** Returns a boolean representing wether this inducer can test itself.
     * Always returns false for a BaseInducer.
     * @return A boolean value. Only possible value is false.
     */
    public boolean supports_full_testing() {
        return false;
    }
    
    /** Convenience function that reads the training and test files in. The
     * method assumes the use of the default extensions(.names,.data,.test) for
     * these files.
     * @param fileStem The name of the names, data, and test files without the
     * the file extensions.
     * @return The probability of incorrect test responses. Possible values
     * are 0.0 to 1.0.
     */
    public double train_and_test_files(String fileStem) {
        return train_and_test_files(fileStem,Globals.DEFAULT_NAMES_EXT,Globals.DEFAULT_DATA_EXT,Globals.DEFAULT_TEST_EXT);
    }
    
    /** Convenience function that reads the training and test files in. The
     * data and training files use the default extensions(.data,.test).
     * @param fileStem The name of the names, data, and test files without the
     * the file extensions.
     * @param namesExtension The extension used for the names file. Should
     * begin with a period.
     * @return The probability of incorrect test responses. Possible values
     * are 0.0 to 1.0.
     */
    public double train_and_test_files(String fileStem,String namesExtension) {
        return train_and_test_files(fileStem,namesExtension,Globals.DEFAULT_DATA_EXT,Globals.DEFAULT_TEST_EXT);
    }
    
    /** Convenience function that reads the training and test files in. The
     * training file uses the default extension(.test).
     * @param fileStem The name of the names, data, and test files without the
     * the file extensions.
     * @param namesExtension The extension used for the names file. Should
     * begin with a period.
     * @param dataExtension  The extension used for the data file. Should
     * begin with a period.
     * @return The probability of incorrect test responses. Possible values
     * are 0.0 to 1.0.
     */
    public double train_and_test_files(String fileStem,String namesExtension,String dataExtension) {
        return train_and_test_files(fileStem,namesExtension,dataExtension,Globals.DEFAULT_TEST_EXT);
    }
    
    /** Convenience function that reads the training and test files in.
     * @param fileStem The name of the names, data, and test files without the
     * the file extensions.
     * @param namesExtension The extension used for the names file. Should
     * begin with a period.
     * @param dataExtension  The extension used for the data file. Should
     * begin with a period.
     * @param testExtension  The extension used for the test file. Should
     * begin with a period.
     * @return The probability of incorrect test responses. Possible values
     * are 0.0 to 1.0.
     */
    public double train_and_test_files(String fileStem,
    String namesExtension,
    String dataExtension,
    String testExtension) {
        InstanceList trainList = new InstanceList(Globals.EMPTY_STRING, fileStem + namesExtension,
        fileStem + dataExtension);
        InstanceList testList = new InstanceList(trainList.get_schema(),
        trainList.get_original_schema(),
        fileStem + testExtension);
        return train_and_test(trainList, testList);
    }
    
    /** Checks if this BaseInducer can be cast to an Inducer class.
     * @return A boolean representing wether this class can be cast. Always false
     * for BaseInducer.
     */
    public boolean can_cast_to_inducer() {
        return false;
    }
    
    /** Casts this BaseInducer to an Inducer class. Can not be done for a
     * BaseInducer. Always displays an error message.
     * @return A reference to the new Inducer. Always returns null for a
     * BaseInducer class.
     */
    public Inducer cast_to_inducer() {
        Error.err("BaseInducer::cast_to_inducer: Cannot cast"+" -->fatal_error");
        return null;
        //This is the code that was originally here
        //   return (Inducer&)(*(Inducer *)NULL_REF);
    }
    
    /** Checks if this BaseInducer can be cast to an IncrInducer class.
     * @return A boolean representing wether this class can be cast. Always false
     * for BaseInducer.
     */
    public boolean can_cast_to_incr_inducer() {
        return false;
    }
    
    /** Casts this BaseInducer to an IncrInducer class. Can not be done for a
     * BaseInducer. Always displays an error message.
     * @return A reference to the new IncrInducer. Always returns null for a
     * BaseInducer class.
     */
    public IncrInducer cast_to_incr_inducer() {
        Error.err("BaseInducer::cast_to_inducer: Cannot cast"+" -->fatal_error");
        return null;
        //This is the code that was originally here
        //   return (IncrInducer&)(*(IncrInducer *)NULL_REF);
    }
    
    // Weight functions
    
    /** Normalize the instance weights in the training data set. Any subclasses which
     * override this function should call this method.
     * @param normFactor The number by which the weights will be normalized.
     */
    public void normalize_weights(double normFactor) {
        if(has_data())
            TS.normalize_weights(normFactor);
    }
}
