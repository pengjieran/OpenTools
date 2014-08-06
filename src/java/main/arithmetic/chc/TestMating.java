package arithmetic.chc;

public class TestMating {

  public static Hypothesis one;
  public static Hypothesis two;
  public static Hypothesis thr;
  public static Hypothesis fou;
  public static Hypothesis fiv;
  public static Hypothesis six;
  public static Hypothesis sev;
  public static Hypothesis eig;


  public static void main(String[] args) {
    int[] o={1,1,1,1,1,1,1,1};
    int[] t={0,0,0,0,0,0,0,0};
    int[] i={1,0,1,0,1,0,1,0};
    one = new Hypothesis(1, o);
    two = new Hypothesis(1, t);
    MatingPair mp = new MatingPair(one, two);
    thr = mp.mate();
    thr.displayHypothesis();
    fou = mp.getSister();
    fou.displayHypothesis();
    MatingPair mp2 = new MatingPair(one, two);
    thr = mp.mate();
    thr.displayHypothesis();
    fou = mp.getSister();
    fou.displayHypothesis();
/*
    mp = new MatingPair(three, four);
    MatingPair mp2 = new MatingPair(three, one);
    Hypothesis five = mp.mate();
    five.displayHypothesis();
    Hypothesis six = mp.getSister();
    six.displayHypothesis();
    Hypothesis seven = mp2.mate();
    seven.displayHypothesis();
    Hypothesis eight = mp2.getSister();
    eight.displayHypothesis();
*/


  }
  
  private static void display() {
    
  }

}
