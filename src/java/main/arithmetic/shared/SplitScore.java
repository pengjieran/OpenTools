package arithmetic.shared;
import java.io.IOException;
import java.io.Writer;
/** A class for determining, holding, and returning the information associated
 * with an attribute split. Uses methods from the Entropy class for the
 * determination of the scores.
 * The scores for a specific item are cached in an internal cache structure.
 * This structure contains the entropy, conditional entropy, split entropy,
 * mutual information, gain ratio, split distribution, label distribution, and
 * the number of instances that these scores have been evaluated on. Access to
 * scores that have been cached are constant time, while items that need
 * refreshment have a longer access time for recalculation.
 * @author James Louis   Initial Java implementation.
 * @author Eric Eros     Initial revision                        7/08/96
 */

public class SplitScore {
    //ENUMS
    /** Value indicating how splits are scored.
     */
    public static final byte mutualInfo = 0;          /* SplitScoreCriterion enum */
    /** Value indicating how splits are scored.
     */
    public static final byte normalizedMutualInfo = 1;/*                          */
    /** Value indicating how splits are scored.
     */
    public static final byte gainRatio = 2;           /*                          */
    /** Value indicating how splits are scored.
     */
    public static final byte mutualInfoRatio = 3;     /*                          */
    /** Value indicating how splits are scored.
     */
    public static final byte externalScore = 4;       /*                          */
    //END ENUM
    
    /** Default criterion for determining the score of a particular split.
     * @see #mutualInfo
     * @see #normalizedMutualInfo
     * @see #gainRatio
     * @see #mutualInfoRatio
     * @see #externalScore
     */
    public static byte defaultSplitScoreCriterion = normalizedMutualInfo;
    /** String names for each form of split criterion. **/
    public static String[] splitScoreCriterionEnum = {"mutualInfo",
    "normalizedMutualInfo","gainRatio","mutualInfoRatio","externalScore"};
    
    /***************************************************************************
     *Cache structure for storing scores from sets of instances. Stores the
     *information for the entropy, conditional entropy, split entropy,
     *mutual information, gain ratio, split distribution, label distribution,
     *and the number of instances that these scores have been evaluated on.
     **************************************************************************/
    private class CacheStruct {
        /** Mutual Information calculation. **/
        public double mutualInfo;
        /** Distribution of attribute splits over the set of instances. **/
        public double[] splitDist;
        /** Distribution of labels over the set of instances. **/
        public double[] labelDist;
        /** The total weight of the set of instances. **/
        public double totalWeight;
        /** The entropy calculation. **/
        public double entropy;
        /** The gain ratio calculation. **/
        public double gainRatio;
        /** The entropy for the split examined. **/
        public double splitEntropy;
        /** The conditional entropy calculation for the split examined. **/
        public double condEntropy;
        
        /** Cache Constructor.
         */
        public CacheStruct() {
            totalWeight = Globals.UNDEFINED_REAL;
            entropy = Globals.UNDEFINED_REAL;
            mutualInfo = Globals.UNDEFINED_REAL;
            condEntropy = Globals.UNDEFINED_REAL;
            gainRatio = Globals.UNDEFINED_REAL;
            splitEntropy = Globals.UNDEFINED_REAL;
            splitDist = null;
            labelDist = null;
        }
        
        /** Copies the specified CacheStruct to this CacheStruct instance.
         * @param source The CacheStruct to be copied from.
         */
        public void copy(CacheStruct source) {
            totalWeight = source.totalWeight;
            entropy = source.entropy;
            mutualInfo = source.mutualInfo;
            condEntropy = source.condEntropy;
            gainRatio = source.gainRatio;
            splitEntropy = source.splitEntropy;
            splitDist = source.splitDist;
            labelDist = source.labelDist;
        }
        
        /** Cache copy constructor.
         * @param source The CacheStruct to be copied from.
         */
        public CacheStruct(CacheStruct source) {
            copy(source);
        }
        
