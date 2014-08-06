package arithmetic.fss;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import arithmetic.shared.DotGraphPref;
import arithmetic.shared.Error;
import arithmetic.shared.Globals;
import arithmetic.shared.MLJ;
import arithmetic.shared.Node;

public class SASearch extends StateSpaceSearch{

//States are stored in graph member.

public static final int DEFAULT_MAX_STALE = 5;
public static final double DEFAULT_EPSILON = 0.001;
public static final double DEFAULT_INITIAL_LAMBDA = 0.001;
public static final int DEFAULT_MAX_EVALUATIONS = 5;
public static final int DEFAULT_MIN_EXP_EVALUATIONS = 5;
public static final int DEFAULT_SAS_SEED = 7258789;

public static final String MAX_NON_IMPROVING_EXPANSIONS_HELP = "Number of non-improving "
   +"expansions that cause the search to be considered stale (hence terminate)";
public static final String MIN_EXPANSION_IMPROVEMENT_HELP = "Improvement of less than this "
   +"epsilon is considered a non-improvement (search is still stale)";
public static final String INITIAL_LAMBDA_HELP = "The temperature in simulated annealing.  "
   +"Higher values cause more randomness";
public static final String MAX_EVALUATIONS_HELP = "Maximum evaluations per node";
public static final String MIN_EXP_EVALUATIONS_HELP = "Minimum evaluations before expanding";
public static final String SEED_HELP = "The seed for the simulated annealing random generator";




/** A random number generator. **/
   private Random randNumGen = new Random();

