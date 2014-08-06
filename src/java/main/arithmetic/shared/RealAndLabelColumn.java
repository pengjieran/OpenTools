package arithmetic.shared;
import java.util.Arrays;

/** The RealAttributeColumn class provides a array representation for a real
 * column (real attribute, label and weight).  The array representation has
 * better data locality than a doubly linked instance list and thus is more
 * efficient for discretization. RealAttributeColumn includes the AttrInfo for
 * the real attribute and the label.  However, these are const references,
 * therefore whoever owns these AttrInfos must not delete them until the
 * RealAttribueColumn referring to them has been deleted.			<P>
 *
 * The size of the data array must not exceed that given in the constructor.
 * Because the class is used for transforming already existing instance lists
 * (see InstanceList.transpose), the maximum size can be estimated well enough
 * in advance.											<P>
 * Zero-weight instances added to the column are disregarded. Since an
 * instance with zero weight is equivalent to no instances, such instances
 * should be disregarded by tasks that use the RealAndLabelColumn (like
 * discretization and the like).
 *
 * @author James Louis 10/12/2001 Converted to Java
 * @author Eric Bauer 6/16/97 Added zero-weight instance blocking
 * @author Cliff Brunk 4/21/97 Added ability to generate an instance list from column
 * @author Alex Kozlov 9/04/96 Initial revision (.h, .c)
 */


public class RealAndLabelColumn {
    
    private AttrLabelPair[] known;
    private AttrLabelPair[] unknown;
    private int knownCount;
    private double knownWeight;
    private int unknownCount;
    private double unknownWeight;
    private boolean ordered;
    private RealAttrInfo attrInfo;
    private NominalAttrInfo labelInfo;
    private double[] knownLabelWeights;
    private double[] unknownLabelWeights;
    
    /** RealAndLabelColumn constructor for labelled columns.
     * @param length The number of real/label pairs to be stored.
     * @param labelCount Number of possible labels.
     * @param rai Attribute information for the real attribute stored.
     * @param nai Attribute information for the label attribute.
     */
    public RealAndLabelColumn(int length, int labelCount,
    RealAttrInfo rai,
    NominalAttrInfo nai) {
        ordered = true;
        attrInfo = rai;
        labelInfo = nai;
        knownCount = 0;
        knownWeight = 0;
        knownLabelWeights = new double[labelCount];
        unknownCount = 0;
        unknownWeight = 0;
        unknownLabelWeights = new double[labelCount];
        // Can we get rid of the extra malloc/new?
        if (!(length == 0)) {
            known = new AttrLabelPair[length];
            for(int j =0; j < known.length; j++) known[j] = new AttrLabelPair();
            unknown = new AttrLabelPair[length];
            for(int j =0; j < unknown.length; j++) unknown[j] = new AttrLabelPair();
        } else {
            known = null;
            unknown = null;
        }
    }
    
    /** RealAndLabelColumn constructor for unlabelled columns
     *
     * @param length The number of real values to be stored.
     * @param rai Attribute information for the real attribute stored.
     */
    public RealAndLabelColumn(int length, RealAttrInfo rai) {
        ordered = true;
        attrInfo = rai;
        labelInfo = null;
        knownCount = 0;
        knownWeight = 0;
        knownLabelWeights = new double[0];
        unknownCount = 0;
        unknownWeight = 0;
        unknownLabelWeights = new double[0];
        // Can we get rid of the malloc/new here?
        if (!(length == 0)) {
            known = new AttrLabelPair[length];
            for(int j =0; j < known.length; j++) known[j] = new AttrLabelPair();
            unknown = new AttrLabelPair[length];
            for(int j =0; j < unknown.length; j++) unknown[j] = new AttrLabelPair();
        } else {
            known = null;
            unknown = null;
        }
    }
    
    /** Returns the information on the real attribute stored in this column.
     * @return The information on the real attribute stored in this column.
     */
    public RealAttrInfo attr_info() {
        return attrInfo;
    }
    
    /** Adds a "unknown" value, label and weight.
     * @param label The label value associated with the unknown value.
     * @param weight The weight of this real/label pair.
     */
    public void add_unknown(int label,double weight) {
        if (!MLJ.approx_equal((float)weight, (float)0.0)) {
            //      DBG(ASSERT(labelInfo != NULL));
            //      DBG(ASSERT(unknownCount < unknown->size()));
            //      ASSERT(0 <= label && label < unknownLabelWeights.size());
            AttrLabelPair alp = unknown[unknownCount];
            //@@ storing UNKNOWN_STORED_REAL_VAL rather than
            //   the value that was seen in the data
            alp.value = (float)Globals.UNKNOWN_STORED_REAL_VAL;
            alp.label = label;
            alp.weight = weight;
            unknownLabelWeights[label] += weight;
            unknownCount++;
            unknownWeight += weight;
        }
    }
    
