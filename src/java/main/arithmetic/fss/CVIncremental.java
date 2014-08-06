package arithmetic.fss;

import java.util.ListIterator;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.Error;
import arithmetic.shared.Basics;
import arithmetic.shared.CatTestResult;
import arithmetic.shared.GlobalOptions;
import arithmetic.shared.IncrInducer;
import arithmetic.shared.Instance;
import arithmetic.shared.InstanceList;
import arithmetic.shared.MLJ;

/***************************************************************************
  An incremental cross validator utilizes the fact that some inducers are 
incremental.  This allows it to do high-fold cross validation fast, 
most notably, leave-one-out, or m-fold CV for m instances. The inducer 
must be an incremental inducer.

  Complexity   : The time should be approximately independent of m, i.e.,
                   leave-one-out should be just as fast as leave-two-out.
		 The complexity of estimate_time_performance is the time it
		   takes to delete the instances from the inducer and add them
		   back, plus the InstanceList operations which have an
		   expected constant time (hash table + instance) per instance.
@author James Louis	8/17/2001	Ported to Java.
@author Yeogirl Yun and Ronny Kohavi	10/10/94	
		Initial revision (.h,.c)
***************************************************************************/


public class CVIncremental extends CrossValidator {
/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private CVIncremental(CVIncremental source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(CVIncremental source){}


/******************************************************************************
  Generate folds and estimate their performance. Note that leave-one-out is 
not affected by the "time" parameter because all times will give the same 
behavior.
******************************************************************************/
protected double estimate_time_performance(BaseInducer baseInducer,
			   InstanceList dataList, int time, int folds)
{
   if (perfData == null)
      Error.fatalErr("CVIncremental.estimate_time_performance(): must be called by "
	 +"estimate performance, or another routine which initializes the "
	 +"performance data");

   if (baseInducer.can_cast_to_incr_inducer() == false)
      Error.fatalErr("CVIncremental.estimate_time_performance() : Inducer "
	  + baseInducer.description() + " must be an IncrInducer.");

   IncrInducer inducer = baseInducer.cast_to_incr_inducer();


   InstanceList shuffledList = dataList.shuffle(rand_num_gen());
   InstanceList oldList = inducer.assign_data(shuffledList);
   inducer.train();

   int totalInstances = dataList.num_instances();
   
   PerfData foldData = new PerfData();
   logOptions.DRIBBLE(folds + " folds: ");
   for (int fold = 0; fold < folds; fold++) {
      logOptions.DRIBBLE(fold + 1 + " ");
      int numInSplit = totalInstances / folds + 
	 ((totalInstances % folds > fold)? 1:0);

      InstanceList testList = incremental_split_prefix(inducer, numInSplit);

      dump_data("-" + time + "-" + fold,
		description(),
		inducer.instance_list(),
		testList);

      boolean saveDribble = GlobalOptions.dribble;
      GlobalOptions.dribble = false;

      {  // We must deallocate CatTestResult before we call
	 // incremental_unite, because the test set is deleted by incremental
	 //   unite, and CatTestResult has references to test set
	 CatTestResult results = new CatTestResult(inducer.get_categorizer(),
			       baseInducer.instance_list(), testList);

	 perfData.insert(results);
	 // @@ erase after tests pass.
	 // update_acc_data(results.num_correct(), results.num_incorrect(),
	 // accData);
	 // @@ remove next line soon!
	 /*	 perfData.get_acc_data(). //This was commented before I got here -JL
	    insert_error(Real(results.num_correct()) / 
			    (results.num_correct() +
			     results.num_incorrect())); */
	 foldData.insert(results);
      }
      GlobalOptions.dribble = saveDribble;

      incremental_unite(inducer, testList);

      if (Basics.DBGSLOW) MLJ.ASSERT(totalInstances== inducer.instance_list().num_instances(),
         "CVIncremental.estimate_time_performance: "
         +"totalInstances!= inducer.instance_list().num_instances()");
   }

   logOptions.DRIBBLE("\n");
   inducer.assign_data(oldList);
   return foldData.get_error_data().mean();
}  


/***************************************************************************
  Split a fold from the training set, and incrementally update the inducer. 
An allocated testList is returned (caller gets ownership). Local function.
***************************************************************************/

static InstanceList incremental_split_prefix(IncrInducer inducer,
					      int numInst)
{
   InstanceList testList = new InstanceList(inducer.instance_list().get_schema());

   for (int i = 0; i < numInst; i++) {
      ListIterator pix = inducer.instance_list().instance_list().listIterator();
      MLJ.ASSERT(pix != null,"CVIncremental.incremental_split_prefix: pix == null.");
      testList.add_instance(inducer.del_instance(pix));
      // pix is updated above, and must not be NULL except for the last
      //   instance. 
      MLJ.ASSERT(pix != null || i == numInst - 1,
         "CVIncremental.incremental_split_prefix: pix == null && "
         +"i != numInst - 1.");
   }
   return testList;
}

/***************************************************************************
  Unite test list back to training set, incrementally updating the inducer.  
We delete the testList.
***************************************************************************/

static void incremental_unite(IncrInducer inducer, InstanceList testList)
{
   for (ListIterator pix = testList.instance_list().listIterator(); pix.hasNext();) {
      ListIterator addPix = inducer.add_instance((Instance)pix.next());
      MLJ.ASSERT(addPix != null,"CVIncremental.incremental_unite: addPix == null.");
   }

   testList = null;
}   


public CVIncremental()
{
   super(defaultNumFolds,defaultNumTimes);
}

public CVIncremental(int nFolds)
{
   super(nFolds,defaultNumTimes);
}

public CVIncremental(int nFolds,int nTimes)
{
   super(nFolds,nTimes);
}


//Test functions and variables

static private String NAMES_FILE = "t_CValidator.names"; // yes, use the same file as 
static private String DATA_FILE  = "t_CValidator.data";  // t_CValidator.
/*

#ifdef INTERACTIVE
  const int NUM_TIMES = 1;
  const int MAX_NUM_INST = 1;
#else
  const int NUM_TIMES = 2;
  const int MAX_NUM_INST = 5;
#endif
const int RAND_SEED = 7258789;


// numInst is the number of instances in a fold.
static private void compare_validators(InstanceList data, int times, int numInst)
{
   Globals.Mcout.write("Comparing "+times+'x' + -numInst + " cross validation."+"\n");

   CrossValidator crossValidator = new CrossValidator(-numInst, times);
   crossValidator.init_rand_num_gen(RAND_SEED);
   TableInducer tabInducer = new TableInducer("Table Inducer", true);
   double acc1 = crossValidator.estimate_performance(tabInducer, data);
   double stdDev1 = crossValidator.error_std_dev();
   Globals.Mcout.write("Error for CValidator: "+acc1+" +- "+stdDev1+"\n");

   CVIncremental incrValidator = new CVIncremental(-numInst, times);
   incrValidator.init_rand_num_gen(RAND_SEED);
   double acc2 = incrValidator.estimate_performance(tabInducer, data);
   double stdDev2 = incrValidator.error_std_dev();
   Globals.Mcout.write("Error for CVIncremental : "+acc2+" +- "+stdDev2+"\n"+"\n");
   MLJ.ASSERT(MLJ.approx_equal(acc1, acc2));
   MLJ.ASSERT(MLJ.approx_equal(stdDev1, stdDev2));
}



public static void main(String[] args){
   Mcout << "t_CVIncremental executing" << endl;
   InstanceList monk("monk1-full");
   InstanceList orgInstList(EMPTY_STRING, NAMES_FILE, DATA_FILE);
   
   // project, removing an attribute so we'll have some duplicates.
   BoolArray mask(4);
   mask[0] = 1; mask[1] = 0; mask[2] = 1; mask[3] = 1;
   InstanceList& instList = *(orgInstList.project(mask));
   Mcout << instList << endl;

   for (int times = 1; times <= NUM_TIMES; times++)
      for (int numInst = 1; numInst <= MAX_NUM_INST; numInst++) {
	 compare_validators(instList, times, numInst);
#ifndef INTERACTIVE
	 if (numInst == 3 && times == 1)
	    compare_validators(monk, times, numInst);
#endif
      }

   delete &instList;


   return 0;
   
}
*/
}
