package arithmetic.shared;

/** Internal structure for the InsanceList class. Used for taking samples from the
 * list.
 * @see InstanceList
 */
public class InstSampleElement {   // used for sample_with_replacement--should not be
    // global, but needed for template instantiation
    /** This class has no access to a copy constructor.
     * @param source The InstSampleElement to be copied.
     */
    private InstSampleElement(InstSampleElement source){}
    
    /** This class has no access to an assign method.
     * @param source The InstSampleElement to be copied.
     */
    private void assign(InstSampleElement source){}
    
    // Public member data
    /** The Instance object held in this InstSample.
     */
    public Instance pix;

    /** TRUE if used in a sample, FALSE otherwise.
     */
    public boolean flag;
    
    /** Constructor.
     */
    InstSampleElement() {
        pix = null;
        flag = false;
    }
}
