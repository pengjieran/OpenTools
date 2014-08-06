package arithmetic.fss;

import java.io.IOException;
import java.io.Writer;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.CatTestResult;
import arithmetic.shared.Error;
import arithmetic.shared.GlobalOptions;
import arithmetic.shared.InstanceList;
import arithmetic.shared.LogOptions;
import arithmetic.shared.MEnum;
import arithmetic.shared.MLJ;

//PerfEstDispatch.c is in MWrapper.

public class PerfEstDispatch{
/* ErrorType ENUM */
   public final static int classError = 0;
   public final static int meanSquaredError = 1;
   public final static int meanAbsoluteError = 2;
   public final static int classLoss = 3;
/* END ENUM */

   // note: PerfEstimationMethod is public but must be declared BEFORE
   // perfEstimationMethod, which is private.  Hence, the violation of
   // ordering rules here.

/* PerfEstimationMethod ENUM */
   public final static int cv = 0;
   public final static int stratCV = 1;
   public final static int testSet = 2;
   public final static int bootstrap = 3;
   public final static int holdOut = 4;
   public final static int automatic = 5;
/* END ENUM */

/**  **/
   protected int DEFAULT_PERF_ESTIMATOR = automatic; //PerfEstimationMethod enum
   protected int DEFAULT_ERROR_TYPE = classError;  //ErrorType enum
   protected double DEFAULT_CV_WEIGHT_THRESHOLD = 5000;
   protected int DEFAULT_SEED = 7258789;
   protected int DEFAULT_CV_FOLDS = 10;
   protected int DEFAULT_CV_TIMES = 1;
   protected double DEFAULT_CV_FRACT = 1.0;
   protected int DEFAULT_MAX_TIMES = 5;
   protected double DEFAULT_DES_STD_DEV = 0.01;
   protected double DEFAULT_ERROR_TRIM = 0.0;
   protected int DEFAULT_BOOTSTRAP_TIMES = 10;
   protected double DEFAULT_BOOTSTRAP_FRACTION = 0.632;
   protected int DEFAULT_HOLDOUT_TIMES = 1;
   protected int DEFAULT_HOLDOUT_NUMBER = 0;
   protected double DEFAULT_HOLDOUT_PERCENT = 2/3; //Real(2)/3;

// help strings for options
   protected String PERF_ESTIMATION_METHOD_HELP =
      "This option selects the method of performance estimation to use in "
      +"evaluating states.  We also allow use of the real error rate (when "
      +"available) to get an upper bound on performance of the search.";
   protected String ERROR_TYPE_HELP =
      "This option selects the dominant type of error estimate to produce.  "
      +"Error chooses raw classification error rate.  Mean squared error and "
      +"mean absolute error are metrics which attempt to assess the performance "
      +"of probability estimates.  Loss allows the loss matrix (if provided) "
      +"to alter performance estimates.";
   protected String CV_WEIGHT_THRESHOLD_HELP =
      "The instance list total weight threshold below which using the automatic "
      +"performance estimation method will result in cross-validation, and above "
      +"which will result in hold out.";
   protected String SEED_HELP =
      "This option specifies a specific seed for the random number generator.";   
   protected String CV_FOLDS_HELP =
      "This option specifies the number of folds to use for cross-validation."
      +"  Specifying a negative number -k produces the leave-k-out algorithm."
      +"  It is an error to choose zero or one.";
   protected String CV_TIMES_HELP =
      "This option specifies the number of times to run cross-validation.  "
      +"Choosing zero times will cause the program to automatically select "
      +"an appropriate number in order to minimize variance.";
   protected String CV_FRACT_HELP =
      "This option specifies the fraction of the proposed training set to "
      +"use within each fold of cross-validation.  Choosing 1.0 performs "
      +"standard cross-validation.";
   protected String MAX_TIMES_HELP =
      "This option specifies the maximum number of times to try to run "
      +"cross-validation while attempting to achieve the desired standard "
      +"deviation.";
   protected String DES_STD_DEV_HELP =
      "This option specifies the desired standard deviation for an automatic"
      +" run of cross-validation.";
   protected String ERROR_TRIM_HELP =
      "This option specifies the trim used for determining error means.";
   protected String BOOTSTRAP_TIMES_HELP =
      "This option specifies the number of times to run bootstrap.";
   protected String BOOTSTRAP_FRACTION_HELP =
      "This option specifies the weight given to the bootstrap sample in the "
      +"bootstrap formula.  Typical values are 0.632 and 0.5.";
   protected String HOLDOUT_TIMES_HELP =
      "This option specifies the number of times to repeat holdout.";
   protected String HOLDOUT_NUMBER_HELP =
      "This option specifies an exact number of instances to hold out for "
      +"training or testing.  Select a positive number to hold out instances "
      +"for training.  Select a negative number to hold out instances for "
      +"testing.  Select zero to choose a percentage instead.";
   protected String HOLDOUT_PERCENT_HELP =
      "This option specifies a percentage of instances to hold out for "
      +"training.";


