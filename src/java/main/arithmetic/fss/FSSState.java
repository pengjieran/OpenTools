package arithmetic.fss;

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

import arithmetic.shared.Globals;
import arithmetic.shared.InstanceList;
import arithmetic.shared.IntRef;
import arithmetic.shared.MLJ;

public class FSSState extends CompState{

/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private FSSState(FSSState source){
      super(null,null);
   }

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(FSSState source){}


//obs FSSState::FSSState(Array<int>*& featureSubset, const PerfEstInfo& gI)
//obs  : CompState(featureSubset, gI)
public FSSState(int[] featureSubset, PerfEstInfo gI)
{
   super(featureSubset, gI);
   complexity = 0;
   for(int i = 0; i < ((int[])get_info()).length; i++) {
      MLJ.ASSERT(((int[])get_info())[i] == 0 || ((int[])get_info())[i] == 1,
         "FSSState.FSSState:get_info()[i] != 0 && get_info()[i] != 1.");
      complexity += ((int[])get_info())[i];
   }
}   

//obs   virtual CompState *create_state(Array<int>*& info)
public CompState create_state(int[] info)
{
return new FSSState(info, (PerfEstInfo)globalInfo);
}

public void construct_lists(PerfEstInfo acInfo,
			       InstanceList trainList,
			       InstanceList testList)
{
   // convert to BoolArray to use project functions
//obs   BoolArray boolFeatureArray(get_info().low(), get_info().size(), FALSE);
   boolean[] boolFeatureArray = new boolean[((int[])get_info()).length];

   for(int i=0; i < ((int[])get_info()).length; i++)
//obs      boolFeatureArray[i] = ((int[])get_info())[i];
      if (((int[])get_info())[i] == 1)
         boolFeatureArray[i] = true;
      else
         boolFeatureArray[i] = false;
   trainList = trainList.project(boolFeatureArray);
   if(testList != null)
      testList = testList.project(boolFeatureArray);
}

public void destruct_lists(PerfEstInfo acInfo,
			      InstanceList trainList,
			      InstanceList testList)
{
//obs   delete trainList;
//obs   delete testList;
   trainList = null;
   testList = null;

}

public void display_info(Writer stream)
{
   try{
   // output node number if known
      if(get_eval_num() > NOT_EVALUATED)
         stream.write("#"+get_eval_num());
      else
         stream.write("#?");

   // figure out true indices in array
//obs   DynamicArray<int> trueIndices(get_info().size());
      Vector trueIndices = new Vector(((int[])get_info()).length);
      int lastTrueIndex = 0;
//obs   for(int i=get_info().low(); i<=get_info().high(); i++)
      for(int i= 0; i<=((int[])get_info()).length; i++)
         if(((int[])get_info())[i]!=0)
//obs	 trueIndices.index(lastTrueIndex++) = i;
         trueIndices.set(lastTrueIndex++, new IntRef(i));
//obs   trueIndices.truncate(lastTrueIndex);
      trueIndices.setSize(lastTrueIndex);

      stream.write("["+trueIndices+"]");

// Displaying names is not very useful because the lines turn out
// to be very long and it's hard to see the actual search.
//#  ifdef DISPLAY_NAMES
      if(Globals.DISPLAY_NAMES)
      {
         stream.write("[");
         for(int i=0; i<lastTrueIndex; i++)
         {
//obs         stream.write(globalInfo.trainList.get_schema().attr_name(trueIndices.index(i)));
            stream.write(((PerfEstInfo)globalInfo).trainList.get_schema().attr_name(((IntRef)trueIndices.get(i)).value));
            if(i < lastTrueIndex-1)
            stream.write(", ");
         }
         stream.write("]");
      }
   }catch(IOException e){e.printStackTrace();System.exit(1);}

//#  endif
}

/*
public:

   // display functions
   virtual void display_info(MLCOStream& stream) const;
*/
}