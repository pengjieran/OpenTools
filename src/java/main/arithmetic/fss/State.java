package arithmetic.fss;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

import arithmetic.shared.Globals;
import arithmetic.shared.LogOptions;
import arithmetic.shared.MLJ;
import arithmetic.shared.Node;

abstract public class State {

   public static final int NOT_EVALUATED = -1;
   public static final double MIN_EVALUATION = - Globals.REAL_MAX;


   // Member data (also see protected data)
   private boolean nodeNotCached;
   private Node nodePtr;   

//obs   protected LocalInfo stateInfo; // State specific info (owned by the State).
//obs   protected GlobalInfo globalInfo; // Shared by all states.
   protected Object stateInfo; // State specific info (owned by the State).
   protected Object globalInfo; // Shared by all states.
   protected double fitness;	 // Result of eval() function.
   protected double stdDev;          // standard deviation of fitness.
   protected int localEvalNum;	 // This should be updated in the derived class.
   protected String description;  // ASCII description (used in graph).
   protected String graphOptions; // Optional parameters when printing graph.
   protected double complexity;      // Complexity metric for this state.
   protected int evalCost;         // total cost of evaluations so far   


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
   private State(State source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(State source){}




//obs   public State(LocalInfo*& initStateInfo, const GlobalInfo& gI)
   public State(Object initStateInfo, Object gI)
   {
      nodeNotCached = true;
      nodePtr = null;
      stateInfo = initStateInfo;
      globalInfo = gI;
      fitness = MIN_EVALUATION;
      stdDev = Globals.UNDEFINED_VARIANCE;
      localEvalNum = NOT_EVALUATED;
      description = Globals.EMPTY_STRING;
      graphOptions = Globals.EMPTY_STRING;
      complexity = 0;
      evalCost = 0;
      MLJ.ASSERT(initStateInfo != null, "State.<init>:");
      initStateInfo = null; // State gains ownership.
   }

   public double get_test_set_fitness() { return -1; }

   // This does anything state specific (i.e. specific to the derived classes)
   // for the final state (e.g. set_graph_options).
   public void set_final_state() {}

//obs   public Real eval(GlobalInfo*, Bool computeReal = TRUE,
//obs		     Bool computeEstimated = TRUE) = 0;

   abstract public double eval(Object GlobalInfo, boolean computeReal,
		     boolean computeEstimated);
   abstract public double eval(Object GlobalInfo, boolean computeReal);
   abstract public double eval(Object GlobalInfo);


   public double get_fitness()
   {
      return fitness;
   }

   public void set_fitness(double newFitness)
   {
      fitness = newFitness;
   }




   public int get_eval_num()
   {
      return localEvalNum;
   }
   public void set_eval_num(int evalNum)
   {
      localEvalNum = evalNum;
   }

   public double get_std_dev()
   {
      return stdDev;
   }



//obs   public DblLinkList<State<LocalInfo, GlobalInfo> * >* gen_succ(GlobalInfo*,
//obs	      StateSpace< State<LocalInfo, GlobalInfo> > *,
//obs					 Bool computeReal = TRUE) = 0;

   abstract public LinkedList gen_succ(Object GlobalInfo, StateSpace stateSpace, boolean computeReal); 
   abstract public LinkedList gen_succ(Object GlobalInfo, StateSpace stateSpace); 


//   virtual const LocalInfo& get_info() const
   public Object get_info()
   { 
      MLJ.ASSERT(stateInfo != null,"State.get_info():stateInfo is null.");
      return stateInfo;
   }


//obs void State<LocalInfo, GlobalInfo>::set_info(LocalInfo*& newStateInfo)
   public void set_info(Object newStateInfo)
   {
      stateInfo = null;		// Out with the old.
      stateInfo = newStateInfo;	// In with the new.
//   DBG(newStateInfo = NULL);	// State gains ownership.
   }

   public int get_eval_cost()
   {
      return evalCost;
   }


//obs NodePtr State<LocalInfo, GlobalInfo>::get_node(const
//obs					       StateSpace<State<LocalInfo,
//obs					       GlobalInfo> > *pGraph)
   public Node get_node(StateSpace pGraph)
   {
      if(nodeNotCached) {
         // look for the state if we don't know whether we're in or not
         nodePtr = pGraph.find_state(this);
         nodeNotCached = false;
      }
      return nodePtr;
   }

//obs   public void set_node(NodePtr n)
   public void set_node(Node n)
   {
      nodeNotCached = false;
      nodePtr = n;
   }


   public String get_description()
   {
      return description;
   }

   public void set_description(String newDescription)
   {
      description = newDescription;
   }

   public String get_graph_options()
   {
      return graphOptions;
   }

   public void set_graph_options(String newGraphOptions)
   {
      graphOptions = newGraphOptions;
   }


//obs   public void display_for_graph(MLCOStream& stream = Mcout) const;
   public void display_for_graph(Writer stream)
   {
      try{
         display_info(stream);
//obs         stream << ": " << MString(fitness, 2);
         stream.write(": "+fitness);
      }catch(IOException e){e.printStackTrace(); System.exit(1);}
   }

   public void display_for_graph()
   {
      try{
         display_info(Globals.Mcout);
//obs         stream << ": " << MString(fitness, 2);
         Globals.Mcout.write(": "+fitness);
      }catch(IOException e){e.printStackTrace(); System.exit(1);}
   }



//obs   public void display_info(MLCOStream& stream = Mcout) const = 0;
   abstract public void display_info(Writer stream);
   abstract public void display_info();

//obs   virtual void display_stats(MLCOStream& stream,GlobalInfo *gInfo) const = 0;
   abstract public void display_stats(Writer stream, Object gInfo);

//obs   public void display(MLCOStream& stream = Mcout, GlobalInfo *gInfo = NULL) const;
   public void display(Writer stream,Object gInfo)
   {
      try{
         display_info(stream);
         stream.write(": ");
         display_stats(stream, gInfo);
      }catch(IOException e){e.printStackTrace(); System.exit(1);}
   }

   public void display(Writer stream)
   {
      try{
         display_info(stream);
         stream.write(": ");
         display_stats(stream,null);
      }catch(IOException e){e.printStackTrace(); System.exit(1);}
   }

   public void display()
   {
      try{
         display_info(Globals.Mcout);
         Globals.Mcout.write(": ");
         display_stats(Globals.Mcout,null);
      }catch(IOException e){e.printStackTrace(); System.exit(1);}
   }

//obs   public Bool operator==(const State& rhs) const;
//obs   public Bool operator!=(const State& rhs) const
//obs      {return ! ((*this)== rhs);}
   public boolean equals(Object rhs)
   {
      return stateInfo == ((State)rhs).get_info();
   }

   public boolean notEqual(Object rhs)
   {
      return !(equals(rhs));
   }

//obs template <class LocalInfo, class GlobalInfo>
//obs void State<LocalInfo, GlobalInfo>::
//obs evaluate_states(GlobalInfo *gInfo,
//obs 		StateSpace<State<LocalInfo, GlobalInfo> >* pGraph,
//obs 		DblLinkList<State<LocalInfo, GlobalInfo>*>* states,
//obs 		Bool computeReal)

   public void evaluate_states(Object gInfo, StateSpace pGraph, LinkedList states)
   {
      evaluate_states(gInfo,pGraph,states,true);
   }

   public void evaluate_states(Object gInfo, StateSpace pGraph, LinkedList states, boolean computeReal)
   {
      logOptions.LOG(2, "Evaluating States:\n");
   
//obs   DLLPix<State<LocalInfo, GlobalInfo> *> statePix(*states);
//obs   for(statePix.first(); statePix; statePix.next()) {
      // see if state is in the graph
//obs      State<LocalInfo, GlobalInfo> *state = (*states)(statePix);
//obs      State<LocalInfo, GlobalInfo> *temp = (*states)(statePix);
      for(int i = 0;i < states.size(); i++)
      {
         State state = (State)states.get(i);
         State temp = (State)states.get(i);
         Node nodePtr = state.get_node(pGraph);
         if(nodePtr != null)
         {
	 // its in the graph, so replace state in list with the one we found
//obs	 (*states)(statePix) = &(pGraph->get_state(nodePtr));
            states.set(i, pGraph.get_state(nodePtr));       
            logOptions.LOG(2, "Node "+states.get(i)+" (in graph)\n");
            temp = null;
         }
         else
         {
         // its not in the graph, so evaluate it
            state.eval(gInfo, computeReal);
            state.set_eval_num(pGraph.get_next_eval_num());
            logOptions.LOG(2, "Node "+state+"\n");
         }
      }
   }

   protected void finalize()
   {
      stateInfo = null;
   }

}