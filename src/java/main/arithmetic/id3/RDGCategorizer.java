package arithmetic.id3;
import java.io.BufferedWriter;
import arithmetic.shared.Error;
import java.io.IOException;

import arithmetic.shared.AugCategory;
import arithmetic.shared.CatDist;
import arithmetic.shared.Categorizer;
import arithmetic.shared.DisplayPref;
import arithmetic.shared.Globals;
import arithmetic.shared.Instance;
import arithmetic.shared.InstanceList;
import arithmetic.shared.Node;
import arithmetic.shared.Schema;

/** Categorizer goes down the RootedCatGraph using the Categorizer at each node
 * until it reaches a node with no children. That node returns the category.
 *
 */
public class RDGCategorizer extends Categorizer {
    /** The rooted categorizer graph which is searched.
     */
    protected RootedCatGraph rcGraph;
    /** The distribution type of the leaves in this graph categorizer.
     */
    protected byte leafDistType;  //TDDTInducer.LeafDistType enum
    /** The leaf distribution parameter.
     */
    protected double leafDistParameter;
    /** TRUE if there are loss values for each node, FALSE otherwise.
     */
    protected boolean hasNodeLosses;
    
    /** Initializes the RDGCategorizer.
     * @param rcg The categorizer graph used for this RDGCategorizer.
     * @param descr The description of this RDGCategorizer.
     * @param numCat The number of possible category values.
     * @param sch The schema associated with the data this RDGCategorizer categorizes.
     */
    public RDGCategorizer(RootedCatGraph rcg, String descr, int numCat,
    Schema sch) {
        super(numCat,descr,sch);
        rcGraph = rcg;  //Change to copy constructor
        leafDistType = TDDTInducer.allOrNothing;
        leafDistParameter = 1.0;
        hasNodeLosses = false;
        
        rcg = null;
        if (rcGraph != null) {
            NodeCategorizer rootCat =
            ((NodeInfo)rcGraph.get_graph().entry(rcGraph.get_root())).get_categorizer();
            if (rootCat.has_distr())
                set_distr(rootCat.get_distr());
        }
        
        //      /*  NOT FINISHED HERE */
        //      System.out.println("Warning-->RDGCategorizer constructor not fully "
        //            + "implmented ");
    }
    
    /** Returns the number of nodes in this RDGCategorizer.
     * @return The number of nodes in this RDGCategorizer.
     */
    public int num_nodes() {
        return rooted_cat_graph().num_nodes();
    }
    /** Returns the number of leaves in this RDGCategorizer.
     * @return The number of leaves in this RDGCategorizer.
     */
    public int num_leaves() {
        return rooted_cat_graph().num_leaves();
    }
    
    /** Returns the number of nontrivial leaves in this RDGCategorizer.
     * @return The number of nontrivial leaves in this RDGCategorizer.
     */
    public int num_nontrivial_leaves() {
        return rooted_cat_graph().num_nontrivial_leaves();
    }
    
    /** Returns the number of nontrivial nodes in this RDGCategorizer.
     * @return The number of nontrivial nodes in this RDGCategorizer.
     */
    public int num_nontrivial_nodes() {
        return rooted_cat_graph().num_nontrivial_nodes();
    }
    
    /** Returns the graph of categorizers.
     * @return The graph of categorizers.
     */
    public RootedCatGraph rooted_cat_graph() {
        if(rcGraph == null)
            Error.err("RootedCatGraph.rooted_cat_graph: null rcGrph"
            + "-->fatal_error");
        return rcGraph;
    }
    
