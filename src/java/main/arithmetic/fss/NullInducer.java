package arithmetic.fss;

import java.io.IOException;
import java.io.Writer;

import arithmetic.shared.AugCategory;
import arithmetic.shared.Categorizer;
import arithmetic.shared.ConstCategorizer;
import arithmetic.shared.DisplayPref;
import arithmetic.shared.Error;
import arithmetic.shared.Globals;
import arithmetic.shared.Inducer;
import arithmetic.shared.InstanceList;


/***************************************************************************
  The Null Inducer is an inducer which does nothing. If fatalOnCall is 
TRUE, any call which is related to training a structure will abort. If it 
is FALSE, calls will do the minimum necessary to allow the null inducer to 
execute in loops where it's used to store and retrieve data. The 
categorizer will constantly return UNKNOWN_CATEGORY_VAL.
@author James Louis	8/14/2001	Ported to Java
@author Ronny Kohavi	9/12/94	Added fatalOnCall option
@author Brian Frasca	1/29/94	Initial revision (.h,.c)
***************************************************************************/
public class NullInducer extends Inducer{

/** **/
   private boolean abortOnCalls;
/** **/
   private boolean trained;      // was train() called

/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private NullInducer(NullInducer source){
      super("");
   }

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(NullInducer source){}


/***************************************************************************
***************************************************************************/
   private void CANNOT_CALL(String function)
   {
      if (abortOnCalls)
         Error.fatalErr("NullInducer::"+ function +"() - cannot call from "
            +"a null inducer");
   }

/***************************************************************************
***************************************************************************/
   public NullInducer(String descr)
   {
      super(descr);
      abortOnCalls = true;
      trained = false;
   }


/***************************************************************************
***************************************************************************/
   public NullInducer(String descr, boolean fatalOnCalls)
   {
      super(descr);
      abortOnCalls = fatalOnCalls;
      trained = false;
   }

/***************************************************************************
***************************************************************************/
   public int class_id(){ return NULL_INDUCER; }

/***************************************************************************
***************************************************************************/
   public InstanceList assign_data(InstanceList data)
   {
      CANNOT_CALL("assign_data");
      return super.assign_data(data);
   }

/***************************************************************************
***************************************************************************/
   public void read_data(String file,
            String namesExtension, 
            String dataExtension)
   {
      CANNOT_CALL("read_data");
      super.read_data(file, namesExtension, dataExtension);
   }

/***************************************************************************
***************************************************************************/
   public void read_data(String file,
            String namesExtension)
   {
      CANNOT_CALL("read_data");
      super.read_data(file, namesExtension, Globals.DEFAULT_DATA_EXT);
   }

/***************************************************************************
***************************************************************************/
   public void read_data(String file)
   {
      CANNOT_CALL("read_data");
      super.read_data(file, Globals.DEFAULT_NAMES_EXT, Globals.DEFAULT_DATA_EXT);
   }

/***************************************************************************
***************************************************************************/
   public InstanceList release_data()
   {
      CANNOT_CALL("release_data");
      return super.release_data();
   }

/***************************************************************************
***************************************************************************/
   public boolean has_data(boolean fatalOnFalse)
   {
      CANNOT_CALL("has_data");
      return super.has_data(fatalOnFalse);
   }

/***************************************************************************
***************************************************************************/
   public boolean has_data()
   {
      CANNOT_CALL("has_data");
      return super.has_data(true);
   }

/***************************************************************************
***************************************************************************/
   public InstanceList instance_list()
   {
      CANNOT_CALL("instance_bag");
      return super.instance_list();
   }

/***************************************************************************
***************************************************************************/
   public void train()
   {
      CANNOT_CALL("train");
   }

/***************************************************************************
***************************************************************************/
   public boolean was_trained(boolean fatalOnFalse)
   {
      CANNOT_CALL("was_trained");
      if (fatalOnFalse && !trained)
         Error.fatalErr("NullInducer::was_trained: Inducer not trained");
      return trained;
   }

/***************************************************************************
***************************************************************************/
   public boolean was_trained()
   {
      CANNOT_CALL("was_trained");
      if (!trained)
         Error.fatalErr("NullInducer::was_trained: Inducer not trained");
      return trained;
   }


/***************************************************************************
***************************************************************************/
   public Categorizer get_categorizer()
   {
      AugCategory unknownAugCat = new AugCategory(Globals.UNKNOWN_CATEGORY_VAL, Globals.UNKNOWN_VAL_STR);
      String unknownDscr = new String("NullInducer");

      CANNOT_CALL("get_categorizer");   
      ConstCategorizer unknownCat = new ConstCategorizer(unknownDscr, unknownAugCat,
            TS.get_schema());
      return unknownCat;
   }

/***************************************************************************
***************************************************************************/
   public Categorizer release_categorizer()
   {
      AugCategory unknownAugCat = new AugCategory(Globals.UNKNOWN_CATEGORY_VAL, Globals.UNKNOWN_VAL_STR);
      String unknownDscr = new String("NullInducer");

      CANNOT_CALL("get_categorizer");   
      ConstCategorizer unknownCat = new ConstCategorizer(unknownDscr, unknownAugCat,
            TS.get_schema());
      return unknownCat;
   }   

/***************************************************************************
***************************************************************************/
   public void display_struct(Writer stream, DisplayPref dp)
   {
      CANNOT_CALL("display_struct");
//      if (stream.output_type() == XStream)
//         Error.fatalErr("NullInducer::display_struct: Xstream is not a valid "
//             +"stream for this display_struct");

      if (dp.preference_type() != DisplayPref.ASCIIDisplay)
         Error.fatalErr("NullInducer::display_struct: Only ASCIIDisplay is "
            +"valid for this display_struct");
      
      try{
         stream.write("Null Categorizer "+description()+"\n");
      }catch(IOException e){e.printStackTrace();System.exit(1);}
   }

/***************************************************************************
***************************************************************************/
   public void display_struct(Writer stream)
   {
      display_struct(stream,DisplayPref.defaultDisplayPref);
   }

/***************************************************************************
***************************************************************************/
   public void display_struct()
   {
      display_struct(Globals.Mcout,DisplayPref.defaultDisplayPref);
   }


  public int num_nontrivial_nodes() {
    return 0;
  }

  public int num_nontrivial_leaves() {
    return 0;
  }
}
