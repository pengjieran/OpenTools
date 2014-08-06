package arithmetic.chc;

import java.io.IOException;

/** CHCDriver is the class that starts the CHC System. Specific arguments
  * must be passed into the System in order for CHC to work properly.
  * These arguments are documented in a Readme file included with this
  * directory and also pointed out to the user by runtime error feedback.
  * It is possible to run CHC for another module. */
public class CHCDriver {
  /** Constructs CHC passing the command line arguments and then
    * runs the program.  */
  public static void main(String[] args) throws IOException {
    CHC chc = new CHC(args);
    chc.runGA("new");
  }

}
