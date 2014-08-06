package arithmetic.chc;

import arithmetic.shared.ScoringMetrics;

/** Class Hypothesis holds the bit string representing the
  * features to be used when running an inducer with a
  * Data set. */
public class Hypothesis {
  /** This is the distribution for the weighted fitness function
    * used on inner loop fitness only. */
  private static double[] fdist;  
  /** Used to determine how many decimals to print when printing
    * numbers. */
  private static int formatlevel = 4;

  private int[] geneticmakeup;
  private int generationnumber;
  /** the generation in which this Hypothesis was spawned. */
  private int spawnedgeneration;
  
//  private double fitness = -.1;
//  private double realfitness = -.1;
  /** the inner loop fitness of this hypothesis is stored inside. */
  private ScoringMetrics metrics = null;
  /** the outer loop fintess of this hypothesis is stored inside. */
  private ScoringMetrics realmetrics = null;
  /** the stage of testing this hypothesis is currently in. 
    * 0 none;
    * 1 inner loop testing;
    * 2 outer loop testing; */
  private int stage;

  /** Takes a beginning generation and 
    * genetic material and creates a hypothesis. 
    * @param g - the current generation.
    * @param d - the genetics of this hypothesis. */ 
  public Hypothesis(int g, int[] d) {
    generationnumber = g;
    spawnedgeneration = g;
    geneticmakeup = d;
  }

  /** Only accepts a beginning generation number. The 
    * genetic material must be filled in later.
    * @param g - the current generation. */ 
  public Hypothesis(int g) {
    generationnumber = g;
    geneticmakeup = null;
  }

  /** Copy Constructor.
    * @param h - the hypothesis to copy. */
  public Hypothesis(Hypothesis h) {
    generationnumber = h.getGeneration();
    spawnedgeneration = h.getGeneration();
    geneticmakeup = h.getGeneticMakeup();
  }

  /** Finds the number of bits that are different between this
    * hypothesis and the hypothesis provided. Used to determine
    * if breeding these two Hypothesis would be beneficial.
    * @param ind - the hypothesis for which comparison will be
    *            made.
    * @return the number of bits that are different. */
  public int geneticDifference(Hypothesis ind) {
    int result = 0;
    if (geneticmakeup.length == ind.length()) {
      for (int i = 0; i < geneticmakeup.length; i++) {
        if (getChromosome(i) != ind.getChromosome(i)) {
          result++;
        }
      }
    }
    else {
      System.out.println("Error in Hypothesis.geneticDifference: Incompatible geneticmakeup present.");
    }
    return result;
  }

  /** Gets a copy of the geneticmakeup of this hypothesis.
    * Must copy becuase changes should not be made to the
    * genetic material after the hypothesis is created.
    * @return an int array with a copy of the genetic makeup. */
  public int[] getGeneticMakeup() {
    int[] result = new int[geneticmakeup.length];
    for ( int i=0; i<result.length; i++) {
      result[i] = geneticmakeup[i];
    }
    return result;
  }

  /** Same as getGeneticMakeup() but returns an array of
    * booleans. Included for the Inducer project function.
    * @param str - value not important. Just tells the class
    *              to use this function.
    * @return the boolean array representing the genetic makeup. */
  public boolean[] getGeneticMakeup(String str) {
    boolean[] result = new boolean[geneticmakeup.length];
    for ( int i=0; i<result.length; i++) {
      if (geneticmakeup[i] == 1) {
        result[i] = true;
      }
      else {
        result[i]= false;
      }
    }
    return result;
  }

  /** Gets the length of the geneticmakeup.
    * @return the length of the geneticmakeup array. */
  public int length() {
    return geneticmakeup.length;
  }

  /** Counts how many genes in the geneticmakeup are turned
    * on (1).
    * @return the number of '1' bits in the geneticmakeup. */
  public int getSize() {
    int result = 0;
    for (int i = 0; i < geneticmakeup.length; i++ ) {
      result += geneticmakeup[i];
    }
    return result;
  }

  /** Sets the outerloop fitness of this hypothesis.
    * @param fit - a ScoringMetrics from an outerloop
    *               inducer. */
  public void setRealFitness(ScoringMetrics fit) {
    if (realmetrics == null) {
      realmetrics = fit;
    }
  }

  public void nextGeneration() { generationnumber++; }

  /** Gets the inner loop fitness. Uses the fitnes distribution array
    * fdist[] to determine the actual fitness.
    * @return the inner loop fitness of this hypothesis. */
  public double getFitness() { 
    if ( metrics == null ) {
      return -1;
    }
    else {
      return ( (metrics.weightIncorrect / metrics.totalWeight()) * fdist[0] ) +
             ( (metrics.treenodes * fdist[1]) / length() ) +
             ( (getSize() * fdist[2] ) / length() );
    }
  }

  /** Gets the outer loop fitness for this hypothesis. Outer loop
    * does not use the fitness distribution array like the inner
    * loop fitness does.
    * @return the outer loop fitness. */
  public double getRealFitness() {
    if ( realmetrics == null ) {
      return -1;
    }
    else {
      return realmetrics.weightIncorrect / realmetrics.totalWeight();
    }
  }

  /** Gets the last generation this hypothesis was active in chc.
  * @return the generation number. */
  public int getGeneration() { return generationnumber; }

  /** Gets the generation this hypothesis was spawned.
    * @return the spawned generation. */
  public int getSpawnedGeneration() { return spawnedgeneration; }

  /** Gets a specific chromosome out of the geneticmakeup array.
    * @param index - the index of the chromosome to get.
    * @return the value of the chromosome. */
  public int getChromosome(int index) {
    return geneticmakeup[index];
  }