    /** Adds a "unknown" value and weight.
     * @param weight The weight of this real value.
     */
    public void add_unknown(double weight) {
        if (!MLJ.approx_equal((float)weight, (float)0.0)) {
            //      DBG(ASSERT(labelInfo == NULL));
            //      DBG(ASSERT(unknownCount < unknown->size()));
            //@@ storing UNKNOWN_STORED_REAL_VAL and  UNKNOWN_NOMINAL_VAL
            //   rather than the values that wer seen in the data
            unknown[unknownCount].value =
            (float)Globals.UNKNOWN_STORED_REAL_VAL;
            unknown[unknownCount].label = Globals.UNKNOWN_NOMINAL_VAL;
            unknown[unknownCount].weight = weight;
            unknownCount++;
            unknownWeight += weight;
        }
    }
    
    /** Adds a known real value, label and weight.
     * @param value Real value to be stored.
     * @param label Label associated with this real value.
     * @param weight The weight of this real/label pair.
     */
    public void add_known(float value, int label, double weight) {
        if (!MLJ.approx_equal((float)weight, (float)0.0)) {
            //      DBG(ASSERT(labelInfo != null));
            //      DBG(ASSERT(knownCount < known->size()));
            //      ASSERT(0 <= label && label < knownLabelWeights.size());
            AttrLabelPair alp = known[knownCount];
            alp.value = value;
            alp.label = label;
            alp.weight = weight;
            knownLabelWeights[label] += weight;
            knownCount++;
            knownWeight += weight;
            ordered = false;
        }
    }
    
    /** Adds a known real value and weight.
     * @param value Real value to be stored.
     * @param weight The weight of this real value.
     */
    public void add_known(float value, double weight) {
        if (!MLJ.approx_equal((float)weight, (float)0.0)) {
            //      DBG(ASSERT(labelInfo == NULL));
            //      DBG(ASSERT(knownCount < known->size()));
            // @@ This should be DBG with routines to make sure that the
            // @@ label isn't used when we used unlabelled Real Columns
            // @@ storing UNKNOWN_NOMINAL_VAL rather than the value
            // @@ that was seen in the data
            known[knownCount].label = Globals.UNKNOWN_NOMINAL_VAL;
            known[knownCount].value = value;
            known[knownCount].weight = weight;
            knownCount++;
            knownWeight += weight;
            ordered = false;
        }
    }
    
    
    /** Returns the weight of the known values.
     * @return The weight of the known values.
     */
    public double known_weight() {
        return knownWeight;
    }
    
    /** Returns the real/label pair at the specified index number.
     * @param index The index to be returned.
     * @return The real/label pair.
     */
    public AttrLabelPair index(int index) {
        return /*(AttrLabelPair)*/known[index];
    }
    
    /** Returns the count of known real values.
     * @return The count of known real values.
     */
    public int known_count() {
        return knownCount;
    }
    
    /** Returns the total weight of all values.
     * @return The total weight of all values.
     */
    public double total_weight() {
        return knownWeight + unknownWeight;
    }
    
    /** Fills the fields of the splitAndLabelDist array, returns the number of
     * used labels for known instances. For the meaning of splitAndLabelDist and
     * labelDist see entropy.java.
     * @return The number of labels used.
     * @param splitAndLabelDist The split and label distribution to be filled.
     * @param labelDist The label to be filled.
     */
    public int init_dist_counts(double[][] splitAndLabelDist,
    double[] labelDist) {
        if (labelInfo == null)
            Error.fatalErr("RealAndLabelColumn::init_dist_counts can only be used with a "+
            "labelled RealAndLabelColumn");
        
        int usedLabels = 0;
        
        labelDist[Globals.LEFT_NODE]    = 0;
        labelDist[Globals.RIGHT_NODE]   = knownWeight;
        labelDist[Globals.UNKNOWN_NODE] = unknownWeight;
        
        for (int labelVal = 0;
        labelVal < knownLabelWeights.length;
        labelVal++) {
            splitAndLabelDist[labelVal][Globals.LEFT_NODE] = 0;
            double knownLabelWeight = knownLabelWeights[labelVal];
            splitAndLabelDist[labelVal][Globals.RIGHT_NODE] = knownLabelWeight;
            if (!(knownLabelWeight == 0))
                usedLabels++;
            splitAndLabelDist[labelVal][Globals.UNKNOWN_NODE] =
            unknownLabelWeights[labelVal];
        }
        
        return usedLabels;
    }
    
    /** Return the number of possible labels.
     * @return The number of possible labels.
     */
    public int label_count() {
        return knownLabelWeights.length;
    }
    
    /** Sorts the real/label pairs.
     */
    public void sort() {
        known = (AttrLabelPair[])MLJArray.truncate(knownCount,known);
        Arrays.sort(known);
        ordered = true;
    }
    
}