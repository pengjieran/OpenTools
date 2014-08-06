package arithmetic.chc;

import java.io.IOException;

import arithmetic.shared.Inducer;

public class TestTHSS {
  private static int[] data;
  private static Hypothesis one;
  private static Inducer inducer;
  private static DataDistributor dist;
  private static boolean newsystem = false;
  private static THSS thss;
  private static Shuffler shuffle;
  private static Sys sys;
  private static double[] fdist = {.75, .125, .125};

  public static void main(String[] args) throws IOException {
      inducer = CHC.determineInducer(args[0]);
      sys = new Sys("samwise.user.cis.ksu.edu", 2222);
      dist = new DataDistributor(args[1], true);
      Hypothesis.setFDist(fdist);
    if (args.length > 2) {
        data = new int[args[2].length()];
        for (int j = 0; j < args[2].length(); j++) {
          if ( args[2].charAt(j) == '1' ) {
            data[j] = 1;
          }
          else {
            data[j] = 0;
          }  
        }
    }

        Hypothesis one = new Hypothesis(0, data);
        for (int i = 0; i < 2; i++ ) {
          one.nextStage();
          thss = new THSS(sys, one, 
                          inducer.description(), dist.file.getName());
          thss.start();
          while (!one.isFit()) {
            try {
              System.out.println(".");
              Thread.sleep(1000);
            }
            catch (Exception e) {
            }
          }
        }
        one.displayHypothesis();

  }
}
