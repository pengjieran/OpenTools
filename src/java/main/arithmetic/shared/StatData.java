package arithmetic.shared;
import java.io.IOException;
import java.io.Writer;
import java.util.ListIterator;
import java.util.Vector;

/** StatData is a class for doing statistical computations.
 * @author James Louis 4/11/2002 Ported to Java.
 * @author Yeogirl Yun 12/12/94 Added percentile method.
 * @author James Dougherty and Ronny Kohavi 5/23/94 Initial revision (.h,.c)
 *
 */
public class StatData{
    
    private Vector items;
    
    /** Class id value.
     * @deprecated Use Java's instanceOf() function instead of class_id().
     */
    public static final int STAT_DATA_ID_BASE = 2200;
    /** Class id value.
     * @deprecated Use Java's instanceOf() function instead of class_id().
     */
    public static final int CLASS_STAT_DATA = STAT_DATA_ID_BASE + 1;
    /** Class id value.
     * @deprecated Use Java's instanceOf() function instead of class_id().
     */
    public static final int CLASS_ACC_DATA  = STAT_DATA_ID_BASE + 2;
    /** Class id value.
     * @deprecated Use Java's instanceOf() function instead of class_id().
     */
    public static final int CLASS_ERROR_DATA= STAT_DATA_ID_BASE + 3;
    
    /** This class has no access to a copy constructor.
     */
    private StatData(StatData source){}
    
    /** Constructor.
     */
    public StatData() {
        items = new Vector();
    }
    
    /** Returns the number of the items stored in this StatData.
     * @return The number of items.
     */
    public int size(){ return items.size(); }
    
    /** Inserts an item into the Array. If an item is inserted into the array and there
     * is no more space available, we fall back on the Array's error checking for
     * operator [].
     *
     * @param item Item to be inserted into the Array.
     */
    public void insert(double item) {
        DoubleRef theItem = new DoubleRef(item);
        items.addElement(theItem);
    }
    
    /** Returns the element at the specified index. Replaces the [] operator.
     * @return The element at the given index.
     * @param indx The index of the element requested.
     */
    public double index(int indx) {
        if (indx > size())
            Error.fatalErr("Error: StatData::operator[]: index "+indx
            +" is out of bound");
        return ((DoubleRef)items.get(indx)).value;
    }
    
    /** Calculates the variance of the data.
     *
     * @param trim Sets the value at which the variance will be trimmed to.
     * @return The trimmed variance.
     */
    public double variance(double trim){
        check_trim(trim);
        if (size() < 2)
            return Globals.UNDEFINED_VARIANCE;
        
        double xBar = mean(trim);       // sorted now.
        int low = (int)(size()*trim); // starting at 0
        int high = size() - low;
        
        double result = low * (MLJ.square_real(((DoubleRef)items.get(low)).value - xBar) +
        MLJ.square_real(((DoubleRef)items.get(high-1)).value - xBar));
        
        for(int k = low; k < high; k++)
            result += MLJ.square_real(((DoubleRef)items.get(k)).value - xBar);
        
        return result / (MLJ.square_real(1 - 2*trim)*(size() - 1));
    }
    
    /** Calculates the variance of the data. Trim defaults to 0.
     *
     * @return The variance value.
     */
    public double variance(){return variance(0);}
    
    /** Calculates the standard-deviation of the data.
     * @param trim Sets the value at which the standard-deviation will be trimmed to.
     * @return The trimmed standard-deviation.
     */
    public double std_dev(double trim) {
        double var = variance(trim);
        if(var == Globals.UNDEFINED_VARIANCE)
            return Globals.UNDEFINED_VARIANCE;
        return Math.sqrt(var);
    }
    
    /** Calculates the standard-deviation of the data. Trim defaults to 0.
     * @return The trimmed standard-deviation.
     */
    public double std_dev(){return std_dev(0);}
    
    
    /** Calculates the variance of the mean of the data.
     * @param trim Sets the value at which the variance will be trimmed to.
     * @return The trimmed variance of the mean.
     */
    public double variance_of_mean(double trim) {
        double var = variance(trim);
        if(var == Globals.UNDEFINED_VARIANCE)
            return Globals.UNDEFINED_VARIANCE;
        return var / size();
    }
    
    /** Calculates the variance of the mean of the data. Trim defaults to 0.
     * @return The trimmed variance of the mean.
     */
    public double variance_of_mean(){return variance_of_mean(0);}
    
