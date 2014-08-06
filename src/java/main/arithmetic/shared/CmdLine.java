package arithmetic.shared;
/** This class contains standard command-line processing for MLC++ utilities.
 * @author James Louis 2/21/2002 Ported to Java
 * @author Dan Sommerfield 2/06/96 Initial revision (.h,.c)
 */
public class CmdLine {

    /** Creates a new instance of CmdLine */
    public CmdLine() {
    }

    /** Process all options on the command line for a standard MLC++ utility.  Th
     * options supported and their semantics are as follows:                        <P>
     * -o filename
     * -----------
     * reads a filename containing lines of the form OPTION=VALUE Each option/value
     * pair should be on a separate line. All option/value pairs are added to the list
     * of available options.                                                        <P>
     * -O OPTION=VALUE
     * ---------------
     * Sets a specific option to a specific value.
     * -s
     * --
     * Suppress the use of the enviornment to fill in the values of unset options.  <P>
     *
     * The return value from this function is the index in args of the first nonoption
     * argument.                                                                    <P>
     *
     * Comments:                                                                   <BR>
     * process_mlc_cmdline processes all options on the command line and will produce
     * an error if it finds an unsupported option.                                 <BR>
     * This function resets the values of a few global variables which are set by so-
     * called "super-nuisance" options at startup time.  The values are reset using
     * values from the option server.
     * @param args String array containing command line arguments.
     * @return The index of the first non-option argument in the String array of arguments.
     */    
    public static int process_mlc_cmdline(String[] args) {
        int SGI_optind;
        
        int getoptReply;
        SGI_optind = 1;
        
        for(int i =0; i < args.length; i++){
            if(args[i] == "-o"){
                    if((++i > args.length)||(args[i].startsWith("-")))
                        Error.fatalErr("process_mlc_cmdline: NULL argument after -o "
                        +"in command line");
                    GetEnv getEnv = new GetEnv(args[i]);
                    break;
                }
/*                
                case "-O": {
                    if((++i > args.length)||(args[i].startsWith("-")))
                        Error.fatalErr("process_mlc_cmdline: NULL argument after -O "
                        +"in command line");
                    optionServer->set_option(optStr);
                    break;
                }
                
                case "-s":
                    optionServer->enable_environment(false);
                    break;
*/                    
            else{
                    Error.fatalErr("process_mlc_cmdline: illegal option.  Usage: "
                    +"u_inducer"+" {[-s] [-o optionfile] [-O option=value]}");
            }
            
        }
//        while (getoptReply != EOF);
        
        // reset supernuisance options; The class GlobalOptions helps us
        // with this task through the reinit() function.
//        GlobalOptions::reinit();
        
        // get a supernuisance option for whether or not we should warn
        // users about unused options in the server
//        OptionServer::displayUnused = get_option_bool("SHOW_UNUSED_OPTIONS",
//        FALSE, "", TRUE, TRUE);
        return SGI_optind;
        
    }
    
}
