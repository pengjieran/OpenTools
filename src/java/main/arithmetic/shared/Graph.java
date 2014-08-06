package arithmetic.shared;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

/** An instance G of the data type graph consists of a list V  of nodes
 * and a list E  of edges (node and edge are item types).
 * Distinct graphs have disjoint node and edge lists.
 * The value of a variable of type node is either the node of some graph, or the
 * special value nil (which is distinct from all nodes), or is undefined
 * (before the first assignment to the variable). A corresponding statement is
 * true for the variables of type edge.
 * <P>
 * A graph with empty node list is called  empty.
 * A pair of nodes (v,w) is associated with every
 * edge; v is called the  source of e and w is called the
 * target of e, and v and w are called  endpoints of e.
 * The edge e is said to be incident to its endpoints.
 * <P>
 * A graph is either  directed or  undirected. The difference between
 * directed and undirected graphs is the way the edges incident to a node
 * are stored and how the concept  adjacent is defined.
 * <P>
 * In directed graphs two lists of edges are associated with every node v:
 * adj_edges(v) = e  v = source(e),
 * i.e., the list of edges starting in v, and
 * in_edges(v) = e  Lvert v = target(e), i.e., the list of
 * edges ending in v.  The list adj_edges(v) is called the adjacency list
 * of node v and the edges in adj_edges(v) are called the edges
 * adjacent to node v. For directed graphs we often use out_edges(v)
 * as a synonym for adj_edges(v).
 * <P>
 * In undirected graphs only the list adj_edges(v) is defined
 * for every every node v. Here it contains all edges incident to v, i.e.,
 * adj_edges(v) = e   v  source(e),target(e).
 * An undirectect graph may not contain selfloops, i.e., it may not contain an
 * edge whose source is equal to its target.
 * <P>
 * In a directed graph an edge is adjacent to its source and in an undirected
 * graph it is adjacent to its source and target. In a directed graph a node w
 * is adjacent to a node v if there is an edge (v,w) E; in an undirected
 * graph w is adjacent to v if there is an edge (v,w) or (w,v) in the
 * graph.
 * <P>
 * A directed graph can be made undirected and vice versa:
 * G.make_undirected() makes the directed graph G undirected by
 * appending for each node v the list in_edges(v) to the list
 * adj_edges(v) (removing selfloops).  Conversely, G.make_directed()
 * makes the undirected graph G directed by splitting for
 * each node v the list adj_edges(v) into the lists out_edges(v) and
 * in_edges(v). Note that these two operations are not exactly inverse to
 * each other.
 * The data type ugraph (cf. section ref{Undirected Graphs)  can only
 * represent undirected graphs.
 * <P>
 * Reversal Information, Maps and Faces
 * The reversal information of an edge e is accessed through G.reversal(e), it has type edge and may or may not be defined (=nil).
 * Assume that G.reversal(e) is defined and let
 * e' = G.reversal(e). Then e = (v,w) and e' = (w,v) for some
 * nodes v and w, G.reversal(e') is defined and e = G.reversal(e'). In other words, reversal deserves its name.
 * <P>
 * We call a directed graph bidirected if for every edge of G the
 * reversed edge also belongs to G and we call a bidirected graph a map
 * if all edges have their reversal information defined. Maps are the data
 * structure of choice for embedded graphs. For an edge e of a map G let
 * face_cycle_succ(e) = cyclic_adj_succ(reversal(e)) and consider the sequence
 * e, face_cycle_succ(e), face_cycle_succ(face_cycle_succ(e)),. The
 * first edge to repeat in this sequence is e (why?) and the set of edges
 * appearing in this sequence is called the face cycle containing e.
 * Each edge is contained in some face cycle and face cycles are pairwise
 * disjoint. Let f be the number of face cycles, n be the number of nodes,
 * m be the number of edges, and let c be the number of connected components.
 * Then g = (m/2 - n - f)/2 + c is called the genus of the map
 * (note that m/2 is the number of edges in the underlying
 * undirected graph). The genus is zero if and only if the map is planar, i.e.,
 * there is an embedding of G into the plane such that for every node v the
 * counter-clockwise ordering of the edges around v agrees with the cyclic
 * ordering of v's adjacency list.
 * <P>
 * If a graph G is a map the faces of G can be constructed explicitely
 * by G.compute_faces(). Afterwards, the faces of G can be traversed
 * by different iterators, e.g., forall_faces(f,G) iterates over
 * all faces , forall_adj_faces(v) iterates over all faces adjacent
 * to node v. By using face maps or arrays (data types face_map
 * and face_array) additional information can be associated with
 * the faces of a graph. Note that any update operation performed on
 * G invalidates the list of faces. See the section on face operations
 * for a complete list of available operations for faces.
 *
 */
public class Graph implements ParamTypes{
    private static int node_data_slots = 0;
    private static int edge_data_slots = 0;
    
    private static int after = 0;
    private static int before = 1;
    
    private boolean undirected;
    
    private int max_node_index; //max_n_index //maximal node index
    private int max_edge_index; //max_e_index //maximal edge index
    private int max_face_index; //max_f_index //maximal face index
    
    private int[] data_sz;	//number of additional node/edge/face data slots
    private LinkedList[] free_data;	//list of unused node/edge/face data slots
    private LinkedList[] map_list;  //list of registered node/edge/face maps
    
    /** List of nodes in this graph.
     */    
    protected LinkedList v_list;	//list of all nodes
    private LinkedList v_free;	//list of free nodes
    
    private LinkedList e_list;	//list of all edges
    private LinkedList e_free;	//list of free edges
    private LinkedList h_list;	//list of hidden edges
    
    private LinkedList f_list;	//list of all faces
    private LinkedList f_free;	//list of free faces
    
    private LinkedList v_list_tmp;	//temporary list of nodes
    private LinkedList e_list_tmp;	//temporary list of edges
    private LinkedList f_list_tmp;	//temporary list of faces
    
    private GraphMap FaceOf;
    private GraphMap adj_iterator;
    
    /** The parent graph of this graph.
     */    
    protected Graph parent;
    
    /** Graph map of nodes.
     */    
    protected GraphMap node_data_map;	//GraphMap is originally graph_map
    /** Graph map of edges.
     */    
    protected GraphMap edge_data_map;
    
    private Graph GGG;			//needed for the sort functions
    
    /** Unused data member.
     */    
    public int space;
    
    /** Sorter class for edges.
     */    
    protected class EdgeSorter implements SortFunction{
        /** Compares edges.
         * @param edge1 First edge compared.
         * @param edge2 Second edge compared.
         * @return Always false.
         */        
        public boolean is_less_than(Object edge1, Object edge2){
            return false;
        }
    }
    
    /** Sorter class for nodes.
     */    
    protected class NodeSorter implements SortFunction{
        /** Compares nodes.
         * @param node1 First node compared.
         * @param node2 Second node compared.
         * @return Always false.
         */        
        public boolean is_less_than(Object node1, Object node2){
            return false;
        }
    }
    
    /** Ordering class for this graph.
     */    
    protected class Orderer implements OrderingFunction{
        /** Returns the value of the given object.
         * @param item The object for which a value calculated.
         * @return Always 0.
         */        
        public int order(Object item){
            return 0;
        }
    }
    
    /** Map edge ordering class.
     */    
    protected class MapEdgeOrd1 implements OrderingFunction {
        /** Returns the index value of the source node of the given edge.
         * @param the_item The edge for which a value is requested.
         * @return The index value.
         */        
        public int order(Object the_item){
            return (((Edge)the_item).source().index());
        }
    }
    
    /** Map edge ordering class.
     */    
    protected class MapEdgeOrd2 implements OrderingFunction {
        //needed for make_map() in Graph :JL
        /** Returns the index value of the target node of the given edge.
         * @param the_item The edge for which a value is requested.
         * @return The index value.
         */        
        public int order(Object the_item){
            return (((Edge)the_item).target().index());
        }
    }
    
    //	protected static NodeSorter node_comparer;
    //	protected static EdgeSorter edge_comparer;
    //	protected static Orderer order_alg;
    /** Comparison function for comparing edges.
     */    
    protected SortFunction edge_comparer;
    /** Comparison function for comparing nodes.
     */    
    protected SortFunction node_comparer;
    /** Ordering function for sorts.
     */    
    protected OrderingFunction order_alg;
    /** Map edge ordering instance.
     */    
    protected MapEdgeOrd1   map_edge_ord1;
    /** Map edge ordering instance.
     */    
    protected MapEdgeOrd2   map_edge_ord2;
    
    
    
    /** Constructor.
     */    
    public Graph(){
        int sz1 = node_data_slots;
        int sz2 = edge_data_slots;
        max_node_index = -1;
        max_edge_index = -1;
        max_face_index = -1;
        parent = null;
        undirected = false;
        data_sz = new int[3];
        data_sz[0] = sz1;
        data_sz[1] = sz2;
        data_sz[2] = 0;
        e_list = new LinkedList();
        e_free = new LinkedList();
        h_list = new LinkedList();
        e_list_tmp = new LinkedList();
        v_list = new LinkedList();
        v_free = new LinkedList();
        v_list_tmp = new LinkedList();
        f_list = new LinkedList();
        f_free = new LinkedList();
        f_list_tmp = new LinkedList();
        
        free_data = new LinkedList[3];
        for(int i = 0; i < 3; i++) free_data[i] = new LinkedList();
        while (sz1 != 0) free_data[0].addFirst(new Integer(sz1--));
        while (sz2 != 0) free_data[1].addFirst(new Integer(sz2--));
        map_list = new LinkedList[3];
        for(int i = 0; i < 3; i++) map_list[i] = new LinkedList();
        node_data_map = new GraphMap(this,0);
        edge_data_map = new GraphMap(this,1);
        adj_iterator  = new GraphMap(this,0,0);
        FaceOf = null;
    }
    
    /** Constructor.
     * @param sz1 Number of nodes.
     * @param sz2 Number of Edges.
     */    
    public Graph(int sz1, int sz2){
        max_node_index = -1;
        max_edge_index = -1;
        max_face_index = -1;
        parent = null;
        undirected = false;
        data_sz = new int[3];
        data_sz[0] = sz1;
        data_sz[1] = sz2;
        data_sz[2] = 0;
        free_data = new LinkedList[3];
        while (sz1 != 0) free_data[0].addFirst(new Integer(sz1--));
        while (sz2 != 0) free_data[1].addFirst(new Integer(sz2--));
        map_list = new LinkedList[3];
        node_data_map = new GraphMap(this,0);
        edge_data_map = new GraphMap(this,1);
        adj_iterator = new GraphMap(this,0,0);
        FaceOf = null;
    }
    
    /** SubGraph constructor.
     * @param a Parent graph.
     * @param nl List of nodes for this graph.
     * @param el List of edges for this graph.
     */    
    public Graph(Graph a, LinkedList nl, LinkedList el){
        // construct subgraph (nl,el) of graph a
        
        parent = a;
        Node v,w;
        Edge e;
        Node[] N = new Node[a.max_node_index+1];
        
        //obs		forall(v,nl)
        for(int position = 0; position < nl.size(); position++){
            v = (Node)nl.get(position);
            if (v.graph_of() != parent)
                System.err.println("graph: illegal node in subgraph constructor");	//error_handler 1
            N[position] = new Node(v.getData());
        }
        
        //obs		forall(e,el)
        for(int position = 0; position < el.size(); position++){
            e = (Edge)el.get(position);
            v = e.source();
            w = e.target();
            if ( e.graph_of()!= parent || N[v.index()] == null || N[w.index()] == null )
                System.err.println("graph: illegal edge in subgraph constructor");	//error_handler 1
            
            new_edge(N[v.index()],e,N[w.index()],null,null,after,after);
            //obs			new_edge(N[v.index()],N[w.index()],e);
        }
        undirected = a.undirected;
        N = null;
    }
    
