package arithmetic.id3;
import java.util.LinkedList;

import arithmetic.shared.Entropy;
import arithmetic.shared.Error;
import arithmetic.shared.Globals;
import arithmetic.shared.Inducer;
import arithmetic.shared.MLJ;
import arithmetic.shared.NominalAttrInfo;
import arithmetic.shared.RealAndLabelColumn;
import arithmetic.shared.Schema;
import arithmetic.shared.SplitAttr;
import arithmetic.shared.SplitScore;
import arithmetic.shared.StatData;

/** The ID3Class is the Java implementation of the ID3 algorithm. The
 * ID3 algorithm is a top-down decision-tree induction algorithm. This
 * algorithm uses the mutual information (original gain criteria),and
 * not the more recent information gain ratio.<P>
 * Complexity:<P>
 * Our split() method uses entropy and takes time O(vy) where v is
 * the total number of attribute values (over all attributes) and y
 * is the number of label values. This can be derived by noting that
 * mutual_info is computed for each attribute.<P>
 * Node categorizers (for predict) are AttrCategorizer and take
 * constant time, thus the overall prediction time is O(path-length).<P>
 * See TDDTInducer for more complexity information.<P>
 * Enhancements:<P>
 * The ID3Compute entropy once for the node, and pass it along to
 * avoid multiple computations like we do now.<P>
 *
 * @author James Louis 12/7/2000 Ported to Java
 * @author Clay Kunz 10/22/96 Changed bestSi to a pointer everywhere so
 * that we don't copy lots of split objects
 * around.
 * @author Yeogirl Yun 7/4/95 Added copy constructor.
 * @author Ronny Kohavi 9/08/93 Initial revision (.h,.c)
 */
public class ID3Inducer extends TDDTInducer
{
    /** Constructor.
     * @param dscr    The description of this inducer.
     * @param aCgraph A previously developed Cgraph.
     */
   public ID3Inducer(String dscr, CGraph aCgraph)
   {
      super(dscr, aCgraph);
   }

   /** Constructor.
    * @param dscr The description of this inducer.
    */
   public ID3Inducer(String dscr)
   {
      super(dscr); 
   }

   /** Copy Constructor.
    * @param source The original ID3Inducer that is being copied.
    */
   public ID3Inducer(ID3Inducer source)
   {
      super(source);
   }

   /** Returns the AttrCategorizer that splits on the best attribute found using
    * mutual information(information gain). Returns null if there is nothing
    * good to split on. Ties between this attribute and earlier attributes are
    * broken.
    * @param catNames The names of the categories that each instance may be
    * catagorized under.
    * @return The NodeCategorizer that splits on the best attribute found. May be
    * null if no good attribute split is found.
    */
   public  NodeCategorizer best_split(LinkedList catNames) 
   {
      Schema schema = TS.get_schema();
//schema used to be SchemaRC :JL
// @@ change these to return an index instead of bestSplit.
//   SplitAttr noSplit;
//bestSplit used to be set equal to noSplit : JL
      SplitAttr[] bestSplit = new SplitAttr[1]; 
	bestSplit[0] = new SplitAttr();
      SplitAttr[] splits = new SplitAttr[schema.num_attr()];
	for(int z = 0; z < splits.length;z++) splits[z] = new SplitAttr();
// @@ Call routine to initialize splits - sets penalty, minSplit
      if (!find_splits(bestSplit, splits)) return null;
      MLJ.ASSERT((bestSplit[0] != null) &&  (bestSplit[0].split_type() != SplitAttr.noReasonableSplit),
		"ID3Inducer:best_split--(bestSplit == null)"+
		"or(bestSplit.split_type() == noReasonableSplit)");
      NodeCategorizer bestCat = null;
      bestCat = split_to_cat(bestSplit[0], catNames);
      MLJ.ASSERT(bestCat != null,"ID3Inducer:best_split--bestCat == null");
//   DBG(bestCat->OK());
      logOptions.LOG(2, "Created split on attribute "+bestSplit[0].get_attr_num()+" ("+
          schema.attr_name(bestSplit[0].get_attr_num())+") at level "+
          get_level()+'\n');
      bestCat.build_distr(instance_list());
      return bestCat;
   }

