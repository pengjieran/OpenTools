package arithmetic.chc;

/** An early implementation of a Hypothesis. Was discarded when
  * Hypothesis was created before any real implementation of 
  * chc was complete. */
public class DNA {
 
  private int[] geneticmakeup;

  public DNA(DNA d) {
    geneticmakeup = d.getGeneticMakeup();
  }

  public int[] getGeneticMakeup() {
    return copyGeneticMakeup();
  }

  public int[] copyGeneticMakeup() {
    int[] result = null;
    if (geneticmakeup == null) {
      System.out.println("ERROR in DNA.copyGeneticMakeup(): geneticmakeup cannot be copied.");
    }
    else {
      result = new int[geneticmakeup.length];
      for (int i = 0; i < geneticmakeup.length; i++) {
        if (geneticmakeup == null || geneticmakeup[i] < 0) {
          System.out.println("ERROR in DNA.copyGeneticMakeup(): improper index at geneticmakeup[" + i + "].");
        }
        else {
          result[i] = geneticmakeup[i];
        }
      }
    }
    return result;
  }

  public int geneticDifference(DNA test) {
    int result = 0;
    if (geneticmakeup.length == test.length()) {
      for (int i = 0; i < geneticmakeup.length; i++) {
        if (getChromosome(i) != test.getChromosome(i)) {
          result++;
        }
      }
    }
    else {
      System.out.println("Error in DNA.geneticDifference: No DNA data present.");
    }
    return result;
  }

  public int getChromosome(int index) {
    return geneticmakeup[index];
  }

  public int length() {
    return geneticmakeup.length;
  }

}