    /** SubGraph constructor.
     * @param G Parent graph.
     * @param el List of Edges for this graph.
     */    
    public Graph(Graph G, LinkedList el){
        // construct subgraph of graph G with edge set el
        Node  v,w;
        Edge  e;
        Node[] N = new Node[G.max_node_index+1];
        for(ListIterator nodes = G.v_list.listIterator(); nodes.hasNext();){
            v =(Node)nodes.next();
            N[v.index()] = null;
        }
        //obs		forall_nodes(v,G) N[index(v)] = 0;
        parent = G;
        for(ListIterator edges = el.listIterator(); edges.hasNext();){
            e =(Edge)edges.next();
            //obs		forall(e,el)
            v = e.source();
            w = e.target();
            if (N[v.index()] == null) N[v.index()] = new_node((Object)v);
            if (N[w.index()] == null) N[w.index()] = new_node((Object)w);
            if ( e.graph_of() != parent )
                System.err.println("graph: illegal edge in subgraph constructor");	//error_handler 1
            new_edge(N[v.index()],N[w.index()],(Object)e);
        }
        undirected = G.undirected;
        N = null;
        //obs		delete[] N;
    }
    
    /** Returns a listiterator of the list of nodes in this graph.
     * @return The list iterator of nodes.
     */    
    public ListIterator nodeIterator(){return v_list.listIterator();}

    /** Returns a listiterator of edges in this graph.
     * @return The list iterator of edges in this graph.
     */    
    public ListIterator edgeIterator(){return e_list.listIterator();}
    
    
    
    
    /** Creates a new edge to the given nodes. Can be used with undirected graphs.
     * @param v The source node.
     * @param e1 Edge connecting to the source node.
     * @param w The target node.
     * @param e2 Edge connecting to the target node.
     * @param i The data to be stored in the edge.
     * @param d1 Method of connecting to e1. The new edge is connected after(if d1=0)/before(if d1=1) e1.
     * @param d2 Method of connecting to e2. The new edge is connected after(if d2=0)/before(if d2=1) e2.
     * @return The new edge. */    
    public Edge new_edge(Node v, Edge e1, Node w, Edge e2, Object i,int d1,int d2){
        if (undirected)
        { if (v == w)
              System.err.println("new_edge(v,e1,w,e2): selfloop in undirected graph.");
          if (e1 != null && v != e1.source() && v != e1.target())
              System.err.println("new_edge(v,e1,w,e2): v is not adjacent to e1.");
          if (e2 != null && w != e2.source() && w != e2.target())
              System.err.println("new_edge(v,e1,w,e2): w is not adjacent to e2.");
        }
        else
        { if (e1 != null && v != e1.source())
              System.err.println("new_edge(v,e1,w,e2): v is not source of e1.");
          if (e2 != null && w != e2.source() && w != e2.target())
              System.err.println("new_edge(v,e1,w,e2): w is not target of e2.");
        }
        
        pre_new_edge_handler(v,w);
        Edge e = add_edge(v,w,i);
        ins_adj_edge(e,v,e1,w,e2,d1,d2);
        post_new_edge_handler(e);
        return e ;
    }
    
    /** Creates a new edge between the given nodes and containing the given information.
     * @param v The source node for the new edge.
     * @param w The target node for the new edge.
     * @param i The data to be stored in the edge.
     * @return The new edge. */    
    public Edge new_edge(Node v, Node w, Object i){
        // append (v,w) it to adj_list of v and to in_list (adj_list) of w
        return new_edge(v,null,w,null,i,0,0);}
    
    /** Creates a new edge between the source of the given edge and the given target node.
     * @param e The edge connected to the source node.
     * @param w The target node.
     * @param i The data stored in the new edge.
     * @param dir Method of connecting to e. The new edge is connected after(if dir=0)/before(if dir=1) e.
     * @return The new edge. */    
    public Edge new_edge(Edge e, Node w, Object i, int dir){
        // add edge (e.source(),w) after/before e1 to adj_list of e.source()
        // append it to in_list (adj_list) of w
        return new_edge(e.source(),e,w,null,i,dir,0);
    }
    
    /** Creates a new edge between the target of the given edge and the given source node.
     * @param v The source node.
     * @param e The edge connected to the target node.
     * @param i The data stored in the new edge.
     * @param dir Method of connecting to e. The new edge is connected after(if dir=0)/before(if dir=1) e.
     * @return The new edge. */    
    public Edge new_edge(Node v, Edge e, Object i, int dir){
        // append edge(v,e.target())  to adj_list of v
        // insert it after/before e to in_list (adj_list) of e.target()
        return new_edge(v,null,e.target(),e,i,0,dir);
    }
    
    /** Creates a new edge 
     * @param e1 Edge connecting to source node.
     * @param e2 Edge connecting to target node.
     * @param i The data stored in the new edge.
     * @param dir1 Method of connecting to e1. The new edge is connected after(if dir1=0)/before(if dir1=1) e1.
     * @param dir2 Method of connecting to e2. The new edge is connected after(if dir2=0)/before(if dir2=1) e2.
     * @return The new edge. */    
    public Edge new_edge(Edge e1, Edge e2, Object i, int dir1, int dir2){
        //add edge (e1.source(),e2.target())
        //after(dir=0)/before(dir=1) e1 to adj_list of e1.source()
        //after(dir=1)/before(dir=1) e2 to in_list (adj_list) of e2.target()
        return new_edge(e1.source(),e1,e2.target(),e2,i,dir1,dir2);
    }
    
    /** Creates a new edge between the given nodes.
     * @param v The source node.
     * @param e Edge connected to the source node.
     * @param w The target node.
     * @param i The data stored in the new edge.
     * @param dir Method of connecting to e. The new edge is connected after(if dir=0)/before(if dir=1) e.
     * @return The new edge. */    
    public Edge new_edge(Node v, Edge e, Node w, Object i, int dir){
        // add edge (v,w) after/before e to adj_list of v
        // append it to in_list (adj_list) of w
        return new_edge(v,e,w,null,i,dir,0);
    }
    
    /** Creates a new edge between the given nodes.
     * @param v The source node.
     * @param w The target node.
     * @param e Edge connected to the target node.
     * @param i The data stored in the new edge.
     * @param dir Method of connecting to e. The new edge is connected after(if dir=0)/before(if dir=1) e.
     * @return The new edge. */    
    public Edge new_edge(Node v, Node w, Edge e, Object i, int dir){
        // append edge (v,w) to adj_list of v
        // insert it after/before e to in_list (adj_list) of w
        return new_edge(v,null,w,e,i,dir,0);
    }
    
    /** Creates a new edge between the given nodes.
     * @param v The source node.
     * @param w The target node.
     * @return The new edge. */    
    public Edge new_edge(Node v,Node w){
        Object x= null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(v,w,x);
    }
    
    /** Creates a new edge between the source of the given edge and the given target node.
     * @param e Edge connected to the source node.
     * @param w The target node.
     * @return The new edge. */    
    public Edge new_edge(Edge e,Node w){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(e,w,x,after);
    }
    
    /** Creates a new edge between the source of the given edge and the given target node.
     * @param e Edge connected to the source node.
     * @param w The target node.
     * @param dir Method of connecting to e. The new edge is connected after(if dir=0)/before(if dir=1) e.
     * @return The new edge. */    
    public Edge new_edge(Edge e,Node w,int dir){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(e,w,x,dir);
    }
    
    /** Creates a new edge between the given source node and the target of the given edge.
     * @param v The source node.
     * @param e Edge connected to the target node.
     * @return The new edge. */    
    public Edge new_edge(Node v, Edge e){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(v,e,x,after);
    }
    
    /** Creates a new edge between the given source node and the target of the given edge.
     * @param v The source node.
     * @param e Edge connected to the target node.
     * @param dir Method of connecting to e. The new edge is connected after(if dir=0)/before(if dir=1) e.
     * @return The new edge. */    
    public Edge new_edge(Node v, Edge e, int dir){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(v,e,x,dir);
    }
    
    /** Creates a new edge between the source and target nodes of the given edges.
     * @param e1 Edge connected to the source node.
     * @param e2 Edge connected to the target node.
     * @param d1 Method of connecting to e1. The new edge is connected after(if d1=0)/before(if d1=1) e1.
     * @param d2 Method of connecting to e2. The new edge is connected after(if d2=0)/before(if d2=1) e2.
     * @return The new edge. */    
    public Edge new_edge(Edge e1, Edge e2, int d1, int d2){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(e1,e2,x,d1,d2);
    }
    
    /** Creates a new edge between the source and target nodes of the given edges. The new edge is added after e2.
     * @param e1 Edge connected to the source node.
     * @param e2 Edge connected to the target node.
     * @param d1 Method of connecting to e1. The new edge is connected after(if d1=0)/before(if d1=1) e1.
     * @return The new edge. */    
    public Edge new_edge(Edge e1, Edge e2, int d1){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(e1,e2,x,d1,after);
    }
    
    /** Creates a new edge between the source and target nodes of the given edges. The new edge is added after both edges.
     * @param e1 Edge connected to the source node.
     * @param e2 Edge connected to the target node.
     * @return The new edge. */    
    public Edge new_edge(Edge e1, Edge e2){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(e1,e2,x,after,after);
    }
    
    /** Creates a new edge between the given nodes.
     * @param v The source node.
     * @param e1 Edge connected to the source node.
     * @param w The target node.
     * @param e2 Edge connected to the target node.
     * @param d1 Method of connecting to e1. The new edge is connected after(if d1=0)/before(if d1=1) e1.
     * @param d2 Method of connecting to e2. The new edge is connected after(if d2=0)/before(if d2=1) e2.
     * @return The new edge. */    
    public Edge new_edge(Node v, Edge e1, Node w, Edge e2, int d1, int d2){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(v,e1,w,e2,x,d1,d2);
    }
    
    /** Creates a new edge between the given nodes. The new edge is added after e2.
     * @param v The source node.
     * @param e1 Edge connected to the source node.
     * @param w The target node.
     * @param e2 Edge connected to the target node.
     * @param d1 Method of connecting to e1. The new edge is connected after(if d1=0)/before(if d1=1) e1.
     * @return The new edge.
     */    
    public Edge new_edge(Node v, Edge e1, Node w, Edge e2, int d1){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(v,e1,w,e2,x,d1,after);
    }
    
    /** Creates a new edge between the given nodes. The new edge is added after both edges.
     * @param v The source node.
     * @param e1 Edge connected to the source node.
     * @param w The target node.
     * @param e2 Edge connected to the target node.
     * @return The new edge. */    
    public Edge new_edge(Node v, Edge e1, Node w, Edge e2){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(v,e1,w,e2,x,after,after);
    }
    
    /** Creates a new edge between the given nodes.
     * @param v The source node.
     * @param e Edge connected to the source node.
     * @param w The target node.
     * @param d Method of connecting to e. The new edge is connected after(if d=0)/before(if d=1) e.
     * @return The new edge. */    
    public Edge new_edge(Node v, Edge e, Node w,int d){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(v,e,w,x,d);
    }
    
    /** Creates a new edge between the given nodes. the new edge is added after e.
     * @param v The source node.
     * @param e Edge connected to the source node.
     * @param w The target node.
     * @return The new edge. */    
    public Edge new_edge(Node v, Edge e, Node w){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(v,e,w,x,after);
    }
    
    /** Creates a new edge between the given nodes.
     * @param v The source node.
     * @param w The target node.
     * @param e Edge connected to the target node.
     * @param d Method of connecting to e. The new edge is connected after(if d=0)/before(if d=1) e.
     * @return The new edge. */    
    public Edge new_edge(Node v, Node w, Edge e, int d){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(v,e,w,x,d);
    }
    
    /** Creates a new edge between the given nodes.
     * @param v The source node.
     * @param w The target node.
     * @param e Edge connected to the target node.
     * @return The new edge. */    
    public Edge new_edge(Node v, Node w, Edge e){
        Object x = null;
        //waiting for init_edge_entry		init_edge_entry(x);
        return new_edge(v,e,w,x,after);
    }
    