        /** Assignment of specified CacheStruct to this CacheStruct instance.
         * @param source The CacheStruct to be copied.
         * @return This CacheStruct.
         */
        public CacheStruct assign(CacheStruct source) {
            copy(source);
            return this;
        }
    }
    
    /** Cache of calculated data. **/
    private CacheStruct cache;
    
    /** A matrix of labels(rows) by splits (columns). **/
    private double[][] splitAndLabelDist; //row, column
    /** Indicator of wether cache is filled with calculations. TRUE indicates
     *the values are in place, FALSE otherwise. **/
    private boolean validCache;
    /** Type of criterion used for examining the attribute split. **/
    private byte splitScoreCriterion;
    private double theExternalScore;
    
    /** Logging options for this class. **/
    protected LogOptions logOptions = new LogOptions();
    
    /** Sets the logging level for this object.
     * @param level  The new logging level.
     */
    public void set_log_level(int level){logOptions.set_log_level(level);}
    
    /** Returns the logging level for this object.
     * @return The level of logging in this class.
     */
    public int  get_log_level(){return logOptions.get_log_level();}
    
    /** Sets the stream to which logging options are displayed.
     * @param strm   The stream to which logs will be written.
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
     * @param opt    The new LogOptions object.
     */
    public void set_log_options(LogOptions opt)
    {logOptions.set_log_options(opt);}
    
    /** Sets the logging message prefix for this object.
     * @param file   The file name to be displayed in the prefix of log messages.
     * @param line   The line number to be displayed in the prefix of log messages.
     * @param lvl1   The log level of the statement being logged.
     * @param lvl2   The level of log messages being displayed.
     */
    public void set_log_prefixes(String file, int line,int lvl1, int lvl2)
    {logOptions.set_log_prefixes(file, line, lvl1, lvl2);}
    
    /** Copy Constructor.
     * @param source The SplitScore to be copied.
     */
    public SplitScore(SplitScore source) {
        cache = new CacheStruct();
        splitAndLabelDist = null;
        cache.splitDist = null;
        cache.labelDist = null;
        reset();
        splitScoreCriterion = source.splitScoreCriterion;
        copy_split_and_label_dist(source);
        // Must initialize after reset
        theExternalScore = source.theExternalScore;
    }
    
    /** Constructor.
     */
    public SplitScore() {
        cache = new CacheStruct();
        splitAndLabelDist = null;
        cache.splitDist = null;
        cache.labelDist = null;
        reset();
        splitScoreCriterion = defaultSplitScoreCriterion;
    }
    
    /** Returns the mutual info (information gain) score for the split this
     * SplitScore object represents. Created to avoid JVM error.
     * @return The unnormalized mutual info for this split.
     */
    public double get_unnormalized_mutual_info() {
        valid_cache(); // Percolate validCache to the cache members.
        if ((cache.mutualInfo == Globals.UNDEFINED_REAL) && (has_distribution(true)))
            cache.mutualInfo =
            Entropy.mutual_info(get_entropy(), get_split_and_label_dist(),
            get_split_dist(), total_weight());
        return cache.mutualInfo;
    }
    
    /** Returns the mutual info (information gain) score for the split this
     * SplitScore object represents. This method updates the cache.
     * @param normalize TRUE if normalization is requested, FALSE otherwise.
     * @return The mutual info value for this split.
     */
    public double get_mutual_info(boolean normalize) {
        valid_cache(); // Percolate validCache to the cache members.
        if ((cache.mutualInfo == Globals.UNDEFINED_REAL) && (has_distribution(true)))
            cache.mutualInfo =
            Entropy.mutual_info(get_entropy(), get_split_and_label_dist(),
            get_split_dist(), total_weight());
        return normalize ?
        normalize_by_num_splits(cache.mutualInfo) : cache.mutualInfo;
    }
    