   /** Fills in the array of splits for current subtree. It does very
    * little, but rarely overriden whereas best_split_info is overridden
    * by subclasses.
    * @return False if there is only one label value, the maximum number
    * of splits is reached, or if there is no reasonable split
    * available.
    * @param bestSplit This is an array of the best splits found during the
    * splitting process.
    * @param splits This is an array of all splits found during the
    * splitting process.
    */
   public boolean find_splits(SplitAttr[] bestSplit,
			    SplitAttr[] splits) 
   {
      if (TS.counters().label_num_vals() == 1)
         return false; // if we have one label value, we're done.
      if ((get_max_level() > 0)&&(get_level() >= get_max_level())) {
         logOptions.LOG(2, "Maximum level "+get_max_level()+" reached "+'\n');
         return false;
      }
      logOptions.LOG(3, TS.counters().toString());
      best_split_info(bestSplit, splits);
      return (bestSplit[0].split_type() != SplitAttr.noReasonableSplit);
   }

   /** Fills in the array of SplitAttr for current subtree. This function
    * is a good candidate to override in subclasses.
    * @param bestSplit	This is an array of the best splits found during the
    * splitting process.
    * @param splits	This is an array of all splits found during the
    * splitting process.
    */
   public  void best_split_info(SplitAttr[] bestSplit, SplitAttr[] splits) 
   {
      Schema schema = TS.get_schema();
   		//schema used to be SchemaRC : JL
      int numAttributes = schema.num_attr();
   
      StatData allMutualInfo = new StatData();
      StatData allNonMultiValMutualInfo = new StatData();
   
      RealAndLabelColumn[] realColumns = null;
      if (get_have_continuous_attributes()) {
         boolean[] mask = new boolean[numAttributes];
         for(int z = 0; z < numAttributes; z++) mask[z] = true;
         realColumns = TS.transpose(mask);
      }

      for (int attrNum = 0; attrNum < numAttributes; attrNum++) {
         split_info(attrNum, splits[attrNum], realColumns);
         // Find the mean of the mutual information over all attributes
         //   with reasonable splits.  From c4.5, we accumulate separately
         //   the mutual information that originates from attributes that
         //   do not have "too many" values.  Unless ALL attributes fail
         //   this criterion we use only those from the "smaller" attributes.
         // @@ We may want to compute the mean only when it's needed, i.e.,
         // @@ for gain-ratio emulation
         if (splits[attrNum].split_type() != SplitAttr.noReasonableSplit) {
            double mi = splits[attrNum].get_mutual_info(false, true);
            MLJ.ASSERT(mi >= 0,"ID3Inducer.best_split_info(SplitAttr,SplitAttr[])--"+
   			" mi < 0");
            logOptions.LOG(3, "Adding mutualInfo "+mi+" to mean.");
            allMutualInfo.insert(mi);
            if (!multi_val_attribute(attrNum)) {
               allNonMultiValMutualInfo.insert(mi);
               logOptions.LOG(3, "  It's not multi-val.");
            }
   	   logOptions.LOG(3,'\n');
         }
      }
      realColumns = null;
      pick_best_split(bestSplit, splits, allMutualInfo,allNonMultiValMutualInfo);
   }

   /** Return true if the attribute has many values according to
    * the C4.5 definition.
    * @return True if this attribute has many values, False otherwise.
    * @param attrNum	The number of the attribute being checked.
    */
   public boolean multi_val_attribute(int attrNum) 
   {
      double totalWeight = get_total_inst_weight();
      MLJ.ASSERT(totalWeight >= 0,"ID3Inducer.multi_val_attribute(int)--"+
   		 " totalWeight < 0");
      Schema schema = TS.get_schema();
//schema used to be SchemaRC : JL
      return ((schema.attr_info(attrNum).can_cast_to_nominal())&&(schema.num_attr_values(attrNum) >= (0.3 * totalWeight)));
   }

