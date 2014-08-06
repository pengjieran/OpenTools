package arithmetic.shared;
import java.util.Arrays;

/** The CatDist class is for representing a distribution of categories. A
 * CatDist object is produced by a categorizer during the scoring process.
 * A loss function may optionally be applied to the CatDist.                <P>
 * It is assumed the distribution is normalized. This is done automatically
 * on construction. The internal array dist should be indexed by category
 * number, starting with UNKNOWN_CATEGORY_VAL.
 *
 * @author James Louis	2/25/2001	Ported to Java.
 * @author Dan Sommerfield	2/10/97	Initial revision.
 */
public class CatDist {
    //CorrectionType ENUM
    /** None Correction Type value.**/
    static public final int none = 0;
    /** Laplace Correction Type value.**/
    static public final int laplace = 1;
    /** Evidence Correction Type value.**/
    static public final int evidence = 2;
    //END CorrectionType ENUM
    
    /** The Schema for the data for which distribution is to be
        calculated. **/
    private Schema schema;
    
    /** The distribution of categories **/
    private double[] dist;
    
    /** The order for used in the event that two categories have the same
        distribution. **/
    private int[] tiebreakingOrder;
    
    /** The options for logging displays. **/
    public static LogOptions logOptions = new LogOptions();
    
    /** Constructor. It builds a distribution based on a single category, with a
     * 1.0 probability given to this category, and 0.0 to all others.
     * @param aSchema	The Schema for the data in this distribution.
     * @param aug		The AugCategory with information on the category on which
     * this distribution is built.
     */
    public CatDist(Schema aSchema, AugCategory aug) {
        schema = aSchema;
        dist = new double[aSchema.num_label_values() +1];
        tiebreakingOrder = new int[aSchema.num_label_values() + 1];
        MLJArray.init_values(-1,tiebreakingOrder);
        set_scores(aug.num());
    }
    
    /** Constructor. It builds an all-or-nothing distribution based on a single
     * category, with a 1.0 probability given to this category, and 0.0 to all
     * others.
     * @param aSchema	The Schema for the data in this distribution.
     * @param singleCat	The specific category on which this distribution is built.
     */
    public CatDist(Schema aSchema, int singleCat) {
        schema = aSchema;
        dist = new double[aSchema.num_label_values() + 1];
        tiebreakingOrder = new int[aSchema.num_label_values() + 1];
        MLJArray.init_values(-1,tiebreakingOrder);
        set_scores(singleCat);
    }
    
    /** Constructor.
     * @param aSchema	The Schema for the data in this distribution.
     * @param fCounts	The frequency count of categories found as labels.
     * @param cType	Type of correction to perform. Range is CatDist.none,
     * CatDist.laplace, CatDist.evidence.
     */
    public CatDist(Schema aSchema, double[] fCounts, int cType) {
        schema = aSchema;
        dist = new double[aSchema.num_label_values() + 1];
        tiebreakingOrder =new int[aSchema.num_label_values() + 1];
        MLJArray.init_values(-1,tiebreakingOrder);
        
        set_preferred_category(0);
        set_scores(fCounts, cType, 1.0);
        set_default_tiebreaking();
    }
    
    /** Constructor.
     * @param aSchema	The Schema for the data in this distribution.
     * @param fCounts	The frequency count of categories found as labels.
     * @param cType	Type of correction to perform. Range is CatDist.none,
     * CatDist.laplace, CatDist.evidence.
     * @param cParam	Correction parameter. Must be equal to or greater than 0.
     */
    public CatDist(Schema aSchema, double[] fCounts, int cType, double cParam) {
        schema = aSchema;
        dist = new double[aSchema.num_label_values() + 1];
        tiebreakingOrder =new int[aSchema.num_label_values() + 1];
        MLJArray.init_values(-1,tiebreakingOrder);
        
        set_preferred_category(0);
        set_scores(fCounts, cType, cParam);
        set_default_tiebreaking();
    }
    
    /** Constructor.
     * @param aSchema	The Schema for the data in this distribution.
     * @param unknownProb	The desired probability weight for the unknown
     * category.
     * @param aDist	A weight distribution for this CatDist object.
     */
    public CatDist(Schema aSchema, DoubleRef unknownProb,
    double[] aDist) {
        schema = aSchema;
        dist = new double[aSchema.num_label_values() + 1];
        tiebreakingOrder =new int[aSchema.num_label_values() + 1];
        MLJArray.init_values(-1,tiebreakingOrder);
        
        set_preferred_category(0);
        set_scores(unknownProb, aDist);
        set_default_tiebreaking();
    }
    