    /** Normalize by the number of splits. Divide by (the number of bits needed to store
     * the value (number of splits - 1)). This method updates the cache.
     *
     * @param score The score to be normalized.
     * @return The normalized score value.
     */
    public double normalize_by_num_splits(double score) {
        int numSplits = num_splits();
        if (numSplits <= 0)
            Error.err("SplitScore::normalize_by_num_splits: number of splits "+
            "not greater than 0: " + num_splits() + "-->fatal_error");
        // If num_splits is 1 or 2, it becomes 2, with the log_2(2) == 1
        if (numSplits >= 3)
            // There may be only one value and it's useful because of
            //   unknowns.  We therefore divide by max(2, value)
            score /= Entropy.log_bin(numSplits);
        
        //            score /= Math.log((double)numSplits);// /Math.log(2.0);
        return score;
    }
    
    /** Returns the number of splits--not including unknowns. This method updates the
     * cache.
     * @return The number of splits.
     */
    public int num_splits() {
        valid_cache(); // Percolate validCache to the cache members.
        int numSplits = Globals.UNDEFINED_INT;
        if (has_distribution(true)) {
            get_split_dist(); // Ensure the distribution is non-NULL.
            // Note that by looking at high, we ignore the UNKNOWN edge
            //   if it exists.
            numSplits = cache.splitDist.length - 1;
            //obs cache.splitDist.high() + 1;
            //The -1 is to offset the movement of unknown values to array index
            //zero. Only the number of actual values for the split should
            //be returned. -JL
        }
        return numSplits;
    }
    
    /** The split distribution is calculated from the split and label distribution.
     *
     * @return The split distribution.
     */
    public double[] get_split_dist() {
        valid_cache(); // Percolate validCache to the cache members.
        if (cache.splitDist != null)
            return (cache.splitDist);
        if (!has_distribution(false))
            Error.err("SplitScore::get_split_dist: splitAndLabelDist has not "+
            "been set-->fatal_error");
        else {
            cache.splitDist = new double[splitAndLabelDist[0].length];
            Matrix.sum_cols(cache.splitDist,splitAndLabelDist);
        }
        return (cache.splitDist);
    }
    
    
    /** Checks if there exists a splitAndLabel distribution.
     *
     * @return TRUE if there is a splitAndLabel distribution, FALSE otherwise.
     */
    public boolean has_distribution(){return has_distribution(true);}
    
    /** Checks if there exists a splitAndLabel distribution.
     *
     * @param fatalOnFalse TRUE if an error message is to be displayed if there is no splitAndLabel
     * distribution.
     * @return TRUE if there is a splitAndLabel distribution, FALSE otherwise.
     */
    public boolean has_distribution(boolean fatalOnFalse) {
        if (!(splitAndLabelDist == null))
            return true;
        if (fatalOnFalse)
            Error.err("SplitScore::has_distribution: no distribution-->"+
            "fatal_error");
        return false;
    }
    
    /** Returns cache.condEntropy, first checking to see if it has yet been set.
     * This method updates the cache.
     * @return The condEntropy stored in the cache.
     */
    public double get_cond_entropy() {
        valid_cache(); // Percolate validCache to the cache members.
        if (cache.condEntropy == Globals.UNDEFINED_REAL && has_distribution(true))
            this.cache.condEntropy =
            Entropy.cond_entropy(get_split_and_label_dist(),
            get_split_dist(), total_weight());
        return cache.condEntropy;
    }
    
    
    private boolean valid_cache() {
        if (validCache && has_distribution(false))
            return true;
        // When the cache is not valid, set the ancillary arrays NULL,
        //   invalidate the numeric data, and set the valid flag TRUE.
        cache.splitDist = null;
        cache.labelDist = null;
        cache.totalWeight = Globals.UNDEFINED_REAL;
        cache.mutualInfo = Globals.UNDEFINED_REAL;
        cache.entropy = Globals.UNDEFINED_REAL;
        cache.condEntropy = Globals.UNDEFINED_REAL;
        cache.gainRatio = Globals.UNDEFINED_REAL;
        cache.splitEntropy = Globals.UNDEFINED_REAL;
        validCache = true;
        return false;
    }
    
