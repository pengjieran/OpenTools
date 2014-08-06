package arithmetic.id3;
import java.util.LinkedList;
import arithmetic.shared.Error;
import java.util.ListIterator;

import arithmetic.shared.AugCategory;
import arithmetic.shared.CatDist;
import arithmetic.shared.CatTestResult;
import arithmetic.shared.Categorizer;
import arithmetic.shared.ConstCategorizer;
import arithmetic.shared.DoubleRef;
import arithmetic.shared.Edge;
import arithmetic.shared.Entropy;
import arithmetic.shared.GlobalOptions;
import arithmetic.shared.Globals;
import arithmetic.shared.Inducer;
import arithmetic.shared.Instance;
import arithmetic.shared.InstanceList;
import arithmetic.shared.IntRef;
import arithmetic.shared.LogOptions;
import arithmetic.shared.MLJ;
import arithmetic.shared.Node;
import arithmetic.shared.NominalAttrInfo;
import arithmetic.shared.Schema;
import arithmetic.shared.SplitScore;

/** Top-down decision-tree (TDDT) inducer induces decision trees
 * top-down by building smaller training sets and inducing trees
 * for them recursively. The decision tree built has categorizers
 * at each node, and these determine how to branch, i.e., to
 * which child to branch, or whether to classify.  The common
 * cases are: AttrCategorizers, which simply return a given value
 * for an attribute in the node, and ThresholdCategorizers, which
 * return 0 or one based on whether an attribute is less than or
 * greater than a given threshold (valid only for real attributes).
 * The leaves are usually constant categorizers, i.e., they just
 * return a constant value independent of the instance.			<P>
 * The induction algorithm calls best_split, a pure virtual
 * function, to determine the best root split.  Once the split has
 * been chosen, the data in the node is split according to the
 * categorizer best_split returns.  A node is formed, and the
 * algorithm is called recursively with each of the children.
 * Once each child returns with a subtree, we connect them to the
 * root we split. ID3Inducer, for example, implements the
 * best_split using information gain, but other methods are
 * possible. best_split() can return any categorizer, thus opening
 * the possibility for oblique trees with perceptrons at nodes,
 * recursive trees, etc.  The leaves can also be of any
 * classifier, thus perceptron-trees (Utgoff) can be created,
 * or a nearest-neighbor within a leaf, etc.					<P>
 * Complexity   :									<P>
 * The complexity of train() is proportional to the number of
 * nodes in the resulting tree times the time for deciding on
 * the split() categorizer (done by the derived classes).
 * predict() takes time proportional to the sum of the
 * categorizers time over the path from the root to a leaf node.	<P>
 * Enhancements :									<P>
 * We may speed things up by having an option to test only
 * splits where the class label changes.  For some measures
 * (e.g., entropy), it can be shown that a split will never be
 * made between two instances with the same class label
 * (Fayyad IJCAI 93 page 1022, Machine Learning journal Vol 8,
 * no 1, page 87, 1992). We may wish to discretize the real values
 * first. By making them linear discrete, we can use the regular
 * counters and things will be faster (note however that the split
 * will usually remain special since it's a binary threshold split,
 * not a multi-way split).								<P>
 * Another problem is with attributes that have many values, for
 * example social-security-number.  Computing all cut points can
 * be very expensive.  We may want to skip such attributes by
 * claiming that each value must have at least some number of
 * instances.  Utgoff in ML94 (page 322) mentions that ID slows
 * his system down considerably.  The problem of course is that if
 * you threshold, it sometimes make sense to split on such
 * attributes.  Taken to an extreme, if we had a real "real-value,"
 * all values would be different with probability 1, and hence we
 * would skip such an attribute.							<P>
 * To speed things up, we may want to have an Inducer that
 * accepts a decision tree and builds stuff in it (vs. getting
 * a graph). Other options allow for doing the recursion by
 * calling a function instead of creating the actual class.
 * The advantage of the current method is that it allows a
 * subclass to keep track of the number of levels (useful for
 * lookahead or something). Yet another option is to "recycle"
 * inducers by using our "this" and just changing the training set.	<P>
 * We currently split instances but keep the original structure,
 * that is, we don't actually delete the attribute tested on. It
 * may be faster in some cases to actually create a new List
 * without the attribute.  The disadvantage is that for multi-valued
 * attributes we may wish to branch again, so we can't always delete.
 * The same goes for tests which are not attributes (e.g.,
 * conjunctions).
 * @author James Louis 12/07/2000 Ported to Java.
 * @author Steve Gustafson 12/07/2000 Ported to Java.
 * @author Chia-Hsin Li 1/03/95 Added Options.
 * @author Ronny Kohavi 9/06/93 Initial revision (.h,.c)
 */
abstract public class TDDTInducer extends Inducer
{
   //ENUMS
    /** Pruning method value.
     */    
      public static final byte none = 0;           /*  PruningMethod enum */
      /** Pruning method value.
       */      
      public static final byte confidence = 1;     /*                     */ 
      /** Pruning method value.
       */      
      public static final byte penalty = 2;        /*                     */ 
      /** Pruning method value.
       */      
      public static final byte linear  = 3;        /*                     */ 
      /** Pruning method value.
       */      
      public static final byte KLdistance = 4;     /*                     */ 
      /** Pruning method value.
       */      
      public static final byte lossConfidence = 5; /*                     */ 
      /** Pruning method value.
       */      
      public static final byte lossLaplace = 6;    /*                     */ 
      
      /** LeafDistType value.
       */      
      public static final byte allOrNothing = 0;      /* LeafDistType enum */ 
      /** LeafDistType value.
       */      
      public static final byte frequencyCounts = 1;   /*                   */ 
      /** LeafDistType value.
       */      
      public static final byte laplaceCorrection = 2; /*                   */ 
      /** LeafDistType value.
       */      
      public static final byte evidenceProjection = 3;/*                   */ 

      /** Evaluation metric value.
       */      
      public static final byte error = 0;    /* EvalMetric enum */ 
      /** Evaluation metric value.
       */      
      public static final byte MSE = 1;      /*                 */ 
      /** Evaluation metric value.
       */      
      public static final byte logLoss = 2;  /*                 */ 
   //END ENUMS


   private static int MIN_INSTLIST_DRIBBLE = 5000;
   private static String MAX_LEVEL_HELP = "The maximum number of levels to grow.  0 "
   +"implies no limit.";
   private static int DEFAULT_MAX_LEVEL = 0;

   private static String LB_MSW_HELP = "This option specifies the value of lower bound "
  +"of the weight while calculating the minimum split "
  +"(overrides weight option).  Set to 0 to have the value determined "
  +"automatically depending on the total weight of the training set.";
   private static double DEFAULT_LB_MSW = 1;

   private static String UB_MSW_HELP = "This option specifies the value of upper bound "
  +"of the weight while calculating the minimum split (overrides lower bound).";
    private static double DEFAULT_UB_MSW = 25;

   private static String MS_WP_HELP = "This option chooses the value of "
  +"the weight percent while calculating the minimum split.";
    private static double DEFAULT_MS_WP = 0;

   private static String NOM_LBO_HELP = "This option specifies if only the lower bound "
  +"will be used for calculating the minimum split for nominal-valued "
  +"attributes.";
    private static boolean DEFAULT_NOM_LBO = false;

   private static String DEBUG_HELP = "This option specifies whether to display the "
  +"debug information while displaying the graph.";
private static boolean DEFAULT_DEBUG = false;

/** Indicates edges representing unknown values should be processed. TRUE 
      indicates unknown edges should be, FALSE otherwise. **/
   private static String UNKNOWN_EDGES_HELP = "This option specifies whether or not to "
  +"allow outgoing UNKNOWN edges from each node. ";
    private static boolean DEFAULT_UNKNOWN_EDGES = true;

   // This option is currently not enabled.
//private static String EMPTY_NODE_PARENT_DIST_HELP = "Should empty nodes get the "
//+"distribution of the parent or zeros";
private static boolean DEFAULT_EMPTY_NODE_PARENT_DIST = false;

   private static String PARENT_TIE_BREAKING_HELP = "Should ties be broken in favor of "
+"the majority category of the parent";
private static boolean DEFAULT_PARENT_TIE_BREAKING = true;

// The following option is currently not enabled.
//const MString PRUNING_BRANCH_REPLACEMENT_HELP = "Should replacing a node "
//"with its largest subtree be allowed during pruning";
   private static boolean DEFAULT_PRUNING_BRANCH_REPLACEMENT = false;

private static String ADJUST_THRESHOLDS_HELP = "Should theshold values be adjusted "
+"to the values of instances";
   private static boolean DEFAULT_ADJUST_THRESHOLDS = false;

private static String PRUNING_METHOD_HELP = "Which algorithm should be used for "
+"pruning.  (If not NONE and PRUNING_FACTOR is 0, a node will be made a leaf "
+"if its potential children would not improve the error count)";
   private static byte DEFAULT_PRUNING_METHOD = confidence;

   private String PRUNING_FACTOR_HELP = "Pruning factor in standard deviations. "
   +"(high value -> more pruning), zero is no pruning, 2.5 is heavy pruning";
   private static double DEFAULT_PRUNING_FACTOR = 0.0; //change this to .6925

   
   
private static String CONT_MDL_ADJUST_HELP = "When TRUE, mutual information for "
+"real attributes is lowered based on MDL";
  private static boolean DEFAULT_CONT_MDL_ADJUST = false;

private static String SMOOTH_INST_HELP = "Set the number of values on each side "
+"of a given entropy value to use for smoothing (0 turns off smoothing).";
   private static int DEFAULT_SMOOTH_INST = 0;

private static String SMOOTH_FACTOR_HELP = "Set the constant for the exponential "
+"distribution used to smooth.";
   private static double DEFAULT_SMOOTH_FACTOR = 0.75;

private static String LEAF_DIST_TYPE_HELP = "This option selects the type of "
+"distribution to create at leaf nodes.  All-or-nothing picks the majority "
+"category and places all weight there.  This is the default.  "
+"Frequency-counts compute the distribution as normalized counts of the "
+"occurance of each class at this leaf.  Laplace-correction uses the "
+"frequency counts but applies a laplace correction of M_ESTIMATE_FACTOR.  "
+"Evidence-projection uses the evidence projection algorithm to correct "
+"the frequency counts.  EVIDENCE_FACTOR is the evidence scaling factor "
+"for the correction.";
   private static byte defaultLeafDistType = allOrNothing;

private static String M_ESTIMATE_FACTOR_HELP = "This option determines the factor "
+"by which to scale the log number of instances when computing an "
+"evidence projection";
  private static double DEFAULT_LEAF_M_ESTIMATE_FACTOR = 0.0;

private static String EVIDENCE_FACTOR_HELP = "This option determines the factor "
+"by which to scale the log number of instances when computing an "
+"evidence projection";
   private static double DEFAULT_LEAF_EVIDENCE_FACTOR = 1.0;

private static String EVAL_METRIC_HELP = "The measure by which the induced tree will "
+"be evaluated. Changing this may affect induction and pruning.";
   private static byte DEFAULT_EVALUATION_METRIC = error;

   private static int totalNodesNum = 0;

   private static int callCount = 0;

   private static int totalAttr = 0;


   private int level; 
   private CGraph cgraph; 
   private DTCategorizer decisionTreeCat;
   private double totalInstWeight;
   private boolean haveContinuousAttributes, errorprune = false;
   private TDDTOptions tddtOptions;

   /** Constructor.
    * @param dscr	The description of the inducer.
    */
   public TDDTInducer(String dscr)
   { 
      super(dscr); 

      tddtOptions = new TDDTOptions();

      CGraph aCgraph = null;
      level = 0;
      cgraph = aCgraph;
      decisionTreeCat = null;
      totalInstWeight = -1; //illegal value;
      
      //this is arbirary = no schema yet
      haveContinuousAttributes = false;

      tddtOptions.maxLevel = DEFAULT_MAX_LEVEL;
      tddtOptions.lowerBoundMinSplitWeight = DEFAULT_LB_MSW;
      tddtOptions.upperBoundMinSplitWeight = DEFAULT_UB_MSW;
      tddtOptions.minSplitWeightPercent = DEFAULT_MS_WP;
      tddtOptions.nominalLBoundOnly = DEFAULT_NOM_LBO;
      tddtOptions.debug = DEFAULT_DEBUG;
      tddtOptions.unknownEdges = DEFAULT_UNKNOWN_EDGES;
      tddtOptions.splitScoreCriterion = SplitScore.defaultSplitScoreCriterion;
      tddtOptions.emptyNodeParentDist = DEFAULT_EMPTY_NODE_PARENT_DIST;
      tddtOptions.parentTieBreaking = DEFAULT_PARENT_TIE_BREAKING;
      tddtOptions.pruningMethod = DEFAULT_PRUNING_METHOD;
      tddtOptions.pruningBranchReplacement = DEFAULT_PRUNING_BRANCH_REPLACEMENT;
      tddtOptions.adjustThresholds = DEFAULT_ADJUST_THRESHOLDS;
      tddtOptions.pruningFactor = DEFAULT_PRUNING_FACTOR;
      tddtOptions.contMDLAdjust = DEFAULT_CONT_MDL_ADJUST;
      tddtOptions.smoothInst = DEFAULT_SMOOTH_INST;
      tddtOptions.smoothFactor = DEFAULT_SMOOTH_FACTOR;
      tddtOptions.leafDistType = defaultLeafDistType;
      tddtOptions.MEstimateFactor = DEFAULT_LEAF_M_ESTIMATE_FACTOR;
      tddtOptions.evidenceFactor = DEFAULT_LEAF_EVIDENCE_FACTOR;
      tddtOptions.evaluationMetric = DEFAULT_EVALUATION_METRIC;

   }

