package arithmetic.chc;

public class Test {
  public static void main(String[] args) {
    int [] one = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
    Hypothesis hypo1 = new Hypothesis(1,one);

    Hypothesis hypo2 = hypo1;
    Hypothesis hypo3 = new Hypothesis(1,one);

    if (hypo1 == hypo2) {
      System.out.println("yeah babe");
    }
    if (hypo1==hypo3) {
      System.out.println("AHHHHHHHHHHHHHHHHHHH");
    }
  }
}
