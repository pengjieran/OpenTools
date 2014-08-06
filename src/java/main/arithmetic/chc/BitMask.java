package arithmetic.chc;

import java.util.StringTokenizer;

import arithmetic.shared.GetEnv;

/** BitMask holds the values used for the CHC Printing
  * options section in the MLJ-Options.file and the bitmask=#######
  * specified on the command line. the masks are called by name. the
  * value of each mask name is stored in a corresponding array.
  * true means the mask is on false means off.
  */
public class BitMask {
    /** hard coded names of the current mask options cancatenated 
      * in one string.
      */
    public static final String maskdefinitions = "print_all_hypothesis:" +
       "best_overall_hypotheses:" + "average_fitness_per_generation:" +
       "best_Spawned_Hypothesis_of_generation:" +
       "best_overall_Hypothesis_of_generation:" +
       "best_fitness_of_each_generation:" + "correct_vs_incorrect_instances:";

    /** holds the individual names of the mask options in an array of
      * Strings taken from the hard coded names.
      */
    private String[] definition;
    /** holds the values (true of false) of the mask options in an array. */
    private boolean[] mask;
    /** the size of the mask arrays. */
    private int size = 0;

  /** Constructor. BitMask must be constructed to be used. */
  public BitMask() {
    StringTokenizer token = new StringTokenizer(maskdefinitions, ":");
    size = token.countTokens();
    definition = new String[size];
    mask = new boolean[size];
    GetEnv getenv = new GetEnv();
    int i = 0;
    while (token.hasMoreTokens()) {
      definition[i] = token.nextToken();
      try {
        mask[i] = getenv.get_option_string(definition[i]+":").equals("yes")?true:false;
      }
      catch (Exception e) {
        mask[i] = false;
      }
      i++;
    }
  }

  /** gets the size of the bitmask arrays.
    * @return the size of the arrays.
    */
  public int size() {
      return size;
  }

  /** changes the value of the mask named s to the value m.
    * Error if no mask s is found.
    * @param s - the name of the mask to change.
    * @param m - the value to set mask s.
    */
  public void changeMask(String s, boolean m) {
      int ind;
      if ( (ind = maskIndex(s)) != -1 ) {
        mask[ind] = m;
      }
      else {
        Options.LOG(0, "No " + s + " mask found");
      }
  }
  
  /** returns true if the specified mask is a valid mask name.
    * @param def - the name of the mask to check.
    * @retrun true if def is a valid mask name; false else.
    */
  public boolean isMask(String def) {
      for (int i = 0; i < size; i++) {
        if (def.toLowerCase().equals(definition[i])) {
          return true;
        }
      }
      return false;
  }

  /** returns the index of the mask named s. -1 if no mask is
    * found.
    * @param s - the name of the mask to find.
    * @return the index of the mask named s.
    */
  public int maskIndex(String s) {
      for (int i = 0; i <size; i++) {
        if (s.toLowerCase().equals(definition[i])) {
          return i;
        }
      }
      return -1;
  }

  /** returns the value of the mask named by the specified
    * String.
    * @param def - the name of the mask to find.
    * @return the value (true or false) of the mask named def.
    */
  public boolean getMask(String def) {
      for (int i = 0; i < size; i++) {
        if (def.equals(definition[i])) {
          return mask[i];
        }
      }
      return false;
  }

  /** this method is used for displaying all mask values.
    * @return a String with all mask values preformatted.
    */
  public String getDisplayString() {
    String result = "";
    for (int i = 0; i < size; i++) {
      result += mask[i];
      result += CHC.ENDL;
    }
    return result;
  }

  /** used to set all mask values with a string of 7 bits.
    * each bit in the string will set the corresponding mask.
    * to true if the bit is 1. everything else is considered 0
    * and will set the mask to false.
    */
  public void setBitMask(String m) {
    if (m.length() == size) {
      for (int i = 0; i < size; i++) {
        if (String.valueOf(m.charAt(i)).equals("1")) {
          mask[i] = true;
        }
        else {
          mask[i] = false;
        }
      }
    }
    else {
      Options.LOG(0, "Given bitmask is incorrect length");
    }
  }
}
