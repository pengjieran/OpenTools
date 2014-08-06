package arithmetic.chc;

/** Replaced by the Hypothesis class for chc purposes. */
public class Individual {
  
  private DNA dna;                 //the genetic makeup of this individual
  private int generationnumber;    //the generation which spawned this individual
  


  public Individual(int g, DNA d) {
    generationnumber = g;
    dna = d;
  }

  public Individual(int g) {
    generationnumber = g;
    dna = null;
  }

  public int geneticDifference(Individual ind) {
    return dna.geneticDifference(ind.getDNA());
  }

  public DNA getDNA() {
    return new DNA(dna);
  }

}