    /** Returns the total weight from the cache. This method updates the cache.
     *
     * @return The total weight.
     */
    public double total_weight() {
        valid_cache(); // Percolate validCache to the cache members.
        if (cache.totalWeight != Globals.UNDEFINED_REAL)
            return cache.totalWeight;
        if (!has_distribution(true))
            Error.err("SplitScore::num_instancess: splitAndLabelDist not yet "+
            "set-->fatal_error");
        else {
            if (cache.splitDist != null)
                cache.totalWeight = MLJArray.sum(cache.splitDist);
            else {
                get_label_dist();
                cache.totalWeight = MLJArray.sum(cache.labelDist);
            }
        }
        return cache.totalWeight;
    }
    
    /** The label distribution is calculated from the split and label distribution.
     * This method updates the cache.
     * @return The label distribution.
     */
    public double[] get_label_dist() {
        valid_cache(); // Percolate validCache to the cache members.
        if (cache.labelDist != null)
            return cache.labelDist;
        if (!has_distribution(false))
            Error.err("SplitScore::get_label_dist: splitAndLabelDist has not "+
            "been set-->fatal_error");
        else {
            //      cache.labelDist = new double[splitAndLabelDist[0].length];
            cache.labelDist = new double[splitAndLabelDist.length];
            Matrix.sum_rows(cache.labelDist,splitAndLabelDist);
        }
        return cache.labelDist;
    }
    
    /** Returns a reference to the requested distribution array.
     *
     * @return The splitAndLabel distribution array.
     */
    public double[][] get_split_and_label_dist() {
        valid_cache();
        if (splitAndLabelDist == null)
            Error.err("SplitScore::get_split_and_label_dist: Array has not "+
            "been allocated-->fatal_error");
        return splitAndLabelDist;
    }
    
    /** Returns cache.entropy, first checking to see if it has yet been set.
     * This method updates the cache.
     *
     *
     * @return The entropy stored in the cache.
     */
    public double get_entropy() {
        valid_cache(); // Percolate validCache to the cache members.
        if (cache.entropy == Globals.UNDEFINED_REAL && has_distribution(true))
            cache.entropy = Entropy.entropy(get_label_dist());
        return cache.entropy;
    }
    
    
    private void verify_strictly_greater(double lhs, double rhs,
    String additionalErrMsg) {//basicCore class function
        if (lhs <= (rhs + (MLJ.realEpsilon))) {
            Error.err(additionalErrMsg + "\n verify_strictly_greater(Real): "+
            "variable is not at least " + MLJ.realEpsilon + " greater than its"+
            "lower bound (" + rhs + ")-->fatal_error");
            //      Error.err(additionalErrMsg + "\n verify_strictly_greater(Real): variable (" + MString(lhs, 20, 0, MString::general) +
            //	  ") is not at least " + MLJ.realEpsilon + " greater than its lower bound (" + rhs + ")-->fatal_error");
        }
    }
    
    /** The criterion calculation depends on the score criterion. For gainRatio it's
     * (surprise) gain ratio.  For mutualInfo and normalizedMutualInfo it's mutualInfo.
     * For mutualInfoRatio it's mutualInfo / entropy. This method updates the cache.
     *
     * @return The score for the split.
     */
    public double score() {
        switch (get_split_score_criterion()) {
            case mutualInfo:
                return get_mutual_info(false);
            case normalizedMutualInfo:
                return get_mutual_info(true);
            case gainRatio:
                return get_gain_ratio();
            case mutualInfoRatio:
                return get_mutual_info_ratio();
            case externalScore:
                return get_external_score();
            default:
                Error.err("SplitScore::score: split score criterion of " +
                get_split_score_criterion() +
                " is out of range-->fatal_error");
                return 0;
        }
    }
    