    private Node add_node(Object info){
        Node v;
        if (v_free.size() == 0){
            //obs			v = (node)std_memory.allocate_bytes(node_bytes());
            //obs			new (v) node_struct(info);
            v = new Node(info);
            v.owner = this;
            v.id = ++max_node_index;
            //don't know what succ_link is			v.succ_link = null;
        }
        else{
            v = (Node)v_free.removeFirst();
            v.data = info;
        }
        v_list.addLast(v);
        GraphMap m;
        //obs		forall(m,map_list[0]) m->re_init_entry(v);
        for(ListIterator maps = map_list[0].listIterator();
        maps.hasNext();
        m = (GraphMap)maps.next(), m.re_init_entry(v));
        return v;
    }
    
    private Edge add_edge(Node v,Node w,Object info){
        Edge e;
        if (v.owner != this)
            System.err.println("new_edge(v,w): v not in graph");
        if (w.owner != this)
            System.err.println("new_edge(v,w): w not in graph");
        if ( e_free.size() == 0){
            //obs			e = (edge)std_memory.allocate_bytes(edge_bytes());
            //obs			new (e) edge_struct(v,w,inf);
            e = new Edge(v,w,info);
            e.id = ++max_edge_index;
        }
        else{
            e = (Edge)e_free.removeFirst();
            e.data = info;
            e.term[0] = v;
            e.term[1] = w;
            e.rev = null;
            e.succ_adj_edge[0] = null;
            e.succ_adj_edge[1] = null;
            e.pred_adj_edge[0] = null;
            e.pred_adj_edge[1] = null;
        }
        e_list.addLast(e);
        GraphMap m;
        //obs		forall(m,map_list[1]) m->re_init_entry(e);
        for(ListIterator maps = map_list[1].listIterator();
        maps.hasNext();
        m = (GraphMap)maps.next(), m.re_init_entry(e));
        return e;
    }
    
    
    private Face add_face(Object info){
        Face f;
        if (f_free.size() == 0){
            //obs			f = (face)std_memory.allocate_bytes(face_bytes());
            //obs			new (f) face_struct(inf);
            f = new Face(info);
            f.owner = this;
            f.id = ++max_face_index;
        }
        else{
            f = (Face)f_free.removeFirst();
            f.data = info;
        }
        f_list.addLast(f);
        GraphMap m;
        //obs		forall(m,map_list[2]) m->re_init_entry(f);
        for(ListIterator maps = map_list[2].listIterator();
        maps.hasNext();
        m = (GraphMap)maps.next(), m.re_init_entry(f));
        return f;
    }
    
    
    /** Adds edge e between the given nodes.
     * @param e Edge to be added.
     * @param v Source node for the edge.
     * @param e1 Edge connected to the source node.
     * @param w Target node for the edge.
     * @param e2 Edge connected to the target node.
     * @param d1 Method of connecting to e1. The new edge is connected after(if d1=0)/before(if d1=1) e1.
     * @param d2 Method of connecting to e2. The new edge is connected after(if d2=0)/before(if d2=1) e2. */    
    public void ins_adj_edge(Edge e, Node v, Edge e1, Node w, Edge e2,int d1,int d2){
        // insert edge e
        // after(if d1=0)/before(if d1=1) e1 to adj_list of v
        // after(if d2=0)/before(if d2=1) e2 to in_list (adj_list) of w
        // (most general form of new_edge)
        
        if ( undirected )
        { if (v == w)
              System.err.print("new_edge(v,e1,w,e2): selfloop in undirected graph.");
          if (e1 != null && v != e1.source() && v != e1.target())
              System.err.print("new_edge(v,e1,w,e2): v is not adjacent to e1.");
          if (e2 != null && w != e2.source() && w != e2.target())
              System.err.print("new_edge(v,e1,w,e2): w is not adjacent to e2.");
          
          v.insert_adj_edge(e,e1,0,0,d1);
          w.insert_adj_edge(e,e2,0,1,d2);
        }
        else
        { if (e1 != null && v != e1.source())
              System.err.print("new_edge(v,e1,w,e2): v is not source of e1.");
          if (e2 != null && w != e2.source() && w != e2.target())
              System.err.print("new_edge(v,e1,w,e2): w is not target of e2.");
          
          v.insert_adj_edge(e,e1,0,0,d1);
          w.insert_adj_edge(e,e2,1,1,d2);
        }
    }
    
    /** Deletes the given edge between the given nodes.
     * @param e The edge to be deleted.
     * @param v The source node for the edge.
     * @param w The target node for the edge. */    
    public void del_adj_edge(Edge e, Node v, Node w)
    { if (undirected){
          v.del_adj_edge(e,0,0);
          w.del_adj_edge(e,0,1);
      }
      else{
          v.del_adj_edge(e,0,0);
          w.del_adj_edge(e,1,1);
      }
    }
    
    /** Registers the given GraphMap.
     * @param M Graphmap to be registered.
     * @return -1 if there is not enough free_data space, 0 otherwise. */    
    public int register_map(GraphMap M){
        int k = M.kind;
        map_list[k].addLast(M);				//appends the map onto the end of map_list
        //		M.g_loc = map_list[k].size() - 1;		//M.g_loc gets the index number of GraphMap M in map_list
        
        //waiting on LEDA_GRAPH_DATA		if (LEDA_GRAPH_DATA)		//may be added to Globals class
        //		if (free_data[k].size() <= 0)
        //		System.err.println("graph::register_map: all data " + data_sz[k] + " slots used");	//error_handler 1
        return (free_data[k].size() <= 0) ? -1 : 0; //free_data[k].removeFirst();
        
    }
    
    /** Unregisters the given GraphMap.
     * @param M Graphmap to be unregistered. */    
    public void unregister_map(GraphMap M){
        int k = M.kind;
        map_list[k].remove(M.g_loc);
        if (M.g_index > 0) free_data[k].addFirst(new Integer(M.g_index));
    }
    
    /** Returns the first node in the graph's node list.
     * @return The first node.
     */    
    public Node first_node(){return (Node) v_list.getFirst();}

    /** Returns the last node in the graph's node list.
     * @return The last node.
     */    
    public Node last_node(){return (Node) v_list.getLast();}

    /** Returns the next node in the node list.
     * @param v The node whose successor is requested.
     * @return The next node. */    
    public Node succ_node(Node v){
        if (v != null && v.id < v_list.size() && v.id > 0)
        {return (Node)v_list.get(v.id + 1);}
        else {return null;}
    }
    
    /** Returns the previous node in the node list.
     * @param v The node whose predecessor is requested.
     * @return The previous node. */    
    public Node pred_node(Node v){
        if (v != null && v.id <= v_list.size() && v.id >= 0)
        {return (Node)v_list.get(v.id - 1);}
        else {return null;}
    }
    
    /** Returns the first edge in the edge list.
     * @return The first edge. */    
    public Edge first_edge(){return (Edge) e_list.getFirst();}

    /** Returns the last edge in the edge list.
     * @return The last edge. */    
    public Edge last_edge(){return (Edge) e_list.getLast();}

    /** Returns the next edge in the edge list.
     * @param e The edge whose successor is requested.
     * @return The next edge.
     */    
    public Edge succ_edge(Edge e){
        if (e != null && e.id < e_list.size() && e.id >= 0)
        {return (Edge)e_list.get(e.id + 1);}
        else {return null;}
    }

    /** Returns the previous edge in the edge list.
     * @param e The edge whose predecessor is requested.
     * @return The previous edge. */    
    public Edge pred_edge(Edge e){
        if (e != null && e.id <= e_list.size() && e.id > 0)
        {return (Edge)e_list.get(e.id - 1);}
        else {return null;}
    }
    
    /** Copy constructor.
     * @param a Graph to be copied.
     */    
    public Graph(Graph a){
        undirected = a.undirected;
        copy_graph(a);
        node_data_map = new GraphMap(this,0);
        edge_data_map = new GraphMap(this,1);
        adj_iterator = new GraphMap(this,0,0);
        a.copy_all_entries();
    }
    
    /** Copies all entries; does nothing.
     */    
    protected void copy_all_entries(){
        //obs		Node v;
        
        //waiting for copy_node_entry		for(ListIterator nodes = v_list.listIterator();
        //waiting for copy_node_entry		    nodes.hasNext();
        //waiting for copy_node_entry		    copy_node_entry(((Node)nodes.next()).data));
        
        //obs		forall_nodes(v,this) copy_node_entry(v.data[0]);
        //obs		for(Node LOOP_VAR = G.first_node(); LOOP_VAR != null; LOOP_VAR = G.succ_node(LOOP_VAR);)
        //obs			copy_node_entry(LOOP_VAR.data);
        //obs		Edge e;
        //obs		forall_edges(e,this) copy_edge_entry(e.data[0]);
        
        //waiting for copy_edge_entry		for(ListIterator edges = e_list.listIterator();
        //waiting for copy_edge_entry		    edges.hasNext();
        //waiting for copy_edge_entry		    copy_edge_entry(((Edge)edges.next()).data));
        // hidden edges
        //waiting for copy_edge_entry		for(ListIterator h_edges = h_list.listIterator();
        //waiting for copy_edge_entry		    h_edges.hasNext();
        //waiting for copy_edge_entry		    copy_edge_entry(((Edge)h_edges.next()).data));
        
        //obs		for(e = (Edge)h_list.getFirst(); e != null; e = (Edge)h_list.succ(e))
        //obs			copy_edge_entry(e.data);
    }
    
    /** Clears all entries, does nothing.
     */    
    protected void clear_all_entries(){
        //waiting for clear_node_entry		for(ListIterator nodes = v_list.listIterator();
        //waiting for clear_node_entry		    nodes.hasNext();
        //waiting for clear_node_entry		    clear_node_entry(((Node)nodes.next()).data));
        //obs		node v;
        //obs		forall_nodes(v,*this) clear_node_entry(v->data[0]);
        //waiting for clear_edge_entry		for(ListIterator edges = e_list.listIterator();
        //waiting for clear_edge_entry		    edges.hasNext();
        //waiting for clear_edge_entry		    clear_edge_entry(((Edge)edges.next()).data));
        //obs		edge e;
        //obs		forall_edges(e,*this) clear_edge_entry(e->data[0]);
        // hidden edges
        //waiting for clear_edge_entry		for(ListIterator h_edges = h_list.listIterator();
        //waiting for clear_edge_entry		    h_edges.hasNext();
        //waiting for clear_edge_entry		    clear_edge_entry(((Edge)h_edges.next()).data));
        //obs		for(e = (edge)h_list.head(); e; e = (edge)h_list.succ(e))
        //obs		clear_edge_entry(e->data[0]);
    }
    
    /** Returns "void".
     * @return "void" */    
    public String node_type(){ return "void"; }

    /** Returns "void".
     * @return "void" */    
    public String edge_type(){ return "void"; }

    /** Returns "void".
     * @return "void" */    
    public String face_type(){ return "void"; }
    
