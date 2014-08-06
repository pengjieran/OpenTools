package arithmetic.fss;

import java.io.Writer;

import arithmetic.shared.Error;
import arithmetic.shared.Globals;
import arithmetic.shared.LogOptions;

public class SearchDispatch {
    
    // Member data
    // StateSpaceSearch/general options
    //obs   SearchMethod searchMethod;
    //obs   ShowTestSetPerf showTestSetPerf;
    byte searchMethod;
    byte showTestSetPerf;
    int evalLimit;
    
    // Hill Climbing (HCSearch) options
    /* None */
    
    // Best-first search (BFSearch) options
    int maxStale;
    double epsilon;
    
    // Simulated Annealing (SASearch) options
    /* maxStale and epsilon same as BFSearch */
    int maxEvals;
    int minExpEvals;
    double lambda;
    int sasSeed;
    
    
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
     * Returns the LogOptions object for this object.
     * @return The LogOptions object for this object.
     ***************************************************************************/
    public LogOptions get_log_options(){return logOptions;}
    
    /***************************************************************************
     * Sets the LogOptions object for this object.
     * @param opt	The new LogOptions object.
     ***************************************************************************/
    public void set_log_options(LogOptions opt)
    {logOptions.set_log_options(opt);}
    
    /***************************************************************************
     * Sets the logging message prefix for this object.
     * @param file	The file name to be displayed in the prefix of log messages.
     * @param line	The line number to be displayed in the prefix of log messages.
     * @param lvl1 The log level of the statement being logged.
     * @param lvl2	The level of log messages being displayed.
     ***************************************************************************/
    public void set_log_prefixes(String file, int line,int lvl1, int lvl2)
    {logOptions.set_log_prefixes(file, line, lvl1, lvl2);}
    
    
    
    
    
    /***************************************************************************
     * This class has no access to a copy constructor.
     ***************************************************************************/
    private SearchDispatch(SearchDispatch source){}
    
    /***************************************************************************
     * This class has no access to an assign method.
     ***************************************************************************/
    private void assign(SearchDispatch source){}
    
    
    
    /***************************************************************************
     * Constructor.
     ***************************************************************************/
    public SearchDispatch() { set_defaults(); }
    
    
    /***************************************************************************
     * Sets all options to their default values.
     ***************************************************************************/
    public void set_defaults() {
        searchMethod = Globals.DEFAULT_SEARCH_METHOD;
        showTestSetPerf = Globals.DEFAULT_SHOW_TEST_SET_PERF;
        evalLimit = Globals.DEFAULT_EVAL_LIMIT;
        maxStale = Globals.DEFAULT_MAX_STALE;
        epsilon = Globals.DEFAULT_EPSILON;
        maxEvals = Globals.DEFAULT_MAX_EVALS;
        minExpEvals = Globals.DEFAULT_MIN_EXP_EVALS;
        lambda = Globals.DEFAULT_LAMBDA;
        sasSeed = Globals.DEFAULT_SAS_SEED;
    }
    
    
    //obs   StateSpaceSearch<LocalInfo, GlobalInfo> build_search()
    public StateSpaceSearch build_search()
    