   /** Constructor.
    * @param descr The description of this inducer.
    * @param aCgraph The CGraph that will hold the decision tree.
    */
   public TDDTInducer(String descr, CGraph aCgraph)
{
   super(descr);
   level = 0;

   tddtOptions = new TDDTOptions();

   cgraph = aCgraph; // save this until we actually construct the tree.
   decisionTreeCat = null;
   totalInstWeight = -1; // illegal value.

   // this is arbitrary - no schema yet
   haveContinuousAttributes = false;

   tddtOptions.maxLevel = DEFAULT_MAX_LEVEL;
   tddtOptions.lowerBoundMinSplitWeight = DEFAULT_LB_MSW;
   tddtOptions.upperBoundMinSplitWeight = DEFAULT_UB_MSW;
   tddtOptions.minSplitWeightPercent = DEFAULT_MS_WP;
   tddtOptions.nominalLBoundOnly = DEFAULT_NOM_LBO;
   tddtOptions.debug = DEFAULT_DEBUG;
   tddtOptions.unknownEdges = DEFAULT_UNKNOWN_EDGES;
   tddtOptions.splitScoreCriterion =  SplitScore.defaultSplitScoreCriterion;
   tddtOptions.emptyNodeParentDist = DEFAULT_EMPTY_NODE_PARENT_DIST;
   tddtOptions.parentTieBreaking = DEFAULT_PARENT_TIE_BREAKING;
   tddtOptions.pruningMethod = DEFAULT_PRUNING_METHOD;
   tddtOptions.pruningBranchReplacement = DEFAULT_PRUNING_BRANCH_REPLACEMENT;
   tddtOptions.adjustThresholds = DEFAULT_ADJUST_THRESHOLDS;
   tddtOptions.pruningFactor = DEFAULT_PRUNING_FACTOR;
   tddtOptions.contMDLAdjust = DEFAULT_CONT_MDL_ADJUST;
   tddtOptions.smoothInst = DEFAULT_SMOOTH_INST;
   tddtOptions.smoothFactor = DEFAULT_SMOOTH_FACTOR;
   tddtOptions.leafDistType = defaultLeafDistType;
   tddtOptions.MEstimateFactor = DEFAULT_LEAF_M_ESTIMATE_FACTOR;
   tddtOptions.evidenceFactor = DEFAULT_LEAF_EVIDENCE_FACTOR;
   tddtOptions.evaluationMetric = DEFAULT_EVALUATION_METRIC;
}

   /** Copy constructor.
    * @param source	The TDDTInducer that is being copied.
    */
   public TDDTInducer(TDDTInducer source)
   {
      super(source);
      cgraph = null;
      decisionTreeCat = null;
      set_level(source.get_level());
      copy_options(source);
      set_total_inst_weight(source.get_total_inst_weight());

      haveContinuousAttributes = source.haveContinuousAttributes;
   }

   /** Sets the level of this TDDTInducer.
    * @param lvl The level to be set.
    */
   public void set_level(int lvl) { level = lvl;}

   /** Returns the level set for this TDDTInducer.
    * @return This TDDTInducer's level setting.
    */
   public int get_level() {return level;}

   /** Sets the total weight of instances in the data set this inducer is currently
    * using.
    * @param wt	The weight that should be set.
    */
   protected void set_total_inst_weight(double wt){ totalInstWeight = wt;}

   /** Returns the total weight of instances in the data set this inducer is using.
    * @return The weight of the instances.
    */
   protected double get_total_inst_weight(){ return totalInstWeight;}

   /** Returns the maximum level which may be set for a TDDTInducer.
    * @return The maximum weight of instances.
    */
   public int get_max_level(){return tddtOptions.maxLevel;}

   /** Sets the maximum level for a TDDTInducer.
    * @param level The new maximum level.
    */
   public void set_max_level(int level){tddtOptions.maxLevel = level;}

   /** Sets the lower bound for minimum split weight.
    * @param val The new lower bound.
    */
   public void set_lower_bound_min_split_weight(double val)
      {  tddtOptions.lowerBoundMinSplitWeight = val; }

   /** Returns the lower bound for minimum split weight.
    * @return The lower bound for minimum split weight.
    */
   public double get_lower_bound_min_split_weight()
      {  return tddtOptions.lowerBoundMinSplitWeight; }

   /** Sets the upper bound for minimum split weight.
    * @param val The new upper bound.
    */
   public void set_upper_bound_min_split_weight(double val)
      {   tddtOptions.upperBoundMinSplitWeight = val; }

   /** Returns the upper bound for minimum split weight.
    * @return The upper bound for minimum split weight.
    */
   public double get_upper_bound_min_split_weight()
      {   return tddtOptions.upperBoundMinSplitWeight; }

   /** Sets a new percentage value for minimum split weight.
    * @param val The new percentage.
    */
   public void set_min_split_weight_percent(double val)
      {   tddtOptions.minSplitWeightPercent = val; }

   /** Returns the percentage value for minimum split weight.
    * @return The percentage value for minimum split weight.
    */
   public double get_min_split_weight_percent()
      {   return tddtOptions.minSplitWeightPercent; }

   /** Sets which lower bounds are used for nominal attributes. TRUE indicates
    * lowerBoundMinSplitWeight, upperBoundMinSplitWeight, and minSplitWeightPercent
    * are not used for setting minimum instances in a node for nominal attributes,
    * FALSE indicates they will be used.
    * @param val The value for the boolean option.
    */
   public void set_nominal_lbound_only(boolean val)
      { tddtOptions.nominalLBoundOnly = val; }

   /** Returns TRUE if lower bounds are to be used for nominal values, FALSE otherwise.
    * @return TRUE indicates lowerBoundMinSplitWeight, upperBoundMinSplitWeight, and
    * minSplitWeightPercent are not used for setting minimum instances in a node for
    * nominal attributes, FALSE indicates they will be used.
    */
   public boolean get_nominal_lbound_only() 
      { return tddtOptions.nominalLBoundOnly; }

   /** Sets whether unknown categories are allowable for edges if the decision tree.
    * @param val TRUE if unknown edges are allowable, FALSE otherwise.
    */
   public void set_unknown_edges(boolean val) {tddtOptions.unknownEdges = val;}

   /** Returns whether unknown edges are allowed.
    * @return TRUE if unknown edges are allowable, FALSE otherwise.
    */
   public boolean get_unknown_edges() { return tddtOptions.unknownEdges; }

   /** Return the criterion used for scoring.
    * @return The split score criterion.
    */
   public byte get_split_score_criterion() 
      {return tddtOptions.splitScoreCriterion; }

   /** Sets the criterion used for split scoring.
    * @param val The new split score criterion.
    */
   public void set_split_score_criterion(byte val)
      {tddtOptions.splitScoreCriterion = val; }

   /** Sets whether an empty node should have the parent's distribution.
    * @param b TRUE indicates an empty node should have the parent's distribution,
    * FALSE otherwise.
    */
   public void set_empty_node_parent_dist(boolean b)
      {tddtOptions.emptyNodeParentDist = b; }

   /** Returns whether an empty node should have the parent's distribution.
    * @return TRUE indicates an empty node should have the parent's distribution,
    * FALSE otherwise.
    */
   public boolean get_empty_node_parent_dist()
      {return tddtOptions.emptyNodeParentDist; }

   /** Set the tie breaking order for distribution ties.
    * @param b the new order for breaking distribution ties.
    */
   public void set_parent_tie_breaking(boolean b)
      {tddtOptions.parentTieBreaking = b; }

   /** Get the order for breaking distribution ties.
    * @return Order for breaking distribution ties.
    */
   public boolean get_parent_tie_breaking()
      {return tddtOptions.parentTieBreaking; }

    /** Sets the Pruning method to be used.
     * @param pM The Pruning method to be used. If the value is not NONE and pruning_factor is 0,
     * then a node will be made a leaf when its (potential) children do not improve
     * the error count.
     */
   public void set_pruning_method(byte pM)
      {tddtOptions.pruningMethod = pM; }

   /** Returns the Pruning method to be used.
    * @return The Pruning method used.
    */
   public byte get_pruning_method()
      {return tddtOptions.pruningMethod; }

   /** Sets whether pruning should allow replacing a node with its largest subtree.
    * @param b TRUE indicates pruning should allow replacing a node with its largest subtree,
    * FALSE otherwise.
    */
   public void set_pruning_branch_replacement(boolean b)
      {tddtOptions.pruningBranchReplacement = b; }

   /** Returns whether pruning should allow replacing a node with its largest subtree.
    * @return TRUE indicates pruning should allow replacing a node with its largest subtree,
    * FALSE otherwise.
    */
   public boolean get_pruning_branch_replacement() 
      {return tddtOptions.pruningBranchReplacement; }

   /** Sets whether threshold should be adjusted to equal instance values.
    * @param b TRUE indicates threshold should be adjusted to equal instance values, FALSE otherwise.
    */
   public void set_adjust_thresholds(boolean b)
      {tddtOptions.adjustThresholds = b; }

   /** Returns whether threshold should be adjusted to equal instance values.
    * @return TRUE indicates threshold should be adjusted to equal instance values, FALSE otherwise.
    */
   public boolean get_adjust_thresholds() 
      {return tddtOptions.adjustThresholds; }

   /** Sets the factor of how much pruning should be done.
    * @param val Factor of how much pruning should be done. High values indicate more pruning.
    */
   public void set_pruning_factor(double val)
      { tddtOptions.pruningFactor = val; }

   /** Returns the factor of how much pruning should be done.
    * @return Factor of how much pruning should be done. High values indicate more pruning.
    */
   public double get_pruning_factor() 
      { return tddtOptions.pruningFactor; }

   /** Returns the number of thresholds on either side to use for smoothing.
    * @return Number of thresholds on either side to use for smoothing; 0 for no smoothing.
    */
   public int get_smooth_inst() { return tddtOptions.smoothInst; }

   /** Sets the number of thresholds on either side to use for smoothing.
    * @param inst Number of thresholds on either side to use for smoothing; 0 for no smoothing.
    */
   public void set_smooth_inst(int inst) { tddtOptions.smoothInst = inst; }

   /** Returns the exponential factor for smoothing.
    * @return The exponential factor for smoothing.
    */
   public double get_smooth_factor() { return tddtOptions.smoothFactor; }

    /** Sets the exponential factor for smoothing.
     * @param factor The new exponential factor for smoothing.
     */
   public void set_smooth_factor(double factor) { tddtOptions.smoothFactor = factor; }
			   
   /** Sets whether the Minimum Description Length Adjustment for continuous attributes
    * should be applied to mutual info.
    * @param val TRUE if the Minimum Description Length Adjustment for continuous attributes should be applied to mutual info, FALSE otherwise.
    */
   public void set_cont_mdl_adjust(boolean val)
      { tddtOptions.contMDLAdjust = val; }

   /** Returns whether Minimum Description Length Adjustment for continuous attributes should be applied to mutual info.
    * @return TRUE if the Minimum Description Length Adjustment for continuous attributes should
    *     * be applied to mutual info, FALSE otherwise.
    */
   public boolean get_cont_mdl_adjust() 
      { return tddtOptions.contMDLAdjust; }

   /** Sets the type of distribution to build at leaves.
    * @param type The type of distribution to build at leaves.
    */
   public void set_leaf_dist_type(byte type)
      { tddtOptions.leafDistType = type; }

   /** Returns the type of distribution to build at leaves.
    * @return The type of distribution to build at leaves.
    */
   public byte get_leaf_dist_type() 
      { return tddtOptions.leafDistType; }

   /** Sets the m-estimate factor for laplace.
    * @param factor The new m-estimate factor for laplace.
    */
   public void set_m_estimate_factor(double factor)
      { tddtOptions.MEstimateFactor = factor; }

   /** Returns the m-estimate factor for laplace.
    * @return The m-estimate factor for laplace.
    */
   public double get_m_estimate_factor() { return tddtOptions.MEstimateFactor; }

   /** Sets the evidence correction factor.
    * @param factor The new evidence correction factor.
    */
   public void set_evidence_factor(double factor)
      { tddtOptions.evidenceFactor = factor; }

   /** Returns the evidence correction factor.
    * @return The evidence correction factor.
    */
   public double get_evidence_factor() { return tddtOptions.evidenceFactor; }
 
   /** Returns whether there are continuous attributes present in the data.
    * @return TRUE indicates there are continuous attributes in the data, FALSE otherwise.
    */
   public boolean get_have_continuous_attributes()
      { return haveContinuousAttributes; }

   /** Sets whether there are continuous attributes present in the data.
    * @param val TRUE indicates there are continuous attributes in the data, FALSE otherwise.
    */
   public void set_have_continuous_attributes(boolean val)
      { haveContinuousAttributes = val; }

   /** Accesses the debug variable.
    * @return TRUE if debugging statements are active, FALSE otherwise.
    */
   public boolean get_debug() { return tddtOptions.debug; }