    /** Copy constructor.
     * @param cDist	The CatDist object to be copied.
     */
    public CatDist(CatDist cDist) {
        schema = cDist.schema;
        dist =(double[]) cDist.dist.clone();
        tiebreakingOrder =(int[]) cDist.tiebreakingOrder.clone();
    }
    
    
    /** Converts the distribution scores to a String.
     * @return A String containing information about the scores.
     */
    private String scoresToString() {
        int i;
        String rtrn = new String();
        for(i = 0 ; i < dist.length-1 ; i++)
            rtrn = rtrn +(int) dist[i]+", ";
        rtrn = rtrn +(int) dist[i];
        return rtrn;
    }
    
    /** Merges the tie breaking order with the given weight distribution.
     * @return The tie breaking order.
     * @param weightDistribution	The given weight distribution of categories.
     */
    public static int[] merge_tiebreaking_order(double[] weightDistribution) {
        double[] dist =(double[]) weightDistribution.clone();
        //      if (Globals.DBG)
        //         MLJ.ASSERT(dist.min() >= 0 || MLJ.approx_equal(dist.min(), 0.0),
        //               "CatDist::merge_tiebreaking_order: Minimum distribution < 0.");
        int[] order = new int[dist.length];
        MLJArray.init_values(Integer.MAX_VALUE,order);
        
        if (dist[0] == Globals.UNKNOWN_CATEGORY_VAL &&
        MLJ.approx_equal(dist[0], 0.0))
            dist[0] = -1;
        int nextIndex = 0;
        
        for(int i = 0 ; i < order.length ; i++) {
            IntRef highestIndex = new IntRef(0);
            MLJArray.max(highestIndex,dist);
            //         if (Globals.DBG)
            //            MLJ.ASSERT(order[highestIndex.value] == Globals.INT_MAX,
            //                  "CatDist::merge_tiebreaking_order: order[highestIndex]"
            //                  + " != Globals.INT_MAX.");
            order[highestIndex.value] = nextIndex++;
            dist[highestIndex.value] = -1;
        }
        MLJ.ASSERT(nextIndex == order.length, "CatDist::merge_tiebreaking_order: nextIndex == order.length");
        return order;
    }
    
    /** Finds the majority category in the given weight distribution, using the
     * given tie breaking order.
     * @return The category which appears the most among the labelled instances.
     * @param weightDistribution	The weight sums for each category found.
     * @param tieBreakingOrder		The order of choices in the event that a tie
     * occurs between categories.
     */
    public static int majority_category(double[] weightDistribution, int[] tieBreakingOrder) {
        IntRef bestIndex = new IntRef(0);
        double highestWeight = MLJArray.max(bestIndex,weightDistribution);
        int lastIndex = bestIndex.value;
        
        while((lastIndex = MLJArray.find(highestWeight, lastIndex + 1,weightDistribution)) != -1)
            if (tieBreakingOrder[lastIndex] <
            tieBreakingOrder[bestIndex.value])
                bestIndex.value = lastIndex;
        return bestIndex.value + Globals.UNKNOWN_CATEGORY_VAL;
    }
    
    /** Merges a given tie breaking order with the given weight distribution.
     * @return The tie breaking order.
     * @param tieBreakingOrder		The order for choices in the event that a tie
     * occurs between categories.
     * @param weightDistribution	The given weight distribution of categories.
     */
    static public int[] merge_tiebreaking_order(int[] tieBreakingOrder,
    double[] weightDistribution) {
        double[] dist =(double[]) weightDistribution.clone();
        int[] order = new int[dist.length];
        MLJArray.init_values(Integer.MAX_VALUE, order);
        
        IntRef bestIndex = new IntRef(0);
        int lastIndex;
        double highestWeight;
        
        int ordering = 0;
        
        for(int i = 0 ; i < order.length ; i++) {
            highestWeight = MLJArray.max(bestIndex, dist);
            lastIndex = bestIndex.value;
            while((lastIndex = MLJArray.find(highestWeight, lastIndex + 1, dist)) != -1)
                if (tieBreakingOrder[lastIndex] < tieBreakingOrder[bestIndex.value])
                    bestIndex.value = lastIndex;
            //         if (Globals.DBG)
            //            MLJ.ASSERT(order[bestIndex.value] == Globals.INT_MAX,"CatDist::"
            //                  +"merge_tiebreaking_order: order[bestIndex] != "
            //                  +"Globals.INT_MAX.");
            order[bestIndex.value] = ordering++;
            dist[bestIndex.value] = -1;
        }
        MLJ.ASSERT(ordering == order.length, "CatDist::merge_tiebreaking_order: ordering == order.length");
        
        return order;
    }
    