    /** Copies the given graph.
     * @param G The graph to be copied. */    
    public void copy_graph(Graph G){
        
        int n = G.number_of_nodes();
        
        for(int k = 0; k < 3; k++){
            data_sz[k] = G.data_sz[k];
            //free_data isn't a list of integers			for(int i=0; i < data_sz[k]; i++) free_data[k].addLast(i);
        }
       
        max_node_index = -1;
        max_edge_index = -1;
        max_face_index = -1;
        e_list.clear();
        FaceOf = null;
        parent = null;
        
        if (n == 0) return;	//If there are no nodes in Graph G there is no need to copy them
        
        Node[] node_vec = new Node[G.max_node_index+1];			//allocate additional vectors
        Edge[] edge_vec = new Edge[G.max_edge_index+1];
        
        if (node_vec == null || edge_vec == null)				//checks to see if vectors were allocated
            System.err.println(" copy_graph: out of memory");	//error_handler 1
        
        // allocate a single block of memory for all nodes
        //no memory allocation in java		// memory_allocate_block(sizeof(node_struct),n);
        
        Node v;
        for(ListIterator nodes = G.v_list.listIterator();
        nodes.hasNext();
        v = (Node)nodes.next(),
        new_node(v.data));
        
        
        // allocate a single block of memory for all edges
        // memory_allocate_block(sizeof(edge_struct),m);
        
        boolean loops_deleted = false;
        
        // copy faces (if existing)
        Face f;
        Face f1;
        for(ListIterator faces = G.f_list.listIterator();
        faces.hasNext();
        f = (Face)faces.next());
        node_vec = null;
        edge_vec = null;
        if ( loops_deleted )
            System.err.println("selfloops deleted in ugraph copy constructor");
    }
    
    /** Creates a new node.
     * @return A new node. */    
    public Object new_node(){
        Object x = null;
        pre_new_node_handler();
        Node v = add_node(x);
        v.setIndexNumber(v_list.indexOf(v));//used to provide reference numbers for Inducer.display_struct
        post_new_node_handler(v);
        return v;
    }
    
    /** Creates a new node.
     * @param i Data to be stored in this node.
     * @return A new node. */    
    protected Node new_node(Object i){
        pre_new_node_handler();
        Node v = add_node(i);
        v.setIndexNumber(v_list.indexOf(v)); //used to provide reference numbers for Inducer.display_struct
        post_new_node_handler(v);
        return v;
    }
    
    private Face new_face(Object i){
        return add_face(i);
    }
    
    private Face new_face(){
        Object i = null;
        return add_face(i);
    }
    
    /** Copies the given graph into this graph.
     * @param G The graph to be copied.
     */    
    public void assign(Graph G){
        if (G != this){
            clear();
            undirected = G.undirected;
            copy_graph(G);
        }
    }
    
    /** Sets the node entry, does nothing.
     * @param v The node to be set.
     * @param s The new setting. */    
    public void set_node_entry(Node v, String s){
        //waiting on clear_node_entry		clear_node_entry(v.data);
        //obs		clear_node_entry(v->data[0]);
        //		if (node_type().compareTo("string") == 0)
        //obs		if (strcmp(node_type(),"string") == 0)
        //waiting on leda_copy			v.data = leda_copy(s);
        //obs			v->data[0] = leda_copy(s);
        //		else{
        //waiting for definition of istrstream			istrstream in(s.cstring());
        //			read_node_entry(in,v.data);
        //obs			read_node_entry(in,v->data[0]);
        //		}
    }
    
    /** Sets the edge entry, does nothing.
     * @param e The edge to be set.
     * @param s The new setting. */    
    public void set_edge_entry(Edge e, String s){
        //waiting on clear_edge_entry		clear_edge_entry(e.data);
        //obs		clear_edge_entry(e->data[0]);
        //		if (edge_type().compareTo("string") == 0)
        //obs		if (strcmp(edge_type(),"string") == 0)
        //waiting on leda_copy			e.data = leda_copy(s);
        //obs			e->data[0] = leda_copy(s);
        //		else{
        //waiting for definition of istrstream			istrstream in(s.cstring());
        //			read_edge_entry(in,e.data);
        //obs			read_edge_entry(in,e->data[0]);
        //		}
    }
    
    // virtual functions called before and after update operations
    // (e.g. useful for graph editors)
    
    // node handler							// called
    /** Function called before inserting a node.         */
    protected void pre_new_node_handler() {}		// before inserting a node
    /** Function called after inserting a node.
     * @param A The node added. */    
    protected void post_new_node_handler(Node A) {}	// after inserting node v
    /** Function called before deleting a node.
     * @param A Node deleted. */    
    protected void pre_del_node_handler(Node A) {}	// before deleting node v
    /** Function called after node deleted. */
    protected void post_del_node_handler() {}		// after deleting a node
    
    
    // edge handler
    /** Function called before a new edge is created.
     * @param A Source node.
     * @param B Target node.
     */    
    protected void pre_new_edge_handler(Node A, Node B) {}	// before creating (v,w)
    /** Function called after new edge is created.
     * @param E The new edge.
     */    
    protected void post_new_edge_handler(Edge E) {}			// after insertion of e
    
    /** Function called when edge is deleted.
     * @param E The edge deleted.
     */    
    protected void pre_del_edge_handler(Edge E) {}			// before deleteing edge e
    /** Function called after edge is deleted.
     * @param A Source node of edge.
     * @param B Target node of edge.
     */    
    protected void post_del_edge_handler(Node A, Node B) {}	// after deletion of (v,w)
    
    /** Function called before edge is moved.
     * @param E Edge moved.
     * @param A Edge's source node.
     * @param B Edge's target node.
     */    
    protected void pre_move_edge_handler(Edge E,Node A,Node B){}	// before moving e to (v,w)
    /** Function called after edge is moved.
     * @param E The edge moved.
     * @param A The edge's source node.
     * @param B The edge's target node.
     */    
    protected void post_move_edge_handler(Edge E,Node A,Node B){}	// after moved e from (v,w)
    
    /** Function called before edge is hidden.
     * @param E Edge to hide.
     */    
    protected void pre_hide_edge_handler(Edge E) {}          // before hiding edge e
    /** Function called after edge is hidden.
     * @param E Edge hidden.
     */    
    protected void post_hide_edge_handler(Edge E) {}         // after hiding edge e
    
    /** Function called before edge is restored.
     * @param E Edge to be restored.
     */    
    protected void pre_restore_edge_handler(Edge E) {}       // before restoring edge e
    /** Function called after edge is restored.
     * @param E Edge restored.
     */    
    protected void post_restore_edge_handler(Edge E) {}      // after restoring edge e
    
    
    // global handler
    /** Function called before entries are cleared.
     */    
    protected void pre_clear_handler()  {}                 // before deleting graph
    /** Function called after entries are cleared.
     */    
    protected void post_clear_handler() {}                 // after deleting graph
    
    
    /** Returns the number of nodes.
     * @return Number of nodes. */    
    public int number_of_nodes(){return v_list.size(); }
    /** Returns the number of edges.
     * @return Number of edges. */    
    public int number_of_edges(){return e_list.size(); }
    
    /** Returns a list of all nodes in this graph.
     * @return A list of nodes.
     */    
    public LinkedList all_nodes(){
        v_list_tmp.clear();
        for(ListIterator nodes = v_list.listIterator();
        nodes.hasNext();
        v_list_tmp.addLast(nodes.next()));
        return v_list_tmp;
    }
    
    /** Returns a list of all edges in this graph.
     * @return A list of edges.
     */    
    public LinkedList all_edges(){
        e_list_tmp.clear();
        for(ListIterator edges = e_list.listIterator();
        edges.hasNext();
        e_list_tmp.addLast(edges.next()));
        return e_list_tmp;
    }
    
    /** Returns a list of all faces in this graph.
     * @return A list of faces.
     */    
    public LinkedList all_faces(){
        f_list_tmp.clear();
        for(ListIterator faces = f_list.listIterator();
        faces.hasNext();
        f_list_tmp.addLast(faces.next()));
        return f_list_tmp;
    }
    
    /** Choose a random node.
     * @return A node.
     */    
    public Node choose_node(){
        int n = number_of_nodes();
        if (n == 0) return null;
        Random rand = new Random();
        int r = rand.nextInt(n);
        Node v = (Node)v_list.get(r);
        return v;
    }
    
    /** Choose random edge.
     * @return An edge.
     */    
    public Edge choose_edge(){
        int m = number_of_edges();
        if (m == 0) return null;
        Random rand = new Random();
        int r = rand.nextInt(m);
        Edge e = (Edge)e_list.get(r);
        return e;
    }
    
    /** Returns true if this graph is directed, false otherwise.
     * @return True if this graph is directed, false otherwise.
     */    
    public boolean is_directed(){ return !undirected; }
    /** Returns true if this graph is undirected, false otherwise.
     * @return True if this graph is undirected, false otherwise.
     */    
    public boolean is_undirected(){ return undirected; }
    /** Returns true if there are no nodes in this graph, false otherwise.
     * @return True if there are no nodes in this graph, false otherwise.
     */    
    public boolean empty(){ return v_list.size() == 0; }
    
    //entry used to return addresses to the data, but there are no addresses in java
    /** Returns the data stored in this graph object.
     * @param v The object whose information is requested.
     * @return The data stored in the given object.
     */    
    public Object entry(Node v){return v.data;}
    /** Returns the data stored in this graph object.
     * @param e The object whose information is requested.
     * @return The data stored in the given object.
     */    
    public Object entry(Edge e){return e.data;}
    /** Returns the data stored in this graph object.
     * @param f The object whose information is requested.
     * @return The data stored in the given object.
     */    
    public Object entry(Face f){return f.data;}
    
    //essentially the same as entry
    /** Returns the data stored in this graph object.
     * @param v The object whose information is requested.
     * @return The data stored in the given object.
     */    
    public Object inf(Node v){return v.data;}
    /** Returns the data stored in this graph object.
     * @param e The object whose information is requested.
     * @return The data stored in the given object.
     */    
    public Object inf(Edge e){return e.data;}
    /** Returns the data stored in this graph object.
     * @param f The object whose information is requested.
     * @return The data stored in the given object.
     */    
    public Object inf(Face f){return f.data;}
    
    /** Splits edge e into edges e1 and e2.
     * @param e Edge to be split.
     * @param node_inf Node data for target node.
     * @param e1 First edge si is split into.
     * @param e2 Second edge e is split into.
     * @return The new target node.
     */    
    protected Node split_edge(Edge e, Object node_inf, Edge e1, Edge e2){
        // splits e into e1 and e2 by putting new node v on e
        // WARNING: changes e1, e2.
        //node v = source(e);
        Node w = e.target();
        Node u = add_node(node_inf);
        e1 = e;
        e2 = add_edge(u,w,e.data);
        //waiting on copy_edge_entry		copy_edge_entry(e2.data);
        if (undirected){
            u.append_adj_edge(e2,0,0);
            w.insert_adj_edge(e2,e,0,1,0);
            w.del_adj_edge(e,0,1);
            e.term[1] = u;
            u.append_adj_edge(e,0,1);
        }
        else{
            u.append_adj_edge(e2,0,0);
            w.insert_adj_edge(e2,e,1,1,0);
            w.del_adj_edge(e,1,1);
            e.term[1] = u;
            u.append_adj_edge(e,1,1);
        }
        return u;
    }
    
    /** Assigns the given data to the graph object.
     * @param v The node to store the data.
     * @param x The data stored.
     */    
    protected void assign(Node v,Object x){v.data = x;}
    /** Assigns the given data to the graph object.
     * @param e The edge to store the data.
     * @param x The data stored.
     */    
    protected void assign(Edge e,Object x){e.data = x;}
    /** Assigns the given data to the graph object.
     * @param f The face to store the data.
     * @param x The data stored.
     */    
    protected void assign(Face f,Object x){f.data = x;}
    
    /** Returns the face containing e.
     * @param e Edge for which face is requested.
     * @return The face holding e.
     */    
    protected Face access_face(Edge e){return (Face)FaceOf.map_access(e); }
    
    /** Merges the given nodes.
     * @param v1 First node merged.
     * @param v2 Second node merged.
     * @return The merged node.
     */    
    public Node merge_nodes(Node v1,Node v2){
        if (undirected)
            System.err.println("merge_nodes not implemented for undirected graphs.");	//error_handler 1
        for(int i=0; i<2; i++){
            if (v1.last_adj_edge[i] != null)
                v1.last_adj_edge[i].succ_adj_edge[i] = v2.first_adj_edge[i];
            else
                v1.first_adj_edge[i] = v2.first_adj_edge[i];
            if (v2.first_adj_edge[i] != null){
                v2.first_adj_edge[i].pred_adj_edge[i] = v1.last_adj_edge[i];
                v1.last_adj_edge[i] = v2.last_adj_edge[i];
            }
            v1.adj_length[i] += v2.adj_length[i];
            v2.adj_length[i] = 0;
            v2.first_adj_edge[i] = null;
            v2.last_adj_edge[i] = null;
        }
        //waiting for del_node		del_node(v2);
        return v1;
    }
    