    /** Computes the scores and updates the cache when there are being computed many
     * times for the same number of instances and entropy. This would happen, for
     * instance, when determining the best threshold for a split.
     * @param sAndLDist The split and label distribution.
     * @param sDist The split distribution.
     * @param lDist The label distribution.
     * @param passedEntropy The entropy value for this split.
     * @param passedWeight The weight of instances for this split.
     * @return The score for this split distribution.
     * @see Entropy#find_best_threshold
     */
    public double score(double[][] sAndLDist, double[] sDist,
    double[] lDist, double passedEntropy,
    double passedWeight) {
        // Distribution arrays are passed as consts; handed over to
        //   SplitScore; then released back to the invoker.
        
        // Save the both the current cache and the splitAndLabelDist reference.
        // Restore them prior to returning.  Note:  the cache saves the
        //   references to the old dists, not the dists themselves.
        double theOldExternalScore = theExternalScore;
        boolean oldValidCache = validCache;
        double[][] oldSplitAndLabelDist = splitAndLabelDist;
        CacheStruct oldCache = cache;
        splitAndLabelDist = null;
        cache.splitDist = null;
        cache.labelDist = null;
        
        double[][] sAndLDistP = sAndLDist;  // No const.
        set_split_and_label_dist(sAndLDistP);
        if (sDist != null) {
            double[] sDistP = sDist; // No const.
            set_split_dist(sDistP);
        }
        if (lDist != null) {
            double[] lDistP = lDist; // No const.
            set_split_dist(lDistP);
        }
        valid_cache();
        if (passedEntropy != Globals.UNDEFINED_REAL) {
            //      DBG(mlc.verify_approx_equal(passedEntropy, get_entropy(),
            //	                          "SplitScore::score: given entropy "
            //	                          "not equal to calculated entropy");
            //	  );
            cache.entropy = passedEntropy;
        }
        cache.totalWeight = passedWeight;
        
        double theScore = score();
        
        if (sDist != null) {
            double[] releasedSplitDist = release_split_dist();
            //      (void)releasedSplitDist;
            //      DBG(ASSERT(mlc.approx_equal(*releasedSplitDist, *sDist)));
        }
        if (lDist != null) {
            double[] releasedLabelDist = release_label_dist();
            //      (void)releasedLabelDist;
            //      DBG(ASSERT(mlc.approx_equal(*releasedLabelDist, *lDist)));
        }
        double[][] releasedSplitAndLabelDist = release_split_and_label_dist();
        //   (void)releasedSplitAndLabelDist;
        //   DBG(ASSERT(mlc.approx_equal(*releasedSplitAndLabelDist, *sAndLDist)));
        
        // Restore
        cache = oldCache;
        splitAndLabelDist = oldSplitAndLabelDist;
        validCache = oldValidCache;
        theExternalScore = theOldExternalScore;
        
        return theScore;
    }
    
    
    
    /** Returns the type of criterion used in scoring splits.
     * @return The scoring criterion.
     * @see #mutualInfo
     * @see #normalizedMutualInfo
     * @see #gainRatio
     * @see #mutualInfoRatio
     * @see #externalScore
     */
    public byte get_split_score_criterion()
    {return splitScoreCriterion;}
    
    /** Returns the value, set externally, for the score.
     *
     * @return The externally set score value.
     */
    public double get_external_score() {
        if (splitAndLabelDist == null && theExternalScore != Globals.UNDEFINED_REAL)
            Error.err("SplitScore::get_external_score:  splitAndLabelDist was "+
            "deleted without theExternalScore being invalidated-->fatal_error");
        if (theExternalScore == Globals.UNDEFINED_REAL)
            Error.err("SplitScore::get_external_score: no score set-->"+
            "fatal_error");
        return theExternalScore;
    }
    