    /** Returns the Schema stored in this CatDist object.
     * @return The Schema for data on which this CatDist object contains
     * information.
     */
    public Schema get_schema() {
        return schema;
    }
    
    /** Allows the results stored in and returned by a CatDist to be changed.
     * This method takes a single category index and builds an all-or-nothing
     * distribution around it. 1.0 probability mass is given to the single category
     * and 0.0 is given to all others.
     * @param singleCat	The index for the category that should have a 1.0
     * probability mass.
     */
    public void set_scores(int singleCat) {
        for(int i = 0 ; i<dist.length ; i++)
            dist[i] = 0.0;
        dist[singleCat] = 1.0;
        set_preferred_category(singleCat);
    }
    
    /** Specifies a single category to prefer if it is ever involved in a tie. If
     * a tie occurs which does not involve the preferred category, the first
     * category (but never unknown) will be chosen.
     * @param cat	The index of the category to be preferred.
     */
    public void set_preferred_category(int cat) {
        if (cat < Globals.UNKNOWN_CATEGORY_VAL ||
        cat > Globals.UNKNOWN_CATEGORY_VAL+schema.num_label_values())
            Error.fatalErr("CatDist::set_preferred_category: specified category "
            +cat+ " is out of range");
        // set up an ordering vector.  Rank the preferred category first
        // (0), then give lower numbers in order to the other categories
        // from first to last.  Assign the unknown category the lowest value.
        int val = 0;
        tiebreakingOrder[cat] = val++;
        for(int i=Globals.FIRST_CATEGORY_VAL ; i < tiebreakingOrder.length ; i++)
            if (i != cat)
                tiebreakingOrder[i] = val++;
        if (cat != Globals.UNKNOWN_CATEGORY_VAL)
            tiebreakingOrder[Globals.UNKNOWN_CATEGORY_VAL] = val++;
        MLJ.ASSERT(val == schema.num_label_values() + 1, "CatDist::set_preferred_category: val == schema.num_label_values() + 1");
        //      if (Globals.DBG)
        //         check_tiebreaking_order(get_tiebreaking_order());
    }
    
    /** Builds a tie breaking order from a weight distribution. If there is a tie
     * among weights, the first one will have a better (i.e. lower) tie breaking
     * rank.
     * @return The tie breaking order of ranks.
     * @param weightDistribution	The distribution of weights for label
     * categories.
     */
    static public int[] tiebreaking_order(double[] weightDistribution) {
        double[] dist =(double[]) weightDistribution.clone();
        //      if(Globals.DBG)
        //         MLJ.ASSERT(dist.min() >= 0 || MLJ.approx_equal(dist.min(), 0.0),
        //               "CatDist::tiebreaking_order: Minimum distribution < 0.");
        int[] order = new int[dist.length];
        
        if (0 == Globals.UNKNOWN_CATEGORY_VAL &&
        MLJ.approx_equal(dist[0], 0.0))
            dist[0] = -1;
        int nextIndex = 0;
        for(int i = 0 ; i < order.length ; i++) {
            IntRef highestIndex = new IntRef(0);
            MLJArray.max(highestIndex,dist);
            //         if(Globals.DBG)
            //            MLJ.ASSERT(order[highestIndex] == Globals.INT_MAX,
            //               "CatDist::tiebreaking_order: order[highestIndex] != Globals.INT_MAX.");
            
            order[highestIndex.value] = nextIndex++;
            dist[highestIndex.value] = -1;
        }
        MLJ.ASSERT(nextIndex == order.length, "CatDist::tiebreaking_order: nextIndex == order.length");
        
        return order;
    }
    
