package arithmetic.chc;

public class TestMutation {

  public static void main(String[] args) {
    int[] one = { 0,0,0,0,0,0,0,0,0,0,0,0,0 };
    int[] two = { 1,1,1,1,1,1,1,1,1,1,1,1,1 };
    Hypothesis o = new Hypothesis(0, one);
    Hypothesis t = new Hypothesis(0, two);

    Mutator.setCMFactor(.90);
    System.out.println(Mutator.getCMFactor());
    Hypothesis[] newhypo = Mutator.cataclysmicMutation(o, 10);
   
    for (int i=0;i<newhypo.length;i++) {
      newhypo[i].displayHypothesis();
    }

  }





}
