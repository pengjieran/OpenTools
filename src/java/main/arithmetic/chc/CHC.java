package arithmetic.chc;

import java.io.IOException;

import arithmetic.id3.ID3Inducer;
import arithmetic.nb.NaiveBayesInd;
import arithmetic.shared.Inducer;
import arithmetic.shared.LogOptions;

/** CHC is a genetic algorithm used with mlj inducers to find the 
  * best possible combination of attributes for testing data samples.
  * The combinations, called hypothesis, are intialized randomly for
  * the first generation and crossed in a logical order for future
  * generations. The process of crossing two hypothesis is called
  * breeding. Inducer algorithms from MLJ are then used with the
  * hypothesis combinations to determine the fitness of the given
  * hypothesis. The hypothses with the highest fitness
  * survive to the next generation where they will breed to
  * produce more combinations of hypotheses.
  *   The process repeats itself for a designated number of
  * generations with a designated population size. */
public class CHC {
  /** The inducer used to test hypothesis. */ 
  private Inducer inducer;
  /** The object that keeps the instances used for training and testing. */
  private DataDistributor dist;
  /** The population where all the hypothesis are stored, bred and tested. */ 
  private Population currentpopulation;
  /** used with dist. The new system of data distribution is used if
    * this is true. */
  //private boolean newsystem = false;

  /** Stores the newline character "\n" */
  public static final String ENDL = "\n";
  public static final String NAIVE = "naive";
  public static final String ID3 = "id3";
  public static final String C45 = "c45";
  public static final int DEFAULTMAXPOP = 10;
  public static final int DEFAULTMINPOP = 2;
  public static final boolean DEFAULTUSEPOPBOUNDARIES = false;
  public static final String GENERATIONPRINTING = "generation";
  public static final String TOTALPRINTING = "total";

  //private boolean usepopboundaries;
  //private int minpopulation = 2;  

  Options options = null;  

  /** Takes the argument string array and breaks it into the options
    * which govern the operations of chc.
    * @param args - the String array containing the options for chc. */
  public CHC(String[] args) throws IOException {
    options = new Options();

    //This section sorts through the command line arguments and pulls out
    //only valuable information typing invaluable information does not
    //necessarily crash the program. Any information obtained here automatically
    //overides information extracted from the MLJ-Options.file.
    for (int i=1; i<args.length;i++) {
      if (args[i].length() >= 8 ) {
        if (args[i].substring(0,8).toLowerCase().equals("inducer=")) {
            try {
              options.optioninducer = args[i].substring(8, args[i].length());
              System.out.println(options.optioninducer);
            }
            catch (Exception e) {
            }
        }
      }
      if (args[i].length() >= 16 ) {
        if (args[i].substring(0,16).toLowerCase().equals("finalgeneration=")) {
          int xyz = 0;
          for (int k=16;k<args[i].length();k++) {
            try {
              xyz = xyz*10 + Integer.valueOf(args[i].substring(k,k+1)).intValue();
            }
            catch (Exception e) {
            }
          }
          options.finalgeneration = xyz;
        }
      }
      if (args[i].length() >= 15) {
        if (args[i].substring(0,15).toLowerCase().equals("populationsize=")) {
          int xyz = 0;
          for (int k=15;k<args[i].length();k++) {
            try {
              xyz = xyz*10 + Integer.valueOf(args[i].substring(k,k+1)).intValue();
            }
            catch (Exception e) {
            }
          }
          options.populationsize=xyz;
        }
      }
      if (args[i].toLowerCase().equals("f")) {
        //newsystem = true;
      }
      if (args[i].length() >= 8) {
        if (args[i].substring(0,8).toLowerCase().equals("bitmask=")) {
          options.setBitMask(args[i].substring(8));
          System.out.println("Setting bitmask");
        }
      }
    }

    inducer = CHC.determineInducer(options.optioninducer);
    options.setDistributor(args[0], true);
    System.out.println(options.getBitMaskDisplayString());
    Options.LOG(3, "   Population Size = "+options.populationsize+"."+CHC.ENDL);
    Options.LOG(3, "   Number of Attributes = "+options.getDist().getattrselectionsize()+"."+CHC.ENDL);
    Options.LOG(3, "   Final Generation = "+options.finalgeneration+"."+CHC.ENDL);
    Options.LOG(3, "   Option Inducer = "+options.optioninducer+"."+CHC.ENDL);
  }