    /** Calculates the standard-deviation of the mean of the data.
     * @param trim Sets the value at which the standard-deviation will be trimmed to.
     * @return The trimmed standard-deviation of the mean.
     */
    public double std_dev_of_mean(double trim) {
        double var = variance_of_mean(trim);
        if(var == Globals.UNDEFINED_VARIANCE)
            return Globals.UNDEFINED_VARIANCE;
        return Math.sqrt(var);
    }
    
    /** Calculates the standard-deviation of the mean of the data. Trim defaults to 0.
     * @return The trimmed standard-deviation of the mean.
     */
    public double std_dev_of_mean(){return std_dev_of_mean(0);}
    
    
    // percentile gives the confidence interval with the given probability
    /** Returns the confidence interval for the given confidence probability.
     *
     * @param confIntProb The confidence probability. Must be between 0 and 1.
     * @param low The lower bound for the interval.
     * @param high The upper bound for the interval.
     */
    public void percentile(double confIntProb, DoubleRef low, DoubleRef high) {
        if (confIntProb > 1 || confIntProb < 0)
            Error.fatalErr("StatData::percentile: confidence interval should be "
            +"between 0 and 1.  "
            +"The given value was : "+confIntProb);
        if (size() < 1)
            Error.fatalErr("StatData::percentile: no data");
        
        // Note, casting constness because logically we're const, but
        //   physically, this is done best by sorting.
        itemsSort();
        
        // For some reason, if size=10, passing in .8 gives 0 without
        //   MLC::real_epsilon() on our Sparc.  Reason is that (double)1-.8-.2
        //   is not 0.
        int lowIndex = (int)(size()*(1-confIntProb)/2 + MLJ.realEpsilon);
        low = (DoubleRef)items.get(lowIndex); // starting at 0
        high = (DoubleRef)items.get(size() - lowIndex - 1);
    }
    
    /** Displays the data.
     * @param stream The Writer to which the StatData is displayed.
     */
    public void display(Writer stream) {
        try{
            //      ListIterator vi = items.listIterator();
            //      while(vi.hasNext())
            stream.write(items.toString());
        }catch(IOException e){e.printStackTrace();}
    }
    
    /** Displays the data.
     */
    public void display(){display(Globals.Mcout);}
    
    
    /** Generate histogram for the data.
     *
     * @param columnWidth The width of columns.
     * @param precision Precision used for numerical display.
     * @param stream The Writer to which the histogram is displayed.
     */
    public void display_math_histogram(double columnWidth, int precision,
    Writer stream) {
        try {
            double meanVal = mean();
            double minVal  = min(items); // could get first, but this is safer
            double maxVal  = max(items);
            
            int low  = cell(minVal, meanVal, columnWidth);
            int high = cell(maxVal, meanVal, columnWidth);
            int[] hist = new int[high - low + 1];
            
            for (int i = 0; i <= high - low + 1; i++)
                hist[cell(((DoubleRef)items.get(i)).value, meanVal, columnWidth)]++;
            stream.write("BarChart[{");
            hist_display(stream,hist);
            stream.write("}, Ticks->{{");
            for (int i = 0; i < hist.length; i++) {
                stream.write('{' + (i+1) + ", " // space for wrapping purposes.
                +MLJ.Mround((low + i) * columnWidth + meanVal, precision)
                +'}');
                if (i < hist.length - 1)
                    stream.write(',');
            }
            
            stream.write("}, Automatic}]\n");
        }catch(IOException e){e.printStackTrace();}
        
    }
    
    /** Generate histogram for the data.
     * @param columnWidth The width of columns.
     * @param precision Precision used for numerical display.
     */
    public void display_math_histogram(double columnWidth, int precision){display_math_histogram(columnWidth,precision,Globals.Mcout);}
    
    
    /** Append function adds the items of the other StatData to the items from this
     * StatData. Use this function to combine statistical data from more than one
     * source.
     *
     * @param other The StatData to be appended.
     */
    public void append(StatData other) {
        items.addAll(other.items);
    }
    
    /** Clear function removes all items from the StatData. This avoids having to use
     * StatData pointers just to allow you to clear out a StatData.
     *
     */
    public void clear() {
        items.clear();
    }
    
    /** Returns a value uniquely identifying this class.
     * @return A value identifying this class.
     * @deprecated Use Java's instanceOf built-in instead.
     */
    public int class_id() {
        return CLASS_STAT_DATA;
    }
    
