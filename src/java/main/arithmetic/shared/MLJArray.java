package arithmetic.shared;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/** This class includes methods useful in the manipulation of arrays. All
 * of the functions are static to make the use of this class easier for any
 * type of array.
 * @author James Louis
 */
public class MLJArray
{

/*class Array {
   NO_COPY_CTOR(Array);

   private boolean owner;

   protected int base;
   protected int arraySize; 
   protected Object[] elements;

   protected void alloc_array(int lowVal, int sizeVal) 
   {
      if (sizeVal < 0)
         Error.fatalErr("Array<>::Illegal bounds requested, base: "+lowVal+
           " high: "+(lowVal + sizeVal));
      base = lowVal;
      arraySize = sizeVal;
      elements = new Object[arraySize > 0 ? arraySize : 1];
      owner = true;
   }

//Shallow copy - JL
   protected void copy(Array source) 
   {  
      alloc_array(source.base, source.arraySize);
      for (int i = 0; i < arraySize; i++)
         elements[i] = source.elements[i];
   }


   #ifdef PC_MSVC_AMBIG
   protected Bool equal_given_that_bounds_are_equal( Array<Element>& rhs) ;
   #endif

   public Array(Array source,Object CtorDummy)
   {
      copy(source);
   }

   public Array(int baseStart, int size)
   {
      alloc_array(baseStart, size);
   }

   public Array(int size)
   {
      alloc_array(0, size);
   }

   public Array(int baseStart, int size,
		      Object initialValue) 
   {
      alloc_array(baseStart, size);
      init_values(initialValue);
   }

   public Array(Object[] carray, int length)
   {
      owner = FALSE;
      base = 0;
      arraySize = length;
      elements = carray;
      MLJ.ASSERT(length >= 0,"MLJArray.Array: length < 0.");
      MLJ.ASSERT(carray != null,"MLJArray.Array: carray == null.");
   }



   public void truncate(int size)
   {
      MLJ.ASSERT(size >= 0,"MLJArray.truncate: size < 0.");
      arraySize = size;
   }


   protected finalize() { if (owner) elements = null; }  

   public void assign(Array elem) 
   {
      if (this != &elem) {
         //DBG(if (elem.size() != size())
         Error.fatalErr("Array.assign: Cannot assign array sized: "+
            elem.size()+" to an array sized: " + size()));
         //DBG(if (elem.low() != low())
         Error.fatalErr("Array.assign: Cannot assign array starting at: " +
            elem.low()+" to an array starting at: "+low());
         for (int i = 0; i < arraySize; i++)
            elements[i] = elem.elements[i];
      }
      return this;
   }

   public Object[] get_elements() { return elements; } 

   public void init_values(Object initialValue) 
   {
  // walk through the array and initialize all of the values
      for (int i = 0; i < arraySize; i++)
         elements[i] = initialValue;
   }

   public int high(){ return base + arraySize - 1; } 
   public int low(){ return base; }
   public int size(){ return arraySize; }
   // read, write, display need MLCstream routines for reading/writing
   //  the object.

   public void read_bin(MLCIStream& str);
   public void write_bin(MLCOStream& str);
   public void display(MLCOStream& stream = Mcout) ;
   public Array<Element>& operator +=( Array<Element>& item);
   public Array<Element>& operator -=( Array<Element>& item);


   // CFront 3 chokes if these are moved outside the class
   // and defined as inline (it generates static function undefined).
   public Object index(int i) {
      DBG(if (i < 0 || i >= arraySize)
	  err << "Array<>::index: reference " << i
	      << " is out of bounds (0.." << arraySize - 1
	      << ')' << fatal_error
	  ); 
      return elements[i];
   }

   public Object index(int i)  {
      DBG(if (i < 0 || i >= arraySize)
	  err << "Array<>::index() :  reference "  << i
	      << " is out of bounds (0.."
	      <<  arraySize - 1 << ')' << fatal_error
	  ); 
  
      return elements[i];
   }

   public Object operator[](int i) {
      DBG(if (i < base || i > base + arraySize - 1)
	  err << "Array<>::operator[]: reference " << i
	      << " is out of bounds (" << base 
	      << ".." << high() << ')' << fatal_error
	  ); 
      return elements[i - base];
   }

   public Object operator[](int i)  {
      DBG(if (i < base || i > base + arraySize - 1)
	  err << "Array<>::operator[] : reference "  << i
	      << " is out of bounds (" << base << ".." << high()
	      << ')' << fatal_error
	  ); 
  
      return elements[i - base];
   }

   // The following two methods return the same data as the index()
   //   methods--but the fast_index() methods do NOT do ANY error checking.
   //   They are provided for speed--not safety.
   public Object fast_index(int i) {return elements[i];}
   public Object fast_index(int i)  {return elements[i];}
   // sort, min/max need operator<
   public void sort();
   public Object max(int& idx) ;  // idx is zero based index to array
   public Object max() ;
   public Object min(int& idx) ;  // idx is zero based index to array
   public Object min() ;
   public min_max( Object min,  Object max) ;
   public Element sum() ; // Sum the elements of the array.

   public int find( Object key, int startPos=0) ;
   
   public int num_element( Object key) ;
   public MString get_element_indexes( Object key) ;

   #ifdef PC_MSVC_AMBIG
   public Bool bounds_are_equal( Array<Element>& rhs) ;
   public Bool operator==( Array<Element>& rhs) ;
   public Bool operator!=( Array<Element>& rhs) ;
   #endif
};
*/

/***************************************************************************
  MLJArray class is not meant to be instanced as an object. The base 
constructor is private to reflect this.
***************************************************************************/
   private MLJArray()
   {}

