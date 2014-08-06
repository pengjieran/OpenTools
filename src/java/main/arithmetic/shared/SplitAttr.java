package arithmetic.shared;

/** A class for determining, holding, and returning the information associated with
 * an attribute split.
 *
 */
public class SplitAttr extends SplitScore{
    //ENUM
    /** SplitTypeEnum value.
     */
    static final public int noReasonableSplit = 1;		//SplitType enum
    /** SplitTypeEnum value.
     */
    static final public int realThresholdSplit = 2;		// "
    /** SplitTypeEnum value.
     */
    static final public int multiRealThresholdSplit = 3;	// "
    /** SplitTypeEnum value.
     */
    static final public int nominalSplit = 4;			// "
    /** SplitTypeEnum value.
     */
    static final public int partitionSplit = 5;			// "
    //END ENUM
    
    /** Names of SplitTypeEnum values.
     */
    public static String[] splitTypeEnum ={" ","no reasonable split","real threshold split",
    "multi real threshold split","nominal split","partition split"};
    
    private class TypeInfo {
        /** The partition for the split.
         */
        public int[] partition; // used in partitionSplit
        /** The threshold for the split.
         */
        public double theThreshold;
        /** Other threholds possible for the split.
         */
        public double[] thresholds;
        // any other info we need
    }
    
    private int attributeNum;
    private TypeInfo typeInfo;
    private double mdlPenalty;
    private boolean existSplit;
    private int splitType;
    private boolean penalizeByMDL;
    private int realValuedSplitIndex;
    
    /** Constructor.
     */
    public SplitAttr() {
        penalizeByMDL = false;
        attributeNum = Globals.UNDEFINED_INT;
        typeInfo = new TypeInfo();
        reset();
    }
    
    /** Returns the type value of this SplitAttr.
     * @see #noReasonableSplit
     * @see #realThresholdSplit
     * @see #multiRealThresholdSplit
     * @see #nominalSplit
     * @see #partitionSplit
     * @return The type value of this attribute.
     */
    public int split_type(){ return splitType; }
    
    /** Returns the mutual information. The mutual information must be >= 0.
     * @param normalize TRUE if the mutual info is to be normalized, FALSE otherwise.
     * @param penalize TRUE if the mutual info should be penalized, FALSE otherwise.
     * @return The mutual information for this attribute split.
     */
    public double get_mutual_info(boolean normalize, boolean penalize) {
        //double result = super.get_mutual_info(false);
        double result = super.get_unnormalized_mutual_info(); // Normalize later.
        if (split_type() == realThresholdSplit ||
        split_type() == multiRealThresholdSplit) {
            // @@ should we penalize multithresholds more? probably so
            // @@ penalize by (numSplits-1)*mdlPenalty
            if (penalize)
                result = Math.max(0.0, result - mdlPenalty);
        }
        if (normalize)
            result = normalize_by_num_splits(result);
        // Result right now >=0, but this is if we decide to avoid
        // the max a few lines above and be like C4.5
        // if (result < 0) {
        //    if (!mlc.approx_equal(result, 0)) {
        //	 ibid->existSplit = FALSE;
        //	 ibid->splitType = noReasonableSplit;
        //  }
        //   result = 0;
        // }
        return result;
    }
    
    /** Returns TRUE if there is a split stored in this SplitAttr.
     * @return Returns TRUE if the SplitAttr contains a split, FALSE otherwise.
     */
    public boolean exist_split(){return existSplit;}
    
    /** Sets if the split should be penalized by minimum description length.
     * @param choice TRUE if penalizing should occur, FALSE otherwise.
     */
    public void set_penalize_by_mdl(boolean choice) {penalizeByMDL = choice;}
    
