package arithmetic.id3;
import java.util.ListIterator;
import arithmetic.shared.Error;

import arithmetic.shared.AugCategory;
import arithmetic.shared.CatDist;
import arithmetic.shared.Categorizer;
import arithmetic.shared.DoubleRef;
import arithmetic.shared.Edge;
import arithmetic.shared.Globals;
import arithmetic.shared.Instance;
import arithmetic.shared.InstanceList;
import arithmetic.shared.Node;
import arithmetic.shared.Schema;
/** An abstract base class categorizer for categorizers that may sit in nodes of
 * decision trees, graphs, etc. Categorizers of this sort generally categorize by
 * making a decision about the instance, and then asking one or more other
 * categorizers in the graph to categorize. The recursion ends when a
 * NodeCategorizer can decide on the category (or distribution, in the case of
 * scoring) without consulting other categorizers.
 *
 * @author James Louis 4/16/2002 Java implementation.
 * @author Clay Kunz 08/08/97 Initial revision (.h,.c)
 */
abstract public class NodeCategorizer extends Categorizer{
    
    //	public NodeCategorizer(){}
    
    // Member data
    private NodeLoss lossInfo;
    private Node nodePtr;
    private CGraph cGraph;
    private boolean smoothDistribution;
    private double smoothFactor;
    //   private DBG_DECLARE(boolean checkGraph;)
    
    
    /** Prints an empty string to System.out.
     */
    public void stop(){
        System.out.print("");
    }
    
    
    
    
    /** Constructor.
     * @param noCat The category for this NodeCategorizer.
     * @param dscr Description of this NodeCategorizer.
     * @param schema Schema for the data this categorizer classifies.
     */
    public NodeCategorizer(int noCat,  String dscr,  Schema schema) {
        super(noCat, dscr, schema);
        nodePtr = null;
        cGraph = null;
        smoothDistribution = false;
        smoothFactor = 0.0;
        lossInfo = new NodeLoss();
        
        //   DBG(checkGraph = true);
        reset_node_loss();
    }
    //used in NodeInfo.toString()
    /** Creates a String representation of this NodeCategorizer.
     * @return A String representation of this NodeCategorizer.
     */
    public String toString() {
        return description();
    }
    
    /** Clears the loss information.
     */
    public void reset_node_loss() {
        lossInfo.totalWeight = 0.0;
        lossInfo.totalLoss = 0.0;
        lossInfo.totalLossSquared = 0.0;
    }
    
    /** Returns TRUE if a graph has been set for this NodeCategorizer, FALSE otherwise.
     * @return TRUE if a graph has been set for this NodeCategorizer, FALSE otherwise.
     */
    public boolean in_graph()  { return (cGraph != null); }
    
    /** Splits the instance list according to the value returned by branch() for each
     * instance.
     * @param il The InstanceList to be split.
     * @return A array of partitions of the given InstanceList.
     */
    public  InstanceList[] split_instance_list( InstanceList il)
    
    {
        //   DBGSLOW(if (!get_schema().equal(il.get_schema()))
        //	   Error.err("NodeCategorizer::split_instance_list: my schema " +
        //			get_schema() + " is not the same as the schema of the instance list to split: " +
        //			il.get_schema() + "-->fatal_error");
        
        // Note num_cat() + 1, and NOT num_cat() because the count starts
        //   from UNKNOWN and not from FIRST.
        InstanceList[] ila =new InstanceList[num_categories() + 1];
        //(Globals.UNKNOWN_CATEGORY_VAL, num_categories() + 1);
        //   for (int i = ila.low(); i <= ila->high(); i++)
        for (int i = 0; i < ila.length; i++)
            ila[i] = new InstanceList(il.get_schema());
        for (ListIterator pix = il.instance_list().listIterator(); pix.hasNext();) {
            Instance instance = (Instance)pix.next();
            ila[branch(instance).num()].add_instance(instance);
            //ila[(int)(branch(instance))].add_instance(instance);
        }
        
        return ila;
    }
    
