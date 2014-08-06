package arithmetic.fss;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.CatTestResult;
import arithmetic.shared.Error;
import arithmetic.shared.GlobalOptions;
import arithmetic.shared.Globals;
import arithmetic.shared.InstanceList;
import arithmetic.shared.LogOptions;
import arithmetic.shared.MLJ;

/***************************************************************************
  PerfEstimator is an abstract base class for Performance Estimation 
methods.  Subclasses include CrossValidator (and StratifiedCV), as well as 
Bootstrap. Update PerfData to take into account the different sizes of 
folds by using weights in PerfData. dumpFiles should be made an option 
(very easy given the current layout).

@author James Louis	8/13/2001	Ported to Java
@author Dan Sommerfield	10/16/94	Minor function revisions.
@author Ronny Kohavi	9/12/94	Re-engineered Dan's project.
@author Dan Sommerfield	4/94	Initial revision (.h,.c) 
	(CS229B class project)	Based on Rich Long's CValidator.
***************************************************************************/

abstract public class PerfEstimator {

   protected String dumpStem = "";    // Don't dump if ""

   protected PerfData perfData;

   public static boolean warnedAlready = false; // moved here from train_and_test function -JL


/** A random number generator. **/
   private Random randNumGen = new Random();

/** Logging options for this class. **/
   protected LogOptions logOptions = new LogOptions();


/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private PerfEstimator(PerfEstimator source){}

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
  Dump data to files if dumpStem != "" 
dumpSuffix is appended to dumpStem before .{names,data,test} are added.  
descr goes into the files as a comment.
@param
@param
@param
@param
***************************************************************************/
   protected void dump_data(String dumpSuffix, String descr,
         InstanceList trainList,
         InstanceList testList)
   {
      try{
         if (dumpStem != Globals.EMPTY_STRING)
         {
//obs      MLCOStream namesStream(dumpStem + dumpSuffix + Globals.DEFAULT_NAMES_EXT);
            FileWriter namesStream = new FileWriter(dumpStem + dumpSuffix + Globals.DEFAULT_NAMES_EXT);
            trainList.display_names(namesStream, true, descr);

//obs      MLCOStream trainStream(dumpStem + dumpSuffix + Globals.DEFAULT_DATA_EXT);
            FileWriter trainStream = new FileWriter(dumpStem + dumpSuffix + Globals.DEFAULT_DATA_EXT);
            trainStream.write("|"+descr+"\n"+trainList+"\n");

//obs      MLCOStream testStream(dumpStem + dumpSuffix + Globals.DEFAULT_TEST_EXT);
            FileWriter testStream = new FileWriter(dumpStem + dumpSuffix + Globals.DEFAULT_TEST_EXT);
            testStream.write("|"+descr+"\n"+testList+"\n");
         }
      }catch(IOException e){e.printStackTrace(); System.exit(1);}
   }   

   // NULL value for localErrorData means don't update error.
   //   virtual void update_acc_data(int numCorrect, int numIncorrect,
   //				ErrorData *localErrorData);

