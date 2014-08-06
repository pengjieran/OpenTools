package arithmetic.fss;

import java.io.IOException;
import java.io.Writer;

import arithmetic.shared.Basics;
import arithmetic.shared.Error;
import arithmetic.shared.GlobalOptions;
import arithmetic.shared.Globals;
import arithmetic.shared.InstanceList;
import arithmetic.shared.LogOptions;

//obs class PerfEstState : public State<Array<int>, PerfEstInfo> {

abstract public class PerfEstState extends State
{
public static int DEFAULT_SEED = 7258789;
public static boolean DEFAULT_USE_COMPOUND = true;
public static double DEFAULT_COMPLEX_PENALTY = 0;
public PerfData perfData = new PerfData();
public int numEvaluations;   

public static final int PERF_EST_INFO = 2001;
public static final int C45AP_INFO = 2002;
public static final int DISC_INFO = 2003;
public static final int FSS_INFO = 2004;
public static final int ORDER_INFO = 2005;
public static final int WEIGHT_INFO = 2006;



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

//obs   PerfEstState(Array<int>*& initialInfo, const PerfEstInfo& gI);
   public PerfEstState(int[] initialInfo, PerfEstInfo gI)
   {
      super(initialInfo, gI);
      numEvaluations = 0;
      complexity = 0;
   }

//obs   virtual ~PerfEstState() { }


   public void set_final_state()
   {
      set_graph_options(",style=filled,color=gray95");
   }

//obs   virtual Real eval(PerfEstInfo *, Bool computeReal=TRUE,
//obs		     Bool computeEstimated=TRUE);
//obs Real PerfEstState::eval(PerfEstInfo *acInfo, Bool computeReal,
//obs		       Bool computeEstimated)
public double eval(Object acInfo)
{return eval(acInfo,true,true);}

public double eval(Object acInfo, boolean computeReal)
{return eval(acInfo,computeReal,true);}

public double eval(Object acInfo, boolean computeReal,
		       boolean computeEstimated)
{
   boolean saveDribble = GlobalOptions.dribble;
   GlobalOptions.dribble = false;
   // it is an error to set both compute booleans to false
   if(!computeReal && !computeEstimated)
      Error.fatalErr("FSSState::eval: one of computeReal or computeEstimated must be TRUE");

   // construct training and test lists for use in evaluation.  The
   // defaults are just the global info values themselves
   InstanceList estTrainList = ((PerfEstInfo)acInfo).trainList;
   InstanceList estTestList = ((PerfEstInfo)acInfo).testList;
   if(!computeReal)
      estTestList = null;
   construct_lists((PerfEstInfo)acInfo, estTrainList, estTestList);
 
   // set seed to insure comparable results across all states,
   // but add the number of evaluations so that we don't wind up repeating
   // the save evaluation when we evaluate multiple times.
   ((PerfEstInfo)acInfo).perfEst.set_seed(((PerfEstInfo)acInfo).seed + numEvaluations);

   // Invoke any pre-evaluation functions here
   pre_eval((PerfEstInfo)acInfo);
   
   // Evaluate state performance using the performance estimator
   // specified in PerfEstInfo.
   // Results of multiple estimations are accumulated into the perfData
   // that we store within this state.
   double error = 0;
   if(computeReal && computeEstimated) {
      error = ((PerfEstInfo)acInfo).perfEst.estimate_performance(((PerfEstInfo)acInfo).inducer,
					     estTrainList,
					     estTestList,
					     perfData);
   }
   else if(computeReal) {
      // temporarily set err estimator to "test-set" to get the
      // test set error
//obs      PerfEstDispatch.PerfEstimationMethod oldMethod =
      int oldMethod =   //PerfEstimationMethod enum
         ((PerfEstInfo)acInfo).perfEst.get_perf_estimator();
      ((PerfEstInfo)acInfo).perfEst.set_perf_estimator(PerfEstDispatch.testSet);
      error = ((PerfEstInfo)acInfo).perfEst.estimate_performance(((PerfEstInfo)acInfo).inducer,
						   estTrainList,
						   estTestList);
      ((PerfEstInfo)acInfo).perfEst.set_perf_estimator(oldMethod);
      ErrorData errorData = perfData.get_error_data();
      errorData.set_test_set(((PerfEstInfo)acInfo).perfEst.get_error_data().test_set_error(),
			     estTestList.num_instances());
   }
   else if(computeEstimated) {
	 // leaving out a test set causes test set error not to be
	 // computed.
	 error = ((PerfEstInfo)acInfo).perfEst.estimate_performance(((PerfEstInfo)acInfo).inducer,
						      estTrainList,
						      perfData);
   }
   else
 //obs     ABORT_IF_REACHED;
          Error.fatalErr("MLC++ internal error: unexpected condition in file PerfEstState.");//, line ");
   
   if(computeEstimated) {
      numEvaluations++;   
      ErrorData errorData = perfData.get_error_data();
      evalCost = perfData.get_cost();
      fitness = (1.0-error) - ((PerfEstInfo)acInfo).complexityPenalty * complexity;
//obs      logOptions.LOG(5, "Computing standard deviation.  Size = "+perfData.length+"\n");
      if(errorData.has_std_dev())
	 stdDev = errorData.std_dev();
      else
	 stdDev = Basics.UNDEFINED_VARIANCE;
   }

   destruct_lists((PerfEstInfo)acInfo, estTrainList, estTestList);
   GlobalOptions.dribble = saveDribble;
   return fitness;
}


