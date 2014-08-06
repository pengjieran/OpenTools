package arithmetic.shared;
import java.util.Arrays;
import java.util.Vector;

/** This class handles all of the Entropy based calculations. All logs
 * are base 2 (other bases just scale the entropy). The reason for using
 * log_bin is simply that the examples in Quinlan's C4.5 book use it, and
 * those examples were used for testing.  It's also a measure of the
 * number of "bits" in information theory, so it's appealing in that sense
 * too. The computation is based on:                                           <BR>
 * 1. "Boolean Feature Discovery in Empirical Learning" / Pagallo and
 * Haussler.                                                                   <BR>
 * 2. "A First Course in Probability, 2nd Edition" / Ross, pages 354-359.
 *                                                                            <BR>
 * 3. "C4.5: Programs for Machine Learning" / Ross Quinlan, pages 18-24.
 *                                                                            <BR>
 * @author James Louis 5/21/2001	Java implementations.
 * @author Cliff Brunk 5/25/96 Added weight to find_best_threshold and
 * get_split_score.
 * @author Eric Eros 12/05/96 Revised find_best_threshold to use
 * SplitAttr, and to use RealAndLabelColumn and SplitAttr.
 * @author Chia-Hsin Li Revised find_best_threshold to allow multiple
 * InstanceLists.
 * @author Brian, Ronny Kohavi 9/04/93 Initial revision
 */
public class Entropy {
    
    /** Constant for binary log calculations.
     */
    static public double M_LOG2E = 1.4426950408889634074;
    
    /** Automatically determine a good lower bound for minSplit, based on the
     * total weight of an instance list at the start of training.
     * @param totalWeight	The total weight of instances in this
     * InstanceList partition.
     * @return A lower bound for minSplit.
     */
    public static double auto_lbound_min_split(double totalWeight) {
        double k = 1.851;
        double m = 8.447;
        if (totalWeight < 1)
            return 1;
        return Math.max(1.0, k*log_bin(totalWeight)-m);
    }
    
    /** Returns the log base two of the supplied number.
     * @param num	The number for which a log is requested.
     * @return The log base two of the supplied number.
     */
    public static double log_bin(double num) {
        //      return Math.log(num)/ Math.log(2);
        return Math.log(num) * M_LOG2E;
    }
    
    /** Compute the best threshold for RealAndLabelColumn(s). Initially, all
     * instances are at the right hand side of splitDist and
     * splitAndLabelDist. To find the best threshold, we shift the instances
     * from right to left and calculate their conditional entropy. Then we
     * pick up the one with minimum conditional entropy and return
     * bestThreshold and minCondEntropy as results. When two entropies are
     * equal, we prefer the one that splits instances equally into two bins
     * in the hope of making the tree shallower. Tests show minor effect but
     * it does help pima for different leaf counts.
     * @param realAndLabelColumn The column of real values and their associated labels
     * @param minSplit The split value for which conditional entropy is minimal.
     * @param split The scores for all available splits.
     * @param bestThreshold The real value that is the best threshold over the supplied real values.
     * @param bestSplitIndex Index of the best split in the supplied RealAndLabelColumn.
     * @param numDistinctValues The number of non-equal values.
     * @param smoothInst The index of the instance to be smoothed toward.
     * @param smoothFactor The amount of normalization done towards the specified instance index.
     */
    public static void find_best_threshold(RealAndLabelColumn realAndLabelColumn,
    double minSplit, SplitScore split,
    DoubleRef bestThreshold, IntRef bestSplitIndex,
    IntRef numDistinctValues, int smoothInst,
    double smoothFactor) {
        if (minSplit < 0)
            Error.fatalErr("find_best_threshold:  minSplit(" +minSplit+ ") must be at least 0");
        bestThreshold.value = Globals.UNDEFINED_REAL;
        bestSplitIndex.value = 0;
        numDistinctValues.value = 0;
        double totalKnownWeight = realAndLabelColumn.known_weight();
        if (realAndLabelColumn.known_weight()<(2 * minSplit)) {
            split.reset();
            return;
        }
        Vector scores = new Vector();
        double[][] sAndLDist = get_score_array(realAndLabelColumn,
        split, minSplit,
        scores, numDistinctValues,
        smoothInst, smoothFactor);
        if (sAndLDist == null)
            return;
        double bestScore = find_best_score(totalKnownWeight,
        scores, minSplit, bestSplitIndex);
        if (bestScore > Globals.UNDEFINED_REAL) {
            bestThreshold.value =
            ((double)(realAndLabelColumn.index(bestSplitIndex.value - 1).value)+
            (double)(realAndLabelColumn.index(bestSplitIndex.value).value))/ 2;
            for(int k = 1 ; k <= bestSplitIndex.value ; k++) {
                AttrLabelPair alp = realAndLabelColumn.index(k - 1);
                int lab = alp.label;
                double weight = alp.weight;
                sAndLDist[lab][Globals.RIGHT_NODE] -= weight;
                sAndLDist[lab][Globals.LEFT_NODE] += weight;
                if (MLJ.approx_equal(sAndLDist[lab][Globals.RIGHT_NODE],0))
                    sAndLDist[lab][Globals.RIGHT_NODE]= 0;
            }
            sAndLDist = split.set_split_and_label_dist(sAndLDist);
        }
        else {
            split.reset();
            sAndLDist = null;
        }
        MLJ.ASSERT(sAndLDist == null, "Entropy::find_best_threshold: sAndLDist == null");
    }
    
