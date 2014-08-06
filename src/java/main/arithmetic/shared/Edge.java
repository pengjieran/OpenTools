package arithmetic.shared;
import java.io.IOException;
import java.io.Writer;

/** The Edge class stores information for the edges in the Graph class. This
 * is part of the LEDA package Graph design. The comments in Graph also apply
 * to the edge class.
 * @author James Louis	5/24/2001	Java Implementation.
 */
public class Edge extends GraphObject{
//based on LEDA

    /** Succesive adjacent Edges. These are edges that are added after this edge
     * but are originating from the same origin node.
     */
   public Edge[] succ_adj_edge;

   /** Preceeding adjacent Edges. These are edges that are present before this edge
    * is created and have the same origin node.
    */
   public Edge[] pred_adj_edge;

   /** Terminal Nodes for this Edge. For directed Edges, term[0] is the source
    * Node and term[1] is the target Node.
    */
   public Node[] term;

   /** The Edge that is reverse in direction to this Edge object.
    */
   public Edge rev;			//edge that travels in reverse direction

   /** TRUE if this Edge object is hidden, otherwise FALSE.
    */
   public boolean hidden;

   /** Constructor.
    * @param terminal1 The origin terminal node.
    * @param terminal2 The destination terminal node.
    * @param info The Object containing data to be stored in thid Edge instance.
    */
   public Edge(Node terminal1, Node terminal2, Object info)
   {
      succ_adj_edge = new Edge[2];
      succ_adj_edge[0] = null;
      succ_adj_edge[1] = null;
      pred_adj_edge = new Edge[2];
      pred_adj_edge[0] = null;
      pred_adj_edge[1] = null;
      id = 0;
      term = new Node[2];
      term[0] = terminal1;
      term[1] = terminal2;
      rev = null;
      data = info;
      hidden = false;
   }

   /** Returns the source Node for this Edge.
    * @return The source Node for this Edge.
    */
   public Node source()
   {
      return term[0];
   }

   /** Returns the target Node for this Edge.
    * @return The destination Node for this Edge.
    */
   public Node target()
   {
      return term[1];
   }

   /** Returns the Node at the opposite end of the specified Edge connected to
    * that Node.
    * @param vertex The Node instance at the given end of the Edge.
    * @param edge The Edge instance for which the opposite edn is requested.
    * @return The Node at the opposite end of the specified Edge instance.
    */
   public Node opposite(Node vertex,Edge edge)
   {
      return (vertex == edge.source()) ? edge.target() : edge.source();
   }

   /** Returns the Graph object which this Edge is a part of.
    * @return The Graph instance this Edge is a part of.
    */
   public Graph graph_of()
   {
      return term[0].graph_of();
   }

   /** Copies the specified Edge object into this Edge object.
    * @param original The Edge instance to be copied.
    */
   public void copy(Edge original)
   {
      if (succ_adj_edge == null) succ_adj_edge = new Edge[2];
      if (pred_adj_edge == null) pred_adj_edge = new Edge[2];
      if (term == null) term = new Node[2];
      for(int j = 0; j < 2; j++)
      {
         succ_adj_edge[j] = original.succ_adj_edge[j];
         pred_adj_edge[j] = original.pred_adj_edge[j];
         term[j] = original.term[j];
      }
      id = original.id;
      data = original.data;
      rev = null;
      hidden = original.hidden;
   }

   /** Returns the specified successive adjacent Edge.
    * @param index The number of the specified Edge. Must be 0 or 1.
    * @return The specified successive adjacent Edge.
    */
   public Edge Succ_Adj_Edge(int index) {
       return (this != null) ? succ_adj_edge[index] : null;
   }

   /** Returns the specified preceeding adjacent Edge.
    * @param index The number of the specified Edge. Must be 0 or 1.
    * @return The specified preceeding adjacent Edge.
    */
   public Edge Pred_Adj_Edge(int index) {
       return (this != null) ? pred_adj_edge[index] : null;
   }