    /** Set the StatData's member data to the same as that passed-in as 'rhs'. Replaces
     * the "=" operator.
     * @param rhs The StatData whose member data is to be duplicated in this StatData.
     * @return This StatData after assignment has taken place.
     */
    public StatData assign(StatData rhs) {
        if (this != rhs) {
            clear();
            append(rhs);
        }
        return this;
    }
    
    /** Compare the base-class member data of StatData for equality. All derived classes
     * need to both define an equality test method, and within that method invoke the
     * base-class equality test method. Replaces the "==" operator.
     * @param rhs The StatData to be compared to.
     * @return TRUE if the member data is equivalent, FALSE otherwise.
     */
    public boolean equals(StatData rhs) {
        if (class_id() != rhs.class_id())
            return false;
        
        mean(); // sort arrays so they are equal in any order
        rhs.mean();
        
        Vector a1 = items;
        Vector a2 = rhs.items;
        
        // Use approximate equality for the reals
        return MLJ.approx_equal(a1, a2);
    }
    
    /** Tests if this StatData does not have equivalent data members. Replaces the "!="
     * operator.
     * @param rhs The StatData to be compared to.
     * @return FALSE if the member data is equivalent, TRUE otherwise.
     */
    public boolean notEquals(StatData rhs)
    {return ! (this == rhs);}
    
    /** Compute squared error around the true value.
     * @param trueVal The true value for which squared error is requested.
     * @return The squared error.
     */
    public double squared_error(double trueVal) {
        if (size() < 1)
            Error.fatalErr("StatData::squared_error: no data");
        
        double result = 0;
        for(int k = 0; k < size(); k++)
            result += MLJ.square_real(((DoubleRef)items.get(k)).value - trueVal);
        
        return result;
    }
    
    
    
    /** Calculates the mean of the data.
     *
     * @return The mean of the data.
     */
    public double mean() {
        return mean(0);
    }
    
    /** Calculates the trimmed mean of the data.
     * @param trim The amount of trimming to be done. Trim = 0.5 now gives the median.
     * @return The trimmed mean of the data.
     */
    public double mean(double trim) {
        check_trim(trim);
        if (size() < 1)
            Error.err("StatData::mean: no data-->fatal_error");
        double result = 0;
        itemsSort();
        int low = (int)(size() * trim); // starting at 0
        int high = size() - low;
        if (low == high) {
            low--; high++;
        }
        for(int k = low; k < high; k++)
            result += ((DoubleRef)items.elementAt(k)).value;
        //   ASSERT(2*low < size()); // Make sure we're not dividing by zero.
        return result / (size() - 2*low);
        
    }
    
    private static void check_trim(double alpha) {
        if (alpha < 0 || alpha > 0.5)
            Error.err("StatData::check_trim: trimming value " + alpha + " is not in the range [0,0.5]-->fatal_error");
    }
    
    private void itemsSort() {
        //uses Bubblesort for the moment
        if (items.size() < 1) return;
        for (int j = 0; j < items.size(); j++)
            for(int k = j+1; k < items.size(); k++) {
                if (((DoubleRef)items.get(j)).value > ((DoubleRef)items.get(k)).value)
                    items.set(j,items.set(k,items.get(j)));
            }
    }
    
    /** Generates a cell number centered aroung 0 for histograms. Cell computation is <BR>
     * floor ((x - (mean+w/2))/w + 1)                                                <BR>
     * floor ((x - mean)/w + .5)
     *
     * @param value The histogram value.
     * @param mean Mean of values in the histogram.
     * @param columnWidth The width of columns in the histogram.
     * @return A cell number.
     */
    public static int cell(double value, double mean, double columnWidth) {
        return (int)Math.floor((value - mean) / columnWidth + 0.5);
    }
    
    
    private double min(Vector vctr) {
        double rtrn = 0;
        double temp;
        ListIterator vi = vctr.listIterator();
        while(vi.hasNext()) {
            temp = ((DoubleRef)vi.next()).value;
            if(temp < rtrn) rtrn = temp;
        }
        return rtrn;
    }
    
    private double max(Vector vctr) {
        double rtrn = 0;
        double temp;
        ListIterator vi = vctr.listIterator();
        while(vi.hasNext()) {
            temp = ((DoubleRef)vi.next()).value;
            if(temp > rtrn) rtrn = temp;
        }
        return rtrn;
    }
    
    private void hist_display(Writer stream, int[] histodata) {
        try {
            if (histodata != null) {
                stream.write(histodata[0]);
                for(int i = 1; i < histodata.length; i++)
                    stream.write(", "+ histodata[i]);
            }
        }catch(IOException e){e.printStackTrace();}
    }
    
}