    /** Calculates the distribution array for the given sorted
     * RealAndLabelColumn. This function then progresses through the input array,
     * shifting counts from the right to the left (in the distributions).
     * When the Real values in the input array change, the potential score of
     * a split at that value is calculated, and stored in the output array,
     * outScores. At the end, outScores contains the discrete thresholds, the
     * scores associated with a split at each of these thresholds, and the
     * indices into the original array.
     * @param realAndLabelColumn The supplied column of real values and their associated label values.
     * @param split The scores for all available splits.
     * @param minSplit The split value for which conditional entropy is minimal.
     * @param outScores The scores after smoothing.
     * @param numDistinctValues The number of non-equal values.
     * @param smoothInst The index of the instance to be smoothed towards.
     * @param smoothFactor The amount of normalization done towards the specified instance index.
     * @return The split and label distribution.
     */
    public static double[][] get_score_array(RealAndLabelColumn realAndLabelColumn,
    SplitScore split, double minSplit,
    Vector outScores,
    IntRef numDistinctValues, int smoothInst,
    double smoothFactor) {
        int numLabels = realAndLabelColumn.label_count();
        double[][] splitAndLabelDist = new double[numLabels+1][3];
        Matrix.initialize(0,splitAndLabelDist);
        double[] splitDist = new double[3];
        realAndLabelColumn.init_dist_counts(splitAndLabelDist, splitDist);
        double[] labelDist = new double[splitAndLabelDist.length];
        Matrix.sum_rows(labelDist,splitAndLabelDist);
        int numUsedLabels = 0;
        for(int labelNum = 0 ;
        labelNum < splitAndLabelDist.length ;
        labelNum++)
            if (labelDist[labelNum] > 0)
                ++numUsedLabels;
        if (numUsedLabels < 2) {
            split.reset();
            return null;
        }
        double theEntropy = entropy(labelDist);
        
        double[][] sAndLDist = new double[splitAndLabelDist.length][splitAndLabelDist[0].length];
        for(int i = 0 ; i < splitAndLabelDist.length ; i++)
            for(int j = 0 ; j < splitAndLabelDist[0].length ; j++)
                sAndLDist[i][j] = splitAndLabelDist[i][j];
        numDistinctValues.value = 0;
        fill_scores(realAndLabelColumn, split, minSplit, theEntropy,
        outScores, numDistinctValues, splitAndLabelDist, splitDist);
        if (sAndLDist != null && smoothInst != 0)
            smooth_scores(outScores, smoothInst, smoothFactor);
        return sAndLDist;
    }
    
    /** Normalizes the scores towards the instance at the supplied index.
     * @param scores The set of scores to be smoothed.
     * @param smoothInst The index of the instance to be smoothed towards.
     * @param smoothFactor The amount of normalization done towards the specified instance index.
     */
    public static void smooth_scores(Vector scores, int smoothInst, double smoothFactor) {
        double[] oldScores = new double[smoothInst+1];
        MLJArray.init_values(-1,oldScores);
        int numThresholds = scores.size();
        
        for(int i = 0 ; i < numThresholds ; i++) {
            double summedScores =((ThresholdInfo)scores.get(i)).score;
            double summedFactor = 1;
            double currFactor = smoothFactor;
            for(int left = 1 ; i - left >= 0 && left <= smoothInst ; left++) {
                summedScores +=(currFactor *((ThresholdInfo)scores.get(i - left)).score);
                summedFactor += currFactor;
                currFactor *= smoothFactor;
            }
            currFactor = smoothFactor;
            for(int right = 1 ; i + right < numThresholds && right <= smoothInst ; right++) {
                summedScores +=(currFactor *((ThresholdInfo)scores.get(i + right)).score);
                summedFactor += currFactor;
                currFactor *= smoothFactor;
            }
            if (i > smoothInst) {
                ((ThresholdInfo)scores.get(i - smoothInst - 1)).score = oldScores[smoothInst];
            }
            for(int j = smoothInst - 1 ; j >= 0 ; j--) {
                oldScores[j+1] = oldScores[j];
            }
            if (MLJ.approx_equal(summedFactor, 0.0))
                Error.fatalErr("smooth_scores: divisor (summedFactor) too close to zero");
            oldScores[0] = summedScores/  summedFactor;
        }
        for(int j = 0 ; j < smoothInst+1 ; j++) {
            if (numThresholds - smoothInst - 1 + j >= 0)
                ((ThresholdInfo)scores.get(numThresholds - smoothInst - 1 + j)).score = oldScores[smoothInst - j];
        }
    }
    