    /** Splits edge e into e1 and e2.
     * @param e Edge to be split.
     * @param e1 First edge split from e.
     * @param e2 Second edge split from e.
     * @return The target node of the split edge.
     */    
    public Node split_edge(Edge e,Edge e1,Edge e2){
        //Warning: changes e1 and e2
        Object x = null;
        //waiting for init_node_entry		init_node_entry(x);
        return split_edge(e,x,e1,e2);
    }
    
    /** Makes the given edge hidden.
     * @param e Edge to hide.
     */    
    public void hide_edge(Edge e){
        if (is_hidden(e))
            System.err.println("graph::hide_edge: edge is already hidden.");	//error_handler 1
        pre_hide_edge_handler(e);
        Node v = e.source();
        Node w = e.target();
        del_adj_edge(e,v,w);
        e_list.remove(e);
        h_list.addLast(e);
        e.id |= 0x80000000;
        post_hide_edge_handler(e);
    }
    
    /** Returns true if the given edge is hidden, false otherwise.
     * @param e The edge to be checked.
     * @return True if the given edge is hidden, false otherwise.
     */    
    public boolean is_hidden(Edge e){return (e.id & 0x80000000) != 0;}
    
    /** Restores edge from being hidden.
     * @param e Edge to be restored.
     */    
    public void restore_edge(Edge e){
        if (!is_hidden(e))
            System.err.println("graph::restore_edge: edge is not hidden.");	//error_handler 1
        pre_restore_edge_handler(e);
        Node v = e.source();
        Node w = e.target();
        h_list.remove(e);
        e_list.addLast(e);
        if (undirected){
            v.append_adj_edge(e,0,0);
            w.append_adj_edge(e,0,1);
        }
        else{
            v.append_adj_edge(e,0,0);
            w.append_adj_edge(e,1,1);
        }
        //		e.id = indexof(e);	//edge id needs to be updated to point to the right location :JL
        e.id = e.index();
        post_restore_edge_handler(e);
    }
    
    /** Restore all edges.
     */    
    public void restore_all_edges(){
        Edge e;
        while (h_list.size() > 0){
            e =(Edge)h_list.getFirst();
            restore_edge(e);
        }
        //obs		Edge e = (Edge)h_list.head();
        //obs		while (e){
        //obs			edge succ = (edge)h_list.succ(e);
        //obs			restore_edge(e);
        //obs			e = succ;
        //obs		}
    }
    
    /** Delete the given node.
     * @param v The node to be deleted.
     */    
    public void del_node(Node v){
        if (v.owner != this)
            System.err.println("del_node(v): v is not in G");	//error_handler 4
        // delete adjacent edges
        Edge  e;
        //waiting for del_edge		while ((e=v.first_adj_edge[0]) != null) del_edge(e);
        if (!undirected)
            //waiting for del_edge		while ((e=v.first_adj_edge[1]) != null) del_edge(e);
            pre_del_node_handler(v);
        //waiting for clear_node_entry		if (parent == null) clear_node_entry(v.data);
        v_list.remove(v);
        v_free.addLast(v);
        //obs		v_free.append(v);
        //waiting for GraphMap() in GraphMap		GraphMap m;
        //waiting for GraphMap() in GraphMap		for(int j = 0;j < 3; j++){
        //waiting for GraphMap() in GraphMap			int i = m.g_index;
        //waiting for clear_entry in GraphMap			if (i > 0) m.clear_entry(v.data);
        //waiting for GraphMap() in GraphMap		}
        //obs		forall(m,map_list[0]){
        //obs			int i = m.g_index;
        //obs			if (i > 0) m.clear_entry(v.data);
        //obs		}
        //waiting for post_del_node_handler in GraphMap		post_del_node_handler();
    }
    
    private void del_face(Face f){
        f_list.remove(f);
        f_free.addLast(f);
        //obs		f_free.append(f);
        GraphMap m;
        for(ListIterator maps = map_list[2].listIterator(); maps.hasNext(); ){
            m = (GraphMap)maps.next();
            //obs		forall(m,map_list[2]){
            int i = m.g_index;
            //waiting for clear_entry in GraphMap			if (i > 0) m.clear_entry(f.data);
            //obs			if (i > 0) m.clear_entry(f.data[i]);
        }
    }
    
    /** Edge to be deleted.
     * @param e Edge to be deleted.
     */    
    public void del_edge(Edge e){
        Node v = e.source();
        Node w = e.target();
        if (v.owner != this) System.err.println("del_edge(e): e is not in G");	//error_handler 10
        pre_del_edge_handler(e);
        if (is_hidden(e)) restore_edge(e);
        if (e.rev != null) e.rev.rev = null;
        del_adj_edge(e,v,w);
        //waiting for clear_edge_entry		if (parent == null) clear_edge_entry(e.data);
        e_list.remove(e);
        e_free.addLast(e);
        //obs		e_free.append(e);
        GraphMap m;
        int i;
        for(int j = 0; j < map_list[1].size(); j++){
            m = (GraphMap)map_list[1].get(j);
            i = m.g_index;
            //waiting for clear_entry			if (i > 0) m.clear_entry(e.data);
        }
        //obs		forall(m,map_list[1]){
        //obs			int i = m->g_index;
        //obs			if (i > 0) m->clear_entry(e->data[i]);
        //obs		}
        post_del_edge_handler(v,w);
    }
    
    /** Deletes the nodes in the given list.
     * @param L List of nodes to be deleted.
     */    
    public void del_nodes(LinkedList L){
        for(int i = 0; i < L.size(); i++) del_node((Node)L.get(i));
        //obs		Node v;
        //obs		forall(v,L) del_node(v);
    }
    
    /** Deletes the edges in the given list.
     * @param L List of edges to be deleted.
     */    
    public void del_edges(LinkedList L){
        for(int i = 0; i < L.size(); i++) del_edge((Edge)L.get(i));
        //obs		edge e;
        //obs		forall(e,L) del_edge(e);
    }
    
    /** Deletes all nodes in graph.
     */    
    public void del_all_nodes() { clear(); }
    
    /** Deletes all edges in graph.
     */    
    public void del_all_edges(){
        Edge e;
        //obs		e = (Edge)e_list.getFirst();
        //obs		while (e)
        //obs			{ edge next = (edge)e_list.succ(e);
        //obs			dealloc_edge(e);
        //obs			e = next;
        //obs		}
        //obs		e = (edge)h_list.head();
        //obs		while (e)
        //obs			{ edge next = (edge)h_list.succ(e);
        //obs			dealloc_edge(e);
        //obs			e = next;
        //obs		}
        //obs		e = (edge)e_free.head();
        //obs		while (e)
        //obs			{ edge next = (edge)e_free.succ(e);
        //obs			dealloc_edge(e);
        //obs			e = next;
        //obs		}
        e_list.clear();
        h_list.clear();
        e_free.clear();
        max_edge_index = -1;
        Node v;
        for(int n = 0; n < v_list.size(); n++){
            v =(Node)v_list.get(n);
            for(int i = 0; i<2; i++){
                v.first_adj_edge[i] = null;
                v.last_adj_edge[i] = null;
                v.adj_length[i] = 0;
            }
        }
        //obs		forall_nodes(v,*this)
        //obs		for(int i=0; i<2; i++)
        //obs			{ v->first_adj_edge[i] = nil;
        //obs			v->last_adj_edge[i] = nil;
        //obs			v->adj_length[i] = 0;
        //obs		}
    }
    
    /** Deletes all faces of graph.
     */    
    public void del_all_faces(){
        f_free.clear();
        f_list.clear();
        FaceOf = null;
        max_face_index = -1;
    }
    
    /** Moves edge e.
     * @param e Edge to be moved.
     * @param e1 Edge connected to the source node.
     * @param e2 Edge connected to the target node.
     * @param d1 Method of connecting to e1. The new edge is connected after(if d1=0)/before(if d1=1) e1.
     * @param d2 Method of connecting to e2. The new edge is connected after(if d2=0)/before(if d2=1) e2.
     */    
    public void move_edge(Edge e,Edge e1,Edge e2,int d1,int d2){
        if (is_hidden(e))
            System.err.println("graph::move_edge:  cannot move hidden edge.");	//error_handler 1
        Node v0 = e.source();
        Node w0 = e.target();
        Node v = e1.source();
        Node w = e1.target();
        pre_move_edge_handler(e,v,w);
        del_adj_edge(e,e.source(),e.target());
        e.term[0] = v;
        e.term[1] = w;
        ins_adj_edge(e,v,e1,w,e2,d1,d2);
        post_move_edge_handler(e,v0,w0);
    }
    
    /** Moves edge e.
     * @param e Edge to be moved.
     * @param e1 Edge connected to the source node.
     * @param w New target node.
     * @param dir Method of connecting to e1. The new edge is connected after(if dir=0)/before(if dir=1) e1.
     */    
    public void move_edge(Edge e,Edge e1,Node w,int dir){
        if (is_hidden(e))
            System.err.println("graph::move_edge:  cannot move hidden edge.");	//error_handler 1
        Node v0 = e.source();
        Node w0 = e.target();
        Node v = e1.source();
        pre_move_edge_handler(e,v,w);
        del_adj_edge(e,e.source(),e.target());
        e.term[0] = v;
        e.term[1] = w;
        ins_adj_edge(e,e1.source(),e1,w,null,dir,0);
        post_move_edge_handler(e,v0,w0);
    }
    
    /** Moves edge e.
     * @param e Edge to be moved.
     * @param v New source node.
     * @param w New target node.
     */    
    public void move_edge(Edge e, Node v, Node w){
        if (is_hidden(e))
            System.err.println("graph::move_edge:  cannot move hidden edge.");	//error_handler 1
        Node v0 = e.source();
        Node w0 = e.target();
        pre_move_edge_handler(e,v,w);
        del_adj_edge(e,e.source(),e.target());
        e.term[0] = v;
        e.term[1] = w;
        ins_adj_edge(e,v,null,w,null,0,0);
        post_move_edge_handler(e,v0,w0);
    }
    
    /** Reverses the direction of the given edge.
     * @param e The edge whose direction is reversed.
     * @return The reversed edge.
     */    
    public Edge rev_edge(Edge e){
        if (is_hidden(e))
            System.err.println("graph::move_edge:  cannot move hidden edge.");	//error_handler	1
        Node v = e.source();
        Node w = e.target();
        pre_move_edge_handler(e,w,v);
        if (is_hidden(e)){ // e hidden
            e.term[0] = w;
            e.term[1] = v;
            return e;
        }
        if (undirected){
            Edge s = e.succ_adj_edge[0];
            Edge p = e.pred_adj_edge[0];
            e.succ_adj_edge[0] = e.succ_adj_edge[1];
            e.pred_adj_edge[0] = e.pred_adj_edge[1];
            e.succ_adj_edge[1] = s;
            e.pred_adj_edge[1] = p;
            e.term[0] = w;
            e.term[1] = v;
        }
        else{
            del_adj_edge(e,v,w);
            e.term[0] = w;
            e.term[1] = v;
            ins_adj_edge(e,w,null,v,null,0,0);
        }
        post_move_edge_handler(e,v,w);
        return e;
    }
    
    /** Reverses the direction on all edges.
     */    
    public void rev_all_edges(){
        if (!undirected){
            LinkedList L = all_edges();
            for(ListIterator LI = L.listIterator();
            LI.hasNext();
            rev_edge((Edge)LI.next()));
            //obs    edge e;
            //obs    forall(e,L) rev_edge(e);
        }
    }
    
