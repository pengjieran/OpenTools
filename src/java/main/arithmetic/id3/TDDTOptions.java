package arithmetic.id3;
/** This class stores information about option settings for Top-Down Decision
 * Tree inducers.
 * @author James Louis   Ported to Java
 */
public class TDDTOptions {
    
    /** The maximum level of growth.
     */
    public int maxLevel;
    
    /** The lower bound for the minimum weight of instances in a node.
     *
     */
    public double lowerBoundMinSplitWeight;
     /** The upper bound for the minimum weight of instances in a node.
      */
    public double upperBoundMinSplitWeight;
    
    /** The percent (p) used to calculate the min weight of instances in a node (m). <BR>
     * m = p * num instances / num categories
     *
     */
    public double minSplitWeightPercent;
    
    /** TRUE indicates lowerBoundMinSplitWeight, upperBoundMinSplitWeight, and
     * minSplitWeightPercent are not used for setting minimum instances in a node for
     * nominal attributes, FALSE indicates they will be used.
     */
    public boolean nominalLBoundOnly;
    
    /** TRUE if debugging options are used.
     */
    public boolean debug;
    
    /** TRUE indicates there will be an edge with "unknown" from every node.
     *
     */
    public boolean unknownEdges;
    
    /** The criterion used for scoring.
     */    
    public byte splitScoreCriterion;
    
    /** TRUE indicates an empty node should have the parent's distribution, FALSE
     * otherwise.
     *
     */    
    public boolean emptyNodeParentDist;
    
    /** TRUE indicates a node should inherit the parent's tie-breaking class, FALSE
     * otherwise.
     *
     */    
    public boolean parentTieBreaking;
    
    /** Pruning method to be used. If the value is not NONE and pruning_factor is 0,
     * then a node will be made a leaf when its (potential) children do not improve
     * the error count.
     *
     */
    public byte pruningMethod;
    
    /** TRUE indicates pruning should allow replacing a node with its largest subtree,
     * FALSE otherwise.
     *
     */    
    public boolean pruningBranchReplacement;
    
    /** TRUE indicates threshold should be adjusted to equal instance values, FALSE
     * otherwise.
     *
     */    
    public boolean adjustThresholds;
    
    /** Factor of how much pruning should be done. High values indicate more pruning.
     */
    public double pruningFactor;
    
    /** TRUE if the Minimum Description Length Adjustment for continuous attributes should
     * be applied to mutual info, FALSE otherwise.
     *
     */    
    public boolean contMDLAdjust;
    
    /** Number of thresholds on either side to use for smoothing; 0 for no smoothing.
     *
     */    
    public int smoothInst;
    
    /** Exponential factor for smoothing.
     */    
    public double smoothFactor;
    
    /** Type of distribution to build at leaves.
     *
     */
    public byte leafDistType;
    
    /** M-estimate factor for laplace.
     */
    public double MEstimateFactor;
    
    /** Evidence correction factor.
     */
    public double evidenceFactor;
    
    /** The metric used to evaluate this decision tree.
     *
     */
    public byte evaluationMetric;
    
    /** Constructor.
     */
    public TDDTOptions(){}
    
}