    /** Fills the Vector of scores with the scores for all the thresholds.
     * The number of distinct values is only true for the range of numbers if
     * the relevant range that does not include minSplit on the right and left.
     * @param realAndLabelColumn The column of real values and their associated labels over which thresholds are
     * created.
     * @param split The SplitScore used for scoring this threshold split.
     * @param minSplit The minimum value for splits.
     * @param theEntropy The Entropy value
     * @param outScores The Vector of scores to be filled.
     * @param numDistinctValues The number of distinct real values for this attribute.
     * @param splitAndLabelDist Distributions over each split and label pair.
     * @param splitDist The distribution over splits.
     */
    public static void fill_scores(RealAndLabelColumn realAndLabelColumn,
    SplitScore split, double minSplit, double theEntropy,
    Vector outScores, IntRef numDistinctValues, double[][] splitAndLabelDist,
    double[] splitDist) {
        double totalWeight = realAndLabelColumn.total_weight();
        int numKnownInstances = realAndLabelColumn.known_count();
        float lastSeen = realAndLabelColumn.index(0).value;
        int threshIndex = 0;
        // We start at 1 because we're comparing a split just before this
        //   element
        for(int k = 1 ; k < numKnownInstances ; k++) {
            AttrLabelPair alp = realAndLabelColumn.index(k - 1);
            float value = alp.value;
            int lab = alp.label;
            double weight = alp.weight;
            float nextValue = realAndLabelColumn.index(k).value;
            splitDist[Globals.RIGHT_NODE] -= weight;
            splitDist[Globals.LEFT_NODE] += weight;
            if (MLJ.approx_equal((float)splitDist[Globals.RIGHT_NODE], 0.0))
                splitDist[Globals.RIGHT_NODE] = 0;
            splitAndLabelDist[lab][Globals.RIGHT_NODE] -= weight;
            splitAndLabelDist[lab][Globals.LEFT_NODE] += weight;
            if (MLJ.approx_equal((float)splitAndLabelDist[lab][Globals.RIGHT_NODE], 0.0))
                splitAndLabelDist[lab][Globals.RIGHT_NODE] = 0;
            double deltaAttr =(double)nextValue -(double)(lastSeen);
            MLJ.ASSERT(deltaAttr >= 0, "Entropy::fill_scores: deltaAttr >= 0 : deltaAttr == " +deltaAttr);
            if (deltaAttr < 2 * MLJ.storedRealEpsilon)
                continue;
            lastSeen = nextValue;
            if (nextValue - value < MLJ.storedRealEpsilon)
                continue;
            if (splitDist[Globals.RIGHT_NODE] >= minSplit && splitDist[Globals.LEFT_NODE] >= minSplit)
                numDistinctValues.value = numDistinctValues.value + 1;
            
            ThresholdInfo outScore = new ThresholdInfo();
            outScores.add(threshIndex++,outScore);
            outScore.index = k;
            outScore.weight = splitDist[Globals.LEFT_NODE];
            outScore.score = split.score(splitAndLabelDist, splitDist, null, theEntropy, totalWeight);
        }
    }
    
    /** Compute the entropy H(Y) for label Y. Giving us an InstanceList forces counters.
     * If you don't give us the total instances, we count (slightly less efficient).
     * Entropy without the sum acts a bit differently by allowing nodes with 0
     * instances and returning entropy -1. The reason is that the caller shouldn't be
     * required to compute the sum just for this.
     *
     * @param labelCount	The count of each label found in the data.
     * @return The entropy for the supplied label counts.
     */
    public static double entropy(double[] labelCount) {
        double sum = MLJArray.sum(labelCount);
        if (MLJ.approx_equal(sum, 0.0))
            return Globals.UNDEFINED_REAL;
        return entropy(labelCount, sum);
    }
    
    /** Compute the entropy H(Y) for label Y. Giving us an InstanceList forces counters.
     * If you don't give us the total instances, we count (slightly less efficient).
     * Entropy without the sum acts a bit differently by allowing nodes with 0
     * instances and returning entropy -1. The reason is that the caller shouldn't be
     * required to compute the sum just for this.
     *
     * @param labelCount	The count of each label found in the data.
     * @param totalInstanceWeight	The total weight for all of the instances.
     * @return The entropy for the supplied label counts.
     */
    public static double entropy(double[] labelCount,double totalInstanceWeight) {
        MLJ.verify_strictly_greater(totalInstanceWeight, 0, "entropy: totalInstanceWeight is too small");
        if (Globals.DBG) {
            double sum = MLJArray.sum(labelCount);
            MLJ.verify_approx_equal(sum, totalInstanceWeight,
            "entropy(,,): sum and totalWeight don\'t "
            + "match");
        }
        double H = 0;
        for(int y = 0 ; y < labelCount.length ; y++)
            if (labelCount[y] != 0) {
                // We know that totalInstanceWeight > 0 by the check
                //   on verify_strictly_greater above.
                double prob_y = labelCount[y]/  totalInstanceWeight;
                H -= prob_y * log_bin(prob_y);
            }
        return H;
    }
    
