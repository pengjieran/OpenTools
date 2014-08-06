package arithmetic.shared;

/** Class structure used for maitaining threshold information in
 * Entropy to make score arrays.
 */
public class ThresholdInfo {
    /** The index of this threshold.
     */
    public int index;
    /** The score value for this particular threshold.
     */
    public double score;
    /** The weight of this particular threshold.
     */
    public double weight;
    /** Constructor.
     */
    public ThresholdInfo(){
        index = Globals.UNDEFINED_INT;
        score = Globals.UNDEFINED_REAL;
        weight = Globals.UNDEFINED_REAL;
    }
}