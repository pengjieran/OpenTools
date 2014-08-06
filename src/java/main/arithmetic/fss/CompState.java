package arithmetic.fss;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import arithmetic.shared.MLJ;
import arithmetic.shared.MLJArray;

abstract public class CompState extends PerfEstState {
    
    public CompState(int[] initialValues, PerfEstInfo gI) {
        super(initialValues, gI);
    }
    
    
    public LinkedList gen_succ(Object info, StateSpace space) {
        return gen_succ(info, space, true);
    }
    
    public LinkedList gen_succ(Object info, StateSpace space, boolean computeReal) {
        LinkedList successors = new LinkedList();
        /*** JL changed this section ***/
        Vector compElemArray = new Vector();
        int compSpaceSize = ((int[])get_info()).length * 2;
        for(int i = 0; i < compSpaceSize; i++)
            compElemArray.add(new CompElem());
        /*** end change ***/
        //      Vector compElemArray = new Vector(((int[])get_info()).length * 2);
        //      compElemArray.clear();
        for(int i = 0 ; i <((int[])get_info()).length ; i++) //JL made change here
        {
            int lb =((PerfEstInfo)info).lower_bound(i);
            int ub =((PerfEstInfo)info).upper_bound(i);
            MLJ.ASSERT(((int[])get_info())[i] >= lb, "CompState.gen_succ: get_info().get(i) < lb.");
            MLJ.ASSERT(((int[])get_info())[i] <= ub, "CompState.gen_succ: get_info().get(i) > ub.");
            CompState upperState = null;
            CompState lowerState = null;
            if (((int[])get_info())[i] < ub) {
                int[] newValues = MLJArray.copy((int[])get_info());
                newValues[i]++;
                upperState = create_state(newValues);
                upperState.set_log_level(get_log_level());
                //            int where = compElemArray.size();
                int where = 0;
                ((CompElem)compElemArray.get(where)).num = i;
                ((CompElem)compElemArray.get(where)).value =((int[])upperState.get_info())[i];
            }
            if (((int[])get_info())[i] > lb) {
                int[] newValues = MLJArray.copy((int[])get_info());
                newValues[i]--;
                lowerState = create_state(newValues);
                lowerState.set_log_level(get_log_level());
                //            int where = compElemArray.size();
                int where = 0;
                ((CompElem)compElemArray.get(where)).num = i;
                ((CompElem)compElemArray.get(where)).value =((int[])lowerState.get_info())[i];
            }
            if (upperState != null)
                successors.add(upperState);
            if (lowerState != null)
                successors.add(lowerState);
        }
        evaluate_states(info, space, successors, computeReal);
        if (((PerfEstInfo)info).use_compound()&&((int[])get_info()).length >= 2) {
            logOptions.LOG(3, "creating compound nodes...\n");
            ListIterator pix = successors.listIterator();
            for(int i = 0 ;
            pix.hasNext();
            i++) {
                State state =(State)pix.next();
                ((CompElem)compElemArray.get(i)).eval = state.get_fitness();
                if (i>0 &&(((CompElem)compElemArray.get(i-1)).num ==
                ((CompElem)compElemArray.get(i)).num)) {
                    if (((CompElem)compElemArray.get(i)).eval >
                    ((CompElem)compElemArray.get(i-1)).eval)
                        ((CompElem)compElemArray.get(i-1)).eval = -1;
                    else
                        ((CompElem)compElemArray.get(i)).eval = -1;
                }
            }
            CompElem.sort(compElemArray);
            logOptions.LOG(5, "Feature Elem Array: " +compElemArray+ "\n");
            int startPosition = compElemArray.size()- 2;
            //         int[] baseValues = convertFrom(MLJArray.copy((boolean[])get_info()));
            int[] baseValues = MLJArray.copy((int[])get_info());
            double bestEval = 0;
            if (compElemArray.size()!= 0) {
                bestEval =((CompElem)compElemArray.get(compElemArray.size()- 1)).eval;
                baseValues[((CompElem)compElemArray.get(compElemArray.size()- 1)).num] =
                ((CompElem)compElemArray.get(compElemArray.size()- 1)).value;
            }
            while(startPosition >= 0 &&
            ((CompElem)compElemArray.get(startPosition)).eval >= 0) {
                baseValues[((CompElem)compElemArray.get(startPosition)).num] =
                ((CompElem)compElemArray.get(startPosition)).value;
                int[] newValues = MLJArray.copy(baseValues);
                logOptions.LOG(4, "Trying compound: ");
                CompState compState = create_state(newValues);
                compState.set_log_level(get_log_level());
                if (compState.get_node(space)== null) {
                    logOptions.LOG(4, " NOT IN GRAPH\n");
                    compState.eval(info, computeReal);
                    compState.set_eval_num(space.get_next_eval_num());
                    
                    successors.add(compState);
                }
                else {
                    logOptions.LOG(4, " in graph\n");
                    CompState tempState = compState;
                    compState =(CompState)(space.get_state(compState.get_node(space)));
                    tempState = null;
                }
                logOptions.LOG(2, "Compound Node " +compState+ "\n");
                if (compState.get_fitness()< bestEval) {
                    logOptions.LOG(3, "Stopped generating compound nodes\n");
                    break;
                }
                bestEval = compState.get_fitness();
                startPosition--;
            }
        }
        space.insert_states(this, successors);
        return successors;
    }
    
    abstract public CompState create_state(int[] initInfo);
    
    private int[] convertFrom(boolean[] other) {
        int[] rtrn = new int[other.length];
        for(int i = 0 ; i < other.length ; i++)
            if (other[i])
                rtrn[i] = 1;
            else rtrn[i] = 0;
        return rtrn;
    }
}