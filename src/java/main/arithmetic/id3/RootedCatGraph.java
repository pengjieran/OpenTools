package arithmetic.id3;
import java.io.IOException;
import arithmetic.shared.Error;
import java.io.Writer;

import arithmetic.shared.AugCategory;
import arithmetic.shared.DisplayPref;
import arithmetic.shared.Edge;
import arithmetic.shared.Globals;
import arithmetic.shared.MLJ;
import arithmetic.shared.Node;

/** RootedCatGraph instances have a specific root node. All nodes in RootedCatGraphs
 * should be reachable from the root.
 *
 * @author Richard Long 8/27/93 Initial revision (.c) <BR> 8/04/93 Initial revision (.h)
 *
 * @author James Louis Java implementation.
 */
public class RootedCatGraph extends CatGraph {
    
    private Node root;
    
    /** Constructor. Initializes root to NULL.
     * @param isGraphSparse TRUE if the graph is sparse.
     */
    public RootedCatGraph(boolean isGraphSparse) {
        super(isGraphSparse);
        root = null;
    }
    
    /** Constructor.
     * @param grph The graph to be assigned to this RootedCatGraph.
     * @param isGraphSparse TRUE if the graph is sparse.
     */
    public RootedCatGraph(CGraph grph, boolean isGraphSparse) {
        super(grph, isGraphSparse);
        root = null;
    }
    
    /** The number of non-trivial nodes in the CatGraph. Trivial nodes are those
     * that both have no instances, and have unknown edges leading to them.
     *
     * @return The number of non-trivial nodes.
     */
    public int num_nontrivial_nodes(){return num_nontrivial_nodes(null);}
    
    /** The number of non-trivial nodes in the CatGraph. Trivial nodes are those
     * that both have no instances, and have unknown edges leading to them.
     *
     * @param fromRoot The node to use as the root in searching for nodes.
     * @return The number of non-trivial nodes.
     */
    public int num_nontrivial_nodes(Node fromRoot) {
        if (fromRoot == null)
            fromRoot = get_root(true);
        
        int numNodes = 1;
        
        
        //      Edge iterEdge;
        //      forall_adj_edges(iterEdge, fromRoot) {
        for(Edge iterEdge=fromRoot.First_Adj_Edge(0); iterEdge != null ;iterEdge = iterEdge.Succ_Adj_Edge(fromRoot)){
            
            if (!trivial_edge(iterEdge)) {
                Node childNode = iterEdge.target();
                numNodes += num_nontrivial_nodes(childNode);
            }
        }
        //      ASSERT(numNodes <= num_nodes());
        return numNodes;
    }
    
    /** The number of non-trivial leaves in the CatGraph. Trivial nodes are those
     * that both have no instances, and have unknown edges leading to them.
     *
     * @return The number of non-trivial leaves.
     */
    public int num_nontrivial_leaves(){return num_nontrivial_leaves(null);}
    
    /** The number of non-trivial leaves in the CatGraph. Trivial nodes are those
     * that both have no instances, and have unknown edges leading to them.
     *
     * @param fromRoot The node to use as root in searching for leaves.
     * @return The number of non-trivial leaves.
     */
    public int num_nontrivial_leaves(Node fromRoot) {
        if (fromRoot == null)
            fromRoot = get_root(true);
        
        if (num_children(fromRoot) == 0)
            return 1;
        else {
            int numLeaves = 0;
            
            for (Edge iterEdge = fromRoot.first_adj_edge(); iterEdge != null;
            iterEdge = iterEdge.adj_succ()) {
                if (!trivial_edge(iterEdge)) {
                    Node childNode = iterEdge.target();
                    numLeaves += num_nontrivial_leaves(childNode);
                }
            }
            //         DBGSLOW(ASSERT(numLeaves <= num_leaves()));
            return numLeaves;
        }
    }
    
    /** Check if an edge is trivial. A trivial edge has an unknown label and leads to a
     * node with no instances.
     *
     * @param e The edge to be checked.
     * @return TRUE if the edge is trivial, FALSE otherwise.
     */
    public boolean trivial_edge(Edge e) {
        return (((AugCategory)cGraph.inf(e)).num() == Globals.UNKNOWN_CATEGORY_VAL &&
        MLJ.approx_equal(
        (float)(get_categorizer(e.target()).total_weight()),
        (float)0.0)
        );
    }

    /** Sets the root of the RootedCatGraph.
     *
     * @param node The new root node.
     */
    public void set_root(Node node) {
        //DBG(if (node != null)
        //       check_node_in_graph(node, true));
        root = node;
    }
    
    
    /** If the root has been set, returns the root.
     * @return The root node of this RootedCatGraph.
     */
    public Node get_root() {
        return get_root(false);
    }
    
    /** If the root has been set, returns the root. If abortOnNULL is FALSE returns NULL
     * if root has not been set. Otherwise aborts when root is not set.
     *
     * @param abortOnNoRoot TRUE if aborting should occur when there is no root, FALSE if the method returns
     * null if there is no root node set.
     * @return The root node or null if the root is not set.
     */
    public Node get_root(boolean abortOnNoRoot) {
        if (root == null && abortOnNoRoot)
            Error.fatalErr("RootedCatGraph::get_root: Root has not been set");
        return root;
    }
    
    /** Sets the attributes used.
     * @param usedAttr An array indicating used attributes. Used attributes are indicated with TRUE
     * values in their index numbers. Unused attributes are indicated with FALSE values.
     */
    public void set_used_attr(boolean[] usedAttr)
    { cGraph.set_used_attr(usedAttr); }
    
    /** Displays the root, as well as CatGraph.display().
     * @param hasNodeLosses TRUE if node loss values have been set.
     * @param hasLossMatrix TRUE if loss matrix has been set.
     * @param stream The Writer to be displayed to.
     * @param dp The display preferences.
     */
    public void display(boolean hasNodeLosses, boolean hasLossMatrix,
    Writer stream, DisplayPref dp) {
        try{
            if (dp.preference_type() == DisplayPref.ASCIIDisplay)
                if (root != null)
                    stream.write("Root: " + get_categorizer(root).description()
                    + "\n");
                else
                    stream.write("Warning, no root defined for graph" + "\n");
            super.display(hasNodeLosses, hasLossMatrix, stream, dp);
        }catch(IOException e){e.printStackTrace();}
    }
    
    
}