    /** Helper function to do all processing for real thresholds. When a split is found,
     * this also determines and stores the threshold, the mutual info, the cond info,
     * and the mdl penalty for cost of storing the threshold.
     *
     * @param column The column of real values for this attribute and their associated label values.
     * @param attrNum The number of the attribute.
     * @param minSplit The minimum split value.
     * @param smoothInst The instance to be smoothed towards.
     * @param smoothFactor The factor by which real values are smoothed.
     * @return A split threshold for a real valued attribute.
     */
    public boolean make_real_split(RealAndLabelColumn column, int attrNum,
    double minSplit, int smoothInst,
    double smoothFactor) {
        reset();
        set_attr_num(attrNum);
        DoubleRef thresh =new DoubleRef(0.0);
        IntRef splitIndex =new IntRef(0);
        IntRef numDistinct =new IntRef(0);
        Entropy.find_best_threshold(column, minSplit, this, thresh, splitIndex,
        numDistinct, smoothInst, smoothFactor);
        save_real_split(thresh, splitIndex, numDistinct);
        return existSplit;
    }
    
    /** The data calculated by find_best_threshold() is saved in the SplitAttr via this
     * function.
     *
     * @param thresh The threshold to be saved.
     * @param splitIndex The index of the split to be saved.
     * @param numDistinct The number of distinct splits.
     */
    public void save_real_split(DoubleRef thresh, IntRef splitIndex, IntRef numDistinct) {
        existSplit = false;
        if (thresh.value == Globals.UNDEFINED_REAL || splitIndex.value < 0 || numDistinct.value <= 0) {
            reset();
            return;
        }
        
        free_type_info();
        splitType = realThresholdSplit;
        if (MLJ.approx_equal(total_weight(), 0.0))
            Error.fatalErr("SplitAttr::save_real_split: Total weight is near 0.   "+
            "Cannot continue, as need to divide by it");
        // We always compute the MDL penalty, so that if callers turn
        //   it on or off, we have the number already computed (correctness,
        //   not efficient).
        mdlPenalty = MLJ.log_bin(numDistinct.value) / total_weight();
        typeInfo.theThreshold = thresh.value;
        existSplit = true;
        realValuedSplitIndex = splitIndex.value;
    }
    
    /** Delete and clear typeInfo.
     */
    public void free_type_info() {
        switch(splitType) {
            case noReasonableSplit:
            case nominalSplit:
                break;
            case realThresholdSplit:
                typeInfo.theThreshold = Globals.UNDEFINED_INT;
                break;
            case multiRealThresholdSplit:
                //	 delete typeInfo.thresholds;
                typeInfo.thresholds = null;
                break;
            case partitionSplit:
                //	 delete typeInfo.partition;
                typeInfo.partition = null;
                break;
            default:
                MLJ.Abort();
        }
    }
    
    /** Reset values, except attribute number.
     */
    public void reset() {
        //   DBG(ASSERT(attributeNum >= 0 || attributeNum == Globals.UNDEFINED_INT));
        super.reset();
        splitType = noReasonableSplit;
        existSplit = false;
        mdlPenalty = 0;
        realValuedSplitIndex = Globals.UNDEFINED_INT;
    }
    
    /** Sets the attribute number for this split.
     * @param num The number of the new attribute.
     */
    public void set_attr_num(int num) {
        if (num < 0 && num != Globals.UNDEFINED_INT) {
            Error.fatalErr("SplitAttr::split_attr_num: attempt to set attribute number to "+
            num+", which is neither non-negative nor UNDEFINED_INT ("+Globals.UNDEFINED_INT+")");
        }
        attributeNum = num;
    }
    
    /** Helper function to do all processing for nominals. Nominal splits always exist.
     *
     * @param instList The InstanceList over which to make a nominal split.
     * @param attributeNumber The number of the attribute to be split.
     * @return TRUE if a nominal split exists, FALSE if not.
     */
    public boolean make_nominal_split(InstanceList instList,
    int attributeNumber) {
        reset();
        set_attr_num(attributeNumber);
        Schema schema = instList.get_schema();
        if (!schema.attr_info(attributeNumber).can_cast_to_nominal())
            Error.fatalErr("SplitAttr::make_nominal_split: attribute "+attributeNumber+
            " can not be cast to nominal");
        InstanceList ptr = instList;
        InstanceList[] array = new InstanceList[1];
        array[0] = ptr;
        double[][] sAndLDist =
        Entropy.build_split_and_label_dist(array, attributeNumber);
        sAndLDist = super.set_split_and_label_dist(sAndLDist);
        return make_nominal_split();
    }
    
    
    /** Helper function to do all processing for nominals. Nominal splits always exist.
     * @return TRUE if a nominal split exists, FALSE if not.
     */
    public boolean make_nominal_split() {
        splitType = nominalSplit;
        existSplit = true;
        boolean normalize = (get_split_score_criterion() == normalizedMutualInfo);
        get_mutual_info(normalize, false); // Determines if split exists
        return existSplit;
    }
    