  /** This method determines which inducer will be used and prepares it
    * for testing data samples. Soft means the program will not be
    * terminated if the inducer is not found.
    * @param ind - a String of the name of the inducer.
    * @return the inducer to use for this chc run. */
  public static Inducer determineInducerSoft(String ind) {
    Inducer result = null;
    if (ind.toLowerCase ().equals (CHC.ID3)) {
      result = new ID3Inducer(CHC.ID3);
    }
    else if (ind.toLowerCase().equals(CHC.NAIVE)) {
      result = new NaiveBayesInd(CHC.NAIVE);
    }
    else if (ind.toLowerCase().equals(CHC.C45)) {
      result = new ID3Inducer(CHC.C45);
      ((ID3Inducer)result).prune(true);
    }
    else {
      result = null;
    }
    return result;
  }

  /** Same as determineInducerSoft except if the inducer is not
    * found the error is treated as fatal and the program exits.
    * @param ind - the string name of the inducer.
    * @return the inducer to use for this chc run. */
  public static Inducer determineInducer(String ind) {
    Inducer result = null;
    result = determineInducerSoft(ind);
    if (result == null) {
      LogOptions.GLOBLOG(0, "Cannot find Inducer " + ind+CHC.ENDL);
      LogOptions.GLOBLOG(0, "Possible options are: "+ CHC.ID3 + ", " + CHC.NAIVE + ", " + CHC.C45 + CHC.ENDL); 
      LogOptions.GLOBLOG(0, "Exit status 1"+CHC.ENDL);
      System.exit(1);
    }
    return result;
  }

  /** getAttrSelectionSize return the number of attributes all instances
    * of this data run will contain.
    * @return the attribute selection size which is equal to the number
    *         of attributes in this data set. */ 
  public int getAttrSelectionSize() { return dist.getattrselectionsize(); }
//  public int getCurrentTheshhold() { return currentpopulation2.getCurrentThreshhold(); }

  /** runGA starts the CHC process. The Population class holds all hypothesis
    * information and has all the algorithms needed to complete a generation.
    * This method tells it to start and also tells it to print results.
    * @param type - a string which specifies which version of population
    *               to use. The old type is now obsolete but hasn't been 
    *               removed yet. */
  public void runGA(String type) {
    if (type.equals("old")) {
    }
    else if (type.equals("new")) {
      currentpopulation = new Population(inducer, options);
      currentpopulation.spawnPopulation();
      while (currentpopulation.getGenerationNumber() <= options.finalgeneration) {
        currentpopulation.nextGeneration();
      }
      if (options.getMask("print_all_hypothesis")) {
        System.out.println("All hypothesis of this run");
        currentpopulation.displayPopulation(CHC.TOTALPRINTING);
      }
      currentpopulation.displayFinalHypothesisFitness(options.getMask("best_overall_hypotheses"));
      currentpopulation.displayAverageFitnessPerGeneration(options.getMask("average_fitness_per_generation"));
      currentpopulation.displayBestSpawnedOfGeneration(options.getMask("best_Spawned_Hypothesis_of_generation"));
    }
  }

  /** This method sorts an array of hypotheses by their fitness in
    * Descending fitness order. This means the lowest fitness is first
    * in the array and the highest fitness is last.
    * @param hypo - the array of Hypotheses to sort.
    * @return the sorted array in Descending fitness order. */
  public static Hypothesis[] sortDescendingFitness(Hypothesis[] hypo) {
    int indexoflowest = 0;
    double lowestvalue;
    if (hypo == null) {
      LogOptions.GLOBLOG(0, "Invalid Array argument for Population2.sortDescendingFitness()"+CHC.ENDL);
    }
    else {
      for (int i=0; i<hypo.length; i++) {
        if (hypo[i] == null) {
        }
        else {
          lowestvalue = hypo[i].getFitness();
          indexoflowest = i;
          for (int j = i; j<hypo.length; j++) {
            if (hypo[j] == null ) {
  	    }
            else {
              if (hypo[j].getFitness() < lowestvalue) {
                lowestvalue = hypo[j].getFitness();
                indexoflowest = j;
              }
            }
          }
        }
        Hypothesis temp = hypo[i];
        hypo[i] = hypo[indexoflowest];
        hypo[indexoflowest] = temp;
      }
    }
    return hypo;
  }

  /** addHypo takes an array of Hypothesis and adds to it another
    * array of Hypothesis returning a single array. The Hypothesis
    * are inserted in the first available slot and if none available
    * the array will be enlarged by increment.
    * @param hypoarray - the array to be increased
    * @param addition - the array to be added to hypoarray
    * @param increment - the size the array will be increased if necessary
    * @return an array with the added hypos */ 
  public static Hypothesis[] addHypo(Hypothesis[] hypoarray,
                             Hypothesis[] addition, int increment) {
    if (increment < 1) {
      increment = 1;
    }
    if (addition == null) {
      return hypoarray;
    }
    else {
      for (int i = 0; i < addition.length; i++) {
        if (addition[i] != null) {
          hypoarray = CHC.addHypo(hypoarray, addition[i], increment);
        }
      }
      return hypoarray;
    }
  }

