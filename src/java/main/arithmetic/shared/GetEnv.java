package arithmetic.shared;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/** This class replaces the functions of GetOption class. It provides access to
 * the MLJ-Options.file for loading parameters and options for MLJ.
 * @author James Louis Added several functions and comments.
 * @author James Louis 1/14/2002 Changes to locate MLJ-Options file automatically
 * from java classpath information.
 */
public class GetEnv {
    /** The name of the MLJ-Options.file.
     */
    private String OptionsFile = "MLJ-Options.file";
    
    /** The name of the MLJ-Options.file and path for use if not present in the
     * working directory. **/
    private String SecondaryOptionsFile = "shared/MLJ-Options.file";
    
    /** The File instance containing information of the MLJ-Options.file. **/
    static File optfile;
    
    /** Constructor. Looks for the MLJ-Options.file in the working directory first and
     * in the MLJ shared source directory second.
     */
    public GetEnv(){
        if(optfile != null) return;
        optfile = new File(OptionsFile);
        
        /*This peice of code is now obsolete. -JL 01/14/2002
        if (!optfile.isFile() ) {
            optfile = new File(SecondaryOptionsFile);
        }*/
        
        String class_path = System.getProperty("java.class.path");
        String file_separator = System.getProperty("file.separator");
        
        // Checks for the MLJ-Options file in the working directory. -JL
        File working_dir_file = new File("."+file_separator+OptionsFile);
        if(working_dir_file.exists()){
            optfile = working_dir_file;
            return;
        }
        
        // Checks for the MLJ-Options file in the MLJ shared source directory.
        StringTokenizer tokenizer = new StringTokenizer(class_path,System.getProperty("path.separator"));
        while(tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            if (token.endsWith("shared"))
                optfile = new File(token+file_separator+OptionsFile);
            if(optfile.exists()) break;
        }
    }
    
    /** Constructor. Looks for the options file with the given name in the working
     * directory first and in the MLJ shared source directory second.
     * @param fileName The name of the file containing option values.
     */
    public GetEnv(String fileName) {
        if(optfile != null) return;
        optfile = new File(fileName);
        String class_path = System.getProperty("java.class.path");
        String file_separator = System.getProperty("file.separator");
        
        // Checks for the MLJ-Options file in the working directory. -JL
        File working_dir_file = new File("."+file_separator+OptionsFile);
        if(working_dir_file.exists()){
            optfile = working_dir_file;
            return;
        }
        
        // Checks for the MLJ-Options file in the MLJ shared source directory.
        StringTokenizer tokenizer = new StringTokenizer(class_path,System.getProperty("path.separator"));
        while(tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            if (token.endsWith("shared"))
                optfile = new File(token+file_separator+OptionsFile);
            if(optfile.exists()) break;
        }
    }
    
    /** A dummy function that returns null when called.
     * @return Always null.
     * @param in Name of the environment variable requested.
     */
    public String getenv(String in){return null;}
    
    /** Returns the value of an integer option of the specified name.
     * @param option The name of the option for which a value is requested.
     * @return The value of the option if found, 0 otherwise.
     */
    public int get_option_int(String option) {
        BufferedReader file;
        try{
            file = new BufferedReader(new FileReader(optfile));
            
            try{
                while(file.ready()) {
                    String line = file.readLine();
                    if (line == null) break;	//Change made for the JDK1.3 -JL
                    StringTokenizer tokens = new StringTokenizer(line," ");
                    while(tokens.hasMoreTokens())
                        if(tokens.nextToken().equals(option))
                            return new Integer(tokens.nextToken()).intValue();
                }
                file.close();
            }catch(IOException e)
            {  Error.err("Could not open Options File!"); }
            
        }catch(FileNotFoundException e)
        {   Error.err("Options File " +
            OptionsFile+" not found!");
        }
        return 0;  //error if reached here!
    }
    
    /** A function which returns the string value following the option name within the
     * options file.
     * @param option The name of the option for which a value is requested.
     * @return The value of the requested option.
     */
    public String get_option_string(String option) {
        BufferedReader file;
        try{
            file = new BufferedReader(new FileReader(optfile));
            
            try{
                while(file.ready()) {
                    String line = file.readLine();
                    if (line == null) break;        //Change made for the JDK1.3
                    StringTokenizer tokens = new StringTokenizer(line," ");
                    while(tokens.hasMoreTokens())
                        if(tokens.nextToken().equals(option))
                            return tokens.nextToken();
                }
                file.close();
            }catch(IOException e)
            {  Error.err("Could not open Options File!"); }
            
        }catch(FileNotFoundException e)
        {   Error.err("Options File " +
            OptionsFile+" not found!");
        }
        return null;  //error if reached here!
    }
    
