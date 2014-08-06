package arithmetic.shared;
/** Provides preferences for non-graphical output.
 * @author James Louis 7/19/2001   Ported to Java.
 * @author Dave Manley 9/23/1993   Initial revision.
 */

public class ASCIIPref extends DisplayPref {
    /** This class has no access to a copy constructor.
     * @param source The AsciiPref instance to be copied.
     */
    private ASCIIPref(ASCIIPref source){
        super(ASCIIDisplay);
    }
    
    /** This class has no access to an assign method.
     * @param source The AsciiPref from which information will be copied.
     */
    private void assign(ASCIIPref source){}
    
    /** Constructor.
     */
    public ASCIIPref() {
        super(ASCIIDisplay);
    }
    
    /** Casts this object to an ASCII preference object.
     * @return A reference to this object.
     */
    public ASCIIPref typecast_to_ASCII() {
        return this;
    }
    
};