    /** Check if it is OK to make a split on the nominal attribute by making sure at
     * least two branches have more than minSplit instances. The need is to split into
     * 2 disjoint sets, both sets containing at least 'minSplit' instances (so there
     * needs to be at least twice 'minSplit' instances). This function checks to see if
     * there are enough instances for such a split to occur. The minSplit must be at
     * least 1.
     *
     * @param attrNum The number of the attribute to be checked.
     * @param counters Counters of the values for this attribute.
     * @param minSplit The minimum split value.
     * @return TRUE if the attribute is ok to split.
     */
    public static boolean ok_to_split(int attrNum, BagCounters counters, double minSplit) {
        //   DBG(ASSERT(minSplit >= 1));
        
        // If there aren't two values (or more), we clearly can't split.
        if (counters.attr_num_vals(attrNum) < 2)
            return false;
        
        
        if (!MLJ.approx_greater(minSplit,1.0))
            return true; // We know there are (at least) two values, each of which
        //   must have at least one instance.
        
        double[] ac = counters.attr_counts()[attrNum];
        if (ac == null)
            Error.fatalErr("ID3Inducer::ok_to_split: No counters");
        
        int numAboveMin = 0;
        for (int i = 0; numAboveMin < 2 && i <= ac.length; i++)
            if (ac[i] >= minSplit)
                numAboveMin++;
        
        return (numAboveMin >= 2);
    }
    
    /** Returns the number of attributes.
     * @return The number of attrbutes.
     */
    public int get_attr_num(){
        //	ASSERT(attributeNum != -1);
        return attributeNum;
    }
    
    /** Get penalty. Only valid if you are penalizing.
     * @return Returns the penalty value.
     */
    public double penalty() {
        if (!get_penalize_by_mdl())
            Error.fatalErr("SplitAttr::penalty: MDL penalty not set");
        return mdlPenalty;
    }
    
    /** Returns the minimum distance length penalty value.
     * @return The minimum distance length penalty value.
     */
    public boolean get_penalize_by_mdl(){return penalizeByMDL;}
    
    /** Returns the mutual gain-ratio.
     *
     * @param penalize TRUE if penalization should occur, FALSE otherwise.
     * @return The mutual gain-ratio.
     */
    public double get_gain_ratio(boolean penalize) {
        double numerator = get_mutual_info(false, penalize);
        double divisor   = get_split_entropy();
        if (MLJ.approx_equal(divisor, 0.0))
            Error.fatalErr("SplitAttr::get_gain_ratio: split entropy ("+divisor+
            ") is too close to zero for division");
        //   ASSERT(numerator != Globals.UNDEFINED_REAL);
        double gain = numerator / divisor;
        
        //   ASSERT(gain >= 0);
        return gain;
    }
    
    /** Return the threshold. Can only be called if the split exists and is a real
     * threshold split.
     *
     * @return The threshold.
     */
    public double threshold() {
        if (splitType != realThresholdSplit)
            Error.fatalErr("SplitAttr::threshold(): split is not realThreshold, it is "+
            name_from_value(splitType,splitTypeEnum));
        
        return typeInfo.theThreshold;
    }
    
    private String name_from_value(int value, String[] enumNames) {
        if(value >= 0 && value < enumNames.length)
            return enumNames[value];
        return "";
    }
    