   // show how stratified this particular train/test pair are
/***************************************************************************
  Shows how stratified a hold out is.
@param
@param
***************************************************************************/
protected void show_stratification(InstanceList trainPart,
				       InstanceList testPart)
{
   logOptions.LOG(2, "Stratification: Training: ");
//obs   InstanceListArray *trainBPA = trainPart.split_by_label();
//obs   for(int i=trainBPA->low(); i<=trainBPA->high(); i++)
//obs      logOptions.LOG(2, (*trainBPA)[i]->num_instances() << "/");
   InstanceList[] trainBPA = trainPart.split_by_label();
   for(int i=0; i<trainBPA.length; i++)
      logOptions.LOG(2, (trainBPA)[i].num_instances() +"/");
   trainBPA = null;

   logOptions.LOG(2, "  Test: ");
//obs   InstanceListArray *testBPA = testPart.split_by_label();
//obs   for(i=testBPA->low(); i<=testBPA->high(); i++)
//obs      logOptions.LOG(2, (*testBPA)[i]->num_instances() << "/");
   InstanceList[] testBPA = testPart.split_by_label();
   for(int i=0; i<testBPA.length; i++)
      logOptions.LOG(2, (testBPA)[i].num_instances() +"/");
   testBPA = null;

   logOptions.LOG(2, "\n");
}





   
   // train_and_test should call dump_data
   // This is protected so we can test outside programs like C4.5
/***************************************************************************
  Trains and tests the given inducer using the given test set, and the 
trainList member. Fills in the perfData array. The complexity of is that 
inducer.train() + CatTestResult(). We could almost get away with a 
DumpInducer which simply dumps the data it gets, and then appends to a test 
file the instances it gets to categorize.  The problem is that when 
categorizing, the instances are unlabelled. If localPerfData is NULL, we 
don't update any statistics, but we still return the error
@return
@param
@param
@param
@param
@param
***************************************************************************/
protected double train_and_test(BaseInducer inducer,
				  InstanceList trainList, 
 			        InstanceList testList,
				  String dumpSuffix,
				  PerfData localPerfData)
{
   // show stratification at loglevel 3
//obs   IFLOG(3, show_stratification(trainList, testList));
   if(get_log_level() >= 3) show_stratification(trainList, testList);

   // dump before assign_data, because we'll lose ownership.
   dump_data(dumpSuffix, description(), trainList, testList);

   double error = 0.0;
   
   if (localPerfData == null || !inducer.can_cast_to_inducer()) {
      if(inducer.supports_full_testing()) {
	 CatTestResult result = inducer.train_and_perf(trainList, testList);
	 error = result.error();
	 if(localPerfData != null)
	    localPerfData.insert(result);
	 result = null;
      }
      else {
	 error = inducer.train_and_test(trainList, testList);
	 if (localPerfData != null)
	    localPerfData.insert_error(error);	
      }
   }
   else {
      CatTestResult result = inducer.train_and_perf(trainList, testList);
      error = result.error();
      localPerfData.insert(result);
      result = null;
   }

   double totalWeight = testList.total_weight();
   double weightIncorrect = error * totalWeight;

   // Nice test, but doesn't work when the inner inducer is a
   // Performance estimator itself.  Therefore, this is a warning.
   if (!warnedAlready && 
       Math.abs(weightIncorrect / totalWeight - error) > 0.001) {
       try{
         Globals.Mcerr.write("PerfEstimator::train_and_test: suspicious error "
            +error+" does not divide well total weight of instances "
            +totalWeight +" (" +(weightIncorrect / totalWeight)
            +" vs. "+error+".  This is OK for PerfEstInducer."
            +" Further warnings of this type suppressed.\n");
       }catch(IOException e){e.printStackTrace(); System.exit(1);}
       warnedAlready = true;
   }

   return error;
}



/***************************************************************************
  Basic constructor.
***************************************************************************/
public PerfEstimator()
{ perfData = null;}

/***************************************************************************
  Destructor. Only needs to get rid of performance data.
***************************************************************************/
protected void finalizer()
{
   perfData = null;
}

abstract public String description();
abstract public double estimate_performance(BaseInducer inducer, InstanceList dataList);

   // estimate performance from a series of files
abstract public double estimate_performance(BaseInducer inducer, String fileStem);

   // Estimate performance from a fileStem with assumed suffixes
   //    -#.{names,data,test} where # denotes a sequence number.
/***************************************************************************
  Trains and tests the inducer from a series of data/test files. Uses the 
files fileStem-#.{names,data} Does NOT shuffle the files that it reads. It 
deletes the perf data by default (not deleting it may be useful for 
repeating this process more than once). The result returned is always 
untrimmed. Restores the CrossValidator's original training list upon 
completion.
@return
@param
@param
@param
@param
***************************************************************************/
public double estimate_file_performance(BaseInducer inducer,
					     int numFiles,
					     String fStem,
					     PerfData localPerfData)
{
   logOptions.LOG(1, "PerfEstimator::estimate_performance: Inducer: "
       +inducer.description()
       +"\n");
   logOptions.LOG(2, "PerfEstimator::estimate_performance: Number of files: " 
       +numFiles+"\n");

   if (dumpStem != Globals.EMPTY_STRING)
      Error.fatalErr("PerfEstimator::estimate_file_performance: it does not "
	     +"make sense to estimate from a file and dump too");

   PerfData totalPerfData = new PerfData();
   for (int fileNum = 0; fileNum < numFiles; fileNum++) {
      String dataFile = new String(fStem + "-" + fileNum);
      double error = single_file_performance(inducer, dataFile,
					   localPerfData);
      totalPerfData.insert_error(error);
   }
   MLJ.ASSERT(totalPerfData.size() == numFiles, 
			"PerfEstimator.estimate_file_performance: "
			+"totalPerfData.size() != numFiles");
   logOptions.LOG(2, "Error of set: " +totalPerfData +"\n");

   return totalPerfData.get_error_data().mean();
}


