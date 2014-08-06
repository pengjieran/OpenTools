package arithmetic.shared;

/** OptionServer serves as an interface between the option mechanism and the
 * physical storage for the options themselves.  Specifically, OptionServer allows
 * options to be retrieved from either the environment, the command line, or any
 * of a number of specially formatted option (configuration) files.
 *
 * @author James Louis Java Implementation
 * @author Dan Sommerfield 2/01/96 Initial revision (.h,.c)
 */
public class OptionServer {
    private boolean useEnv; //true = fall back on env if option mising
    private OptionServer backup; //backup server used for extra options
    //if none found
    private StringMap optionMap;
    
    /** Constructor. The default option state has no special options and will search
     * the environment for options before giving up.
     *
     */
    public OptionServer() {
        useEnv = true;
        backup = null;
        optionMap = new StringMap(512);
    }
    
    /** Sets an option given a textual name and value.  If the option was already set,
     * this replaces the old value.
     * @param optName The option name.
     * @param optVal The option value.
     */
    public void set_option(String optName, String optVal) {
        //use StringMaps's "map" function because we're not sure whether
        //or not this option was previously set
        OptionAccess acc = new OptionAccess();
        acc.value = optVal;
        acc.count = 0;
        optionMap.map(optName, acc);
    }

    /** The main function in this class, this function gets the value of an option given
     * its name (optName). The value is placed in the string optVal. The function
     * returns TRUE if the option is defined, either within the options table or (if
     * useEnv is set) in the environment. If the option is undefined, the function
     * returns FALSE.
     *
     * @param optName The option name.
     * @param optVal The value assigned to this option.
     * @return TRUE if the option is defined, FALSE otherwise.
     */
    public boolean get_option(String optName, String[] optVal) {
        //if we find the option in the table, return immediately
        OptionAccess acc = new OptionAccess();
        if(optionMap.get(optName, acc)) {
            optVal[0] = acc.value;
            return true;
        }
        
        //otherise if we should use the environment, call getenv
        //if getenv returns null, then the option is not set.
        //But if the option is setenv'd to nothing (getenv returns ""),
        //then we'll set optVal to "" and return TRUE.
        else if (useEnv == true) {
            GetEnv environment = new GetEnv();
            String envStr = environment.getenv(optName);
            if (envStr != null) {
                optVal[0] = envStr;
                return true;
            }
        }
        
        //one last-chance, try the backup server, if we have one
        if(backup!=null)
            return backup.get_option(optName, optVal);
        // not found anywhere and no backup - return false
        else
            return false;
    }
    
    
}
