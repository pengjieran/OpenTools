package arithmetic.fss;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.Categorizer;
import arithmetic.shared.Error;
import arithmetic.shared.Inducer;
import arithmetic.shared.InstanceList;
import arithmetic.shared.MLJ;

public class ProjectInd extends Inducer {

/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private ProjectInd(ProjectInd source){
   super("");}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(ProjectInd source){}

   // Member data
   ProjectCat categorizer;
   BaseInducer wrappedInducer;
//obs   BoolArray * attrMask;   
   boolean[] attrMask;   

// shortSchema only needed for incremental enhancement
//   SchemaRC * shortSchema;

/*
public void OK(){OK(0);}
public void OK(int level)
{
   super.OK(level);

   // if there is a wrapped inducer and it is a base inducer, there won't be
   // any TS on board to check the schema, so return.
   if ( has_wrapped_inducer(false) &&
	! wrappedInducer.can_cast_to_inducer() )
	  return;
   
   if ( has_project_mask() &&
	has_data(false) &&
	attrMask.size() != TS.get_schema().num_attr() )
      Error.fatalErr("ProjectInd::OK: Mask size ("+attrMask.size()
	  +") does not match the number of attributes ("
	  +TS.get_schema().num_attr()+") in the training set.");
}
*/



public ProjectInd(String description)
{
   super(description);
   wrappedInducer = null;
   attrMask = null;
   categorizer = null;

// shortSchema only needed for incremental enhancement
//   shortSchema = null;
}


protected void finilizer()
{
//   if (Basics.DBG) OK();
   categorizer = null;
   wrappedInducer = null;
   attrMask = null;
//      delete shortSchema;
}

public int class_id(){ return PROJECT_INDUCER; }


public void set_wrapped_inducer(BaseInducer ind)
{
   if( ind.can_cast_to_inducer() && ind.has_data(false) )
      Error.fatalErr("ProjectInd::set_wrapped_inducer: Wrapped inducer shouldn't "
	 +"have data.  ProjectInd will assign projected data.");
   wrappedInducer = ind;
   ind = null;
}

public BaseInducer get_wrapped_inducer()
{
   has_wrapped_inducer(true);
   return wrappedInducer;
}

public boolean has_wrapped_inducer(){return has_wrapped_inducer(false);}
public boolean has_wrapped_inducer(boolean fatalOnFalse)
{
   if( fatalOnFalse && wrappedInducer == null)
      Error.fatalErr("ProjectInd::has_wrapped_inducer: No inducer set.  Call "
	 +"set_wrapped_inducer() first");

   return ( wrappedInducer != null);
}

public BaseInducer release_wrapped_inducer()
{
   has_wrapped_inducer(true);
   BaseInducer retInd = wrappedInducer;
   wrappedInducer = null;
   return retInd;
}


public void set_project_mask(boolean[] attr)
{
   MLJ.ASSERT(attr != null,"ProjectInd.set_project_mask: attr == null.");
   if (attrMask != null)
      attrMask = null;
   attrMask = attr;
//   if(Basic.DBG) OK();
   attr = null;
}


public boolean[] get_project_mask()
{
   has_project_mask(true);
   return attrMask;
}

public boolean has_project_mask(){return has_project_mask(false);}
public boolean has_project_mask(boolean fatalOnFalse)
{
   if( fatalOnFalse && wrappedInducer == null)
      Error.fatalErr("ProjectInd::has_project_mask: No mask set.  Call "
	 +"set_project_mask() first");

   return ( attrMask != null);
}


public boolean was_trained(){return was_trained(true);}
public boolean was_trained(boolean fatalOnFalse)
{
   if( fatalOnFalse && categorizer == null)
      Error.fatalErr("ProjectInd::was_trained: No categorizer, "
	 +"Call train() to create categorizer");
   return ( categorizer != null);
}

public Categorizer get_categorizer()
{
   was_trained(true);
   return categorizer;
}


public Categorizer release_categorizer()
{
   was_trained(true);
   Categorizer retCat = categorizer;
   categorizer = null;
   return retCat;
}

public boolean can_cast_to_inducer()
{
   has_wrapped_inducer(true); 
   return wrappedInducer.can_cast_to_inducer();
}

public boolean can_cast_to_incr_inducer()
{
//   if (has_wrapped_inducer(FALSE))	// don't abort if no inducer
//      return wrappedInducer->can_cast_to_incr_inducer();
   return false;
}


public void train()
{
   has_wrapped_inducer();
   if (!wrappedInducer.can_cast_to_inducer())
      Error.fatalErr("ProjectInd::train(): wrapped inducer must be derived "
	 +"from Inducer (not BaseInducer) in order to train.");
   Inducer wInd = wrappedInducer.cast_to_inducer();
   has_project_mask();
   has_data();
//   if (Basics.DBG) OK();
   categorizer = null;
   
// shortSchema only needed for incremental enhancement
//   delete shortSchema;
//   shortSchema = new SchemaRC(TS->get_schema().project(*attrMask));
   
   // project data from my TS and put into wrapped inducer
   InstanceList shortInstList = TS.project(get_project_mask());
   
   // existing InstanceList is returned by assign_data() &
   //   it should be deleted
//obs   wInd.assign_data(shortInstList) = null;
   wInd.assign_data(shortInstList);
   
   // wInd gets ownership of InstanceList:
   MLJ.ASSERT(shortInstList == null,"ProjectInd.train: shortInstList != null.");
   
   // Train wrapped inducer on projected data.
   wInd.train();
   Categorizer newcat = wInd.release_categorizer();

   // create ProjectCat
   categorizer = new ProjectCat(description(), get_project_mask(),
				TS.get_schema(), newcat);

   categorizer.set_log_level(get_log_level());
}






public double train_and_test(InstanceList trainingSet,
				InstanceList testSet) 
{
   MLJ.ASSERT(trainingSet != null && testSet != null,"ProjectInd.train_and_test: trainingSet == null || testSet == null.");
   has_wrapped_inducer();
   has_project_mask();
   
   double error;
   // use base class's function when possible so that updates to it will
   // be used by this:
   if (wrappedInducer.can_cast_to_inducer())
      error = super.train_and_test(trainingSet, testSet);

   else {
      // project training and test sets
      InstanceList shortTraining = trainingSet.project(get_project_mask());
      InstanceList shortTest = testSet.project(get_project_mask());
      
      error = wrappedInducer.train_and_test(shortTraining, shortTest);
   }
   return error;
}



// IncrInducer& ProjectInd::cast_to_incr_inducer()
// {
//   ASSERT(can_cast_to_incr_inducer());
//   return *this;
// }

// Pix ProjectInd::add_instance(const InstanceRC& instance)
// {
//   was_trained(TRUE);
//   ASSERT(shortSchema != NULL);
//   
//   if (! wrappedInducer->can_cast_to_incr_inducer()) 
//      err << "ProjectInd::add_instance: Cannot add instance to "
//	 "non-incremental wrapped inducer." << fatal_error;
//
//   Pix returnPixVal = TS->add_instance(instance);
//   wrappedInducer->cast_to_incr_inducer().add_instance(
//      instance.project(*shortSchema, *attrMask));
//
//   return returnPixVal;
// }

// InstanceRC ProjectInd::del_instance(Pix& pix)
// {
//   was_trained(TRUE);
//   if (! wrappedInducer->can_cast_to_incr_inducer()) 
//      err << "ProjectInd::del_instance: Cannot remove instance from "
//	 "non-incremental wrapped inducer." << fatal_error;
//
//   InstanceRC instance = TS->remove_instance(pix);
//   InstanceRC shortInstance =
//      instance.project(*shortSchema, *attrMask);
//
//   Bool notFound = TRUE;
//   for (Pix shortPix=wrappedInducer->instance_list().first();
//	shortPix && notFound;
//	wrappedInducer->instance_list().next(shortPix) ) 
//      if (wrappedInducer->instance_list().get_instance(shortPix)
//	  == shortInstance) {
//	 wrappedInducer->cast_to_incr_inducer().del_instance(shortPix); 
//	 notFound = FALSE;
//      }
//   if (notFound)
//      err << "ProjectInd::del_instance: Internal Error: instance to be "
//	 "deleted not found in wrapped inducer's training set." << fatal_error;
//   
//   return instance;
// }

/*   
   virtual void display(MLCOStream& stream = Mcout,
			const DisplayPref& dp = defaultDisplayPref) const;
void ProjectInd::display(MLCOStream& stream,
			 const DisplayPref& dp) const
{
   if (stream.output_type() == XStream)
      err << "ProjectInd::display_struct: Xstream is not a valid "
          << "stream for this display_struct"  << fatal_error;

   if (dp.preference_type() != DisplayPref::ASCIIDisplay)
      err << "ProjectInd::display_struct: Only ASCIIDisplay is "
          << "valid for this display_struct"  << fatal_error;
      

   stream << "ProjectInd Inducer " << description() << endl;
   if ( has_wrapped_inducer(FALSE) ) {
      stream << "    Wrapped Inducer: " << get_wrapped_inducer().description()
	     << endl;
      if ( was_trained(FALSE) )
	 stream << "    Current Categorizer "  << endl;
	 get_categorizer().display_struct(stream, dp);
   }
   else
      stream << "No wrapped inducer set." << endl;
}
*/

/** Test function. */
/*
private void test_batch_inducer(boolean useAutoMinSplit)
{
   ProjectInd outerInd = new ProjectInd("ProjectInd: outer projecter");
   ID3Inducer id3Inducer = new ID3Inducer("ProjectInd: wrapped ID3 Inducer");
   
   if (useAutoMinSplit) id3Inducer.set_lower_bound_min_split_weight(0);

   BaseInducer wrappedIndPtr = id3Inducer;
   
   outerInd.set_wrapped_inducer(wrappedIndPtr);
   MLJ.ASSERT(wrappedIndPtr == null, "ProjectInd.test_batch_inducer: wrappedIndPtr != null\n");
   
   boolean[] attr = new boolean[6];
   attr[0] = true;
   attr[1] = true;
   attr[2] = false;
   attr[3] = false;
   attr[4] = true;
   attr[5] = false;
   
   outerInd.set_project_mask(attr);
   MLJ.ASSERT(attr == null,"ProjectInd.test_batch_inducer: attr != null \n");

   outerInd.read_data("monk1-full");
   outerInd.train();

   MLJ.ASSERT(outerInd.can_cast_to_incr_inducer() == false,"ProjectInd.test_batch_inducer: outerInd.can_cast_to_incr_inducer() != false \n");

   InstanceList testSet = new InstanceList(outerInd.instance_list().get_schema(),
			outerInd.instance_list().get_original_schema(),
			"monk1-full.test");
   CatTestResult catResult = new CatTestResult(outerInd.get_categorizer(),
			   outerInd.instance_list(), testSet);

   System.out.print("Projected Categorizer Results\n"+catResult+"\n");

   // test misc accessor methods and training twice:
   System.out.print("Wrapped Inducer: "+outerInd.get_wrapped_inducer().description()
	 +" used.\n");
   MLJ.ASSERT(!outerInd.can_cast_to_incr_inducer(),"ProjectInd.test_batch_inducer: !outerInd.can_cast_to_incr_inducer != true \n");
   boolean[] attr2 = new boolean[6];
   for(int m = 0; m < attr2.length; m++) attr2[m] = false;
   outerInd.set_project_mask(attr2);
   outerInd.train();

   //delete outerInd.release_wrapped_inducer();
   //delete outerInd.release_categorizer();
   MLJ.ASSERT(!outerInd.was_trained(false),"ProjectInd.test_batch_inducer:!outerInd.was_trained(false) != true \n");
}

private void test_errors() {
    System.out.print("\n   Testing error conditions. \n");
    ProjectInd outerInd = new ProjectInd("ProjectInd: outer projecter");
    BaseInducer wrappedIndPtr = new TableInducer("ProjectInd: wrapped Tbl Inducer", false);
    wrappedIndPtr.read_data("monk1-full");
    
    //   if (!memCheck) //memCheck not implemeted yet - JL
    //TEST_ERROR("ProjectInd::set_wrapped_inducer: Wrapped inducer shouldn't",
    //outerInd.set_wrapped_inducer(wrappedIndPtr));
    // if (!memCheck)
    //  TEST_ERROR("ProjectInd::has_wrapped_inducer: No inducer set.  Call ",
    //	 BaseInducer rtnInd = outerInd.release_wrapped_inducer();
    //       (void) rtnInd);
    //if (!memCheck)
    // TEST_ERROR("ProjectInd::has_project_mask: No mask set.  Call ",
    //	 outerInd.has_project_mask(TRUE));
    //if (!memCheck)
    // TEST_ERROR("ProjectInd::was_trained: No categorizer, ",
    //	 outerInd.was_trained(TRUE));
    
    InstanceList ts = wrappedIndPtr.release_data();
    wrappedIndPtr = null;
    wrappedIndPtr = new ID3Inducer("ProjectInd: wrapped ID3 Inducer");
    outerInd.set_wrapped_inducer(wrappedIndPtr);
    
    boolean[] attrBad = new boolean[5];
    for(int m =0; m < attrBad.length; attrBad[m] = true, m++);
    outerInd.set_project_mask(attrBad);
    
    //   if (!memCheck) {//memCheck not implemeted yet - JL
    //    TEST_ERROR("ProjectInd::OK: Mask size (",
    //		 outerInd.assign_data(ts));
    //    ts = outerInd.release_data();
    //   }
    
    
    boolean[] attr = new boolean[6];
    for(int m =0; m < attr.length; attr[m] = true, m++);
    outerInd.set_project_mask(attr);
    outerInd.assign_data(ts);
    MLJ.ASSERT(ts == null,"ProjectInd.test_errors: ts != null \n");
    
    boolean[] attrBad2 = new boolean[7];
    for(int m =0; m < attrBad2.length; attrBad2[m] = true, m++);
    
    //   if (!memCheck)//memCheck not implemeted yet - JL
    //      TEST_ERROR("ProjectInd::OK: Mask size (",
    //		 outerInd.set_project_mask(attrBad2));
    attr = new boolean[6];
    for(int m =0; m < attr.length; attr[m] = true, m++);
    outerInd.set_project_mask(attr);   // to allow gracefull destruction
    
    // Incremental functions not active:
    //   outerInd.train();
    //   Pix pix = outerInd.instance_bag().first();
    //   InstanceRC inst = outerInd.instance_bag().get_instance(pix);
    //    if (!memCheck) {
    //      TEST_ERROR("ProjectInd::add_instance: Cannot add instance to ",
    //	         outerInd.add_instance(inst));
    //      TEST_ERROR("ProjectInd::del_instance: Cannot remove instance from ",
    //	         outerInd.del_instance(pix));
    //    }
    
    // As of 10/4/96, the following is the only positive use of memCheck
    //   ("if (memCheck)", rather than "if (!memCheck)").
    //if (memCheck)//memCheck not implemeted yet - JL
    //    attrBad2 = null;
}


public static void main(String[] args)
{
   System.out.println("\nt_ProjectInd testing start.");
   test_batch_inducer(false);
   test_batch_inducer(true);
//   test_incr_inducer();
   test_errors();
   
   System.out.println("t_ProjectInd testing competed.");
   System.exit(0);
}
*/


  public int num_nontrivial_nodes() {
    return 0;
  }
  
  public int num_nontrivial_leaves() {
    return 0;
  }

}