    /** Traverses the graph of nodes from this NodeCategorizer to determine the category
     * the given instance should be predicted as.
     * @param inst The instance for which a prediction is requested.
     * @return The category for the given instance.
     */
    abstract public AugCategory branch(Instance inst);
    
    
    /** Categorize an instance.
     * @param instance The instance to be categorized.
     * @return The category of the given instance.
     */
    public AugCategory categorize(Instance instance) {
        if (!in_graph())
            Error.fatalErr("NodeCategorizer::categorize: can only categorize from "
            +"inside a graph");
        return get_child_categorizer(instance).categorize(instance);
    }
    
    /** Returns TRUE if scoring supported by this node categorizer. TRUE is always
     * returned.
     * @return TRUE.
     */
    public  boolean supports_scoring()  { return true; }
    /** Score an instance. Scoring function contains the option of carrying the loss
     * information through the graph.
     * @param inst The instance to be scored.
     * @return The score of the given instance.
     */
    public  CatDist score( Instance inst){ return score(inst, false); }
    /** Score an instance. Scoring function contains the option of carrying the loss
     * information through the graph.
     * @param inst The instance to be scored.
     * @param addLoss TRUE if the loss information is to be carried through the graph, FALSE
     * otherwise.
     * @return The score of the given instance.
     */
    public  CatDist score( Instance inst, boolean addLoss) {
        if (!in_graph())
            Error.err("NodeCategorizer::score: can only score from inside a graph-->fatal_error");
        CatDist dist = get_child_categorizer(inst).score(inst, addLoss);
        // smoothing is not yet supported
        //      if (smoothDistribution) {
        //         Error.err("NodeCategorizer::score: smoothing is not yet supported-->fatal_error");
        //         dist.smooth_toward(get_distr(), smoothFactor);
        //      }
        if (addLoss)
            add_instance_loss(inst, dist);
        return dist;
    }
    
    /** Updates the loss information for this node to reflect the node's performance on
     * the given instance, and the given prediction.
     *
     * @param instance The instance to which given prediction applies.
     * @param pred The prediction of category distributions.
     */
    public  void add_instance_loss( Instance instance,
    CatDist pred) {
        int correctCat = Globals.UNKNOWN_CATEGORY_VAL;
        
        AugCategory predictedCat = pred.best_category();
        correctCat = instance.label_info().get_nominal_val(instance.get_label());
        if (correctCat == Globals.UNKNOWN_CATEGORY_VAL)
            Error.err("NodeCategorizer::add_instance_loss: instance " + instance + " has UNKNOWN_CATEGORY_VAL-->fatal_error");
        double loss;
        if (get_schema().has_loss_matrix())
            loss = get_schema().get_loss_matrix()[correctCat][predictedCat.num()];
        else if (predictedCat.num() == correctCat)
            loss = 0;
        else
            loss = 1;
        
        update_loss(instance.get_weight(), loss);
    }
    
    /** Returns the child categorizer of this node that is found by following the edge
     * with the given label.
     *
     * @param branch The category of the edge for which the child categorizer is requested.
     * @return The child categorizer.
     */
    public  NodeCategorizer get_child_categorizer(AugCategory branch) {
        Node childNode = get_graph().get_child(get_node(), branch);
        return ((NodeInfo)get_graph().entry(childNode)).get_categorizer();
    }
    
    /** Retrieves the appropriate categorizer one level down in the graph, obtained by
     * following the edge appropriate for the instance provided.
     *
     * @param inst The instance provided for determining which edge to traverse.
     * @return The child categorizer of the appropriate edge.
     */
    public  NodeCategorizer get_child_categorizer(Instance inst) {
        return get_child_categorizer(branch(inst));
    }
    
    /** Updates the loss information with the given values.
     * @param weight The new weight value.
     * @param loss The new loss value.
     */
    protected void update_loss(double weight, double loss) { lossInfo.update(weight, loss); }
    
    /** Returns the graph for this NodeCategorizer.
     * @return The graph for this NodeCategorizer.
     */
    protected  CGraph get_graph() {
        if (cGraph == null)
            Error.err("NodeCategorizer::get_graph: the graph is null-->fatal_error");
        return cGraph;
    }
    
    /** Returns the node for this NodeCategorizer.
     * @return The node for this NodeCategorizer.
     */
    protected Node get_node() {
        if (nodePtr == null)
            Error.err("NodeCategorizer::get_node: the node is null-->fatal_error");
        return nodePtr;
    }
    