    /** A function which returns the string value following the option name within the
     * options file.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @return The value for the requested option.
     */
    public String get_option_string(String optionName, String defaultValue, String optionHelp, boolean nuisance) {
        return get_option_string(optionName,defaultValue,optionHelp,nuisance,false);
    }
    
    
    /** Returns the max range of a real value option of the specified name.
     * @param option The name of the option for which a value is requested.
     * @return The value of the option if found, 0 otherwise.
     */
    public double get_option_real_range(String option) {
        BufferedReader file;
        try{
            file = new BufferedReader(new FileReader(optfile));
            
            try{
                while(file.ready()) {
                    String line = file.readLine();
                    if (line == null) break;	//Change made for the JDK1.3 -JL
                    StringTokenizer tokens = new StringTokenizer(line," ");
                    while(tokens.hasMoreTokens())
                        if(tokens.nextToken().equals(option))
                            return new Double(tokens.nextToken()).doubleValue();
                }
                file.close();
            }catch(IOException e)
            {  Error.err("FATAL ERROR - Could not open Options File!"); }
            
        }catch(FileNotFoundException e)
        {   Error.err("FATAL ERROR - Options File " +
            OptionsFile+" not found!");
        }
        return 0;  //error if reached here!
    }
    
    /** Returns the value of an boolean option of the specified name.
     * @param option The name of the option for which a value is requested.
     * @return The value of the option if found, false otherwise.
     */
    public boolean get_option_bool(String option) {
        BufferedReader file;
        try{
            file = new BufferedReader(new FileReader(optfile));
            
            try{
                while(file.ready()) {
                    String line = file.readLine();
                    if (line == null) break;	//Change made for the JDK1.3 -JL
                    StringTokenizer tokens = new StringTokenizer(line," ");
                    while(tokens.hasMoreTokens())
                        if(tokens.nextToken().equals(option))
                            if(new String(tokens.nextToken()).trim().equals("true"))
                                return true;
                            else return false;
                }
                file.close();
            }catch(IOException e)
            {  Error.err("FATAL ERROR - Could not open Options File!"); }
            
        }catch(FileNotFoundException e)
        {   Error.err("FATAL ERROR - Options File " +
            OptionsFile+" not found!");
        }
        return false;  //error if reached here!
    }
    
    
    /** Returns the value of an boolean option of the specified name.
     * @param option The name of the option for which a value is requested.
     * @param default_value The value of the option if not found in the options file.
     * @param nuisance The String description for a help display.
     * @param nuisance_flag Flag for nuisance value use.
     * @return The value of the option if found, the default_value otherwise.
     */
    public boolean get_option_bool(String option, boolean default_value,
    String nuisance, boolean nuisance_flag) {
        BufferedReader file;
        try{
            file = new BufferedReader(new FileReader(optfile));
            
            try{
                while(file.ready()) {
                    String line = file.readLine();
                    if (line == null) break;	//Change made for the JDK1.3 -JL
                    StringTokenizer tokens = new StringTokenizer(line," ");
                    while(tokens.hasMoreTokens())
                        if(tokens.nextToken().equals(option))
                            if(new String(tokens.nextToken()).trim().equals("true"))
                                return true;
                            else return false;
                }
                file.close();
            }catch(IOException e)
            {  Error.err("FATAL ERROR - Could not open Options File!"); }
            
        }catch(FileNotFoundException e)
        {   Error.err("FATAL ERROR - Options File " +
            OptionsFile+" not found!");
        }
        return default_value;
    }
    
/*    public double get_option_real(String optionName, double defaultValue,
    String optionHelp, boolean nuisance, boolean noPrompt){
        BufferedReader file;
        try{
            file = new BufferedReader(new FileReader(optfile));
 
            try{
                while(file.ready()) {
                    String line = file.readLine();
                    if (line == null) break;	//Change made for the JDK1.3 -JL
                    StringTokenizer tokens = new StringTokenizer(line," ");
                    while(tokens.hasMoreTokens())
                        if(tokens.nextToken().equals(option))
                            return new Integer(tokens.nextToken()).intValue();
                }
                file.close();
            }catch(IOException e)
            {  Error.err("Could not open Options File!"); }
 
        }catch(FileNotFoundException e)
        {   Error.err("Options File " +
            OptionsFile+" not found!");
        }
        return 0;  //error if reached here!
    }
 */
    /** Reads the value of an option as an Enum object.
     * @param optionName The name of the option for which a value is requested.
     * @param optionMEnum The MEnum object that will hold the option values.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @param returnVal The values for the requested option.
     */
    public void get_option_enum_no_default(String optionName,
    MEnum optionMEnum,
    String optionHelp,
    boolean nuisance,
    int returnVal){
        returnVal =  _get_option_enum(optionName, optionMEnum, optionHelp, nuisance);
    }
    
