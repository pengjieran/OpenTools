package arithmetic.shared;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

/** Node representation object for internal use in the Graph class.
 * @see Graph
 */
public class Node extends GraphObject{
    /** The Graph class that owns this Node object.
     */
    public Graph owner; //Reference to graph that this node belongs to.
    /** The Edge chain that represents the first edge connecting to this Node.
     */
    public Edge[] first_adj_edge;
    /** The Edge chain that represents the last edge connecting to this Node.
     */
    public Edge[] last_adj_edge;
    /** The lengths of all Edges that have this Node as their source Node.
     */
    public int[] adj_length;
    /** The index number of this Node object.
     */
    public int index_number;
    
    /** Constructor.
     * @param inf The data to be stored in this Node object.
     */
    public Node(Object inf){
        data = inf;
        owner = null;
        id = 0;
        first_adj_edge = new Edge[2];
        last_adj_edge = new Edge[2];
        adj_length = new int[2];
        for(int j=0; j<2; j++){
            first_adj_edge[j]= null;
            last_adj_edge[j] = null;
            adj_length[j] = 0;
        }
    }
    
    /** Appends the given Edge to the adjacency list of this Node.
     * @param e The Edge to be added.
     * @param i Position in the adjancency list.
     * @param chain_e The position in the chain of Edges making up this edge representation.
     */
    public void append_adj_edge(Edge e, int i, int chain_e){
        // append e to adj_list[i]
        // use succ/pred_adj_edge[chain_e] references for chaining of e
        Edge last = last_adj_edge[i];
        e.succ_adj_edge[chain_e] = null;
        if (last == null) { // empty list
            first_adj_edge[i] = e;
            e.pred_adj_edge[chain_e] = null;
        }
        else {
            e.pred_adj_edge[chain_e] = last;
            if (last.source() == last.target())  // loop
                last.succ_adj_edge[chain_e] = e;
            else
                last.succ_adj_edge[(this == last.source())?0:1] = e;
        }
        last_adj_edge[i] = e;
        adj_length[i]++;
    }
    
    /** Inserts an Edge object into the chain of Edges.
     * @param e The Edge to be inserted.
     * @param e1 The Edge in the chain of Edge objects that will be connected to the Edge being
     * inserted.
     * @param i The position in the adjacency list indicating the chain of Edges to be added to.
     * @param chain_e The position in the chain of Edges making up this edge representation.
     * @param dir If dir is 0, the Edge e is inserted after e1 in the chain of Edges, otherwise e
     * is inserted before e1.
     */
    public void insert_adj_edge(Edge e, Edge e1, int i, int chain_e, int dir){
        // insert e after (dir==0) or before (dir!=0) e1 into adj_list[i]
        // use succ/pred_adj_edge[chain_e] references for chaining
        
        if (e1 == null) {
            append_adj_edge(e,i,chain_e);
            return;
        }
        Edge e2;       // successor (dir==0) or predecessor (dir!=0) of e1
        int  chain_e1; // chaining used for e1
        if (e1.source() == e1.target()) // e1 is a self-loop
            chain_e1 = chain_e;
        else
            chain_e1 = (this == e1.source()) ? 0 : 1;
            
            if (dir == 0) {
                e2 = e1.succ_adj_edge[chain_e1];
                e.pred_adj_edge[chain_e] = e1;
                e.succ_adj_edge[chain_e] = e2;
                e1.succ_adj_edge[chain_e1] = e;
                if (e2 == null)
                    last_adj_edge[i] = e;
                else {
                    if (e2.source() == e2.target())
                        e2.pred_adj_edge[chain_e] = e;
                    else
                        e2.pred_adj_edge[(this==e2.source()) ? 0:1] = e;
                }
            }
            else {
                e2 = e1.pred_adj_edge[chain_e1];
                e.succ_adj_edge[chain_e] = e1;
                e.pred_adj_edge[chain_e] = e2;
                e1.pred_adj_edge[chain_e1] = e;
                if (e2 == null)
                    first_adj_edge[i] = e;
                else {
                    if (e2.source() == e2.target()) //loop
                        e2.succ_adj_edge[chain_e] = e;
                    else
                        e2.succ_adj_edge[(this==e2.source()) ? 0:1] = e;
                }
            }
            adj_length[i]++;
    }
    
    /** Deletes the specified Edge.
     * @param e The Edge to be deleted.
     * @param i The Edge chain containing the Edge to be deleted.
     * @param chain_e The position in the chain of Edges making up this edge representation.
     */
    public void del_adj_edge(Edge e, int i, int chain_e){
        // remove e from adj_list[i]
        // with respect to succ/pred_adj_edge[chain_e] references
        
        Edge e_succ = e.succ_adj_edge[chain_e];
        Edge e_pred = e.pred_adj_edge[chain_e];
        
        if (e_succ != null)
            if (e_succ.source() == e_succ.target()) // loop
                e_succ.pred_adj_edge[chain_e]= e_pred;
            else
                e_succ.pred_adj_edge[(this==e_succ.source()) ? 0:1] = e_pred;
        else
            last_adj_edge[i] = e_pred;
        
        if (e_pred != null)
            if (e_pred.source() == e_pred.target()) // loop
                e_pred.succ_adj_edge[chain_e] = e_succ;
            else
                e_pred.succ_adj_edge[(this==e_pred.source()) ? 0:1] = e_succ;
        else
            first_adj_edge[i] = e_succ;
        
        adj_length[i]--;
    }
    
