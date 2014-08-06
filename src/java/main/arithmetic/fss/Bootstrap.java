package arithmetic.fss;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.Error;
import arithmetic.shared.GlobalOptions;
import arithmetic.shared.InstanceList;
import arithmetic.shared.MLJ;

/***************************************************************************
  The Bootstrap is an error estimation method developed by Bradley Efron 
(Efron, An Introduction to the Bootstrap, 1993). This class implements a 
simplified version of bootstrap (simple bootstrap), along with the usual 
.632 bootstrap method.  Optionally, the user may choose a weighting for the 
bootstrap sample error other than .632.

  Assumptions  : Assumes that the order and contents of the training
                    list returned by the inducer is the same as the
		    order and contents of the training list passed to
		    the inducer.
		 The training list may be altered by the inducer
                    during training, but it must be returned to the
		    same state.

  Complexity   : estimate_performance takes O(numTimes *
                       O(Inducer.train_and_test())).
  Enhancements : Keep the relative order of instances intact.  This
		    seems to be hard if the inducer is allowed to
		    modify its training set (e.g. when it does cross
		    validation) since references to the list will change.

@author James Louis 8/13/2001	Ported to Java.
@author Dan Sommerfield 10/4/94 
			Refitted to connect with new PerfEstimator class.
			Added estimate_performance using files.
@author Dan Sommerfield 6/01/94
			Initial revision as CS229B project.
***************************************************************************/

public class Bootstrap extends PerfEstimator {


   // we want to make this enum public, but we have to declare it before
   // all private/protected declarations which depend on it.
   // Hence the unusual public/private/protected ordering.
/* BootstrapType ENUM*/
   public static final byte simple = 0;
   public static final byte fractional = 2;
/* End BootstrapType ENUM*/

   private int numTimes;
   private byte bsType; //BootstrapType enum
   private double bsFract;

