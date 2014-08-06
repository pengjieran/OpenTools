package arithmetic.id3;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ListIterator;

import arithmetic.shared.AugCategory;
import arithmetic.shared.DisplayPref;
import arithmetic.shared.Error;
import arithmetic.shared.Edge;
import arithmetic.shared.GetEnv;
import arithmetic.shared.Globals;
import arithmetic.shared.LogOptions;
import arithmetic.shared.MLJ;
import arithmetic.shared.Node;

/** CatGraph is a directed graph whose nodes have references to Categorizers.
 * Edges are labelled with the category number they match. The CatGraph can be
 * either complete or sparse. This is decided at the time of creation and
 * cannot be changed thereafter.								<P>
 *
 * For complete graphs:									<P>
 * Each node's first edge must be labelled either UNKNOWN_CATEGORY_VAL or
 * FIRST_CATEGORY_VAL. Each additional edge must be labelled with the next
 * category in ascending order.								<P>
 *
 * For sparse graphs:									<P>
 * A node may have zero or more children. Detection of a child can be done
 * using the get_child_if_exists() function, which returns a reference to the
 * child node if it exists, and otherwise returns a NULL reference.            <P>
 *
 * @author James Louis	2/25/2001	Ported to Java.
 * @author Jay DeSouza	8/13/97	Added handling for sparse graphs
 * @author Richard Long	8/20/93	Initial revision (.c)
 * @author Richard Long	8/19/93	Initial revision (.h)
 */
public class CatGraph {
    /** The CGraph object containing the graph used for this CatGraph.
     */
    protected CGraph cGraph;
    /** TRUE if the graph is allocated, FALSE if the graph is set to NULL.
     */
    boolean graphAlloc;
    /** TRUE if the graph is sparsely generated.
     */
    boolean isSparse;
    /** Logging options for this class.
     */
    protected LogOptions logOptions = new LogOptions();
    /** Distribution display help string.
     */
    protected String distDispHelp = "This option specifies whether to display the "+
    "distribution of instances of the nodes in the graph while displaying. ";
    /** The default value for distribution display. The default is FALSE.
     */
    protected boolean defaultDistDisp = false;
    
    
    /** Sets the logging level for this object.
     * @param level	The new logging level.
     */
    public void set_log_level(int level){logOptions.set_log_level(level);}
    
    /** Returns the logging level for this object.
     * @return The logging level for this object.
     */
    public int  get_log_level(){return logOptions.get_log_level();}
    
    /** Sets the stream to which logging options are displayed.
     * @param strm	The stream to which logs will be written.
     */
    public void set_log_stream(Writer strm)
    {logOptions.set_log_stream(strm);}
    
    /** Returns the stream to which logs for this object are written.
     * @return The stream to which logs for this object are written.
     */
    public Writer get_log_stream(){return logOptions.get_log_stream();}
    
    /** Returns the LogOptions object for this object.
     * @return The LogOptions object for this object.
     */
    public LogOptions get_log_options(){return logOptions;}
    
    /** Sets the LogOptions object for this object.
     * @param opt	The new LogOptions object.
     */
    public void set_log_options(LogOptions opt)
    {logOptions.set_log_options(opt);}
    
    /** Sets the logging message prefix for this object.
     * @param file	The file name to be displayed in the prefix of log messages.
     * @param line	The line number to be displayed in the prefix of log messages.
     * @param lvl1 The log level of the statement being logged.
     * @param lvl2	The level of log messages being displayed.
     */
    public void set_log_prefixes(String file, int line,int lvl1, int lvl2)
    {logOptions.set_log_prefixes(file, line, lvl1, lvl2);}
    
    /** Constructor.
     * @param isGraphSparse	TRUE if this CatGraph is sparsely populated. FALSE
     * otherwise.
     */
    public CatGraph(boolean isGraphSparse) {
        cGraph = new CGraph();
        isSparse = isGraphSparse;
        graphAlloc = true;
        logOptions = new LogOptions();
        logOptions.LOG(3, "CatGraph::CatGraph(Bool isGraphSparse): isSparse = "
        + isSparse + " is_sparse() = " + is_sparse() + '\n');
    }
    
