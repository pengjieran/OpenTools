package arithmetic.fss;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.GlobalOptions;
import arithmetic.shared.InstanceList;
import arithmetic.shared.MLJ;
import arithmetic.shared.Error;

/***************************************************************************
  StratifiedCV divides the training set into separate sets based on the 
label of each instance.  Cross Validation is then performed, taking 
instances from each small list in turn.  The overall process is similar to 
that used with CrossValidator. Assumes that the order and contents of the 
training list returned by the inducer is the same as the order and 
contents of the training list passed to the inducer. The training list may 
be altered by the inducer during training, but it must be returned to the 
same state.

  Complexity   : estimate_performance() takes O(numTimes*numFolds*
                   O(train_and_test()).
		 auto_estimate() takes O(estimate_performance() * number
		   of iterations).  Number of iterations is bounded
		   by Log_2(numFolds).
  Enhancements : Keep the relative order of instances intact.  This
		    seems to be hard if the inducer is allowed to
		    modify its training set (e.g. when it does cross
		    validation) since references to the list will change.
		 When a fraction of training set is specified, keep the
		    fraction stratified.  Currently, the fraction is
		    just a straight (random) fraction of the training
		    set.
@author James Louis	8/16/2001	Ported to Java.
@author Dan Sommerfield	10/15/94	Re-engineered to fit new PerfEstimator framework.
@author Dan Sommerfield	6/01/94	Initial revision as CS229B project.
***************************************************************************/