   public static MEnum perfEstimationMethodEnum = new MEnum();

   public static MEnum errorTypeEnum;

   double errTrim;
   double stdDevTrim;
   int randSeed;
   double cvWeightThreshold;
   
   // CV options
   int cvFolds;
   int cvTimes;
   int maxTimes;
   double cvFraction;
   double desStdDev;

   // Bootstrap options
   int bootstrapTimes;
   double bootstrapFraction;

   // HoldOut options
   int hoTimes;
   int hoNumber;
   double hoPercent;

   // results
   PerfData perfData = new PerfData();
   int actualTimes;   

   int perfEstimationMethod; //PerfEstimationMethod enum
   int errorType; //ErrorType enum

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
   private PerfEstDispatch(PerfEstDispatch source){}

   public PerfEstDispatch() { set_defaults(); }

   public void set_defaults()
   {
      set_perf_estimator(DEFAULT_PERF_ESTIMATOR);
      set_error_type(DEFAULT_ERROR_TYPE);
      set_seed(DEFAULT_SEED);
      set_cv_weight_threshold(DEFAULT_CV_WEIGHT_THRESHOLD);
      set_cv_folds(DEFAULT_CV_FOLDS);
      set_cv_times(DEFAULT_CV_TIMES);
      set_cv_fraction(DEFAULT_CV_FRACT);
      set_max_times(DEFAULT_MAX_TIMES);
      set_desired_std_dev(DEFAULT_DES_STD_DEV);
      set_error_trim(DEFAULT_ERROR_TRIM);
      set_bootstrap_times(DEFAULT_BOOTSTRAP_TIMES);
      set_bootstrap_fraction(DEFAULT_BOOTSTRAP_FRACTION);
      set_holdout_times(DEFAULT_HOLDOUT_TIMES);
      set_holdout_number(DEFAULT_HOLDOUT_NUMBER);
      set_holdout_percent(DEFAULT_HOLDOUT_PERCENT);
   }

/*bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb 1
   public void set_user_options(){set_user_options("");}
   public void set_user_options(String prefix)
   {
      set_perf_estimator(
         get_option_enum(
            (prefix + "PERF_ESTIMATOR"), perfEstimationMethodEnum,
            get_perf_estimator(), PERF_ESTIMATION_METHOD_HELP,
            false));
      if (perfEstimationMethod == automatic)
         cvWeightThreshold =
            get_option_real_range(prefix + "CV_WEIGHT_THRESHOLD",
            DEFAULT_CV_WEIGHT_THRESHOLD, 0.0, REAL_MAX,
            CV_WEIGHT_THRESHOLD_HELP, false, false);
      set_error_type(
         get_option_enum(
            (prefix + "ERROR_TYPE"), errorTypeEnum,
            get_error_type(), ERROR_TYPE_HELP, true));
      set_seed(
         get_option_int(prefix + "PERF_EST_SEED", get_seed(),
            SEED_HELP, true));
      set_error_trim(
         get_option_real(prefix + "ERROR_TRIM", get_error_trim(),
            ERROR_TRIM_HELP, true));

      if(perfEstimationMethod == cv || perfEstimationMethod == stratCV ||
         perfEstimationMethod == automatic)
         {
            set_cv_folds(
               get_option_int(prefix + "CV_FOLDS", get_cv_folds(),
                  CV_FOLDS_HELP, false));

            set_cv_times(
               get_option_int(prefix + "CV_TIMES", get_cv_times(),
                  CV_TIMES_HELP, true));

            set_cv_fraction(
               get_option_real_range(prefix + "CV_FRACT", get_cv_fraction(),
                  0.0, 1.0,
                  CV_FRACT_HELP, true));

            if(get_cv_times() == 0)
            {
               set_desired_std_dev(get_option_real(prefix + "DES_STD_DEV",
                  get_desired_std_dev(),
               DES_STD_DEV_HELP, true));
               set_max_times(get_option_int(prefix + "MAX_TIMES", get_max_times(),
                  MAX_TIMES_HELP, true));
            }		   
         }

      if (perfEstimationMethod == bootstrap)
      {
         set_bootstrap_times(get_option_int(prefix + "BS_TIMES",
            get_bootstrap_times(),
            BOOTSTRAP_TIMES_HELP, true));
         set_bootstrap_fraction(get_option_real(prefix + "BS_FRACTION",
            get_bootstrap_fraction(),
            BOOTSTRAP_FRACTION_HELP, true));
      }

      if (perfEstimationMethod == holdOut || perfEstimationMethod == automatic)
      {
         set_holdout_times(get_option_int(prefix + "HO_TIMES",
            get_holdout_times(),
            HOLDOUT_TIMES_HELP, true));
         set_holdout_number(get_option_int(prefix + "HO_NUMBER",
            get_holdout_number(),
            HOLDOUT_NUMBER_HELP, true));
         if(get_holdout_number() == 0)
            set_holdout_percent(get_option_real_range(prefix + "HO_PERCENT",
               get_holdout_percent(),
               0, 1,
               HOLDOUT_PERCENT_HELP,
               true));
      }

      if (perfEstimationMethod == testSet)
      {
      // no more extra options for testSet
      }
   }

eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee 1*/


