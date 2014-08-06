package arithmetic.shared;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;


/** The MEnum class implements a dynamic enumeration type. This type is implemented
 * using a list of (name, value) pairs which define the enumeration. The same name
 * is assumed not to appear twice in the list.  We do not check for this condition
 * until the destructor because it requires time quadratic on the length of the
 * list to check.
 * @author James Louis Java Implementation.
 * @author Dan Sommerfield 10/26/94 Initial revision (.h,.c)
 *
 */
public class MEnum{
    
    /** Container class for value/string pairs.
     */
    private class MEnumField {
        /** The String name of an Enum value.
         */
        public String name;
        /** The integer value of an Enum value.
         */
        public int value;
        /** Constructor
         */
        public MEnumField() {
            name = "";
            value = -1;
        }
        //Added for MEnum copy constructor.
        /** Copies this MEnumField.
         * @return A copy of this MEnumField.
         */
        public MEnumField copy() {
            MEnumField other = new MEnumField();
            other.name = new String(name);
            other.value = value;
            return other;
        }
    }
    
    /** A list of MEnumFields associated with this MEnum object.
     */
    private Vector fields; //for MEnumField objects
    
    /** Invariant checker. Checks array bounds, and checks for negative values, null
     * names, and duplicate names. This takes O(n^2), so we surrounded it with DBG
     * statements, (in the caller) and call it rarely.
     *
     */
    public void OK() {
        // check items
        for(int i=0; i<fields.size(); i++) {
            MLJ.ASSERT(((MEnumField)fields.get(i)).name != "","MEnum.OK():field["+i+"].name == \"\".");
            MLJ.ASSERT(((MEnumField)fields.get(i)).value >= 0,"MEnum.OK():field["+i+"].value < 0.");
            for(int j=0; j<i; j++)
                if(((MEnumField)fields.get(i)).name.equalsIgnoreCase(((MEnumField)fields.get(j)).name))
                    Error.fatalErr("MEnum::OK: name "+((MEnumField)fields.get(i)).name+" has conflicting "
                    +"values "+((MEnumField)fields.get(j)).value+" and "+((MEnumField)fields.get(i)).value);
        }
    }
    
    /** Constructor.
     */
    public MEnum() {
        fields = new Vector();
    }
    
    /** Constructor which takes a name and a value.  Creates an enumeration with one
     * element.
     * @param name The name of a value to be placed in this MEnum object.
     * @param value The value to be placed in this MEnum object.
     */
    public MEnum(String name, int value) {
        fields = new Vector(1);
        // make sure name is not empty
        if(name == "")
            Error.fatalErr("MEnum::MEnum: name for value "+value+" is empty");
        // make sure value is >= 0
        if(value < 0)
            Error.fatalErr("MEnum::MEnum: value for name "+name+" is < 0");
        // build with name and value.
        ((MEnumField)fields.firstElement()).name = name;
        ((MEnumField)fields.firstElement()).value = value;
    }
    
    /** Copy constructor. We need this or we won't be able to do nice initialization
     * This is also a nice place to put a sanity check (debug mode only). This results
     * in having the sanity check performed once on initialization, which is reasonable.
     * @param other The MEnum object to be copied.
     */
    public MEnum(MEnum other) {
        for(int i = 0; i < other.fields.size(); i++){
            fields.add(((MEnumField)other.fields.get(i)).copy());
            
            if (Globals.DBG){OK();}
        }
    }
    
    /** Appends one enumeration onto another. Returns the resulting combined
     * enumeration so that operations may be chained.
     * @param other The MEnum object to be appended to this MEnum object.
     */
    public void append(MEnum other) {
        fields.addAll(other.fields);
    }
    
    /** Makes this MEnum object equivalent to the given MEnum object.
     * @param src The MEnum object to which this MEnum object will be equivalent.
     */
    public void assign(MEnum src) {fields = src.fields;}
    
    /** Displays the enumeration.  If full is TRUE, displays the values along with the
     * names.
     * @param out The Writer to which this MEnum object will be displayed.
     * @param full TRUE if values are to be displayed with their names, FALSE if only names
     * are to be displayed.
     */
    public void display(Writer out, boolean full) {
        try{
            int i;
            for(i=0; i < fields.size()-1; i++) {
                out.write(((MEnumField)fields.get(i)).name);
                if(full)
                    out.write("="+((MEnumField)fields.get(i)).value);
                out.write(", ");
            }
            out.write(((MEnumField)fields.get(i)).name);
            if(full)
                out.write("="+((MEnumField)fields.get(i)).value);
        }catch(IOException e){e.printStackTrace(); System.exit(1);}
    }
    
    /** Displays the enumeration. Does not display MEnum values.
     * @param out The Writer to which this MEnum object will be displayed.
     */
    public void display(Writer out) {
        try{
            int i;
            for(i=0; i < fields.size()-1; i++) {
                out.write(((MEnumField)fields.get(i)).name);
                out.write(", ");
            }
            out.write(((MEnumField)fields.get(i)).name);
        }catch(IOException e){e.printStackTrace(); System.exit(1);}
    }
    
    /** Finds a value given its name. Returns -1 on failure. We return an error status
     * rather than aborting because the functions which call this are part of the user
     * interface code for the option help system.
     * @param name The name for which a value is requested.
     * @return The value associated with the name of -1 if the name is not found in this
     * MEnum object.
     */
    public int value_from_name(String name) {
        for(int i=0; i<fields.size(); i++) {
            // get an uppercase version of target and source name
            String targetUC = ((MEnumField)fields.get(i)).name.toUpperCase();
            String sourceUC = name.toUpperCase();
            
            // cut target to the size of source
            String targetCutUC = targetUC.substring(
            0, Math.min(targetUC.length(), sourceUC.length()));
            
            // check for equality.  This determines if source is equal to
            // and prefix of target
            if(targetCutUC == sourceUC)
                return ((MEnumField)fields.get(i)).value;
        }
        return -1;
    }
    
    /** Finds the name given a value.  In case we have two names for the same value,
     * returns the first one in the list. If the value is not found, returns "".
     *
     * @see #value_from_name(String)
     * @param value The value for which a name is requested.
     * @return The name associated with this value or an empty string if the value
     * is not found in this MEnum object.
     */
    public String name_from_value(int value) {
        for(int i=0; i<fields.size(); i++)
            if(value == ((MEnumField)fields.get(i)).value)
                return ((MEnumField)fields.get(i)).name;
        return "";
    }
    
    /** Checks if the value is not in this MEnum object.
     * @param value The value to be checked.
     * @return TRUE if the value is associated with a name.
     */
    public boolean check_value(int value) {
        return name_from_value(value) != "";
    }
}