    /** Constructor.
     * @param aGraph		CGraph on which all operations will take place. It
     * should remain unchanged while a part of this
     * CatGraph object.
     * @param isGraphSparse	TRUE if this CatGraph is sparsely populated. FALSE
     * otherwise.
     */
    public CatGraph(CGraph aGraph, boolean isGraphSparse) {
        cGraph = aGraph;
        isSparse = isGraphSparse;
        graphAlloc = false;
        logOptions = new LogOptions();
        logOptions.LOG(6, "CatGraph::CatGraph(CGraph, Bool): isSparse = "
        + isSparse + " is_sparse() = " + is_sparse() + '\n');
    }
    
    /** Checks if this CatGraph is sparsely populated.
     * @return TRUE if this CatGraph is sparsely populated.
     */
    public boolean is_sparse() {
        return isSparse;
    }
    
    /** Returns the number of Nodes in this CatGraph.
     * @return The number of Nodes in this CatGraph.
     */
    public int num_nodes() {
        return cGraph.number_of_nodes();
    }
    
    /** Returns the number of leaves in this CatGraph.
     * @return The number of leaves in this CatGraph.
     */
    public int num_leaves() {
        return cGraph.num_leaves();
    }
    
    /** Returns the CGraph stored in this CatGraph object.
     * @return The CGraph stored in this CatGraph object.
     */
    public CGraph get_graph() {
        return cGraph;
    }
    
    /** Returns the number of attributes stored in this CatGraph.
     * @return The number of attributes stored in this CatGraph.
     * @param maxAttr	The maximum number of attributes stored in the CatGraph.
     */
    public int num_attr(int maxAttr) {
        return cGraph.num_attr(logOptions.get_log_options() , maxAttr);
    }
    
    /** Creates a new Node.
     * @return The new Node.
     * @param cat		The Categorizer to be stored in the new Node.
     * @param level The level for the new node placement.
     */
    public Node create_node(NodeCategorizer[] cat, int level) {
        NodeInfo nodeInfo = cGraph.get_prototype() .create_my_type(level);
        nodeInfo.set_categorizer(cat);
        MLJ.ASSERT(cat[0] == null, "CatGraph::create_node: cat != NULL");
        return cGraph.new_node(nodeInfo);
    }
    