   /** Sets the debugging option.
    * @param val TRUE if debugging statements are active, FALSE otherwise.
    */
   public void set_debug(boolean val) { tddtOptions.debug = val; }

   /** Sets the evaluation metric used.
    * @param metric The evaluation metric to be used.
    */
   public void set_evaluation_metric(byte metric)
      { tddtOptions.evaluationMetric = metric; }

   /** Accesses  the evaluation metric used.
    * @return The evaluation metric to be used.
    */
   public byte get_evaluation_metric() 
      { return tddtOptions.evaluationMetric; }

   /** Sets statistical information about the tree. This information consists of the total
    * number of nontrivial nodes and the total number of attributes.
    */
   protected void accumulate_tree_stats()
   {
      //ASSERT(decisionTreeCat);
      totalNodesNum += num_nontrivial_nodes();
      if(class_id() == ID3_INDUCER || class_id() == SGI_DT_INDUCER)
         totalAttr += 
	    decisionTreeCat.rooted_cat_graph().num_attr(TS.num_attr());
      callCount++;
   }

   /** Copies the option settings from the given TDDTInducer.
    * @param inducer The TDDTInducer with the options to be copied.
    */
   public void copy_options(TDDTInducer inducer)
   {
      // Copy the continuous attributes flag
      set_have_continuous_attributes(inducer.get_have_continuous_attributes());

      logOptions.set_log_options(inducer.logOptions.get_log_options());
      set_max_level(inducer.get_max_level());
      set_lower_bound_min_split_weight(
         inducer.get_lower_bound_min_split_weight());
      set_upper_bound_min_split_weight(
         inducer.get_upper_bound_min_split_weight());
      set_min_split_weight_percent(inducer.get_min_split_weight_percent());
      set_nominal_lbound_only(inducer.get_nominal_lbound_only());
      set_debug(inducer.get_debug());
      set_unknown_edges(inducer.get_unknown_edges());
      set_split_score_criterion(inducer.get_split_score_criterion());
      set_empty_node_parent_dist(inducer.get_empty_node_parent_dist());
      set_parent_tie_breaking(inducer.get_parent_tie_breaking());
      set_pruning_method(inducer.get_pruning_method());
      set_pruning_branch_replacement(inducer.get_pruning_branch_replacement());
      set_adjust_thresholds(inducer.get_adjust_thresholds());
      set_pruning_factor(inducer.get_pruning_factor());
      set_cont_mdl_adjust(inducer.get_cont_mdl_adjust());
      set_smooth_inst(inducer.get_smooth_inst());
      set_smooth_factor(inducer.get_smooth_factor());
      set_leaf_dist_type(inducer.get_leaf_dist_type());
      set_m_estimate_factor(inducer.get_m_estimate_factor());
      set_evidence_factor(inducer.get_evidence_factor());
      set_evaluation_metric(inducer.get_evaluation_metric());

   }

   /** Sets the user options according to the option file.
    * @param prefix The prefix for the option names.
    */   
   public void set_user_options(String prefix){
       tddtOptions.maxLevel = getEnv.get_option_int(prefix + "MAX_LEVEL",
       tddtOptions.maxLevel, MAX_LEVEL_HELP, true, false);
/*       tddtOptions.pruningMethod = get_option_enum(prefix + "PRUNING_METHOD",
           pruningMethodEnum,tddtOptions.pruningMethod, PRUNING_METHOD_HELP, false);
*/       if (tddtOptions.pruningMethod != TDDTInducer.none) {
           tddtOptions.pruningFactor = getEnv.get_option_real(prefix + "PRUNING_FACTOR",
               tddtOptions.pruningFactor, PRUNING_FACTOR_HELP, true, false);
       }
       tddtOptions.lowerBoundMinSplitWeight = getEnv.get_option_real(
           prefix + "LBOUND_MIN_SPLIT",tddtOptions.lowerBoundMinSplitWeight,
           LB_MSW_HELP, true);
       tddtOptions.upperBoundMinSplitWeight = getEnv.get_option_real(
           prefix + "UBOUND_MIN_SPLIT",Math.max(tddtOptions.upperBoundMinSplitWeight,
           tddtOptions.lowerBoundMinSplitWeight),UB_MSW_HELP, true);
       if (tddtOptions.upperBoundMinSplitWeight <
       tddtOptions.lowerBoundMinSplitWeight)
           Error.fatalErr("TDDTInducer.set_user_options: upper bound must be >= "
            +"lower bound");
       tddtOptions.minSplitWeightPercent =
       getEnv.get_option_real(prefix + "MIN_SPLIT_WEIGHT",
       tddtOptions.minSplitWeightPercent, MS_WP_HELP, true);
       tddtOptions.nominalLBoundOnly =
       getEnv.get_option_bool(prefix + "NOMINAL_LBOUND_ONLY",
       tddtOptions.nominalLBoundOnly, NOM_LBO_HELP);
       tddtOptions.debug =
       getEnv.get_option_bool(prefix + "DEBUG",
       tddtOptions.debug, DEBUG_HELP, true);
       tddtOptions.unknownEdges =
       getEnv.get_option_bool(prefix + "UNKNOWN_EDGES",
       tddtOptions.unknownEdges, UNKNOWN_EDGES_HELP, true);
/*       tddtOptions.splitScoreCriterion =
       getEnv.get_option_enum(prefix + "SPLIT_BY", SplitScore.splitScoreCriterionEnum,
       tddtOptions.splitScoreCriterion,
       SplitScore.splitScoreCriterionHelp, true);
       // The following is a rare option.  It may be supported by uncommenting
       // the following.  It allows an empty node to get the distribution
       // of the parent, which is what C4.5 does.  However, in C4.5,
       // you can determine that it's an empty node by looking at the
       // instance count, which is what we use when reading C4.5 trees.
       //tddtOptions.emptyNodeParentDist =
       //   get_option_bool(prefix + "EMPTY_NODE_PARENT_DIST",
       //	      tddtOptions.emptyNodeParentDist, EMPTY_NODE_PARENT_DIST_HELP,
       //	      false);
*/       tddtOptions.parentTieBreaking =
       getEnv.get_option_bool(prefix + "PARENT_TIE_BREAKING",
       tddtOptions.parentTieBreaking,
       PARENT_TIE_BREAKING_HELP, false);
       // @@   tddtOptions.pruningBranchReplacement =
       //       get_option_bool(prefix + "PRUNING_BRANCH_REPLACEMENT",
       // 		      tddtOptions.pruningBranchReplacement,
       // 		      PRUNING_BRANCH_REPLACEMENT_HELP,
       // 		      false);
       tddtOptions.pruningBranchReplacement = false;
       tddtOptions.adjustThresholds =
       getEnv.get_option_bool(prefix + "ADJUST_THRESHOLDS",
       tddtOptions.adjustThresholds, ADJUST_THRESHOLDS_HELP,
       false);
       tddtOptions.contMDLAdjust =
       getEnv.get_option_bool(prefix + "CONT_MDL_ADJUST",
       tddtOptions.contMDLAdjust, CONT_MDL_ADJUST_HELP);
       tddtOptions.smoothInst = getEnv.get_option_int(prefix + "SMOOTH_INST",
       tddtOptions.smoothInst,
       SMOOTH_INST_HELP);
       if (tddtOptions.smoothInst != 0)
/*           tddtOptions.smoothFactor = getEnv.get_option_real(prefix + "SMOOTH_FACTOR",
           tddtOptions.smoothFactor,
           SMOOTH_FACTOR_HELP);
       
       tddtOptions.leafDistType = getEnv.get_option_enum(prefix + "LEAF_DIST_TYPE",
       leafDistTypeMEnum,
       tddtOptions.leafDistType,
       LEAF_DIST_TYPE_HELP,
       false);
*/       if(tddtOptions.leafDistType == laplaceCorrection)
           tddtOptions.MEstimateFactor =
           getEnv.get_option_real(prefix + "M_ESTIMATE_FACTOR",
           tddtOptions.MEstimateFactor,
           M_ESTIMATE_FACTOR_HELP, true);
       else if(tddtOptions.leafDistType == evidenceProjection)
           tddtOptions.evidenceFactor =
           getEnv.get_option_real(prefix + "EVIDENCE_FACTOR",
           tddtOptions.evidenceFactor,
           EVIDENCE_FACTOR_HELP, true);
/*       tddtOptions.evaluationMetric =
       getEnv.get_option_enum(prefix + "EVAL_METRIC", evalMetricEnum,
       tddtOptions.evaluationMetric, EVAL_METRIC_HELP, false);
*/
   }

   
   /** Trains the inducer on a stored dataset and collects statistics.
    */
   public void train() 
   {
      train_no_stats();
      accumulate_tree_stats();
   }

   /** Trains the inducer on a stored dataset. No statistical data is
    * collected for the test of the categorizer.
    * @return The number of attributes.
    */
   public int train_no_stats()
   {
      //IFLOG(3, display_options(get_log_stream()));
      has_data();
      //DBG(OK());
      //ASSERT(get_level() == 0); //should never be modified for user created
                                  //inducers.
      decisionTreeCat = null;     //remove any existing tree categorizer.
                                  //OK must be ignored until done. otherwise
				  //we get a freed-memory read (???)
      
      boolean usedAutoLBoundMinSplit = false;

      if(tddtOptions.lowerBoundMinSplitWeight == 0.0) {
         usedAutoLBoundMinSplit = true;
	 tddtOptions.lowerBoundMinSplitWeight = 
	    Entropy.auto_lbound_min_split(TS.total_weight());
	 logOptions.LOG(2, "Auto-setting lbound minSplit to "
	         + tddtOptions.lowerBoundMinSplitWeight);
      }
 
      boolean foundReal = false;
      Schema schema = TS.get_schema();   //SchemaRC

      // Checking for real(continuous) attributes. -JL
      for(int i=0;i<schema.num_attr() && !foundReal; i++)
         foundReal = schema.attr_info(i).can_cast_to_real();
      set_have_continuous_attributes(foundReal);

      // The decision Tree either creates a new graph, or gets ours.
      DecisionTree dTree = null; 
      if(cgraph!=null)
         dTree = new DecisionTree(cgraph);
      else dTree = new DecisionTree();

      // Display the training InstanceList. -JL
      logOptions.LOG(4, "Training with instance list\n" + instance_list().out(false));
      set_total_inst_weight(TS.total_weight());

      boolean saveDribble = GlobalOptions.dribble;
      if(TS.num_instances() <= MIN_INSTLIST_DRIBBLE)
         GlobalOptions.dribble = false;
      else
         logOptions.DRIBBLE("There are over " + MIN_INSTLIST_DRIBBLE 
	                    +" instances in training set. Showing progress");
      
      DoubleRef numErrors =new DoubleRef(0);
      DoubleRef pessimisticErrors =new DoubleRef(0);
      IntRef numLeaves =new IntRef(0);

      int[] tieBreakingOrder = TS.get_distribution_order();

      // Induce_decision_tree returns the root of the created decision tree.
      // The root is set into this TDDTInducer.
      dTree.set_root(induce_decision_tree(dTree.get_graph(), tieBreakingOrder,
                                           numErrors, pessimisticErrors,
					   numLeaves,-1));
      tieBreakingOrder = null;
      logOptions.DRIBBLE("Decision tree classifier has been induced\n");
      GlobalOptions.dribble = saveDribble;

      if (usedAutoLBoundMinSplit)
         tddtOptions.lowerBoundMinSplitWeight = 0.0;

      int numAttr = -1;
      if (class_id() == ID3_INDUCER || class_id() == SGI_DT_INDUCER)
         numAttr = dTree.num_attr(TS.num_attr());

      // Creating a reusable categorizer from the induced tree. -JL
      build_categorizer(dTree);

      if (errorprune) {prune(dTree, dTree.get_root(), instance_list(), true);}
      
//      MLJ.ASSERT(dTree == null,"TDDTInducer.train_no_stats: dTree != null.");
      // Display some information about the induced tree. -JL
      if (numAttr != -1)
         logOptions.LOG(1, "Tree has " + num_nontrivial_nodes() + " nodes, "
             + num_nontrivial_leaves()
             + " leaves, and " + numAttr + " attributes."+'\n');

      RootedCatGraph rcg = decisionTreeCat.rooted_cat_graph();
      //IFLOG(1, show_hist(get_log_stream(), rcg));
      if (get_adjust_thresholds())
         decisionTreeCat.adjust_real_thresholds(instance_list());
	return numAttr;
   }

   /** Returns the number of Nodes that contain no Instances and have only uknown edges
    * leading to them.
    * @return The number of Nodes that contain no Instances and have only uknown edges
    * leading to them.
    */
   public int num_nontrivial_nodes()
   {
      was_trained(true);
      return decisionTreeCat.num_nontrivial_nodes();
   }
   
   /** Returns the number of leaves that contain no Instances and have only uknown edges
    * leading to them.
    * @return The number of leaves that contain no Instances and have only uknown edges
    * leading to them.
    */
   public int num_nontrivial_leaves()
   {
      was_trained(true);
      return decisionTreeCat.num_nontrivial_leaves();
   }

   /** Checks if this inducer has a valid decision tree.
    * @return True iff the class has a valid decisionTree categorizer.
    * @param fatalOnFalse TRUE if an error message should be displayed if the inducer is not trained,
    * FALSE otherwise.
    */
   public boolean was_trained(boolean fatalOnFalse)
   {
      if(fatalOnFalse && decisionTreeCat == null)
         Error.err("TDDTInducer.was_trained: No decision tree categorizer. "
	           + " Call train() to create categorizer -->fatal_error");
      return decisionTreeCat != null;
   }

