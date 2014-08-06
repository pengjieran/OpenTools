package arithmetic.fss;

import java.io.Writer;

import arithmetic.shared.Globals;
import arithmetic.shared.LogOptions;

public abstract class StateSpaceSearch{

//protected StateSpace< State<LocalInfo, GlobalInfo> >* graph;
protected StateSpace graph;
protected int numExpansions;
protected int evalLimit;
protected byte showTestSetPerf;

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
   private StateSpaceSearch(StateSpaceSearch source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(StateSpaceSearch source){}

protected abstract boolean terminate();

//public State<LocalInfo, GlobalInfo>& search(State<LocalInfo, GlobalInfo>* initialState, GlobalInfo *gInfo)
//{return search(initialState, gInfo, Globals.EMPTY_STRING);}
public State search(State initialState, Object gInfo)
{return search(initialState, gInfo, Globals.EMPTY_STRING);}

//public abstract State<LocalInfo, GlobalInfo>& search(State<LocalInfo, GlobalInfo>* initialState, GlobalInfo *gInfo,
//		String displayFileName);
public abstract State search(State initialState, Object gInfo, String displayFileName);

/***************************************************************************
  Constructor.
***************************************************************************/
public StateSpaceSearch(){graph = null; showTestSetPerf = ShowTestSetPerf.showBestOnly;}

/***************************************************************************
  Finalizer.
***************************************************************************/
protected void finalize(){graph = null;}

/***************************************************************************
  Sets user options common to all search methods.
@param prefix
***************************************************************************/
public void set_user_options(String prefix)
{
//   set_show_test_set_perf(
//      get_option_enum(prefix + "SHOW_TEST_SET_PERF", showTestSetPerfEnum,
//	      get_show_test_set_perf(), Globals.SHOW_TEST_SET_PERF_HELP, true));


   // don't prompt for eval limit because it is specific to whatever
   // is using the StateSpaceSearch (e.g. FSS)
}

/***************************************************************************
  Resets all options to their default values.
***************************************************************************/
public void set_defaults()
{
   set_show_test_set_perf(Globals.DEFAULT_SHOW_TEST_SET_PERF);
   set_eval_limit(Globals.DEFAULT_MAX_EVALS);
}

/***************************************************************************
  Checks if the graph member exists for this object.
***************************************************************************/
public boolean has_graph(){return graph != null;}

/***************************************************************************
  Returns the graph member fro this object.
@return The graph for this object.  
***************************************************************************/
//public StateSpace< State<LocalInfo, GlobalInfo> > get_graph(){return graph;}
public StateSpace get_graph(){return graph;}

/***************************************************************************
  Returns the numExpansions member of this object.
@return The numExpansions member of this object.
***************************************************************************/
public int get_num_expansions(){return numExpansions;}

/***************************************************************************
  Sets the showTestSetPerf member for this object.
@param sra	The new showTestSetPerf member for this object.
***************************************************************************/
public void set_show_test_set_perf(byte i_showTestSetPerf)
{showTestSetPerf = i_showTestSetPerf;}

/***************************************************************************
  Returns the showTestSetPerf member object for this object.
@return The showTestSetPerf member object for this object.
***************************************************************************/
public byte get_show_test_set_perf(){return showTestSetPerf;}

/***************************************************************************
  Sets the evaluation limit for this object.
@param newEvalLimit	The new evaluation limit for this object.
***************************************************************************/
public void set_eval_limit(int newEvalLimit){evalLimit = newEvalLimit;}

/***************************************************************************
  Returns the evaluation limit object for this object.
@return The evaluation limit object for this object.
***************************************************************************/
public int get_eval_limit(){return evalLimit;}

}