    /** Returns the best category according to the weight distribution. If a loss
     * matrix is defined, the distribution will be multiplied by the loss matrix to
     * produce a vector of expected losses.  The best category is the one with the
     * smallest expected loss.
     * @return An AugCategory containing information about the best category found.
     */
    public AugCategory best_category() {
        // having no values at all is an error
        MLJ.ASSERT(dist.length > 0, "CatDist::best_category: dist.length > 0");
        double bestProb = -1;
        int bestCat = -1;
        
        // If a loss matrix is defined, multiply it by the scoring vector
        // to obtain the loss vector.
        if (schema.has_loss_matrix()) {
            double[][] lossMatrix = schema.get_loss_matrix();
            double[] lossVector = new double[schema.num_label_values() + 1];
            
            multiply_losses(lossMatrix, dist, lossVector);
            
            // these are LOSSES, so pick the smallest one
            bestProb = Double.MAX_VALUE;
            for(int i=0 ; i < lossVector.length ; i++) {
                if (MLJ.approx_equal(lossVector[i], bestProb)) {
                    if (tiebreakingOrder[i] < tiebreakingOrder[bestCat]) {
                        bestProb = lossVector[i];
                        bestCat = i;
                    }
                }
                else if (lossVector[i] < bestProb) {
                    bestProb = lossVector[i];
                    bestCat = i;
                }
            }
            logOptions.LOG(4, "Probs: " +scoresToString() + ".  Loss vector: "
            +lossVector+ ".  Picked " +bestCat+ '\n');
        }
        // Otherwise, just pick the highest probability in the distribution.
        // In the event of a tie, prefer the category with the LOWER
        // tiebreakingOrder.
        else {
            //      MLJ.ASSERT(dist.low() == Globals.UNKNOWN_CATEGORY_VAL,"CatDist::best_category: dist.low() == Globals.UNKNOWN_CATEGORY_VAL");
            for(int i=0 ; i < dist.length ; i++) {
                if (MLJ.approx_equal(dist[i], bestProb)) {
                    // we have a tie.
                    if (tiebreakingOrder[i] < tiebreakingOrder[bestCat]) {
                        bestProb = dist[i];
                        bestCat = i;
                    }
                }
                else if (dist[i] > bestProb) {
                    // always pick this category--its the best so far
                    bestProb = dist[i];
                    bestCat = i;
                }
            }
            
            logOptions.LOG(4, "Probs: " +scoresToString() + ".  Picked " +bestCat+ '\n');
            if (bestCat == Globals.UNKNOWN_CATEGORY_VAL &&
            !GlobalOptions.allowUnknownPredictions) {
                Error.err("CatDist::best_category: attempting to predict "
                + "UNKNOWN without a loss matrix set.  Set "
                + "ALLOW_UNKNOWN_PREDICTIONS to Yes to deactivate this "
                + "error.");
                Error.err("Probabilities: " +dist+ '\n');
                Error.err("Tie breaking order: " +tiebreakingOrder+ '\n');
                Error.fatalErr("");
            }
        }
        
        if (bestCat == Globals.UNKNOWN_CATEGORY_VAL)
            return Globals.UNKNOWN_AUG_CATEGORY;
        else
            return new AugCategory(bestCat,schema.nominal_label_info() .get_value(bestCat));
    }
    