  public int stage() { return stage; }

  public void setFitness(ScoringMetrics fit) {
    if (stage == 0) { //error
    }
    if (stage == 1 && metrics == null) {
      metrics = fit;
    }
    if (stage == 2 && realmetrics == null) {
      realmetrics = fit;
    }
  }

  public boolean isFit() {
    if (stage < 1) {
      return true;
    }
    else if (stage == 1) {
      return (metrics != null);
    }
    else if (stage == 2) {
      return ( (metrics != null) && (realmetrics != null) );
    }
    else {
      return false;
    }
  }

  public void nextStage() {
    boolean success; //if the stage increase is successful
    if (stage < 1) {
      stage = 1;
      success = true;
    }
    else if (stage == 1) {
      if (isFit()) {    
        stage = 2;
        success = true;
      }
      else {
        success = false;
      }
    }
    else if (stage == 2) {
      success = false;
    }
  }

  public void displayHypothesis() { displayHypothesis("- ","\n"); }
  public void displayHypothesis(String pre, String post) {
    System.out.print(pre);
    for (int i = 0; i < geneticmakeup.length; i++) {
      System.out.print(geneticmakeup[i]);
    }
    System.out.print("  Fitness: "  );
    printDouble(getFitness(), formatlevel);
    if (realmetrics != null) {
      System.out.print("  RealFitness: ");
      printDouble(getRealFitness(), formatlevel);
    }
    System.out.print("  Spawned: "+spawnedgeneration+post);
  }

  public String getHypothesisString() { return getHypothesisString("- ", "\n"); }
  public String getHypothesisString(String pre, String post) {
    String result = "";
    result += pre;
    for (int i = 0; i < geneticmakeup.length; i++) {
      result += geneticmakeup[i];
    }
    if (metrics == null) {
      result += " Not Tested";
    }
    else {
      result += " Fitness: ";
      result += new Double(formatDouble(getFitness(), formatlevel)).toString();
      result += " [" + metrics.numIncorrect + "/" + metrics.totalInstances() + "]";
    }
    if ( getRealFitness() >= 0) {
      result += " Real: ";
      result += new Double(formatDouble(getRealFitness(), formatlevel)).toString();
      result += " [" + realmetrics.numIncorrect + "/" + realmetrics.totalInstances() + "]";
    }
    result += "  Spawned: "+spawnedgeneration+post;
    return result;
  }

  
  /** prints an double right alligned in the number of spaces given by level.
    * level must be greater than the physical length of the double op.
    * @param op - the int to be printed.
    * @param level - the number of spaces to be used.
    * @param space - a string printed in front of the int. */
  public static void printDouble(double op, int level) {
    boolean negative = op < 0;
    int place=1;
    for (int i=0; i<level;i++) {
      place = place*10;
    }
    int tmp = (int)(op*place);
    if (negative) {
      System.out.print("-");
    }
    System.out.print(".");
    printInt(Math.abs(tmp), level, "0");
  }

  /** automatically calls printInt(int, int, String) adding a default " "
    * for the String param.
    * @param op - the int to be printed.
    * @param level - level the number of spaces to be used. */
  public static void printInt(int op, int level) { printInt(op, level, " "); }

  /** prints an int right alligned in the number of spaces given by level.
    * level must be greater than the physical length of the int op.
    * @param op - the int to be printed.
    * @param level - the number of spaces to be used.
    * @param space - a string printed in front of the int. */
  public static void printInt(int op, int level, String space) {
    boolean negative = op < 0;
    int place = 10;
    int i = 1;
    boolean snap = false;
    while ( !snap ) {
      if ( place > Math.abs(op) ) {
        snap = true;
      }
      else {
        place = place * 10;
        i++;
      }
    }
    for (int j = i; j < level; j++) {
      System.out.print(space);
    }
    System.out.print(op);
  }

  /** formats a double to only display the given number of decimals.
    * No rounding occurs is this proceedure.
    * @param op - the double to format.
    * @param level - the number of decimals.
    * @return the formatted double. */
  public static double formatDouble(double op, int level) {
    int place = 1;
    for (int i=0; i<level;i++) {
      place = place*10;
    }
    int fit = (int)(op*place);
    double result = (double)fit / place;
    return result;
  }

  /** Sets the num of decimals to be displayed with the formatDouble method.
    * @param num - the number of decimals to display. */
  public static void setFormat(int num) {
    formatlevel = num;
  }

  /** Sets the weigthed distribution for determining fitness.
    *
    *     a(inducer fitness) + b(treesize)/n + c(attribute size)/n
    *
    * where n is the total number of attributes and a + b + c = 1.0.
    * @param d - an array[3] with the three distributions. */
  public static void setFDist(double[] d) {
    fdist = d;
  }

  /** returns an exact copy of this Hypothesis
    * @return the copy of this Hypothesis */
  public Hypothesis copy() {
    return new Hypothesis(this);
  }
  /** Yes, this method tests to see if the hypo given in the arguments
    * is in fact the very same memory addressed hypo as this hypo. It
    * works exactly like it looks.
    * @param hypo - the hypo to be compared with this hypo.
    * @return true if the hypo is exactly this hypo, false else. */
  public boolean is (Hypothesis hypo) {
    if (this == hypo) {
      return true;
    }
    else {
      return false;
    }
  }

  /** This method compares the genetic makeup of the hypo given in the 
    * arguments to the makeup of this hypo for equality.
    * @param hypo - the hypo for which the comparison is to be made.
    * @return true if the genetic makeups are the same, false else. */
  public boolean equals(Hypothesis hypo) {
    if (this.geneticDifference(hypo) == 0 ) {
      return true;
    }
    else {
      return false;
    }
  }

}
