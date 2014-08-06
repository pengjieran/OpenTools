package arithmetic.fss;

import java.io.Writer;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.Globals;
import arithmetic.shared.InstanceList;
import arithmetic.shared.LogOptions;

/***************************************************************************
***************************************************************************/

/* found in PerfEstState.h */

abstract public class PerfEstInfo extends StateGlobalInfo
{
   final public static int PERF_EST_INFO = 2001;
   final public static int C45AP_INFO    = 2002;
   final public static int DISC_INFO     = 2003;
   final public static int FSS_INFO      = 2004;
   final public static int ORDER_INFO    = 2005;
   final public static int WEIGHT_INFO   = 2006;


   public int seed;
   public boolean useCompound;
   public double complexityPenalty;
   public PerfEstDispatch perfEst = new PerfEstDispatch();
   public InstanceList trainList;
   public InstanceList testList;
   public BaseInducer inducer;
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
  Constructor.
***************************************************************************/
   public PerfEstInfo(){
      inducer = null;
      trainList = null;
      testList = null;
      seed = PerfEstState.DEFAULT_SEED;
      useCompound = PerfEstState.DEFAULT_USE_COMPOUND;
      complexityPenalty = PerfEstState.DEFAULT_COMPLEX_PENALTY;
   }

// functions required by CompState (see CompState.c for details)

/***************************************************************************
***************************************************************************/
   abstract public int lower_bound(int index);

/***************************************************************************
***************************************************************************/
   abstract public int upper_bound(int index);

/***************************************************************************
***************************************************************************/
   public void display_values(int[] values){display_values(values, Globals.Mcout);}

/***************************************************************************
***************************************************************************/
   abstract public void display_values(int[] values, Writer out);

/***************************************************************************
***************************************************************************/
   public boolean use_compound() { return useCompound; }

//   virtual int class_id() const { return PERF_EST_INFO; }

}