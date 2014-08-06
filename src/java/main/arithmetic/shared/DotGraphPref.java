package arithmetic.shared;

/** Preferences for DotGraph displays.
 *
 * @author James Louis	1/19/2001	Ported to Java.
 * @author Dave Manley	9/23/93	Initial revision
 */
public class DotGraphPref extends DisplayPref{
    
    
    /** This class has no access to a copy constructor.
     * @param source The object to be copied.
     */
    private DotGraphPref(DotGraphPref source){
        super(DotGraphDisplay);
    }
    
    /** This class has no access to an assign method.
     * @param source The object to be copied.
     */
    private void assign(DotGraphPref source){}
    
    /** Constructor.
     */
    public DotGraphPref() {
        super(DotGraphDisplay);
    }
    
    /** Casts this object to a DotGraphPref.
     * @return Returns a reference to this object.
     */
    public DotGraphPref typecast_to_DotGraph() {
        return this;
    }
    
}