  /** addHypo takes an array of Hypothesis and adds to it a single
    * Hypothesis returning a single array. The Hypothesis is inserted
    * in the first available slot and if none available the array
    * will be enlarged by increment.
    * @param hypoarray - the array to be increased
    * @param addition - the Hypothesis to be added to hypoarray
    * @param increment - the size the array will be increased if necessary
    * @return an array with the added hypo */ 
  public static Hypothesis[] addHypo(Hypothesis[] hypoarray,
                            Hypothesis addition, int increment) {
    //boolean hasnulls = false;
    int firstnull = -1;
      if ( increment < 1 ) { increment = 1; }
    if (hypoarray == null) {
      Hypothesis[] result = new Hypothesis[increment];
      result[0] = addition;
      return result;
    }
    else {
      for (int i = 0; i < hypoarray.length; i++) {
        if ( (hypoarray[i] == null) && (firstnull < 0) ) {
          firstnull = i;
        }
      }
        if (firstnull >= 0) {
          hypoarray[firstnull] = addition;
          return hypoarray;
        }
        else {
          Options.LOG(7, "Increasing Hypo array size" + CHC.ENDL);
          Hypothesis[] temparray = new Hypothesis[hypoarray.length + increment];
          for (int j = 0; j < hypoarray.length; j++) {
            temparray[j] = hypoarray[j];
          }
          temparray[hypoarray.length] = addition;
          return temparray;
        }
    }
  }

  /** addString takes an array of Strings and adds to it a single
    * String returning a single array. The String is inserted in 
    * the first available slot and if none available the array
    * will be enlarged by increment.
    * @param strarray - the array to be increased
    * @param addition - the Hypothesis to be added to hypoarray
    * @param increment - the size the array will be increased if necessary
    * @return an array with the added hypo */ 
  public static String[] addString(String[] strarray,
                                 String addition, int increment) {
    //boolean hasnulls = false;
    int firstnull = -1;
      if ( increment < 1) { increment = 1; }
    if (strarray == null) {
      String[] result = new String[increment];
      result[0] = addition;
      return result;
    }
    else {
      for (int i = 0; i < strarray.length; i++) {
        if ( (strarray[i] == null) && (firstnull < 0) ) {
          firstnull = i;
        }
      }
        if (firstnull >= 0) {
          strarray[firstnull] = addition;
          return strarray;
        }
        else {
          String[] temparray = new String[strarray.length + increment];
          for (int j = 0; j < strarray.length; j++) {
            temparray[j] = strarray[j];
          }
          temparray[strarray.length] = addition;
          return temparray;
        }
    }
  }

  /** cleanHypo removes all null values within the given hypo so future method 
    * calls will not result in an accidental NullPointerExceptionError.
    * @param hypo - the Hypothesis[] to be cleaned
    * @return an array with the same information but no null values */
  public static Hypothesis[] cleanHypo(Hypothesis[] hypo) {
    int count = 0;
    if (hypo == null) {
      return new Hypothesis[0];
    }
    else {
      for ( int i = 0; i < hypo.length; i++) {
        if (hypo[i] == null) {
        }
        else {
          count++;
        }
      }
      int marker = 0;
      Hypothesis[] temphypo = new Hypothesis[count];
      for ( int i = 0; i < hypo.length; i++) {
        if (hypo[i] == null) {
        }
        else {
          temphypo[marker++] = hypo[i];
        }
      }
      return temphypo;
    }
  }

  /** this methods prepares an array of Hypos for the next stage of testing.
    * @param h - an array of hypos. */
  public static void prepForTest(Hypothesis[] h) {
    if (h != null) {
      for (int i = 0; i < h.length; i++) {
        h[i].nextStage();
      }
    }
  }

  /** Because I can never remember the order of this manuver I
   * went ahead and wrote it down in a single method call. */
  public static int sTI(String str) {
    return Integer.valueOf(str).intValue();  
  }
  /** Same as above. */
  public static double sTD(String str) {
    return Double.valueOf(str).doubleValue();  
  }
  /** Same as above. */
  public static boolean sTB(String str) {
    return Boolean.valueOf(str).booleanValue();
  }
  /** Same as above. */
  public static String iTS(int i) {
    return String.valueOf(i);
  }
  /** Same as above. */
  public static String dTS(double d) {
     return String.valueOf(d);
  }
  /** Same as above. */
  public static String bTS(boolean b) {
     return String.valueOf(b);
  }
}
