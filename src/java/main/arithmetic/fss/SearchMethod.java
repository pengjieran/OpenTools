package arithmetic.fss;


public class SearchMethod{

/* SearchMethod ENUM */
public final static byte bestFirst = 0;
public final static byte hillClimbing = 1;
public final static byte simulatedAnnealing = 2;
/* END ENUM */

private byte value;

SearchMethod(){};
SearchMethod(byte i_value){ value = i_value;}

public String toString(){
   switch (value){
      case 0 : return "bestFirst";
      case 1 : return "hillClimbing";
      case 2 : return "simulatedAnnealing";
      default : return "SearchMethod.toString:: The stored value ("+value+")is not a specified value.";
   }
}

}