    /** Reverses all edges.
     * @return Graph of reversed edges.
     */    
    public Graph rev(){rev_all_edges(); return this;}
    
    /** Creates a list of reversed edges.
     * @return List of reversed edges.
     */    
    public LinkedList insert_reverse_edges(){
        
        LinkedList L = new LinkedList();
        Edge e = first_edge();
        if (e != null){
            L.addLast(new_edge(e.target(),e.source(),e.data));
            //waiting for copy_edge_entry			copy_edge_entry(e.data);
            e = succ_edge(e);
        }
        Edge stop = last_edge();
        while (e != stop){
            L.addLast(new_edge(e.target(),e.source(),e.data));
            //waiting for copy_edge_entry			copy_edge_entry(e.data);
            e = succ_edge(e);
        }
        return L;
    }
    
    /** Converts this graph to an undirected graph.
     */    
    public void make_undirected(){
        if (undirected) return;
        LinkedList loops = new LinkedList();
        Edge e;
        for(ListIterator edges = e_list.listIterator();edges.hasNext();){
            e =(Edge)edges.next();
            if(e.source() == e.target()) loops.addLast(e);
        }
        if ( !(loops.size() == 0 ))
            System.err.println("selfloops deleted in ugraph constructor");	//error_handler	0
        for(ListIterator loopiter = loops.listIterator();
        loopiter.hasNext();
        del_edge((Edge)loopiter.next()));
        //obs		forall(e,loops) del_edge(e);
        /* adj_list(v) = out_list(v) + in_list(v) forall nodes v  */
        Node v;
        for(ListIterator nodes = v_list.listIterator();nodes.hasNext();){
            //obs		forall_nodes(v,*this){
            v =(Node)nodes.next();
            // append in_list to adj_list
            if (v.first_adj_edge[1] == null) continue;
            if (v.first_adj_edge[0] == null){ // move in_list to adj_list
                v.first_adj_edge[0] = v.first_adj_edge[1];
                v.last_adj_edge[0]  = v.last_adj_edge[1];
                v.adj_length[0]     = v.adj_length[1];
            }
            else{	// both lists are non-empty
                v.last_adj_edge[0].succ_adj_edge[0] = v.first_adj_edge[1];
                v.first_adj_edge[1].pred_adj_edge[1] = v.last_adj_edge[0];
                v.last_adj_edge[0] = v.last_adj_edge[1];
                v.adj_length[0] += v.adj_length[1];
            }
            v.first_adj_edge[1] = null;
            v.last_adj_edge[1] = null;
            v.adj_length[1] = 0;
        }
        undirected = true;
    }
    
    /** Converts this graph to a directed graph.
     */    
    public void make_directed(){
        if (!undirected) return;
        // for every node v delete entering edges from adj_list(v)
        // and put them back into in_list(v)
        Node v;
        for(ListIterator nodes = v_list.listIterator();nodes.hasNext();){
            //obs		forall_nodes(v,*this){
            v =(Node)nodes.next();
            Edge e = v.first_adj_edge[0];
            while (e != null)
                if (v == e.target()){
                    Edge e1 = e.succ_adj_edge[1];
                    v.del_adj_edge(e,0,1);
                    v.append_adj_edge(e,1,1);
                    e = e1;
                }
                else
                    e = e.succ_adj_edge[0];
        }
        undirected = false;
    }
    
    /** Clears this graph of edges, nodes, and faces.
     */    
    public void clear(){
        pre_clear_handler();
        GraphMap m;
        for(int k=0; k<3; k++){
            for(ListIterator maps = map_list[k].listIterator();maps.hasNext();){
                //obs			forall(m,map_list[k])
                m = (GraphMap)maps.next();
                if (m.g_index > 0) m.clear_table();
            }
        }
        del_all_faces();
        del_all_edges();
        //obs		Node v = (Node)v_list.head();
        //obs		while (v != null){
        //obs			Node next = (Node)v_list.succ(v);
        //obs			dealloc_node(v);
        //obs			v = next;
        //obs		}
        //obs		v = (node)v_free.head();
        //obs		while (v)
        //obs			{ node next = (node)v_free.succ(v);
        //obs			dealloc_node(v);
        //obs			v = next;
        //obs		}
        v_list.clear();
        v_free.clear();
        max_node_index = -1;
        post_clear_handler();
    }
    
    /** Joins the given graph with this graph.
     * @param G Graph to be joined.
     */    
    public void join(Graph G){
        // moves all objects from G to this graph and clears G
        if (G.undirected != undirected)
            System.err.println("graph::join(G): cannot merge directed and undirected graphs.");	//error_handler 1
        for(int d=0; d<3; d++){
            if (G.data_sz[d] != data_sz[d])
                System.err.println("graph::join(G): cannot merge graphs with different data sizes.");	//error_handler 1
        }
        Node v;
        Edge e;
        Face f;
        int i = max_node_index;
        for(ListIterator nodes = G.v_list.listIterator();
        nodes.hasNext();
        v = (Node)nodes.next(),
        v.id = ++i,
        v.owner = this);
        //obs		forall_nodes(v,G) { v->id = ++i; v->owner = this; }
        max_node_index = i;
        int j = max_edge_index;
        for(ListIterator edges = G.e_list.listIterator();
        edges.hasNext();
        e = (Edge)edges.next(),
        e.id = ++j);
        //obs		forall_edges(e,G) e->id = ++j;
        max_edge_index = j;
        int k = max_face_index;
        for(ListIterator faces = G.f_list.listIterator();
        faces.hasNext();
        f = (Face)faces.next(),
        f.id = ++k);
        //obs		forall_faces(f,G) f->id = ++k;
        max_face_index = k;
        v_list.addAll(G.v_list);
        e_list.addAll(G.e_list);
        f_list.addAll(G.f_list);
        //obs		v_list.conc(G.v_list);
        //obs		e_list.conc(G.e_list);
        //obs		f_list.conc(G.f_list);
        G.v_list.clear();
        G.e_list.clear();
        G.f_list.clear();
        G.max_node_index = -1;
        G.max_edge_index = -1;
        G.max_face_index = -1;
    }
    
    /** Returns the reversed edge.
     * @param e The edge for which reversed is requested.
     * @return The reverse of e.
     */    
    public Edge reversal(Edge e){ return e.rev; }
    /** Returns the reversed edge.
     * @param e The edge for which reversed is requested.
     * @return The reverse of e.
     */    
    public Edge reverse(Edge e){ return e.rev; }
    
    //waiting for face_cycle_succ	public Edge next_face_edge(Edge e){ return face_cycle_succ(e); }
    
    /** Returns the face containing e.
     * @param e Edge for which face is requested.
     * @return The face holding e.
     */    
    public Face face_of(Edge e){ return access_face(e); }
    
    //waiting for face_of	public Face adj_face(Edge e){ return face_of(e); }
    
    /** Returns the number of faces.
     * @return The number of faces.
     */    
    public int number_of_faces(){ return f_list.size(); }
    
    /** Returns the first face in the face list.
     * @return The first face in the face list.
     */    
    public Face first_face(){ return (Face)f_list.getFirst(); }
    /** Returns the last face in the face list.
     * @return The last face in the face list.
     */    
    public Face last_face(){return (Face) f_list.getLast();}
    /** Returns the next face in the face list.
     * @param f The face whose successor is requested.
     * @return The next face.
     */    
    public Face succ_face(Face f){
        if (f != null && f.id < f_list.size() && f.id >= 0)
        {return (Face)f_list.get(f.id + 1);}
        else {return null;}
        //obs		return f?(face)f_list.succ(f):0;
    }
    /** Returns the previous face in the face list.
     * @param f The face whose predecessor is requested.
     * @return The previous face.
     */    
    public Face pred_face(Face f){
        if (f != null && f.id <= f_list.size() && f.id > 0)
        {return (Face)f_list.get(f.id - 1);}
        else {return null;}
        //obs		return f?(face)f_list.pred(f):0;
    }
    
    /** Randomly returns a face.
     * @return A face.
     */    
    public Face choose_face(){
        int l = number_of_faces();
        if (l == 0) return null;
        Random rand = new Random();
        int r = rand.nextInt(l);
        Face f = (Face)f_list.get(r);
        //obs		int r = rand_int(0,l-1);
        //obs		edge f = first_face();
        //obs		while (r--) f = succ_face(f);
        return f;
    }
    
    /** Returns the nodes reached through all adjacent edges.
     * @param v The node for which adjacent nodes are requested.
     * @return List of adjacent nodes.
     */    
    public LinkedList adj_nodes(Node v){
        
        LinkedList result = new LinkedList();
        for(Edge e = v.first_adj_edge[0]; e != null; e = e.Succ_Adj_Edge(v)) result.addLast(e.opposite(v,e));
        //obs		Edge e;
        //obs		forall_adj_edges(e,v) result.append(opposite(v,e));
        return result;
    }
    
    /** Returns the adjacent edges of the given node.
     * @param v The node for which adjacent edges are requested.
     * @return List of adjacent edges.
     */    
    public LinkedList adj_edges(Node v){
        LinkedList result = new LinkedList();
        for(Edge e = v.first_adj_edge[0]; e != null; e = e.Succ_Adj_Edge(v)) result.addLast(e);
        //obs		Edge e;
        //obs		forall_adj_edges(e,v) result.append(e);
        return result;
    }
    
    /** Returns the size of the given face.
     * @param f The face for which size is requested.
     * @return The face size.
     */    
    public int size(Face f){ return f.sz; }
    
    /** Returns the first edge in the graph face.
     * @param f Face for which a first edge is requested.
     * @return The first edge.
     */    
    public Edge first_face_edge(Face f){ return f.head; }
/*
        public LinkedList triangulate_map(){
                Node v;
                Edge x;
                LinkList L;
                node_array<int>  marked(*this,0);
                if ( !make_map() )
                        System.err.println("TRIANGULATE_PLANAR_MAP: graph is not a map.");	//error_handler 1
                for(ListIterator nodes = v_list.listIterator(); nodes.hasNext(); ){
                        v = (Node)nodes.next();
//obs		forall_nodes(v,*this){
                        LinkedList El = adj_edges(v);
                        Edge e,e1,e2,e3;
//obs			forall(e1,El) marked[target(e1)]=1;
                        for(ListIterator edges1 = El.listIterator(); edges1.hasNext(); marked[edges1.next.target()]=1);
                        for(ListIterator edges2 = El.listIterator(); edges2.hasNext(); ){
//obs			forall(e,El){
                                e1 = edges2.next();
//obs				e1 = e;
                                e2 = face_cycle_succ(e1);
                                e3 = face_cycle_succ(e2);
                                while (e3.target() != v)
                                        // e1,e2 and e3 are the first three edges in a clockwise
                                        // traversal of a face incident to v and t(e3) is not equal
                                        // to v.
                                        if ( !marked[e2.target()] ){
                                                // we mark w and add the edge {v,w} inside F, i.e., after
                                                // dart e1 at v and after dart e3 at w.
                                                marked[e2.target()] = 1;
                                                L.append(x  = new_edge(e3,e1.source()));
                                                L.append(e1 = new_edge(e1,e3.source()));
                                                set_reversal(x,e1);
                                                e2 = e3;
                                                e3 = face_cycle_succ(e2);
                                        }
                                        else{
                                                // we add the edge {source(e2),target(e3)} inside F, i.e.,
                                                // after dart e2 at source(e2) and before dart
                                                // reversal_of[e3] at target(e3).
                                                e3 = face_cycle_succ(e3);
                                                L.append(x  = new_edge(e3,e2.source()));
                                                L.append(e2 = new_edge(e2,e3.source()));
                                                set_reversal(x,e2);
                                        }
                                        //end of while
                                } //end of stepping through incident faces
                        Edge e4 = v.first_adj_edge[0];
                        Node w = (e4 != null) ? opposite(v,e4) : null;
                        for(; w != null; e4 = Succ_Adj_Edge(e4,v)) marked[w] = 0;
//obs			forall_adj_nodes(w,v) marked[w] = 0;
                } // end of stepping through nodes
                return L;
        }
 */
    
