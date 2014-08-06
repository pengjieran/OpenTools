package arithmetic.chc;

import java.net.Socket;
import java.util.LinkedList;

import arithmetic.shared.Inducer;
import arithmetic.shared.ScoringMetrics;

/** FitnessMachine performs the task of testing Hypothesis. It
  * is optimized for speed since multiple inducer runs can be 
  * time consuming. Original implementation spawned a separate
  * thread for a FitnessMachine but it was decided that this
  * was unnessesary. The structure still exists however. */
public class FitnessMachine {

    private DataDistributor dist;
    private Inducer inducer;

    private LinkedList hypos = new LinkedList();
    private LinkedList rhypos = new LinkedList();
    private boolean go = true;
    private boolean popsleep;
    private boolean sleeping;
    private Population parent;

    private LinkedList thss = new LinkedList();
    private Sys[] sys;
    
//    private LogOptions logOptions = new LogOptions("WRAPPER");

    private int numcalc;
    private int tenths;
    private int numfinished;

  public FitnessMachine(Inducer ind, DataDistributor d) {
    dist = d;
    inducer = ind;
    sys = SysOp.getSys();
    sys = SysOp.trySys(sys);
  }

  public void run() {
    Options.LOG(2, "Fitness Machine running"+CHC.ENDL);
    numcalc = hypos.size() + rhypos.size();
    tenths = numcalc/10;
    numfinished = 0;
    popsleep = false;
    go = true;
    while ( !hyposEmpty() ) {
      calculateFitness();
//      percentDone();
    }

/*
    while ( !hyposEmpty() || !sysClear() ) {
      while ( !hyposEmpty() ) {
        this.calculateFitness();
        percentDone();
      }
      while ( !sysClear() ) {
        try { Thread.sleep(1000); }
        catch (Exception e) {}
        checkTHSS();
      }
    }
    checkTHSS();
*/
  }

  private void percentDone() {
    if (numfinished == numcalc) {
      Options.LOG(2, "done."+CHC.ENDL);
    }
    else if ( (numfinished%tenths) == 0) {
      Options.LOG(2, ((numfinished/tenths)*10) + "%  " );
    }
    else {}
  }

  public synchronized void add(Hypothesis[] h) {
      for(int i = 0; i < h.length; i++) {
        add(h[i]);
      }
  }

  public synchronized void add(Hypothesis h) {
    h.nextStage();
    hypos.add(h);
  }

  public synchronized void calculateFitness() {
      if (hypos.size() > 0) {
        calculateFitness((Hypothesis)hypos.removeFirst());
      }
      else if (rhypos.size() > 0) {
        calculateFitness((Hypothesis)rhypos.removeFirst());
      }
      else {
      }
  }

  private Sys getSys() {
    Sys result = null;
    int count = 0;
    while ( (result == null) && (count < 5) ) {
      for (int i = 0; i < sys.length; i++) {
        if ( !sys[i].isOccupied() && sys[i].checked ) {
          return sys[i];
        }
      }
    }
    return null;
  }

  public void calculateFitness(Hypothesis hypo) {
    if (hypo.stage() == 2) {
      ScoringMetrics temp =
        inducer.project_train_and_perf(dist.getFinalTrainer(),        
        dist.getTestData(), hypo.getGeneticMakeup("")).get_metrics();
      temp.treenodes = inducer.num_nontrivial_nodes();
      temp.treeleaves = inducer.num_nontrivial_leaves();
      hypo.setRealFitness(temp);
    }
    else if (hypo.stage() == 1) {
      ScoringMetrics temp =
        inducer.project_train_and_perf(dist.getTrainData(),        
        dist.getValidationData(), hypo.getGeneticMakeup("")).get_metrics();
      hypo.setFitness(temp);
    }
    numfinished++;

/*
    Sys tempsys = getSys();
    if ( tempsys != null) {
      if ( !hypo.isFit() ) {
        THSS thss = new THSS(tempsys, hypo, inducer.description(),
                             dist.file.getName() );
        thss.setDaemon(true);
        this.thss.add(thss);
        thss.start();
        numfinished++;
      }
    }
*/
  }

  public boolean hyposEmpty() {
    if (hypos.size() == 0 && rhypos.size() == 0) {
      return true;
    }
    else {
      return false;
    }
  }

  public boolean sysClear() {
    for (int i = 0; i < sys.length; i++) {
      if ( sys[i].isOccupied() ) {
        return false;
      }
    }
    return true;
  }

  public static Socket setSocket(Sys s) {
    return s.setSocket();
  }

  private void checkTHSS() {
    int i = thss.size();
    while ( i-- > 0 ) {
      THSS temp = (THSS)(thss.removeFirst());
      if (temp != null) {
        if ( temp.isAlive() ) {
          if( temp.timedOut() ) {
            add(temp.recover());
          }
          else {
            thss.add(temp);
          } 
        }
        else {
        }
      }
      else {
      }
    }
  }
}

/*  this is just a backup area for some functions that got scratched.
 *
    if (hypo.stage() == 2) {
      ScoringMetrics temp =
        inducer.project_train_and_perf(dist.getFinalTrainer(),        
        dist.getTestData(), hypo.getGeneticMakeup("")).get_metrics();
      temp.treenodes = inducer.num_nontrivial_nodes();
      temp.treeleaves = inducer.num_nontrivial_leaves();
      hypo.setRealFitness(temp);
    }
    else if (hypo.stage() == 1) {
      ScoringMetrics temp =
        inducer.project_train_and_perf(dist.getTrainData(),        
        dist.getValidationData(), hypo.getGeneticMakeup("")).get_metrics();
      hypo.setFitness(temp);
    }
*/