    {
        //obs      StateSpaceSearch<LocalInfo, GlobalInfo> *search;
        StateSpaceSearch search = null;
        // set search-specific options
        switch(searchMethod) {
            case SearchMethod.hillClimbing:
                //obs            search = new HCSearch<LocalInfo, GlobalInfo>;
                search = new HCSearch();
                break;
                
            case SearchMethod.bestFirst: {
                //obs            search = new BFSearch<LocalInfo, GlobalInfo>;
                //obs            BFSearch<LocalInfo, GlobalInfo> *bfSearch =
                //obs               (BFSearch<LocalInfo, GlobalInfo> *)search;
                search = new BFSearch();
                BFSearch bfSearch = (BFSearch)search;
                bfSearch.set_max_non_improving_expansions(maxStale);
                bfSearch.set_min_expansion_improvement(epsilon);
                break;
            }
            
            case SearchMethod.simulatedAnnealing: {
                //obs            search = new SASearch<LocalInfo, GlobalInfo>;
                //obs            SASearch<LocalInfo, GlobalInfo> *saSearch =
                //obs               (SASearch<LocalInfo, GlobalInfo> *)search;
                search = new SASearch();
                SASearch saSearch = (SASearch)search;
                saSearch.set_max_non_improving_expansions(maxStale);
                saSearch.set_min_expansion_improvement(epsilon);
                saSearch.set_max_evaluations(maxEvals);
                saSearch.set_min_exp_evaluations(minExpEvals);
                saSearch.set_initial_lambda(lambda);
                saSearch.set_sas_seed(sasSeed);
                //            break;
            }
            
            default:
                Error.fatalErr("SearchDispatch: unsupported type of search");
                
        }
        // set general search options
        search.set_show_test_set_perf(showTestSetPerf);
        search.set_eval_limit(evalLimit);
        
        return search;
    }
    
    
/*
 
 
   public void set_user_options()
{ set_user_options(Globals.EMPTY_STRING);}
 
/***************************************************************************
  Description  : Sets options based on user input/external options stored
                   in the option server.  Only sets options appropriate
                   for the selected search method.
  Comments     :
 ***************************************************************************
   public void set_user_options(String prefix)
   {
      searchMethod = get_option_enum(prefix + "SEARCH_METHOD",
                                  searchMethodEnum,
                                  searchMethod,
                                  SEARCH_METHOD_HELP, FALSE);
      showTestSetPerf = get_option_enum(prefix + "SHOW_TEST_SET_PERF",
                                      showTestSetPerfEnum,
                                      showTestSetPerf,
                                      SHOW_TEST_SET_PERF_HELP, TRUE);
      evalLimit = get_option_int(prefix + "EVAL_LIMIT",
                              evalLimit,
                              EVAL_LIMIT_HELP, TRUE);
 
      if(searchMethod == bestFirst ||
         searchMethod == simulatedAnnealing) {
         maxStale = get_option_int(prefix + "MAX_STALE", maxStale,
                                MAX_STALE_HELP, FALSE);
         epsilon = get_option_real(prefix + "EPSILON", epsilon,
                                EPSILON_HELP, FALSE);
      }
 
      if(searchMethod == simulatedAnnealing) {
         maxEvals = get_option_int(prefix + "MAX_EVALS", maxEvals,
                                MAX_EVALS_HELP, TRUE);
         minExpEvals = get_option_int(prefix + "MIN_EXP_EVALS", minExpEvals,
                                   MIN_EXP_EVALS_HELP, TRUE);
         lambda = get_option_real(prefix + "LAMBDA", lambda,
                               LAMBDA_HELP, FALSE);
         sasSeed = get_option_int(prefix + "SAS_SEED", sasSeed,
                               SAS_SEED_HELP, TRUE);
      }
   }
 
 
 */
    // set/get functions for all options
    
    //obs   public void set_search_method(SearchMethod method) { searchMethod = method; }
    public void set_search_method(byte method) { searchMethod = method; }
    
    //obs   public byte get_search_method()   { return searchMethod; }
    public byte get_search_method()   { return searchMethod; }
    
    //obs   public void set_show_test_set_perf(ShowTestSetPerf show)
    public void set_show_test_set_perf(byte show)
    { showTestSetPerf = show; }
    
    //obs   public ShowTestSetPerf get_show_test_set_perf()
    public byte get_show_test_set_perf()
    { return showTestSetPerf; }
    
    public void set_eval_limit(int limit) {
        if(limit < 0)
            Error.fatalErr("SearchDispatch::set_eval_limit: EVAL_LIMIT must be >= 0");
        evalLimit = limit;
    }
    
    public int get_eval_limit()   { return evalLimit; }
    
    public void set_max_stale(int newMaxStale) {
        if(newMaxStale < 1)
            Error.fatalErr("SearchDispatch::set_max_stale: MAX_STALE must be positive");
        maxStale = newMaxStale;
    }
    
