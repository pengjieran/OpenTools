package arithmetic.shared;

/** This file processes a series of static, global options used by the MLC++
 * library. These options are read as super-nuisance options--they are never
 * prompted from the user. Some of these options are used BEFORE main() executes.
 * Therefore, option values read on the command-line are not yet available. These
 * options are given values directly from the environment at init time, and may be
 * reset to command-line values once main() executes.
 * @author James Louis 4/15/2002 Java Implementation
 * @author Dan Sommerfield 1/17/97 Initial revision (.h,.c)
 */
public class GlobalOptions {
    /** The level of logs that will be displayed.
     */
    public static int logLevel = 0;
    /** The level of debugging options that will be performed.
     */
    public static int debugLevel = 1;
    
    /** TRUE if the origin of a log entry is to be displayed, FALSE otherwise.
     */
    public static boolean showLogOrigin = false;
    /** TRUE if unknown values are valid for predictions on data.
     */
    public static boolean allowUnknownPredictions = true;
    
    /** TRUE if dribble messages should be displayed, FALSE otherwise.
     */
    public static boolean dribble = false;
    
    /** The amount of precision used in performance displays.
     */
    public static int printPerfPrecision;
    
}
