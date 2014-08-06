package arithmetic.chc;

import java.util.Random;

import arithmetic.shared.LogOptions;
  

public class Spawner {

  private int attrselectionsize;
  private LogOptions logOptions = new LogOptions("WRAPPER");
  private Random rand;

  public Spawner(int attrselsize) {
    if (attrselsize < 1) {
      logOptions.ERR(0, "Atribute attrselectionsize cannot be zero.");
    }
    attrselectionsize = attrselsize;
    rand = new Random(System.currentTimeMillis());
  }

  public Hypothesis[] spawnPopulation(int number, int generation) {
    logOptions.LOG(3, "Spawning"+CHC.ENDL);
      Hypothesis[] result = new Hypothesis[number];
      for (int i = 0; i < number; i++) {
        result[i] = newHypo(generation);
      }
    return result;
  }

  public Hypothesis newHypo(int generation) {
      int[] newint = new int[attrselectionsize];
      for (int j = 0; j < attrselectionsize; j++) {
        newint[j] = rand.nextInt(2);
      }
    Hypothesis newhypo = new Hypothesis(generation, newint);
    return newhypo;
  }
    
}