    /** Sets the distribution scores for the current distribution.
     * @param fCounts	The frequency counts of categories found.
     * @param cType	Type of correction to perform. Range is CatDist.none,
     * CatDist.laplace, CatDist.evidence.
     * @param cParam	Correction parameter. Must be equal to or greater than 0.
     */
    public void set_scores(double[] fCounts,
    int cType, double cParam) {
        if (fCounts.length != dist.length)
            Error.fatalErr("CatDist::set_scores: size of frequency counts array ("
            +fCounts.length+ ") does not match number of categories "
            + "in data (including unknown) (" +dist.length+ ").  "
            + "It is possible you are using the wrong version of "
            + "the CatDist constructor");
        int numLabelVals = schema.num_label_values();
        
        // compute the total sum of the counts.  Negative frequency counts
        // are not permitted, but slightly negative ones (negative only by
        // error) will be clamped to zero.
        double total = 0;
        for(int i=0 ; i<fCounts.length ; i++) {
            DoubleRef val =new DoubleRef(fCounts[i]);
            MLJ.clamp_above(val, 0.0, "CatDist::CatDist: negative frequency counts "
            + "are not permitted" , fCounts.length);
            total += val.value;
        }
        
        // If all counts are zero, make an all-even distribution,
        // but give the unknown class zero weight.
        if (MLJ.approx_equal(total, 0.0)) {
            double evenProb = 1.0/ (dist.length -1);
            dist[0] = 0.0;
            for(int i=1 ; i<dist.length ; i++)
                dist[i] = evenProb;
            total = 1.0;
        }
        // compute probabilities.  The method depends on the correction type
        else {
            switch (cType) {
                // frequency counts: normalize the counts.
                case  none: {
                    MLJ.ASSERT(!MLJ.approx_equal(total, 0.0) , "CatDist::set_scores: !MLJ.approx_equal(total, 0.0)");
                    for(int i=1 ; i<dist.length ; i++)
                        dist[i] = fCounts[i]/  total;
                }
                break;
                
                // Laplace correction: each count is equal to
                // (fCount + cParam) / (total + cParam)
                // zero cParam means use 1/total as the correction factor.
                case  laplace: {
                    if (cParam < 0.0)
                        Error.fatalErr("CatDist::CatDist: negative correction parameter "
                        + "(cParam) values are not permitted for laplace correction");
                    MLJ.verify_strictly_greater(total + cParam, 0, "CatDist::CatDist: "
                    + " total + cParam too clost to zero");
                    double finalCorrection =(cParam == 0.0) ?(1.0 / total) : cParam;
                    for(int i=1 ; i<dist.length ; i++) {
                        double divisor = total + numLabelVals * finalCorrection;
                        
                        if (MLJ.approx_equal(divisor, 0.0))
                            Error.fatalErr("CatDist::CatDist: divisor too close to zero");
                        dist[i] =(fCounts[i] + finalCorrection)/  divisor;
                    }
                    
                }
                break;
                
                // Evidence projection algorithm:
                case  evidence: {
                    if (cParam <= 0.0)
                        Error.fatalErr("CatDist::CatDist: negative or zero correction parameter "
                        + "(cParam) values are not permitted for evidence projection");
                    // copy fCounts into dist
                    // fCounts may have a different bound than dist, so we can't
                    // use operator=.
                    for(int i=0 ; i<fCounts.length ; i++)
                        dist[i] = fCounts[i];
                    
                    // correct
                    apply_evidence_projection(dist, cParam, true);
                }
                break;
                
                default:
                    MLJ.Abort();
            }
            
            // Assign unknown the remainder of the distribution
            double newTotal = 0;
            for(int i=1 ; i<dist.length ; i++)
                newTotal += dist[i];
            dist[0] =(1.0 - newTotal);
            
            // If unknown is near zero, pin it to zero and renormalize
            // the other probabilities.  This will prevent the unknown
            // from picking up probability mass due to numerical errors
            if (MLJ.approx_equal(dist[0], 0.0)) {
                dist[0] = 0;
                for(int i=1 ; i<dist.length ; i++)
                    dist[i] /= newTotal;
            }
            // WARNING: Do not attempt to pin near-zero and near-one
            // values here--this will can cause the distribution not
            // to sum to 1.0!
        }
    }
    
    
    /** Sets the distribution scores for the given distribution.
     * @param unknownProb	The probability weight for the unknown category.
     * @param aDist		The distribution weights. This will be altered by
     * this method. Must have a length equal to the
     * number of categories.
     */
    public void set_scores(DoubleRef unknownProb, double[] aDist) {
        MLJ.clamp_to_range(unknownProb, 0, 1, "CatDist::CatDist: probability "
        + "selected for unknown values must be in range "
        + "[0.0, 1.0]");
        
        if (aDist.length != schema.num_label_values())
            Error.fatalErr("CatDist::CatDist: size of distribution array (" +
            aDist.length + ") does not match number of categories in data "
            + "(" + schema.num_label_values() + ")");
        
        // normalize probabilities
        double remainder = 1.0 - unknownProb.value;
        double total = 0;
        for(int i=0 ; i<schema.num_label_values() ; i++)
            total += aDist[i];
        
        // Again, don't divide by zero.
        if (!MLJ.approx_equal(total, 0.0))
            for(int i=1 ; i<schema.num_label_values() + 1 ; i++) {
                dist[i] = aDist[i-1] * remainder/  total;
            }
        else
            for(int i=1 ; i<dist.length ; i++)
                dist[i] = 0.0;
        
        dist[0] = unknownProb.value;
    }
    