   /** Choose the best attribute to split the on from all possible splits.
    * @param bestSplit	The array of the best splits found during splitting
    * process.
    * @param splits	The array of all splits found during the splitting
    * process.
    * @param allMutualInfo	Statistical information about all instances.
    * @param allNonMultiValMutualInfo	Statistical information about instances
    * where an attribute can only have one
    * value at a time.
    */
   public void pick_best_split(SplitAttr[] bestSplit,
					SplitAttr[] splits,
					StatData allMutualInfo,
					StatData allNonMultiValMutualInfo) 
   {
      Schema schema = TS.get_schema();
      int numAttributes = schema.num_attr();

      if (get_split_score_criterion() != SplitScore.gainRatio) {
         for (int attrNum = 0; attrNum < numAttributes; attrNum++) {
            SplitAttr split = splits[attrNum];
            if (split.split_type() != SplitAttr.noReasonableSplit) {
      	    // Remember the best.  MLJ.realEpsilon is added because on
      	    //   monk1, the difference is 1e-16, and we want to tie break
      	    //   exactly as C4.5 does.
      	    // First half of test is because bestSplit might be unset, in
      	    //   which case we can't get its criterion score.
               if (bestSplit[0].split_type() == SplitAttr.noReasonableSplit
                  || split.score() > (bestSplit[0].score() + MLJ.realEpsilon))
                  bestSplit[0] = split;
            }
         }
      } else { // gain ratio
         double meanMutualInfo = Globals.UNDEFINED_REAL;
         if (allMutualInfo.size() > 0) 
         if (all_attributes_multi_val() || allNonMultiValMutualInfo.size() == 0) {
            meanMutualInfo = allMutualInfo.mean();
            if (all_attributes_multi_val()) logOptions.LOG(3, "All attributes are multi-val."+'\n');
         }
         else
            meanMutualInfo = allNonMultiValMutualInfo.mean();      
         logOptions.LOG(3,"Mean mutual info is "+meanMutualInfo+'\n');
   
         // Look at the criterion score for each attribute.  Any time an
         //   attribute has a mutual info greater than the mean mutual info
         //   it's a candidate for chosing as best.  If its score is
         //   greater than the max so far, pick it.
         double maxScore = Globals.UNDEFINED_REAL;
         boolean foundScoreAboveMean = false;
         for (int attrNum = 0; attrNum < numAttributes; attrNum++) {
            SplitAttr split = splits[attrNum];
            logOptions.LOG(3,"For attribute "+attrNum+", checking for reasonable split");
            if (split.split_type() == SplitAttr.noReasonableSplit){
               logOptions.LOG(3,"...Sorry, no reasonable split"+'\n');
            }
            else {
               boolean mutualInfoAboveMean = split.get_mutual_info(false,true) >
               meanMutualInfo + MLJ.realEpsilon;
   	    // was || maxScore == Globals.UNDEFINED_REAL)
//               if (maxScore == Globals.UNDEFINED_REAL) MLJ.ASSERT(!foundScoreAboveMean);
               if (mutualInfoAboveMean || !foundScoreAboveMean) {
//                  MLJ.ASSERT(meanMutualInfo != Globals.UNDEFINED_REAL);
                  logOptions.LOG(3, ", and that "+split.score()+" > "+meanMutualInfo+'\n');
                  double score = split.score();
                  logOptions.LOG(3,"Testing attribute "+attrNum+" ("+schema.attr_name(attrNum)+"): "+'\n');
//                  logOptions.LOG(3,split);
//                  logOptions.LOG(3,"Comparing score ("+score+") against max score + eps ("+
//                     (maxScore+MLJ.realEpsilon())+")"+'\n');
                  if (score > maxScore + MLJ.realEpsilon ||
                     (!foundScoreAboveMean && mutualInfoAboveMean)) {
                     logOptions.LOG(2,"Chose attribute "+attrNum+'\n');
                     maxScore = score;
                     bestSplit[0] = split;
                     logOptions.LOG(3,"max score becomes "+maxScore+'\n');
                  }
                  if (mutualInfoAboveMean)
                     foundScoreAboveMean = true;
               } else{
                  logOptions.LOG(3,"...not above mean"+'\n');
               }
            }
         }
      }
   }

   /** Checks if all attributes are multi-valued. An attribute is multivalued if
    * an instance can have more than one value at one time. If the attribute
    * contains values that are neither real or nominal, an abort message is issued.
    * @return False if the values are real numbers or if the nominal values are
    * not multivalued. Otherwise True is returned.
    */
   public boolean all_attributes_multi_val() 
   {
      boolean multiVal = true;
      Schema schema = TS.get_schema();
      for (int attrNum = 0; attrNum < schema.num_attr() && multiVal; attrNum++){
         if (schema.attr_info(attrNum).can_cast_to_real())
            multiVal = false;
         else if (schema.attr_info(attrNum).can_cast_to_nominal())
            multiVal = multi_val_attribute(attrNum);
               else
                  MLJ.Abort();
      }
      return multiVal;
   }
   /** Compute the split information for a given attribute.
    * @param attrNum The index number of this attribute column.
    * @param split The attribute to be split on.
    */
   public void split_info(int attrNum, SplitAttr split)
   {
      split_info(attrNum, split, null);
   }