    /** Creates a directed Edge from Node "from" to Node "to". Assigns the Edge
     * the value "edgeLabel" and gets ownership of the AugCategory.             <P>
     * For non-sparse graphs:                                                 <BR>
     * The category given must be the category following the category for
     * the previous Edge.                                                       <BR>
     * The first Edge must have label UNKNOWN_CATEGORY_VAL or
     * FIRST_CATEGORY_VAL.
     *
     * @param from		The Node that is the source of the directed Edge.
     * @param to		The Node that is the destination of the directed Edge.
     * @param edgeLabel	The category to be assigned to the new Edge.
     */
    public void connect(Node from,Node to,
    AugCategory edgeLabel) {
        // grab these values in advance.  Makes debugging easier too.
        NodeInfo fromInfo =(NodeInfo) get_graph() .entry(from);
        NodeInfo toInfo =(NodeInfo) get_graph() .entry(to);
        NodeCategorizer fromCat = fromInfo.get_categorizer();
        NodeCategorizer toCat = toInfo.get_categorizer();
        
        if (!fromCat.in_graph())
            Error.fatalErr("CatGraph::connect: the \'from\' node "
            +fromCat.description()
            + " is not in the graph");
        if (!toCat.in_graph())
            Error.fatalErr("CatGraph::connect: the \'to\' node "
            +toCat.description()
            + " is not in the graph");
        logOptions.LOG(8, "CatGraph::connect: isSparse = " +isSparse
        + " is_sparse() = " +is_sparse()
        + " !is_sparse() = " +!is_sparse() + '\n');
        if (!is_sparse()) {
            if (from.outdeg() == 0 && edgeLabel.num() != Globals.FIRST_CATEGORY_VAL &&
            edgeLabel.num() != Globals.UNKNOWN_CATEGORY_VAL)
                Error.fatalErr("CatGraph::connect: The first edge must have label "
                +Globals.FIRST_CATEGORY_VAL+ " or "
                +Globals.UNKNOWN_CATEGORY_VAL+ ".  Given label was " +edgeLabel.num());
            if (from.outdeg() != 0 &&
            edgeLabel.num() != ((AugCategory) cGraph.inf(from.last_adj_edge())) .num() + 1)
                Error.fatalErr("CatGraph::connect: Edge label "
                + ((AugCategory) cGraph.inf(from.last_adj_edge())) .num() + 1
                + " must follow edge label "
                + ((AugCategory) cGraph.inf(from.last_adj_edge())) .num()
                + "; got edge label " + edgeLabel.num());
        }
        for(Edge edgePtr = from.First_Adj_Edge(0) ; edgePtr != null ; edgePtr = edgePtr.Succ_Adj_Edge(from))
            if ((cGraph.inf(edgePtr)) == edgeLabel) {
                Error.err("CatGraph::connect: Attempting to add a duplicate edge: "
                +edgeLabel
                + ".  Edge " + (cGraph.inf(edgePtr)) + " already exists. ");
            }
        cGraph.new_edge(from, to, edgeLabel);
        edgeLabel = null;
    }
    
    /** Returns the number of children the specified Node has.
     * @return The number of children the specified Node has.
     * @param parent	The specified Node.
     */
    public int num_children(Node parent) {
        return parent.outdeg();
    }
    
    /** Returns the NodeCategorizer stored in the specified Node.
     * @return The NodeCategorizer stored in the specified Node.
     * @param nodePtr The Node containing the NodeCategorizer.
     */
    public NodeCategorizer get_categorizer(Node nodePtr) {
        return((NodeInfo) cGraph.inf(nodePtr)) .get_categorizer();
    }
    
    /** Checks if specified Node is in this NatGraph object.
     * @return TRUE if the Node is a node that is in the CatGraph. Otherwise,
     * returns FALSE.
     * @param node			The Node to be looked for.
     * @param fatalOnFalse	TRUE if an error message should be displayed if the
     * specified Node is not in the CatGraph, FALSE
     * otherwise.
     */
    public boolean check_node_in_graph(Node node,
    boolean fatalOnFalse) {
        Node node2;
        for(node2 = cGraph.first_node() ; node2 == null ; node2 = cGraph.succ_node(node2)) {
            if (node == node2)
                return true;
        }
        if (fatalOnFalse) {
            if (node == null)
                Error.fatalErr("CatGraph::check_node_in_graph: The given node 0x0 "
                + "is not in this graph");
            else
                Error.fatalErr("CatGraph::check_node_in_graph: The given node "
                +node+ " is not in this graph");
        }
        return false;
    }
    