    /** Reads the value of an option as an Enum object.
     * @param optionName The name of the option for which a value is requested.
     * @param optionMEnum The MEnum object that will hold the option values.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @return The value for the requested option.
     */
    public int _get_option_enum(String optionName,
    MEnum optionMEnum,
    String optionHelp,
    boolean nuisance) {
        // grab the value out of get_option
        // Try to lookup the string in the enum value.
        //        String val = MEnumOption(optionName, optionMEnum, "", optionHelp, nuisance, false).get(false);
        String val = MEnumOption(optionName, optionMEnum, "", optionHelp, nuisance, false);
        int enumValue = optionMEnum.value_from_name(val);
        //ASSERT(enumValue >= 0);
        return enumValue;
    }
    
    
    /** Reads the value of an option as an MEnum object.
     * @param optionName The name of the option for which a value is requested.
     * @param optionEnum The MEnum object that will hold the option values.
     * @param deflt The default value for the option if it is not found in the option file.
     * @param help Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @param noPrompt TRUE if prompting always occurs regardless of wether option is set, FALSE
     * otherwise.
     * @return The value for the requested option.
     */
    public String MEnumOption(String optionName, MEnum optionEnum,
    String deflt, String help, boolean nuisance, boolean noPrompt){
        String value = deflt;
        value = get_option_string(optionName);
        return value;
    }
    
    /** Returns the value of an string option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param optionHelp Help display string for using this option.
     * @return The value for the requested option.
     */
    public String get_option_string_no_default(String optionName, String optionHelp) {
        return get_option_string(optionName);
    }
    
    /** Returns the value of an boolean option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param optionHelp Help display string for using this option.
     * @return The value for the requested option.
     */
    public boolean get_option_bool(String optionName, boolean defaultValue, String optionHelp) {
        return get_option_bool(optionName,defaultValue,optionHelp,false);
    }
    
    /** Returns the value of an integer option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @param noPrompt TRUE if prompting always occurs regardless of wether option is set, FALSE
     * otherwise.
     * @return The value for the requested option.
     */
    public int get_option_int(String optionName, int defaultValue, String optionHelp, boolean nuisance, boolean noPrompt) {
        BufferedReader file;
        try{
            file = new BufferedReader(new FileReader(optfile));
            
            try{
                while(file.ready()) {
                    String line = file.readLine();
                    if (line == null) break;	//Change made for the JDK1.3 -JL
                    StringTokenizer tokens = new StringTokenizer(line," ");
                    while(tokens.hasMoreTokens())
                        if(tokens.nextToken().equals(optionName))
                            return new Integer(tokens.nextToken()).intValue();
                }
                file.close();
            }catch(IOException e)
            {  Error.err("Could not open Options File!"); }
            
        }catch(FileNotFoundException e)
        {   Error.err("Options File " +
            OptionsFile+" not found!");
        }
        return defaultValue;  //error if reached here!
    }
    
    /** Returns the value of an integer option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param optionHelp Help display string for using this option.
     * @return The value for the requested option.
     */
    public int get_option_int(String optionName, int defaultValue, String optionHelp) {
        return get_option_int(optionName,defaultValue,optionHelp,false,false);
    }
    
    /** Returns the value of an integer option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @return The value for the requested option.
     */
    public int get_option_int(String optionName, int defaultValue) {
        return get_option_int(optionName,defaultValue,"",false,false);
    }
    
    /** Returns the value of an integer option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @return The value for the requested option.
     */
    public int get_option_int(String optionName, int defaultValue, String optionHelp, boolean nuisance) {
        return get_option_int(optionName,defaultValue,optionHelp,nuisance,false);
    }
    
    /** Returns the value of an string option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @return The value for the requested option.
     */
    public String get_option_string_no_default(String optionName, String optionHelp, boolean nuisance) {
        return get_option_string(optionName);
    }
    
    /** Returns the value of an string option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @param noPrompt TRUE if prompting always occurs regardless of wether option is set, FALSE
     * otherwise.
     * @return The value for the requested option.
     */
    public String get_option_string(String optionName, String defaultValue, String optionHelp, boolean nuisance, boolean noPrompt) {
        String value = get_option_string(optionName);
        if(value == null) value = defaultValue;
        return value;
    }
    
    /** Returns the value of an integer option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param optionHelp Help display string for using this option.
     * @return The value for the requested option.
     */
    public int get_option_int(String optionName, String optionHelp) {
        return get_option_int(optionName);
    }
    
    /** Returns the value of an integer option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @return The value for the requested option.
     */
    public int get_option_int(String optionName, String optionHelp, boolean nuisance) {
        return get_option_int(optionName);
    }
    