    //	public LinkedList triangulate_planar_map(){
    //waiting for triangulate_map		LinkedList L = triangulate_map();
    //waiting for compute_faces		compute_faces();
    //		return L;
    //	}
    
    /** Returns the next edge in the face.
     * @param e The edge for which a successor is requested.
     * @param F The face for which the next edge is requested.
     * @return The next edge.
     */    
    public Edge next_face_edge(Edge e, Face F){
        //waiting for succ_face_edge		e = succ_face_edge(e);
        return (e == F.head) ? null : e;
    }
    
    /** Returns the next adjacent edge.
     * @param e Edge for which successor is requested.
     * @param v The node connected to these adjacent edges.
     * @return The next adjacent edge.
     */    
    public Edge adj_succ(Edge e, Node v){
        return e.succ_adj_edge[(v==e.source()) ? 0 : 1];
    }
    
    /** Returns the previous adjacent edge.
     * @param e Edge for which predecessor edge is requested.
     * @param v The node connected to these adjacent edges.
     * @return The next previous edge.
     */    
    public Edge adj_pred(Edge e, Node v){
        return e.pred_adj_edge[(v==e.source()) ? 0 : 1];
    }
    
    /** Returns the next adjacent edge. If end is reached, first edge is returned.
     * @param e Edge for which successor is requested.
     * @param v The node connected to these adjacent edges.
     * @return The next adjacent edge.
     */    
    public Edge cyclic_adj_succ(Edge e, Node v){
        Edge r = adj_succ(e,v);
        return (r != null) ? r :  v.first_adj_edge[0];
    }
    
    /** Returns the previous adjacent edge. If beginning is reached, return the last edge.
     * @param e Edge for which predecessor edge is requested.
     * @param v The node connected to these adjacent edges.
     * @return The next previous edge.
     */    
    public Edge cyclic_adj_pred(Edge e, Node v){
        Edge r = adj_pred(e,v);
        return (r != null) ? r : v.last_adj_edge[0];
    }
/*
        bool Is_Bidirected(const graph& G, edge_array<edge>& reversal)
        {
                // computes for every edge e = (v,w) in G its reversal reversal[e] = (w,v)
                // in G ( nil if not present). Returns true if every edge has a
                // reversal and false otherwise.
                int n = G.max_node_index();
                int count = 0;
                edge e,r;
                forall_edges(e,G) reversal[e] = 0;
                list<edge> El = G.all_edges();
                El.bucket_sort(0,n,&edge_ord2);
                El.bucket_sort(0,n,&edge_ord1);
                list<edge> El1 = G.all_edges();
                El1.bucket_sort(0,n,&edge_ord1);
                El1.bucket_sort(0,n,&edge_ord2);
                // merge El and El1 to find corresponding edges
                while (! El.empty() && ! El1.empty())
                {
                        e = El.head();
                        r = El1.head();
                        if (target(r) == source(e))
                                if (source(r) == target(e))
                                {
                                        reversal[e] = r;
                                        El1.pop();
                                        El.pop();
                                        count++;
                                }
                                else
                                        if (index(source(r)) < index(target(e)))
                                                El1.pop();
                                        else
                                                El.pop();
                        else
                                if (index(target(r)) < index(source(e)))
                                        El1.pop();
                                else
                                        El.pop();
                }
                return (count == G.number_of_edges()) ? true : false;
        }
 
        public void make_bidirected(LinkedList list1){
                // make graph bi-directed by inserting reversal edges
                // appends new edges to R
                edge_array<edge> rev(this,null);
                if (Is_Bidirected(this,rev))
                        return;						//graph is already bidirected
                // build list L of edges having no reversals
                LinkedList L;
                Edge e;
                for(ListIterator edges = e_list.listIterator(); edges.hasNext(); ){
                        e = edges.next();
//obs		forall_edges(e,this)
                        if (rev[e] == null) L.append(e);
                }
                // insert missing reversals
                for(ListIterator moreedges = L.listIterator(); moreedges.hasNext(); ){
                        e = moreedges.next();
//obs		forall(e,L){
                        edge r = this.new_edge(target(e),source(e));
                        list1.append(r);
                }
        }
 
        public boolean is_bidirected(){
                edge_array<edge> rev(*this,0);
                return Is_Bidirected(*this,rev);
        }
 */
/*
        public boolean Is_Map(Graph G){
//waiting for a definition		edge_array<edge> rev(G);
                if (!Is_Bidirected(G,rev)) return false;
                Edge x;
                for(ListIterator edges = G.e_list.listIterator(); edges.hasNext(); ){
                        x = (Edge)edges.next();
//obs		forall_edges(x,G)
                        Edge y = G.reversal(x);
                        if (x != G.reversal(y)) return false;
                        if (x.source() != y.target() || y.source() != x.target()) return false;
                }
                return true;
        }
 
        public boolean is_map(){ return Is_Map(this); }
 */
    /** Returns true if every edge has a reverse edge, false otherwise.
     * @return True if every edge has a reverse edge, false otherwise.
     */    
    public boolean make_map(){
        // computes for every edge e = (v,w) in G its reversal r = (w,v)
        // in G ( nil if not present). Returns true if every edge has a
        // reversal and false otherwise.
        //obs		int n     = max_node_index();
        int n	    = max_node_index;
        int count = 0;
        LinkedList El1 = all_edges();
        LinkedList El2 = El1;
        Edge a;
        for(ListIterator edges = El1.listIterator(); edges.hasNext();){
            a = (Edge)edges.next();
            a.rev = null;
        }
        //obs		forall(e,El1) e->rev = 0;
        bucket_sort(El1,0,n,map_edge_ord2);
        bucket_sort(El1,0,n,map_edge_ord1);
        bucket_sort(El2,0,n,map_edge_ord1);
        bucket_sort(El2,0,n,map_edge_ord2);
        
        //waiting for bucket_sort		El1.bucket_sort(0,n,&map_edge_ord2);
        //waiting for bucket_sort		El1.bucket_sort(0,n,&map_edge_ord1);
        //waiting for bucket_sort		El2.bucket_sort(0,n,&map_edge_ord1);
        //waiting for bucket_sort		El2.bucket_sort(0,n,&map_edge_ord2);
        // merge El1 and El2 to find corresponding edges
        
        while (!( El1.size() == 0) && !( El2.size() == 0)){
            Edge e = (Edge)El1.getFirst();
            Edge r = (Edge)El2.getFirst();
            if (r.target() == e.source())
                if (r.source() == e.target()){
                    e.rev = r;
                    El2.removeFirst();
                    El1.removeFirst();
                    count++;
                }
                else
                    if (r.source().index() < e.target().index())
                        El2.removeFirst();
                    else
                        El1.removeFirst();
            else
                if (r.target().index() < e.source().index())
                    El2.removeFirst();
                else
                    El1.removeFirst();
        }
        return (count == number_of_edges()) ? true : false;
    }
    
    /** Adds reverse edges of all edges in graph to the given list.
     * @param R List of reverse edges.
     */    
    public void make_map(LinkedList R){
        if (make_map()) return;
        LinkedList el = all_edges();
        Edge e;
        for(ListIterator edges = el.listIterator(); edges.hasNext(); ){
            e = (Edge)edges.next();
            //obs		forall(e,el){
            if (e.rev == null){
                Edge r = new_edge(e.target(),e.source());
                e.rev = r;
                r.rev = e;
                R.addLast(r);
                //obs				R.append(r);
            }
        }
    }
    
    
    
    /** Sort nodes in the given graph map; does nothing.
     * @param A Graph map of nodes.
     */    
    public void sort_nodes(GraphMap A){
        //obs		NA = &A;
        switch (A.elem_type_id()) {
            
            case INT_TYPE_ID:
                //waiting for sort_nodes					sort_nodes(int_array_cmp_nodes);
                //					break;
            case FLOAT_TYPE_ID:
                //waiting for sort_nodes					sort_nodes(float_array_cmp_nodes);
                //					break;
            case DOUBLE_TYPE_ID:
                sort_nodes(A.array_cmp);
                //waiting for sort_nodes					sort_nodes(double_array_cmp_nodes);
                break;
            default:
                System.err.println("G.sort_nodes(node_array<T>): T must be numerical.");	//error_handler 1
        }
    }
    
    
    /** Sort edges in the given graph map; does nothing.
     * @param A Graph map of edges.
     */    
    public void sort_edges(GraphMap A){
        //obs		NA = &A;
        switch (A.elem_type_id()) {
            
            case INT_TYPE_ID:
                //obs					sort_edges(int_array_cmp_edges);
                //obs					break;
            case FLOAT_TYPE_ID:
                //obs					sort_edges(float_array_cmp_edges);
                //obs					break;
            case DOUBLE_TYPE_ID:
                sort_edges(A.array_cmp);
                //obs					sort_edges(double_array_cmp_edges);
                break;
            default:
                System.err.println("G.sort_nodes(node_array<T>): T must be numerical.");	//error_handler 1
        }
    }
    
    
    /** Sort nodes in the given list.
     * @param vl List of nodes.
     */    
    public void sort_nodes(LinkedList vl){
        if (vl.size() != number_of_nodes())
            System.err.println("graph::sort_nodes(list<node>): illegal node list");	//error_handler 1
        v_list.clear();
        Node v;
        for(ListIterator nodes = vl.listIterator(); nodes.hasNext(); ){
            v = (Node)nodes.next();
            //obs		forall(v,vl) {
            if (v.owner != this)
                System.err.println("graph::sort_nodes(list<node>): illegal node list");	//error_handler 1
            v_list.addLast(v);
            //obs			v_list.append(v);
        }
    }
    
    
    /** Sort edges in the given list.
     * @param el List of edges.
     */    
    public void sort_edges(LinkedList el){
        Node v;
        Edge e;
        if (el.size() != number_of_edges())
            System.err.println("graph::sort_edges(list<edge>): illegal edge list");	//error_handler 1
        // clear all adjacency lists
        for(ListIterator nodes = v_list.listIterator(); nodes.hasNext(); ){
            v = (Node)nodes.next();
            //obs		forall_nodes(v,*this)
            for(int i=0; i<2; i++){
                v.first_adj_edge[i] = null;
                v.last_adj_edge[i] = null;
                v.adj_length[i] = 0;
            }
        }
        e_list.clear();
        for(ListIterator edges = el.listIterator(); edges.hasNext(); ){
            e = (Edge)edges.next();
            //obs		forall(e,el){
            if (e.term[0].owner != this)
                System.err.println("graph::sort_edges(list<edge>): edge not in graph");	//error_handler 1
            e_list.addLast(e);
            //obs			e_list.append(e);
            e.source().append_adj_edge(e,0,0);
            if (undirected)
                e.target().append_adj_edge(e,0,1);
            else
                e.target().append_adj_edge(e,1,1);
        }
    }
    
    private void QS_SWAP(LinkedList D, int index1, int index2){
        //obs	private void QS_SWAP(type,A,D,x,y){
        //obs		#define QS_SWAP(type,A,D,x,y)
        //obs		dlink* tmp_p = A[x];
        //obs		A[x] = A[y];
        //obs		A[y] = tmp_p;
        Object temp = D.remove(index1);
        D.add(index1, D.get(index2));
        D.remove(index2);
        D.add(index2,temp);
        //obs		type   tmp_d = D[x];
        //obs		D[x] = D[y];
        //obs		D[y] = tmp_d;
    }
    