   /** Returns the sum of an array of double values.
    * @return The sum of the array.
    * @param array	The array of double values to be added.
    */
   static public double sum(double[] array)
   {
      if (array.length == 0)
         Error.fatalErr("sum() - empty array");
      double total = 0;
      for(int i = 0 ; i < array.length ; i++)
         total += array[i];
      return total;
   }

   /** Initializes an array to a specified double value.
    * @param initialValue	The value to be initialized.
    * @param array		The array to be initialized.
    */
   static public void init_values(double initialValue, double[] array)
   {
      if (array.length == 0)
         Error.fatalErr("init_values() - empty array");
      for(int i = 0 ; i < array.length ; i++)
         array[i] = initialValue;
   }

   /** Initializes an array to a specified integer value.
    * @param initialValue	The value to be initialized.
    * @param array		The array to be initialized.
    */
   static public void init_values(int initialValue, int[] array)
   {
      if (array.length == 0)
         Error.fatalErr("init_values() - empty array");
      for(int i = 0 ; i < array.length ; i++)
         array[i] = initialValue;
   }

   /** Comparison function for the sort algorithm.
    * @return An integer representing the result of the comparison. The
    * possible values are -1, 0, 1.
    * @param a	The first item to be compared.
    * @param b	The second item to be compared.
    * @deprecated
    */
   static public int sort_compare(Sortable a,Sortable b)
   {
      // Note, on random data, you don't expect equality much, so it's better
      // to use operator< first.
      //Note that smaller numbers are at the beginning

      if (a.lessThan (b))
         return -1;
      else if (a.Equals (b))
         return 0;
      else
      return 1;
   }

/***************************************************************************
  Recursive Quicksort of an array of objects that implement the Sortable interface.
@param elements	The elements to be sorted in this iteration.
@param minpos	The minimum position included in this iteration.
@param maxpos	The maximum position included in this iteration.
@deprecated
***************************************************************************/
   static private void qsort(Sortable[] elements, int minpos, int maxpos)
   {
      if (minpos >= maxpos)
         return;
      int Up = 0;
      int Down = 1;
      Sortable temp = elements[Up];
      while(Up < Down)
      {
         for(Up = minpos + 1 ; Up < maxpos ; Up++)
            if (sort_compare (elements[Up],elements[minpos]) == 1)
               break;
         for(Down = maxpos ; Down > minpos ; Down--)
            if (sort_compare (elements[Down],elements[minpos]) == -1)
               break;
         if (Up < Down)
         {
            temp = elements[Up];
            elements[Up] = elements[Down];
            elements[Down] = temp;
         }
      }
      temp = elements[minpos];
      elements[minpos] = elements[Down];
      elements[Down] = temp;
      qsort(elements,minpos,Down - 1);
      qsort(elements,Down + 1, maxpos);
   }

