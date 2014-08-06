package arithmetic.chc;


/** Breeder holds all algorithmns necessary for Breeding a population
  * of Hypotheses to produce the next generation. Two Hypotheses are
  * only able to breed if they differ by the threshold. */ 
public class Breeder {
  /** The array the holds the Hypotheses for possible reporduction. */
  private Hypothesis[] stock;
  /** The amount of attributes each Hypothesis pair must differ in
    * order to breed. */
  private int currentthreshhold;
  /** The array which will hold the newly created Hypotheses. */
  private Hypothesis[] resulthypo;
  /** The marker for the position in the resulthypo array currently
    * ready for storage. */
  private int resultmarker;
  /** The array which collects the pairs of Hypothesis ready for breeding. */
  private MatingPair[] pairs = new MatingPair[0];
  /** The marker pointing to the next position in the pairs array. */
  private int pairmarker;
  /** The number of pairs found so far. */
  private int numpairs = 0;

  /** General Constructor.
    * @param pop - an array containing the hypotheses used for breeding. */
  public Breeder(Hypothesis[] pop) {
    stock = pop;
  }

  /** Produces a number of new hypotheses specified. Automatically
    * used cataclysmic mutation if there are to few matingpairs to
    * fill the new hypothesis quota.
    * @param numchildren - the number of new hypotheses to produce.
    * @param thresh - the threshold for breeding two hypothesis.
    * @return an array with the new hypotheses. */
  public Hypothesis[] breed(int numchildren, int thresh) {
    currentthreshhold = thresh;
    resultmarker = 0;
    resulthypo = new Hypothesis[numchildren];
    int i = 2;
    while ( (numpairs <= (numchildren/2)) && i < stock.length ) {
      int[][] factors = findSFactors(i++);
      for (int a = 0; a < factors.length; a++) {
        for (int b = 0; b < factors[a].length; b++) {
        }
      }
      boolean testok = true;
      for (int j = 0; j < factors.length; j++) {
        for (int k = 0; k < factors[j].length; k++) {
          if ( factors[j][k] >= stock.length ) {
            testok = false;
          }
        }
        if (testok) {
          if ( stock[factors[j][0]].geneticDifference(stock[factors[j][1]]) > currentthreshhold ) {
            addMatingPair(new MatingPair(stock[factors[j][0]], stock[factors[j][1]]));
          }
        }
      }
    }
    for (int m = 0; m < pairs.length; m++) {
      if (pairs[m] == null) {
      }
      else {
        addResultHypo(pairs[m].mate());
        addResultHypo(pairs[m].getSister());
      }
    }
    resulthypo = CHC.cleanHypo(resulthypo);
    return resulthypo;
  }

  /** Fuction helps the breed function by adding the new hypotheses
    * to the resulthypo array.
    * @param hypo - the new hypo to add to the array. */
  private void addResultHypo(Hypothesis hypo) {
    if (resultmarker == resulthypo.length) {
    }
    else {
      resulthypo[resultmarker++] = hypo;
    }
  }

  /** adds a matingpair to the array of matingpairs.
    * @param mp - the matingpair to add to the array. */
  private void addMatingPair(MatingPair mp) {
    numpairs++;
    if (pairmarker == pairs.length) {
      MatingPair[] newpairs = new MatingPair[pairmarker + 1];
      for (int i = 0; i < pairmarker; i++) {
        newpairs[i] = pairs[i];
      }
      pairs = newpairs;
    }
    pairs[pairmarker++] = mp;
  }

  /** findSFactors find all two number combinations which will produce
    * the number given including 1 and the actual number. For example,
    * if the number 28 is given an array of
    *       { { 1, 28 }
    *         { 2, 14 }
    *         { 4, 7  ) }
    * will be returned.
    * @param number - the number to be factored
    * @return the array of all "special" factors */
  public static int[][] findSFactors(int number) {
    int[][] result = new int[0][0];
    for ( int i = 1; i < (int)Math.sqrt(number) + 1; i++ ) {
      if ( (number % i) == 0 ) {
        int[][] temp = new int[result.length + 1][2];
        for (int j = 0; j < result.length; j++) {
          temp[j] = result[j];
        }
        temp[(temp.length - 1)][0] = i;
        temp[(temp.length - 1)][1] = number/i;
        result = temp; 
      }
    }
    return result;
  }

  /** This method is an optional replacemet for findSFactors. Instead
    * of finding factors it finds addative numbers, or numbers which 
    * add to equal the given number. If the number 28 were given an
    * array of
    *       { { 0, 28 }
    *         { 1, 27 }
    *         { 2, 26 }
    *           . . .
    *         { 14, 14 } }
    * would be returned. notice { 14, 14 } is also returned.
    * @param number - an int for which all addative number will be returned.
    * @return an array with all the addative numbers for the given number. */
  public static int[][] findSAddatives(int number) {
    int[][] result = new int[number/2 + 1][2];
    for ( int i = 0; i < (int)number/2 + 1; i++ ) {
        result[i][0] = i; 
        result[i][1] = number - i;
    }
    return result;
  }
}