   /** Induce a decision tree in the given graph.
    * @param aCgraph                  The graph that will contain the decision tree.
    * @param tieBreakingOrder The tie breaking order for breaking distribution ties.
    * @param numSubtreeErrors Number of errors to this point.
    * @param pessimisticSubtreeErrors Error estimate if this was a leaf node.
    * @param numLeaves                The number of leaves in the decision tree.
    * @param remainingSiblings Siblings that have not be induced yet.
    * @return The root node of the decision tree.
    */
   public Node induce_decision_tree(CGraph aCgraph, int[] tieBreakingOrder, DoubleRef numSubtreeErrors, DoubleRef pessimisticSubtreeErrors, IntRef numLeaves, int remainingSiblings)
   {
      if (TS.no_weight())
         Error.fatalErr("TDDTInducer.induce_decision_tree: list has zero weight");
   
//      DBG(pessimisticSubtreeErrors = -1);
      // Create a decision tree object to allow building nodes in the CGraph.
      DecisionTree decisionTree = new DecisionTree(aCgraph);

      // Display training InstanceList. -JL
      logOptions.LOG(4, "Training set ="+'\n'+TS.out(false)+'\n');

      LinkedList catNames = new LinkedList();
//      catNames[0] = null;
      NodeCategorizer[] rootCat = new NodeCategorizer[1];
 	rootCat[0] = best_split(catNames);
      if (rootCat[0] == null) {
         rootCat[0] = create_leaf_categorizer(TS.total_weight(),
					tieBreakingOrder, numSubtreeErrors,
					pessimisticSubtreeErrors);
         // We catch empty leaves in induce_tree_from_split. Hence, this can't be
         // a trivial leaf.
//         MLJ.ASSERT(MLJ.approx_greater(rootCat[0].total_weight(), 0),"TDDTInducer.induce_decision_tree: MLJ.approx_greater(rootCat[0].total_weight(), 0) == false");
         numLeaves.value = 1;
         decisionTree.set_root(decisionTree.create_node(rootCat, get_level()));
         MLJ.ASSERT(rootCat[0] == null,"TDDTInducer.induce_decision_tree: rootCat[0] != null"); // create_node gets ownership
//         IFDRIBBLE(dribble_level(level, "Leaf node", remainingSiblings));
      } else {
         NodeCategorizer splitCat = rootCat[0];
         decisionTree.set_root(decisionTree.create_node(rootCat, get_level()));
         MLJ.ASSERT(rootCat[0] == null,"TDDTInducer.induce_decision_tree: rootCat[0] != null"); // create_node gets ownership
         induce_tree_from_split(decisionTree, splitCat, catNames,
			     tieBreakingOrder, numSubtreeErrors,
			     pessimisticSubtreeErrors, numLeaves,
			     remainingSiblings);
      }

      catNames = null;
//      DBG(catNames = null);
      logOptions.LOG(6, "TDDT returning " + decisionTree.get_root() +'\n');

      MLJ.ASSERT(pessimisticSubtreeErrors.value >= 0,"TDDTInducer.induce_decision_tree: pessimisticSubtreeErrors.value < 0");
      return decisionTree.get_root();

//      System.out.println("Warning-->TDDTInducer.induce_decision_tree"
//         +" not implemented yet");
//      return null; 
   }

   /** Builds a decision tree categorizer for the given DecisionTree.
    * @param dTree The DecisionTree to use for creating the categorizer.
    */
   protected void build_categorizer(DecisionTree dTree)
   {
      decisionTreeCat = null;
      decisionTreeCat = new DTCategorizer(dTree, description(),
                                          TS.num_categories(),
                                          TS.get_schema());
      
      decisionTreeCat.set_leaf_dist_params(tddtOptions.leafDistType,
                                           tddtOptions.MEstimateFactor,
					   tddtOptions.evidenceFactor);
      //ASSERT(dtree==null);
   }

   /** Best_split finds the best split in the node and returns a categorizer
    * implementing it. It allocates and returns catNames containing the names of the
    * resulting categories.
    *
    * @param catNames The list of categories found in the Node.
    * @return The Categorizer using the list of categories.
    */
   abstract public NodeCategorizer best_split(LinkedList catNames);

   /** Computes the number of errors this node would make as a leaf. If
    * totalWeight is zero, the distribution is ignored, else totalWeight
    * must be the sum of the distribution counts.
    * @return The number of errors this node would make if it were a leaf
    * on the decision tree.
    * @param cat			The Categorizer for the node being checked.
    * @param predictClass	The category for which this node is being
    * checked.
    * @param totalWeight	The weight of all instances in a data set.
    */
   protected static double num_cat_errors(Categorizer cat, int predictClass, double totalWeight)
{
   double numErrors = 0;
   if (!cat.has_distr())
      Error.fatalErr("TDDTInducer.num_cat_errors: Categorizer has no distribution");
//   DBG(
//      const double[]& dist = cat.get_distr();
//      double sum = dist.sum();
//      // if (numInstances > 0) @@ will this fail?  If yes, comment why
//      MLJ.verify_approx_equal((StoredReal)sum, (StoredReal)totalWeight,
//                              "TDDTInducer.num_cat_errors: summation of "
//			      "distribution fails to equal number of "
//			      "instances", 100);
//      );  

   if (totalWeight > 0) { // we're not an empty leaf
      double numPredict = cat.get_distr()[predictClass - Globals.FIRST_NOMINAL_VAL];
      double nodeErrors = totalWeight - numPredict; // error count
//      ASSERT(nodeErrors >= 0);
      numErrors = nodeErrors; // increment parent's count of errors
   }
   return numErrors;
}

