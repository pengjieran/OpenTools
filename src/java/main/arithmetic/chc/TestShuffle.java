package arithmetic.chc;

public class TestShuffle {
  private static Shuffler shuffle;
  public static void main(String[] args) {
    shuffle = new Shuffler(10);
    while(shuffle.ready()) {
      System.out.println(" "+shuffle.selectint()+"");
    }
  }
}