   int numNonImprovingExpansions;
   int maxNumberOfNonImprovingExpansions;
   double minIncreaseForImprovingExpansion;
   int maxEvaluations;
   int minExpEvaluations; // minimum evals before expanding a node
   int numReEvaluations;  // total number of reevaluations
   double initialLambda;
   int sasSeed;
//obs   DynamicArray<SANode> nodeList;
   Vector nodeList;



/***************************************************************************
  Returns the random number generator for this object.
@return The random number generator for this object.
***************************************************************************/
   public Random rand_num_gen() {return randNumGen;}

/***************************************************************************
  Sets the seed for the random number generator stored in this object.
@param seed	The new seed for the number generator.
***************************************************************************/
   public void init_rand_num_gen(long seed) {randNumGen.setSeed(seed);}

/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private SASearch(SASearch source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(SASearch source){}

//obs template <class LocalInfo, class GlobalInfo>
//obs SASearch<LocalInfo, GlobalInfo>::SASearch()
   public SASearch()
   {
      super();
      nodeList = new Vector();
      numReEvaluations = 0;
      set_defaults();
   }

//obs template <class LocalInfo, class GlobalInfo>
//obs void SASearch<LocalInfo, GlobalInfo>::set_defaults()
   public void set_defaults()
   {
      super.set_defaults();
      set_max_non_improving_expansions(DEFAULT_MAX_STALE);
      set_min_expansion_improvement(Globals.DEFAULT_EPSILON);
      set_max_evaluations(DEFAULT_MAX_EVALUATIONS);
      set_min_exp_evaluations(DEFAULT_MIN_EXP_EVALUATIONS);
      set_initial_lambda(DEFAULT_INITIAL_LAMBDA);
   }

/*public void set_user_options(String stem)
{
   super.set_user_options(stem);
   set_max_non_improving_expansions(
      get_option_int(stem + "MAX_STALE",
		     get_max_non_improving_expansions(),
		     MAX_NON_IMPROVING_EXPANSIONS_HELP,
		     true));
   set_min_expansion_improvement(
      get_option_real(stem + "EPSILON",
		      get_min_expansion_improvement(),
		      MIN_EXPANSION_IMPROVEMENT_HELP,
		      true));
   set_max_evaluations(
      get_option_int(stem + "MAX_EVALS",
		     get_max_evaluations(),
		     MAX_EVALUATIONS_HELP,
		     true));
   set_min_exp_evaluations(
      get_option_int(stem + "MIN_EXP_EVALS",
		     get_min_exp_evaluations(),
		     MIN_EXP_EVALUATIONS_HELP,
		     true));
   set_initial_lambda(
      get_option_real(stem + "LAMBDA",
		      get_initial_lambda(),
		      INITIAL_LAMBDA_HELP,
		      true));
   set_sas_seed(
      get_option_int(stem + "SAS_SEED",
		     DEFAULT_SAS_SEED,
		     SEED_HELP,
		     true));
}
*/





   protected boolean terminate()
   {
      if (nodeList.size() == 0) 
      {
         logOptions.LOG(2, "Terminating due to empty node list\n");
         return true;
      }
   
      MLJ.ASSERT(get_eval_limit() >= 0,"SASearch.terminate():get_eval_limit() < 0.");
      MLJ.ASSERT(graph.get_num_states() >= 0,"SASearch.terminate():graph.get_num_states() < 0.");
      MLJ.ASSERT(numNonImprovingExpansions >= 0,"SASearch.terminate():numNonImprovingExpansions < 0.");
      MLJ.ASSERT(get_max_non_improving_expansions() > 0,"SASearch.terminate():get_max_non_improving_expansions() <= 0.");

      if (numNonImprovingExpansions >= get_max_non_improving_expansions()) 
      {
         logOptions.LOG(2, "Terminating because number of non-improving expansions "
            +"hit the maximum ("+get_max_non_improving_expansions()
            +")\n");
         return true;
      }

      if ((get_eval_limit() != 0)&&(graph.get_num_states()+numReEvaluations > get_eval_limit()))
      {
         logOptions.LOG(2, "Terminating because more nodes were evaluated than "
            +"EVAL_LIMIT ("+graph.get_num_states()+numReEvaluations+">"
            +get_eval_limit()+")\n");
         return true;
      }
   
      return false;
   }

   public int get_max_non_improving_expansions()
   {
      return maxNumberOfNonImprovingExpansions;
   }

   public int get_sas_seed()
   {
      return sasSeed;
   }

   public void set_sas_seed(int newSeed)
   {
      sasSeed = newSeed;
      init_rand_num_gen(sasSeed);
   }

   void set_initial_lambda(double initLambda)
   {
      if (initLambda <= 0)
         Error.fatalErr("SASearch::set_initial_lambda: initLambda ("
            +initLambda+") must be greater than 0");
      initialLambda = initLambda;
   }

   void set_min_exp_evaluations(int minEval)
   {
      if (minEval < 1 || minEval > get_max_evaluations())
         Error.fatalErr("SASearch::set_min_exp_evaluations: minEval ("
            +minEval+") must be >0 and <max_evaluations"
            +get_max_evaluations());
      minExpEvaluations = minEval;
   }

   public void set_max_evaluations(int maxEval)
   {
      if (maxEval < 1)
         Error.fatalErr("SASearch::set_max_evaluations: maxEval ("
            +maxEval+") must be greater than 0");
      maxEvaluations = maxEval;
   }

   public int get_max_evaluations()
   {
      return maxEvaluations;
   }

   public void set_min_expansion_improvement(double minImprovement)
   {
      minIncreaseForImprovingExpansion = minImprovement;
   }

//obs template <class LocalInfo, class GlobalInfo>
//obs Real SASearch<LocalInfo, GlobalInfo>::get_min_expansion_improvement() const
   public double get_min_expansion_improvement()
   {
      return minIncreaseForImprovingExpansion;
   }

   public void set_max_non_improving_expansions(int maxExpansions)
   {
      if (maxExpansions < 1)
         Error.fatalErr("SASearch::set_max_non_improving_expansions: "
            +"maxExpansions ("+maxExpansions+") must be at least 1");
      maxNumberOfNonImprovingExpansions = maxExpansions;
   }

//obs Real SASearch<LocalInfo, GlobalInfo>::get_initial_lambda() const
   public double get_initial_lambda()
   {
      return initialLambda;
   }

//obs template <class LocalInfo, class GlobalInfo>
//obs int SASearch<LocalInfo, GlobalInfo>::get_min_exp_evaluations() const
   public int get_min_exp_evaluations()
   {
      return minExpEvaluations;
   }


//obs template <class LocalInfo, class GlobalInfo>
//obs void SASearch<LocalInfo, GlobalInfo>::reeval_node(int nodeNum,
//obs     GlobalInfo *gInfo, NodePtr& bestRetired,
//obs     StateSpace< State<LocalInfo, GlobalInfo> >& graph)
   private void reeval_node(int nodeNum,
      Object gInfo, Node bestRetired,
      StateSpace graph)

   {
      SANode saNode = (SANode)nodeList.get(nodeNum);
   // Re-evaluate node if it has not reached its max.
      if (saNode.numEvaluations < get_max_evaluations())
      {
         saNode.numEvaluations++;

      // when reevaluating, never get the real error
         saNode.fitness = graph.get_state(saNode.Node).eval(gInfo, false, true);
         logOptions.LOG(1, "re-evaluated node "+
            graph.get_state(saNode.Node)+"\n");
         logOptions.LOG(3, "Re-eval fitness = "+saNode.fitness
            +"\n");
         numReEvaluations++;
      }
      else
      {
         logOptions.LOG(2, "node "
            +graph.get_state(saNode.Node)
            +" has reached maximum number of evaluations ("
            +get_max_evaluations()+")\n");

      // if the newly retired node has a better evaluation than
      // the best retired node so far, replace the best retired node.
         if(bestRetired == null ||
               MLJ.approx_greater(saNode.fitness, graph.get_state(bestRetired).
               get_fitness()))
         {
            bestRetired = saNode.Node;
            logOptions.LOG(2, "new best node (" +(graph.get_num_states() + numReEvaluations)
               +" evals {retired}) "+graph.get_state(bestRetired)+"\n");

	 // if the best retired node has no test set error, give it one
	 // if we're in best error mode
            if(get_show_test_set_perf() == ShowTestSetPerf.showBestOnly &&
                  graph.get_state(bestRetired).get_test_set_fitness() < 0)
            {
               graph.get_state(bestRetired).eval(gInfo, true, false);
               logOptions.LOG(2, "needed test set error for best retired node\n");
            }
         }

      // yank the maxed-out node out of the list, then use
      // truncate to reduce the list's size
//obs      nodeList[nodeNum] = nodeList[nodeList.high()];
//obs      nodeList.truncate(nodeList.size() - 1);
         nodeList.setElementAt(nodeList.lastElement(),nodeNum);
         nodeList.remove(nodeList.size()-1);
      }
   }


   public State search(State initialState, Object gInfo)
   {
      return search(initialState,gInfo,Globals.EMPTY_STRING);
   }

   public State search(State initialState, Object gInfo,
      String displayFileName)
   {
   // Initial setup.
//obs   nodeList.truncate(0);   // make sure node list has 0 elements
      nodeList.clear();   // make sure node list has 0 elements
      graph = null;
//obs   graph = new StateSpace< State<LocalInfo, GlobalInfo> >;
      graph = new StateSpace();
      if(get_log_level() > 0)
         graph.set_log_level(get_log_level() - 1);
      double lambda = get_initial_lambda();
      Node bestRetired = null;
      numReEvaluations = 0;
      numExpansions = 0;
   
      numNonImprovingExpansions = 0;
   
   // Evalute initial node and put on node list.
      double initialEval = initialState.eval(gInfo,
         get_show_test_set_perf() == ShowTestSetPerf.showAlways);
      initialState.set_eval_num(graph.get_next_eval_num());
      Node initialNode = graph.create_node(initialState);
      MLJ.ASSERT(initialNode != null,"SASearch.search:initialNode == null.");
//obs   nodeList[nodeList.size()] = SANode(initialNode, initialEval);
      nodeList.add(new SANode(initialNode, initialEval));
      double bestEval = State.MIN_EVALUATION;
      Node bestNode = null;
      boolean lastIterationWasExpansion = false;

      logOptions.LOG(1, "Beginning SASearch\n");
      logOptions.LOG(1, "initial node = "
         +((State)graph.get_state(initialNode)).get_eval_num()+"\n"
         +graph.get_state(initialNode)+"\n");

      while (!terminate())
      {
      // Use the sim_anneal function to pick a node randomly.  Use the
      // standard-deviation of the first node to determine lambda.
      // If the first node has no standard deviation, always pick it.
         int nodeNum;

      // pick a lambda
         double saLambda = graph.get_state(((SANode)nodeList.get(0)).Node).get_std_dev();
         if(nodeList.size() > 1 && saLambda != 0 && saLambda != Globals.UNDEFINED_VARIANCE)
         {

	 // build a real array of fitnesses for sim_anneal
//obs	 double[] fitnessArray(nodeList.low(), nodeList.size());
            double[] fitnessArray = new double[nodeList.size()];
//obs	 for(int i=nodeList.low(); i<=nodeList.high(); i++)
            for(int i = 0; i < nodeList.size(); i++)
               fitnessArray[i] = graph.get_state(((SANode)nodeList.get(i)).Node).get_fitness();

	 // call sim_anneal to pick the slot
            nodeNum = sim_anneal.sim_anneal(fitnessArray, saLambda*lambda, rand_num_gen());
         }
         else
            nodeNum = 0;
      
         logOptions.LOG(2, "picked slot "+nodeNum+": "
            +graph.get_state(((SANode)nodeList.get(nodeNum)).Node)+"\n");

         if (((SANode)nodeList.get(nodeNum)).isExpanded ||
               ((SANode)nodeList.get(nodeNum)).numEvaluations < get_min_exp_evaluations())
         {
            lastIterationWasExpansion = false;
            reeval_node(nodeNum, gInfo, bestRetired, graph);
         }
         else
         {
	 // Evaluate all successors not already in the graph and add them
	 // to the node list.
            lastIterationWasExpansion = true;
            numNonImprovingExpansions++;
            logOptions.LOG(1, "expanded node "
               +graph.get_state(((SANode)nodeList.get(nodeNum)).Node)+"\n");
            ((SANode)nodeList.get(nodeNum)).isExpanded = true;
//obs	 DblLinkList<State<LocalInfo, GlobalInfo>*>* successors =
            LinkedList successors =
            graph.get_state(((SANode)nodeList.get(nodeNum)).Node).gen_succ(gInfo, graph,
               get_show_test_set_perf() == ShowTestSetPerf.showAlways);
//obs	 while (!successors.empty()) {
            while (!(successors.size() == 0))
            {
//obs	    State<LocalInfo, GlobalInfo>* childState = successors.remove_front();
//obs	    State childState = successors.remove_front();
               State childState = (State)successors.removeFirst();
               Node childNode = childState.get_node(graph);
               MLJ.ASSERT(childNode != null,"SASearch.search:childNode == null.");
               double childEval = childState.get_fitness();
               MLJ.ASSERT(childEval > State.MIN_EVALUATION,"SASearch.search:childEval <= State.MIN_EVALUATION.");
//obs	    nodeList[nodeList.size()] = SANode(childNode, childEval);
               nodeList.add(new SANode(childNode, childEval));
            }
            successors = null;
         }

      // Make sure firstNode has been evaluated enough times.
      // Sort list of nodes (best first) and display best node.
//obs      nodeList.sort();
         SANode.sort(nodeList);
//      IFLOG(3, display_nodelist(get_log_stream()));

      // If node 0 is the last node and gets retired, then we have
      // no nodes remaining in the nodeList and we must exit this
      // loop.
         while (nodeList.size() > 0 &&
            ((SANode)nodeList.get(0)).numEvaluations < get_min_exp_evaluations())
         {
            reeval_node(0, gInfo, bestRetired, graph);
//obs	 nodeList.sort();
            SANode.sort(nodeList);
//         IFLOG(3, display_nodelist(get_log_stream()));
         }
         if(nodeList.size() > 0)
         {
            Node firstNode = ((SANode)nodeList.get(0)).Node;
            MLJ.ASSERT(firstNode != null, "SASearch.search:firstNode == null.");
            double firstEval = ((State)graph.get_state(firstNode)).get_fitness();
	 // If two nodes have the same error, one gets evaluated
	 //    and drops, we have a new best node.
	 // Another possibility is that the first node's fitness changed.
            if (firstNode != bestNode || firstEval != bestEval)
            {
               bestEval = firstEval;
               if ((bestRetired != null)&&(((State)graph.get_state(bestRetired)).get_fitness()>bestEval))
               {
                  logOptions.LOG(2, "best node is retired "+graph.get_state(bestRetired)+"\n");
                  bestNode = firstNode;
               }
               else if (firstNode != bestNode)
               {
                  if(bestNode == null ||
                     Math.abs(firstEval - ((State)graph.get_state(bestNode)).get_fitness()) >
                     get_min_expansion_improvement())
                  {
                     numNonImprovingExpansions = 0;
                     if (bestNode != null)
                     {
                        logOptions.LOG(2, "Resetting stale to 0.  First is "+firstEval
                           +" best is "
                           +((State)graph.get_state(bestNode)).get_fitness()+"\n");
                     }
                  }
                  else
                     logOptions.LOG(3, "Non-stale improvement\n");
                  bestNode = firstNode;

	       // get real error for best node if called for
                  if(get_show_test_set_perf() == ShowTestSetPerf.showBestOnly)
                     ((State)graph.get_state(bestNode)).eval(gInfo, true, false);
	    
                  logOptions.LOG(1, "new best node ("+(graph.get_num_states() +
                     numReEvaluations)+" evals) "
                     +graph.get_state(bestNode)+"\n");
               }
               else
               {
                  logOptions.LOG(2, "re-evaluated best node ("+(graph.get_num_states() +
                     numReEvaluations)+" evals) "
                     +graph.get_state(bestNode)+"\n");
               }
            }
         }
         if(lastIterationWasExpansion)
            numExpansions++;
      
         logOptions.LOG(1, "Iteration complete.  ");
         if (lastIterationWasExpansion)
            logOptions.LOG(1, "Expansion ("+numNonImprovingExpansions+" stale).  ");
         else
            logOptions.LOG(1, "Reevaluation.  ");
         logOptions.LOG(1, "\n");

         logOptions.LOG(1, "Total evaluations: "+(graph.get_num_states()+numReEvaluations)
            +" (" +graph.get_num_states() +" new + " +
         numReEvaluations +"old)\n" );

         Node apparentBest;
         if((bestRetired != null)&&(((State)graph.get_state(bestNode)).get_fitness()<
            ((State)graph.get_state(bestRetired)).get_fitness()))
         {

            logOptions.LOG(1, "Current best (retired) ");
            apparentBest = bestRetired;
         }
         else
         {
            logOptions.LOG(1, "Current best ");
            apparentBest = bestNode;
         }

         logOptions.LOG(1, "(" +(graph.get_num_states() + numReEvaluations)+" evals) ");
         logOptions.LOG(1, graph.get_state(apparentBest) +"\n");
      }

   // get real error for final node if called for
      if((bestRetired != null)&&(((State)graph.get_state(bestRetired)).get_fitness() >=
            ((State)graph.get_state(bestNode)).get_fitness()))
      {
         logOptions.LOG(2, "Final best node is best retired node\n");
         bestNode = bestRetired;
      }
      if(get_show_test_set_perf() == ShowTestSetPerf.showFinalOnly)
         ((State)graph.get_state(bestNode)).eval(gInfo, true, false);
   // @@ temporary hack to get this working.  Should be an ASSERT
   // else if(get_show_test_set_perf() == ShowTestSetPerf.showBestOnly)
      // we're supposed to have a real error here, so make sure we
      // actually have one.
      // ASSERT(graph.get_state(bestNode).get_real_error() >= 0);
      else if(get_show_test_set_perf() == ShowTestSetPerf.showBestOnly && 
	   ((State)graph.get_state(bestNode)).get_test_set_fitness() < 0)
      {
         logOptions.LOG(1, "WARNING: final best node has no test set error! "
            +" computing now...\n");
         ((State)graph.get_state(bestNode)).eval(gInfo, true, false);
      }

      logOptions.LOG(1, "final best node = "
         +graph.get_state(bestNode)+"\n");

      logOptions.LOG(1, "expanded " +numExpansions +" nodes\n");
      logOptions.LOG(2, "evaluated " +(graph.get_num_states()+numReEvaluations)
         +" nodes\n");
      graph.set_final_node(bestNode);
   
   // display the state space, if called for
//obs      if(displayFileName != null)
      if(displayFileName != Globals.EMPTY_STRING)
      {
//obs      MLCOStream out(displayFileName);
         try
         {
            FileWriter out = new FileWriter(displayFileName);
            DotGraphPref pref = new DotGraphPref();
            graph.display(out, pref);
         }catch(IOException e){e.printStackTrace(); System.exit(1);}
      }
      MLJ.ASSERT(bestNode != null,"SASearch.search:bestNode == null.");
      return (State)graph.get_state(bestNode);
   }


/*
   // Private methods
   virtual void display_nodelist(MLCOStream& stream = Mcout) const;
   virtual void reeval_node(int nodeNum, GlobalInfo *gInfo,
			    NodePtr& bestRetired,
			    StateSpace< State<LocalInfo, GlobalInfo> >& graph);
public:
   
   virtual const State<LocalInfo, GlobalInfo>&
      search(State<LocalInfo, GlobalInfo>* initialState, GlobalInfo *gInfo,
	     const MString& displayFileName = EMPTY_STRING);
*/

}