    /** Returns the value of an integer option of the specified name between the given
     * bounds.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param lowerBound The lower bound for possible option values.
     * @param upperBound The upper bound for possible values.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @param noPrompt TRUE if prompting always occurs regardless of wether option is set, FALSE
     * otherwise.
     * @return The value for the requested option.
     */
    public int get_option_int_range(String optionName, int defaultValue, int lowerBound, int upperBound, String optionHelp, boolean nuisance, boolean noPrompt) {
        int i = get_option_int(optionName,defaultValue,optionHelp,nuisance,noPrompt);
        return i;
    }
    
    /** Returns the value of an integer option of the specified name between the given
     * bounds.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param lowerBound The lower bound for possible option values.
     * @param upperBound The upper bound for possible values.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @return The value for the requested option.
     */
    public int get_option_int_range(String optionName, int defaultValue, int lowerBound, int upperBound, String optionHelp, boolean nuisance) {
        return get_option_int_range(optionName,defaultValue,lowerBound,upperBound,optionHelp,nuisance,false);
    }
    
    /** Returns the value of an integer option of the specified name between the given
     * bounds.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param lowerBound The lower bound for possible option values.
     * @param upperBound The upper bound for possible values.
     * @param optionHelp Help display string for using this option.
     * @return The value for the requested option.
     */
    public int get_option_int_range(String optionName, int defaultValue, int lowerBound, int upperBound, String optionHelp) {
        return get_option_int_range(optionName,defaultValue,lowerBound,upperBound,optionHelp,false,false);
    }
    
    /** Returns the value of an integer option of the specified name between the given
     * bounds.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param lowerBound The lower bound for possible option values.
     * @param upperBound The upper bound for possible values.
     * @return The value for the requested option.
     */
    public int get_option_int_range(String optionName, int defaultValue, int lowerBound, int upperBound) {
        return get_option_int_range(optionName,defaultValue,lowerBound,upperBound,"",false,false);
    }
    
    /** Returns the value of an integer option of the specified name between the given
     * bounds.
     * @param optionName The name of the option for which a value is requested.
     * @param lowerBound The lower bound for possible option values.
     * @param upperBound The upper bound for possible values.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @return The value for the requested option.
     */
    public int get_option_int_range(String optionName, int lowerBound, int upperBound, String optionHelp, boolean nuisance) {
        return get_option_int(optionName);
    }
    
    /** Returns the value of an double option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @param noPrompt TRUE if prompting always occurs regardless of wether option is set, FALSE
     * otherwise.
     * @return The value for the requested option.
     */
    public double get_option_real(String optionName, double defaultValue, String optionHelp, boolean nuisance, boolean noPrompt) {
        BufferedReader file;
        try{
            file = new BufferedReader(new FileReader(optfile));
            
            try{
                while(file.ready()) {
                    String line = file.readLine();
                    if (line == null) break;	//Change made for the JDK1.3 -JL
                    StringTokenizer tokens = new StringTokenizer(line," ");
                    while(tokens.hasMoreTokens())
                        if(tokens.nextToken().equals(optionName))
                            return new Double(tokens.nextToken()).doubleValue();
                }
                file.close();
            }catch(IOException e)
            {  Error.err("FATAL ERROR - Could not open Options File!"); }
            
        }catch(FileNotFoundException e)
        {   Error.err("FATAL ERROR - Options File " +
            OptionsFile+" not found!");
        }
        return defaultValue;
    }
    
    /** Returns the value of an double option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @return The value for the requested option.
     */
    public double get_option_real(String optionName, double defaultValue, String optionHelp, boolean nuisance) {
        return get_option_real(optionName,defaultValue,optionHelp,nuisance,false);
    }
    
    /** Returns the value of an double option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @param optionHelp Help display string for using this option.
     * @return The value for the requested option.
     */
    public double get_option_real(String optionName, double defaultValue, String optionHelp) {
        return get_option_real(optionName,defaultValue,optionHelp,false,false);
    }
    
    /** Returns the value of an double option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param defaultValue The default value for the option if it is not found in the option file.
     * @return The value for the requested option.
     */
    public double get_option_real(String optionName, double defaultValue) {
        return get_option_real(optionName,defaultValue,"",false,false);
    }
    
    /** Returns the value of an double option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param optionHelp Help display string for using this option.
     * @param nuisance TRUE if prompting is required when option is not set, FALSE otherwise.
     * @return The value for the requested option.
     */
    public double get_option_real(String optionName, String optionHelp, boolean nuisance) {
        return get_option_real(optionName,0,optionHelp,nuisance,false);
    }
    
    /** Returns the value of an double option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @param optionHelp Help display string for using this option.
     * @return The value for the requested option.
     */
    public double get_option_real(String optionName, String optionHelp) {
        return get_option_real(optionName,0,optionHelp,false,false);
    }
    
    /** Returns the value of an double option of the specified name.
     * @param optionName The name of the option for which a value is requested.
     * @return The value for the requested option.
     */
    public double get_option_real(String optionName) {
        return get_option_real(optionName,0,"",false,false);
    }
    
}