    /** Compute the entropy H(Y) for label Y. Giving us an InstanceList forces counters.
     * If you don't give us the total instances, we count (slightly less efficient).
     * Entropy without the sum acts a bit differently by allowing nodes with 0
     * instances and returning entropy -1. The reason is that the caller shouldn't be
     * required to compute the sum just for this.
     *
     * @param instList The supplied instances for whihc entropy will be calculated.
     * @return The entropy value.
     */
    public static double entropy(InstanceList instList) {
        return entropy(instList.counters().label_counts(),instList.total_weight());
    }
    
    
    /** Search a score array to find the best score/index.
     * @param totalKnownWeight Total weight of all Instances for which a value is known.
     * @param scores The scores of the available Splits.
     * @param minSplit The minimum value for a split.
     * @param bestSplitIndex The index of the best split.
     * @return The score of the best split.
     */
    public static double find_best_score(double totalKnownWeight,
    Vector scores, double minSplit,
    IntRef bestSplitIndex) {
        double minimalDistanceFromCenter = totalKnownWeight/  2;
        double bestScore = Globals.UNDEFINED_REAL;
        for(int k = 0 ; k < scores.size(); k++) {
            
            if (totalKnownWeight -((ThresholdInfo)scores.get(k)).weight < minSplit)
                break;
            if (((ThresholdInfo)scores.get(k)).weight < minSplit)
                continue;
            double currentScore =((ThresholdInfo)scores.get(k)).score;
            double currentDistance = Math.abs(totalKnownWeight / 2 -((ThresholdInfo)scores.get(k)).weight);
            
            if (currentScore > bestScore + MLJ.realEpsilon ||(MLJ.approx_equal(bestScore, currentScore)&& currentDistance < minimalDistanceFromCenter)) {
                bestSplitIndex.value =((ThresholdInfo)scores.get(k)).index;
                minimalDistanceFromCenter = currentDistance;
                bestScore = currentScore;
                LogOptions.GLOBLOG(6, "index " +bestSplitIndex+ ", entropy " +bestScore+ '\n');
            }
        }
        return bestScore;
    }
    
    /** Build the splitAndLabelDist and splitDist arrays needed for calculating
     * conditional entropy. All of the splitAndLabelDist arrays of the Instance Lists
     * are concatenated. The unaccounted instances allow the list of nodes to be
     * partial, i.e., not to contain all instances. The split will be created so that
     * the unaccounted instances are in an extra split with the same label, so that the
     * entropy will be decreased correctly as if they were in a pure node.
     * @param currentLevel	The list of instances in the current partition
     * for which a split is being determined.
     * @param attrNum The number of the attribute over which a split and label distribution is to be
     * built.
     * @return Distributions over each split and label pair.
     */
    public static double[][] build_split_and_label_dist(InstanceList[] currentLevel, int attrNum) {
        return build_split_and_label_dist(currentLevel,attrNum,0);
    }
    
    /** Build the splitAndLabelDist and splitDist arrays needed for calculating
     * conditional entropy. All of the splitAndLabelDist arrays of the Instance Lists
     * are concatenated. The unaccounted instances allow the list of nodes to be
     * partial, i.e., not to contain all instances. The split will be created so that
     * the unaccounted instances are in an extra split with the same label, so that the
     * entropy will be decreased correctly as if they were in a pure node.
     * @param currentLevel The list of instances in the current partition for which a split is being
     * determined.
     * @param attrNum The number of the attribute over which a split and label distribution is to be
     * built.
     * @param unaccountedWeight	The weight for instances that are not
     * accounted for in this partition.
     * @return Distributions over each split and label pair.
     */
    public static double[][] build_split_and_label_dist(InstanceList[] currentLevel,
    int attrNum,double unaccountedWeight) {
        MLJ.ASSERT(currentLevel[0] != null, "Entropy::build_split_and_label_dist: currentlevel == null.");
        Schema schema = currentLevel[0].get_schema();
        int numInstLists = currentLevel.length;
        int numLabelValues = schema.num_label_values();
        int numAttrValues = schema.num_attr_values(attrNum);
        MLJ.ASSERT(numInstLists > 0, "Entropy::build_split_and_label_dist: numInstLists <= 0.");
        MLJ.ASSERT(numLabelValues > 0, "Entropy::build_split_and_label_dist: numLabelValues <= 0.");
        MLJ.ASSERT(numAttrValues > 0, "Entropy::build_split_and_label_dist: numAttrValues <= 0.");
        MLJ.ASSERT(unaccountedWeight >= 0, "Entropy::build_split_and_label_dist: unaccountedWeight < 0.");
        double[][] splitAndLabelDist = new double[numLabelValues][numInstLists*(numAttrValues+1)+((MLJ.approx_equal(unaccountedWeight, 0.0))?0:1)];
        int countSplitAndLabelDistCol = 0;
        for(int instListCount = 0 ; instListCount < numInstLists ; instListCount++) {
            MLJ.ASSERT(currentLevel[instListCount] != null, "Entropy::build_split_and_label_dist: currentLevel[instListCount] == null.");
            for(int attrValCount = 0 ;
            attrValCount < numAttrValues +1 ; //add 1 to account for unknown being moved from -1 to 0 -JL
            attrValCount++, countSplitAndLabelDistCol++) {
                for(int labelValCount = 0 ; labelValCount < numLabelValues ;
                labelValCount++) {
                    BagCounters bc = currentLevel[instListCount].counters();
                    splitAndLabelDist[labelValCount][countSplitAndLabelDistCol]=
                    bc.value_counts()[attrNum][labelValCount+1][attrValCount];
                    //labelValCount needs 1 added because the unknown label has
                    //has been moved from -1 to 0. The first nominal label is now
                    //at 1, not 0, in the BagCounters class. -JL
                }
            }
        }
        // Assign the unaccounted to the last split with all counts
        //   for one label (so it looks pure).
        if (unaccountedWeight > 0) {
            MLJ.ASSERT(countSplitAndLabelDistCol == splitAndLabelDist[0][splitAndLabelDist.length], "Entropy::build_split_and_label_dist: countSplitAndLabelDistCol != splitAndLabelDist[0][splitAndLabelDist.length]");
            for(int labelValCount = 0 ; labelValCount < numLabelValues ; labelValCount++)
                splitAndLabelDist[labelValCount][countSplitAndLabelDistCol]=(labelValCount != 0)? 0 : unaccountedWeight;
        }
        return splitAndLabelDist;
    }
    
