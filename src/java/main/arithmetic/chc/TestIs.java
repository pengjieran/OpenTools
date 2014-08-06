package arithmetic.chc;

public class TestIs {

  public static int[] one = { 1, 1, 1, 1, 1 };
  public static int[] two = { 0, 0, 1, 1, 1 }; 

  public static void main(String[] args) {
    Hypothesis o = new Hypothesis(0, one);
    Hypothesis t = new Hypothesis(0, two);
    Hypothesis three = new Hypothesis(0, one);

    if ( o.is(three) ) {
      System.out.println("IS");
    }
    else {
      System.out.println("IS NOT");
    }
    if ( o.equals(three) ) {
      System.out.println("Equals");
    }
    else {
      System.out.println("Not Equal");
    }
  }

}
