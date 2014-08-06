package arithmetic.fss;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import arithmetic.shared.DotGraphPref;
import arithmetic.shared.Globals;
import arithmetic.shared.MLJ;
import arithmetic.shared.Node;

public class HCSearch extends StateSpaceSearch{

   double bestEval;
   double prevBestEval;
//obs   NodePtr bestNode, prevBestNode;
   Node bestNode;
   Node prevBestNode;


/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private HCSearch(HCSearch source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(HCSearch source){}

//obs template <class LocalInfo, class GlobalInfo>
//obs Bool HCSearch<LocalInfo, GlobalInfo>::terminate()
   protected boolean terminate()
   {
      if (prevBestNode == bestNode)
      {
         logOptions.LOG(2, "Terminating because we cannot find a better node\n");
         return true;
      }
   
      MLJ.ASSERT(get_eval_limit() >= 0,"HCSearch.terminate:get_eval_limit() < 0.");
      MLJ.ASSERT(graph.get_num_states() >= 0,"HCSearch.terminate:graph.get_num_states() < 0.");
      if ((get_eval_limit() != 0) && graph.get_num_states() > get_eval_limit())
      {
         logOptions.LOG(2, "Terminating because more nodes were evaluated than "
            +"EVAL_LIMIT ("+graph.get_num_states()+">"
            +get_eval_limit()+")\n");
         return true;
      }
   
      return false;
   }

   public HCSearch()
   {
   }


   public State search(State initialState, Object gInfo)
   {
      return search(initialState, gInfo, Globals.EMPTY_STRING);
   }


//obs template <class LocalInfo, class GlobalInfo>
//obs const State<LocalInfo, GlobalInfo>& HCSearch<LocalInfo, GlobalInfo>::
//obs search(State<LocalInfo, GlobalInfo>* initialState, GlobalInfo *gInfo,
//obs        const MString& displayFileName)
   public State search(State initialState, Object gInfo, String displayFileName)
   {
      graph = null;
//obs   graph = new StateSpace< State<LocalInfo, GlobalInfo> >;
      graph = new StateSpace();
      numExpansions = 0;
      prevBestNode = null;
      double bestEval  = initialState.eval(gInfo, get_show_test_set_perf() == ShowTestSetPerf.showAlways);

      initialState.set_eval_num(graph.get_next_eval_num());
      bestNode  = graph.create_node(initialState);
      graph.set_initial_node(bestNode);
      if(get_show_test_set_perf() == ShowTestSetPerf.showBestOnly)
         graph.get_state(bestNode).eval(gInfo, true, false);

      while (!terminate())
      {
         prevBestNode = bestNode;

      // display new best node
         logOptions.LOG(1, "new best node ("+graph.get_num_states()+" evals) "+graph.get_state(bestNode)+"\n");
      
//obs      DblLinkList<State<LocalInfo, GlobalInfo>*>* successors =
//obs	 graph->get_state(prevBestNode).gen_succ(gInfo, graph,
//obs				      get_show_test_set_perf() == showAlways);
         LinkedList successors = graph.get_state(prevBestNode).gen_succ(gInfo, graph,
            get_show_test_set_perf() == ShowTestSetPerf.showAlways);


         while (!successors.isEmpty())
         {
            numExpansions++;
//obs         State<LocalInfo, GlobalInfo>* childState =
//obs	    successors->remove_front();
            State childState = (State)successors.removeFirst();
            double childEval = childState.get_fitness();
            MLJ.ASSERT(childEval > State.MIN_EVALUATION,"HCSearch.search:childEval <= State.MIN_EVALUATION.");
//obs         NodePtr childNode = childState->get_node(graph);
            Node childNode = childState.get_node(graph);
            MLJ.ASSERT(childNode != null,"HCSearch.search:childNode == null.");
            if (MLJ.approx_greater(childEval, bestEval))
            {
               bestEval = childEval;
               bestNode = childNode;
            }
         }

         successors = null;
         if(get_show_test_set_perf() == ShowTestSetPerf.showBestOnly)
            graph.get_state(bestNode).eval(gInfo, true, false);

      }

   // get real error on final node if needed
      if(get_show_test_set_perf() == ShowTestSetPerf.showFinalOnly)
         graph.get_state(bestNode).eval(gInfo, true, false);
      logOptions.LOG(1, "final best node "+graph.get_state(bestNode)+"\n");

      logOptions.LOG(1, "expanded "+numExpansions+" nodes\n");
      logOptions.LOG(2, "evaluated "+graph.get_num_states()+" nodes\n");
      graph.set_final_node(bestNode);

   // display the state space, if called for
//obs   if(displayFileName) {
//obs      MLCOStream out(displayFileName);
//obs      if(displayFileName != null)
      if(displayFileName != Globals.EMPTY_STRING)
      {
         try{
            FileWriter out = new FileWriter(displayFileName);
            DotGraphPref pref = new DotGraphPref();
            graph.display(out, pref);
         }catch(IOException e){e.printStackTrace();System.exit(1);}
      }
      return graph.get_state(bestNode);
   }
   
}