    /** Recomputes the distribution of the categorizer according to the given instance
     * list, splits it, and redistributes the split lists among the child categorizers.
     * This process is used to backfit an instance list to a graph structure.
     *
     * @param il The instance list used for recomputation.
     * @param pruningFactor The amount of pruning being done.
     * @param pessimisticErrors The pessimistic Error value.
     * @param ldType Leaf distribution type.
     * @param leafDistParameter The leaf distribution.
     * @param parentWeightDist The weight distribution of the parent categorizer.
     * @param saveOriginalDistr TRUE if the original distribution should be preserved, FALSE otherwise.
     */
    public  void distribute_instances( InstanceList il,
    double pruningFactor,
    DoubleRef pessimisticErrors,
    int ldType,  			//TDDTInducer.LeafDistType
    double leafDistParameter,
    double[] parentWeightDist,
    boolean saveOriginalDistr) {
        CGraph myGraph = get_graph();
        Node myNode = get_node();
        if (myNode.outdeg() <= 0)
            Error.err("NodeCategorizer::distribute_instances: " +
            "this node has no children -- leaf categorizers " +
            "should be held inside a LeafCategorizer-->fatal_error");
        
        if (saveOriginalDistr && has_distr())
            set_original_distr(get_distr());
        build_distr(il);
        
        double[] myWeightDistribution = null;
        double[] augmentedWeightDist = null;
        
        if (il.no_weight())
            myWeightDistribution = parentWeightDist;
        else {
            double[] distrNoUnknown = get_distr();
            augmentedWeightDist = new double[distrNoUnknown.length + 1];
            //	 new Array<double>(UNKNOWN_CATEGORY_VAL, distrNoUnknown.size() + 1, 0);
            for (int i = 0; i < augmentedWeightDist.length; i++)
                augmentedWeightDist[i] = distrNoUnknown[i];
            myWeightDistribution = augmentedWeightDist;
        }
        
        InstanceList[] instLists = split_instance_list(il);
        //   forall_adj_edges(edgePtr, myNode) {
        for(Edge edgePtr = myNode.First_Adj_Edge(0);
        edgePtr != null;
        edgePtr = edgePtr.Succ_Adj_Edge(myNode)){
            int num = ((AugCategory)myGraph.inf(edgePtr)).num();
            Node child = edgePtr.target();
            //      ASSERT((instLists)[num]);
            NodeCategorizer childCat = ((NodeInfo)myGraph.inf(child)).get_categorizer();
            childCat.distribute_instances(instLists[num], pruningFactor,
            pessimisticErrors, ldType,
            leafDistParameter, myWeightDistribution,
            saveOriginalDistr);
            instLists[num] = null;
        }
        
        augmentedWeightDist = null;
        
        //   DBG(
        //       // Make sure we don't have any leftover instances or this is a bug
        //       for (Category cat = instLists->low(); cat <= instLists->high(); cat++)
        //          if ((instLists)[cat] != null)
        //	     // Maybe we don't have unknown edges
        //	     if ((instLists)[cat]->no_weight()) {
        //	        delete (instLists)[cat];
        //	        (instLists)[cat] = null;
        //	     } else
        //	        Error.err("NodeCategorizer::distribute_inst: Missed InstanceList " + cat + "-->fatal_error");
        //       );
        instLists = null;
    }
    
    /** Install the graph and node into the object.
     * @param aGraph The graph of NodeCategorizers.
     * @param aNode The node for this NodeCategorizer.
     */
    public void set_graph_and_node(CGraph aGraph, Node aNode) {
        if (aGraph == null || aNode == null)
            Error.err("NodeCategorizer::set_graph_and_node: neither the graph nor the node may be null-->fatal_error");
        if (cGraph != null || nodePtr != null)
            Error.err("NodeCategorizer::set_graph_and_node: the node and graph have already been set-->fatal_error");
        
        cGraph = aGraph;
        nodePtr = aNode;
        //   DBG(OK(0));
    }
    
    /** Returns the loss information.
     * @return The loss information.
     */
    public NodeLoss get_loss() { return lossInfo; }
}