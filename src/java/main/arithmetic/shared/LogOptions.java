package arithmetic.shared;
import java.io.Writer;

/** Provide support for MLC++ logging.
 * @author James Louis Java Implementation.
 * @author Eric Eros 7/8/96 Added DribbleOptions.
 * @author Chia-Hsin Li 12/27/94 Added FLOG.
 * @author James Dougherty 10/10/94
 * @author Ronny Kohavi 12/21/93 Initial revision (.h,.c)
 */
public class LogOptions {
    private Writer globalLogStream; //MLC++ defined in a.h, basics.h
    
    private int logLevel = 3;    //default logLevel (??) -SG
    private Writer stream;
    private static int DEFAULT_LINE_NUM_WIDTH = 4;
    
   /* Options that are contained in a MLC++ MLCIOStream,
      here we can just reference them to write the log file,
      when it is written to a file, rather that to System.out */
    
    static String newline_prefix = "";
    static String err_prefix = "Err: "; // Added JWP 11-24-2001
    String wrap_prefix;
    String WRAP_INDENT = "\n";
    
    /** Constructor.
     */
    public LogOptions() {
        logLevel = GlobalOptions.logLevel;
        stream = globalLogStream;
    }
    
    /** Constructor.
     * @param logOptionName The name of the log level option.
     */
    public LogOptions(String logOptionName) {
        GetEnv getenv =  new GetEnv();
        logLevel = getenv.get_option_int(logOptionName);
        stream = globalLogStream;
    }
    
    /** Set LogOptions based on another instance as prototype.
     * @param opt The LogOptions instance copied.
     */
    public void set_log_options(LogOptions opt) {
        set_log_level(opt.get_log_level());
        set_log_stream(opt.get_log_stream());
    }
    
    /** Sets the log level. Negative log levels are turned into 0. This
     * allows calls such as set_log_level(get_log_level() - 3).
     * @param level The new log level.
     */
    public void set_log_level(int level) {
        logLevel = Math.max(0,level);
    }
    
    /** Returns the log level.
     * @return The log level.
     */
    public int get_log_level() {
        return logLevel;
    }
    
    /** Sets the display stream.
     * @param strm The stream to be written to.
     */
    public void set_log_stream(Writer strm) {
        stream = strm;
    }
    
    /** Returns the stream used for display.
     * @return The stream used for display.
     */
    public Writer get_log_stream() {
        return stream;
    }
    
    /** Sets prefixes displayed before log message.
     * @param file The name of the file containing this log message.
     * @param line Line number of log message.
     */
    public void set_log_prefixes(String file, int line) {
        if(GlobalOptions.showLogOrigin) {
            String adjFile = file;
            newline_prefix = adjFile + "::"+line+": ";
        }
        else
            newline_prefix = "";
        wrap_prefix = newline_prefix+ WRAP_INDENT;
    }
    
    /** Sets prefixes displayed before log message.
     * @param file The name of the file containing this log message.
     * @param line Line number of log message.
     * @param stmtLogLevel The log level of the log message statement.
     * @param lgLevel The current log level being displayed.
     */
    public void set_log_prefixes(String file, int line, int stmtLogLevel,
    int lgLevel) {
        if(GlobalOptions.showLogOrigin) {
            String adjFile = file;
            newline_prefix = "["+stmtLogLevel+"<="+logLevel+ "] "+adjFile+"::"+line+": ";
        }
        else
            newline_prefix = "";
        wrap_prefix = newline_prefix + WRAP_INDENT;
    }
    
    /** Returns this LogOptions.
     * @return This LogOptions object.
     */
    public LogOptions get_log_options() {
        return this;
    }
    
    /** Displays a log message.
     * @param logNumber The log number of the message.
     * @param logMessage The message being displayed.
     */
    public void LOG(int logNumber, String logMessage) {
        if(logNumber <= logLevel)  {
            System.out.print(newline_prefix + logMessage);
            System.out.flush();
        }
    }
    
    /** Displays a log message.
     * @param logNumber The log number of the message.
     * @param logChar The message being displayed.
     */
    public void LOG(int logNumber, char logChar) {
        if(logNumber <= logLevel){
            System.out.print(newline_prefix + logChar);
            System.out.flush();
        }
    }
    
    /** Displays an error message.
     * @param logNumber The log level of the error message.
     * @param errMessage The error message.
     */
    public void ERR(int logNumber, String errMessage) {
        if (logNumber <= logLevel) {
            System.err.print(err_prefix + errMessage);
            System.err.flush();
        }
    }
    
    /** Displays log message according to the global log level.
     * @param logNumber The log number of the message.
     * @param logMessage The message to be displayed.
     */
    static public void GLOBLOG(int logNumber, String logMessage) {
        if(logNumber <= GlobalOptions.logLevel){
            System.out.print(newline_prefix + logMessage);
            System.out.flush();
        }
    }
    
    /** Displays log message according to the global log level.
     * @param logNumber The log number of the message.
     * @param logChar The message to be displayed.
     */
    static public void GLOBLOG(int logNumber, char logChar) {
        if(logNumber <= GlobalOptions.logLevel){
            //         System.out.print("LOG-->"+logChar);
            System.out.print(newline_prefix + logChar);
            System.out.flush();
        }
    }
    
    /** If the dribble is set to TRUE, this method displays the given message.
     * @param dribbleMessage The message to be displayed.
     * @see GlobalOptions#dribble
     */
    public void DRIBBLE(String dribbleMessage) {
        if(GlobalOptions.dribble){
            System.out.println("DRIBBLE-->"+dribbleMessage);
            System.out.flush();
        }
    }
}