    /** Returns the minSplit which is used in find_best_threshold(), given
     * lowerBoundMinSplit, upperBoundMinSplit, and minSplitPercent. This
     * function is called by inducers.
     * @return The minSplit which is used in find_best_threshold().
     * @param upperBoundMinSplit Upper bound for the minimum split value.
     * @param totalWeight The total weight of all instances in the list of instances for which a split
     * is requested.
     * @param numTotalCategories Number of possible values an instance may be categorized as.
     * @param lowerBoundMinSplit Lower bound for the minimum split value.
     * @param minSplitPercent The percentage of total weight per category that represents the minimum value
     * for a split.
     */
    public static double min_split(double totalWeight,
    int numTotalCategories, double lowerBoundMinSplit,
    double upperBoundMinSplit, double minSplitPercent) {
        if (numTotalCategories <= 0)
            Error.fatalErr("min_split: divisor (numTotalCategories) is zero or neg");
        if ((Globals.DBG)&&(lowerBoundMinSplit <= 0))
            Error.fatalErr("min_split:  lowerBoundMinSplit ("
            + lowerBoundMinSplit + ") must be at least one");
        double minSplit = minSplitPercent * totalWeight/  numTotalCategories;
        if (minSplit > upperBoundMinSplit)
            minSplit = upperBoundMinSplit;
        if (minSplit <= lowerBoundMinSplit)
            minSplit = lowerBoundMinSplit;
        MLJ.ASSERT(minSplit > 0, "Entropy::build_split_and_label_dist: minSplit <= 0");
        return minSplit;
    }
    
    /** Returns the minSplit which is used in find_best_threshold(), given
     * lowerBoundMinSplit, upperBoundMinSplit, and minSplitPercent. This
     * function is called by inducers.
     * @param instList The list of instances over which a split is requested.
     * @param upperBoundMinSplit Upper bound for the minimum split value.
     * @param lowerBoundMinSplit Lower bound for the minimum split value.
     * @param minSplitPercent The percentage of total weight per category that represents the minimum value
     * for a split.
     * @param ignoreNumCat Indicator that the number of values that an instance may be classified as should
     * be ignored for this split computation.
     * @return The minSplit which is used in find_best_threshold().
     */
    public static double min_split(InstanceList instList,
    double lowerBoundMinSplit, double upperBoundMinSplit,
    double minSplitPercent, boolean ignoreNumCat) {
        if (ignoreNumCat)
            return min_split(instList.total_weight(), 1, lowerBoundMinSplit, upperBoundMinSplit, minSplitPercent);
        else return min_split(instList.total_weight(), instList.num_categories(), lowerBoundMinSplit, upperBoundMinSplit, minSplitPercent);
    }
    