    /** Returns the mutual information ratio, which is the ratio between the mutual
     * info and entropy. The mutual information must be >= 0. Although const, this
     * method updates the cache.
     *
     * @return Mutual information ratio.
     */
    public double get_mutual_info_ratio() {
        double denominator = get_entropy();
        MLJ.verify_strictly_greater(denominator, 0,
        "SplitScore::get_mutual_info_ratio: Need to divide by entropy, which "+
        "is too small");
        DoubleRef ratio = new DoubleRef(get_mutual_info(false) / denominator);
        MLJ.clamp_to_range(ratio, 0, 1, "SplitScore::get_mutual_info_ratio: "+
        "ratio not in required range [0, 1]");
        return ratio.value;
    }
    
    /** Determines, and returns, cache.gainRatio. This method updates the cache.
     *
     * @return The gainRatio stored in the cache.
     */
    public double get_gain_ratio() {
        valid_cache(); // Percolate validCache to the cache members.
        double gain = cache.gainRatio;
        if (cache.gainRatio == Globals.UNDEFINED_REAL && has_distribution(true)) {
            double numerator = get_mutual_info(false);
            double divisor = get_split_entropy();
            // If the divisor is zero, we abort.
            if (MLJ.approx_equal(divisor, 0.0))
                Error.err("SplitScore::get_gain_ratio: split entropy (" + divisor +
                ") is too close to zero for division. Split and Label Dist is: " +
                splitAndLabelDist + "-->fatal_error");
            //      ASSERT(numerator != Globals.UNDEFINED_REAL);
            gain = numerator / divisor;
            cache.gainRatio = gain;
            if (gain < 0)
                Error.err("SplitScore::get_gain_ratio: negative gain: " +
                gain + "=" + numerator + '/' + divisor + "-->fatal_error");
        }
        if (gain < 0)
            Error.err("SplitScore::get_gain_ratio: negative gain: "+ gain +"-->fatal_error");
        return gain;
    }
    
    /** Returns cache.splitEntropy, first checking to see if it has yet been set.
     * This method updates the cache.
     * @return The split entropy value stored in the cache.
     */
    public double get_split_entropy() {
        valid_cache(); // Percolate validCache to the cache members.
        if (cache.splitEntropy == Globals.UNDEFINED_REAL && has_distribution(true))
            cache.splitEntropy = Entropy.entropy(get_split_dist(), total_weight());
        return cache.splitEntropy;
    }
    
    /** Sets the split score criterion.
     * @param choice The chosen split score criterion.
     * @see #mutualInfo
     * @see #normalizedMutualInfo
     * @see #rainRatio
     * @see #mutualInfoRatio
     * @see #externalScore
     */
    public void set_split_score_criterion(byte choice)
    {splitScoreCriterion = choice; }
    
    /** Clear (delete) distribution array data.
     *
     */
    public void reset() {
        //   delete cache.splitDist;
        cache.splitDist = null;
        //   delete cache.labelDist;
        cache.labelDist = null;
        //   delete splitAndLabelDist;
        splitAndLabelDist = null;
        theExternalScore = Globals.UNDEFINED_REAL;
        
        validCache = false;
        valid_cache();
    }
    
    /** Stores the cache.splitDist array.
     *
     * @param sDist The split distribution array to be cached.
     */
    public void set_split_dist(double[] sDist) {
        valid_cache(); // Percolate validCache to the cache members.
        // Only delete one object's distribution if it doesn't share its
        //   location with the one passed in--otherwise by deleting one, we'd
        //   be deleting both.
        if (cache.splitDist == sDist)
            Error.fatalErr("SplitScore::set_split_dist: got my own pointer");
        //   delete cache.splitDist;
        cache.splitDist = sDist;
        sDist = null;
        //   DBGSLOW(OK());
    }
    