   // almost like the copy constructor, but doesn't copy perfData.  
   public void copy_options(PerfEstDispatch source)
{
   set_log_options(source.get_log_options());
   set_perf_estimator(source.get_perf_estimator());
   set_error_type(source.get_error_type());
   set_cv_weight_threshold(source.get_cv_weight_threshold());
   set_seed(source.get_seed());
   set_cv_folds(source.get_cv_folds());
   set_cv_times(source.get_cv_times());
   set_cv_fraction(source.get_cv_fraction());
   set_max_times(source.get_max_times());
   set_desired_std_dev(source.get_desired_std_dev());
   set_error_trim(source.get_error_trim());
   set_bootstrap_times(source.get_bootstrap_times());
   set_bootstrap_fraction(source.get_bootstrap_fraction());
   set_holdout_times(source.get_holdout_times());
   set_holdout_number(source.get_holdout_number());
   set_holdout_percent(source.get_holdout_percent());
}


   public double estimate_performance(BaseInducer inducer,
			  InstanceList trainList,
			  InstanceList testList){return estimate_performance(inducer,trainList,testList,null);}

/*   public double estimate_performance(BaseInducer baseInducer,
			  InstanceList trainList,
			  InstanceList testList,
			  PerfData providedPerfData)
   {
      // Create train and test InstanceLists as copies of passed-in InstanceLists.
      InstanceList newTrainInstList = (InstanceList)trainList.clone();

      //Added for estimate_performance(BaseInducer,InstanceList,PerfData) -JL
      InstanceList newTestInstList = null;
      if(testList != null)
         newTestInstList = (InstanceList)testList.clone();

//obs         InstanceList newTestInstList = (InstanceList)testList.clone();
   
      // Call the protected version.
      return estimate_performance(baseInducer, newTrainInstList,
			       newTestInstList, providedPerfData);
   }
*/

public double estimate_performance(BaseInducer baseInducer,
					   InstanceList trainInstList,
					   InstanceList testInstList,
					   PerfData providedPerfData)
{
   if (trainInstList == null)
      Error.fatalErr("PerfEstDispatch.estimate_performance: Null training instList");
   if (trainInstList.no_weight())
      Error.fatalErr("PerfEstDispatch.estimate_performance: "
	 +"no weight in training InstanceList");
   
   if (testInstList != null && testInstList.no_weight())
      Error.fatalErr("PerfEstDispatch.estimate_performance: "
	 +"no weight in test InstanceList");

   boolean usedAutomaticMethod = false;

   if (perfEstimationMethod == automatic) {
      usedAutomaticMethod = true;
      perfEstimationMethod = trainInstList.total_weight() < cvWeightThreshold
	 ? cv : holdOut;
      logOptions.LOG(1, "Using "
	  +perfEstimationMethodEnum.name_from_value(perfEstimationMethod)
	  +" for performance estimation.\n");
   }
   
   // This reference keeps track of the PerfEstimator to use.
   PerfEstimator perfEstimator = null;
   
   // Det up an performance estimator.  The one to use depends on the method
   // we selected in the options.  Set the options for each estimator
   // here, too.
   if (perfEstimationMethod == stratCV) {
      StratifiedCV crossValidator = new StratifiedCV();
      crossValidator.set_log_level(get_log_level());
      crossValidator.init_rand_num_gen(get_seed());
      crossValidator.set_folds(Math.min(get_cv_folds(),
					trainInstList.num_instances()));
      crossValidator.set_fraction(get_cv_fraction());
      if (get_cv_times() == 0)
         crossValidator.auto_estimate_performance(baseInducer,
						   trainInstList,
						   desStdDev,
						   maxTimes);
      else {
         crossValidator.set_times(get_cv_times());
	 crossValidator.estimate_performance(baseInducer, trainInstList);
      }
      actualTimes = crossValidator.get_times();
      perfEstimator = crossValidator;
   } else if (perfEstimationMethod == cv) {
      CrossValidator crossValidator;
      if (baseInducer.can_cast_to_incr_inducer())
	 crossValidator = new CVIncremental();
       else
	 crossValidator = new CrossValidator();

      crossValidator.set_log_level(get_log_level());
      crossValidator.set_fraction(get_cv_fraction());

      crossValidator.init_rand_num_gen(get_seed());
      crossValidator.set_folds(Math.min(get_cv_folds(),
					trainInstList.num_instances()));

      if (get_cv_times() == 0)      
         crossValidator.auto_estimate_performance(baseInducer,
						trainInstList,
						desStdDev,
						maxTimes);
      else {
 	 crossValidator.set_times(get_cv_times());
	 crossValidator.estimate_performance(baseInducer, trainInstList);
      }
      actualTimes = crossValidator.get_times();
      perfEstimator = crossValidator;
   } else if (perfEstimationMethod == bootstrap){
      actualTimes = get_bootstrap_times();
      Bootstrap bootstrap = new Bootstrap(get_bootstrap_times());
      bootstrap.set_fraction(get_bootstrap_fraction());
      bootstrap.set_type(Bootstrap.fractional);
      bootstrap.set_log_level(get_log_level());
      bootstrap.init_rand_num_gen(get_seed());
      bootstrap.estimate_performance(baseInducer, trainInstList);
      perfEstimator = bootstrap;
   } else if (perfEstimationMethod == holdOut) {
      HoldOut holdout = new HoldOut(get_holdout_times(),
				     get_holdout_number(),
				     get_holdout_percent());
      holdout.set_log_level(get_log_level());
      holdout.init_rand_num_gen(get_seed());
      holdout.estimate_performance(baseInducer, trainInstList);
      perfEstimator = holdout;
   } else if (perfEstimationMethod == testSet){
      actualTimes = 0; // We didn't do any estimation runs.

      // It is an error to use testSet if we have no real performance.
      if(testInstList == null) {
	 if(providedPerfData != null) {
	    // If an ErrorData was provided, but we have no test set,
	    // then just return the value we had before rather than
	    // aborting.  We do this to fix a design bug in SASearch
	    // which would cause an abort every time a node is
	    // reevaluated under testSet.
	    // Based on the control flow here, the net effect here is
	    // to do nothing.
	 }
	 else {
	    Error.fatalErr("PerfEstDispatch::estimate_performance: cannot use test "
	       +"set error when no testing set is provided");
	 }
      }
 
   } else
      Error.fatalErr("PerfEstDispatch::estimate_performance: "
	 +"invalid performance estimator");


   // Clear the current PerfData first.
   perfData.clear();
   
   if(perfEstimator != null) {
      // If an PerfData was provided, append its results first.
      if(providedPerfData != null)
	 perfData.append(providedPerfData);

      // Now accumulate results into perfData.
      perfData.append(perfEstimator.get_perf_data());
   }

   // Set real statistics if we have a test InstanceList.
   if(testInstList != null) {
      boolean saveDribble = GlobalOptions.dribble;
      GlobalOptions.dribble = false;

      // if full testing is supported, make sure ALL error metrics
      // in PerfData have their test set information present.
      if(baseInducer.supports_full_testing()) {
	 CatTestResult result = baseInducer.train_and_perf(trainInstList,
							    testInstList);
	 perfData.set_test_set(result);
	 if (perfEstimationMethod == testSet) {
	    MLJ.ASSERT(perfData.size() == 0,"PerfEstDispatch.estimate_performance: perfData.size() != 0.");
	    perfData.insert(result);
	 }
	 result = null;
      }
      else
	 perfData.set_test_set(baseInducer.train_and_test(trainInstList,
							  testInstList),
			       testInstList.total_weight());
      
      GlobalOptions.dribble = saveDribble;
   }

   perfEstimator = null;
   trainInstList = null;
   testInstList = null;

   // If a PerfData was provided, accumulate new results into it.
   if(providedPerfData != null) {
      providedPerfData.clear();
      providedPerfData.append(perfData);
   }   

   if (usedAutomaticMethod)
      perfEstimationMethod = automatic;
   
   // call get_error_data() to take errorType into account when
   // returning results.
   return get_error_data().error(errTrim);

}