    /** Sets the tiebreaking order to the default values.
     */
    public void set_default_tiebreaking() {
        int val = 0;
        for(int i=Globals.FIRST_CATEGORY_VAL ; i<tiebreakingOrder.length ; i++)
            tiebreakingOrder[i] = val++;
        tiebreakingOrder[Globals.UNKNOWN_CATEGORY_VAL] = val++;
        //   ASSERT(val == schema.num_label_values() + 1,"CatDist::set_default_tiebreaking: val == schema.num_label_values() + 1");
        //   DBG(check_tiebreaking_order(get_tiebreaking_order()));
    }
    
    /** Returns the distribution scores.
     * @return The distribution of scores.
     */
    public double[] get_scores() {
        return dist;
    }
    
    /** Multiplies the given distribution by the given loss matrix to produce
     * a vector of expected losses.
     * @param lossMatrix	The loss matrix.
     * @param probDist	The probability distribution for which loss is to be
     * calculated. Must have the same bounds as the number
     * of columns in the loss matrix.
     * @param lossVector	Contains the vector of expected losses. Will be changed by
     * this function. Must have the same bounds as the number
     * of columns in the loss matrix.
     */
    public static void multiply_losses(double[][] lossMatrix,
    double[] probDist,
    double[] lossVector) {
        // check: lossVector and probDist should have the same bounds.
        // these should equal the firstCol and highCol of the lossMatrix
        //   ASSERT(probDist.low() == lossVector.low());
        //   ASSERT(lossVector.low() == lossMatrix.start_col());
        //   ASSERT(probDist.high() == lossVector.high());
        //   ASSERT(lossVector.high() == lossMatrix.high_col());
        
        // check: lossMatrix should have one fewer rows than columns
        //   ASSERT(lossMatrix.start_row() == lossMatrix.start_col() + 1);
        //   ASSERT(lossMatrix.high_row() == lossMatrix.high_col());
        
        // now multiply
        for(int lvIndex = 0 ; lvIndex <= lossVector.length ; lvIndex++) {
            lossVector[lvIndex] = 0;
            // ignore unknowns in the probability distribution
            for(int pdIndex =1 ; pdIndex <= probDist.length ; pdIndex++)
                lossVector[lvIndex] += probDist[pdIndex] * lossMatrix[pdIndex][lvIndex];
        }
    }
    
    /** Applies the evidence projection algorithm. This function is used by
     * CatDist's auto-correction mode, and also inside NaiveBayesCat when it turns
     * on evidence projection.
     * @param counts		Counts is an array of frequency counts. It is
     * assumed that the sum of these counts is the
     * total weight of information(i.e. total number
     * of instances). This total weight is scaled
     * by eviFactor.
     * @param eviFactor		Factor for computing the total evidence available.
     * @param firstIsUnknown	Setting firstIsUnknown to TRUE will cause the first
     * value in the counts array to be treated as
     * "unknown"-- it will not participate in the
     * projection algorithm but will reduce
     * probability weight given to the other counts.
     * The counts array is adjusted in-place to
     * become a normalized array of corrected
     * probabilities.
     */
    public static void apply_evidence_projection(double[] counts, double eviFactor,
    boolean firstIsUnknown) {
        // If firstIsUnknown is set, we must have at least one element in
        // counts
        if (firstIsUnknown && counts.length == 0)
            Error.fatalErr("apply_evidence_projection: you have selected the first "
            + "count to be UNKNOWN, but the counts array has size 0");
        double[] projProbs = new double[counts.length];
        double total = MLJArray.sum(counts);
        double totalKnown = total;
        if (firstIsUnknown)
            totalKnown -= counts[0];
        
        // Compute total evidence available.  This is computed as
        // log(1+total*eviFactor).
        double logTotalInfo = MLJ.log_bin((total * eviFactor) + 1.0);
        
        // Apply evidence projection; we convert to logs, project
        // infinite evidence onto the maximum evidence available
        // (logTotalInfo), then convert back to probabilities.
        double projTotal = 0;
        int start =(firstIsUnknown) ? 1 : 0;
        for(int i=start ; i<counts.length ; i++) {
            // project the single probability
            projProbs[i] = single_evidence_projection(counts[i],
            total,
            logTotalInfo);
            
            // Add to the new sum (for normalization)
            projTotal += projProbs[i];
        }
        
        // renormalize.  Leave out original unknown probability mass.
        double reNormFactor = totalKnown/  total;
        MLJ.verify_strictly_greater(projTotal, 0, "apply_evidence_projection: "
        + "projection total must be non-negative");
        for(int i =start ; i<counts.length ; i++)
            counts[i] = projProbs[i] * reNormFactor/  projTotal;
        
        // If firstIsUnknown is set, give it mass
        if (firstIsUnknown)
            counts[0] = counts[0]/  total;
    }
    
