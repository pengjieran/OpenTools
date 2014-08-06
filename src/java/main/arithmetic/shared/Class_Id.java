package arithmetic.shared;
/** This class contains the reference numbers for different inducers and
 * classes.
 * @deprecated Use Java's instanceof operator.
 * @author James Louis	7/6/2001	Ported to Java.
 */
public class Class_Id {
    /** Defined base class IDs for class hierarchies. Class IDs for actual
     * classes are offsets from these. **/
    public static int class_id_h = 1;
    /** The base ID number for categorizers. **/
    public static int CATEGORIZER_ID_BASE =       0;
    /** The base ID number for inducers. **/
    public static int INDUCER_ID_BASE     =    1000;
    /** The base ID number for state information classes. **/
    public static int STATE_INFO_ID_BASE  =    2000;
    /** The base ID number for split scoring classes. **/
    public static int SPLIT_SCORE_ID_BASE =    2100;
    /** The base ID number for static data classes. **/
    public static int STAT_DATA_ID_BASE   =    2200;
    /** The base ID number for discretizers. **/
    public static int DISCRETIZOR_ID_BASE =    2300;
    /** The base ID number for hash tables. **/
    public static int HASH_TABLE_ID_BASE  =    2400;
    /** The base ID number for centroid classes. **/
    public static int CENTROID_ID_BASE    =    3000;
    
    
    /** Define the base class ID for IDs which are automatically generated
     * by the factory when no ID is provided. This value must be
     * significantly larger than other ID bases. **/
    public static int AUTO_ID_BASE        =   10000;
    
    
}