   public double estimate_performance(BaseInducer inducer,
			  InstanceList trainList){return estimate_performance(inducer,trainList,(PerfData)null);}

   public double estimate_performance(BaseInducer baseInducer,
			  InstanceList trainInstList,
			  PerfData pPerfData)
   {
      InstanceList newTrainInstList = (InstanceList)trainInstList.clone();
      return estimate_performance(baseInducer, newTrainInstList, null, pPerfData);
   }

      
   // generic parameters
   public void set_perf_estimator(int perfEstimation ) //perfEstimation is PerfEstimationMethod enum
{
   if (perfEstimation != cv && perfEstimation != stratCV &&
	   perfEstimation != testSet && perfEstimation != bootstrap &&
           perfEstimation != holdOut && perfEstimation != automatic)
           Error.fatalErr("PerfEstDispatch::set_performance_estimator:  performance "
                  +"estimator must be either cv, stratCV, testSet, "
                  +"bootstrap, holdOut, or automatic");
   perfEstimationMethod = perfEstimation;
}

   public int get_perf_estimator() { return perfEstimationMethod; } //perfEstimationMethod is PerfEstimationMethod enum

   public void set_error_type(int errType) { errorType = errType; } //errType is ErrorType enum
   public int get_error_type() { return errorType; } //errType is ErrorType enum
   public void set_error_trim(double trim) { errTrim = trim; }
   public double get_error_trim() { return errTrim; }
   public void set_seed(int seed) { randSeed = seed; }

