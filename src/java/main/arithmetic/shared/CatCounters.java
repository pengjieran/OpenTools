package arithmetic.shared;

/** Counters for categories in CatTestResult.
 * @author James Louis Created 2/25/2001
 */
public class CatCounters {
    /** Total weight of instances in test-set in this category. **/
    public double numTestSet;
    
    /** Evaluation metrics for this category. **/
    public ScoringMetrics metrics;
    
    /** Constructor.
     */
    public CatCounters() {
        numTestSet = 0;
        metrics = new ScoringMetrics();
    }
    
}