   /** Sorts the array of Objects that implement the Sortable Interface.
    * @param theArray	The array to be sorted. The objects in the array must
    * implment the sortable interface.
    * @deprecated
    */
   static public void sort(Sortable[] theArray)
   {
      qsort(theArray,0,theArray.length - 1);

//	DBG(for (int i = 0; i < size()-1; i++)
//		if (! (index(i) < index(i+1) || index(i) == index(i+1)))
//		Error.fatalErr("Array<Element>::sort: sort error at index "+i+
//                    ".\n  Most probably cause is that operator< and operator="+
//                    " are inconsistent");
   }

   /** Sorts the specified array by the method supplied.
    * @param theArray	The array to be sorted.
    * @param sortMethod	The Method used for sorting.
    * @deprecated
    */
   static public void sort(Object[] theArray, Method sortMethod)
   {
      qsort(theArray,0,theArray.length - 1,sortMethod);
   }

/***************************************************************************
  Recursive Quicksort of an array of objects.
@param elements	The elements to be sorted in this iteration.
@param minpos	The minimum position included in this iteration.
@param maxpos	The maximum position included in this iteration.
@param sortMethod	The Method used to sort the array.
@deprecated
***************************************************************************/
   static private void qsort(Object[] elements, int minpos, int maxpos, Method sortMethod)
   {
      try
      {
         if (minpos >= maxpos)
            return;
         int Up = 0;
         int Down = 1;
         Object temp = elements[Up];
         Object[] args = new Object[1];
         while(Up < Down)
         {
            for(Up = minpos + 1 ; Up < maxpos ; Up++)
            {
               args[0] = elements[minpos];
               if (((Boolean) sortMethod.invoke (elements[Up],args)) .booleanValue ())
                  break;
            }
            for(Down = maxpos ; Down > minpos ; Down--)
            {
               args[0] = elements[minpos];
               if (((Boolean) sortMethod.invoke (elements[Down],args)) .booleanValue ())
                  break;
            }
            if (Up < Down)
            {
               temp = elements[Up];
               elements[Up] = elements[Down];
               elements[Down] = temp;
            }
         }
         temp = elements[minpos];
         elements[minpos] = elements[Down];
         elements[Down] = temp;
         qsort(elements,minpos,Down - 1,sortMethod);
         qsort(elements,Down + 1, maxpos,sortMethod);
      } catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   /** Finds the maximum value in an array of double values.
    * @return The maximum value.
    * @param index	An IntRef holding the place of the maximum value in the
    * array.
    * @param array	The array to be searched.
    */
   static public double max(IntRef index, double[] array)
   {
      if (array.length == 0)
         Error.fatalErr("Array<Element>::max() - empty array");
      double max = array[0];
      index.value = 0;
      for(int i = 1 ; i < array.length ; i++)
         if (array[i] > max)
         {
            max = array[i];
            index.value = i;
         }
      return max;
   }

   /** Searches an array of double values for a specified value.
    * @return The first index of the value searched for if found, otherwise -1.
    * @param key			The value to be searched for.
    * @param startPosition	The array index to start searching from.
    * @param array		The array to be searched.
    */
   static public int find(double key, int startPosition, double[] array)
   {
      if (startPosition < 0 || startPosition > array.length)
         Error.fatalErr("Array<>::find: start position " +startPosition+ " is out " 
                + "of the legal range [0-" +array.length+ "]");
      for(int idx = startPosition ; idx < array.length ; idx++)
         if (array[idx] == key)
            return idx;
   // could not find it--return -1
      return -1;
   }

   /** Sorts the specified array of integers.
    * @param theArray	The array to be sorted. The array is changed by the use
    * of this method.
    */
   static public void sort(int[] theArray)
   {
      qsort(theArray,0,theArray.length-1);
   }

/***************************************************************************
  Recursive Quicksort of an array of integers.
@param elements	The elements to be sorted in this iteration.
@param minpos	The minimum position included in this iteration.
@param maxpos	The maximum position included in this iteration.
@deprecated
***************************************************************************/
   static private void qsort(int[] elements, int minpos, int maxpos)
   {
      if (minpos >= maxpos)
         return;
      int Up = 0;
      int Down = 1;
      int temp = elements[Up];
      while(Up < Down)
      {
         for(Up = minpos + 1 ; Up <= maxpos ; Up++)
            if (elements[Up] > elements[minpos])
               break;
         for(Down = maxpos ; Down >= minpos ; Down--)
            if (elements[Down] <= elements[minpos])
               break;
         if (Up < Down)
         {
            temp = elements[Up];
            elements[Up] = elements[Down];
            elements[Down] = temp;
         }
      }
      temp = elements[minpos];
      elements[minpos] = elements[Down];
      elements[Down] = temp;
      qsort(elements,minpos,Down - 1);
      qsort(elements,Down + 1, maxpos);
   }

   /** Counts the number of values in a boolean array match a specified value.
    * @return The number of elements which match the specified value.
    * @param key		The value to be searched for.
    * @param array	The array to be searched.
    */
   static public int num_element(boolean key, boolean[] array)
   {
      int sum = 0;
      for(int idx = 0 ; idx < array.length ; idx++)
         if (array[idx] == key)
            sum++;
      return sum;
   }

   /** Copies the given integer array and returns the copy.
    * @param source The array to be copied.
    * @return The new copy.
    */   
   static public int[] copy(int[] source)
   {
      int[] rtrn =new int[source.length];
      for(int i = 0;i < source.length;i++)
         rtrn[i] = source[i];
      return rtrn;
   }

   /** Copies the given double array and returns the copy.
    * @param source The array to be copied.
    * @return The new copy.
    */   
   static public double[] copy(double[] source)
   {
      double[] rtrn =new double[source.length];
      for(int i = 0;i < source.length;i++)
         rtrn[i] = source[i];
      return rtrn;
   }

   /** Copies the given boolean array and returns the copy.
    * @param source The array to be copied.
    * @return The new copy.
    */   
   static public boolean[] copy(boolean[] source)
   {
      boolean[] rtrn =new boolean[source.length];
      for(int i = 0;i < source.length;i++)
         rtrn[i] = source[i];
      return rtrn;
   }

/*class Array {
   NO_COPY_CTOR(Array);

   private boolean owner;

   protected int base;
   protected int arraySize; 
   protected Object[] elements;

   protected void alloc_array(int lowVal, int sizeVal) 
   {
      if (sizeVal < 0)
         Error.fatalErr("Array<>::Illegal bounds requested, base: "+lowVal+
           " high: "+(lowVal + sizeVal));
      base = lowVal;
      arraySize = sizeVal;
      elements = new Object[arraySize > 0 ? arraySize : 1];
      owner = true;
   }

//Shallow copy - JL
   protected void copy(Array source) 
   {  
      alloc_array(source.base, source.arraySize);
      for (int i = 0; i < arraySize; i++)
         elements[i] = source.elements[i];
   }


   #ifdef PC_MSVC_AMBIG
   protected Bool equal_given_that_bounds_are_equal( Array<Element>& rhs) ;
   #endif

   public Array(Array source,Object CtorDummy)
   {
      copy(source);
   }

   public Array(int baseStart, int size)
   {
      alloc_array(baseStart, size);
   }

   public Array(int size)
   {
      alloc_array(0, size);
   }

   public Array(int baseStart, int size,
		      Object initialValue) 
   {
      alloc_array(baseStart, size);
      init_values(initialValue);
   }

   public Array(Object[] carray, int length)
   {
      owner = FALSE;
      base = 0;
      arraySize = length;
      elements = carray;
      MLJ.ASSERT(length >= 0,"MLJArray.Array: length < 0.");
      MLJ.ASSERT(carray != null,"MLJArray.Array: carray == null.");
   }



   public void truncate(int size)
   {
      MLJ.ASSERT(size >= 0,"MLJArray.truncate: size < 0.");
      arraySize = size;
   }


   protected finalize() { if (owner) elements = null; }  

   public void assign(Array elem) 
   {
      if (this != &elem) {
         //DBG(if (elem.size() != size())
         Error.fatalErr("Array.assign: Cannot assign array sized: "+
            elem.size()+" to an array sized: " + size()));
         //DBG(if (elem.low() != low())
         Error.fatalErr("Array.assign: Cannot assign array starting at: " +
            elem.low()+" to an array starting at: "+low());
         for (int i = 0; i < arraySize; i++)
            elements[i] = elem.elements[i];
      }
      return this;
   }

   public Object[] get_elements() { return elements; } 

   public void init_values(Object initialValue) 
   {
  // walk through the array and initialize all of the values
      for (int i = 0; i < arraySize; i++)
         elements[i] = initialValue;
   }

   public int high(){ return base + arraySize - 1; } 
   public int low(){ return base; }
   public int size(){ return arraySize; }
   // read, write, display need MLCstream routines for reading/writing
   //  the object.

   public void read_bin(MLCIStream& str);
   public void write_bin(MLCOStream& str);
   public void display(MLCOStream& stream = Mcout) ;
   public Array<Element>& operator +=( Array<Element>& item);
   public Array<Element>& operator -=( Array<Element>& item);


   // CFront 3 chokes if these are moved outside the class
   // and defined as inline (it generates static function undefined).
   public Object index(int i) {
      DBG(if (i < 0 || i >= arraySize)
	  err << "Array<>::index: reference " << i
	      << " is out of bounds (0.." << arraySize - 1
	      << ')' << fatal_error
	  ); 
      return elements[i];
   }

   public Object index(int i)  {
      DBG(if (i < 0 || i >= arraySize)
	  err << "Array<>::index() :  reference "  << i
	      << " is out of bounds (0.."
	      <<  arraySize - 1 << ')' << fatal_error
	  ); 
  
      return elements[i];
   }

   public Object operator[](int i) {
      DBG(if (i < base || i > base + arraySize - 1)
	  err << "Array<>::operator[]: reference " << i
	      << " is out of bounds (" << base 
	      << ".." << high() << ')' << fatal_error
	  ); 
      return elements[i - base];
   }

   public Object operator[](int i)  {
      DBG(if (i < base || i > base + arraySize - 1)
	  err << "Array<>::operator[] : reference "  << i
	      << " is out of bounds (" << base << ".." << high()
	      << ')' << fatal_error
	  ); 
  
      return elements[i - base];
   }

   // The following two methods return the same data as the index()
   //   methods--but the fast_index() methods do NOT do ANY error checking.
   //   They are provided for speed--not safety.
   public Object fast_index(int i) {return elements[i];}
   public Object fast_index(int i)  {return elements[i];}
   // sort, min/max need operator<
   public void sort();
   public Object max(int& idx) ;  // idx is zero based index to array
   public Object max() ;
   public Object min(int& idx) ;  // idx is zero based index to array
   public Object min() ;
   public min_max( Object min,  Object max) ;
   public Element sum() ; // Sum the elements of the array.

   public int find( Object key, int startPos=0) ;
   
   public int num_element( Object key) ;
   public MString get_element_indexes( Object key) ;

   #ifdef PC_MSVC_AMBIG
   public Bool bounds_are_equal( Array<Element>& rhs) ;
   public Bool operator==( Array<Element>& rhs) ;
   public Bool operator!=( Array<Element>& rhs) ;
   #endif
};
*/

   /** This function truncates an array of Objects.
    * @param newSize The new size of the array.
    * @param array The array to be truncated.
    * @return The truncated array.
    */
   static public Object[] truncate(int newSize, Object[] array)
   {
      MLJ.ASSERT(array.length >= 0, "Array::truncate: array.length < 0");
      Object[] newArray = (Object[])Array.newInstance(array.getClass().getComponentType(),newSize);
      for(int i = 0 ; i < newArray.length ; i++)
         newArray[i] = array[i];
      return newArray;
   }
}