   public int get_seed(){ return randSeed; }

   public void set_cv_weight_threshold(double threshold) { cvWeightThreshold = threshold; }
   public double get_cv_weight_threshold() { return cvWeightThreshold; }
   
   // Parameters for cross validation.
   public void set_cv_folds(int folds)
{
   if(folds == 0 || folds == 1)
      Error.fatalErr("PerfEstDispatch::set_cv_folds: picking "+folds+" folds "
	 +"is illegal");
   cvFolds = folds;
}

   public int  get_cv_folds() { return cvFolds; }
   public void set_cv_times(int times) { cvTimes = times; }
   public int  get_cv_times() { return cvTimes; }
   public void set_cv_fraction(double fract) { cvFraction = fract; }
   public double get_cv_fraction() { return cvFraction; }
   public void set_max_times(int times) { maxTimes = times; }
   public int  get_max_times() { return maxTimes; }
   public void set_desired_std_dev(double dstd) { desStdDev = dstd; }
   public double get_desired_std_dev() { return desStdDev; }

   // Parameters for bootstrap.
   public void set_bootstrap_times(int times)
{
   if (times < 1)
      Error.fatalErr("PerfEstDispatchimator::set_bootstrap_times: times ("+times
	  +") must be at least 1");
   bootstrapTimes = times;
}