    /** Returns the minSplit which is used in find_best_threshold(), given
     * lowerBoundMinSplit, upperBoundMinSplit, and minSplitPercent. This
     * function is called by inducers.
     * @param instList The list of instances over which a split is requested.
     * @param upperBoundMinSplit Upper bound for the minimum split value.
     * @param lowerBoundMinSplit Lower bound for the minimum split value.
     * @param minSplitPercent The percentage of total weight per category that represents the minimum value
     * for a split.
     * @return The minSplit which is used in find_best_threshold().
     */
    public static double min_split(InstanceList instList,
    double lowerBoundMinSplit, double upperBoundMinSplit,
    double minSplitPercent) {
        return min_split(instList,lowerBoundMinSplit,upperBoundMinSplit,minSplitPercent,false);
    }
    
    
    /** Computes conditional entropy of the label given attribute X. From Ross,
     * Conditional entropy is defined as:                                          <BR>
     *             H(Y|X) = sum_x H(Y|X=x)*P(X=x).                            <BR>
     *                    = sum_x (-sum_y p(Y=y|X=x)log p(Y=y|X=x)) * P(X=x)  <BR>
     *             now derive Pagallo & Haussler's formula                    <BR>
     *                    = -sum_{x,y} p(Y=y, X=x) log p(Y=y|X=x)             <BR>
     *             Here we estimate p(Y=y, X=x) by counting, but if we
     *               have priors on the probabilities of the labels, then     <BR>
     *               p(x,y) = p(x|y)*p(y) = count(x,y)/s(y)* prior(y)         <BR>
     *               and p(x) = sum_y prior(y) count(x,y)/s(y).               <BR>
     *
     *             By counting we get the following:                          <BR>
     *             -sum_{x,y} num(Y=y,X=x)/num-rec * log num(Y=y,X=x)/num(X=x)
     *
     * @param splitAndLabelDist Distributions over each split and label pair.
     * @param splitDist The distribution over splits.
     * @param totalWeight The total weight distributed.
     * @return The conditional entropy.
     */
    public static double cond_entropy(double[][] splitAndLabelDist,
    double[] splitDist, double totalWeight) {//AttrAndOrigin class function
        //   DBG(MLC::verify_strictly_greater(totalWeight, 0,
        //				    "cond_entropy:: totalWeight must be "
        //				    "positive");
        
        double sum = MLJArray.sum(splitDist);
        MLJ.verify_approx_equal(totalWeight, sum,
        "cond_entropy:: totalWeight does not match splitDist");
        sum = Matrix.total_sum(splitAndLabelDist);
        MLJ.verify_approx_equal(totalWeight, sum,
        "cond_entropy:: totalWeight does not match splitAndLabelDist");
        //       DBGSLOW(
        //	       Array<Real> sDist(UNKNOWN_CATEGORY_VAL,
        //				splitAndLabelDist.num_cols());
        //	       splitAndLabelDist.sum_cols(sDist);
        //	       ASSERT(MLJ.approx_equal(sDist, splitDist));
        //	      )
        //       );
        DoubleRef H = new DoubleRef(0);
        for (int x = 0;
        x < splitAndLabelDist[0].length; x++) {
            double num_x = splitDist[x];
            if (MLJ.approx_equal(num_x, 0.0))
                continue;
            for (int y = 0;
            y < splitAndLabelDist.length; y++) {
                double num_xy = splitAndLabelDist[y][x];
                if (MLJ.approx_equal(num_xy, 0.0))
                    continue;
                H.value -= num_xy * log_bin(num_xy/num_x);
            }
        }
        H.value /= totalWeight; // We know this won't be division by zero.
        MLJ.clamp_above(H, 0, "cond_entropy: negative entropy not allowed");
        return H.value;
    }
    
    /** Computes conditional entropy of the label given attribute X. From Ross,
     * Conditional entropy is defined as:                                          <BR>
     *             H(Y|X) = sum_x H(Y|X=x)*P(X=x).                            <BR>
     *                    = sum_x (-sum_y p(Y=y|X=x)log p(Y=y|X=x)) * P(X=x)  <BR>
     *             now derive Pagallo & Haussler's formula                    <BR>
     *                    = -sum_{x,y} p(Y=y, X=x) log p(Y=y|X=x)             <BR>
     *             Here we estimate p(Y=y, X=x) by counting, but if we
     *               have priors on the probabilities of the labels, then     <BR>
     *               p(x,y) = p(x|y)*p(y) = count(x,y)/s(y)* prior(y)         <BR>
     *               and p(x) = sum_y prior(y) count(x,y)/s(y).               <BR>
     *
     *             By counting we get the following:                          <BR>
     *             -sum_{x,y} num(Y=y,X=x)/num-rec * log num(Y=y,X=x)/num(X=x)
     *
     * @param instList The instance list over which conditional entropy is calculated.
     * @param attrNumX The number of the attribute for which conditional entropy is requested.
     * @return The conditional entropy.
     */
    public static double cond_entropy(InstanceList instList, int attrNumX) {
        return cond_entropy(instList.counters().value_counts()[attrNumX],
        instList.counters().attr_counts()[attrNumX],
        instList.total_weight());
    }
    
    /** Compute the mutual information which is defined as I(Y;X) = H(Y) - H(Y|X). Some
     * researchers like Quinlan call this "gain." This is the amount of information
     * gained about the category value of an instance after we test the variable X.
     *
     * @param ent Entropy value.
     * @param splitAndLabelDist Distributions over each split and label pair.
     * @param splitDist The distribution over splits.
     * @param totalWeight Total weight of the Instances trained on.
     * @return The mutual information value.
     */
    public static double mutual_info(double ent,double[][] splitAndLabelDist,
    double[] splitDist, double totalWeight) {
        double condEntropy = Entropy.cond_entropy(splitAndLabelDist, splitDist,
        totalWeight);
        DoubleRef mi = new DoubleRef(ent - condEntropy);
        // Mutual information should never be negative; the following
        //   accounts for possible numerical representation errors.
        MLJ.clamp_above(mi, 0, "mutual_info: negative values not allowed");
        return mi.value;
    }
    
