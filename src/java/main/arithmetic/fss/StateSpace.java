package arithmetic.fss;

import java.io.Writer;
import java.util.LinkedList;
import java.util.ListIterator;

import arithmetic.shared.DisplayPref;
import arithmetic.shared.DoubleRef;
import arithmetic.shared.Globals;
import arithmetic.shared.Graph;
import arithmetic.shared.LogOptions;
import arithmetic.shared.MLJ;
import arithmetic.shared.Node;

public class StateSpace extends Graph{

//State objects are stored in graph. State replaces StateType

//obs   NodePtr initialNode;
//obs   NodePtr finalNode;
   private Node initialNode;
   private Node finalNode;

   private int numStatesTotal;
   private int numStatesExpanded;


/** Logging options for this class. **/
   protected LogOptions logOptions = new LogOptions();

/***************************************************************************
  Sets the logging level for this object.
@param level	The new logging level.
***************************************************************************/
   public void set_log_level(int level){logOptions.set_log_level(level);}

/***************************************************************************
  Returns the logging level for this object.
***************************************************************************/
   public int  get_log_level(){return logOptions.get_log_level();}

/***************************************************************************
  Sets the stream to which logging options are displayed.
@param strm	The stream to which logs will be written.
***************************************************************************/
   public void set_log_stream(Writer strm)
      {logOptions.set_log_stream(strm);}

/***************************************************************************
  Returns the stream to which logs for this object are written.
@return The stream to which logs for this object are written.
***************************************************************************/
   public Writer get_log_stream(){return logOptions.get_log_stream();}

/***************************************************************************
  Returns the LogOptions object for this object.
@return The LogOptions object for this object.
***************************************************************************/
   public LogOptions get_log_options(){return logOptions;}

/***************************************************************************
  Sets the LogOptions object for this object.
@param opt	The new LogOptions object.
***************************************************************************/
   public void set_log_options(LogOptions opt)
      {logOptions.set_log_options(opt);}

/***************************************************************************
  Sets the logging message prefix for this object.
@param file	The file name to be displayed in the prefix of log messages.
@param line	The line number to be displayed in the prefix of log messages.
@param lvl1 The log level of the statement being logged.
@param lvl2	The level of log messages being displayed.
***************************************************************************/
   public void set_log_prefixes(String file, int line,int lvl1, int lvl2)
      {logOptions.set_log_prefixes(file, line, lvl1, lvl2);}

/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private StateSpace(StateSpace source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(StateSpace source){}


//obs StateSpace<StateType>::StateSpace()
   public StateSpace()
   {
      super();
//obs    GRAPH<StateType*,Real>(),
      numStatesTotal = 0;
      numStatesExpanded = 0;
   }

//obs void StateSpace<StateType>::set_final_node(NodePtr node)
void set_final_node(Node node)
{
// Can't use get_state because set_final_state is non-const.
   finalNode = node;
   ((State)inf(node)).set_final_state();
}

//obs int StateSpace<StateType>::get_next_eval_num()
   public int get_next_eval_num()
   {
      // Skip the first one that is always best anyway
      if (numStatesTotal > 0)
         logOptions.DRIBBLE(".");
//obs         DRIBBLE("." << flush);
      return numStatesTotal++;
   }

//obs NodePtr StateSpace<StateType>::create_node(StateType*& state)
public Node create_node(State state)
{
   Node node = new_node(state);

   // mark state as in graph
   state.set_node(node);

   // log information on new state
   logOptions.LOG(3,"added node "+state+": ");
//   logOptions.IFLOG(3, state.display_stats(get_log_stream(), null));
   logOptions.LOG(3,"\n");

   state = null;
   return node;
}

//obs template<class StateType>
//obs NodePtr StateSpace<StateType>::find_state(const StateType& state) const
   public Node find_state(Object state)
   {
      Node nodePtr;
//obs   forall_nodes(nodePtr, *this)
      for(ListIterator nodes = v_list.listIterator(); nodes.hasNext();)
      {
         nodePtr =(Node)nodes.next();
         if (get_state(nodePtr) == state)
         return nodePtr;
      }
      return null;
   }

   public void set_initial_node(Node node)
   {
      initialNode = node;
   }


//obs template<class StateType>
//obs void StateSpace<StateType>::connect(NodePtr from, NodePtr to, Real edge)
//  new_edge(Node,Node,double) needs to be created.-JL
   public void connect(Node from, Node to, double edge)
   {
      new_edge(from, to, new DoubleRef(edge));
   }


//obs template<class StateType>
//obs void StateSpace<StateType>::insert_states(StateType& parent,
//obs					  DblLinkList<StateType*>* states)
   public void insert_states(State parent,LinkedList states)
   {
//obs   DLLPix<StateType*> statePix(*states, 1);

      double parentEval = parent.get_fitness();
      Node parentNode = parent.get_node(this);
      MLJ.ASSERT(parentNode != null,"StateSpace.insert_states:parentNode == null.");
      logOptions.LOG(3, "Adding children of "+parent+"\n");

//obs   while(statePix) {
      for(int i = 0; i < states.size();)
      {
      // add to graph if state is not there already
         State state = (State)states.get(i);
         Node nodePtr = state.get_node(this);
         if(nodePtr != null)
         {
            // delete from list (PIX will be advanced automatically)
            states.remove(i);
         }
         else
         {
	 // add to graph.  Then advance PIX (because we didn't in the for
	 // loop).
            double childEval = state.get_fitness();
            Node childNode = create_node(state);
            connect(parentNode, childNode, childEval - parentEval);
            i++;
         }
      }
   }

//obs template<class StateType>
//obs StateType& StateSpace<StateType>::get_state(NodePtr node) const
public State get_state(Node node)
{
   return (State)inf(node);
}

//obs template<class StateType>
//obs StateType& StateSpace<StateType>::get_initial_state() const
public State get_initial_state()
{
   return get_state(initialNode);
}

//obs template<class StateType>
//obs StateType& StateSpace<StateType>::get_final_state() const
public State get_final_state()
{
   return get_state(finalNode);
}

//obs template<class StateType>
//obs int StateSpace<StateType>::get_num_states() const
public int get_num_states()
{
   return numStatesTotal;
}



public void display_initial_state(){display_initial_state(Globals.Mcout);}
public void display_initial_state(Writer stream)
{
   get_initial_state().display(stream);
}

public void display_final_state(){display_final_state(Globals.Mcout);}
public void display_final_state(Writer stream)
{
   get_final_state().display(stream);
}

public void display(){display(Globals.Mcout,DisplayPref.defaultDisplayPref);}
public void display(Writer stream){display(stream,DisplayPref.defaultDisplayPref);}
public void display(Writer stream, DisplayPref dp)
{
/*   // XStream is a special case--the only option so far where you don't
   // just send something to the MLCOStream.
   if (stream.output_type() == XStream) {
      process_XStream_display(dp);
      return;
   }

   // Other cases are depend only on DisplayPreference
   switch (dp.preference_type()) {

   case DisplayPref::ASCIIDisplay:
      print(stream.get_stream());    // this is Leda's print routine
      stream << flush;
      break;

   case DisplayPref::DotPostscriptDisplay:
      process_DotPostscript_display(stream, dp);
      break;

   case DisplayPref::DotGraphDisplay:
      process_DotGraph_display(stream,dp);
      break;

   default:
      Error.fatalErr("StateSpace<>::display: Unrecognized output type: "
          +stream.output_type());

   }
*/}


protected void finalize()
{
   free();
}

protected void free()
{
   Node nodePtr;
//obs   forall_nodes(nodePtr, *this)
//   for(ListIterator nodes = v_list.listIterator(); nodes.hasNext();)
//      ((Node)nodes.next()).inf() = null;
   clear();
}


/*

   // Private method
   void process_DotPoscript_preferences(MLCOStream& stream,
					const DisplayPref& pref) const;
protected:
   // Protected methods
  
   virtual void process_XStream_display(const DisplayPref& dp) const;
   virtual void process_DotPostscript_display(MLCOStream& stream,
					      const DisplayPref& dp) const;
   virtual void process_DotGraph_display(MLCOStream& stream,
					 const DisplayPref& dp) const;
   virtual void convertToDotFormat(MLCOStream& stream,
				   const DisplayPref& pref) const;
public:
*/
}