   /** Creates a leaf categorizer (has no children). We currently create
    * a ConstCategorizer with a description and the majority category.
    * Note that the augCategory will contain the correct string,
    * but the description will contain more information which may
    * help when displaying the graph. The augCategory string must
    * be the same for CatTestResult to work properly (it compares
    * the actual string for debugging purposes).
    * @return The LeafCategorizer created.
    * @param tieBreakingOrder Order for breaking distribution ties.
    * @param totalWeight The total weight of the training data set.
    * @param numErrors The number of errors this LeafCategorizer
    * will produce.
    * @param pessimisticErrors Error estimate if this was a leaf node.
    */
public LeafCategorizer create_leaf_categorizer(double totalWeight,
				     int[] tieBreakingOrder,
				     DoubleRef numErrors, DoubleRef pessimisticErrors)
{return create_leaf_categorizer(totalWeight,tieBreakingOrder,numErrors,pessimisticErrors,null);}

/** Creates a leaf categorizer (has no children). We currently create
 * a ConstCategorizer with a description and the majority category.
 * If the distrArray is given, we don't reference the training set,
 * except for its schema (used in pruning).<P>
 * Note that the augCategory will contain the correct string,
 * but the description will contain more information which may
 * help when displaying the graph. The augCategory string must
 * be the same for CatTestResult to work properly (it compares
 * the actual string for debugging purposes).
 * @return The LeafCategorizer created.
 * @param tieBreakingOrder Order for breaking distribution ties.
 * @param totalWeight The total weight of the training data set.
 * @param numErrors The number of errors this LeafCategorizer
 * will produce.
 * @param pessimisticErrors Error estimate if this was a leaf node.
 * @param distrArray Distribution of weight over labels.
 */
public LeafCategorizer create_leaf_categorizer(double totalWeight,
				     int[] tieBreakingOrder,
				     DoubleRef numErrors, DoubleRef pessimisticErrors,
				     double[] distrArray)
{
   // Find tiebreaking order.
   int[] myTiebreakingOrder = null;
   double[] weightDistribution = (distrArray!=null) ? distrArray : TS.counters().label_counts();
//   ASSERT(weightDistribution.low() == Globals.UNKNOWN_CATEGORY_VAL);
   if (tddtOptions.parentTieBreaking)
      myTiebreakingOrder = CatDist.merge_tiebreaking_order(tieBreakingOrder,
                                     weightDistribution);
   else
      myTiebreakingOrder = CatDist.tiebreaking_order(weightDistribution);
   
   ConstCategorizer leafCat = null;

   // @@ this is silly. We compute the majority category, make a ConstCat for
   // it, then turn around and predict a different category (the one that
   // produces the least loss). We use this majority to compute the number of
   // errors, even if we don't predict it!
   int majority = CatDist.majority_category(weightDistribution,
						  myTiebreakingOrder);

   if(tddtOptions.leafDistType == allOrNothing) {
      AugCategory	augMajority = new AugCategory(majority,
		     TS.get_schema().category_to_label_string(majority));
      logOptions.LOG(3, "All-or-nothing Leaf is: "); 
      leafCat = new ConstCategorizer(" ", augMajority, TS.get_schema());
      AugCategory bestPrediction = leafCat.get_category();
      logOptions.LOG(3, ""+bestPrediction.toString()+'\n');
      String myDescr = bestPrediction.description();//.read_rep();
      leafCat.set_description(myDescr);
   } else {
      double[] fCounts = weightDistribution;
      CatDist cDist = null;
      switch (tddtOptions.leafDistType) {
	 case frequencyCounts:
	    cDist = new CatDist(TS.get_schema(), fCounts, CatDist.none);
	    logOptions.LOG(3, "Frequency-count Leaf is: ");
	    break;
	 case laplaceCorrection:
	    cDist = new CatDist(TS.get_schema(), fCounts, CatDist.laplace,
				tddtOptions.MEstimateFactor);
	    logOptions.LOG(3, "Laplace Leaf is: ");
	    break;
	 case evidenceProjection:
	    cDist = new CatDist(TS.get_schema(), fCounts, CatDist.evidence,
				tddtOptions.evidenceFactor);
	    logOptions.LOG(3, "Evidence Leaf is: ");
	    break;
	 default:
	    MLJ.Abort();
      }
      logOptions.LOG(3, ""+cDist+'\n');
      cDist.set_tiebreaking_order(myTiebreakingOrder);
      AugCategory bestCategory = cDist.best_category();
      String myDescr = bestCategory.description();//.read_rep();
      leafCat = new ConstCategorizer(myDescr, cDist, TS.get_schema());
//      DBG(ASSERT(cDist == null));
   }

   myTiebreakingOrder = null; //delete myTiebreakingOrder;
   
//   ASSERT(leafCat);

//   DBGSLOW(
//	   InstanceRC dummy(leafCat.get_schema());
//	   MStringRC predDescr = leafCat.categorize(dummy).description();
//	   if (predDescr != leafCat.description())
//	      Error.fatalErr("cat descriptions don't match: I picked "
//	          +leafCat.description()+", leafCat predicted "
//	          +predDescr+". CatDist is "+leafCat.get_cat_dist());
//	   );
   
   if (distrArray != null) {
      double[] newDistr = new double[distrArray.length - 1];//(0, distrArray.length - 1, 0);
      for (int i = 0; i < newDistr.length; i++)
	    newDistr[i] = distrArray[i];
      leafCat.set_distr(newDistr);
   } else {
      // Use coarser granularity when approx_equal invoked with floats.
      if (MLJ.approx_equal((float)totalWeight,0.0)
	 && !tddtOptions.emptyNodeParentDist) {
	 double[] disArray = new double[TS.num_categories()];// (0, TS.num_categories(), 0);
	 leafCat.set_distr(disArray);
      } else
	 leafCat.build_distr(instance_list());
   }

   // If there are no instances, we predict like the parent and
   //   the penalty for pessimistic errors comes from the other children.
   //   Note that we can't just call num_cat because the distribution
   //   may be the parent's distribution
   // Use coarser granularity when approx_equal invoked with floats.
   if (MLJ.approx_equal((float)totalWeight,0.0)) {
      numErrors.value = 0;
      pessimisticErrors.value = 0;
   } else {
      numErrors.value = num_cat_errors(leafCat, majority, totalWeight);
      pessimisticErrors.value = CatTestResult.pessimistic_error_correction(
                          numErrors.value, totalWeight, get_pruning_factor());
   }

//   ASSERT(numErrors >= 0);
//   ASSERT(pessimisticErrors >= 0);
/*
   if (get_debug()) {
      int numChars = 128;
      char buffer[numChars];
      for (int chr = 0; chr < numChars; chr++)
	   buffer[chr] = '\0';
      MLCOStream *stream = new MLCOStream(EMPTY_STRING, buffer, numChars);
      CatDist score = leafCat.get_cat_dist();
      *stream << score.get_scores();
      String pDist = stream.mem_buf();
      stream = null; //delete stream;
      stream = new MLCOStream(EMPTY_STRING, buffer, numChars);
      *stream << leafCat.get_distr();
      String wDist = stream.mem_buf();
      stream = null; //delete stream;
      MString& newDescr = leafCat.description();
      String dbgDescr = newDescr + " (#=" + MString(totalWeight,0) +
	 " Err=" + MString(numErrors, 0) + "/" +
	String(pessimisticErrors, 2) + ")\\npDist=" + pDist +
	 "\\nwDist=" + wDist;
      leafCat.set_description(dbgDescr);
   }
*/
   Categorizer cat = leafCat;
   
   LeafCategorizer leafCategorizer = new LeafCategorizer(cat);
//   DBG(ASSERT(cat == null));
   return leafCategorizer;
}

/** Induce decision tree from a given split. The split is provided
 * in the form of a categorizer, which picks which subtree a given
 * instance will follow.
 * @param decisionTree Decision tree induced.
 * @param splitCat The categorizer for this split.
 * @param catNames List of category names.
 * @param tieBreakingOrder Order for breaking distribution ties.
 * @param numSubtreeErrors Number of errors this subtree produces in categorization of Instances.
 * @param pessimisticSubtreeErrors Error estimate if this was a leaf node.
 * @param numLeaves Number of leaves on a subtree.
 * @param remainingSiblings Siblings that have not be induced yet.
 */
protected void induce_tree_from_split(DecisionTree decisionTree, NodeCategorizer splitCat, LinkedList catNames, int[] tieBreakingOrder, DoubleRef numSubtreeErrors, DoubleRef pessimisticSubtreeErrors, IntRef numLeaves, int remainingSiblings)
{
   int[] myTiebreakingOrder =
      CatDist.merge_tiebreaking_order(tieBreakingOrder,
				       TS.counters().label_counts());
   InstanceList[] instLists =
      splitCat.split_instance_list(instance_list());
   // Add one if we have unknown instances
//   IFDRIBBLE(dribble_level(level, splitCat.description(), remainingSiblings));
   numSubtreeErrors.value = 0;
   pessimisticSubtreeErrors.value = 0;
   numLeaves.value = 0;
   DoubleRef numChildErrors = new DoubleRef(0);
   DoubleRef childPessimisticErrors = new DoubleRef(0);
   Node largestChild = null; // with the most instances (weight)
   DoubleRef maxChildWeight = new DoubleRef(-1);   
   for (int cat = 0; cat < instLists.length; cat++) {
      if (instLists[cat].num_instances() >= instance_list().num_instances())
	 Error.fatalErr("TDDTInducer.induce_tree_from_split: the most recent split "
	     +splitCat.description()+" resulted in no reduction of the "
	     +"instance list total weight (from "
	     +instance_list().total_weight()+" to "
	     +instLists[cat].total_weight());
      int remainingChildren = instLists.length - cat;
      Node child;
      if (instLists[cat].no_weight()) {
		// No weight of instances with this value.  Make it a leaf (majority),
		//   unless category unknown.
//		if (cat != UNKNOWN_CATEGORY_VAL)
//			IFDRIBBLE(dribble_level(level+1, "Leaf node",
//				remainingChildren));
		if (get_unknown_edges() || cat != Globals.UNKNOWN_CATEGORY_VAL) { 
			logOptions.LOG(3, "Category: " + (cat - 1)//-1 added to match MLC output -JL
				+" empty.  Assigning majority"+'\n');
			NodeCategorizer[] constCat = new NodeCategorizer[1];
			constCat[0] = create_leaf_categorizer(0, myTiebreakingOrder,
				numChildErrors, childPessimisticErrors);
			if (cat != Globals.UNKNOWN_CATEGORY_VAL)
				++numLeaves.value;  // don't count trivial leaves
			MLJ.ASSERT(numChildErrors.value == 0,"TDDTInducer.induce_tree_from_split: numChildErrors.value != 0");
			MLJ.ASSERT(childPessimisticErrors.value == 0,"TDDTInducer.induce_tree_from_split: childPessimisticErrors.value != 0");
			child = decisionTree.create_node(constCat, get_level() + 1);
			MLJ.ASSERT(constCat[0] == null,"TDDTInducer.induce_tree_from_split: constCat != null");
			//create_node gets ownership
			logOptions.LOG(6, "Created child leaf "+child+'\n');
			logOptions.LOG(6, "Connecting root "+decisionTree.get_root()
				+" to child "+child
				+" with string '"+(String)catNames.get(cat)+"'"+'\n');
			connect(decisionTree, decisionTree.get_root(), child,
				cat, (String)catNames.get(cat));
		}
      } else { // Solve the problem recursively.
		CGraph aCgraph = decisionTree.get_graph();
		logOptions.LOG(3, "Recursive call"+'\n');
		double totalChildWeight = instLists[cat].total_weight();
		TDDTInducer childInducer =
		create_subinducer(name_sub_inducer(splitCat.description(), cat),
			      aCgraph);
		childInducer.set_total_inst_weight(get_total_inst_weight());
		childInducer.assign_data(instLists[cat]);
		IntRef numChildLeaves = new IntRef(0);
		child = childInducer.induce_decision_tree(aCgraph,
						    myTiebreakingOrder,
						    numChildErrors,
						    childPessimisticErrors,
						    numChildLeaves,
						    remainingChildren);
		numSubtreeErrors.value += numChildErrors.value;
		pessimisticSubtreeErrors.value += childPessimisticErrors.value;
		numLeaves.value += numChildLeaves.value;
		if (totalChildWeight > maxChildWeight.value) {
			maxChildWeight.value = totalChildWeight;
			largestChild = child;
		}
		childInducer = null; //delete childInducer;
		Node root = decisionTree.get_root();
		logOptions.LOG(6, "Connecting child "+child+" to root "
			+root+", using "+cat
			+" with string '"+(String)catNames.get(cat)+"'"+'\n');
		connect(decisionTree, decisionTree.get_root(), child,
		cat, (String)catNames.get(cat));
	}
   }

   MLJ.clamp_above(maxChildWeight, 0, "TDDTInducer.induce_tree_from_split: "
   		   +"maximum child's weight must be non-negative");

	MLJ.ASSERT(largestChild != null,"TDDTInducer.induce_tree_from_split: largestChild == null");
//   DBGSLOW(decisionTree.OK(1));
   
   instLists = null; //delete &instLists;
/*   prune_subtree(decisionTree, myTiebreakingOrder,
		 largestChild, numSubtreeErrors, pessimisticSubtreeErrors,
		 numLeaves);
*/   myTiebreakingOrder = null; //delete myTiebreakingOrder;
/*   
   if (get_debug()) {
      // Cast away constness for modifying the name.
      Categorizer splitC = (Categorizer)decisionTree.
	         get_categorizer(decisionTree.get_root());
      String name = splitC.description();
      double[] distribution = splitC.get_distr();
      int numChars = 128;
      char buffer[numChars];
      for (int chr = 0; chr < numChars; chr++)
	         buffer[chr] = '\0';
      MLCOStream stream(EMPTY_STRING, buffer, numChars);
      stream << distribution;
      String distDescrip = stream.mem_buf();
      String newName = name + "\\nErr=" + String(numSubtreeErrors, 3) +
	 "/" + String(pessimisticSubtreeErrors, 3);
      if (splitC.class_id() != CLASS_CONST_CATEGORIZER)
	 newName += "\\nwDist=" + distDescrip;
      splitC.set_description(newName);
   }
*/
//   if (get_level() == 0)
//      DRIBBLE(endl);
}

/** Connects two nodes in the specified CatGraph.
 * @param catGraph The CatGreph containing these nodes.
 * @param from     The node from which the edge originates.
 * @param to       The node to which the edge connects.
 * @param edgeVal  The value of the AugCategory associated with that edge.
 * @param edgeName The name of the edge.
 */
protected void connect(CatGraph catGraph, Node from, Node to, int edgeVal, String edgeName)
{
   AugCategory edge = new AugCategory(edgeVal, edgeName);
   logOptions.GLOBLOG(6, "TDDTInducer's connect(), given string '" +edgeName
	   +"', is using '" + edge.description()
	   +"' as an edge description\n");
   catGraph.connect(from, to, edge);
//   ASSERT(edge == NULL); // connect() gets ownership
//   catGraph.OK(1);
}

/** Create a string to name the subinducer. We just append some basic info.
 * @return The name of the subinducer.
 * @param catDescr	The description of this subinducer.
 * @param catNum	The category number for which this subinducer is
 * inducing.
 */
public String name_sub_inducer(String catDescr, int catNum)
{
   String CAT_EQUAL = " Cat=";
   String CHILD_EQUAL = " child =";
   
   return description() + CAT_EQUAL + catDescr + CHILD_EQUAL + catNum;
}

/** Create_subinducer creates the Inducer for calling recursively. Note that since
 * this is an abstract class, it can't create a copy of itself.
 *
 * @param dscr The description for the sub inducer.
 * @param aCgraph The categorizer graph to use for the subinducer.
 * @return The new subinducer.
 */
abstract public TDDTInducer create_subinducer(String dscr, CGraph aCgraph);

/** When the subtree rooted from the current node does not improve
 * the error, the subtree may be replaced by a leaf or by its largest
 * child. This serves as a collapsing mechanism if the pruning factor
 * is 0, i.e., we collapse the subtree if it has the same number of
 * errors as all children.<P>
 * "Confidence" pruning is based on C4.5's pruning method. "Penalty"
 * pruning is based on "Pessimistic Decision tree pruning based on tree
 * size" by Yishay Mansour, ICML-97. "Linear" pruning is used to implement
 * cost-complexity pruning as described in CART.  Its use is not
 * recommended otherwise. "KLdistance" pruning uses the Kullback-Leibler
 * distance metric to determine whether to prune.<P>
 * This function is divided into three main parts. First, initial
 * checks are performed and values are set. Second, the test specific
 * to each pruning method is performed. Last, if pruning is
 * necessary, do it.
 * @param decisionTree Tree to be pruned.
 * @param tieBreakingOrder Order for breaking distribution ties.
 * @param largestChild The largest child node.
 * @param numSubtreeErrors Number of errors this subtree produces in categorization of Instances.
 * @param pessimisticSubtreeErrors Error estimate if this was a leaf node.
 * @param numLeaves Number of leaves on a subtree.
 */
public void prune_subtree(DecisionTree decisionTree,
				int[] tieBreakingOrder,
				Node largestChild,
				DoubleRef numSubtreeErrors,
				DoubleRef pessimisticSubtreeErrors,
				IntRef numLeaves)
{
logOptions.LOG(0,"Pruning is taking place.\n");
   MLJ.ASSERT(numSubtreeErrors.value >= 0,"TDDTInducer:prune_subtree:"
			+" numSubtreeErrors < 0");
   MLJ.ASSERT(pessimisticSubtreeErrors.value >= 0,"TDDTInducer:prune_subtree:"
			+" pessimisticSubtreeErrors < 0");
   Node treeRoot = decisionTree.get_root(true);

   // @@ CatDTInducers can't prune, but we don't want to check
   // get_prune_tree() here because even if we're not doing pruning, this code
   // does some useful safety checks. The checks aren't valid on
   // CatDTInducers, because they do not compute pessmisticSubtreeErrors.
//   if (this instanceof CatDTInducer) return;
//   if (class_id() == CatDT_INDUCER)
//      return;

//   DBGSLOW(if (numLeaves != decisionTree.num_nontrivial_leaves())
//	      Error.fatalErr("TDDTInducer.prune_subtree: number of leaves given "
//	          +numLeaves+" is not the same as the number counted "
//	          +decisionTree.num_nontrivial_leaves()));

//   DBGSLOW(
//       // We don't want any side effect logging only in debug level
//       logOptions logOpt(logOptions.get_log_options());
//       logOpt.set_log_level(0);
//       double pess_err =
//         pessimistic_subtree_errors(logOpt, decisionTree, treeRoot, *TS,
//				    get_pruning_factor(), tieBreakingOrder);
//       MLJ.verify_approx_equal(pess_err, pessimisticSubtreeErrors,
//			       "TDDTInducer.prune_subtree: pessimistic error"
//			       " differs from expected value");
//          );
   // How many errors (weighted) would we make with a leaf here?
   int myMajority = TS.majority_category(tieBreakingOrder);
   double numMajority = TS.counters().label_count(myMajority);
   double totalWeight = TS.total_weight();
   double myErrors = totalWeight - numMajority;
   if (!(MLJ.approx_greater(myErrors, numSubtreeErrors.value) ||
	MLJ.approx_equal(myErrors, numSubtreeErrors.value)))
      Error.fatalErr("TDDTInducer.prune_subtree: myErrors is not >= numSubtreeErrors"
	 +": myErrors - numSubtreeErrors = "+(myErrors - numSubtreeErrors.value));
   int numChildren = decisionTree.num_children(treeRoot);

   // test if a leaf; if so, we can exit immediately
   if (numChildren == 0) {
      numSubtreeErrors.value = totalWeight - numMajority;
      numLeaves.value = 1;
      return;
   }
   
   logOptions.LOG(3, "Testing at "
      +decisionTree.get_categorizer(treeRoot).description()
      +" (weight "+decisionTree.get_categorizer(treeRoot).total_weight()
      +')'+'\n');

   boolean pruneSubtree = false;
   boolean pruneChild = false;
   // We need to declare these here, as we use them during pruning
   double myPessimisticErrors = CatTestResult.pessimistic_error_correction(
                    myErrors, TS.total_weight(), get_pruning_factor());
   DoubleRef childPessimisticErrors = new DoubleRef(0);
   if (get_pruning_factor() == 0)  
      MLJ.verify_approx_equal(myPessimisticErrors, myErrors,
			      "TDDTInducer.prune_subtree:pessimistic error "
			      +"when computed for leaf, "
			      +"differs from expected value");

   switch (get_pruning_method()) {
      case confidence:
	 //@@ replace "100 * MLC.real_epsilon()" with "0.1" for
	 //@@   C4.5 functionality 
	 if (myPessimisticErrors - pessimisticSubtreeErrors.value <
	     100 * MLJ.realEpsilon ||
	     MLJ.approx_equal(myPessimisticErrors, pessimisticSubtreeErrors.value))
	    pruneSubtree = true;
	 
	 logOptions.LOG(3,"My pessimistic errors="+myPessimisticErrors
	    +", subtree errors="+pessimisticSubtreeErrors);
	 
	 if (get_pruning_branch_replacement()) {
	    Error.fatalErr("TDDTInducer.prune_subtree: Branch replacement not yet "
	       +"implemented");
	    // How many errors would we make if we took the most
	    // frequent branch?
	    childPessimisticErrors.value =
	       pessimistic_subtree_errors(logOptions.get_log_options(), decisionTree,
					  largestChild, TS,
					  get_pruning_factor(),
					  tieBreakingOrder);
	    if (childPessimisticErrors.value - pessimisticSubtreeErrors.value <
		MLJ.realEpsilon &&
		childPessimisticErrors.value < myPessimisticErrors) {
	       pruneSubtree = false;
	       pruneChild = true;
	    }
	    logOptions.LOG(3, ", child errors="+childPessimisticErrors);
	 }
	 logOptions.LOG(3, '\n');
//	 DBG(check_pessimistic_error_count(decisionTree,
//					   pessimisticSubtreeErrors,
//					   myPessimisticErrors));
	 break;
	 
      case lossConfidence: {
	 // Measures the expected loss given the current probability
	 // distribution, and compares it against the sum of losses at each
	 // child. The expected loss is skewed using the pessimistic
	 // correction. This only works for 2-class problems.
	 //ASSERT(TS.get_schema().num_label_values() == 2);

	 DoubleRef leafErrors =new DoubleRef(0);
       DoubleRef leafPessimisticErrors =new DoubleRef(0);
	 NodeCategorizer thisLeaf =
	    create_leaf_categorizer(totalWeight, tieBreakingOrder, leafErrors,
				    leafPessimisticErrors);
	 //ASSERT(thisLeaf.class_id() == CLASS_LEAF_CATEGORIZER);
	 Instance inst = new Instance(thisLeaf.get_schema());
	 CatDist dist = thisLeaf.score(inst);
	 AugCategory augPredicted = dist.best_category();
	 logOptions.LOG(3, "leaf here would predict "+augPredicted+'\n');
	 int predicted = augPredicted.num();
	 dist = null;
	 thisLeaf = null;
	 double nonPredicted = totalWeight - TS.counters().label_counts()[predicted];
	 double predictedPessimisticError =
	    CatTestResult.pessimistic_error_correction(nonPredicted,
							totalWeight,
							get_pruning_factor());
	 int minority = 1 - predicted; // @@ again, this is ugly.
	 double minorityLoss =
	    TS.get_schema().get_loss_matrix()[minority][predicted];
	 double myPessimisticLoss = predictedPessimisticError * minorityLoss;
	 logOptions.LOG(3, "My pessimistic errors="+predictedPessimisticError
	     +'\n');
	 // We have to compute the sum of pessimistic loss for each
	 // subtree. @@ It would be better to pass this back up from the
	 // children.
	 double subtreePessimisticLoss = pessimistic_subtree_loss(logOptions.get_log_options(), decisionTree,
				     treeRoot, TS, get_pruning_factor(),
				     tieBreakingOrder);

	 if (MLJ.approx_less(myPessimisticLoss, subtreePessimisticLoss) ||
	     MLJ.approx_equal(myPessimisticLoss, subtreePessimisticLoss))
	    pruneSubtree = true;
	 
	 logOptions.LOG(2, "My pessimistic loss="+myPessimisticLoss
	    +", total pessimistic subtree loss="+subtreePessimisticLoss
	    +'\n');

	 break;
      }

      case lossLaplace: {
	 // Same as lossConfidence, but skews using the laplace correction,
	 // rather than the pessimistic correction. This should allow it to
	 // generalize to more than 2-class problems.
	 Schema schema = TS.get_schema();
	 DoubleRef leafErrors = new DoubleRef(0);
       DoubleRef leafPessimisticErrors = new DoubleRef(0);
	 NodeCategorizer thisLeaf =
	    create_leaf_categorizer(totalWeight, tieBreakingOrder, leafErrors,
				    leafPessimisticErrors);
	 //ASSERT(thisLeaf.class_id() == CLASS_LEAF_CATEGORIZER);
	 Instance inst = new Instance(schema);
	 CatDist dist = thisLeaf.score(inst);
	 AugCategory augPredicted = dist.best_category();
	 logOptions.LOG(3, "leaf here would predict "+augPredicted+'\n');
	 int predicted = augPredicted.num();
	 dist = null;
	 thisLeaf = null;
	 
	 CatDist adjWeightDist = new CatDist(schema, TS.counters().label_counts(),
			       CatDist.laplace, get_pruning_factor());
	 double[] probDist = adjWeightDist.get_scores();
	 double[][] lossMatrix = schema.get_loss_matrix();

	 double myLaplaceLoss = 0;
	 for (int cat = 0; cat <= probDist.length; cat++)
	    myLaplaceLoss += probDist[cat] * lossMatrix[cat][predicted];
	 myLaplaceLoss *= TS.total_weight();
	 
	 logOptions.LOG(2, "This distribution is "+TS.counters().label_counts()
	    +", laplace loss is "+myLaplaceLoss+'\n');
	 double subtreeLaplaceLoss =
	    laplace_subtree_loss(logOptions.get_log_options(), decisionTree, treeRoot,
				 TS, tieBreakingOrder);

	 if (MLJ.approx_less(myLaplaceLoss, subtreeLaplaceLoss))
	    pruneSubtree = true;
	 
	 break;
      }
      
      case penalty: {
	 double penaltyErrors =
	    get_pruning_factor() * Math.sqrt(totalWeight*numLeaves.value)
	    + numSubtreeErrors.value;
	 MLJ.ASSERT(penaltyErrors >= numSubtreeErrors.value,"TDDTInducer."
           + "prune_subtree:penaltyErrors is less than numSubtreeErrors.");
	 if (!MLJ.approx_greater(myErrors,penaltyErrors))
	    pruneSubtree = true;

	 logOptions.LOG(2, "Total errors="+numSubtreeErrors+", penalty errors="
	     + penaltyErrors+'\n');
	 break;
      }
      
      case linear: {
	 double additErrors = myErrors - numSubtreeErrors.value;
	 //ASSERT(additErrors>=0);
	 double totalTrainingSetWeight = get_total_inst_weight();
	 double alpha = (additErrors/totalTrainingSetWeight) / (numLeaves.value-1);
	 if (!MLJ.approx_greater(alpha,get_pruning_factor()))
	    pruneSubtree = true;

	 logOptions.LOG(2,"totalWeight = "+totalWeight
	    +", numLeaves = "+numLeaves+", numErrors = "+myErrors
	    +", additErrors = "+additErrors+", alpha = "+alpha
	    +'\n');

	 break;
      }
      
      case KLdistance: {
	 double myDistance = calc_KL_distance(decisionTree);
	 //ASSERT(myDistance>=0);
	 if (!MLJ.approx_greater(myDistance, get_pruning_factor()))
	    pruneSubtree = true;

	 logOptions.LOG(3, "Kullback-Leibler distance = "+myDistance+'\n');

	 break;
      }

      case none:
	 break;
      
      default:
	 MLJ.Abort();	 
   }
   
   MLJ.ASSERT( ! ( pruneSubtree && pruneChild ),"TDDTInducer:prune_subtree:"
			+" pruneSubtree and pruneChild are both TRUE" );
//   DBGSLOW(decisionTree.OK(0));
   if (pruneSubtree) {
      NodeCategorizer[] newRootCat = new NodeCategorizer[1];
	newRootCat[0] = create_leaf_categorizer(totalWeight, tieBreakingOrder,
				 numSubtreeErrors, pessimisticSubtreeErrors);
//      DBG_DECLARE(Categorizer* catCopy = NULL);
//      DBG(catCopy = newRootCat.clone());
      MLJ.ASSERT(myErrors == numSubtreeErrors.value,"TDDTInducer:prune_subtree:"
			+" myErrors not equal to numSubtreeErrors");
      MLJ.verify_approx_equal(myPessimisticErrors,
			      pessimisticSubtreeErrors.value,
			      "TDDTInducer.prune_subtree: error in "
			      +"computation of pessimitic pruning");
      // we know we're not a trivial leaf, since we're pruning back
      numLeaves.value = 1;
      logOptions.LOG(3, "Pruning subtree at "
	  +decisionTree.get_categorizer(treeRoot).description()+" to "
	  +newRootCat[0].description()+" with "+TS.total_weight()
	 +" weight in " +TS.num_instances()+" instances"+'\n');
      // replace rootCat with newRootCat
      Node newNode = decisionTree.create_node(newRootCat, get_level());
//      DBG(ASSERT(newRootCat == NULL)); //create_node gets ownership
      decisionTree.delete_subtree(treeRoot, newNode);
      MLJ.ASSERT(treeRoot == decisionTree.get_root(true),"TDDTInducer:prune_subtree:"
			+" treeRoot not equal to decisionTree.get_root(true)");
      // Check that the categorizer was really moved over.
//      DBG(ASSERT(decisionTree.get_categorizer(treeRoot) == *catCopy));
//      DBG(delete catCopy);
      treeRoot = decisionTree.get_root(true);
//      DBG(decisionTree.OK(1));
//      DBGSLOW(decisionTree.OK(0));
   } else if (pruneChild) {
      Error.fatalErr("TDDTInducer.prune_subtree: branch replacement code reached "
	 +"-- this should not happen");
      Categorizer childCat =
	 decisionTree.get_categorizer(largestChild);
      logOptions.LOG(2, "Replacing subtree at "
	  +decisionTree.get_categorizer(treeRoot).description()
	 +" with " +childCat.description() +'\n');
      decisionTree.delete_subtree(treeRoot, largestChild);
      // Note that deletion just replaces the categorizer, so
      //   the root reference should remain valid
      MLJ.ASSERT(decisionTree.get_root() == treeRoot,"TDDTInducer:prune_subtree:"
			+" decisionTree.get_root() not equal to treeRoot");
      double distParameter = 1.0;
      if (tddtOptions.leafDistType == laplaceCorrection)
	 distParameter = tddtOptions.MEstimateFactor;
      if (tddtOptions.leafDistType == evidenceProjection)
	 distParameter = tddtOptions.evidenceFactor;
      MLJ.ASSERT(!TS.no_weight(),"TDDTInducer:prune_subtree:"
			+" TS.no_weight is TRUE");
      //@@ The following also should update numSubtreeErrors and numLeaves
      decisionTree.distribute_instances(treeRoot, TS, 0, 
					childPessimisticErrors,
					tddtOptions.leafDistType,
					distParameter,
					TS.counters().label_counts());
      pessimisticSubtreeErrors.value = childPessimisticErrors.value;
//      DBG(decisionTree.OK(1));
//      DBGSLOW(decisionTree.OK(0));
   }

}

/** Computes the subtree loss, skewing the leaf weight distributions
 * using the laplace correction.
 * @return The loss produced by this subtree.
 * @param logOptions The options for logging messages.
 * @param dt DecisionTree over which the metric is to be calculated.
 * @param subtree The current node used for calculation.
 * @param il The InstanceList containing Instances that reach the current subtree Node.
 * @param tieBreakingOrder Order for breaking distribution ties.
 */
protected double laplace_subtree_loss(LogOptions logOptions, DecisionTree dt, Node subtree, InstanceList il, int[] tieBreakingOrder)
{
   double totalLaplaceLoss;
   Schema schema = il.get_schema();
   NodeCategorizer splitCat = dt.get_categorizer(subtree);

   if (dt.num_children(subtree) == 0) {
//      ASSERT(splitCat.class_id() == CLASS_LEAF_CATEGORIZER);
      LeafCategorizer leafCat = (LeafCategorizer)splitCat;
//      ASSERT(leafCat.get_categorizer().class_id() == CLASS_CONST_CATEGORIZER);
      if (il.no_weight())
	 totalLaplaceLoss = 0;
      else {
	 double[] weightDistr = il.counters().label_counts();
	 double lapruneCorrection = get_pruning_factor();
	 CatDist catDist = new CatDist(schema, weightDistr, CatDist.laplace,
			 lapruneCorrection);
	 catDist.set_tiebreaking_order(tieBreakingOrder);
	 AugCategory predictedCat = catDist.best_category();
	 int predict = predictedCat.num();
	 totalLaplaceLoss = 0;
	 double[][] lossMatrix = schema.get_loss_matrix();
	 double[] scores = catDist.get_scores();
	 
	 for (int actual = 0; actual <= scores.length; actual++)
	    totalLaplaceLoss += scores[actual] * lossMatrix[actual][predict];
	 totalLaplaceLoss *= il.total_weight();
//	 FLOG(4, "Subtree laplace-corrected loss for leaf " <<
//	      splitCat.description() << " dist=" << weightDistr
//	      << " laplace loss=" << totalLaplaceLoss << endl);
      }
   } else {
/*      if (splitCat.class_id() != CLASS_CONST_CATEGORIZER &&
          splitCat.class_id() != CLASS_MULTITHRESH_CATEGORIZER &&
          splitCat.class_id() != CLASS_THRESHOLD_CATEGORIZER &&
          splitCat.class_id() != CLASS_ATTR_CATEGORIZER &&
          splitCat.class_id() != CLASS_ATTR_SUBSET_CATEGORIZER &&
	  splitCat.class_id() != CLASS_DISC_NODE_CATEGORIZER)
	 Error.fatalErr("TDDTInducer.laplace_subtree_loss: unrecognized "
	    +"node class with id "+splitCat.class_id());
*/      
      totalLaplaceLoss = 0;
      InstanceList[] instLists = splitCat.split_instance_list(il);

      CGraph cGraph = dt.get_graph();
//      forall_adj_edges(edgePtr, subtree) {
      for(Edge edgePtr = subtree.First_Adj_Edge(0);
				edgePtr != null;
				edgePtr = edgePtr.Succ_Adj_Edge(subtree)){
	 int num = ((AugCategory)cGraph.inf(edgePtr)).num();
	 Node child = edgePtr.target();
//	 ASSERT((*instLists)[num]);
	 int[] subOrder =
	    CatDist.merge_tiebreaking_order(tieBreakingOrder,
				instLists[num].counters().label_counts());
	 totalLaplaceLoss += laplace_subtree_loss(logOptions, dt,
						  child,
						  instLists[num],
						  subOrder);
	 subOrder = null;
	 instLists[num] = null;
      }
      
//      FLOG(4, "Subtree laplace loss for node " <<
//	   splitCat.description() << " Total weight =" << il.total_weight()
//	   << " laplace=" << totalLaplaceLoss << endl);
	   
      // Make sure we don't have any leftover instances or this is a bug
      // The only exception is the UNKNOWN_CATEGORY, which may be
      //    left out if we have no unknown edges
//      for (int cat = instLists.low(); cat <= instLists.length; cat++) {
      for (int cat = 0; cat <= instLists.length; cat++) {
	 if (instLists[cat] != null)
	    // Maybe we don't have unknown edges.  Use StoredReal to use
	    //   coarser granularity, and use a multiplier of 10 for the
	    //   default granularity.
	    if (MLJ.approx_equal((float)
				 (instLists[cat].total_weight()),
				 (float)0, 10) ||
		cat == Globals.UNKNOWN_CATEGORY_VAL){
	       instLists[cat] = null;
	    } else
	       Error.fatalErr("TDDTInducer.laplace_subtree_loss: "
		  +"Missed instance list "+cat);
      }
      instLists = null;
   }
   return totalLaplaceLoss;
}   

/** Computes the pessimistic errors for subtree given a training set.
 * We only prune subtrees that have const categorizers at the leaves.
 * It may be an interesting research topic finding ways to prune nodes
 * containing other categorizers (naive-bayes, for example).
 * @return The number of pessimistic errors.
 * @param logOptions The options for logging information.
 * @param dt The decision tree for which a pessimistic errors value is calculated.
 * @param subtree The current node in the decision tree.
 * @param il The list of instances for determining errors.
 * @param zValue Z value for pessimistic error correction.
 * @param tieBreakingOrder Order for breaking distribution ties.
 */
protected double pessimistic_subtree_errors(LogOptions logOptions, DecisionTree dt, Node subtree, InstanceList il, double zValue, int[] tieBreakingOrder)
{
   double totalPessimisticErrors;

   NodeCategorizer splitCat = dt.get_categorizer(subtree);
   if (dt.num_children(subtree) == 0) {
//      ASSERT(splitCat.class_id() == CLASS_LEAF_CATEGORIZER);
      LeafCategorizer leafCat = (LeafCategorizer)splitCat;
//      ASSERT(leafCat.get_categorizer().class_id() == CLASS_CONST_CATEGORIZER);
      // StoredReal parameters mean coarser granularity for approx_equal.
      if (MLJ.approx_equal((float)(il.total_weight()), (float)0))
	 totalPessimisticErrors = 0;
      else {
	 int majority = il.majority_category(tieBreakingOrder);
	 double numErrors = il.total_weight() - 
	    il.counters().label_counts()[majority];
//	 ASSERT(numErrors >= 0);
	 totalPessimisticErrors =
	    CatTestResult.pessimistic_error_correction(numErrors,
							il.total_weight(),
							zValue);
//	 FLOG(4, "Subtree pessimistic errors for leaf " <<
//	      splitCat.description() << " Total=" << il.total_weight()
//	      << " errors=" << numErrors << " pessimistic=" 
//	      << totalPessimisticErrors << endl);
      }
   } else {
/*      if (splitCat.class_id() != CLASS_CONST_CATEGORIZER &&
          splitCat.class_id() != CLASS_MULTITHRESH_CATEGORIZER &&
          splitCat.class_id() != CLASS_THRESHOLD_CATEGORIZER &&
          splitCat.class_id() != CLASS_ATTR_CATEGORIZER &&
          splitCat.class_id() != CLASS_ATTR_SUBSET_CATEGORIZER &&
	  splitCat.class_id() != CLASS_DISC_NODE_CATEGORIZER)
	 err << "TDDTInducer.pessimistic_subtree_errors: unrecognized "
	    "node class with id " << splitCat.class_id()
	     << fatal_error;
*/      
      totalPessimisticErrors = 0;
      InstanceList[] instLists = splitCat.split_instance_list(il);
//      Edge edgePtr;
      CGraph cGraph = dt.get_graph();
//      forall_adj_edges(edgePtr, subtree) {
      for(Edge edgePtr = subtree.First_Adj_Edge(0);
				edgePtr != null;
				edgePtr = edgePtr.Succ_Adj_Edge(subtree)){
	 int num = ((AugCategory)cGraph.inf(edgePtr)).num();
	 Node child = edgePtr.target();
//	 ASSERT(instLists[num]);
	 int[] subOrder =
	    CatDist.merge_tiebreaking_order(tieBreakingOrder,
			       instLists[num].counters().label_counts());
	 totalPessimisticErrors += pessimistic_subtree_errors(logOptions,
				   dt, child, instLists[num], zValue,
							      subOrder);
	 subOrder = null;
	 instLists[num] = null;
      }
      
//      FLOG(4, "Subtree pessimistic errors for node " <<
//	   splitCat.description() << " Total=" << il.total_weight()
//	   << " pessimistic=" << totalPessimisticErrors << endl);
	   
      // Make sure we don't have any leftover instances or this is a bug
      // The only exception is the UNKNOWN_CATEGORY, which may be
      //    left out if we have no unknown edges
//      for (int cat = instLists.low(); cat <= instLists.high(); cat++) {
      for (int cat = 0; cat <= instLists.length; cat++) {
	 if (instLists[cat] != null)
	    // Maybe we don't have unknown edges.  Use StoredReal to use
	    //   coarser granularity, and use a multiplier of 10 for the
	    //   default granularity.
	    if (MLJ.approx_equal((float)(instLists[cat].total_weight()),
				 (float)0, 10) ||
		cat == Globals.UNKNOWN_CATEGORY_VAL){
	       instLists[cat] = null;
	    } else
	       Error.fatalErr("TDDTInducer.pessimistic_subtree_errors: "
		  +"Missed instance list "+cat);
      }
      instLists = null;
   }
   return totalPessimisticErrors;
}   

/** Computes the pessimistic loss for subtree given a training set.
 * @return The number of pessimistic loss.
 * @param logOptions The options for logging information.
 * @param dt The decision tree for which a pessimistic loss value is calculated.
 * @param subtree The current node in the decision tree.
 * @param il The list of instances for determining errors.
 * @param zValue Z value for pessimistic error correction.
 * @param tieBreakingOrder Order for breaking distribution ties.
 */
protected double pessimistic_subtree_loss(LogOptions logOptions, DecisionTree dt, Node subtree, InstanceList il, double zValue, int[] tieBreakingOrder)
{
   double totalPessimisticLoss;

   NodeCategorizer splitCat = dt.get_categorizer(subtree);

   if (dt.num_children(subtree) == 0) {
//      ASSERT(splitCat.class_id() == CLASS_LEAF_CATEGORIZER);
      LeafCategorizer leafCat = (LeafCategorizer)splitCat;
//      ASSERT(leafCat.get_categorizer().class_id() == CLASS_CONST_CATEGORIZER);
      if (il.no_weight())
	 totalPessimisticLoss = 0;
      else {
	 Instance dummy = new Instance(leafCat.get_schema());
	 CatDist dist = leafCat.score(dummy);
	 int predicted = dist.best_category().num();
	 dist = null;
	 double numErrors = il.total_weight() -
	    il.counters().label_counts()[predicted];
//	 ASSERT(numErrors >= 0);
	 double totalPessimisticErrors =
	    CatTestResult.pessimistic_error_correction(numErrors,
							il.total_weight(),
							zValue);
	 // @@ this is probably ugly, but we have no way to pick the minority
	 // besides this.
	 int minority = 1 - predicted;
	 double minorityLoss =
	    il.get_schema().get_loss_matrix()[minority][predicted];
	 totalPessimisticLoss =
	    totalPessimisticErrors * minorityLoss;
//	 FLOG(1, "Subtree pessimistic errors for leaf " <<
//	      splitCat.description() << " Total=" << il.total_weight()
//	      << " errors=" << numErrors << " pessimistic=" 
//	      << totalPessimisticErrors
//	      << " pessimistic loss=" << totalPessimisticLoss << endl);
      }
   } else {
/*      if (splitCat.class_id() != CLASS_CONST_CATEGORIZER &&
          splitCat.class_id() != CLASS_MULTITHRESH_CATEGORIZER &&
          splitCat.class_id() != CLASS_THRESHOLD_CATEGORIZER &&
          splitCat.class_id() != CLASS_ATTR_CATEGORIZER &&
          splitCat.class_id() != CLASS_ATTR_SUBSET_CATEGORIZER &&
	  splitCat.class_id() != CLASS_DISC_NODE_CATEGORIZER)
	 err << "TDDTInducer.pessimistic_subtree_loss: unrecognized "
	    "node class with id " << splitCat.class_id()
	     << fatal_error;
  */    
      totalPessimisticLoss = 0;
      InstanceList[] instLists = splitCat.split_instance_list(il);

      CGraph cGraph = dt.get_graph();
//      forall_adj_edges(edgePtr, subtree) {
      for(Edge edgePtr = subtree.First_Adj_Edge(0);
				edgePtr != null;
				edgePtr = edgePtr.Succ_Adj_Edge(subtree)){
	 int num = ((AugCategory)cGraph.inf(edgePtr)).num();
	 Node child = edgePtr.target();
//	 ASSERT((*instLists)[num]);
	 int[] subOrder =
	    CatDist.merge_tiebreaking_order(tieBreakingOrder,
				instLists[num].counters().label_counts());
	 totalPessimisticLoss += pessimistic_subtree_loss(logOptions, dt,
							  child,
							  instLists[num],
							  zValue, subOrder);
	 subOrder = null;
	 instLists[num] = null;
      }
      
//      FLOG(4, "Subtree pessimistic loss for node " <<
//	   splitCat.description() << " Total weight =" << il.total_weight()
//	   << " pessimistic=" << totalPessimisticLoss << endl);
	   
      // Make sure we don't have any leftover instances or this is a bug
      // The only exception is the UNKNOWN_CATEGORY, which may be
      //    left out if we have no unknown edges
//      for (int cat = instLists->low(); cat <= instLists->high(); cat++) {
      for (int cat = 0; cat <= instLists.length; cat++) {
	 if (instLists[cat] != null)
	    // Maybe we don't have unknown edges.  Use StoredReal to use
	    //   coarser granularity, and use a multiplier of 10 for the
	    //   default granularity.
	    if (MLJ.approx_equal((float)
				 (instLists[cat].total_weight()),
				 (float)0, 10) ||
		cat == Globals.UNKNOWN_CATEGORY_VAL){
	       instLists[cat] = null;
	    } else
	       Error.fatalErr("TDDTInducer.pessimistic_subtree_loss: "
		  +"Missed instance list "+cat);
      }
      instLists = null;
   }
   return totalPessimisticLoss;
}   

/** Determines the Kullback Leibler distance between the root of the decision tree and
 * all of its children.
 * @param decisionTree The decision tree over which the metric is to be used.
 * @return The distance value.
 */
public double calc_KL_distance(DecisionTree decisionTree)
{
   
   Node treeRoot = decisionTree.get_root(true);
   int numChildren = decisionTree.num_children(treeRoot);
//   ASSERT(numChildren != 0);

   int i;
   Categorizer cat = decisionTree.get_categorizer(treeRoot);
   double totalWeight = cat.total_weight();
   double[] constParentDist = cat.get_distr();
   double[] augParentDist = new double[constParentDist.length +1];
		//(UNKNOWN_CATEGORY_VAL, constParentDist.size() + 1,0);
   for (i = 0; i <= augParentDist.length; i++)
      augParentDist[i] = constParentDist[i];
   
   // Normalize distribution
   //  If only Array had a normalize() or /= function...
   int[] dummyOrder = CatDist.tiebreaking_order(augParentDist);
   DoubleRef leafErrors = new DoubleRef(0);
   DoubleRef leafPessimisticErrs = new DoubleRef(0);
   LeafCategorizer leafCat =
      create_leaf_categorizer(cat.total_weight(), dummyOrder, leafErrors,
			      leafPessimisticErrs, augParentDist);
   dummyOrder = null;
   Categorizer innerCat = leafCat.get_categorizer();
//   ASSERT(innerCat.class_id() == CLASS_CONST_CATEGORIZER);
   ConstCategorizer constCat = (ConstCategorizer)innerCat;
   CatDist leafCatDist = constCat.get_cat_dist();
   double[] parentDist = leafCatDist.get_scores();
   
   logOptions.LOG(2,"parent distribution: "+constParentDist+" , after normalizing"
       +parentDist+", numChildren=" +numChildren +", totalWeight="
       +totalWeight );
   
   // iterate through all children
   Node child = null;
   double aveCartDistance = 0;
   double maxCartDistance = 0;
   double aveKLDistance = 0;
   double maxKLDistance = 0;
   // Keep track of the number of not empty children in the following
   int numChild = 0;
//   forall_adj_nodes(child, treeRoot) {
   Edge Loopvar=treeRoot.First_Adj_Edge(0);
   for(child = (Loopvar!=null)? Loopvar.opposite(treeRoot,Loopvar) : null;
       child!=null;
       Loopvar=Loopvar.Succ_Adj_Edge(treeRoot)){
      logOptions.LOG(2,"Trying new child"+'\n');
      Categorizer childCat = decisionTree.get_categorizer(child);
      double totalChildWeight = childCat.total_weight();
      if (totalChildWeight > 0) {
	 double[] constChildDist = childCat.get_distr();
	 double[] augChildDist = new double[constChildDist.length+1];
			 //(Globals.UNKNOWN_CATEGORY_VAL,constChildDist.length + 1, 0);
	 for (i = 0; i <= augChildDist.length; i++)
	    augChildDist[i] = constChildDist[i];
	 // Normalize the distribution
	 dummyOrder = CatDist.tiebreaking_order(augChildDist);
	 DoubleRef childErrors =new DoubleRef(0);
       DoubleRef childPessimisticErrs =new DoubleRef(0);
	 LeafCategorizer thisChildCat =
	    create_leaf_categorizer(totalChildWeight, dummyOrder,
				    childErrors, childPessimisticErrs,
				    augChildDist);
	 dummyOrder = null;
	 Categorizer childInnerCat = thisChildCat.get_categorizer();
//	 ASSERT(childInnerCat.class_id() == CLASS_CONST_CATEGORIZER);
	 ConstCategorizer childConstCat =
	    (ConstCategorizer)childInnerCat;
	 CatDist childCatDist = childConstCat.get_cat_dist();
	 double[] childDist = childCatDist.get_scores();
 
	 logOptions.LOG(2,"child distribution: "+constChildDist 
	     +", after normalizing:" +childDist +", totalWeight="
	     +totalChildWeight +'\n');

	 // Calculate the distance
	 double dist = 0;
//	 for (i=childDist.low() ; i<=childDist.high() ; i++) {
	 for (i=0; i<=childDist.length; i++) {
	    dist += (childDist[i]-parentDist[i]) *
	       (childDist[i]-parentDist[i]);
	 }
	 dist = Math.sqrt(dist);
	 //Real kullbackDistance = NaiveBayesCat.kullback_leibler_distance(parentDist,childDist);
	 //Real kullbackDistance = kullback_leibler_distance(parentDist,childDist);
	 double kullbackDistance = kullback_leibler_distance(childDist,parentDist);
	 // Update the distance variables
	 aveCartDistance += dist;
	 if (maxCartDistance <= dist) {
	    maxCartDistance = dist;
	 }
	 aveKLDistance += totalChildWeight / totalWeight * kullbackDistance;
	 if (maxKLDistance <= kullbackDistance) {
	    maxKLDistance = kullbackDistance;
	 }
	 logOptions.LOG(2, "KL distance=" +kullbackDistance +'\n');
	 ++numChild;
	 thisChildCat = null;
      } else {
	 logOptions.LOG(2,"child has no instances" +'\n');
      }
   }
   leafCat = null;
   logOptions.LOG(2,"ave cart distance: " +aveCartDistance/numChild 
       +" max cart distance: " +maxCartDistance +" ave KL distance: " 
       +aveKLDistance/totalWeight +" max KL distance: " +maxKLDistance
       +'\n');
   MLJ.ASSERT(aveCartDistance>=0,"TDDTInducer.calc_KL_distance:aveCartDistance is less than zero.");
   MLJ.ASSERT(maxCartDistance>=0,"TDDTInducer.calc_KL_distance:maxCartDistance is less than zero.");
   MLJ.ASSERT(aveKLDistance>=0,"TDDTInducer.calc_KL_distance:aveKLDistance is less than zero.");
   MLJ.ASSERT(maxKLDistance>=0,"TDDTInducer.calc_KL_distance:maxKLDistance is less than zero.");
   //@@ Pick one of the following
   //return aveCartDistance/numChild;
   //return maxCartDistance;
   return aveKLDistance;
   //return maxKLDistance;
}

/** Computes a Kullback Leibler distance metric given an array of p(x)
 * and q(x) for all x. The Kullback Leibler distance is defined as:<BR>
 * sum over all x  p(x) log(p(x)/q(x))			<P>
 * This is not idential to the one in NaiveBayesCat, as this one has
 * been changed to handle 0's in the arrays as follows			<BR>
 * <TABLE>
 * <TR> <TH>   p(x) <TH> q(x) <TH> kl(p,q) </TR>
 * <TR> <TD>   0    <TD> 0    <TD> ignore  </TR>
 * <TR> <TD>   0    <TD> >0   <TD> 0       </TR>
 * <TR> <TD>   >0   <TD> 0    <TD> error   </TR>
 * <TR> <TD>   >0   <TD> >0   <TD> OK      </TR>
 * </TABLE>
 * Note that ignore and 0 are note the same, as the former does not
 * update the count of the number of non-empty children.
 * @param p The child node distribution.
 * @param q The parent node distribution.
 * @return The distance value.
 */
static public double kullback_leibler_distance(double[] p,
					    double[] q)
{
   if(p.length!= q.length)
      Error.fatalErr("kullback_leibler_distance: p and q arrays have different "
	 +"sizes");

   DoubleRef sum =new DoubleRef(0);
   double sump = 0;
   double sumq = 0;
   for (int i=0; i<p.length; i++) {
      if (p[i] < 0)
	 Error.fatalErr("p(" + i + ") < zero");
      if (q[i] < 0)
	 Error.fatalErr("q(" + i + ") < zero");
      if (p[i] != 0 && q[i] == 0) {
	 Error.fatalErr("p(" + i + ") != zero, q(" + i + ") == zero"
	    +", p = " + p + ", q = " + q );
      }
      if (q[i] != 0 && p[i] != 0) {
	 sum.value += (p[i] * MLJ.log_bin(p[i] / q[i]));
      }
      sump += p[i];
      sumq += q[i];
   }
   
   MLJ.verify_approx_equal(sump, 1, "KL distance: sum of p doesn't add to 1");
   MLJ.verify_approx_equal(sumq, 1, "KL distance: sum of q doesn't add to 1");
   MLJ.clamp_above(sum, 0, "KL distance must be positive");
   
   return sum.value;
}

/** Returns the categorizer that the Inducer has generated.
 * @return The generated Categorizer.
 */
public Categorizer get_categorizer()
{
   was_trained(true);
   return decisionTreeCat;
}

/** Returns the number of nodes (categorizers) in the decision tree, and
 * number of leaves.
 * @return An integer representing the number of categorizers(Node and Leaf)
 * in the decision tree.
 */
public int num_nodes()
{
   was_trained(true);
   return decisionTreeCat.num_nodes();
}

/** Gives ownership of the generated categorizer to the caller, reverting the
 * Inducer to untrained state.
 * @return The Categorizer created by training this Inducer.
 */
public Categorizer release_categorizer()
{
   was_trained(true);
   Categorizer retCat = decisionTreeCat;
   decisionTreeCat = null;
   return retCat;
}

/** Prunes decision tree.
 * @param dTree The decision tree to be pruned.
 * @param node The current node in the decision tree.
 * @param il Instance list for determining errors.
 * @param prune TRUE if the decision tree is to be pruned, FALSE otherwise.
 * @return The error of the pruned tree.
 */

