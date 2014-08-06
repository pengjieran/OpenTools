package arithmetic.fss;

import java.io.BufferedWriter;
import java.io.IOException;

import arithmetic.shared.AugCategory;
import arithmetic.shared.Basics;
import arithmetic.shared.Categorizer;
import arithmetic.shared.DisplayPref;
import arithmetic.shared.Error;
import arithmetic.shared.Instance;
import arithmetic.shared.MLJ;
import arithmetic.shared.MLJArray;
import arithmetic.shared.Schema;

public class ProjectCat extends Categorizer {
/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private ProjectCat(ProjectCat source){
      super(0,"",null);
   }

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(ProjectCat source){}


   // Member data
//obs   BoolArray attrMask;
   boolean[] attrMask;
   Schema fullSchema;
   Schema shortSchema;
   Categorizer categorizer;   

public void OK(){OK(0);}
public void OK(int level)
{
   MLJ.ASSERT(categorizer != null,"ProjectCat.OK(int): categorizer == null");
   int num_true = 0;
   for(int j = 0; j< attrMask.length; j++) if (attrMask[j]) num_true++;
//obs   MLJ.ASSERT(shortSchema.num_attr() == attrMask.num_true(),"ProjectCat.OK(int): shortSchema.num_attr() != attrMask.num_true()");
   MLJ.ASSERT(shortSchema.num_attr() == num_true,"ProjectCat.OK(int): shortSchema.num_attr() != attrMask.num_true()");
}

//obs public ProjectCat( const MString& dscr,
//obs 			const BoolArray& attrmask,
//obs 			const SchemaRC& schema,
//obs 			Categorizer*& cat )
public ProjectCat(String dscr, boolean[] attrmask, Schema schema, Categorizer cat)
{
   super(cat.num_categories(), dscr, schema);
//obs   attrMask(attrmask, ctorDummy),
   attrMask = MLJArray.copy(attrmask);
   fullSchema = schema;
   try{
      shortSchema = schema.project(attrmask);
   }catch(CloneNotSupportedException e){e.printStackTrace(); System.exit(1);}
   categorizer = cat;
   cat = null;			// take ownership
}

/*
//obs public ProjectCat(ProjectCat source, CtorDummy dummyArg)
public ProjectCat(ProjectCat source, Object dummyArg)
{
//obs   super(source, ctorDummy);
   attrMask = MLJArray.copy(source.attrMask);
   fullSchema = source.fullSchema;
   shortSchema = source.shortSchema;

   categorizer = (Categorizer)source.categorizer.clone();
}
*/

protected void finalizer()
{
   categorizer = null;
}

public AugCategory categorize(Instance inst)
{
   if (Basics.DBG) OK();
   return categorizer.categorize(inst.project(shortSchema, attrMask));
}

private String attrMask_toString()
{
   String rtrn = "";
   for(int g = 0; g < attrMask.length; g++)
      if(attrMask[g]) {rtrn = rtrn + " 1";}
      else {rtrn = rtrn + " 0";}
   return rtrn + ".";
}

public void display_struct(BufferedWriter stream,DisplayPref dp)
{
   try{
   if (dp.preference_type() == DisplayPref.ASCIIDisplay)
   {
      stream.write("Projecting Categorizer for "+description()+"\n"
	     +"   Attribute Mask: "+attrMask_toString()+"\n"
	     +"   Projected Schema: "+shortSchema +"\n"
	     +"   Using Categorizer: ");
      categorizer.display_struct(stream, dp);
      stream.write("\n");
   } else // unrecognized.  Just pass down
      categorizer.display_struct(stream, dp);
   }catch(IOException e){e.printStackTrace();System.exit(1);}
}


/*
public Categorizer clone()
{
//obs   if (class_id() != CLASS_PROJECT_CATEGORIZER)
   if(!(this instanceOf ProjectCat)
      Error.fatalErr("ProjectCat::clone: invoked for improper class of id "
	  +this.getClass().getName();
   return new ProjectCat(this, ctorDummy);
}
*/

   public int class_id(){ return CLASS_PROJECT_CATEGORIZER; }


   public Categorizer inner_categorizer(){
      return categorizer;
   }

public Categorizer release_categorizer()
{
   if(categorizer == null)
      Error.fatalErr("ProjectCat::release_categorizer: categorizer already "
	 +"released");
   Categorizer cat = categorizer;
   categorizer = null;
   return cat;
}

public boolean[] get_mask(){ return attrMask; }

//obs   virtual Bool operator==(const Categorizer& rhs) const;
public boolean equals(Categorizer rhs)
{
//obs   if (!(class_id() == rhs.class_id()))
   if (this.getClass() != rhs.getClass())
      return false;
   
   boolean isEqual = super.equals(rhs);

   ProjectCat other = (ProjectCat) rhs; // safe

   isEqual = isEqual &&
	   attrMask == other.attrMask &&
	   fullSchema == other.fullSchema &&
	   shortSchema == other.shortSchema;

   isEqual = isEqual &&
      ((categorizer == null) == (other.categorizer == null));

   if (categorizer != null)
      isEqual = isEqual &&
	 (categorizer == other.categorizer);
      
   return isEqual;
}
}
