package arithmetic.shared;
import java.io.BufferedWriter;
import java.io.Writer;

/** Abstract base class for Categorizers. Number of categories must be
 * strictly positive (greater than zero). Description cannot be empty or NULL.
 * @author James Louis	2/25/2001	Ported to Java.
 * @author Chia-Hsin Li	11/29/94	Add function building probability
 * distribution array.
 * @author Ronny Kohavi	8/03/93	Initial revision
 */
abstract public class Categorizer extends Globals implements Cloneable {
    /** The Schema of data this Categorizer can categorize. **/
    private Schema schema;
    /** The total weight of instances. **/
    private double totalWeight;
    /** Number of possible categories for labelling. **/
    private int numCat;
    /** Description of this Categorizer. **/
    private String descr;
    /** The distribution of weights. **/
    private double[] distrArray;
    /** The original distribution of weights. **/
    private double[] originalDistr;
    /** Extra text for visualization purposes. **/
    private String extraVizText;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CATEGORIZER_ID_BASE = 0;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_CONST_CATEGORIZER =         CATEGORIZER_ID_BASE +  1;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_MULTITHRESH_CATEGORIZER =   CATEGORIZER_ID_BASE +  2;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_THRESHOLD_CATEGORIZER =     CATEGORIZER_ID_BASE +  3;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_ATTR_CATEGORIZER =          CATEGORIZER_ID_BASE +  4;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_RDG_CATEGORIZER =           CATEGORIZER_ID_BASE +  5;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_BAD_CATEGORIZER =           CATEGORIZER_ID_BASE +  6;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_TABLE_CATEGORIZER =         CATEGORIZER_ID_BASE +  7;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_IB_CATEGORIZER =            CATEGORIZER_ID_BASE +  8;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_LAZYDT_CATEGORIZER =        CATEGORIZER_ID_BASE +  9;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_NB_CATEGORIZER =            CATEGORIZER_ID_BASE + 10;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_PROJECT_CATEGORIZER =       CATEGORIZER_ID_BASE + 11;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_DISC_CATEGORIZER =          CATEGORIZER_ID_BASE + 12;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_ATTR_EQ_CATEGORIZER =       CATEGORIZER_ID_BASE + 13;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_DTREE_CATEGORIZER =         CATEGORIZER_ID_BASE + 14;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_BAGGING_CATEGORIZER =       CATEGORIZER_ID_BASE + 15;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_LINDISCR_CATEGORIZER =      CATEGORIZER_ID_BASE + 16;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_CASCADE_CATEGORIZER =       CATEGORIZER_ID_BASE + 17;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_STACKING_CATEGORIZER =      CATEGORIZER_ID_BASE + 18;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_ATTR_SUBSET_CATEGORIZER =   CATEGORIZER_ID_BASE + 19;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_MULTI_SPLIT_CATEGORIZER =   CATEGORIZER_ID_BASE + 20;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_ONE_R_CATEGORIZER =         CATEGORIZER_ID_BASE + 21;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_CONSTRUCT_CATEGORIZER =     CATEGORIZER_ID_BASE + 22;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_LEAF_CATEGORIZER =          CATEGORIZER_ID_BASE + 24;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_DISC_NODE_CATEGORIZER =     CATEGORIZER_ID_BASE + 25;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_MAJORITY_CATEGORIZER =      CATEGORIZER_ID_BASE + 95;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_ODT_CATEGORIZER =           CATEGORIZER_ID_BASE + 96;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_CLUSTER_CATEGORIZER =       CATEGORIZER_ID_BASE + 97;
    
    /** Class identification number.
     * @deprecated The Java instanceof operator should be used instead of
     * the numerical class identity system. **/
    public static final int CLASS_OPTION_CATEGORIZER =        CATEGORIZER_ID_BASE + 98;
    
    
    
    /** Logging options for this class. **/
    protected LogOptions logOptions = new LogOptions();
    
    /** Sets the logging level for this object.
     * @param level	The new logging level.
     */
    public void set_log_level(int level){logOptions.set_log_level(level);}
    
    /** Returns the logging level for this object.
     * @return The log level for this Object.
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
    
    /** This class has no access to a copy constructor.
     * @param source The Categorizer to be copied.
     */
    private Categorizer(Categorizer source){}
    
    /** This class has no access to an assign method.
     * @param source The Categorizer to be copied into this Categorizer object.
     */
    private void assign(Categorizer source){}
    
