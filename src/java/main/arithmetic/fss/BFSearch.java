package arithmetic.fss;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import arithmetic.shared.DotGraphPref;
import arithmetic.shared.Error;
import arithmetic.shared.Globals;
import arithmetic.shared.MLJ;
import arithmetic.shared.Node;

public class BFSearch extends StateSpaceSearch{

   public final static String MAX_EXPANSIONS_HELP = "This option ...";
   public final static String EPSILON_HELP = "This option...";
   public final static String MAX_FITNESS_HELP = "What the optimal node's fitness should be. 0 if unbounded";
   public final static String BEAM_WIDTH_HELP = "The number of nodes to expand at each step";
   public final static String PRUNE_HELP = "Whether to prune the unexpanded nodes at each step";
   public final static int DEFAULT_MAX_STALE = 5;
   public final static double DEFAULT_EPSILON = 0.001;
   public final static double DEFAULT_MAX_FITNESS = 1.0;
   public final static int DEFAULT_BEAM_WIDTH = 1;
   public final static boolean DEFAULT_PRUNE = false;


   // Protected member data
   protected int maxNonImprovingExpansions;
   protected double minExpansionImprovement;
   protected double maxFitness;
   protected int beamWidth;
   protected boolean prune;
   
   protected int numNonImprovingExpansions;
   protected Node bestNode;
//obs DblLinkList<NodePtr> openList;
//obs DblLinkList<NodePtr> closedList;
   LinkedList openList = new LinkedList();
   LinkedList closedList = new LinkedList();

/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private BFSearch(BFSearch source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(BFSearch source){}

//obs template <class LocalInfo, class GlobalInfo>
//obs Bool BFSearch<LocalInfo, GlobalInfo>::terminate()
   protected boolean terminate()
   {
      if (openList.isEmpty())
      {
         logOptions.LOG(2, "Terminating due to empty open list\n");
         return true;
      }
   
      MLJ.ASSERT(get_eval_limit() >= 0,"BFSearch.terminate:get_eval_limit() < 0");
      MLJ.ASSERT(graph.get_num_states() >= 0,"BFSearch.terminate:graph.get_num_states() < 0");
      MLJ.ASSERT(numNonImprovingExpansions >= 0,"BFSearch.terminate:numNonImprovingExpansions < 0");
      MLJ.ASSERT(maxNonImprovingExpansions > 0,"BFSearch.terminate:maxNonImprovingExpansions <= 0");
      if (numNonImprovingExpansions >= maxNonImprovingExpansions)
      {
         logOptions.LOG(2, "Terminating because number of non-improving expansions "
            +"hit the maximum ("+maxNonImprovingExpansions+")\n");
         return true;
      }

      if (get_eval_limit() != 0 && graph.get_num_states() > get_eval_limit())
      {
         logOptions.LOG(2, "Terminating because more nodes were evaluated than "
            +"EVAL_LIMIT ("+graph.get_num_states()+">"
            +get_eval_limit()+")\n");
         return true;
      }
   
      return false;
   }

   public BFSearch()
   {
      set_defaults();
   }

/*
//obs template <class LocalInfo, class GlobalInfo>
//obs void BFSearch<LocalInfo, GlobalInfo>::set_user_options(const MString& stem)
public void set_user_options(String stem)
{
   super.set_user_options(stem);
   set_max_non_improving_expansions(
      get_option_int(stem + "MAX_STALE", maxNonImprovingExpansions,
		     MAX_EXPANSIONS_HELP, false));
   set_min_expansion_improvement(
      get_option_real(stem + "EPSILON", minExpansionImprovement,
		      EPSILON_HELP, false));
   set_max_fitness(get_option_real(stem + "MAX_FITNESS", maxFitness,
				   MAX_FITNESS_HELP, true));
   set_beam_width(get_option_int_range(stem + "BEAM_WIDTH", beamWidth, 1,
				       INT_MAX, BEAM_WIDTH_HELP, true));
   set_prune(get_option_bool(stem + "PRUNE", prune, PRUNE_HELP, true));

}
*/

//obs void BFSearch<LocalInfo, GlobalInfo>::set_defaults()
   public void set_defaults()
   {
      super.set_defaults();
      set_max_non_improving_expansions(DEFAULT_MAX_STALE);
      set_min_expansion_improvement(DEFAULT_EPSILON);
      set_max_fitness(DEFAULT_MAX_FITNESS);
      set_beam_width(DEFAULT_BEAM_WIDTH);
      set_prune(DEFAULT_PRUNE);
   }

   public void set_beam_width(int width)
   {
      if (width < 1)
         Error.fatalErr("BFSearch::set_beam_width: the given width "+width
            +" is less than 1, which is not allowed");
      beamWidth = width;
   }

   public void set_prune(boolean doPrune)
   {
      prune = doPrune;
   }

   public void set_max_non_improving_expansions(int maxNIE)
   {
      maxNonImprovingExpansions = maxNIE;
   }

   public void set_min_expansion_improvement(double minEI)
   {
      minExpansionImprovement = minEI;
   }

   public void set_max_fitness(double maxF)
   {
      maxFitness = maxF;
   }

   public State search(State initialState, Object gInfo)
   {
      return search(initialState,gInfo,Globals.EMPTY_STRING);
   }

//obs template <class LocalInfo, class GlobalInfo>
//obs const State<LocalInfo, GlobalInfo>& BFSearch<LocalInfo, GlobalInfo>::
//obs search(State<LocalInfo, GlobalInfo>* initialState, GlobalInfo *gInfo,
//obs        const MString& displayFileName)
   public State search(State initialState, Object gInfo, String displayFileName)
   {
   // Setup variables.
      numExpansions = 0;
      numNonImprovingExpansions = 0;
      bestNode = null;
      double bestEval = State.MIN_EVALUATION;
      graph = null;
//obs   graph = new StateSpace< State<LocalInfo, GlobalInfo> >;
      graph = new StateSpace();
      graph.set_log_level(get_log_level()-1);

   // Start with empty open/closed lists.
//obs   openList.free();
//obs   closedList.free();
      openList.clear();
      closedList.clear();
   
   // Evalute initial node and put in on the open list.
      initialState.eval(gInfo, get_show_test_set_perf() == ShowTestSetPerf.showAlways);
      initialState.set_eval_num(graph.get_next_eval_num());
//obs   NodePtr root = graph->create_node(initialState);
      Node root = graph.create_node(initialState);
      graph.set_initial_node(root);
//obs   openList.append(root);
      openList.add(root);

      int numChildrenGenerated = 1;

   // Declare these here so they don't get allocated & deleted with each loop
//obs   Array<NodePtr> bestNodes(beamWidth);
      Node[] bestNodes = new Node[beamWidth];
//obs   Array<Real> fitnesses(beamWidth);
      double[] fitnesses = new double[beamWidth];
      while (!terminate())
      {

      // Find the best nodes on the open list.  Note that there must be some
      // node on the open list or else we wouldn't pass the termination
      // condition.

//obs      int numGoodNodes = MLC::min(beamWidth, openList.length());
         int numGoodNodes = Math.min(beamWidth, openList.size());

         for (int i = 0; i < numGoodNodes; i++)
         {
//	 DBG(ASSERT(openList.length() > 0));
            double bestOpenNodeFitness = State.MIN_EVALUATION;
//obs	 DLLPix<NodePtr> bestOpenNodePix(openList,0);
//obs	 for (DLLPix<NodePtr> nodePix(openList,1); nodePix; ++nodePix) {
            int bestOpenNodePix = 0;
            for (int nodePix = 0; nodePix < openList.size(); nodePix++)
            {
//obs	    const State<LocalInfo, GlobalInfo>& tmpState =
//obs	       graph->get_state(openList(nodePix));
//obs	    State tmpState = graph.get_state(openList(nodePix));
               State tmpState = graph.get_state((Node)openList.get(nodePix));

               if (tmpState.get_fitness() > bestOpenNodeFitness)
               {
                  bestOpenNodeFitness = tmpState.get_fitness();
                  bestOpenNodePix = nodePix;
               }
            }
//	 DBG(ASSERT(bestOpenNodePix));
//obs	 bestNodes[i] = openList(bestOpenNodePix);
            bestNodes[i] = (Node)openList.get(bestOpenNodePix);
            fitnesses[i] = bestOpenNodeFitness;
//	 DBG(ASSERT(fitnesses[i] ==
//		    graph->get_state(bestNodes[i]).get_fitness()));
            logOptions.LOG(3, "Added "+graph.get_state(bestNodes[i])
               +" to list of parents to expand\n");
	 // Remove best node from open list and put in on the closed list.
//obs	 closedList.append(bestNodes[i]);
            closedList.add(bestNodes[i]);
            openList.remove(bestOpenNodePix);
         }


      // We know that the arrays of bestNodes & fitnesses are sorted from best
      // to worst (kth best).  So the very best is in bestNodes[0]
         Node bestChild = bestNodes[0];
         double bestChildFitness = fitnesses[0];
      
         if (maxFitness != 0.0 &&
            (MLJ.approx_greater(bestChildFitness + minExpansionImprovement,
                        maxFitness) ||
            MLJ.approx_equal(bestChildFitness + minExpansionImprovement,
                        maxFitness)))
         {
            bestNode = bestChild;

	 // terminating because got max (or near) fitness
            if(get_show_test_set_perf() == ShowTestSetPerf.showBestOnly)
               graph.get_state(bestNode).eval(gInfo, true, false);
            logOptions.LOG(2, "Terminating because we found a node with near maximum "
               +"fitness:"+maxFitness+"\n");
            break; 
         }
      
      // Prune everything we didn't expand, if appropriate
         if (prune)
            while (!openList.isEmpty())
               closedList.add(openList.removeFirst());
      
      // if we're showing real error on best nodes only, get
      // the real error of the best node now
         if(get_show_test_set_perf() == ShowTestSetPerf.showBestOnly)
            graph.get_state(bestChild).eval(gInfo, true, false);
      
      // Update number of non-improving expansions.
      // In this case, an expansion is non-improving if all of the children
      // are no good (so if the best one is no good)
      // @@ changing the next if statement to:
      // @@   if (bestChildFitness - bestEval > minExpansionImprovement)
      // @@ causes t_FSSInducer to fail on the diff
         if (MLJ.approx_greater(bestChildFitness,
            bestEval + minExpansionImprovement))
            numNonImprovingExpansions = 0;
         else if (numChildrenGenerated > 0)
            numNonImprovingExpansions++;
         if (numChildrenGenerated > 0)
            numExpansions++;
      
      // Remember best node added to the closed list.
         if (MLJ.approx_greater(bestChildFitness , bestEval))
         {
            bestEval = bestChildFitness;
            bestNode = bestChild;
            logOptions.DRIBBLE("\nNew best node ("+graph.get_num_states()+" evals) "
               +graph.get_state(bestNode)+"\n");
         }

         for (int i = 0; i < numGoodNodes; i++)
            logOptions.LOG(2, "expanding "+graph.get_state(bestNodes[i])+"\n");
         logOptions.LOG(2, numNonImprovingExpansions+" non-improving expansions.\n");

      // If termination condition is reached, exit without evaluating nodes
      // from final expansion.
         if (numNonImprovingExpansions >= maxNonImprovingExpansions)
         {
            logOptions.LOG(2, "Terminating because number of non-improving expansions "
               +"hit the maximum ("+maxNonImprovingExpansions+")\n");
            break;
         }
      
      // For all successors which are not in the graph (on open or closed
      // lists), evaluate them and put them on the open list.
         boolean computeRealNow = (get_show_test_set_perf() == ShowTestSetPerf.showAlways);
         numChildrenGenerated = 0;
         for (int i = 0; i < numGoodNodes; i++)
         {
//obs	 DblLinkList<State<LocalInfo, GlobalInfo>*>* successors =
//obs	    graph->get_state(bestNodes[i]).gen_succ(gInfo, graph,
//obs						    computeRealNow);
            LinkedList successors = graph.get_state(bestNodes[i]).gen_succ(gInfo, graph,
               computeRealNow);
            numChildrenGenerated += successors.size();
            while (!successors.isEmpty())
            {
               Node childNode = ((State)successors.removeFirst()).get_node(graph);
               MLJ.ASSERT(childNode != null,"BFSearch.search:childNode == null.");
               openList.add(childNode);
            }
//obs	 delete successors;
            successors = null;
         }
      }
      logOptions.DRIBBLE("\nFinal best node "
          +graph.get_state(bestNode)+"\n");
      logOptions.DRIBBLE("Expanded "+numExpansions+" nodes\n");
      logOptions.LOG(2, "evaluated "+graph.get_num_states()+" nodes\n");
  
      graph.set_final_node(bestNode);

   // if we're showing real error on the final state only, compute
   // real error here
      if(get_show_test_set_perf() == ShowTestSetPerf.showFinalOnly)
         graph.get_state(bestNode).eval(gInfo, true, false);
   
   // display the state space, if called for
      if(displayFileName != Globals.EMPTY_STRING)
      {
         try
         {
               FileWriter out = new FileWriter(displayFileName);
               DotGraphPref pref = new DotGraphPref();
               graph.display(out, pref);
         }catch(IOException e){e.printStackTrace();System.exit(1);}
      }
      return graph.get_state(bestNode);
   }

}