   protected static int BootstrapDefaultNumTimes = 50;
   protected static byte BootstrapDefaultType = fractional;//BootstrapType enum
   protected static double BootstrapDefaultFraction = 0.632;
   protected double apparentError;

// constants for dump suffixes used by refined bootstrap when estimating
// from dumped files.  This suffix is used specially to get apparent
// error from a dumped file.
   protected static String BOOTSTRAP_AA_SUFFIX = "-A";



/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private Bootstrap(Bootstrap source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(Bootstrap source){}



   public int get_times() { return numTimes; }
   public byte get_type() { return bsType; } //returns BootstrapType enum
   public double get_fraction() { return bsFract; }

/****************************************************************************
  Train_and_test, modified to apply the bootstrap formula for the 
appropriate type of bootstrap at each sample.
****************************************************************************/
   protected double train_and_test(BaseInducer inducer,
         InstanceList trainList,
         InstanceList testList,
         String dumpSuffix,
         PerfData localPerfData)
   {
      boolean saveDribble = GlobalOptions.dribble;
      GlobalOptions.dribble = false;
      double error = super.train_and_test(inducer, trainList, testList,
            dumpSuffix, null);
      GlobalOptions.dribble = saveDribble;
   // now apply the bootstrap forumla if we're doing a fractional bootstrap
   // do NOT compute if localPerfData = none.  Then, we're getting apparent
   // error.
   // error = error*bsFract + apparent * (1 - bsFract)
      if(bsType == fractional && localPerfData!= null)
         error = error * bsFract + apparentError * (1.0 - bsFract);

   // insert the error now
      if(localPerfData != null)
         localPerfData.insert_error(error);

   // log the error
      logOptions.LOG(2, "Individual error: "+error+"\n");
      return error;
   }

/*****************************************************************************
 Constructor.  Allows immediate options setting. 
Sets statistical data to NULL.
*****************************************************************************/
public Bootstrap()
{
   super();
   set_type(BootstrapDefaultType);
   set_times(BootstrapDefaultNumTimes);
   set_fraction(BootstrapDefaultFraction);
}

/*****************************************************************************
 Constructor.  Allows immediate options setting. 
Sets statistical data to NULL.
*****************************************************************************/
public Bootstrap(int nTimes)
{
   super();
   set_type(BootstrapDefaultType);
   set_times(nTimes);
   set_fraction(BootstrapDefaultFraction);
}

/*****************************************************************************
 Constructor.  Allows immediate options setting. 
Sets statistical data to NULL.
*****************************************************************************/
public Bootstrap(int nTimes, byte type /*BootstrapType enum*/)
{
   super();
   set_type(type);
   set_times(nTimes);
   set_fraction(BootstrapDefaultFraction);
}

/*****************************************************************************
 Constructor.  Allows immediate options setting. 
Sets statistical data to NULL.
*****************************************************************************/
public Bootstrap(int nTimes, byte type /*BootstrapType enum*/, double fraction)
{
   super();
   set_type(type);
   set_times(nTimes);
   set_fraction(fraction);
}

   
   // options
   public void set_times(int num)
   {
   if(num <= 0)
      Error.fatalErr("Bootstrap.set_times: illegal number of times: "+num);
   numTimes = num;
   }


public void set_type(byte type /*BootstrapType enum*/) {
   if(type == simple || type == fractional)
      bsType = type;
   else
      Error.fatalErr("Bootstrap.set_type: illegal type:"+type);
}

public void set_fraction(double fract) {
   if(fract <= 0.0 || fract > 1.0)
      Error.fatalErr("Bootstrap.set_fraction: Illegal value: "+fract
	  +".  Must be between 0.0 and 1.0");
   bsFract = fract;
}

/*****************************************************************************
 Description:  This function returns a full description of the error
               estimation method, including the specific type (i.e. type of
               bootstrap)
*****************************************************************************/

public String description(){
   String typeName = "";
   switch(bsType) {
      case simple:
	 typeName = "simple";
	 break;
      case fractional:
	 typeName = bsFract+ " fractional";
	 break;
      default:
	 MLJ.ASSERT(false,"Bootstrap.description: BootstrapType invalid.");    // should never get here
	 break;
   }
//obs   return String(numTimes, 0) + "x " + typeName + " Bootstrap";
   return numTimes + "x " + typeName + " Bootstrap";
}


/****************************************************************************
 Description : estimate_performance for Bootstrap does all the work.  It builds
               bootstrap sample, then calls train_performance to get train for
	       the appropriate type of bootstrap method.
 Comments    :
*****************************************************************************/
public double estimate_performance(BaseInducer inducer,
				  InstanceList dataList)
{
  
   logOptions.LOG(2, "Inducer: " + inducer.description() + "\n");
   logOptions.LOG(2, "Number of times: " + numTimes + "\n");
   logOptions.LOG(3, "Training list: " + dataList + "\n");

   if (dataList.get_schema().has_loss_matrix())
      Error.fatalErr("Bootstrap.estimate_performance: Bootstrap can only be used "
	 +"on instance lists without loss matricies. Remove or disable the "
	 +"loss matrix and try again");
   
   // clear out statistical data
//obs   delete perfData;
   perfData = null;
   perfData = new PerfData();
   
   // we will need the "apparent error"--that is, the error of
   // the inducer when trained AND tested on the training set.
   // Apparent error tends to be very low, even 0.0 for many inducers
   // assuming there are no conflicting instances.
   InstanceList dataPtr = dataList;
   boolean saveDribble = GlobalOptions.dribble;
   GlobalOptions.dribble = false;
   apparentError = train_and_test(inducer, dataPtr, dataList, 
				  BOOTSTRAP_AA_SUFFIX, null);
   GlobalOptions.dribble = saveDribble;

   MLJ.ASSERT(numTimes != 0,"Bootstrap.estimate_performance: numTimes == 0.");
   logOptions.DRIBBLE("Bootstrapping " + numTimes + " times: ");
   for(int time = 0; time < numTimes; time++) {
      logOptions.DRIBBLE(time + 1 +" ");    
      // create a bootstrap sample
      InstanceList testList = new InstanceList(dataList.get_schema());
      InstanceList trainList;
      
      trainList =
	 dataList.sample_with_replacement(dataList.num_instances(), testList,
					  rand_num_gen());
      logOptions.LOG(2, "\nTotal samples: " +trainList.num_instances()
	  +"\tUnique Used Samples: "
	  +(dataList.num_instances() - testList.num_instances())
	  +"\tUnused Samples: " + testList.num_instances() +"\n");
      logOptions.LOG(3, "bootstrap sample:\n" + trainList + "\n"
	  +"unused sample:\n"+testList+"\n");
      // make sure test sample has elements if we're using fractional
      // bootstrap.
      // An empty test set occurs if the sampling process happens to
      // sample the entire dataset (very rare for all but very small
      // datasets)
      if(bsType == fractional && testList.no_instances()) {
       logOptions.LOG(2, "Bootstrap: empty test list at time: "+time+"\n");
	 time--;   // force repeat of this run of the loop
      }    
      else {

	 saveDribble = GlobalOptions.dribble;
	 GlobalOptions.dribble = false;
//obs	 train_and_test(inducer, trainList, testList, "-" + MString(time, 0),
//obs			perfData);
	 train_and_test(inducer, trainList, testList, "-" + time,perfData);
	 GlobalOptions.dribble = saveDribble;
      }
//obs      delete trainList;
      trainList = null;
   }

   logOptions.DRIBBLE("\n");
   logOptions.LOG(2, "Error: "+this+"\n");

   perfData.insert_cost(numTimes);
   return error();
}


/*****************************************************************************
  Trains and tests the inducer using files. Uses the files fileStem.names, 
fileStem-T.data, and fileStem-T.test, where T is an integer in the 
range[0, numTimes-1]. Fractional bootstrap also use the file 
fileStem-A.data, and fileStem-A.test to compute apparent error. Most of the 
work is done before the files are dumped.
*****************************************************************************/
public double estimate_performance(BaseInducer inducer, String fileStem) {

   perfData = null;
   perfData = new PerfData();
   
   // get apparent error first, if needed.
   if(bsType != simple) {
      apparentError =
       single_file_performance(inducer, fileStem + BOOTSTRAP_AA_SUFFIX, null);
   }

   // get results using estimate_file_performance
   estimate_file_performance(inducer, numTimes, fileStem, perfData);

   logOptions.LOG(2, "Error: "+this+"\n");

   perfData.insert_cost(numTimes);
   return error();
}

//Test functions and variables
/*
private String FILE_STEM  = "t_lensesBS";
private String NAMES_FILE = "t_Bootstrap.names";
private String DATA_FILE  = "t_Bootstrap.data";
private String BS_STEM = "t_Bootstrap";

private int NUM_TIMES = 20;
private int RAND_SEED = 7258789;

void cleanup()
{
   // Create a file so the asterisk won't fail.
   MLCOStream dummy(BS_STEM + "-dummy.names");
   MLCOStream dummy2(BS_STEM + "-dummy.data");
   MLCOStream dummy3(BS_STEM + "-dummy.test");
   dummy.close();
   dummy2.close();
   dummy3.close();

   // Without a shell this doesn't work because the asterisk isn't expanded
   // The input from /dev/null ensures yes to all questions
   // Note that this call DOES expand to -A-*.names, etc as well, because
   // -* can expand to -A-*.
   remove_wildcard_files(BS_STEM + "-*.names");
   remove_wildcard_files(BS_STEM + "-*.data");
   remove_wildcard_files(BS_STEM + "-*.test");

   // repeat the cleanup with the other FILE_STEM
   MLCOStream dummy4(FILE_STEM + "-dummy.names");
   MLCOStream dummy5(FILE_STEM + "-dummy.data");
   MLCOStream dummy6(FILE_STEM + "-dummy.test");
   dummy4.close();
   dummy5.close();
   dummy6.close();
   remove_wildcard_files( FILE_STEM + "-*.names");
   remove_wildcard_files( FILE_STEM + "-*.data");
   remove_wildcard_files( FILE_STEM + "-*.test");
   
}

public static void main(String[] args) {
       Mcout << "t_Bootstrap executing." << endl;
   cleanup();

   const int NUM_BS_TYPES = 3;
   for(int i=0; i<NUM_BS_TYPES; i++) {
      Real bsFract;
    
      // based on the loop index, select a bootstrap type.
      Bootstrap::BootstrapType bsType;
      switch(i) {
	 case 0:
	    bsType = Bootstrap::simple;
	    bsFract = 0.632; break;
	 case 1: 
	    bsType = Bootstrap::fractional;
	    bsFract = 0.632; break;
	 case 2: 
	    bsType = Bootstrap::fractional;
	    bsFract = 0.5; break;
	 default:
	    ASSERT(FALSE);
      }
      Mcout << "t_Bootstrap: type = " << bsType << endl;

      InstanceList instList(EMPTY_STRING, NAMES_FILE, DATA_FILE);
      MString prefix("t_Bootstrap.out");
      MString outName = prefix  + MString(i+1, 1);
      MLCOStream out1(outName);
      Bootstrap bootstrap;

      if (!memCheck) {
	 TEST_ERROR("Bootstrap::set_times: illegal number",
		    bootstrap.set_times(-1));
	 TEST_ERROR("Bootstrap::set_type: illegal type",
		    bootstrap.set_type((Bootstrap::BootstrapType)666));
      }

      bootstrap.set_type(bsType);
      bootstrap.set_times(NUM_TIMES);
      if(bsType == Bootstrap::fractional)
	 bootstrap.set_fraction(bsFract);

      bootstrap.set_log_level(2);
      bootstrap.set_log_stream(out1);
      bootstrap.init_rand_num_gen(RAND_SEED);

      if (!memCheck) {
	 TEST_ERROR("PerfEstimator::check_error_data: Must be called",
		    bootstrap.error());
	 TEST_ERROR("PerfEstimator::check_error_data: Must be called",
		    bootstrap.error_std_dev());
      }
      
      Mcout << bootstrap << endl;
      out1 << bootstrap << endl;

      bootstrap.dump_files(instList, FILE_STEM);
      out1 << "Finished dump" << endl;

      Bootstrap bs2(NUM_TIMES, bsType, bsFract);
      bs2.set_log_level(2);
      bs2.set_log_stream(out1);
      ID3Inducer id3Inducer("t_Bootstrap id3 inducer");
      bs2.init_rand_num_gen(RAND_SEED);
      Real acc2 = bs2.estimate_performance(id3Inducer, FILE_STEM);
      ASSERT(acc2 == bs2.error());

      out1  << "ID3 categorizer bootstrap from " << FILE_STEM;
      bs2.display_error_data(out1);
      out1 << endl;
      Mcout << "ID3 categorizer bootstrap from "  << FILE_STEM;
      bs2.display_error_data(Mcout);
      Mcout << endl;

      Bootstrap bs(NUM_TIMES, bsType, bsFract);
      bs.set_log_level(3);
      bs.set_log_stream(out1);
      bs.rand_num_gen().init(RAND_SEED);
      Real accBS = bs.estimate_performance(id3Inducer, instList);
      out1  << "ID3 bs in memory " << bs << endl;
      Mcout << "ID3 bs in memory " << bs << endl;   

      ASSERT(accBS == bs.error());
      ASSERT(accBS == acc2);

      out1 << "ID3 categorizer bootstrap from Instance List: " << endl
	   << bs << endl;

   }

   // Extra bootstrap tests.  These will only be done on the default
   // (.632) bootstrap.
   Mcout << "Extra tests:" << endl;
   InstanceList instList(EMPTY_STRING, NAMES_FILE, DATA_FILE);
   MLCOStream out1("t_Bootstrap.out4");
    
   Bootstrap bs(NUM_TIMES, Bootstrap::fractional);
   bs.set_log_level(3);
   bs.set_log_stream(out1);
   bs.rand_num_gen().init(RAND_SEED);

   ID3Inducer id3Inducer("t_Bootstrap id3 inducer");

   // Test for errors.
   bs.set_times(1);
   bs.estimate_performance(id3Inducer, instList);
   bs.set_fraction(0.5);

   if (!memCheck) {
      TEST_ERROR("Bootstrap::set_fraction: Illegal value",
		 bs.set_fraction(-0.5));
      TEST_ERROR("Bootstrap::set_fraction: Illegal value",
		 bs.set_fraction(1.5));
   }

   // Try everything with a C45 inducer, so we know there's no problem with
   // external inducers.
   cleanup();
   C45Inducer c45Inducer("my c45 inducer");
   Bootstrap C45BS;
   C45BS.set_times(2);
   C45BS.set_log_level(3);
   C45BS.init_rand_num_gen(RAND_SEED);
   Real bserr = Mround(C45BS.estimate_performance(c45Inducer, instList), 4);
   Mcout << "c4.5 error is " << bserr << endl;

   // Dump files.
   C45BS.init_rand_num_gen(RAND_SEED);
   C45BS.set_log_level(0);
   C45BS.dump_files(instList, "t_Bootstrap");

   // Run C4.5 "manually," i.e., without using Bootstrap
   InstanceList train0("t_Bootstrap-0");
   InstanceList test0 (train0.get_schema(),
		       train0.get_original_schema(),
		       "t_Bootstrap-0.test");
   InstanceList train1("t_Bootstrap-1");
   InstanceList test1 (train1.get_schema(),
		       train1.get_original_schema(),
		       "t_Bootstrap-1.test");
   InstanceList train2("t_Bootstrap-A");
   InstanceList test2 (train2.get_schema(),
		       train2.get_original_schema(),
		       "t_Bootstrap-A.test");
   
   Real pruneErr, noPruneErr, estimateErr;
   int noPruneSize, pruneSize;
   
   MString c45Pgm = C45Inducer::defaultPgmName + C45Inducer::defaultPgmFlags;
   LogOptions c45Log;
  
   run_c45(c45Log, c45Pgm, train0, test0, pruneErr, noPruneErr, estimateErr, 
	   noPruneSize, pruneSize);
   Real err0 = pruneErr;

   run_c45(c45Log, c45Pgm, train1, test1, pruneErr, noPruneErr, estimateErr, 
	   noPruneSize, pruneSize);
   Real err1 = pruneErr;
   
   run_c45(c45Log, c45Pgm, train2, test2, pruneErr, noPruneErr, estimateErr, 
	   noPruneSize, pruneSize);
   Real errApp = pruneErr;

   Real avgErr = Mround((err0 + err1) / 2 * .632 + errApp*.368, 4);
   Mcout << "Running C4.5 manually yields " << err0 << ", " << err1 <<
      " and apparent " << errApp << " with the average " << avgErr << endl;

   if(!mlc.approx_equal(avgErr, bserr))
      err << "C45 manual error (" << avgErr << ") does not match "
	 "bootstrap error (" << bserr << ")" << fatal_error;
      
   cleanup();

   System.exit(0); // return success to shell

}
*/
}
