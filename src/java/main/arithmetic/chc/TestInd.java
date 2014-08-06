package arithmetic.chc;

import java.io.IOException;

import arithmetic.id3.ID3Inducer;

public class TestInd {
  private static int[] data;
  private static Hypothesis one;
  private static ID3Inducer inducer;
  private static DataDistributor dist;
  private static boolean newsystem = false;

  private static Shuffler shuffle;
  public static void main(String[] args) throws IOException {
    inducer = new ID3Inducer("ID3");

    if (args.length > 1) {
        data = new int[args[1].length()];
        for (int j = 0; j < args[1].length(); j++) {
          if ( args[1].charAt(j) == '1' ) {
            data[j] = 1;
          }
          else {
            data[j] = 0;
          }  
        }
    }
    if (args.length > 2) {
      if (args[2].toLowerCase().equals("f")) {
        newsystem = true;
      }
    }

        dist = new DataDistributor(args[0], newsystem);
        Hypothesis one = new Hypothesis(0, data);
        one.setFitness(inducer.project_train_and_perf(dist.getTrainData(), dist.getValidationData(), one.getGeneticMakeup("")).get_metrics());
        one.setRealFitness(inducer.project_train_and_perf(dist.getFinalTrainer(), dist.getTestData(), one.getGeneticMakeup("")).get_metrics());
        one.displayHypothesis();

  }
}