    /** Copies the given SplitAttr inot this SplitAttr.
     * @param original The SplitAttr to be copied.
     */
    public void copy(SplitAttr original) {
        
        attributeNum = original.attributeNum;
        
        typeInfo = new TypeInfo();
        typeInfo.partition = (int[])typeInfo.partition.clone();
        typeInfo.theThreshold = typeInfo.theThreshold;
        typeInfo.thresholds = (double[])typeInfo.thresholds.clone();
        
        mdlPenalty = original.mdlPenalty;
        existSplit = original.existSplit;
        splitType = original.splitType;
        penalizeByMDL = original.penalizeByMDL;
        realValuedSplitIndex = original.realValuedSplitIndex;
    }
    
    /** Initialize attribute data and distribution arrays. The first version bases it on
     * splits that were done before calling us. The second version does the split
     * based on a given categorizer and computes its worth based on the resulting
     * instance lists.
     *
     * @param instLists The InstanceList to use in initialization.
     * @param attributeNumber The number of the attribute.
     */
    public void initialize(InstanceList[] instLists,int attributeNumber) {
        reset();
        set_attr_num(attributeNumber);
        
        int numLabels = instLists[0].get_schema().num_label_values();
        
        double[][] splitAndLabelDist = new double[numLabels][instLists.length];
        for (int child = 0; child <= instLists.length; child++) {
            //MLJ.ASSERT(instLists[child] != null);
            // everything in a set has the same attr value, by definition
            for (int labelValCount = 0; labelValCount < numLabels;
            labelValCount++) {
                BagCounters bc = instLists[child].counters();
                splitAndLabelDist[labelValCount][child] = bc.label_count(labelValCount);
            }
        }
        set_split_and_label_dist(splitAndLabelDist);
    }
    
    /** The criterion calculation depends on the score criterion. For gainRatio it's
     * (surprise) gain ratio. For mutualInfo and normalizedMutualInfo it's mutualInfo.
     *
     * @return The score for this split.
     */
    public double score() {
        switch (get_split_score_criterion()) {
            case mutualInfo:
                return get_mutual_info(false, penalizeByMDL);
            case normalizedMutualInfo:
                return get_mutual_info(true, penalizeByMDL);
            case gainRatio:
                return get_gain_ratio(penalizeByMDL);
            case mutualInfoRatio:
                return get_mutual_info_ratio();
            case externalScore:
                return get_external_score();
            default:
                Error.fatalErr("SplitAttr::score: split score criterion of " +
                get_split_score_criterion() + " is out of range");
                return 0;  // Can't get here.
        }
    }
    
    /** Computes the scores and updates the cache when there are being computed many
     * times for the same number of instances and entropy. This would happen, for
     * instance, when determining the best threshold for a split.
     * @param sAndLDist The split and label distribution.
     * @param sDist The split distribution.
     * @param lDist The label distribution.
     * @param entropy The entropy value.
     * @param totalWeight The total weight of instances.
     * @return The score for this split.
     */
    public double score(double[][] sAndLDist, double[] sDist,
    double[] lDist, double entropy, double totalWeight){
        return super.score(sAndLDist, sDist, lDist, entropy,totalWeight);
    }
    
    /** Computes the scores and updates the cache when there are being computed many
     * times for the same number of instances and entropy. This would happen, for
     * instance, when determining the best threshold for a split.
     * @param sAndLDist The split and label distribution.
     * @param sDist The split distribution.
     * @param lDist The label distribution.
     * @param entropy The entropy value.
     * @return The score for this split.
     */
    public double score(double[][] sAndLDist, double[] sDist,
    double[] lDist, double entropy){
        return super.score(sAndLDist, sDist, lDist, entropy,
        Globals.UNDEFINED_REAL);
    }
    
    /** Computes the scores and updates the cache when there are being computed many
     * times for the same number of instances and entropy. This would happen, for
     * instance, when determining the best threshold for a split.
     * @param sAndLDist The split and label distribution.
     * @param sDist The split distribution.
     * @param lDist The label distribution.
     * @return The score for this split.
     */
    public double score(double[][] sAndLDist, double[] sDist, double[] lDist){
        return super.score(sAndLDist, sDist, lDist, Globals.UNDEFINED_REAL,
        Globals.UNDEFINED_REAL);
    }
    
}//End of class