   /** Returns the successive adjacent Edge connected to the specified Node.
    * Searches through all succesive Edges until the appropriate one is found.
    * Assumes the connecting Edge exists.
    * @param vertex The specified Node for which a connecting Edge is requested.
    * @return The Edge connecting to the specified Node.
    */
   public Edge Succ_Adj_Edge(Node vertex)
   {
      return (this != null) ? Succ_Adj_Edge((vertex==source()) ? 0:1) : null;
   }

   /** Returns the preceeding adjacent Edge connected to the specified Node.
    * Searches through all preceeding Edges until the appropriate one is found.
    * Assumes the connecting Edge exists.
    * @param vertex The specified Node for which a connecting Edge is requested.
    * @return The Edge connecting to the specified Node.
    */
   public Edge Pred_Adj_Edge(Node vertex)
   {
      return (this != null) ? Pred_Adj_Edge((vertex==source()) ? 0:1) : null;
   }

   /** Returns the first successive adjacent Edge.
    * @return The immediately succesive Edge.
    */
   public Edge adj_succ()
   {
      return succ_adj_edge[0];
   }

   /** Returns the first preceeding adjacent Edge.
    * @return The immediately preceeding Edge.
    */
   public Edge adj_pred()
   {
      return pred_adj_edge[0];
   }

   /** Returns the first successive Edge instance from the source Node for this Edge
    * instance.
    * @return The Edge instance from the source Node for this Edge instance.
    */
   public Edge cyclic_adj_succ()
   {
      if (succ_adj_edge[0] != null){return (Edge)term[0].first_adj_edge();}
      else {return null;}
   }

   /** Returns the first preceeding Edge instance from the source Node for this Edge
    * instance.
    * @return The Edge instance from the source Node for this Edge instance.
    */
   public Edge cyclic_adj_pred()
   {
      if (pred_adj_edge[0] != null){return (Edge)term[0].last_adj_edge();}
      else {return null;}
   }

   /** Returns the first Edge object connected to the source Node.
    * @return The Edge object immediately connecting the source Node to the Edges connecting
    * to the next Node.
    */
   public Edge cyclic_in_succ()
   {
      if (succ_adj_edge[1] != null){return (Edge)term[1].first_in_edge();}
      else {return null;}
   }

   /** Returns the last Edge object before reaching a destination Node.
    * @return The last Edge object before the edge terminates at a Node.
    */
   public Edge cyclic_in_pred()
   {
      if (pred_adj_edge[1] != null){return (Edge)term[1].last_in_edge();}
      else {return null;}
   }

   /** Returns the succeding adjacent Edge of the specified Edge object.
    * @param e The Edge for which the adjacent Edge is requested.
    * @return The adjacent Edge of the specified Edge.
    */
   public Edge in_succ(Edge e)
   {
      return succ_adj_edge[1];
   }

   /** Returns the adjacent preceeding Edge of the specified Edge object.
    * @param e The Edge for which the adjacent Edge is requested.
    * @return The adjacent Edge of the specified Edge.
    */
   public Edge in_pred(Edge e)
   {
      return pred_adj_edge[1];
   }

   /** Displays the object currently stored in this Edge object.
    * @param out The Writer to which the Edge information will be displayed.
    * @throws IOException if the Writer experiences an IOException during use.
    */
   public void print_edge_entry(Writer out) throws IOException
   {
      out.write("(" + data.toString() +")");
   }

   /** Displays this Edge object to the given Writer.
    * @param out The Writer to which the Edge information will be displayed.
    * @throws IOException if the Writer experiences an IOException during use.
    */
   public void print_edge(Writer out) throws IOException
   {
// if (super() != 0)
//     super()->print_edge(edge(graph::inf(e)),o);
//  else
//     {
      out.write("[" + source() + "]");
      out.write(((source().owner.is_undirected()) ? "==" : "--"));
      print_edge_entry(out);
      out.write(((source().owner.is_undirected()) ? "==" : "-->"));
      out.write("[" + target() + "]");
//      }
   }

}