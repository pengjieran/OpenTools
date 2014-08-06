package arithmetic.chc;

import java.util.Random;

/*******************************************************************************************
  Shuffler is a Class that Randomly selects an object from an array. Selection is
   done with out replacement.
*******************************************************************************************/
public class Shuffler extends Random {
  private Object[] object;

  /*****************************************************************************************
    Constructor made specifically to handle ints which are not objects in Java.
     @param ob - the array of ints to be randomly selected.
  *****************************************************************************************/
  public Shuffler(int[] ob) {
    super(System.currentTimeMillis());
    if (ob == null) {
    }
    else {
      object = new Object[ob.length];
      for (int i = 0; i < ob.length; i++) {
        object[i] = new Integer(ob[i]);
      }
    }
  }

  /*****************************************************************************************
    Classic Constructor to take any type of Java Object and randomly select it.
     @param ob - the array of Objects to be selected.
  *****************************************************************************************/
  public Shuffler(Object[] ob) {
    super(System.currentTimeMillis());
    if (ob == null) {
      
    }
    else {
      object = ob;
    }
  }
  /*****************************************************************************************
    Constuctor made especially to make a list of numbers from 0 to num and add
     them to the list. This feature can be use with random selection of array elements.
     @param num - the number of consecutive ints that will be included in the selection.
  *****************************************************************************************/
  public Shuffler(int num) {
    super(System.currentTimeMillis());
    object = new Object[num];
    for (int i = 0; i < num; i++) {
      object[i] = new Integer(i);
    }
  }
  /*****************************************************************************************
    select is the method used to randomly select without replacement an item from the list and return it.
     @return the Object randomly selected from the list.
  *****************************************************************************************/
  public Object select() {
    int selectposition = nextInt(object.length);
//    System.out.println("Selectposition = "+selectposition);
    Object result;
    Object[] newob = new Object[object.length-1];
    for (int i = 0; i < selectposition; i++) {
//      System.out.print(i+",");
      newob[i] = object[i];
//      System.out.print(i+" ");
    }
    result = object[selectposition];
    for (int i = selectposition; i < newob.length; i++) {
//      System.out.print(i+",");
      newob[i] = object[i+1];
//      System.out.print(i+" ");
    }
    object = newob;
    return result;
  }

  /*****************************************************************************************
    Same as the select method but returns the int value of the randomly selected object. This method
     Should only be used if this Shuffler was created using ints.
     @return the randomly selected int.
  *****************************************************************************************/
  public int selectint() {
    return ((Integer)select()).intValue();
  }
  
  /*****************************************************************************************
    Ready is the method that checks to see if there are more objects to select.
     @return true if there is at least one more object to select; false otherwise.
  *****************************************************************************************/
  public boolean ready() {
    return object.length > 0;
  }


}
  