   // Estimate one file from a fileStem
/***************************************************************************
  Trains and tests the inducer from a single dumped file. Restores the 
estimator's original training list upon completion. 
@return
@param
@param
@param
***************************************************************************/

public double single_file_performance(BaseInducer inducer,
					String dataFile,
					PerfData localPerfData)
{
   if (dumpStem != Globals.EMPTY_STRING)
      Error.fatalErr("PerfEstimator::single_file_performance: it does not "
	 +"make sense to estimate from a file and dump too");

   logOptions.LOG(3, "Reading data file: "+dataFile+"\n");

   // Not local because train_and_test gets ownership and returns it.
   InstanceList trainList = new InstanceList(dataFile);
   logOptions.LOG(3, "Number of instances in file "+dataFile+": "
       +trainList.num_instances()+".  Total weight "+
       trainList.total_weight()+"\n");
   String testFile = new String(dataFile + Globals.DEFAULT_TEST_EXT);
   logOptions.LOG(3, "Reading test file: "+testFile+"\n");

   InstanceList testList = new InstanceList(trainList, testFile);
   logOptions.LOG(3, "Instances in test list: "+testList.num_instances()+
       ".  Total weight "+testList.total_weight()+"\n");
   boolean saveDribble = GlobalOptions.dribble;
   GlobalOptions.dribble = false;
   double error = train_and_test(inducer, trainList, testList, "dummy",
				  localPerfData);
   GlobalOptions.dribble = saveDribble;
   logOptions.LOG(3, "Error of file: "+error+"\n");
   trainList = null; 
   return error;
}

/***************************************************************************
  Verify that perf data is available.  Aborts with an error message if not.
***************************************************************************/
public void check_error_data()
{
   if (perfData == null)
      Error.fatalErr("PerfEstimator::check_error_data: Must be called "
	 +"after estimate_performance.  No error data");
   
}

public void check_perf_data()
{
   if (perfData == null)
      Error.fatalErr("PerfEstimator::check_perf_data: Must be called "
	 +"after estimate_performance. No performance data");
}


/***************************************************************************
  Return performance data or aborts if it is NULL. get_error_data is a 
convenience function which gets the error portion of the performance data. 
Mostly useful for the testers.
@return
***************************************************************************/
public ErrorData get_error_data()
{
   check_perf_data();
   return perfData.get_error_data();
}


public PerfData get_perf_data()
{
   check_perf_data();
   return perfData;
}

public void display_error_data()
{display_error_data(Globals.Mcout,0,ErrorData.defaultPrecision);}

public void display_error_data(Writer out)
{display_error_data(out,0,ErrorData.defaultPrecision);}

public void display_error_data(Writer out, double trim)
{display_error_data(out,trim,ErrorData.defaultPrecision);}

/***************************************************************************
  Nicely display the error data, or print "no error data" if none.
***************************************************************************/
public void display_error_data(Writer out, double trim, int precision)
{
   try{
      if (perfData == null)
         out.write("no error data");
      else
         perfData.display_error(out, trim, precision);
   }catch(IOException e){e.printStackTrace();System.exit(1);}
}


public void display_perf_data()
{display_perf_data(Globals.Mcout,0,ErrorData.defaultPrecision);}

public void display_perf_data(Writer out)
{display_perf_data(out,0,ErrorData.defaultPrecision);}

public void display_perf_data(Writer out, double trim)
{display_perf_data(Globals.Mcout,trim,ErrorData.defaultPrecision);}

public void display_perf_data(Writer out, double trim, int precision)
{
   try{
      if (perfData == null|| perfData.perf_empty())
         out.write("no performance data");
      else
         perfData.display(out, false, trim, precision);
   }catch(IOException e){e.printStackTrace();System.exit(1);}
}


/***************************************************************************
  Description : Returns the trimmed mean/standard deviation for the
                   error of the induced Categorizers for the
		   training and test lists generated through cross-validation.
		The standard deviation is the std-dev of the MEAN error.
  Comments    : trim defaults to 0. 
                
***************************************************************************/
public double error(double trim)
{
   check_error_data();
   return perfData.get_error_data().mean(trim);
}

public double error_std_dev()
{return error_std_dev(0);}

public double error_std_dev(double trim)
{
   check_error_data();
   return perfData.get_error_data().std_dev_of_mean(trim);
}

public double error()
{return error(0);}

   
/***************************************************************************
  File dump: writes out all training lists used in estimation.
***************************************************************************/

public void dump_files(InstanceList dataList, String fStem)
{
   if (fStem == Globals.EMPTY_STRING)
      Error.fatalErr("PerfEstimator::dump_files: empty filestem");
  dumpStem = fStem;

  NullInducer inducer = new NullInducer(fStem + " Null Inducer", false);
  logOptions.LOG(1, "Dumping files; estimated performances will be empty\n");
  estimate_performance(inducer, dataList);
  dumpStem = Globals.EMPTY_STRING;  // Don't dump in the future.
}


/***************************************************************************
  Display information about this estimator. The description is printed 
only if descrip is TRUE. Performance data info is printed
***************************************************************************/

public void display(Writer out, boolean descrip,
			   double trim, int precision)
{
   try{
   if (descrip)
      out.write(description()+": ");
   display_error_data(out, trim, precision);
   out.write("\n");
   display_perf_data(out, trim, precision);
   }catch(IOException e){e.printStackTrace(); System.exit(1);}
}

public void display()
{display(Globals.Mcout,false,0.0,ErrorData.defaultPrecision);}
public void display(Writer out)
{display(out,false,0.0,ErrorData.defaultPrecision);}
public void display(Writer out, boolean descrip)
{display(out,descrip,0.0,ErrorData.defaultPrecision);}
public void display(Writer out, boolean descrip, double trim)
{display(out,descrip,trim,ErrorData.defaultPrecision);}


}
