package arithmetic.shared;
/** The DisplayPref class contains information on displaying options for MLJ
 * inducers. There are seven possible preference settings(ASCIIDisplay,
 * DotPostscriptDisplay, DotGraphDisplay, TreeVizDisplay, EviVizDisplay,
 * RuleVizDisplay, and TableVizDisplay).
 *
 * @author James Louis	5/28/2001	Java Implementation and redesigning.
 * @author Dave Manley	9/23/93	Initial revision
 */
public class DisplayPref {
    //DisplayType Enum
    /** The value indicating the ASCIIDisplay preference setting.**/
    static final public int ASCIIDisplay = 0;
    /** The value indicating the DotPostscriptDisplay preference setting.**/
    static final public int DotPostscriptDisplay = 1;
    /** The value indicating the DotGraphDisplay preference setting.**/
    static final public int DotGraphDisplay = 2;
    /** The value indicating the TreeVizDisplay preference setting.**/
    static final public int TreeVizDisplay = 3;
    /** The value indicating the EviVizDisplay preference setting.**/
    static final public int EviVizDisplay = 4;
    /** The value indicating the RuleVizDisplay preference setting.**/
    static final public int RuleVizDisplay = 5;
    /** The value indicating the TableVizDisplay preference setting.**/
    static final public int TableVizDisplay = 6;
    
    //DisplayOrientation Enum
    /** The value indicating the DisplayLandscape orientation setting.**/
    static final public int DisplayLandscape = 0;
    /** The value indicating the DisplayPortrait orientation setting.**/
    static final public int DisplayPortrait = 1;
    
    //DotRatio Enum
    /** The value indicating the RatioFill ratio setting.**/
    static final public int RatioFill = 0;
    /** The value indicating the RatioDefault ratio setting.**/
    static final public int RatioDefault = 1;
    
    /** The display preference for this DisplayPref object.**/
    private int pref;
    /** The x coordinate for the page size for the DotPostscriptPref preference.**/
    private int pageSize_x;
    /** The y coordinate for the page size for the DotPostscriptPref preference.**/
    private int pageSize_y;
    /** The x coordinate for the graph size for the DotPostscriptPref preference.**/
    private int graphSize_x;
    /** The y coordinate for the graph size for the DotPostscriptPref preference.**/
    private int graphSize_y;
    /** The orientation setting for the DotPostscriptPref preference.**/
    private int orientation;
    /** The ratio setting for the DotPostscriptPref preference.**/
    private int ratio;
    /** Indicator for whether the TreeVizPref preference should show backfit disks.**/
    private boolean displayBackfitDisks;
    
    //obs extern ASCIIPref defaultDisplayPref;
    /** The default display preferences. **/
    public static ASCIIPref defaultDisplayPref = new ASCIIPref();
    
    
    /** This class has no access to a copy constructor.
     * @param source The object to be copied.
     */
    private DisplayPref(DisplayPref source){}
    
    /** This class has no access to an assign method.
     * @param source The object to be copied.
     */
    private void assign(DisplayPref source){}
    
    /** Constructor.
     * @param preference	The display preference type. Takes only integers 0-6 where
     * each integer represents an enumerated preference
     * type (ASCIIDisplay,etc).
     */
    public DisplayPref(int preference) {
        pref = preference;
        displayBackfitDisks = false;
    }
    
    /** Returns the preference type.
     * @return The preference type.
     */
    public int preference_type() {
        return pref;
    }
    
    /** Sets a new preference type.
     * @param newpref	The new preference type.
     */
    public void set_preference_type(int newpref) {
        pref = newpref;
    }
    
    /** Converts this object to a string value.
     * @return A string value representing this object.
     */
    public String toString() {
        switch (pref) {
            case  ASCIIDisplay : return "ASCIIDisplay";
            case  DotPostscriptDisplay : return "DotPostscriptDisplay";
            case  DotGraphDisplay : return "DotGraphDisplay";
            case  TreeVizDisplay : return "TreeVizDisplay";
            case  EviVizDisplay : return "EviVizDisplay";
            case  RuleVizDisplay : return "RuleVizDisplay";
            case  TableVizDisplay : return "TableVizDisplay";
            default : return "Unknown Preference Type";
        }
    }
    
    /** Returns the x coordinate for the page size for the DotPostscriptPref
     * setting.
     * @return The x coordinate for the page size for the DotPostscriptPref
     * setting.
     */
    public int get_page_size_x() {
        return pageSize_x;
    }
    
    /** Returns the y coordinate for the page size for the DotPostscriptPref
     * setting.
     * @return The y coordinate for the page size for the DotPostscriptPref
     * setting.
     */
    public int get_page_size_y() {
        return pageSize_y;
    }
    
    /** Returns the x coordinate for the graph size for the DotPostscriptPref
     * setting.
     * @return The x coordinate for the graph size for the DotPostscriptPref
     * setting.
     */
    public int get_graph_size_x() {
        return graphSize_x;
    }
    
    /** Returns the y coordinate for the graph size for the DotPostscriptPref
     * setting.
     * @return The y coordinate for the graph size for the DotPostscriptPref
     * setting.
     */
    public int get_graph_size_y() {
        return graphSize_y;
    }
    
    /** Returns the orientation for the DotPostscriptPref setting.
     * @return The orientation for the DotPostscriptPref setting.
     */
    public int get_orientation() {
        return orientation;
    }
    
    /** Returns the ratio for the DotPostscriptPref setting.
     * @return The ratio for the DotPostscriptPref setting.
     */
    public int get_ratio() {
        return ratio;
    }
    
    /** Sets the page size for the DotPostscriptPref setting.
     * @param newX	The new x coordinate value for the page size.
     * @param newY The new y coordinate value for the page size.
     */
    public void set_page_size(int newX, int newY) {
        pageSize_x = newX;
        pageSize_y = newY;
    }
    
    /** Sets the graph size for the DotPostscriptPref setting.
     * @param newX The new x coordinate value for the page size.
     * @param newY The new y coordinate value for the page size.
     */
    public void set_graph_size(int newX, int newY) {
        graphSize_x = newX;
        graphSize_y = newY;
    }
    
    /** Sets the orientation for the DotPostscriptPref setting.
     * @param newOrientation	The new orientation setting.
     */
    public void set_orientation(int newOrientation) {
        if ((newOrientation > 1)||(newOrientation < 0))
            Error.err("DisplayPref.set_orientation: Not a valid orientation.");
        else orientation = newOrientation;
    }
    
    /** Sets the ratio for the DotPostscriptPref setting.
     * @param newRatio	The new ratio setting.
     */
    public void set_ratio(int newRatio) {
        if ((newRatio > 1)||(newRatio < 0))
            Error.err("DisplayPref.set_ratio: Not a valid ratio.");
        else ratio = newRatio;
    }
    
    /** Returns whether the TreeVizPref setting is set to display bacfitting.
     * @return TRUE if the TreeVizPref setting is set to display backfitting, or
     * FALSE otherwise.
     */
    public boolean get_display_backfit_disks() {
        return displayBackfitDisks;
    }
    
    /** Sets the TreeVizPref setting to display backfitting.
     * @param disp	TRUE if displaying backfitting is requested, FALSE otherwise.
     */
    public void set_display_backfit_disks(boolean disp) {
        displayBackfitDisks = disp;
    }
    
}