    /** Compute the mutual information which is defined as I(Y;X) = H(Y) - H(Y|X). Some
     * researchers like Quinlan call this "gain." This is the amount of information
     * gained about the category value of an instance after we test the variable X.
     * @param instList The instance list over which mutual information is calculated.
     * @param attrNumX The number of the attribute for which mutual information is requested.
     * @return The mutual information value.
     */
    public static double mutual_info(InstanceList instList, int attrNumX) {
        if (instList.counters().attr_counts()[attrNumX] == null)
            Error.fatalErr("entropy::mutual_info: attribute "+attrNumX+
            " is not nominal (counts array is NULL)");
        
        double ent = entropy(instList.counters().label_counts(),
        instList.total_weight());
        return mutual_info(instList, ent, attrNumX);
    }
    
    /** Compute the mutual information which is defined as I(Y;X) = H(Y) - H(Y|X). Some
     * researchers like Quinlan call this "gain." This is the amount of information
     * gained about the category value of an instance after we test the variable X.
     * @param instList The instance list over which mutual information is calculated.
     * @param ent Entropy value.
     * @param attrNumX The number of the attribute for which mutual information is requested.
     * @return The mutual information value.
     */
    public static double mutual_info(InstanceList instList,
    double ent, int attrNumX) {
        if (instList.counters().attr_counts()[attrNumX] == null)
            Error.fatalErr("entropy::mutual_info: attribute "+attrNumX+
            " is not nominal (counts array is NULL)");
        
        return mutual_info(ent,
        instList.counters().value_counts()[attrNumX],
        instList.counters().attr_counts()[attrNumX],
        instList.total_weight());
    }
    
    
    /** Builds the distribution arrays necessary for calculating conditional entropy for
     * nominal attributes. All of the splitAndLabelDist arrays of the Instance Lists are
     * concatenated. The unaccounted instances allow the list of nodes to be partial,
     * i.e., not to contain all instances. The split will be created so that the
     * unaccounted instances are in an extra split with the same label, so that the
     * entropy will be decreased correctly as if they were in a pure node.
     *
     * @param currentLevel The list of instances in the current partition for which a split is being
     * determined.
     * @param attrNum The number of the attribute for which mutual information is requested.
     * @return The distribution over splits.
     */
    static public double[] build_nominal_attr_split_dist(InstanceList[] currentLevel,
    int attrNum) {
        return build_nominal_attr_split_dist(currentLevel,attrNum,0);
    }
    
    /** Builds the distribution arrays necessary for calculating conditional entropy for
     * nominal attributes. All of the splitAndLabelDist arrays of the Instance Lists are
     * concatenated. The unaccounted instances allow the list of nodes to be partial,
     * i.e., not to contain all instances. The split will be created so that the
     * unaccounted instances are in an extra split with the same label, so that the
     * entropy will be decreased correctly as if they were in a pure node.
     *
     * @param currentLevel The list of instances in the current partition for which a split is being
     * determined.
     * @param attrNum The number of the attribute for which mutual information is requested.
     * @param unaccountedWeight Weight that is not accounted for in the list of instances.
     * @return The distribution over splits.
     */
    static public double[] build_nominal_attr_split_dist(InstanceList[] currentLevel,
    int attrNum, double unaccountedWeight) {
        MLJ.ASSERT(currentLevel[0]!= null,"Entropy.build_nominal_attr_split_dist:currentLevel[0]== null.");
        Schema schema = currentLevel[0].get_schema();
        int numInstLists = currentLevel.length;
        int numAttrValues = schema.num_attr_values(attrNum);
        
        MLJ.ASSERT(numInstLists > 0,"Entropy.build_nominal_attr_split_dist:numInstLists <= 0");
        MLJ.ASSERT(numAttrValues > 0,"Entropy.build_nominal_attr_split_dist:numAttrValues <= 0");
        
        int unaccnt_wght_col = (unaccountedWeight > 0)? 1 : 0;
        double[] splitDist = new double[numInstLists * (numAttrValues + 1) + unaccnt_wght_col];
        int countSplitDist = Globals.UNKNOWN_CATEGORY_VAL;
        
        for (int instListCount = 0; instListCount < numInstLists; instListCount++) {
            MLJ.ASSERT(currentLevel[instListCount] != null,"Entropy.build_nominal_attr_split_dist:currentLevel[instListCount] == null");
            for (int attrCount = Globals.UNKNOWN_CATEGORY_VAL; attrCount < numAttrValues;
            attrCount++, countSplitDist++) {
                BagCounters bc = currentLevel[instListCount].counters();
                splitDist[countSplitDist] = bc.attr_counts()[attrNum][attrCount];
            }
        }
        if (unaccountedWeight > 0) {
            MLJ.ASSERT(countSplitDist == splitDist.length,"Entropy.build_nominal_attr_split_dist:countSplitDist != splitDist.length");
            splitDist[countSplitDist] = unaccountedWeight;
        }
        return splitDist;
    }
    
    
    /** Compute the J-measure. See papers by Goodman and Smyth, such as Data
     * Engineering, v.4, no.4, pp.301-316, 1992. The J-measure summed over all
     * values of x gives info-gain. The J-measure is                               <BR>
     * sum_y p(x,y)log(p(x,y)/(p(x)p(y)))                                          <BR>
     * 1/n * sum_y n(x,y)log(n(x,y)*n/(n(x)n(y)))                                  <BR>
     * Used in t_entropy.java.
     *
     * @return The j-measure value.
     * @param splitAndLabelDist Distributions over each split and label pair.
     * @param splitDist The distribution over splits.
     * @param labelCounts Counts of each label found in the data.
     * @param x The x value for the j-measure equation.
     * @param totalWeight Total weight of all data.
     */
    
