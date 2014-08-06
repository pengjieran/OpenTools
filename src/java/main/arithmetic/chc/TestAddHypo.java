package arithmetic.chc;

public class TestAddHypo {

  private static int[][] dat = { {0, 0, 0, 0, 0},
                                 {0, 0, 1, 1, 0},
                                 {1, 1, 1, 1, 1} };
  private static Hypothesis one;
  private static Hypothesis two;
  private static Hypothesis three;
  public static void main (String[] args) {
    one = new Hypothesis(0, dat[0]);
    two = new Hypothesis(0, dat[1]);
    three = new Hypothesis(0, dat[2]);
    Hypothesis[] temphypo = new Hypothesis[4];
    temphypo[0] = one;
    temphypo[1] = two;

    temphypo = CHC.addHypo(temphypo, three, 10);
    temphypo = CHC.cleanHypo(temphypo);
    System.out.println(Population.getHypothesisString(temphypo, 0));

  }



}