   public double prune(DecisionTree dTree, Node node, InstanceList il, boolean prune) {

      int i = 0, largestbranchsize = 0;
      double treeerror=0, leaferror=0, brancherror= 0; 
      int misclass = 0;
      int numChildren = node.outdeg();
      Node largestbranch = node;            
      
      double[] computedCounts = null;
      double[] labelCounts = null;
      NominalAttrInfo nai = il.nominal_label_info();
      computedCounts =new double[nai.num_values()+1]; // (Globals.UNKNOWN_CATEGORY_VAL, nai.num_values() + 1, 0);
      double[] count = computedCounts;
      for(ListIterator pixLI = il.instance_list().listIterator();pixLI.hasNext();){
         Instance pix = (Instance)pixLI.next();
         count[nai.get_nominal_val(pix.get_label())] += pix.get_weight();
      }
      labelCounts = computedCounts;
  
      int best = CatDist.majority_category(labelCounts, il.get_distribution_order());
      computedCounts = null;

	for (i=0;i<labelCounts.length;i++) {
         if (i!=best) {misclass+=labelCounts[i];}
      }

      if (numChildren == 0) {return predictErrors(il.num_instances(), misclass);}
      else {
         NodeCategorizer splitCat = ((NodeInfo)(node.data)).get_categorizer();
         InstanceList[] ilsplit = splitCat.split_instance_list(il);
         if (numChildren==ilsplit.length) {i = 0;}
         else {i=1;}
         for(Edge edgePtr = node.First_Adj_Edge(0);edgePtr != null;edgePtr = edgePtr.Succ_Adj_Edge(node)){
            Node child = edgePtr.target();
            treeerror += prune(dTree, edgePtr.target(), ilsplit[i], prune);
            if (ilsplit[i].num_instances() > largestbranchsize) {
               largestbranch = child;
               largestbranchsize = ilsplit[i].num_instances();
            }
            i++;
         }
         leaferror = predictErrors(il.num_instances(), misclass);
         brancherror = prune(dTree, largestbranch, il, false);
      }

 	if (!prune) {return treeerror;}

      if (leaferror<=treeerror+0.1&&leaferror<=brancherror+0.1) {
	      Node newnode = getNewNode(node, best);
            dTree.delete_subtree(node, newnode);
		return leaferror;
	}
	else {
	   if (brancherror<=treeerror+0.1) {
		dTree.delete_subtree(node, largestbranch);
            return brancherror;
         }
         else {return treeerror;}
      }
   }