    public static double j_measure(double[][] splitAndLabelDist,
    double[] splitDist, double[] labelCounts,
    int x, double totalWeight) {
        MLJ.verify_strictly_greater(totalWeight, 0, "j_measure: totalWeight is "+
        "too small");
        
        DoubleRef j = new DoubleRef();
        for (int y = 0;
        y < splitAndLabelDist.length; y++) {
            double num_xy = splitAndLabelDist[y][x];
            double num_x  = splitDist[x];
            double num_y  = labelCounts[y];
            if (!MLJ.approx_equal(num_xy, 0.0)) { // beware of log(0)
                if (Globals.DBG) MLJ.ASSERT((num_x > 0 && num_y > 0),"Entropy.j_measure: num_x <= 0 || num_y <= 0");
                j.value += num_xy *
                log_bin(totalWeight*(num_xy)/(num_x * num_y));
            }
        }
        j.value /= totalWeight; // We know this won't be division by zero.
        
        // Allow for possible numerical representation errors.
        MLJ.clamp_above(j, 0, "j_measure: negative j-measure not allowed");
        
        return j.value;
    }
    
    /** Compute the J-measure. See papers by Goodman and Smyth, such as Data
     * Engineering, v.4, no.4, pp.301-316, 1992. The J-measure summed over all
     * values of x gives info-gain. The J-measure is                               <BR>
     * sum_y p(x,y)log(p(x,y)/(p(x)p(y)))
     * 1/n * sum_y n(x,y)log(n(x,y)*n/(n(x)n(y)))                                  <BR>
     * Used in t_entropy.java.
     *
     * @param instList The list of Instances over which a j measure is to be
     * calculated.
     * @param attrNumX The number of attributes in the Schema of the Instances
     * supplied.
     * @param x The x value for the j-measure equation.
     * @return The j-measure value.
     */
    public static double j_measure(InstanceList instList, int attrNumX, int x) {
        return j_measure(instList.counters().value_counts()[attrNumX],
        instList.counters().attr_counts()[attrNumX],
        instList.counters().label_counts(), x,
        instList.total_weight());
    }
    
    
    /** Builds columns of real values and their associated label values. Invokes
     * InstanceList's transpose function to provide a single column for the passed
     * attribute number, sorts it, and returns the columns to the caller. The second
     * calling argument, if set to an attribute index, results in a single column
     * being transposed and sorted. When set to UNDEFINED_INT, all columns are
     * so treated.
     * @param instList The instance list containing the instance values for the attribute.
     * @param attrNum The number of the attribute for which the real and label column is
     * requested.
     * @return The columns of real values and their associated labels, organized by attribute.
     */
    public static RealAndLabelColumn[] build_real_and_label_columns(
    InstanceList instList, int attrNum) {
        // We initialize the array to FALSE, except for any element(s)
        //   we want to get the RealAndLabelColumn for, which is/are set to TRUE.
        boolean initializer = (attrNum == Globals.UNDEFINED_INT) ? true : false;
        boolean[] transp = new boolean[instList.get_schema().num_attr()];
        Arrays.fill(transp, initializer);
        if (attrNum != Globals.UNDEFINED_INT)
            transp[attrNum] = true;
        RealAndLabelColumn[] columns = instList.transpose(transp);
        
        // If a particular column was requested, check that it was transposed.
        if (attrNum != Globals.UNDEFINED_INT)
            if (columns[attrNum] != null)
                columns[attrNum].sort();
            else
                Error.fatalErr("build_real_and_label_columns: for attribute " +attrNum
                +", no column was built to sort");
        else
            for (int x = 0; x < instList.get_schema().num_attr(); x++)
                if (columns[x] != null)
                    columns[x].sort();
        return columns;
    }
    
    /** Builds a column of real values and their associated label values for the given
     * attribute. Invokes InstanceList's transpose function to provide a single column
     * for the passed attribute number, sorts it, and returns the columns to the caller.
     * The second calling argument, if set to an attribute index, results in a single
     * column being transposed and sorted. When set to UNDEFINED_INT, all columns are
     * so treated.
     * @param instList The instance list containing the instance values for the attribute.
     * @param attrNum The number of the attribute for which the real and label column is
     * requested.
     * @return The column of real values and their associated labels.
     */
    public static RealAndLabelColumn build_real_and_label_column(InstanceList
    instList, int attrNum) {
        RealAndLabelColumn[] columns = build_real_and_label_columns(instList, attrNum);
        // We want to pass the sorted column back to the caller, but delete
        //   the rest of the array.  Save a reference to the single desired
        //   column, and set the entry in the array that points to it to NULL so
        //   that when the array's deleted, the column isn't.  The caller must
        //   delete the single column.
        RealAndLabelColumn sortedColumn = columns[attrNum];
        columns[attrNum] = null;
        columns = null;
        return sortedColumn;
    }
    
}