public class StratifiedCV extends CrossValidator {

/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private StratifiedCV(StratifiedCV source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(StratifiedCV source){}

/*****************************************************************************
  Return a short string identifying this estimator.
*****************************************************************************/
   public String description()
   {
      return ("Stratified " + super.description());
   }

   public StratifiedCV(int nFolds, int nTimes)
   {
      super(nFolds, nTimes);
   }

   public StratifiedCV(int nFolds)
   {
      super(nFolds, defaultNumTimes);
   }

   public StratifiedCV()
   {
      super(defaultNumFolds, defaultNumTimes);
   }

/*****************************************************************************
  This function uses an array of proportions to distribute num_to_distribute 
instances among the array "splits".  Each element in the split array 
indicates how many instances this particular category should receive (of the 
number given).  Extra instances are distributed probabilistically. When 
finished, the other two arrays are updated to reflect the assignments made 
to the splits array.  These act as counters to make sure all the numbers of 
instances add up properly.
*****************************************************************************/
protected void calculate_fold_split(int[] splits,
					int[] totalInSplit,
					int total,
					int numToDistribute)
{

   // assume that the lower/upper of all arrays are the same
//obs   MLJ.ASSERT(splits.low() == totalInSplit.low());
//obs   MLJ.ASSERT(splits.high() == totalInSplit.high());
   MLJ.ASSERT(splits.length == totalInSplit.length,
      "StratifiedCV.calculate_fold_split: splits.length != totalInSplit.length.");
   
   // distribute the integral portions to each split,
   // fill a parallel array with remainder information
   // sum up all the remainders (to determine how many extras we
   // have.)
   int instnum;
   double sum = 0;
   int extras = numToDistribute;
   double[] remainders = new double[splits.length];
   for(instnum = 0; instnum < splits.length; instnum++) {
      double fval = (double)numToDistribute * ((double)(totalInSplit[instnum]) /
				       (double)total);
      splits[instnum] = (int)fval;
      remainders[instnum] = fval - splits[instnum];
      sum += remainders[instnum];
      extras -= splits[instnum];
   }

   // distribute the extras probabilistically across the array
   // while we have extras left, get a random number from 0 to the
   // current sum of all probabilities in the remainder array.
   // Use this number to probabilistically choose an element from the
   // array.  This element receives an extra instance in its split
   // array.  After the extra instance is assigned, reduce the
   // probability for that instance to zero.
   while(extras > 0) {
      double indexor = rand_num_gen().nextDouble() * sum;
      for(instnum = 0; indexor >= remainders[instnum]; instnum++)
	 indexor -= remainders[instnum];

      // add an extra to the instance we just found
      extras --;
      sum -= remainders[instnum];
      remainders[instnum] = 0.0;
      splits[instnum]++;

      // Make sure we actually assigned an instance
      MLJ.ASSERT(instnum < splits.length,"StratifiedCV.calculate_fold_split: instnum >= splits.length.");
   }

   // finally, subtract from totalInSplit and total
   for(instnum = 0; instnum < splits.length; instnum++) {
      totalInSplit[instnum] -= splits[instnum];
      total -= splits[instnum];
   }

   // output the contents of the split array
   logOptions.LOG(6, "Split array:"+splits+"\n");
   logOptions.LOG(6, "Totals array:" + totalInSplit + "\n");
   logOptions.LOG(6, "Running total:" + total + "\n" + "\n");
}


/*****************************************************************************
  This function is responsible for constructing training and test lists for 
a given fold.  The construction of these lists relies on a preconstructed 
(once for each TIME, not fold) array which keeps information on how to split 
up the instances to handle the cross-validation in such a way that the 
results are not biased and so that no two training or test InstanceLists 
overlap. The large argument list is needed to allow us to pass in the 
precomputed array information.  The trainList and testList passed in should 
be empty.
*****************************************************************************/
protected void split_fold(InstanceList[] splitList,//splitList was InstanceListArray -JL
			      int[] totalSize,
			      int totalInstances,
			      int totalCounter,
			      int fold, int folds,
			      InstanceList trainList,
			      InstanceList testList) {

   MLJ.ASSERT(trainList.no_instances(),"StratifiedCV.split_fold: trainList has no instances.");
   MLJ.ASSERT(testList.no_instances(),"StratifiedCV.split_fold: testList has no instances.");

   // set up an array to tell us how many instances each
   // split should receive.  Use the calculate_fold_split function here
   int[] numInSplit = new int[splitList.length];
   int numInFold = (totalInstances / folds);
   if(totalInstances % folds > fold) numInFold++;
   calculate_fold_split(numInSplit, totalSize, totalCounter, numInFold);
   
   for (int k = 0; k <= splitList.length; k++) {
      logOptions.LOG(4, "Number in split "+fold+ 
	  "/" + k + ": " + numInSplit[k] +"\n");

      // split the list and copy its parts
      InstanceList trainSegment = splitList[k];
      InstanceList testSegment = trainSegment.split_prefix(numInSplit[k]);
      InstanceList trainSegmentCopy = (InstanceList)trainSegment.clone();
      InstanceList testSegmentCopy = (InstanceList)testSegment.clone();
      // unite the originals in "rotated" position and stick the
      // united list back into the array
      trainSegment.unite(testSegment);

      // unite the copies into the testList and trainList
      testList.unite(testSegmentCopy);
      trainList.unite(trainSegmentCopy);
   }
   // We don't print a newline so error can come on the same line in
   // loglevel 3 and higher.
   logOptions.LOG(3, "Number of instances in fold " + fold + ": " + numInFold + ". ");
}

/*****************************************************************************
  Estimates performance for a single run of cross validation. Multiple runs 
are handled by the base class.
*****************************************************************************/
protected double estimate_time_performance(BaseInducer inducer,
			   InstanceList dataList, int time, int folds)
{
   int totalInstances = dataList.num_instances();

   // create basic lists for cross validation
   InstanceList shuffledList = dataList.shuffle(rand_num_gen());
   InstanceList trainList = new InstanceList(dataList.get_schema());
   InstanceList testList = new InstanceList(dataList.get_schema());

   // split the training list by label
   InstanceList[] splitList = shuffledList.split_by_label();
   logOptions.LOG(3, "Number of splits: " +splitList.length +"\n");

   // create an array parallel to splitList containing the number of
   // instances in each split.  The elements in this array will decrease
   // as instances are assigned to training and test lists (this array
   // serves as a counter)
   int[] totalSize = new int[splitList.length];
   for(int i = 0; i < splitList.length; i++)
      totalSize[i] = splitList[i].num_instances();

   // also set up counter of the total number of instances left to
   // be assigned
   int totalCounter = totalInstances;

   // now, run cross validation for each split
   PerfData foldData = new PerfData();
   logOptions.DRIBBLE(folds + " folds: ");
   for (int fold = 0; fold < folds; fold++) {
      logOptions.DRIBBLE(fold + 1 + " ");
      // calculate the train and test lists using split_fold
      split_fold(splitList, totalSize, totalInstances, totalCounter, 
		 fold, folds, trainList, testList);
	
      // display the lists
      logOptions.LOG(6, "Test list: \n" + testList + "\n");
      logOptions.LOG(6, "Train list: \n" + trainList + "\n");

      // shuffle training list, then call train-and-test
      InstanceList shuffledTrainList = trainList.shuffle(rand_num_gen());

      // pull out an (unstratified) fraction of the training list,
      // if called for
      InstanceList fractList = shuffledTrainList;
      if(fraction < 1.0) {
	 int numInTrain = shuffledTrainList.num_instances();
	 int numInFract = (int)(fraction * (double)numInTrain + 0.5);
	 if(numInFract <= 0)
	    Error.fatalErr("StratifiedCV::estimate_time_performance: number of "
	       +"instances in the fraction <= 0");
	 fractList = shuffledTrainList.split_prefix(numInFract);
      }
      boolean saveDribble = GlobalOptions.dribble;
      GlobalOptions.dribble = false;
      double error = 
	 train_and_test(inducer, fractList, testList,
			"-" + time + "-" + fold,
			perfData);
      GlobalOptions.dribble = saveDribble;
      foldData.insert_error(error);
      
      // clean up
      shuffledTrainList = null;
      if(fraction < 1.0)
	 fractList = null;
      trainList.remove_all_instances();
      testList.remove_all_instances();
   }
   logOptions.DRIBBLE("\n");

   // when we're done with all folds, all counters should be zero
   // otherwise, we've assigned things incorrectly
   MLJ.ASSERT(totalCounter == 0,"StratifiedCV.estimate_time_performance: totalCounter != 0.");
   for(int cc = 0; cc < totalSize.length; cc++) {
      MLJ.ASSERT(totalSize[cc] == 0,"StratifiedCV.estimate_time_performance: totalSize[cc] != 0.");
   }
      
   shuffledList = null;
   trainList = null;
   testList = null;
   splitList = null;
   return foldData.get_error_data().mean();
}


}