    /** Creates a postscript file of the receiving graph via a dot description.
     * This method applies only to the DotGraph DisplayPref.
     * @param stream		The stream to be written to.
     * @param dp			The preferences for display.
     * @param hasNodeLosses TRUE if this Node contains loss values.
     * @param hasLossMatrix	TRUE if this CatGraph has a loss matrix assigned to
     * it, FALSE otherwise.
     */
    protected void process_DotGraph_display(Writer stream,
    DisplayPref dp,
    boolean hasNodeLosses,
    boolean hasLossMatrix) {
        try {
            FileWriter tempfile = new FileWriter("catgraph.dot-graph");
            convertToDotFormat(tempfile, dp, hasNodeLosses, hasLossMatrix);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    /** Creates a postscript file of the receiving graph in a graphical form.
     * This method applies only to the DotPostscript DisplayPref.
     * @param stream		The stream to be written to.
     * @param dp			The preferences for display.
     * @param hasNodeLosses TRUE if this Node contains loss values.
     * @param hasLossMatrix	TRUE if this CatGraph has a loss matrix assigned to
     * it, FALSE otherwise.
     */
    protected void process_DotPostscript_display(Writer stream,
    DisplayPref dp,
    boolean hasNodeLosses,
    boolean hasLossMatrix) {
        try {
            //Originally this used a temporary file name for dot file output -JL
            FileWriter tempfile = new FileWriter("catgraph.dot-in");
            convertToDotFormat(tempfile, dp, hasNodeLosses, hasLossMatrix);
            tempfile.close();
            
            //This is a system call to the dot program -JL
            //TmpFileName tmpfile2(".dot-out");
            //if(system(*GlobalOptions::dotUtil + " -Tps " + tmpfile1 + " -o " + tmpfile2))
            //Mcerr << "CatGraph::display: Call to dot failed." << endl;
            //stream.include_file(tmpfile2);     // this feeds the correct output
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /** Prints a representation of the CatGraph to the specified stream, using the
     * Categorizer descriptions to label the nodes. This mehtod takes into account
     * the different combinations of streams and display preferences. See
     * DisplayPref.java for more details with regards to valid combinations,
     * functionality, and options.
     * @param hasNodeLosses TRUE if this Node contains loss values.
     * @param hasLossMatrix	TRUE if this CatGraph has a loss matrix assigned to
     * it, FALSE otherwise.
     * @param stream		The stream to be written to.
     * @param dp			The preferences for display.
     */
    // The hasNodeLosses is first because it's non-default here.
    // The default is handled by the virtual base class that calls
    //    the header file, which in turn passes a FALSE here.
    public void display(boolean hasNodeLosses, boolean hasLossMatrix,
    Writer stream, DisplayPref dp) {
        // XStream is a special case--the only option so far where you don't
        // just send something to the MLCOStream.
        //   if (stream.output_type() == XStream) {
        //      process_XStream_display(dp, hasNodeLosses, hasLossMatrix);
        //      return;
        //   }
        try {
            // Other cases are depend only on DisplayPreference
            switch (dp.preference_type()) {
                case  DisplayPref.ASCIIDisplay:
                    // Note that we're calling get_fstream and not get_stream to avoid
                    //   overflow when cout is auto-wrapped (since it's a strstream).
                    //   This means that there is no wrapping here.
                    cGraph.print(stream);
                    stream.flush();
                    break;
                    
                case  DisplayPref.DotPostscriptDisplay:
                    process_DotPostscript_display(stream, dp, hasNodeLosses, hasLossMatrix);
                    break;
                    
                case  DisplayPref.DotGraphDisplay:
                    process_DotGraph_display(stream,dp, hasNodeLosses, hasLossMatrix);
                    break;
                    
                default:
                    Error.fatalErr("CatGraph::display: Unrecognized output type: "
                    + dp.toString());
                    
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /***************************************************************************
  Converts the representation of the graph to dot format and directs it to
the specified stream.
@param stream		The stream to be written to.
@param pref			The preferences for display.
@param hasNodeLosses
@param hasLossMatrix	TRUE if this CatGraph has a loss matrix assigned to
                                        it, FALSE otherwise.
     ***************************************************************************
   protected void convertToDotFormat(Writer stream,
          DisplayPref pref,
          boolean hasNodeLosses,
          boolean hasLossMatrix)
   {
      try
      {
         stream.write(convertToDotFormat(pref,hasNodeLosses,hasLossMatrix));
      } catch(IOException e)
      {
         e.printStackTrace();
      }
   }
     */
    
    /** Returns a representation of the graph to dot format.
     * @param stream		The stream to be written to.
     * @param pref			The preferences for display.
     * @param hasNodeLosses TRUE if this Node contains loss values.
     * @param hasLossMatrix	TRUE if this CatGraph has a loss matrix assigned to
     * it, FALSE otherwise.
     */
    protected void convertToDotFormat(Writer stream,
    DisplayPref pref,
    boolean hasNodeLosses,
    boolean hasLossMatrix) {
        try {
            GetEnv getenv = new GetEnv();
            boolean displayDistr = getenv.get_option_bool("DIST_DISP", defaultDistDisp, distDispHelp, true);
            
            // send header and open brace to stream.
            stream.write("/* Machine generated dot file */ \n\n"
            +"digraph G { \n\n");
            
            
            // Preferences that only make sense for the Postscript Display
            
            if (pref.preference_type() == DisplayPref.DotPostscriptDisplay) {
                process_DotPoscript_preferences(stream, pref);
            }
            
            // We add each node to the dot output.
            Node v = null;
            for(ListIterator NLI = cGraph.nodeIterator() ; NLI.hasNext() ;) {
                v =(Node) NLI.next();
                
                stream.write("/*  node " + v.index() +":  */\n");
                stream.write("node_" + v.index() +" [label=\"" + get_categorizer(v).description());
                
                if (hasNodeLosses) {
                    stream.write("\\nEstimated ");
                    if (hasLossMatrix)
                        stream.write("losses: ");
                    else
                        stream.write("error: ");
                    NodeLoss na = get_categorizer(v) .get_loss();
                    if (Math.abs(na.totalWeight) < MLJ.realEpsilon)
                        stream.write("?");
                    else {
                        double loss = na.totalLoss/  na.totalWeight;
                        stream.write(MLJ.numberToString(loss*100, 2) +"% (" + na.totalWeight +')');
                    }
                }
                
                if (displayDistr && get_categorizer(v).has_distr())
                    stream.write("\\n" + get_categorizer(v).get_distr());
                
                stream.write("\"]\n");
                
                Edge e;
                for(ListIterator ELI = cGraph.edgeIterator() ; ELI.hasNext() ;) {
                    e =(Edge) ELI.next();
                    stream.write("node_" + v.index() +"->" +"node_"
                    + e.target() .index() +" [label=\""
                    +((AugCategory) cGraph.inf(e)) .description() +"\"] \n");
                }
                MLJ.ASSERT(v != null, "CatGraph::convertToDotFormat"
                + "(DisplayPref,boolean,boolean): v equals NULL");
            }
            MLJ.ASSERT(v == null, "CatGraph::convertToDotFormat"
            + "(DisplayPref,boolean,boolean): v does not equal NULL");
            
            stream.write("}\n");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    /** Gets the preferences from the DisplayPref class. This method applies only to
     * DotPostscript display type.
     * @param stream The Writer to which the CatGraph will be displayed.
     * @param pref The preferences to use in displaying the CatGraph.
     */
    void process_DotPoscript_preferences(Writer stream,
    DisplayPref pref) {
        // Remember: These are preferences that only make sense for the
        // Postscript Display
        float pageSizeX = pref.get_page_size_x();
        float pageSizeY = pref.get_page_size_y();
        float graphSizeX = pref.get_graph_size_x();
        float graphSizeY = pref.get_graph_size_y();
        int orientation;
        orientation = pref.get_orientation();
        int ratio;
        ratio = pref.get_ratio();
        
        try{
            stream.write("page = \"" + pageSizeX + ","
            + pageSizeY + "\";\n"
            + "size = \"" + graphSizeX + ","
            + graphSizeY + "\";\n");
            if (orientation == pref.DisplayLandscape)
                stream.write("orientation = landscape;\n");
            else
                stream.write("orientation = portrait;\n");
            if (ratio == pref.RatioFill)
                stream.write("ratio = fill;\n");
        }catch(IOException e){e.printStackTrace();}
    }
}