    private void gen_quick_sort(LinkedList D, int left, int right, SortFunction sorter){
        //New quicksort function created by James Louis	:JL
        if (left > right)
            System.err.println("graph::gen_quick_sort: lower bound greater than upper bound");
        int d = right - left;
        if (d < 2)
            if (d == 0){return;}								//in case there is only one object
            else {
                if (sorter.is_less_than(D.get(left),D.get(right)))		//in case there is only two objects
                    QS_SWAP(D,left,right);
                return;
            }
        int i = left + 1;
        int j = right;
        while(true){
            while(sorter.is_less_than(D.get(i),D.get(left))) i++;
            while(!(sorter.is_less_than(D.get(left),D.get(j)))) j--;
            if (i < j) {QS_SWAP(D,i,j);}
            else break;
        }
        QS_SWAP(D,left,j);
        gen_quick_sort(D,left,j-1,sorter);
        gen_quick_sort(D,j+1,right,sorter);
    }
    
    /** Sorts nodes using the given sort function.
     * @param sorter Sort function.
     */    
    public void sort_nodes(SortFunction sorter){
        //obs	public void sort_nodes(int (*f)(const node&, const node&)){
        LinkedList vl = all_nodes();
        //obs		list<node> vl = all_nodes();
        
        //obs		vl.cmp_ptr = f;
        
        //obs  if (min_d < 1) min_d = 1; //min_d is 1 explicitly
        
        if (vl.size() <= 1) return;	//nothing to sort
        //obs  if (vl.length() <= 1) return;    // nothing to sort
        
        //obs  dlink** A = new dlink*[count+2];	//count is the same thing as list size
        //obs  dlink** left  = A+1;			//first object in the list
        //obs  dlink** right = A+count;		//last object in the list
        
        //obs  dlink** p = A+1;						//the variable p accomplishing nothing
        //obs  for(dlink* it = h; it; it = it->succ) *p++ = it;	//creates a reference list of objects in the list to be sorted
        
        //obs         GenPtr* D = new GenPtr[count+2];	//creates space for an array of references
        //obs         GenPtr* dp = D+1;			//allows iteration through the references in D
        
        //obs         for(dlink** q=left; q<=right; q++) *dp++ = (*q)->e;	//q iterates through A reference list
        
        gen_quick_sort(vl,0,vl.size()-1,sorter);
        
        //obs		D = null;
        //obs         delete[] D;
        
        //obs         if (min_d > 1)		//min_d is always 1
        //obs           gen_insertion_sort(left,right,right);		//function is never used
        
        //obs	A[0] = A[count+1] = null;
        //obs  A[0] = A[count+1] = 0;
        
        //obs  for(dlink** q=left; q<=right; q++)
        //obs  { (*q)->succ = *(q+1);
        //obs    (*q)->pred = *(q-1);
        //obs   }
        
        //obs  h = *left;	//setting the new head
        //obs  t = *right;	//setting the hew tail
        
        //obs		A = null;
        //obs  delete[] A;
        
        //obs		dlist::sort(1,0);	//replaced by the above code
        //obs		vl.sort(f);		//replaced by the above code
        sort_nodes(vl);
    }
    
    
    /** Sorts edges using the given sort function.
     * @param sorter Sort function.
     */    
    public void sort_edges(SortFunction sorter){
        //obs	public void sort_edges(int (*f)(const edge&, const edge&)){
        LinkedList el = all_edges();
        //obs		list<edge> el = all_edges();
        //obs		cmp_ptr = cmp;	//cmp is the compare function
        //obs  if (min_d < 1) min_d = 1;
        
        if (el.size() <= 1) return;    // nothing to sort
        //obs  if (length() <= 1) return;    // nothing to sort
        
        //obs  int type_id = (special) ? el_type_id() : UNKNOWN_TYPE_ID;
        
        //obs  dlink** A = new dlink*[count+2];
        //obs  dlink** left  = A+1;
        //obs  dlink** right = A+count;
        
        //obs  dlink** p = A+1;
        //obs  for(dlink* it = h; it; it = it->succ) *p++ = it;
        //obs         GenPtr* D = new GenPtr[count+2];
        //obs         GenPtr* dp = D+1;
        //obs         for(dlink** q=left; q<=right; q++) *dp++ = (*q)->e;
        gen_quick_sort(el,0,el.size()-1,sorter);
        //obs    gen_quick_sort(A,D,1,count,min_d);
        //obs         delete[] D;
        //obs         if (min_d > 1)		//min_d is always 1
        //obs           gen_insertion_sort(left,right,right);	//function is never run
        //obs  A[0] = A[count+1] = 0;
        
        //obs  for(dlink** q=left; q<=right; q++)
        //obs  { (*q)->succ = *(q+1);
        //obs    (*q)->pred = *(q-1);
        //obs   }
        
        //obs  h = *left;
        //obs  t = *right;
        
        //obs  delete[] A;
        //obs		dlist::sort(1,0);		//replaced by the above code
        //obs		el.sort(f);		//replaced by the above code
        sort_edges(el);
    }
    
    /** Sorts nodes.
     */    
    protected void sort_nodes(){
        //obs		GGG = this;	//this is not needed b/c of the SortFunction interface
        sort_nodes(node_comparer);
    }
    
    
    /** Sorts edges.
     */    
    protected void sort_edges(){
        //obs		GGG = this;	//this is not needed b/c of the SortFunction interface
        sort_edges(edge_comparer);
    }
    
    
    /** Sorts the given list.
     * @param the_list The list to be sorted.
     * @param i The index of the first value to be sorted.
     * @param j The index of the last value to be sorted.
     * @param ord_reference Ordering function.
     */    
    public void bucket_sort(LinkedList the_list, int i, int j, OrderingFunction ord_reference){
        //bucket sort algorithm for lists. Created by JL
        //		ord_ptr = f;
        if((the_list == null)||(the_list.size() <= 0)) return;
        //obs		if (h==nil) return; // empty list
        int n = j-i+1;
        LinkedList bucket = new LinkedList();
        //obs		list_item* bucket= new list_item[n+1];
        if (bucket == null)
            //obs		if (bucket == 0)
            System.err.println(("list::bucketsort: cannot allocate" + n + "buckets (out of memory)"));	//error_handler 1
        
        //obs		list_item* stop = bucket + n;
        //obs		list_item* p;
        //obs		list_item q;
        //obs		list_item x;
        
        //obs		for(p=bucket;p<=stop;p++)  *p = 0;
        
        Object this_item;
        for(ListIterator the_list_iterator = the_list.listIterator();
        the_list_iterator.hasNext(); )
            //obs  while (h)
        {
            this_item = the_list_iterator.next();
            //obs			x = h;
            //obs			h = h->succ;
            int k = ord_reference.order(this_item);
            //obs			int k = ord(x->e);
            if (k >= i && k <= j) {
                bucket.add(k,this_item);
                //obs				p = bucket+k-i;
                //obs				x->pred = *p;
                //obs				if (*p) (*p)->succ = x;
                //obs				*p = x;
            }
            else
                System.err.println("bucket_sort: value" + k + "out of range");	//error_handler 4
        }
        
        //obs		for(p=stop; *p==0; p--);
        
        // now p points to the end of the rightmost non-empty bucket
        // make it the new head  of the list (remember: list is not empty)
        
        //obs		t = *p;
        //obs		t->succ = nil;
        
        //obs for(q = *p; q->pred; q = q->pred); // now q points to the start of this bucket
        
        // link buckets together from right to left:
        // q points to the start of the last bucket
        // p points to end of the next bucket
        
        //obs		while(--p >= bucket)
        //obs		if (*p)
        //obs			{ (*p)->succ = q;
        //obs 		q->pred = *p;
        //obs			for(q = *p; q->pred; q = q->pred);
        //obs		}
        
        //obs h = q;   // head = start of leftmost non-empty bucket
        
        the_list = bucket;
        bucket = null;
    }
    
    /** Sorts the given list.
     * @param the_list The list to be sorted.
     * @param ord Ordering function.
     */    
    public void bucket_sort(LinkedList the_list, OrderingFunction ord)

    {
        if ((the_list == null)||(the_list.size() < 1)) return;		//empty list
        int i = ord.order(the_list.getFirst());
        int j = i;
        for(int p = 1; p < the_list.size(); p++)
        {
            int o = ord.order(the_list.get(p));
            if ( o < i ) i = o;
            if ( o > j ) j = o;
        }
        
        bucket_sort(the_list,i,j,ord);
    }
    
    /** Sorts the node list between the given indecies.
     * @param l The index of the first value to be sorted.
     * @param h The index of the last value to be sorted.
     * @param ord Ordering function.
     */    
    public void bucket_sort_nodes(int l, int h, OrderingFunction ord)
    {
        LinkedList vl = all_nodes();
        bucket_sort(vl,l,h,ord);
        sort_nodes(vl);
    }
    
    /** Sorts the edge list between the given indecies.
     * @param l The index of the first value to be sorted.
     * @param h The index of the last value to be sorted.
     * @param ord Ordering function.
     */    
    public void bucket_sort_edges(int l, int h, OrderingFunction ord)
    {
        LinkedList el = all_edges();
        bucket_sort(el,l,h,ord);
        sort_edges(el);
    }
    
    /** Sorts the node list.
     * @param ord Ordering function.
     */    
    public void bucket_sort_nodes(OrderingFunction ord)
    {
        LinkedList vl = all_nodes();
        bucket_sort(vl,ord);
        sort_nodes(vl);
    }
    
    /** Sorts the edge list.
     * @param ord Ordering function.
     */    
    public void bucket_sort_edges(OrderingFunction ord)
    {
        LinkedList el = all_edges();
        bucket_sort(el,ord);
        sort_edges(el);
    }
    
    /** Sorts the node list in the given map.
     * @param A The graph map to be sorted.
     */    
    public void bucket_sort_nodes(GraphMap A)
    {
        switch (A.elem_type_id()) {
            
            case INT_TYPE_ID:
                bucket_sort_nodes(A.array_ord_node);
                break;
            default:
                System.err.println("G.bucket_sort_nodes(node_array<T>): T must be integer.");//error_handler 1
        }
    }
    
    /** Sorts the edge list in the given map.
     * @param A The graph map to be sorted.
     */    
    public void bucket_sort_edges(GraphMap A)
    {
        switch (A.elem_type_id()) {
            
            case INT_TYPE_ID:
                bucket_sort_edges(A.array_ord_edge);
                break;
            default:
                System.err.println("G.bucket_sort_edges(edge_array<T>): T must be integer.");	//error_handler 1
        }
    }
    
    /*Debugging and insight functions*/
    /** Does nothing.
     * @param N The node to be examined.
     * @param S The string used for comment.
     */    
    public void touch(Node N, String S){}
    /** Does nothing.
     * @param E The edge to be examined.
     * @param S The string used for comment.
     */    
    public void touch(Edge E, String S){}
    /** Sets comment string; does nothing.
     * @param S The string used for comment.
     */    
    public void comment(String S){}
    /** Returns true.
     * @return Always true.
     */    
    public boolean query(){ return true; }
    /** Returns true.
     * @param N Source node queried.
     * @param M Target node queried.
     * @return Always true.
     */    
    public boolean query(Node N, Node M){ return true; }
    /** Returns true.
     * @param E Edge queried.
     * @return Always true.
     */    
    public boolean query(Edge E){ return true; }
    
    
    /** Prints graph to the supplied writer.
     * @param s Header string.
     * @param out The writer to which display is written.
     */    
    public void print(String s, Writer out) {
        try{
            Node v;
            out.write(s + '\n');
            for(ListIterator nodes = v_list.listIterator(); nodes.hasNext(); ) {
                v = (Node)nodes.next();
                v.print_node(out);
                out.write(" : ");
                for(Edge e = v.first_adj_edge[0]; e != null; e = e.Succ_Adj_Edge(v))
                    e.print_edge(out);
                out.write('\n');
            }
            out.write('\n');
        }catch(IOException e){e.printStackTrace();}
    }
    
    /** Prints graph to the supplied writer.
     * @param out The writer to which display is written.
     */    
    public void print(Writer out){print("",out);}
    
}//End of Graph