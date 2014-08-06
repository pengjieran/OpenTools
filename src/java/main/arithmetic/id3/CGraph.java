package arithmetic.id3;
import java.util.ListIterator;

import arithmetic.shared.AugCategory;
import arithmetic.shared.Edge;
import arithmetic.shared.Error;
import arithmetic.shared.Globals;
import arithmetic.shared.Graph;
import arithmetic.shared.LogOptions;
import arithmetic.shared.MLJArray;
import arithmetic.shared.Node;

/** CGraph is derived from LEDA's CGRAPH<NodeInfo*, AugCategory*> This allows
 * us to add functions.
 * @author James Louis	12/06/2000	Ported to Java.
 * @author Eric Bauer	9/16/96	Changed OK(), num_attr() for multi-threshold support
 * @author Chia-Hsin Li	11/21/94	Fixed set_categorizer which leaked.
 * @author Richard Long	1/28/94	Initial revision (.h,.c)
 */
public class CGraph extends Graph
{
    /** Information on the nodes stored in this graph.
     */
   private NodeInfo prototype;
   /** The default starting level for this graph.
    */
   static public int DEFAULT_LEVEL = -1;

   /** Constructor.
    */
   public CGraph()
   {
      prototype = new NodeInfo(0);
   }

   /** Accesses the information on the specified Node.
    * @param node	The specified node.
    * @return The information on the specified node.
    */
   public NodeInfo node_info(Node node)
   {
      return(NodeInfo)entry(node);
   }

   /** Sets which attributes have been used.
    * @param usedAttr	The set of used attributes. The array should be the same
    * length as the number of attributes. Each element
    * corresponds to an attribute. A TRUE value indicates
    * that attribute was used; a FALSE value indicates it
    * was not.
    */
   public void set_used_attr(boolean[] usedAttr)
   {
      for(ListIterator nodes = v_list.listIterator();nodes.hasNext();)
         ((NodeInfo)inf((Node)nodes.next())).get_categorizer().set_used_attr(usedAttr);
   }

   /** Returns the number of leaves in this graph. The complexity of num_leaves()
    * takes O(N), where N is the number of nodes in the graph.
    * @return The number of leaves.
    */
   public int num_leaves()
   {
      int numLeaves = 0;
      for(ListIterator nodes = v_list.listIterator();nodes.hasNext();)
         if (((Node)nodes.next()).outdeg() == 0)
            numLeaves++;
      return numLeaves;
   }

   /** Returns the number of attributes.
    * @param log		The logging options.
    * @param maxAttr	The upper bound on the number of attributes.
    * @return The number of attributes.
    */
   public int num_attr(LogOptions log, int maxAttr)
   {
      boolean[] usedAttr = new boolean[maxAttr];
      for(int z = 0 ; z < usedAttr.length ; z++)
         usedAttr[z] = false;
      set_used_attr(usedAttr);
      int numAttr = 0;
      for(int i = 0 ; i < usedAttr.length ; i++)
         if (usedAttr[i] == true)
            numAttr++;
      numAttr = MLJArray.num_element(true,usedAttr);
      return numAttr;
   }

   /** Returns information on the nodes stored in this Cgraph.
    * @return The information on the nodes stored in this Cgraph.
    */
   public NodeInfo get_prototype()
   {
      return prototype;
   }

   /** Returns the child of the given Node, following the Edge with the given
    * label. Aborts if no such Edge exists.
    * @param parent	The parent Node to this Edge.
    * @param edgeLabel	The Edge to the child Node.
    * @return The child of the given Node and Edge.
    */
   protected Node get_child(Node parent, 
          AugCategory edgeLabel)
   {
      for(Edge edgePtr = parent.First_Adj_Edge (0) ; edgePtr != null ; edgePtr = edgePtr.Succ_Adj_Edge (parent))
      {
         if (((AugCategory)inf(edgePtr)).num() == edgeLabel.num())
         {
            return edgePtr.target();
         }
      }
      for(Edge edgePtr = parent.First_Adj_Edge (0) ; edgePtr != null ; edgePtr = edgePtr.Succ_Adj_Edge (parent))
         if (((AugCategory) inf (edgePtr)) .num () == Globals.UNKNOWN_CATEGORY_VAL)
            return edgePtr.target();
      Error.fatalErr("CGraph::get_child: Node does not have an edge labelled \'" 
             +edgeLabel.description () + "\' (" +edgeLabel.num () + ')' 
             + " nor does it have an UNKNOWN edge");
      return null;
   }

   /** Returns the categories possible for an Edge.
    * @param edge	The Edge for which information is requested.
    * @return The information on what categories are possible for the specifed
    * Edge.
    */
   public AugCategory edge_info(Edge edge)
   {
      return(AugCategory) entry(edge);
   }

   /** Assigns a categorizer with the new specified Node to the old specified
    * Node.
    * @param oldNode	The old Node specified.
    * @param newNode	The new Node specified.
    */
   public void assign_categorizer(Node oldNode, Node newNode)
   {
      ((NodeInfo)inf(oldNode)).assign_categorizer((NodeInfo)inf(newNode));
      ((NodeInfo)inf(oldNode)).get_categorizer().set_graph_and_node(this, oldNode);
      assign(oldNode, inf(oldNode));
   }

   /** Creates a new Node and stored the specified NodeInfo in the new Node.
    * @param nodeInfo	The node information to be stored in the new Node.
    * @return The new Node created.
    */
   public Node new_node(NodeInfo nodeInfo)
   {
      Node node = super.new_node(nodeInfo);
      nodeInfo.get_categorizer() .set_graph_and_node(this, node);
      return node;
   }
}
