package arithmetic.fss;


public class ShowTestSetPerf{

/* ShowTestSetPerf ENUM */
public final static byte showNever = 0;
public final static byte showFinalOnly = 1;
public final static byte showBestOnly = 2;
public final static byte showAlways = 3;
/* END ENUM */

private byte value;

public ShowTestSetPerf(){ value = 0;}
public ShowTestSetPerf(byte i_value){ value = i_value;}

public byte get_value(){return value;}
public void set_value(byte i_value){value = i_value;}

public String toString(){
   switch (value){
      case 0 : return "showNever";
      case 1 : return "showFinalOnly";
      case 2 : return "showBestOnly";
      case 3 : return "showAlways";
      default : return "ShowTestSetPerf.toString:: The stored value ("+value+")is not a specified value.";
   }
}

}