    public int get_max_stale()   { return maxStale; }
    
    public void set_epsilon(double newEpsilon) {
        epsilon = newEpsilon;
    }
    
    public double get_epsilon()   { return epsilon; }
    
    public void set_max_evals(int evals) {
        if(evals < 0)
            Error.fatalErr("SearchDispatch::set_max_evals: MAX_EVALS must be >= 0");
        maxEvals = evals;
    }
    
    public int get_max_evals()   { return maxEvals; }
    
    public void set_min_exp_evals(int minExp) {
        if(minExp < 0)
            Error.fatalErr("SearchDispatch::set_min_exp_evals: MIN_EXP_EVALS must be >= 0");
        minExpEvals = minExp;
    }
    
    public int get_min_exp_evals()   { return minExpEvals; }
    
    public void set_lambda(double newLambda) {
        if(newLambda < 0)
            Error.fatalErr("SearchDispatch::set_lambda: LAMBDA must be >= 0");
        lambda = newLambda;
    }
    
    public double get_lambda()   { return lambda; }
    
    public void set_sas_seed(int newSeed) {
        sasSeed = newSeed;
    }
    
    public int get_sas_seed()   { return sasSeed; }
  /*  
    public static void main(String[] args) {
        // force inclusion of MWrapper to avoid warnings
        //BaseInducer *(*useMwrapper)(const MString) = env_inducer;
        //(void)useMwrapper;
        
        Globals.Mcout.write("Executing t_SearchDispatch\n");
        SearchDispatch dispatch = new SearchDispatch();
        
        // set options through the option server, then call set user options
        // for each major use of the dispatcher:
        
        // set up an HC search
        optionServer.set_option("SEARCH_METHOD=hill-climbing");
        optionServer.set_option("SHOW_TEST_SET_PERF=never");
        optionServer.set_option("EVAL_LIMIT=200");
        dispatch.set_user_options();
        HCSearch hcSearch = (HCSearch)dispatch.build_search();
        MLJ.ASSERT(hcSearch.get_show_test_set_perf() == showNever);
        MLJ.ASSERT(hcSearch.get_eval_limit() == 200);
        
        // set up a BF search
        optionServer.set_option("SEARCH_METHOD=best-first");
        optionServer.set_option("MAX_STALE=10");
        optionServer.set_option("EPSILON=0.1");
        dispatch.set_user_options();
        BFSearch bfSearch = (BFSearch )dispatch.build_search();
        
        // set up an SA search
        optionServer.set_option("SEARCH_METHOD=simulated-annealing");
        optionServer.set_option("MAX_EVALS=20");
        optionServer.set_option("MIN_EXP_EVALS=10");
        optionServer.set_option("LAMBDA=0.2");
        optionServer.set_option("SAS_SEED=12345");
        dispatch.set_user_options();
        SASearch saSearch = (SASearch)dispatch.build_search();
        MLJ.ASSERT(saSearch.get_max_non_improving_expansions() == 10,"SearchDispatch.main: saSearch.get_max_non_improving_expansions() != 10");
        MLJ.ASSERT(saSearch.get_min_expansion_improvement() == 0.1,"SearchDispatch.main: saSearch.get_min_expansion_improvement() != 0.1");
        MLJ.ASSERT(saSearch.get_max_evaluations() == 20,"SearchDispatch.main: saSearch.get_max_evaluations() == 20");
        MLJ.ASSERT(saSearch.get_min_exp_evaluations() == 10,"SearchDispatch.main: saSearch.get_min_exp_evaluations() == 10");
        MLJ.ASSERT(saSearch.get_sas_seed() == 12345,"SearchDispatch.main: saSearch.get_sas_seed() == 12345");
        
        // clean up
        hcSearch = null;
        bfSearch = null;
        saSearch = null;
        
        Globals.Mcout.write("Success!\n");
        System.exit(0);
    }
*/
}