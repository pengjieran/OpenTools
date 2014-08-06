package arithmetic.fss;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.Categorizer;
import arithmetic.shared.InstanceList;
import arithmetic.shared.MLJ;
import arithmetic.shared.MLJArray;

/***************************************************************************
  Wrapper inducer for automatic feature subset selection. The feature 
selection is a best-first search from the initial state of no attributes 
(features).

  Complexity   : Training is the number of states searched times the
                   estimation time per state.
@author James Louis	8/8/2001	Ported to Java
@author Dan Sommerfield	5/24/95	Fit into new (SearchInducer) framework
@author Ronny Kohavi 	10/25/94	Initial revision (.h,.c)
***************************************************************************/


public class FSSInducer extends SearchInducer{

//Direction ENUM
/** Value for forward direction FSS. **/
   public static final int forward = 0;
/** Value for backward direction FSS. **/
   public static final int backward = 1;
//End ENUM

/** Indicator of FSS direction. **/
   protected int direction; //Direction enum

/** Default direction for FSS. **/
   private static int DEFAULT_DIRECTION = forward; //Direction enum
/** Help string for FSS direction option. **/
   private static String DIRECTION_HELP = "This option chooses the direction in "
      +"which to search.  Forward causes the search to begin with an empty subset "
      +"of features, while backward causes the search to begin with a full subset.";


/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private FSSInducer(FSSInducer source){
      super("",null);
   }

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(FSSInducer source){}

/***************************************************************************
  Constructor.
@param description	A description of this Inducer.
@param ind			The Inducer thathas been wrapped by this FSSInducer.
***************************************************************************/
   public FSSInducer(String description, BaseInducer ind)
   {
      super(description, ind);
      direction = DEFAULT_DIRECTION;
      // establish gloabal info (required)
      globalInfo = create_global_info();
   }

/***************************************************************************
  Constructor. The wrapped Inducer is set to null by default.
@param description	A description of this Inducer.
***************************************************************************/
   public FSSInducer(String description)
   {
      super(description, null);
      direction = DEFAULT_DIRECTION;
      // establish gloabal info (required)
      globalInfo = create_global_info();
   }


//obs   virtual int class_id() const { return FSS_INDUCER; }
   public int class_id(){ return FSS_INDUCER; }
   

/***************************************************************************
  Get extra options from the user.
@param prefix	Prefix string of the options associated with this FSSInducer.
***************************************************************************/
   public void set_user_options(String prefix)
   {
/*      super.set_user_options(prefix);

   // also pick a starting point (direction)
      direction =
         get_option_enum(prefix + "DIRECTION", directionEnum,
            DEFAULT_DIRECTION,
            DIRECTION_HELP, true);
*/   }

/***************************************************************************
  Display information about this FSSInducer.
@param stream	The writer to which this information will be displayed.
***************************************************************************
   public void display(Writer stream)
   {
      super.display(stream);
   }

/***************************************************************************
  Display information about this FSSInducer. The output is writted to Globals.Mcout by default.
***************************************************************************
   public void display()
   {
      super.display(Globals.Mcout);
   }
*/


/***************************************************************************
@return
***************************************************************************/
   public PerfEstInfo create_global_info()
   {
      return new FSSInfo();
   }

/***************************************************************************
  Create the initial state information. All TRUE for backward searches, all FALSE for forward.
@return
@param IL	The InstanceList on which training is to be done.
***************************************************************************/
   public int[] create_initial_info(InstanceList IL)
   {
      int[] rtrn = null; //Changed to fix scope problem.-JL
      has_global_info();
      switch(direction)
      {
         case forward:
//Changed to fix scope problem.-JL
//obs            int[] rtrn = new int[globalInfo.trainList.num_attr()];
            rtrn = new int[globalInfo.trainList.num_attr()];
            MLJArray.init_values(0,rtrn);
            return rtrn;
         case backward:
//Changed to fix scope problem.-JL
//obs            int[] rtrn = new int[globalInfo.trainList.num_attr()];
            rtrn = new int[globalInfo.trainList.num_attr()];
            MLJArray.init_values(1,rtrn);
            return rtrn;
         default:
            MLJ.ASSERT(false,"FSSInducer.create_initial_info(InstanceList):");
         return null;
      }
   }


/***************************************************************************
@return
@param
@param
***************************************************************************/
   public PerfEstState create_initial_state(int[] initialInfo,
					      PerfEstInfo gI)
   {
      return new FSSState(initialInfo, gI);
   }


/***************************************************************************
  Convert a state description to a Categorizer.
@return
@param
***************************************************************************/
   public Categorizer state_to_categorizer(int[] stateInfo)
   {
//obs      boolean[] boolArray = stateInfo;
      boolean[] boolArray = new boolean[stateInfo.length];
      for(int i = 0; i < stateInfo.length; i++) if(stateInfo[i] == 1) boolArray[i] = true;
      boolean[] attrMask = MLJArray.copy(boolArray);
//obs      FSSInducer ibid = const_cast<FSSInducer*> (this);
      FSSInducer ibid = this;
   
   // knock out any data owned by the globalInfo's inducer
      InstanceList oldData = ibid.globalInfo.inducer.release_data();

   // use attribute mask to create categorizer:
      ProjectInd projInd = new ProjectInd("ProjectInd for FSS Categorization");
      projInd.set_wrapped_inducer(ibid.globalInfo.inducer);

      projInd.set_project_mask(attrMask);
//obs      delete projInd.assign_data(ibid.TS);		 // sets TS to NULL
      projInd.assign_data(ibid.TS);
      projInd.train();

      Categorizer theCat = projInd.release_categorizer();

   // restore state
      ibid.globalInfo.inducer = projInd.release_wrapped_inducer();
      MLJ.ASSERT(globalInfo.inducer != null,"FSSInducer.state_to_categorizer: globalInfo.inducer == null");
//obs      delete ibid.globalInfo.inducer.assign_data(oldData);
      ibid.globalInfo.inducer.assign_data(oldData);
      ibid.TS = projInd.release_data();

      return theCat;
}

}