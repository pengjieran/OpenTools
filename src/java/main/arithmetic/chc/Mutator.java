package arithmetic.chc;

/** Implements cataclysmic mutation on a Hypothesis. 
  * 
  * Cataclysmic Mutation takes a Hypothesis and switches exactly
  * m number of bits. This is needed when no Hypothesis in a
  * generation can breed because no two Hypothesis has a 
  * genetic differenct of more that teh mating threshold. */
public class Mutator {
    /** The number of bits m to changes within each Hypothesis. */
    private static double cmfactor = .35;

  /** Constructor does not need to be called. All function are 
    * static. */
  public Mutator() {}

  public static void setCMFactor(double d) { cmfactor = d; }
  public static double getCMFactor() { return cmfactor; }

  public static Hypothesis[] cataclysmicMutation(Hypothesis[] parent, int mult) {
    if (parent == null) {
      return null;
    }
    else {
    	Hypothesis[] finalhypo = new Hypothesis[parent.length*mult];
    	for (int i = 0; i < parent.length; i++) {
    		finalhypo = CHC.addHypo(finalhypo, cataclysmicMutation(parent[i], mult), 10);
    	}
    	return CHC.cleanHypo(finalhypo); 
    }
  }

  /** This method uses a Hypothesis and creates a number of Hypothesis which
    * is specified by the user. All hypos created this way are mutaions
    * of the original hypo.
    * @param parent - the basis hypothesis for the mutations.
    * @param mult - the number of mutated hypos to create.
    * @return an array of mutated hypos.
    */
  public static Hypothesis[] cataclysmicMutation(Hypothesis parent, int mult) {
    Hypothesis[] result = new Hypothesis[mult];
    for (int j=0;j<mult;j++) {
      int[] dna = parent.getGeneticMakeup();
      try {
        Thread.sleep(10);
      }
      catch (Exception e) {
        System.out.println("Improper conditions: Cannot verify Randomness of Mutation!");
      }
      Shuffler shuffle = new Shuffler(dna.length);
      for (int i=0;i<(parent.length()*cmfactor);i++) {
        int z = shuffle.selectint();
        if (dna[z] == 1) {
          dna[z] = 0;
        }
        else {
          dna[z] = 1;
        }
      }
      result[j]=new Hypothesis(parent.getGeneration(), dna);
    }
    return result;
  }

}