    //public void set_split_and_label_dist(double[][] sAndLDist)
    /** Stores the splitAndLabelDist array.
     * @param sAndLDist The new split and label distribution.
     * @return The old split and label distribution.
     */
    public double[][] set_split_and_label_dist(double[][] sAndLDist) {
        valid_cache(); // Percolate validCache to the cache members.
        // We can't compare for equality and use our results because
        //   the caller's reference may be a reference to an automatic variable,
        //   in which case our delete would screw it up.
        
        // Only delete one object's distribution if it doesn't share its
        //   location with the one passed in--otherwise by deleting one, we'd
        //   be deleting both.
        if (splitAndLabelDist == sAndLDist)
            Error.fatalErr("SplitScore::set_split_and_label_dist: got my own pointer");
        reset();
        splitAndLabelDist = sAndLDist;
        sAndLDist = null;
        return sAndLDist;
    }
    
    private void copy_dist(SplitScore source) {
        if (this != source) {
            reset();
            copy_split_and_label_dist(source);
            validCache = false;
        }
    }
    
    private void copy_split_and_label_dist(SplitScore source) {
        // When the new distribution differs from the old, but the array is
        //   of the same size, don't delete the old splitAndLabelDist array.
        //   Reuse it.
        valid_cache(); // Percolate validCache to the cache members.
        if (splitAndLabelDist != null && source.splitAndLabelDist != null &&
        splitAndLabelDist.length == source.splitAndLabelDist.length) {
            // Keep the splitAndLabelDist, but nothing else in the cache.
            double[][] savedDistribution = splitAndLabelDist;
            splitAndLabelDist = null;
            reset();
            splitAndLabelDist = savedDistribution;
            theExternalScore = Globals.UNDEFINED_REAL;
            splitAndLabelDist = source.get_split_and_label_dist();
        }
        else {
            reset();
            if (source.splitAndLabelDist != null)
                splitAndLabelDist = Matrix.copy(source.get_split_and_label_dist());
        }
    }
    
    /** Set the external score. The score must be non-negative, although we could
     * change it to anything but UNDEFINED_REAL.
     * @param extScore The external score value.
     */
    
    public void set_external_score(double extScore) {
        if (extScore < 0)
            Error.fatalErr("SplitScore::set_external_score: score="+extScore
            +" is negative");
        
        if (!has_distribution(false))
            Error.fatalErr("SplitScore::set_external_score: no distribution");
        MLJ.ASSERT(extScore != Globals.UNDEFINED_REAL,
        "SplitScore.set_external_score: extScore == Globals.UNDEFINED_REAL.");
        theExternalScore = extScore;
    }
    
    /** Checks if an external score has been set.
     * @return TRUE if the external score is set, FALSE otherwise.
     */
    public boolean has_external_score(){return theExternalScore != Globals.UNDEFINED_REAL;}
    
    
    /** Produces formatted display of contents of object. This method updates the
     * cache. Does not abort on unset data.
     */
    public void display() {
        display(Globals.Mcout,DisplayPref.defaultDisplayPref);
    }
    
