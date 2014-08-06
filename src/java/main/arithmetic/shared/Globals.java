package arithmetic.shared;
import java.io.OutputStreamWriter;
import java.io.Writer;

/** This class contains the settings used globally in MLJ.
 * @author James Louis 4/15/2002 Java implementation.
 */
public class Globals {
    /** Default datafile extension.
     */    
    public static final String DEFAULT_DATA_EXT  = ".data";
    /** Default namesfile extension.
     */    
    public static final String DEFAULT_NAMES_EXT = ".names";
    /** Default testfile extension.
     */    
    public static final String DEFAULT_TEST_EXT = ".test";
    /** A universal reference to an empty string.
     */    
    public static final String EMPTY_STRING = "";
    /** Single quote string.
     */    
    public static final String SINGLE_QUOTE = "'";
    
    /** The instancelist used throughout the MLJ library.
     */    
    public static final InstanceList TS = null;
    //   public static final double REAL_EPSILON = 2.22204e-16;
    //   public static final double STORED_REAL_EPSILON = 1.1920928955078e-7;
    //   public static final double clampingEpsilon = REAL_EPSILON * 10;
    
    //for use in AugCategory
    //   public static int UNKNOWN_CATEGORY_VAL = -1;
    /** The integer value representing unknown category values.
     */    
    public static int UNKNOWN_CATEGORY_VAL = 0;
    /** The maximum number of categories.
     */    
    public static int MAX_NUM_CATEGORIES = 1000000;
    
    /** The double value indicating an undefined real.
     */    
    public static double UNDEFINED_REAL =Double.MIN_VALUE;
    /** The integer value indicating an undefined integer.
     */    
    public static int UNDEFINED_INT  =Integer.MIN_VALUE;
    /** The float value indicating an undefined stored real.
     */    
    public static float UNKNOWN_STORED_REAL_VAL =Float.MIN_VALUE;
    
    /** The maximum number for a stored real value.
     */    
    public static float STORED_REAL_MAX =Float.MAX_VALUE;
    
    //for use in InstanceList
    /** The index value at which actual category values begin.
     */    
    public static int FIRST_CATEGORY_VAL = UNKNOWN_CATEGORY_VAL + 1;
    
    /** The value for unknown nodes.
     */    
    public static final int UNKNOWN_NODE = UNKNOWN_CATEGORY_VAL;
    /** The index value for the left node in a tree.
     */    
    public static final int LEFT_NODE = FIRST_CATEGORY_VAL;
    /** The index value for the right node in a tree.
     */    
    public static final int RIGHT_NODE = FIRST_CATEGORY_VAL+1;
    
    /** The index value for unknown nominal values.
     */    
    public static final int UNKNOWN_NOMINAL_VAL = UNKNOWN_CATEGORY_VAL;
    /** The index value for the first actual nominal value.
     */    
    public static final int FIRST_NOMINAL_VAL = UNKNOWN_NOMINAL_VAL + 1;	//ADDED BY JL
    /** The string used to represent unknown values.
     */    
    public static final String UNKNOWN_VAL_STR = "?";
    /** The AugCategory representing unknown values.
     */    
    public static final AugCategory UNKNOWN_AUG_CATEGORY = new AugCategory(1,"UNKNOWN_AUG_CATEGORY");
    
    
    /** A universal reference to a BadCategorizer instance.
     */    
    public static BadCategorizer badCategorizer = new BadCategorizer();
    
    //for use in CatTestResult
    /** The Z value for determining confidence values.
     */    
    public static double CONFIDENCE_INTERVAL_Z; // value such that area under standard
    // normal curve going left & right Z,
    // has CONFIDENCE_INTERVAL_PROBABILIT
    
    /** TRUE if debug statements are to be displayed, FALSE otherwise.
     */    
    public static boolean DBG = false;
    
    //for use in SearchDispatch, found in search_enum
    //obs public static SearchMethod DEFAULT_SEARCH_METHOD;
    /** The default search method for search inducers.
     */    
    public static byte DEFAULT_SEARCH_METHOD;
    /** Default for how to show performance evaluations.
     */    
    public static byte DEFAULT_SHOW_TEST_SET_PERF;
    /** Default evaluation limit.
     */    
    public static int DEFAULT_EVAL_LIMIT;
    /** The default maximum number of non-improving expansions in a best first search.
     */    
    public static int DEFAULT_MAX_STALE = 5;
    /** The default value for expansion improvement evaluations during simulated
     * annealing.
     */    
    public static double DEFAULT_EPSILON;
    /** The default maximum number of evalutions in a state space search.
     */    
    public static int DEFAULT_MAX_EVALS;
    /** The default minimum number of expasion evalutations.
     */    
    public static int DEFAULT_MIN_EXP_EVALS;
    /** Default lambda value for simulated annealing.
     */    
    public static double DEFAULT_LAMBDA;
    /** Default search random number seed.
     */    
    public static int DEFAULT_SAS_SEED;
    /** Help string for the SHOW_TEST_SET_PERF option.
     */    
    public static String SHOW_TEST_SET_PERF_HELP;
    
    /** The output stream writer for MLJ.
     */    
    public static Writer Mcout = new OutputStreamWriter(System.out);
    /** The error output stream for MLJ.
     */    
    public static Writer Mcerr = new OutputStreamWriter(System.err);
    
    /** The name for the options file.
     */    
    public static String optionsFileName="";
    /** The optionserver used to read options.
     */    
    public static OptionServer optionServer = new OptionServer();
    
    /** The value used for undefined varience values.
     */    
    public static double UNDEFINED_VARIANCE = Double.MAX_VALUE;
    /** Maximum value for real values.
     */    
    public static double REAL_MAX = Double.MAX_VALUE;
    
    //for FSSState
    /** TRUE if names are to be displayed in FSS.
     */    
    public static boolean DISPLAY_NAMES= false;
}
