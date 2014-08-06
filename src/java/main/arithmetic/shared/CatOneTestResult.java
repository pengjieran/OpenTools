package arithmetic.shared;

/** This object contains the result information on one instance passed through
 * an inducer.
 * @author James Louis 12/08/2000  Ported to Java.
 */
public class CatOneTestResult {
    /** The instance whose information is stored in this object.**/
    public Instance instance;
    /** The information on the category of the instance associated with this
        object. **/
    public AugCategory augCat;
    /** The correct category for the instance. **/
    public int correctCat;
    /** The predicted classification distribution. **/
    public CatDist predDist;
    /** The correct classification distribution. **/
    public CatDist correctDist;
    /** Indicator for whether this instance is part of a training instance
        list. **/
    public boolean inTrainIL;
    
    /** Constructor.
     */
    public CatOneTestResult() {
        instance = null;
        augCat = null;
        correctCat = Globals.UNDEFINED_INT;
        predDist = null;
        correctDist = null;
        inTrainIL = false;
    }
}