   public double get_error()
   {
      return perfData.get_error_data().mean();
   }

   public double get_error_std_dev()
   {
      return perfData.get_error_data().std_dev_of_mean();
   }

   public double get_test_set_fitness()
   {
      return perfData.get_error_data().test_set_error();
   }

   public double get_theoretical_std_dev()
   {
      return perfData.get_error_data().theo_std_dev();
   }


   public void pre_eval(PerfEstInfo info)
   {}

   public void construct_lists(PerfEstInfo info, InstanceList trainList, 
				InstanceList testList)
   {
//obs      (void)trainList;
//obs      (void)testList;
   }

   public void destruct_lists(PerfEstInfo info, InstanceList trainList,
			       InstanceList testList)
   {
//obs      (void)trainList;
//obs      (void)testList;
   }

//obs   virtual void display_info(MLCOStream& stream = Mcout) const;
public void display_info()
{display_info(Globals.Mcout);}

public void display_info(Writer stream)
{
   try{
   if(get_eval_num() > NOT_EVALUATED)
      stream.write("#"+get_eval_num());
   else
      stream.write("#?");

   stream.write(" [");
   ((PerfEstInfo)globalInfo).display_values((int[])get_info(), stream);
   stream.write("]");
   }catch(IOException e){e.printStackTrace();System.exit(1);}
}


//obs   virtual void display_stats(MLCOStream& stream,
//obs			      PerfEstInfo *acInfo) const;
//obs public void display_stats(Writer stream, PerfEstInfo acInfo)
public void display_stats(Writer stream, Object acInfo)
{
   try{

   if(numEvaluations == 0) {
      stream.write("<unevaluated>");
   }
   else {
      int errorType = PerfEstDispatch.classError; //PerfEstDispatch.ErrorType enum
      if(acInfo != null)
	 errorType = ((PerfEstInfo)acInfo).perfEst.get_error_type();
      switch(errorType) {
	 case PerfEstDispatch.classError:
	    stream.write("error: "+perfData.get_error_data());
	    break;
	 case PerfEstDispatch.meanSquaredError:
	    stream.write("mse: "+perfData.get_mean_squared_error_data());
	    break;
	 case PerfEstDispatch.meanAbsoluteError:
	    stream.write("mae: "+perfData.get_mean_absolute_error_data());
	    break;
	 case PerfEstDispatch.classLoss:
	    stream.write("loss: "+perfData.get_loss_data());
	    break;
	 default:
//obs	    ABORT_IF_REACHED;
          Error.fatalErr("MLC++ internal error: unexpected condition in file PerfEstState.");//, line ");
      }
      
      stream.write(" cost: "+perfData.get_cost()+" complexity: "+complexity);
   }
   }catch(IOException e){e.printStackTrace();System.exit(1);}

}

/*
//obs   virtual void display_for_graph(MLCOStream& stream = Mcout) const;   

public void display_for_graph()
{display_for_graph(Globals.Mcout);}

public void display_for_graph(Writer stream)
{
   try{
      display_info(stream);
      stream.write("\\n");
      perfData.get_error_data().dot_display(stream);
   }catch(IOException e){e.printStackTrace();System.exit(1);}
}
*/
}