   /** Compute the split information for a given attribute.
    * @param attrNum     The index number of this attribute column.
    * @param split       The attribute to be split on.
    * @param realColumns The columns of values specified for each attribute over
    * all instances in a data set.
    */
   public void split_info(int attrNum, SplitAttr split,
		   RealAndLabelColumn[] realColumns) 
   {
      Schema schema = TS.get_schema();

      if (attrNum < 0 || attrNum > schema.num_attr())
         Error.fatalErr("ID3Inducer::split_info: attrNum " + attrNum +
            " not in range 0 to " + schema.num_attr());
   
      logOptions.LOG(2,"Testing attribute "+attrNum+" ("+TS.get_schema().attr_name(attrNum)+"): ");

   // split needs to know if the Minimum Description Length Adjustment
   //   for continuous attributes needs to be applied.
      split.set_penalize_by_mdl(get_cont_mdl_adjust());
      double minSplit = Entropy.min_split(instance_list(),
			     get_lower_bound_min_split_weight(),
			     get_upper_bound_min_split_weight(),
			     get_min_split_weight_percent());
      double nominalMinSplit = minSplit;
      if (get_nominal_lbound_only()) {
         nominalMinSplit = get_lower_bound_min_split_weight();
         if (nominalMinSplit < 1)
            Error.fatalErr("split_info:  lowerBoundMinSplit (" +
               nominalMinSplit+") must be at least one");
      }
      logOptions.LOG(4,"Min split: "+(int)minSplit+", nominal min split: "+(int)nominalMinSplit+'\n');

   // If the attribute is nominal and has more than one value, then use
   //   mutual_info or gain_ratio to determine the information gained by
   //   splitting on it.
      if (schema.attr_info(attrNum).can_cast_to_nominal()) {
         split.set_split_score_criterion(get_split_score_criterion());
         if (SplitAttr.ok_to_split(attrNum, TS.counters(), nominalMinSplit)) {
            split.make_nominal_split(instance_list(), attrNum);
            logOptions.LOG(2,"Criterion score for attribute "+attrNum+" is "+split.score()+'\n');
	 MLJ.ASSERT(split.score() >= 0, "ID3Inducer::split_info(): split.score() >= 0");
         }
      }
   // Otherwise the attribute should be real and real_mutual_info is used
   //   to determine the threshold which gives the maximum information
   //   gain when split on.
      else if (schema.attr_info(attrNum).can_cast_to_real()) {
      // Find the best threshold (in find_best_threshold(), invoked through
      //   make_real_split()) based on the default split criterion; then
      //   set the criterion back.
      // @@ This will be an option for reals.
         if (realColumns == null)
            Error.fatalErr("ID3Inducer::split_info: can't split on real attribute "+
               schema.attr_name(attrNum)+" -- realColumns is null");
         RealAndLabelColumn column = realColumns[attrNum];
         if (column == null)
            Error.fatalErr("ID3Inducer::split_info: can't split on real attribute "+
               schema.attr_name(attrNum)+" -- the given column is null");
         column.sort();

         split.set_split_score_criterion(SplitScore.defaultSplitScoreCriterion);
         split.set_penalize_by_mdl(false);
         split.make_real_split(column, attrNum, minSplit,
            get_smooth_inst(), get_smooth_factor());
         split.set_split_score_criterion(get_split_score_criterion());
         split.set_penalize_by_mdl(get_cont_mdl_adjust());
         if (split.exist_split()) {
            logOptions.LOG(2,"Threshold: "+split.threshold()+", criterion score: "+
               split.score()+", entropy: "+split.get_entropy()+'\n');
            MLJ.ASSERT(split.score() >= 0, "ID3Inducer::split_info(): split.score() >= 0");
         }
      } else
         MLJ.Abort();
      
      if (split.split_type() == SplitAttr.noReasonableSplit)
      	logOptions.LOG(2,"No reasonable split"+'\n');
   }

//   public void set_unknown_edges(){ }

//   public void display_struct(){ }

//Additions by JL

//   private NO_DEFAULT_OPS(ID3Inducer);