   /**
    * @param total
    * @param misclass
    * @return
    */   
   public double predictErrors(int total, int misclass) {
	return misclass + addErrs(total, misclass);
   }

   /**
    * @param N
    * @param e
    * @return
    */   
   public double addErrs(double N, double e){

      double Val[] = {0, 0.000000001, 0.00000001, 0.0000001, 0.000001, 0.00001, 0.00005, 0.0001, 0.0005, 0.001, 0.005, 0.01, 0.05, 0.10, 0.20, 0.40, 1.00};
      double Dev[] = {100, 6.0, 5.61, 5.2, 4.75, 4.26, 3.89, 3.72, 3.29, 3.09, 2.58, 2.33, 1.65, 1.28, 0.84, 0.25, 0.00};

      double CF = .25;
      double Val0, Pr, Coeff = 0;
      int i;
    
      i = 0;
      while (CF > Val[i]) {
        i++;
      }
      Coeff = Dev[i-1]+(Dev[i] - Dev[i-1]) * (CF-Val[i-1]) / (Val[i] - Val[i-1]);
      Coeff = Coeff * Coeff;
    
      if (e < .000001) {
         return N * (1 - Math.exp(Math.log(CF) / N));
      } else {
         if (e < 0.9999) {
            Val0 = N * (1 - Math.exp(Math.log(CF) / N));
            return Val0 + e * (addErrs(N, 1.0) - Val0);
         } else {
            if (e + 0.5 >= N) {
	         return 0.67 * (N - e);
            } else {
               Pr = (e + 0.5 + Coeff / 2 + Math.sqrt(Coeff * ((e + 0.5) * (1 - (e + 0.5) / N) + Coeff / 4))) / (N + Coeff);
               return (N * Pr - e);
	      }
         }
      }
   }

   /** Creates a new categorizer Node.
    * @param node The Node for which a categorizer Node is to be created.
    * @param category The category to be stored in this Node.
    * @return The new Node if not a leaf or the old Node if the result is a leaf.
    */   
   public Node getNewNode(Node node, int category) {

      int numchildren = node.outdeg(), leafcategory, i;
    
      if (numchildren == 0) { 
         leafcategory=((ConstCategorizer)((LeafCategorizer)((NodeInfo)node.data).get_categorizer()).get_categorizer()).get_category().num();
         if (leafcategory == category) {
            return node;
         }
      }  
      else {
         for(Edge edgePtr = node.First_Adj_Edge(0);edgePtr != null;edgePtr = edgePtr.Succ_Adj_Edge(node)){
	      Node child = edgePtr.target();
            Node newnode =  getNewNode(child, category);
            if (newnode != null) {return newnode;}
         }
      }   
      return null;
   }

   /** Sets the errorprune value to the given value.
    * @param prune The new value for the errorprune.
    */   
	public void prune(boolean prune) {
		errorprune = prune;
	} 
}