    /** Produces formatted display of contents of object. This method updates the
     * cache. Does not abort on unset data. This method updates the cache.
     * @param stream The Writer to be displayed to.
     */
    public void display(Writer stream) {
        display(stream,DisplayPref.defaultDisplayPref);
    }
    
    
    /** Produces formatted display of contents of object. This method updates the
     * cache. Does not abort on unset data. This method updates the cache.
     * @param stream The Writer to be displayed to.
     * @param dp The display preferences.
     */
    public void display(Writer stream, DisplayPref dp) {
        String endl = "\n";
        String INDENT = "  ";
        String UNSET = "unset";
        double val;
        try{
            if (dp.preference_type() == DisplayPref.ASCIIDisplay) {
                valid_cache();
                stream.write("SplitScore:" + endl);
                stream.write(INDENT + "split score criterion = "
                + splitScoreCriterionEnum[get_split_score_criterion()] + endl);
                if (!has_distribution(false))
                    stream.write(INDENT + "has no cache data" + endl);
                else {
                    stream.write(INDENT + "total weight = ");
                    if (Globals.UNDEFINED_REAL == (val = total_weight()))
                        stream.write(UNSET + endl);
                    else
                        stream.write(val + endl);
                    if (num_splits() > 0)
                        stream.write(INDENT + "number of splitting values = "
                        + num_splits() + endl);
                    stream.write(INDENT + "entropy = ");
                    if (Globals.UNDEFINED_REAL == (val = get_entropy()))
                        stream.write(UNSET + endl);
                    else
                        stream.write(val + endl);
                    stream.write(INDENT + "split entropy = ");
                    if (Globals.UNDEFINED_REAL == (val = get_split_entropy()))
                        stream.write(UNSET + endl);
                    else
                        stream.write(val + endl);
                    stream.write(INDENT + "conditional entropy = ");
                    if (Globals.UNDEFINED_REAL == (val = get_cond_entropy()))
                        stream.write(UNSET + endl);
                    else
                        stream.write(val + endl);
                    stream.write(INDENT + "mutual information = "
                    + get_mutual_info(false) + endl);
                    stream.write(INDENT + "normalized mutual information = "
                    + get_mutual_info(true) + endl);
                    stream.write(INDENT + "gain ratio = ");
                    if (Globals.UNDEFINED_REAL == (val = get_gain_ratio()))
                        stream.write(UNSET + endl);
                    else
                        stream.write(val + endl);
                    if (has_external_score())
                        stream.write(INDENT + "external score = " + get_external_score()
                        + endl);
                    // Choose not to stream the arrays at low log levels.
                    // Note that we use stream, not get_log_stream() here
                    //   because they should go to the same stream.
                    int LEVEL = 4;
                    if (has_distribution(false)) {
                        if (get_log_level() >= LEVEL) stream.write(INDENT + "splitAndLabelDist = "
                        + get_split_and_label_dist() + endl);
                        if (get_log_level() >= LEVEL) stream.write(INDENT + "labelDist = "
                        + get_label_dist() + endl);
                        if (get_log_level() >= LEVEL) stream.write(INDENT + "splitDist = "
                        + get_split_dist() + endl);
                    }
                }
            }
        }catch(IOException e){e.printStackTrace(); System.exit(1);}
    }
    
    /** Assigns the given SplitScore data to this SplitScore.
     * @param rhs The SplitScore to be copied.
     * @return This SplitScore after assignment.
     */
    public SplitScore assign(SplitScore rhs) {
        if (this != rhs) {
            copy_dist(rhs);
            splitScoreCriterion = rhs.splitScoreCriterion;
            theExternalScore = rhs.theExternalScore;
        }
        return (this);
    }
    
    /** Returns the split and label distribution array and releases ownership.
     *
     * @return The split and label distribution.
     */
    public double[][] release_split_and_label_dist() {
        double[][] distribution = splitAndLabelDist;
        if (distribution == null)
            Error.fatalErr("SplitScore::release_split_and_label_dist: there is no "
            +"distribution to release");
        splitAndLabelDist = null;
        reset();
        return distribution;
    }
    
    /** Returns the label distribution array and releases ownership.
     * @return The label distribution.
     */
    public double[] release_label_dist() {
        get_label_dist();
        double[] distribution = cache.labelDist;
        if (distribution == null)
            Error.fatalErr("SplitScore::release_label_dist: there is no "
            +"label distribution to release");
        cache.labelDist = null;
        return distribution;
    }
    
    /** Returns the split distribution array and releases ownership.
     * @return The split distribution.
     */
    public double[] release_split_dist() {
        get_split_dist();
        double[] distribution = cache.splitDist;
        if (distribution == null)
            Error.fatalErr("SplitScore::release_split_dist: there is no "
            +"split distribution to release");
        cache.splitDist = null;
        return distribution;
    }
}