    /** Returns a single, unnormalized, evidence projection of a count based on
     * the max evidence available.
     * @return An evidence projection of the category count.
     * @param count		The count of a particular category.
     * @param total		The total count of all categories.
     * @param maxEvidence	Projection factor.
     */
    public static double single_evidence_projection(double count, double total, double maxEvidence) {
        double normProb = count/  total;
        
        // Pin values which are near 1.0 or 0.0
        if (MLJ.approx_equal(normProb, 0.0))
            normProb = 0.0;
        else if (MLJ.approx_equal(normProb, 1.0))
            normProb = 1.0;
        
        // compute evidence and weight.  For finite evidence,
        // weight is simply 1.0.  For infinite evidence, weight is 0.0.
        // We can use == comparison to 0.0 because we pinned the value
        // above.
        double evidence;
        double weight;
        if (normProb == 0.0) {
            evidence = 1.0;
            weight = 0.0;
        }
        else {
            evidence = -MLJ.log_bin(normProb);
            weight = 1.0;
        }
        
        // compute the projected probability using the weight
        // This is a 1D projection in homogenous coordinates.
        // Infinity will be projected onto logTotalInfo.
        if (MLJ.approx_equal(evidence + maxEvidence * weight, 0.0))
            Error.fatalErr("single_evidence_projection: divisor too close to zero");
        double projEvi = maxEvidence * evidence/ (evidence + maxEvidence*weight);
        
        // Take exponential to get the probability
        return Math.pow(2, -projEvi);
    }
    
    /** Sets the tiebreaking order to the given order.
     * @param order	The new tiebreaking order. The length of the array should
     * be the same as the number of categories.
     */
    public void set_tiebreaking_order(int[] order) {
        //   if(order.low() != tiebreakingOrder.low() ||
        //      order.high() != tiebreakingOrder.high())
        //      Error.fatalErr("CatDist::set_tiebreaking_order: the given array's bounds "
        //	 +"("+order.low()+" - "+order.high()+") are incorrect. "
        //	 +"Bounds should be ("+tiebreakingOrder.low()+" - "
        //	 +tiebreakingOrder.high()+")");
        
        tiebreakingOrder = order;
        check_tiebreaking_order(get_tiebreaking_order());
    }
    
    /** Checks if the current tiebreaking order is the same as the given
     * tiebreaking order. If it is not, and error message is displayed.
     * @param order	The order to be compared to.
     */
    public void check_tiebreaking_order(int[] order) {
        // sort the array
        int[] sortedOrder =(int[]) order.clone();
        Arrays.sort(sortedOrder);
        
        boolean bad = false;
        for(int i = 0 ; i < order.length && !bad ; i++)
            if (i != sortedOrder[i])
                bad = true;
        if (bad)
            Error.fatalErr("CatDist::check_tiebreaking_order: Tiebreaking order "
            +order+ " has bad form");
    }
    
    /** Returns the tiebreaking order.
     * @return The tiebreaking order.
     */
    public int[] get_tiebreaking_order() {
        return tiebreakingOrder;
    }
    
    /** Testing code for the CatDist class.
     * @param args Command line arguments.
     */    
    public static void main(String[] args) {
        InstanceList IL = new InstanceList(args[0]);
        Schema SC = IL.get_schema();
        CatDist CD = new CatDist(SC,1);
        CD.set_scores(new double[SC.num_attr()],CatDist.none,0);
        System.out.println("Done.");
    }
    
    
}
