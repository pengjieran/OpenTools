package arithmetic.shared;
import java.util.ListIterator;

/** Abstract template for incremental inducers.
 * @author James Louis java Implementation
 */
abstract public class IncrInducer extends Inducer { // ABC
    
    /** This class has no access to a copy constructor.
     * @param source  */
    private IncrInducer(IncrInducer source){super("");}
    
    /** This class has no access to an assign method.
     * @param source  */
    private void assign(IncrInducer source){}
    
    /** Constructor.
     * @param description Description of this incremental inducer.
     */
    public   IncrInducer(String description){super(description);}
    
    /** Add an instance of data to this inducer.
     * @param instance The instance to be added.
     * @return The iterator of instances after the addition of the instance.
     */
    abstract public ListIterator add_instance(Instance instance);
    /** Deletes the instance of data in the current position of the list iterator.
     * @param pix The iterator of the list of instances.
     * @return The instance deleted.
     */
    abstract public Instance del_instance(ListIterator pix);
}