   /** Create an Inducer for recursive calls. Since TDDTInducer is an abstract
    * class, it can't do the recursive call.
    * @param descr   The description of the new subinducer.
    * @param aCgraph A previously defined Cgraph for the inducer.
    * @return The new sub-ID3Inducer created.
    */
   public TDDTInducer create_subinducer(String descr, CGraph aCgraph) 
   {
      ID3Inducer inducer = new ID3Inducer(descr, aCgraph);
      inducer.copy_options(this);
      inducer.set_level(get_level() + 1);
      return inducer;
   }

   /** Build categorizer for the given attribute. The specified SplitAttr is
    * assumed to be valid.
    * @param split    The attribute to be split on.
    * @param catNames The category names that an instance may be categorized
    * under.
    * @return The NodeCategorizer containing a categorizer that splits on this
    * attribute.
    */
   public NodeCategorizer split_to_cat(SplitAttr split,
					 LinkedList catNames) 
   {
      int attrNum = split.get_attr_num();
      Schema schema = TS.get_schema();

//   MLJ.ASSERT(split.split_type() != SplitAttr::noReasonableSplit);
//   MLJ.ASSERT(attrNum >= 0);
//   MLJ.ASSERT(split.score() >= 0);
   
   // Else, build the categorizer
      String attrName = schema.attr_info(attrNum).name();
      if (get_debug()) {
         attrName = attrName+" (#=" + TS.num_instances()+
            "\\nENT="+ split.get_entropy()+
            "\\nMI=" + split.get_mutual_info(false, false)+
            "\\nGAIN=" + split.get_gain_ratio(false);
         if (split.get_penalize_by_mdl())
            attrName =attrName+"\\nMDL penalty=" + split.penalty();
         attrName =attrName+"\\nSCORE=" + split.score() + ")";
      }

      if (schema.attr_info(attrNum).can_cast_to_nominal()) {
//         MLJ.ASSERT(split.split_type() == SplitAttr::nominalSplit);
         NominalAttrInfo nai = schema.attr_info(attrNum).cast_to_nominal();
         int size = nai.num_values() + 1; // +1 for unknown
//         catNames = new String[size];//(Globals.UNKNOWN_CATEGORY_VAL, size);
         catNames.add(Globals.UNKNOWN_CATEGORY_VAL,"?");
         int cat = Globals.FIRST_CATEGORY_VAL;
         for (int i = 1; i < size; i++, cat++)
            catNames.add(cat,nai.get_value(i));

//UNKNOWN_NOMINAL_VAL is now 0 instead of -1. As a result, the 
//original input value for get_value would have been out of bounds 
//for the number of values. - JL
//            catNames.add(cat,nai.get_value(i + Globals.UNKNOWN_NOMINAL_VAL));
      
         return new AttrCategorizer(schema, attrNum, attrName);
      }
      else if (schema.attr_info(attrNum).can_cast_to_real()) {
         if (split.split_type() == SplitAttr.realThresholdSplit) {
            logOptions.LOG(5, split.threshold()+""+'\n');	 
            ThresholdCategorizer cat = new
            ThresholdCategorizer(schema, attrNum, split.threshold(),
               attrName);
		String[] categories = cat.real_edge_strings();
            for(int z = 0; z < categories.length; z++)
			catNames.add(z,categories[z]);
            return cat;
         } else {
            Error.fatalErr("ID3Inducer::split_to_cat: bad split type for "+
               "continuous attribute "+attrNum);
            return null;
         }
      } else {
         Error.fatalErr("ID3Inducer::split_to_cat: unrecognized attribute type"+
            " for attribute "+attrNum);
         return null;
      }
   }

   /** Returns the reference to the copy of ID3Inducer with the same settings.
    * @return A reference to an ID3Inducer.
    */
   public Inducer copy() 
   {
      Inducer ind = new ID3Inducer(this);
      return ind;
   }

   /** Returns the class id of this of this inducer.
    * @deprecated This method should be replaced with Java's instanceof operator.
    * @return Integer assigned to this inducer.
    */
   public int class_id(){ return ID3_INDUCER; }

}