   public int  get_bootstrap_times() { return bootstrapTimes; }
   public void set_bootstrap_fraction(double fract)
{
   if(fract <= 0.0 || fract > 1.0)
      Error.fatalErr("PerfEstDispatchimator::set_bootstrap_fraction: fraction ("+
	 fract+") must be between 0.0 and 1.0");
   bootstrapFraction = fract;
}

   public double get_bootstrap_fraction() { return bootstrapFraction; }

   // Parameters for holdout
   public void set_holdout_times(int times) { hoTimes = times; }
   public int get_holdout_times() { return hoTimes; }
   public void set_holdout_number(int num) { hoNumber = num; }
   public int get_holdout_number() { return hoNumber; }
   public void set_holdout_percent(double pct) { hoPercent = pct; }
   public double get_holdout_percent() { return hoPercent; }

   // get results; get_error_data returns your chosen error data based
   // on errorType.  get_perf_data gets the full suite of error stats.
   public ErrorData get_error_data()
{
   switch(errorType) {
      case classError:
	 return perfData.get_error_data();
      case meanSquaredError:
	 return perfData.get_mean_squared_error_data();
      case meanAbsoluteError:
	 return perfData.get_mean_absolute_error_data();
      case classLoss:
	 return perfData.get_loss_data();
      default:
	 Error.fatalErr("PerfEstDispatch::get_error_data: errorType has "
	    +"illegal value: "+(int)errorType);
	 return perfData.get_error_data();
   }
}

   public PerfData get_perf_data()
{
   return perfData;
}


   // get number of times auto-cv was actually run
   public int get_actual_times() { return actualTimes; }
   
   // display:  we can display settings, performance information, or both
   public void display_settings(Writer stream)
{
   try{
   // display generic parameters first
   int acMethod = get_perf_estimator(); //PerfEstimationMethod  enum
   String acMethodString = perfEstimationMethodEnum.name_from_value(acMethod);
   stream.write("Method: "+acMethodString+"\n");
   String errorTypeString = errorTypeEnum.name_from_value(errorType);
   stream.write("Error type: "+errorTypeString+"\n");
   if (acMethod == automatic)
      stream.write("CV weight threshold: "+cvWeightThreshold+"\n");
   stream.write("Trim: "+get_error_trim()+"\n");
   stream.write("Seed: "+get_seed()+"\n");

   // CV parameters
   if(acMethod == cv || acMethod == stratCV || acMethod == automatic) {
      stream.write("Folds: "+get_cv_folds());
      if(cvTimes >= 1)
	 stream.write(",  Times: "+get_cv_times()+"\n");
      else {
	 stream.write(",  Desired std dev: "+get_desired_std_dev()+
	           ",  Max times: "+get_max_times()+"\n");
      }
   }

   // bootstrap parameters
   if(acMethod == bootstrap) {
      stream.write("Times: "+get_bootstrap_times()+",  Fraction: "+
	 get_bootstrap_fraction()+"\n");
   }

   // holdout parameters
   if(acMethod == holdOut || acMethod == automatic) {
      stream.write("Times: "+get_holdout_times());
      if(get_holdout_number() == 0)
	 stream.write(",  Percent: "+get_holdout_percent()+"\n");
      else
	 stream.write(",  Number: "+get_holdout_number()+"\n");
   }
   }catch(IOException e){e.printStackTrace();System.exit(1);}
}
/*bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb 3
   public void display_performance(Writer stream)
{
   perfData.display_error(stream, errTrim);
}

   public void display(Writer stream)
{
   display_settings(stream);
   stream.write("Error: ");
   display_performance(stream);
   stream.write("\n");
   if (!perfData.perf_empty()) {
      perfData.display_non_error(stream,
				 CatTestResult.get_compute_log_loss());
      stream.write("\n");
   }
}
eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee 3*/

}