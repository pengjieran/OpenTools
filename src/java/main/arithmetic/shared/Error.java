package arithmetic.shared;
import java.io.BufferedReader;
import java.io.StreamTokenizer;

/** The Error class handles runtime error reporting. This class is not meant to be
 * instantiated and the functions in this class are all static.
 * @author James Louis	5/24/2001       Java Implementation.
 *                     1/04/2002       Created err/out stream option.
 */
public class Error {
    
/*    private static PrintStream output_stream;
    Waiting on GetEnv.getenv(String) to be finished. -JL
    static{
        GetEnv getenv = new GetEnv();
        String stream_option = getenv.getenv("MLJ_ERRORSTREAM");
        if(stream_option.equalsIgnoreCase("ERR")) output_stream = System.err;
        else output_stream = System.out;
    }
 */
    
    /** Constructor. This removes the possibility of instatiating this class with
     * a base constructor.
     */
    private Error(){}
    
    /** Displays an error message.
     * @param errMessage	The message to be displayed.
     */
    public static void err(String errMessage) {
        System.out.println("Error-->"+errMessage);
    }
    
    /** Displays a fatal error message.
     * @param errMessage	The message to be displayed.
     */
    public static void fatalErr(String errMessage) {
        System.out.println("Error-->"+errMessage+"-->fatal_error");
    }
    
    
    //  Description : Report a parse error in an input file, including the line
    //                  number and character position of the error.
    //  Comments    :
    
    /** Displays a parse error message.
     * @param input      Information about where in the files the error occured.
     * @param errorMsg	The message to be displayed.
     */
    public static void parse_error(BufferedReader input, String errorMsg) {
        System.out.println("Parse Error-->"+errorMsg+"-->fatal_error");
        //System.out.prinln("Parse error in " << input.getClass() << " at line "
        //<< input.line_count()
        //<< " and character " << input.pos_in_line() << ":" << endl <<
        //errorMsg << fatal_error;
    }
    
    /** Displays a parse error message.
     * @param input      Information about where in the files the error occured.
     * @param errorMsg   The message to be displayed.
     */
    public static void parse_error(StreamTokenizer input, String errorMsg) {
        System.out.println("Parse Error-->"+errorMsg+"-->fatal_error");
        //System.out.prinln("Parse error in " << input.getClass() << " at line "
        //<< input.line_count()
        //<< " and character " << input.pos_in_line() << ":" << endl <<
        //errorMsg << fatal_error;
    }
    
}
