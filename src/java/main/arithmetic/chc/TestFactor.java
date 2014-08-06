package arithmetic.chc;

public class TestFactor {

  public static void main(String[] args) {
    int i = Integer.valueOf(args[0]).intValue();
    
    int[][] j = Breeder.findSFactors(i);
    int[][] k = Breeder.findSAddatives(i);
    System.out.println("findSFactors() returned:");
    for (int m = 0; m < j.length; m++) {
      for (int n = 0; n < j[m].length; n++) {
        System.out.print(j[m][n] + " ");
      }
      System.out.println();
    }
    System.out.println("findSAddatives() returned:");
    for (int m = 0; m < k.length; m++) {
      for (int n = 0; n < k[m].length; n++) {
        System.out.print(k[m][n] + " ");
      }
       System.out.println();
    }
  }

}