    /** Returns the data stored in this Node object.
     * @return The data stored in this Node object.
     */
    public Object getData(){return data;}
    
    /** Returns the Graph that contains this Node.
     * @return The Graph that contains this Node.
     */
    public Graph graph_of(){return owner;}
    
    /** Returns the index number of this Node object.
     * @return The index number of this Node object.
     */
    public int index()    { return id & 0x7fffffff;  }
    
    /** Returns the number of Edges adjacent to Node.
     * @return The number of Edges in the adjacency list of this Node.
     */
    public int outdeg(){return adj_length[0];}
    
    /** Returns the number of Edges leading into this Node.
     * @return The number of Edges leading into this Node.
     */
    public int indeg(){return adj_length[1];}
    
    /** Returns the number of Edges connected to this Node.
     * @return The number of Edges connected to this Node.
     */
    public int degree(){return adj_length[0] + adj_length[1];}
    
    /** Copies the given Node object into this Node object.
     * @param original The Node object to be copied.
     */
    public void copy(Node original){
        owner = original.owner;
        if (first_adj_edge == null) first_adj_edge = new Edge[2];
        if (last_adj_edge == null) last_adj_edge = new Edge[2];
        if (adj_length == null) adj_length = new int[2];
        for(int j = 0; j < 2; j++){
            adj_length[j] = original.adj_length[j];
            first_adj_edge[j] = original.first_adj_edge[j];
            last_adj_edge[j] = original.last_adj_edge[j];
        }
    }
    
    /** Returns the first adjacent Edge at the indicated position in the adjacency list.
     * @param i The position of this Edge.
     * @return The first adjacent Edge at the indicated position in the adjacency list.
     */
    public Edge First_Adj_Edge(int i) {return first_adj_edge[i];}
    
    /** Returns the last adjacent Edge at the indicated position in the adjacency list.
     * @param i The position of this Edge.
     * @return The last adjacent Edge at the indicated position in the adjacency list.
     */
    public Edge Last_Adj_Edge(int i) { return last_adj_edge[i];  }
    
    /** Return a list of all Edges starting from this Node.
     * @return A list of all Edges starting from this Node.
     */
    public LinkedList adj_edges(){
        LinkedList result = new LinkedList();
        for(Edge e = first_adj_edge[0]; e!=null; e = e.Succ_Adj_Edge(this)) result.addLast(e);
        return result;
    }
    
    /** Return a list of all Edges starting from this Node if the owning Graph is
     * directed, otherwise an empty list is returned.
     * @return A list of all Edges starting from this Node.
     */
    public LinkedList out_edges(){
        LinkedList result = new LinkedList();
        if(owner.is_directed())
            for(Edge e = first_adj_edge[0]; e != null; e = e.succ_adj_edge[0]) result.addLast(e);
        return result;
    }
    
    /** Returns a list of all Edges ending at this Node.
     * @return A list of all Edges ending at this Node.
     */
    public LinkedList in_edges(){
        LinkedList result = new LinkedList();
        for(Edge e = first_adj_edge[1]; e != null; e = e.succ_adj_edge[1]) result.addLast(e);
        return result;
    }
    
    
    /** Returns a list of the Node targets of all Edges whose source is this Node.
     * @return A list of the Nodes
     */
    public LinkedList adj_nodes(){
        LinkedList result = new LinkedList();
        for(Edge e = first_adj_edge[0]; e!=null; e = e.Succ_Adj_Edge(this)) result.addLast(e.opposite(this,e));
        return result;
    }
    
    /** Returns the first Edge in the adjacency list of this Node.
     * @return The first Edge in the adjacency list of this Node.
     */
    public Edge first_adj_edge(){return first_adj_edge[0];}
    
    /** Returns the last Edge in the adjacency list of this Node.
     * @return The last Edge in the adjacency list of this Node.
     */
    public Edge last_adj_edge(){return last_adj_edge[0];}
    
    /** Returns the first Edge with this Node as its target.
     * @return The first Edge with this Node as its target.
     */
    public Edge first_in_edge(){return first_adj_edge[1];}
    
    /** Returns the last Edge with this Node as its target.
     * @return The last Edge with this Node as its target.
     */
    public Edge last_in_edge(){return last_adj_edge[1];}
    
    /** Displays the data stored in this Node object.
     * @param out The Writer to which the data will be displayed.
     * @throws IOException if the Writer experiences an Exception.
     */
    void print_node_entry(Writer out) throws IOException
    { out.write("(" + data.toString() +")"); }
    
    /** Displays this Node object.
     * @param out The Writer to which this Node object will be displayed.
     * @throws IOException if the Writer experiences an Exception.
     */
    public void print_node(Writer out) throws IOException {
        out.write("[" + this + "]");
        print_node_entry(out);
    }
    
    /** Converts this Node object to a String representation.
     * @return A String representation of this Node object.
     */
    public String toString() {
        return Integer.toString(index_number);
    }
    
    /** Sets the index number for this Node object.
     * @param new_number The new index number.
     */
    public void setIndexNumber(int new_number) {
        index_number = new_number;
    }
    
}// End of Node class