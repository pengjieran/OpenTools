package arithmetic.chc;

public class TestClean {
  private static Hypothesis[] hypo = new Hypothesis[10]; 
  private static int[] iii = {1,1,1,1,1,1,1,1};
  private static Hypothesis h = new Hypothesis(0, iii);

  public static void main(String[] args) {
    hypo[0] = h;
    for (int i = 1; i < hypo.length; i++) {
      hypo[i] = null;
    }
    hypo = CHC.cleanHypo(hypo);
    System.out.println(hypo.length);
    for (int i = 0; i < hypo.length; i++) {
      hypo[i].displayHypothesis();
    }

  }








}