    /** We intend to replace each real threshold value with the value of the instance
     * nearest (less than or equal) it in value. For each attribute, build a list
     * (array) of all associated thresholds. This will eventually hold the modified
     * thresholds, to be copied back into the graph, so keep a NodePtr with each
     * threshold. Also, build a list (array) of all values for this attribute obtained
     * from instanceList. Sort the lists. For each attribute, walk throuhgh the two
     * data structures, replacing the threshold value in the first with the greatest
     * item from the second that is less than or equal to the threshold it is
     * replacing. Update the graph--for each node with a real-valued threshold
     * categorizer, identify the item in the array with the same NodePtr, and copy the
     * updated threshold back into the graph.
     * @param instances The instances over which thresholds are found.
     */
    public void adjust_real_thresholds(InstanceList instances) {
        System.out.println("Warning-->RDGCategorizer.adjust_real_thresholds"
        +" not implemented yet, add support for real values");
    }
    
    /** Set the leaf distribution type parameters, so that we can properly reconstruct
     * leaf nodes when backfitting.
     *
     * @param ldType The leaf distribution type.
     * @param mEst Laplace correction estimate.
     * @param eviFactor Evidence projection factor.
     */
    public void set_leaf_dist_params(byte ldType, double mEst, double eviFactor) {
        leafDistType = ldType;
        
        switch(new Byte(leafDistType).intValue()) {
            case TDDTInducer.allOrNothing:
            case TDDTInducer.frequencyCounts:
                leafDistParameter = 1.0;
                break;
            case TDDTInducer.laplaceCorrection:
                leafDistParameter = mEst;
                break;
            case TDDTInducer.evidenceProjection:
                leafDistParameter = eviFactor;
                break;
            default:
                System.out.println("RDGCategorizer.set_leaf_dist_params"
                +" ABORT_IF_REACHED");
        }
    }
    
    /** Prints a readable representation of the categorizer to the given stream.
     *
     * @param stream The stream to which this RDGCategorizer is printed.
     * @param dp The display preferences.
     */
    public void display_struct(BufferedWriter stream,
    DisplayPref dp) {
        try{
            if (dp.preference_type() == DisplayPref.ASCIIDisplay)
                stream.write("Rooted Decision Graph Categorizer "+description()+'\n');
            rcGraph.display(hasNodeLosses, get_schema().has_loss_matrix(), stream, dp);
        }catch(IOException e){e.printStackTrace();}
    }
    
    /** Updates usedAttr to include the attributes used in this categorizer.
     *
     * @param usedAttr The array of used attributes. TRUE values indicate the attribute with the index
     * number is used, FALSE otherwise.
     */
    public void set_used_attr(boolean[] usedAttr) {
        rcGraph.set_used_attr(usedAttr);
    }
    
    /** Cateogrize the given instance.
     * @param inst The instance to be categorized.
     * @return The category for this instance.
     */
    public AugCategory categorize(Instance inst) {
        CatDist dist = score(inst);
        AugCategory augCat = dist.best_category();
        dist = null;
        return augCat;
    }
    
    /** Score the given instance.
     * @param inst The instance to be scored.
     * @return The categorizer distribution for this instance.
     */
    public CatDist score(Instance inst) {
        return score_from_node(inst, rcGraph.get_root(true),false);
    }
    
    /** Score the given instance using the current node.
     * @param inst The instance to be scored.
     * @param currentNode The node used for scoring.
     * @param computeNodeLoss TRUE if the node loss values are to be calculated, FALSE otherwise.
     * @return The categorizer distribution for this instance.
     */
    public CatDist score_from_node(Instance inst,
    Node currentNode,
    boolean computeNodeLoss) {
        //   DBGSLOW(OK());
        int correctCat = Globals.UNKNOWN_CATEGORY_VAL;
        
        if (computeNodeLoss) {
            correctCat = inst.label_info().get_nominal_val(inst.get_label());
            if (correctCat == Globals.UNKNOWN_CATEGORY_VAL)
                Error.fatalErr("RDGCategorizer::score_from_node: computeNodeLoss is "
                +"TRUE, yet instance "+inst
                +" has UNKNOWN_CATEGORY_VAL");
        }
        
        NodeCategorizer cat = rcGraph.get_categorizer(currentNode);
        
        return cat.score(inst, computeNodeLoss);
    }
}
