package arithmetic.fss;

import java.util.Random;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.CatTestResult;
import arithmetic.shared.Categorizer;
import arithmetic.shared.Error;
import arithmetic.shared.Globals;
import arithmetic.shared.Inducer;
import arithmetic.shared.InstanceList;
import arithmetic.shared.MLJ;

/***************************************************************************
  Wrapper inducer for search-based induction methods.
  Complexity   : Training is the number of states searched times the
                   estimation time per state.
@author Dan Sommerfield	(5/21/95)	Initial revision (.h,.c)
@author James Louis	(8/9/2001)	Ported to Java.
***************************************************************************/
abstract public class SearchInducer extends Inducer
{

/** **/
private Random randNumGen;
/** **/
private String dotFileName;


/** **/
protected int[] finalStateInfo;
//protected SearchMethod serachMethod;
/** **/
protected Categorizer categorizer;
/** **/
protected BaseInducer baseInducer;
/** **/
public PerfEstInfo globalInfo;// Global search information. Must be 
					//set in derived class constructor

/** **/
private String COMPLEX_PENALTY_HELP = "This option specifies a multiplier "
  +"which determines how much complexity of a state hurts its fitness.";

/** **/
private String USE_COMPOUND_HELP = "This option specifies whether or not to "
  +"combine information about generated states in the search in an attempt "
  +"to generate a better state more quickly.";

/** **/
private String DOT_FILE_NAME_HELP = "This option specifies the file that will "
  +"receive output for the graphical representation of the search, which "
  +"can be displayed by dotty.";

//SearchDispatch<Array<int>, PerfEstInfo> searchDispatch;  originally- JL 
/** **/
SearchDispatch searchDispatch = new SearchDispatch();


/***************************************************************************
  Constructor.
@param description	A description of this SearchInducer object.
***************************************************************************/
public SearchInducer(String description)
{
super(description);
baseInducer = null;
dotFileName = description + ".dot";
categorizer = null;
globalInfo = null;
finalStateInfo = null;
}

/***************************************************************************
  Constructor.
@param description	A description of this SearchInducer object.
@param ind			The inducer to be wrapped in this SearchInducer 
					object.
***************************************************************************/
public SearchInducer(String description, BaseInducer ind)
{
super(description);
baseInducer = ind;
dotFileName = description + ".dot";
categorizer = null;
globalInfo = null;
finalStateInfo = null;
}

/***************************************************************************
  Verify that the global info was correctly created by the constructor.
@return TRUE if the global info has been set, FALSE otherwise.
***************************************************************************/
public boolean has_global_info()
{
return has_global_info(false);
}

/***************************************************************************
  Verify that the global info was correctly created by the constructor.
@return TRUE if the global info has been set, FALSE otherwise.
@param fatalOnFalse	Set to TRUE if an error message display is 
					requested. FALSE otherwise.
***************************************************************************/
public boolean has_global_info(boolean fatalOnFalse)
{
if(fatalOnFalse && (globalInfo == null))
Error.fatalErr("SearchInducer::has_global_info: global_info should have "
+ "been set by constructor");
return (globalInfo != null);
}

/***************************************************************************
  Returns true if this class has a valid Categorizer object. Displays an 
error message if there is no categorizer.
@return TRUE if there is a Categorizer, FALSE otherwise.
***************************************************************************
public boolean was_trained(){return was_trained(true);}

/***************************************************************************
  Returns true if this class has a valid Categorizer object.
@param fatalOnFalse	Set to TRUE if an error message is requested, 
					FALSE otherwise.
@return TRUE if there is a Categorizer, FALSE otherwise.
***************************************************************************/
public boolean was_trained(boolean fatalOnFalse){
   if((fatalOnFalse)&&(categorizer == null))
      Error.fatalErr("SearchInducer::was_trained: No categorizer, "
	  +"Call train() to create categorizer");
   return (categorizer != null);
}

/***************************************************************************
  Returns the categorizer that the inducer has generated.
@return The Categorizer this Inducer produces.
****************************************************************************/
public Categorizer get_categorizer(){
   was_trained(true);
   return categorizer;
}

/***************************************************************************
  Gives ownership of the generated categorizer to the caller, reverting 
the Inducer to untrained state.
@return The Categorizer this Inducer produces.
***************************************************************************/
public Categorizer release_categorizer(){
   was_trained(true);
   Categorizer retCat = categorizer;
   categorizer = null;
   return retCat;
}

abstract public PerfEstInfo create_global_info();
abstract public int[] create_initial_info(InstanceList trainingSet);
abstract public PerfEstState create_initial_state(int[] initialInfo, PerfEstInfo gI);

/***************************************************************************
  Sets the seed for the random number generator to the specified value.
@param newSeed	The specified value for the new seed.
***************************************************************************/
public void set_seed(int newSeed){
//   DBG(has_global_info());
   globalInfo.seed = newSeed;
}

/***************************************************************************
  Returns the seed number for the random number generator.
@return The seed number for the random number generator.
***************************************************************************/
public int get_seed(){
//   DBG(has_global_info());
   return globalInfo.seed;
}

/***************************************************************************
***************************************************************************/
public void set_use_compound(boolean newCompound){
//   DBG(has_global_info());
   globalInfo.useCompound = newCompound;
}

/***************************************************************************
***************************************************************************/
public boolean get_use_compound(){
//   DBG(has_global_info());
   return globalInfo.useCompound;
}

/***************************************************************************
***************************************************************************/
public void set_cmplx_penalty(double penalty){
//   DBG(has_global_info());
   globalInfo.complexityPenalty = penalty;
}

/***************************************************************************
***************************************************************************/
public double get_cmplx_penalty(){
//   DBG(has_global_info());
   return globalInfo.complexityPenalty;
}

/***************************************************************************
  Returns the final state info. Displays an error message if there is no 
final state info.
@return The final state info.
***************************************************************************/
public int[] get_final_state_info(){
   if(finalStateInfo == null)
      Error.err("SearchInducer::get_final_state_info: finalStateInfo is NULL)");
   return finalStateInfo;
}

/***************************************************************************
  Default state_to_categorizer function aborts.
***************************************************************************/
public Categorizer state_to_categorizer(int[] stateInfo){
   Error.fatalErr("SearchInducer::state_to_categorizer: should never be called");
   return null;
}

/***************************************************************************
  Returns TRUE if this Inducer can support full testing. The SearchInducer 
can support full testing only if the wrapped inducer can.
@return TRUE if this Inducer supports full testing, FALSE otherwise.
***************************************************************************/
public boolean supports_full_testing(){
   has_global_info();
   return baseInducer.can_cast_to_inducer();
}

/***************************************************************************
***************************************************************************/
public PerfEstDispatch perf_est_dispatch()
{
//  DBG(has_global_info());
  return globalInfo.perfEst;
}

/***************************************************************************
  Read all options from the user.
@param prefix
***************************************************************************/
public Random rand_num_gen() {return randNumGen;}

/***************************************************************************
  Read all options from the user.
@param prefix
***************************************************************************/
public void init_rand_num_gen(long seed) {randNumGen.setSeed(seed);}

/***************************************************************************
  Find best attributes and create categorizer.
***************************************************************************/
public void train()
{
   has_data();
//   DBG(OK());

   has_global_info();

   // give a nice error message if attempting to use test-set as an
   // performance estimator
   if(globalInfo.perfEst.get_perf_estimator() == PerfEstDispatch.testSet)
      Error.fatalErr("SearchInducer::train: performance estimation method may not "
	 +"be test-set if using Inducer::train()");
   
   // set the inducer
   globalInfo.inducer = baseInducer;
   if(globalInfo.inducer == null)
      Error.fatalErr("SearchInducer::train: must call set_user_options prior to "
	 +"train if inducer is not set");
   
   if (globalInfo.inducer.can_cast_to_inducer() == false)
      Error.fatalErr("SearchInducer::train: wrapped inducer must be derived from "
	 +"Inducer to use train()");

   // test data not available so force the SHOW_TEST_SET_PERF option to
   // never for this search.
   byte oldOption = searchDispatch.get_show_test_set_perf();
   searchDispatch.set_show_test_set_perf(ShowTestSetPerf.showNever);

   // use the final state of the search to build a categorizer
   search(TS);
   categorizer = null;
   categorizer = state_to_categorizer(finalStateInfo);

   // restore the previous value of SHOW_TEST_SET_PERF
   searchDispatch.set_show_test_set_perf(oldOption);
}




/*
protected SearchDispatch<Array<int>, PerfEstInfo> searchDispatch;   

/***************************************************************************
  Read all options from the user.
@param prefix
***************************************************************************
public void set_user_options(String prefix){
   // make sure the global info exists
   GetEnv GE = new GetEnv();
   has_global_info();
  
   // if no inducer, use an option
   if(baseInducer == null)
      baseInducer = env_inducer(prefix);

   // create a modified prefix without the final underscore (if there is one)
   String modPrefix;
   if((prefix.length()!= 0)&&(prefix.endsWith("_")))
      modPrefix = prefix.substring(0, prefix.length()-2);
   else
      modPrefix = prefix;

   // read dot file name here using modified-prefix.dot as the
   // default name of the file.
   dotFileName =
      GE.get_option_string(prefix + "DOT_FILE", modPrefix + ".dot",
			DOT_FILE_NAME_HELP, false);

   // set search options
   searchDispatch.set_user_options(prefix);
   
   // set performance estimation options
   perf_est_dispatch().set_user_options(prefix);

   // set extra search-global options
   globalInfo.useCompound =
      GE.get_option_bool(prefix + "USE_COMPOUND", globalInfo.useCompound,
		      USE_COMPOUND_HELP, true);

   globalInfo.complexityPenalty =
      GE.get_option_real(prefix + "CMPLX_PENALTY",
		      globalInfo.complexityPenalty, COMPLEX_PENALTY_HELP,
		      true);

   globalInfo.seed = perf_est_dispatch().get_seed();
}


/***************************************************************************
  Search and return the final state reached.
***************************************************************************/
private double search(InstanceList trainingSet){
   has_global_info();

   // set the inducer
   globalInfo.inducer = baseInducer;
   if(globalInfo.inducer == null)
      Error.fatalErr("SearchInducer::search: must call set_user_options prior to "
	 +"search if inducer is not set");

   // set up global info for search
//   globalInfo.trainList = null;
   globalInfo.trainList =(InstanceList)trainingSet.clone();
   
   // lower the inducer's log level.
   globalInfo.inducer.set_log_level(get_log_level() - 5);

   // set up the search using the dispatcher.  Multiply the EVAL_LIMIT
   // in the dispatcher by the number of attributes in the data here
   int savedEvalLimit = searchDispatch.get_eval_limit();
   searchDispatch.set_eval_limit(savedEvalLimit * trainingSet.num_attr());
//obs   StateSpaceSearch<Array<int>, PerfEstInfo> *ssSearch = searchDispatch.build_search();
   StateSpaceSearch ssSearch = searchDispatch.build_search();

   // copy log level to the search
   ssSearch.set_log_level(get_log_level()); 
   globalInfo.perfEst.set_log_level(get_log_level() - 4);

   // Initialize the first state
   int[] initialInfo = create_initial_info(trainingSet);
   PerfEstState initialState = create_initial_state(initialInfo, globalInfo);
   initialState.set_log_level(get_log_level());

   // multiply eval limit by number of attributes in data
   //@@ do this from the dispatcher ***

   // Search
//obs   const State<Array<int>, PerfEstInfo>& searchState = ssSearch.search(initialState, globalInfo, dotFileName);
   State searchState = ssSearch.search(initialState, globalInfo, dotFileName);

   finalStateInfo = null;
//obs   finalStateInfo = new Array<int>(searchState.get_info(), ctorDummy);

   int[] otherInfo = (int[])searchState.get_info();
   finalStateInfo = new int[otherInfo.length];   
   for(int i = 0;
       i < otherInfo.length;
       i++)
      finalStateInfo[i] = otherInfo[i];
   

   double finalError = globalInfo.testList == null ? - Globals.REAL_MAX :
      searchState.get_test_set_fitness();

   // restore modified EVAL_LIMIT option here
   searchDispatch.set_eval_limit(savedEvalLimit);

   logOptions.LOG(2, "Search node:"+searchState+"\n");

   ssSearch = null;
   
   return finalError;
}

/***************************************************************************
  Find best attributes and run test.
  Comments    : @@ this currently only works if our inner inducer is an
                  Inducer (it should work just if the inner inducer supports
		  full testing).
@return
@param
@param
***************************************************************************/
public CatTestResult train_and_perf(InstanceList trainingSet,
					 InstanceList testList)
{
   if (trainingSet == null)
      Error.fatalErr("SearchInducer::train_and_perf: given a NULL training set");

   if (!supports_full_testing())
      Error.fatalErr("SearchInducer::train_and_perf: my inner inducer does not "
	 +"produce categorizers");
   
   // if we're simulating a full Inducer, use its train_and_test instead
   if (can_cast_to_inducer())
      return super.train_and_perf(trainingSet, testList);

   MLJ.ASSERT(trainingSet != null && testList != null,
      "SearchInducer.train_and_perf: trainingSet == null || testList == null.");
   has_global_info();

   // set up test data
//obs   delete globalInfo->testList;
   globalInfo.testList = null;
   globalInfo.testList = (InstanceList)testList.clone();

   double error = search(trainingSet);
   MLJ.ASSERT(error > -Globals.REAL_MAX,"SearchInducer.train_and_perf: error <= -Globals.REAL_MAX.");

   // If we're a regular inducer, actually build the categorizer
   //    for the last state, so we can extract information out of it.
   MLJ.ASSERT(baseInducer.can_cast_to_inducer(),"SearchInducer.train_and_perf: !baseInducer.can_cast_to_inducer().");

   // Assign the training set to ourselves so we can create the categorizer
   InstanceList oldTS = trainingSet;
   InstanceList origTS = assign_data(trainingSet);
   oldTS.OK();
   if (origTS!= null)
      origTS.OK();
//obs   delete categorizer;
   categorizer = null;
   categorizer = state_to_categorizer(get_final_state_info());
   trainingSet = assign_data(origTS);
   MLJ.ASSERT(trainingSet == oldTS,"SearchInducer.train_and_perf: trainingSet != oldTS.");

   MLJ.ASSERT(trainingSet != null,"SearchInducer.train_and_perf: trainingSet == null.");
   trainingSet.OK();
   CatTestResult result = new CatTestResult(categorizer, trainingSet, testList);
   return result;
}


/***************************************************************************
  Train and test from dumped files.
***************************************************************************/
public double train_and_test_files(String fileStem)
{
return train_and_test_files(fileStem, Globals.DEFAULT_NAMES_EXT, 
Globals.DEFAULT_DATA_EXT, Globals.DEFAULT_TEST_EXT);
}

/***************************************************************************
  Train and test from dumped files.
***************************************************************************/
public double train_and_test_files(String fileStem, String namesExtension)
{
return train_and_test_files(fileStem, namesExtension, 
Globals.DEFAULT_DATA_EXT, Globals.DEFAULT_TEST_EXT);
}

/***************************************************************************
  Train and test from dumped files.
***************************************************************************/
public double train_and_test_files(String fileStem, String namesExtension, 
String dataExtension)
{
return train_and_test_files(fileStem, namesExtension, dataExtension, 
Globals.DEFAULT_TEST_EXT);
}

/***************************************************************************
  Train and test from dumped files.
***************************************************************************/
public double train_and_test_files(String fileStem, 
String namesExtension, String dataExtension, String testExtension)
{
   InstanceList trainList = new InstanceList(Globals.EMPTY_STRING, fileStem + namesExtension,
			  fileStem + dataExtension);
   InstanceList testList = new InstanceList(trainList.get_schema(),
			  trainList.get_original_schema(),
			  fileStem + testExtension);
   double error = train_and_test(trainList, testList);
   return error;

}


/*
public void display(){display(Globals.Mcout);}
public void display(Writer stream){
   has_global_info();
   globalInfo.perfEst.display_settings(stream);
}
*/





/***************************************************************************
  Find best attributes and run test.
@result
@param
@param
***************************************************************************/
public double train_and_test(InstanceList trainingSet,
			       InstanceList testList)
{
   // if we're simulating a full Inducer, use its train_and_test instead
   if (can_cast_to_inducer())
      return super.train_and_test(trainingSet, testList);

   MLJ.ASSERT(trainingSet != null && testList != null,
      "SearchInducer.train_and_test: trainingSet != null && testList != null.");
   has_global_info();

   // set up test data
//obs   delete globalInfo->testList;
   globalInfo.testList = null;
   globalInfo.testList = (InstanceList)testList.clone();

   double error = search(trainingSet);
   MLJ.ASSERT(error > -Globals.REAL_MAX,
      "SearchInducer.train_and_test: error > -Globals.REAL_MAX.");

   // If we're a regular inducer, actually build the categorizer
   //    for the last state, so we can extract information out of it.
   if (baseInducer.can_cast_to_inducer()) {
      // Assign the training set to ourselves so we can create the categorizer
      InstanceList oldTS = trainingSet;
      InstanceList origTS = assign_data(trainingSet);
//obs      delete categorizer;
      categorizer = null;
      categorizer = state_to_categorizer(get_final_state_info());
      trainingSet = assign_data(origTS);
      MLJ.ASSERT(trainingSet == oldTS,
         "SearchInducer.train_and_test: trainingSet != oldTS.");
   }
   return error;
}

/***************************************************************************
  Cast to inducer if base can cast.
***************************************************************************/
public boolean can_cast_to_inducer()
{
   has_global_info();

   globalInfo.inducer = baseInducer;
   MLJ.ASSERT(globalInfo.inducer != null,"SearchInducer.can_cast_to_inducer: globalInfo.inducer == null.");
   boolean canCast = globalInfo.inducer.can_cast_to_inducer();
   if (!canCast)
      	logOptions.LOG(2, "SearchInducer wrapping around base inducer");
   else {
      canCast = searchDispatch.get_show_test_set_perf() == ShowTestSetPerf.showNever;
      if (!canCast)
	 logOptions.LOG(2, "SearchInducer show test set perf != never");
   }
   if (canCast)
      logOptions.LOG(2, "SearchInducer simulating Inducer\n");
   else
      logOptions.LOG(2, ". Simulating BaseInducer\n");

   return canCast;
}

//obs public SearchDispatch<Array<int>, PerfEstInfo>& search_dispatch()
public SearchDispatch search_dispatch()
{return searchDispatch; }



