package arithmetic.fss;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.Error;
import arithmetic.shared.Basics;
import arithmetic.shared.GlobalOptions;
import arithmetic.shared.InstanceList;
import arithmetic.shared.MLJ;

public class CrossValidator extends PerfEstimator {

   private int numFolds;
   private int numTimes;

   protected double fraction;

   public static int  defaultNumFolds = 10;
   public static int  defaultNumTimes = 1;
   public static int  defaultMaxFolds = 20;        // for auto_set_folds
   public static int  defaultAutoFoldTimes;   // for auto_set_folds
   public static double defaultStdDevEpsilon = 0.001;   // for auto_set_folds
   public static double defaultAccEpsilon = 0.005;      // for auto_set_folds
   public static int  defaultMaxTimes = 10;        // for auto_estimate
   public static double defaultAutoStdDev = 0.01;      // for auto_estimate


/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private CrossValidator(CrossValidator source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(CrossValidator source){}


/***************************************************************************
  Description : Estimate error for a single time (multi fold).
  Comments    : protected function
***************************************************************************/
protected double estimate_time_performance(BaseInducer inducer,
					    InstanceList dataList, 
					    int time, int folds)
{
   int totalInstances = dataList.num_instances();

   InstanceList shuffledList = dataList.shuffle(rand_num_gen());
   PerfData foldData = new PerfData();
   logOptions.DRIBBLE(folds + " folds: ");
   for (int fold = 0; fold < folds; fold++) {
      logOptions.DRIBBLE(fold + 1 + " ");
      int numInSplit = totalInstances / folds +
	 ((totalInstances % folds > fold)? 1:0);
      logOptions.LOG(3, "Number of instances in fold " + fold + ": " +
	  numInSplit + ".");
      InstanceList testList = shuffledList.split_prefix(numInSplit);
      InstanceList fractList = shuffledList;
      logOptions.LOG(3, "Total weights of fold " + fold + " (train/test): "
	  + fractList.total_weight() + "/" + testList.total_weight()
	  + "\n");
      
      if(fraction < 1.0) {
	 double numInFract = fraction * (double)(shuffledList.num_instances());
	 int intNumInFract = (int)(numInFract + 0.5);
	 if(intNumInFract == 0)
	    Error.fatalErr("CrossValidator.estimate_time_performance: "
	       +"No instances left in cv fraction");
	 fractList = shuffledList.split_prefix(intNumInFract);
      }
      
      logOptions.LOG(6, "Training set is:" + "\n" + fractList + "\n");
      logOptions.LOG(6, "Test set is:" + "\n" + testList + "\n");
      boolean saveDribble = GlobalOptions.dribble;
      GlobalOptions.dribble = false;
      double error = 
	 train_and_test(inducer, fractList, testList,
			"-" + time + "-" + fold,
			perfData);
      GlobalOptions.dribble = saveDribble;
      logOptions.LOG(3, "Error " + error + "\n");
      foldData.insert_error(error);
      
      if(fraction < 1.0) {
	 // clean up fractional list
	 fractList.unite(shuffledList);
	 shuffledList = fractList;
      }
      
      shuffledList.unite(testList);
      testList = null; //testList is returned null from InstanceList.unite -JL
      MLJ.ASSERT(testList == null,"CrossValidator.estimate_time_performance: testList != null.");
   }
   logOptions.DRIBBLE("\n");
   logOptions.LOG(2, "fold " + foldData + ". ");
   shuffledList = null;

   return foldData.get_error_data().mean();
}   

/***************************************************************************
  Contructor for cross-validation. See estimate_performance with same 
arguments.
***************************************************************************/
public CrossValidator(int nFolds, int nTimes)
{
   set_folds(nFolds);
   set_times(nTimes);
   set_fraction(1.0);
}

/***************************************************************************
  Contructor for cross-validation. See estimate_performance with same 
arguments.
***************************************************************************/
public CrossValidator(int nFolds)
{
   set_folds(nFolds);
   set_times(defaultNumTimes);
   set_fraction(1.0);
}

/***************************************************************************
  Contructor for cross-validation. See estimate_performance with same 
arguments.
***************************************************************************/
public CrossValidator()
{
   set_folds(defaultNumFolds);
   set_times(defaultNumTimes);
   set_fraction(1.0);
}


public void set_folds(int num)
{
   if (num == 0)
      Error.fatalErr( "CrossValidator.set_folds: num folds (" + num + ") == 0");

   numFolds = num;
}
   
public void set_times(int num)
{
   if (num <= 0)
      Error.fatalErr( "CrossValidator.set_times: num times (" + num + ") <= 0");
   numTimes = num;

}

public void set_fraction(double fract)
{
   if( fract <= 0 || fract > 1.0)
      Error.fatalErr( "CrossValidator.set_fraction: " + fract + " is out of the range (0,1]");
   fraction = fract;
}

public int get_folds(){return numFolds;}
public int get_times(){MLJ.ASSERT(numTimes > 0,"CrossValidator.get_times: numTimes <= 0"); return numTimes;}

/***************************************************************************
  Prints identifying string for this estimator.  This includes number of 
times and folds.
***************************************************************************/
public String description()
{
  return numTimes + "x" + numFolds + " Cross validator";
}

/***************************************************************************
  Trains and tests the inducer using "numFolds"-fold cross-validation, and 
repeated numTimes times. If numFolds is negative, it means leave-k-out, 
where k is Math.abs(numFolds). Shuffles the trainList before each time cross 
validation is performed. Use init_rand_num_gen(seed) to achieve 
reproducible results; otherwise results may vary because of variations in 
the shuffling of the data.
***************************************************************************/

// Helper function to help support leave-k-out
public static String compute_folds(int numFolds, int totalInstances)
{
   if (totalInstances == 0)
	 Error.fatalErr("CrossValidator::estimate_performance: 0 instances in dataList");

   if (totalInstances == 1)
      Error.fatalErr("CrossValidator::estimate_performance: Cannot estimate error for 1 instance" ); 

   if (numFolds == 0 || numFolds > totalInstances)
      Error.fatalErr("CrossValidator::estimate_performance: number of folds ("
	  + numFolds + ") is invalid for data with "
	  + totalInstances + " instances" );
   
   String foldsStr;
   if (numFolds > 0) 
      foldsStr = String.valueOf(numFolds);
   else { // leave-Math.abs(numFolds)-out
      int actualFolds = (int)(Math.ceil((double)(totalInstances) / Math.abs(numFolds)) + 0.5);
      if (actualFolds < 2)
	 Error.fatalErr("CrossValidator::estimate_performance: number of folds ("
	     + numFolds + ") will leave no training instances");
      foldsStr = actualFolds + " (leave "
             + Math.abs(numFolds) + " out)";
      numFolds = actualFolds;
      MLJ.ASSERT(numFolds > 1,"CrossValidator.compute_folds: numFolds <= 1.");
   }
   return foldsStr;
}

public double estimate_performance(BaseInducer inducer, 
				       InstanceList dataList)
{
   // copy dataList in slow debug mode to check for ordering problems
   InstanceList dataListPtr = dataList;
   if (Basics.DBGSLOW) dataListPtr = (InstanceList)dataList.clone();
   
   int totalInstances = dataList.num_instances();
   int actualFolds = numFolds; // cannot be negative
   String foldsStr = compute_folds(actualFolds, totalInstances);

   logOptions.LOG(1, "Inducer: " + inducer.description() + "\n");
   logOptions.LOG(1, "Number of folds: " + foldsStr + ", Number of times: " 
       + numTimes + "\n");
   perfData = null;
   perfData = new PerfData();
   for (int time = 0; time < numTimes; time++) {
      if (numTimes > 1)
	 logOptions.DRIBBLE("Time " + time + "\n");
      estimate_time_performance(inducer, dataListPtr, time, actualFolds);
      if (numTimes > 1) // Don't print this if it's only once because we get
			// the same output below
         logOptions.LOG(2, "Overall: " + this + "\n");
   }

   // check the copied data list ordering in slow debug mode to
   // make sure CValidator does not mess up list ordering
   if (Basics.DBGSLOW){ 
      if(!(dataList == dataListPtr))
        Error.err( "CrossValidator::estimate_performance: ordering of dataList "
               +"changed during cross-validation" + "\n");
      dataListPtr = null;
   }

   if (perfData.size() != numTimes * actualFolds)
      Error.fatalErr("CrossValidator:estimate_performance error size "
	  + perfData.size() + " does not match expected size "
	  + (numTimes * actualFolds)
	  + ". Probable cause: train_and_test not updating perfData");
   
   logOptions.LOG(1, "Untrimmed error ");
   if(get_log_level() >= 1) display_error_data(get_log_stream());
   logOptions.LOG(1, "\n");

   // set cost within this accData
   perfData.insert_cost(actualFolds * numTimes);
   return error();
}

/***************************************************************************
  Automatically estimate error to the given std-dev level.
***************************************************************************/

public double auto_estimate_performance(BaseInducer inducer,
					    InstanceList dataList,
					    double desiredStdDev,
					    int  maxTimes)
{
   if (numFolds == -1)
      Error.fatalErr("CrossValidator::auto_estimate_performance: it does not make "
	     +" sense to do leave-one-out multiple times");

   int totalInstances = dataList.num_instances();
   int actualFolds = numFolds; // cannot be negative
   compute_folds(actualFolds, totalInstances);

   logOptions.LOG(1, "Inducer: " + inducer.description() + "\n");
   logOptions.DRIBBLE("Number of folds: " + actualFolds + ". Looping until "
          +"std-dev of mean =" + desiredStdDev + "\n");
   perfData = null;
   perfData = new PerfData();
   int time = 0;
   do {
      set_times(time + 1); // so asserts will work.
      estimate_time_performance(inducer, dataList, time, actualFolds);
      // The NULL in the statement below belongs to the else in the LOG()
      //   macro.   The compiler crashes without it!  If you take it out and
      //   it works, leave it out.
      logOptions.LOG(2, "Overall error: " + this + "\n");
   } while (++time < maxTimes && error_std_dev() > desiredStdDev);

   set_times(time); // so caller can do do get_times();

   if (perfData.size() != actualFolds * numTimes)
      Error.fatalErr("CrossValidator:auto_estimate_performance error size "
	  + perfData.size() + " does not match expected size "
	  + (actualFolds * time)
	  + ". Probable cause: train_and_test not updating perfData");
   
   logOptions.LOG(1, "Untrimmed error " + this + "\n");

   perfData.insert_cost(actualFolds * get_times());
   
   return error();
}

public double auto_estimate_performance(BaseInducer inducer,
					    InstanceList dataList,
					    double desiredStdDev)
{
   return auto_estimate_performance(inducer,dataList,desiredStdDev,defaultMaxTimes);
}

public double auto_estimate_performance(BaseInducer inducer,
					    InstanceList dataList)
{
   return auto_estimate_performance(inducer,dataList,defaultAutoStdDev,defaultMaxTimes);
}

/***************************************************************************
  Trains and tests the inducer using files. Uses the files fileStem.names, 
fileStem-T-F.data, and fileStem-T-F.test, where T is an integer in the range
[0, numTimes-1] and F is an integer in the range [0, numFolds-1]. 
Unimplemented in this version.
***************************************************************************/

public double estimate_performance(BaseInducer inducer, String fileStem)
{
   perfData = null;
   perfData = new PerfData();
   
   for (int time = 0; time < numTimes; time++) {
      double err =
	 estimate_file_performance(inducer, numFolds,
				fileStem + "-" + time, perfData);
      logOptions.LOG(2, "fold error for set " + time + " is " + err + "\n");
   }

   logOptions.LOG(1, "error: " + this + "\n");

   return error();
}

/***************************************************************************
  Attempt to find good values for the numFolds that will be suitable for 
the current training set size. This is an expensive operation that is 
useful if you intend to do many cross validations on variants of this 
dataset. It decreases the number of folds until the error or std-dev 
deteriorates.
***************************************************************************/

public void auto_set_folds(BaseInducer inducer,
				    InstanceList dataList,
				    int maxFolds,
				    int maxTimes,
				    double accEpsilon,
				    double stdDevEpsilon)

{
   int saveLogLevel = get_log_level();
   int saveNumTimes = get_times();
   set_folds(maxFolds); 
   double prevStdDev = 1;       // high number for StdDev
   double prevErr = 1.0;        // worst possible error
   int  prevFolds = maxFolds; // in case we stop on first iteration.

   if (maxFolds < 2)
      Error.fatalErr("CrossValidator::auto_set_folds: maxFolds: " + maxFolds
	  + " less than 2");
   
   if (stdDevEpsilon < 0)
      Error.fatalErr("CrossValidator::auto_set_folds: stdDevEpsilon: "
	  + stdDevEpsilon + " negative");

   if (accEpsilon < 0)
      Error.fatalErr("CrossValidator::auto_set_folds: accEpsilon: "
	  + accEpsilon + " negative");

   if (maxFolds > dataList.num_instances()) {
      maxFolds = dataList.num_instances();
      logOptions.LOG(1, "Max folds set to number of instances: " + maxFolds + "\n");
   }

   do {
      logOptions.LOG(2, "auto_set_folds trying " + get_folds() + " folds...");
      set_log_level(saveLogLevel - 2); // estimate at loglevel-2
      auto_estimate_performance(inducer, dataList, defaultAutoStdDev,
				maxTimes);
      set_log_level(saveLogLevel);

      // The real std-dev is higher since we're suppose to find
      //   a good value for 1xfolds.
      double stdDev = error_std_dev() * Math.sqrt(get_times());
      double errorRate = error();

      logOptions.LOG(2, "mean " + Math.round(error()) * 100 + '%' 
          + " +- " + Math.round(stdDev) * 100 + '%' 
	  + " (average of " + get_times() + ")" +  "\n");

      // If one of the following is true, we stop and return previous number
      // of folds:
      //   1. Error increases by accEpsilon.
      //   2. Std-dev goes up by stdDevEpsilon.
      
      if (errorRate >= prevErr + accEpsilon ||
	  stdDev >= prevStdDev + stdDevEpsilon) {
	 set_times(saveNumTimes);
	 MLJ.ASSERT(prevFolds >= 2,"CrossValidator.auto_set_folds: prevFolds < 2.");
	 set_folds(prevFolds);
	 logOptions.LOG(1, "Fold setting set to " + get_folds()
	     + " (significant deterioration or above std-dev threshold)"
	     + "\n");
	 return;
      }

      prevStdDev = stdDev;
      prevErr = errorRate;
      prevFolds = get_folds();
      int newFolds = get_folds() / 2;
      if (newFolds < 2)
	 newFolds = 2;
      set_folds(newFolds);
   } while (prevFolds > 2);

   set_times(saveNumTimes);
   logOptions.LOG(1, "Fold setting set to " +  get_folds()
           + " (minimum possible)" + "\n");
   return;
}

public void auto_set_folds(BaseInducer inducer,
				    InstanceList dataList)

{
   auto_set_folds(inducer,dataList,defaultMaxFolds,defaultMaxTimes,defaultAccEpsilon,defaultStdDevEpsilon);
}

public void auto_set_folds(BaseInducer inducer,
				    InstanceList dataList,
				    int maxFolds)

{
   auto_set_folds(inducer,dataList,maxFolds,defaultMaxTimes,defaultAccEpsilon,defaultStdDevEpsilon);
}

public void auto_set_folds(BaseInducer inducer,
				    InstanceList dataList,
				    int maxFolds,
				    int maxTimes)

{
   auto_set_folds(inducer,dataList,maxFolds,maxTimes,defaultAccEpsilon,defaultStdDevEpsilon);
}

public void auto_set_folds(BaseInducer inducer,
				    InstanceList dataList,
				    int maxFolds,
				    int maxTimes,
				    double accEpsilon)

{
   auto_set_folds(inducer,dataList,maxFolds,maxTimes,accEpsilon,defaultStdDevEpsilon);
}

/*
public:
   // Public data

   // Methods
   // Auto-settings attempts to set numFolds by an ad-hoc to a reasonable
   //   number such that the variance resulting from the training-set size is
   //   not too big.
   virtual void auto_set_folds(BaseInducer& inducer, InstanceList& dataList,
			       int maxFolds = defaultMaxFolds,
			       int maxTimes = defaultMaxTimes,
			       double AccEpsilon  = defaultAccEpsilon,
			       double stdDevEpsilon = defaultStdDevEpsilon);
*/
}