    /** Constructor.
     * @param noCat	The number of categories for labelling.
     * @param dscr		The description of this Categorizer object.
     * @param sch		The Schema for the data to be categorized.
     */
    public Categorizer(int noCat, String dscr, Schema sch) {
        numCat = noCat;
        descr = dscr;
        schema = sch;
        distrArray = null;
        originalDistr = null;
        extraVizText = new String("");
        if (numCat <= 0)
            Error.fatalErr("Categorizer::Categorizer: number of categories must be positive (" +
            num_categories() + " is invalid");
        //      if (descr.compareTo ("") == 0)
        if (descr.equals(""))
            Error.fatalErr("Categorizer::Categorizer: empty description");
    }
    
    /** Returns the number of categories.
     * @return The number of categories.
     */
    public int num_categories() {
        return numCat;
    }
    
    /** Returns the description of this Categorizer.
     * @return The description of this Categorizer.
     */
    public String description() {
        return descr;
    }
    
    /** Clones this Categorizer.
     * @return The clone of this Categorizer.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /** Sets the attributes used for this Categorizer. Displays an error message
     * for this Categorizer class or subclass.
     * @param barray	A boolean array representing the attributes. TRUE
     * indicates the attribute should be included in the
     * categorization process.
     */
    public void set_used_attr(boolean[] barray) {
        Error.fatalErr("Categorizer::set_used_attr is not defined for the subclass " +
        this.getClass() .getName());
    }
    
    /** Sets the description of this Categorizer.
     * @param val	The new description.
     */
    public void set_description(String val) {
        descr = val;
    }
    
    /** Checks if this Categorizer has a weight distribution.
     * @return TRUE if there is a weighted distribution for this Categorizer,
     * FALSE otherwise.
     */
    public boolean has_distr() {
        return distrArray != null;
    }
    
    /** Returns the weight distribution for this Categorizer.
     * @return The weight distribution for this Categorizer.
     */
    public double[] get_distr() {
        MLJ.ASSERT(distrArray != null,"Categorizer::get_distr: distrArray is NULL.");
        return distrArray;
    }
    
    /** Returns the total weight of the Instances categorized.
     * @return The total weight of the Instances categorized.
     */
    public double total_weight() {
        if (!has_distr())
            Error.err("Categorizer::total_weight: no distribution-->fatal_error");
        MLJ.ASSERT(totalWeight > -MLJ.realEpsilon,"Categorizer::total_weight: totalWeight <= -MLJ.real_epsilon");
        return totalWeight;
    }
    
    /** Returns the Schema for data to be categorized.
     * @return The Schema for data to be categorized.
     */
    public Schema get_schema() {
        return schema;
    }
    
    /** Sets the original weight distribution to the given distribution.
     * @param dist The new original weight distribution.
     */
    public void set_original_distr(double[] dist) {
        originalDistr =(double[]) dist.clone();
    }
    
    /** Displays the structure of the Categorizer.
     * @param stream	The output stream to be written to.
     * @param dp		The preferences for display.
     */
    abstract public void display_struct(BufferedWriter stream, DisplayPref dp);
    
    /** Categorizes the given Instance.
     * @return The category this Instance is labelled as.
     * @param IRC	The Instance to be categorized.
     */
    abstract public AugCategory categorize(Instance IRC);
    
    /** Returns the CatDist containing the weighted distribution score for the
     * given Instance. Displays an error message for the Categorizer Class.
     * @return The CatDist containing the weighted distribution.
     * @param IRC	The Instance to be scored.
     */
    public CatDist score(Instance IRC) {
        Error.fatalErr("Categorizer does not support scoring");
        return null;
    }
    
    /** Checks if this Categorizer supports scoring.
     * @return FALSE for the Categorizer class.
     */
    public boolean supports_scoring() {
        return false;
    }
    
    /** Builds a weight distribution based on the given InstanceList.
     * @param instList	The InstanceList whose weight distribution is to be
     * calculated.
     */
    public void build_distr(InstanceList instList) {
        Schema schema = instList.get_schema();
        totalWeight = instList.total_weight();
        int numLabelValue = schema.num_label_values();
        distrArray = null;
        distrArray = new double[numLabelValue + 1];
        for(int labelCount = 0 ; labelCount < numLabelValue; labelCount++)
            distrArray[labelCount] =
            instList.counters() .label_counts() [labelCount];
    }
    
    /** Sets the weight distribution to the given distribution.
     * @param val The new distribution.
     */
    public void set_distr(double[] val) {
        MLJ.ASSERT(val != null,"Categorizer::set_distr: val is NULL.");
        distrArray = null;
        distrArray = new double[val.length];
        totalWeight = 0.0;
        for(int i = 0 ; i < val.length ; i++) {
            totalWeight += val[i];
            distrArray[i] = val[i];
        }
    }
    
}