  public int num_nontrivial_nodes() {
    return 0;
  }

  public int num_nontrivial_leaves() {
    return 0;
  }



/*






/***************************************************************************
  Description : Create an inducer from the given inducerType.
                Return NULL if none of the inducers are recognized.
  Comments    :
***************************************************************************

public static BaseInducer search_inducers(String prefix, InducerType inducerType,String inducerName)
{
   if (inducerType == fss) {
      FSSInducer *inducer = new FSSInducer(inducerName);
      inducer.set_user_options(prefix + "FSS_");
      return inducer;
   } else if (inducerType == discSearch) {
      DiscSearchInducer *inducer = new DiscSearchInducer(inducerName);
      inducer.set_user_options(prefix + "DISC_");
      return inducer;
   } else if (inducerType == orderFSS) {
      OrderFSSInducer *inducer = new OrderFSSInducer(inducerName);
      inducer.set_user_options(prefix + "OFSS_");
      return inducer;
   } else if (inducerType == c45ap) {
      C45APInducer *inducer = new C45APInducer(inducerName);
      inducer.set_user_options(prefix + "AP_");
      return inducer;
   } else if (inducerType == tableCas) {
      TableCasInd *inducer = new TableCasInd(inducerName);
      inducer.set_user_options(prefix);
      return inducer;
   } else if (inducerType == WeightSearch) {
      WeightSearchInducer *inducer = new WeightSearchInducer(inducerName);
      inducer.set_user_options(prefix + "WEIGHT_");
      return inducer;
   } else if (inducerType == fcfInducer) {
      ConstrFilterInducer *inducer = new ConstrFilterInducer(inducerName);
      inducer.set_user_options(prefix + "CONSTR_");
      return inducer;
   } else
      return NULL;
}
*/


/*
class SearchInducer : public Inducer {
public:
   // Methods
   // Get's ownership of given inducer.  Defaults to environment
   //   variable <prefix>INDUCER if NULL
   virtual void set_user_options(const MString& prefix);
   void display(MLCOStream& stream = Mcout) const;

};
*/

}
