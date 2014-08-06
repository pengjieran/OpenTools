package arithmetic.id3;
import arithmetic.shared.AugCategory;
import arithmetic.shared.DoubleRef;
import arithmetic.shared.Edge;
import arithmetic.shared.Error;
import arithmetic.shared.InstanceList;
import arithmetic.shared.MLJ;
import arithmetic.shared.Node;

/** DecisonTrees are RootedCatGraphs where each node other than the root has
 * exactly one parent.  The root has no parents.
 * @author James Louis 5/29/2001   Ported to Java.
 * @author Eric Eros 4/18/96 Added delete_subtree
 * @author Ronny Kohavi  4/16/96 Added treeviz display
 * @author Richard Long 9/02/93 Initial revision (.c,.h)
 */

public class DecisionTree extends RootedCatGraph {
    /** Indicates if this DecisionTree is sparsely populated.
     */
    boolean isGraphSparse = false;
    
    /** Constructor.
     */
    public DecisionTree() {
        super(false);
    }
    
    /** Constructor.
     * @param grph The CGraph object to be used to maintain the DecisionTree.
     */
    public DecisionTree(CGraph grph) {
        super(grph, false);
    }
    
    /** Distribute instances to a subtree. This function is used whenever we
     * replace a node with its child.  The distributions of the child include
     * only the instances there while if we replace, we must update all the
     * counts. This function is also the backfitting function for decision trees.
     * @param subtree The subtree over which Instances will be distributed.
     * @param il InstanceList to be distributed over the DecisionTree.
     * @param pruningFactor The amount of pruning to be done on this tree.
     * @param pessimisticErrors Number of errors estimated for the new distribution.
     * @param ldType Leaf Distribution Type.
     * @param leafDistParameter The distribution of instances that reach this leaf node.
     * @param parentWeightDist The weight distribution of the parent node.
     */
    public void distribute_instances(Node subtree,
    InstanceList il,
    double pruningFactor,
    DoubleRef pessimisticErrors,
    int ldType, 			//TDDTInducer.LeafDistType
    double leafDistParameter,
    double[] parentWeightDist) {
        distribute_instances(subtree,il,pruningFactor,pessimisticErrors,ldType,
        leafDistParameter,parentWeightDist,false);
    }
    
    /** Distribute instances to a subtree. This function is used whenever we
     * replace a node with its child.  The distributions of the child include
     * only the instances there while if we replace, we must update all the
     * counts. This function is also the backfitting function for decision trees.
     * @param subtree The subtree over which Instances will be distributed.
     * @param il InstanceList to be distributed over the DecisionTree.
     * @param pruningFactor The amount of pruning to be done on this tree.
     * @param pessimisticErrors Number of errors estimated for the new distribution.
     * @param ldType Leaf Distribution Type.
     * @param leafDistParameter The distribution of instances that reach this leaf node.
     * @param parentWeightDist The weight distribution of the parent node.
     * @param saveOriginalDistr TRUE if the original instance distribution should be preserved, FALSE otherwise.
     */
    public void distribute_instances(Node subtree,
    InstanceList il,
    double pruningFactor,
    DoubleRef pessimisticErrors,
    int ldType, 			//TDDTInducer.LeafDistType
    double leafDistParameter,
    double[] parentWeightDist,
    boolean saveOriginalDistr) {
        //   DBGSLOW(check_node_in_graph(subtree, TRUE));
        NodeCategorizer splitCat = ((NodeInfo)cGraph.inf(subtree)).get_categorizer();
        
        logOptions.LOG(3, "Distributing instances: " + il + '\n' + "categorizer is "
        +splitCat.description()+'\n');
        
        splitCat.distribute_instances(il, pruningFactor, pessimisticErrors, ldType,
        leafDistParameter, parentWeightDist,
        saveOriginalDistr);
    }
    
    /** Removes a subtree recursively. This is used to                           <BR>
     * a)  Remove a node and all nodes below it if the second parameter is NULL,<BR>
     * b)  Remove just the nodes under a particular node, if both parameters are
     * the same (the named node remains in the graph).                          <BR>
     * c)  Replace the subtree rooted at the first parameter with the subtree
     * rooted at the second parameter, if the two parameters are not equal, and
     * are non-null.                                                            <P>
     * We allow replacing node X with a child of node X (or a node related
     * through comman ancestors) or, in general, replacing a subtree with
     * another subtree. In both cases, we disconnect the parents of the new node
     * node from the new node.                                                  <P>
     * We do not allow replacing node X with an ancester (parent, etc.) of
     * node X, as this would make no sense.                                     <P>
     * The method is as follows:                                                <P>
     * 1)  If 'node' is to be deleted, delete the edges connecting it to its
     * parents.                                                                 <BR>
     * 2)  If 'node' is to be replaced by 'newNode', delete the edges connecting
     * 'newNode' to its parents.                                                <BR>
     * 3)  Delete the edges from 'node' to all its children.                    <BR>
     * 4)  If 'node' is to be deleted, since it's now completely disconnected,
     * delete it.                                                               <BR>
     * 5)  If 'node' is to be replaced by 'newNode',                            <BR>
     * 5a)  Connect all of 'newNode's children to 'node' (adding edges),        <BR>
     * 5b)  Delete all the edges from 'newNode' to its children.                <BR>
     * 5c)  Since 'newNode' is now completely disconnected, delete it.          <BR>
     * 6)  For all the children discovered in step 3, recurse to delete them.
     * @param node Node to be replaced.
     * @param newNode New Node to be used for replacement.
     */
    public void delete_subtree(Node node, Node newNode) {
        if (node == null)
            Error.fatalErr("DecisionTree::delete_subtree: node is NULL");
        
        // Delete a subtree, given the starting  node.  The second parameter
        //   NULL means the top-most node is deleted--it is set NULL for all
        //   recursive calls from here, so that all children are deleted.  If the
        //   second parameter is non-NULL, it needs to point to a node in the
        //   same cGraph as the first.
        boolean deleteNode = (newNode == null);
        boolean replaceWithSelf = (node == newNode);
        boolean replaceWithOther = !deleteNode && !replaceWithSelf;
        
        // We can extend this routine to support the new node being the root,
        //   but it seems very strange to do so, since we usually delete the
        //   newNode.
        //   One would need to set the new node to be the root.  For safely
        //   it's better to abort in this case that can't happen right now.
        // Note that replacing the root with a child is OK and the root
        //   will be the new node because it's the categorizer that's replace,
        //   and the root reference remains valid
        if (!replaceWithSelf && newNode == get_root())
            Error.fatalErr("DecisionTree::delete_subtree: new node cannot be root");
        
        if (deleteNode)
            logOptions.LOG(5, " 1. Deleting the node " + node + '\n');
        else
            logOptions.LOG(5, " 2. Removing the subtree from node " + node + '\n');
        if (!deleteNode && !replaceWithSelf)
            logOptions.LOG(5, " 3. Replacing it with the node " + newNode + '\n');
        
        // Ensure specified node(s) in graph (check_node_in_graph(node, TRUE)
        //   aborts when node isn't in graph.)
        //   DBGSLOW(check_node_in_graph(node, TRUE));
        if (replaceWithOther) {
            check_node_in_graph(newNode, true);
            // 'node' is to be replaced with 'newNode'.  This is only legal when
            //   'newNode' is NOT an ancester of 'node'.
            // The following function is only called once, as newNode is NULL in
            //   all recursive calls.
            //      DBG(if (check_node_reachable(newNode, node))
            //	    err << "DecisionTree::delete_subtree: attempt to replace a "
            //	       "node with its own ancestor" << fatal_error);
        }
        
        Edge iterEdge;
        Edge oldEdge;
        if (deleteNode) {
            // If 'node' is to be deleted, remove the edges from its parent(s).
            iterEdge = node.first_in_edge();
            while (iterEdge != null) {
                oldEdge = iterEdge;
                iterEdge = oldEdge.in_succ(oldEdge);
                //	 oldEdge.entry() = null;
                cGraph.del_edge(oldEdge);
            }
            MLJ.ASSERT(node.indeg() == 0,"DecisionTree.delete_subtree: node.indeg() != 0");
        }
        
        // 'node' is to be replaced with 'newNode'.  That means that the
        //   current incoming edges to 'newNode' are extraneous, and need
        //   to be removed.
        if (replaceWithOther) {
            iterEdge = newNode.first_in_edge();
            while (iterEdge != null) {
                oldEdge = iterEdge;
                iterEdge = oldEdge.in_succ(oldEdge);
                Node parentNode = oldEdge.source();
                logOptions.LOG(5, " 4. Removing parent " + parentNode + " from " + newNode
                + " (deleting edge " + oldEdge + ")" + '\n');
                //	 cGraph[oldEdge] = null;
                cGraph.del_edge(oldEdge);
            }
            MLJ.ASSERT(newNode.indeg() == 0,"DecisionTree.delete_subtree: newNode.indeg() != 0");
        }
        // Disconnect 'node' (the old node) from all outgoing edges.  Save references
        //   to the targets of these edges so we can (effectively) follow them,
        int numChildren = node.outdeg();
        int currentChild = 0;
        
        MLJ.ASSERT(numChildren >= 0,"DecisionTree.delete_subtree: numChildren < 0");
        // Declared before the loop because we use it after the if
        //    for replaceWithSelf.
        Node[] children = new Node[numChildren];
        if (numChildren > 0) {
            // We're not a leaf, we've got children to delete.
            //    a) Copy the (references to the) children nodes.
            //    b) Delete the edges.
            iterEdge = node.first_adj_edge();
            while (iterEdge != null) {
                logOptions.LOG(5, " 7. Disconnecting edge " + iterEdge
                + " from node " + node + " to its child " + '\n'
                +  iterEdge.target() + '\n');
                oldEdge = iterEdge;
                iterEdge = oldEdge.adj_succ();
                // Save the other node attached to this edge.
                Node childNode = oldEdge.target();
                children[currentChild++] = childNode;
                // Delete the connection.
                //	 cGraph[oldEdge] = null;
                cGraph.del_edge(oldEdge);
            }
        }
        MLJ.ASSERT(currentChild == numChildren,"DecisionTree.delete_subtree: currentChild != numChildren");
        MLJ.ASSERT(node.outdeg() == 0,"DecisionTree.delete_subtree: node.outdeg() != 0");
        
        // Delete the node.
        if (deleteNode) {
            logOptions.LOG(5, " 8. Deleting the node " + node + '\n');
            MLJ.ASSERT(node.indeg() == 0,"DecisionTree.delete_subtree: node.indeg() != 0");
            MLJ.ASSERT(node.outdeg() == 0,"DecisionTree.delete_subtree: node.outdeg() != 0");
            //      cGraph[node] = null;
            cGraph.del_node(node);
        }
        else if (replaceWithOther) {
            // Delete 'newNode' after moving all its children over to 'node',
            //  and assigning its categorizer to 'node'.
            cGraph.assign_categorizer(node, newNode);
            iterEdge = newNode.first_adj_edge();
            while (iterEdge != null) {
                oldEdge = iterEdge;
                iterEdge = oldEdge.adj_succ();
                Node childNode = oldEdge.target();
                AugCategory aug = new
                AugCategory(cGraph.edge_info(oldEdge).num(),
                cGraph.edge_info(oldEdge).description());
                cGraph.new_edge(node, childNode, aug);
                //	 cGraph[oldEdge] = null;
                cGraph.del_edge(oldEdge);
            }
            MLJ.ASSERT(newNode.indeg() == 0,"DecisionTree.delete_subtree: newNode.indeg() != 0");
            MLJ.ASSERT(newNode.outdeg() == 0,"DecisionTree.delete_subtree: newNode.outdeg() != 0");
            //      cGraph.entry(newNode) = null;
            cGraph.del_node(newNode);
            // Re-assign the levels of each node in the subtree we just moved.
            if (get_graph().node_info(node).level() != CGraph.DEFAULT_LEVEL)
                assign_subtree_levels(node, get_graph().node_info(node).level());
        }
        // Recurse--all children must delete themselves.
        for (currentChild = 0; currentChild < numChildren; currentChild++) {
            logOptions.LOG(5, " 9. Now to delete child " + currentChild + " of "
            + numChildren + " children" + '\n');
            delete_subtree(children[currentChild], null);
        }
    }
    
    /** Creates NodeInfo objects for every Node in the branch starting at the
     * given Node and assigns each NodeInfo its appropriate level in the tree.
     * @param node The base node where level assignment will start.
     * @param baseLevel The initial level for the base Node.
     */
    public void assign_subtree_levels(Node node, int baseLevel) {
        //   MLJ.ASSERT(baseLevel != DEFAULT_LEVEL);
        NodeInfo rootInfo = cGraph.node_info(node);
        logOptions.LOG(5, "Replacing level "+rootInfo.level()+" with "+baseLevel+'\n');
        cGraph.node_info(node).set_level(baseLevel);
        Edge iterEdge;
        Edge oldEdge;
        iterEdge = node.first_adj_edge();
        int nextLevel;
        //   if (get_categorizer(node).class_id() == CLASS_MULTI_SPLIT_CATEGORIZER)
        //      nextLevel = baseLevel;
        //   else
        nextLevel = baseLevel + 1;
        while (iterEdge != null) {
            oldEdge = iterEdge;
            iterEdge = oldEdge.adj_succ();
            Node childNode = oldEdge.target();
            assign_subtree_levels(childNode, nextLevel);
        }
    }
    
    /***************************************************************************
  Display this DecisionTree.
@param
@param
@param
@param
     ***************************************************************************
   public void display(boolean hasNodeLosses, boolean hasLossMatrix,
      Writer stream, DisplayPref dp)
   {
      stream.write(display(hasNodeLosses, hasLossMatrix, dp));
   }
 
/***************************************************************************
  Display this DecisionTree.
@param
@param
@param
     ***************************************************************************
   public String display(boolean hasNodeLosses, boolean hasLossMatrix,
      DisplayPref dp)
   {
      String return_value = new String();
   // Note that if the display is XStream, our virtual function gets it
//   if (stream.output_type() == XStream ||
//      dp.preference_type() != DisplayPref::TreeVizDisplay)
//      RootedCatGraph.display(hasNodeLosses, hasLossMatrix, stream, dp);
      else
      {
         String dataName = stream.description() + ".data";
         MLCOStream data(dataName);
         convertToTreeVizFormat(stream, data, dp, hasNodeLosses, hasLossMatrix);
      }
   }
     */
    
/*
/***************************************************************************
  Displays the DecisionTree in TreeVizFormat.
@param
@param
@param
@param
@param
 ***************************************************************************
   public void convertToTreeVizFormat(Writer conf, Writer data,
      DisplayPref displayPref,
      boolean hasNodeLosses,
      boolean hasLossMatrix) throws IOException
   {
      Node rootNode = get_root(true);
 
      NodeCategorizer cat = get_categorizer(rootNode);
      Schema schema = cat.get_schema();
      NominalAttrInfo nai = schema.nominal_label_info();
      int numLabelValues = nai.num_values();
      MLJ.ASSERT(numLabelValues >= 1,"DecisionTree::convertToTreeVizFormat:numLabelValues < 1");
   // Avoid log of 1, which is a scale of zero, and causes division
   //    by zero.
      double scale = MLJ.log_bin(Math.max(numLabelValues, 2));
      int[] permLabels = schema.sort_labels(); // permuted labels
 
      boolean dispBackfitDisks =
         displayPref.typecast_to_treeViz().get_display_backfit_disks();
      write_subtree(get_log_options(), scale, data, permLabels,
         Globals.EMPTY_STRING, Globals.EMPTY_STRING, this, rootNode,
         dispBackfitDisks, hasNodeLosses, hasLossMatrix);
 
      String protectedLabelName = new String(Globals.SINGLE_QUOTE + MLJ.protect(nai.name(),"`\\")
         + Globals.SINGLE_QUOTE);
 
      conf.write(minesetVersionStr + "\n");
      conf.write("# MLC++ generated file for MineSet Tree Visualizer.\n"
         + "input {\n"
         + "\t file \"" + data.description() + "\";\n"
         + "\t options backslash on;\n"
         + "\t key string " + protectedLabelName + " {\n");
 
      for (int i = 0; i < numLabelValues; i++)
      {
         conf.write("\t\t " + nai.get_value(permLabels[i]).quote());
         if (i != numLabelValues - 1)
            conf.write(",");
         conf.write("\n");
      }
      permLabels = null;
 
 
      conf.write("\t };\n"
         + "\t expression `Node label`[] separator ':';\n"
         + "\t string `Test attribute`;\n"
         + "\t string `Test value`;\n"
         + "\t float `Subtree weight` [" + protectedLabelName
         + "] separator ',' ;\n"
         + "\t float Percent [" + protectedLabelName + "] separator ',' ;\n");
      if (dispBackfitDisks)
         conf.write("\t float OriginalDist [" + protectedLabelName
           + "] separator ',' ;\n");
      conf.write("\t float Purity;\n");
 
      if (hasNodeLosses)
      {
         conf.write("\t float `Test-set subtree weight`;\n");
         if (hasLossMatrix)
            conf.write("\t float `Test-set loss`;\n"
               + "\t float `Mean loss std-dev`;\n");
         else
            conf.write("\t float `Test-set error`;\n"
               + "\t float `Mean err std-dev`;\n");
      }
 
      conf.write("}\n\n");
 
      conf.write("hierarchy {\n"
         + "\t levels `Node label`;\n"
         + "\t key `Subtree weight`;\n"
         + "\t aggregate base {\n"
         + "\t\t sum `Subtree weight`;\n");
      if (dispBackfitDisks)
         conf.write("\t\t sum `OriginalDist`;\n");
      conf.write("\t\t any Purity;\n"
         + "\t\t any `Test attribute`;\n"
         + "\t\t any `Test value`;\n");
 
      if (hasNodeLosses)
      {
         conf.write("\t\t any `Test-set subtree weight`;\n");
         if (hasLossMatrix)
            conf.write("\t\t any `Test-set loss`;\n"
               + "\t\t any `Mean loss std-dev`;\n");
         else
            conf.write("\t\t any `Test-set error`;\n"
               + "\t\t any `Mean err std-dev`;\n");
      }
 
      conf.write("\t }\n"
         + "\t options organization same;\n"
         + "}\n");
 
   // Pick the midpoint entropy color to be 3/4 versus 1/4 for two class probs.
   // This just makes the color scale much better then 50, which requires
   // 89% versus 11% to be the middle color.
      double[] typicalMix = new double[2];
      typicalMix[0] = 3;
      typicalMix[1] = 1;
      DoubleRef midPointEnt = new DoubleRef(100 - Entropy.entropy(typicalMix)*100 / scale);
      MLJ.clamp_to_range(midPointEnt, 0, 100,
         "DecisionTree::convertToTreeVizFormat: mid-point does "
         + "not clamp to range [0-100]");
 
      MLJ.ASSERT(schema.num_label_values() > 0,"DecisionTree::"
         + "convertToTreeVizFormat:schema.num_label_values() <= 0");
   // Even though nulls are never used, we want to distinguish
   // them in case somebody changes anything.  They're therefore hidden.
      conf.write("view hierarchy landscape {\n"
         + "\t height `Subtree weight`, normalize, max 5.0;\n");
      if (dispBackfitDisks)
         conf.write("\t disk height `OriginalDist`;\n");
      conf.write("\t base height max 2.0;\n"
         + "\t base label `Test attribute`;\n"
         + "\t line label `Test value`;\n"
         + "\t color key;\n");
      //   	   "\t base color legend label \"Purity\";\n"
      //     "\t base color Purity, "
      //     "colors \"red\" \"yellow\" \"green\""
      //           ", scale 0 " << midPointEnt << " 100, legend on;\n"
      //     "\t base color legend \"impure\" \"mixed\" \"pure\";\n"
      if (hasNodeLosses)
      {
         double min = 0;
         double max = 0;
         loss_min_max(this, min, max);
         if (max - min < 0.01)
         max += 0.01; // Avoid cases where both are zero and we rely
                      // on a treeviz tiebreaker (happens in mushroom).
         NodeLoss rootLoss = get_categorizer(rootNode).get_loss();
         double medColor = suggest_mid(min, max, rootLoss.totalWeight,
            rootLoss.totalLoss);
 
         if (!hasLossMatrix)
         {
            min *= 100;
            max *= 100;
            medColor *=100;
         }
 
         if (hasLossMatrix)
         conf.write("\t base color legend label \"Test-set loss\";\n"
            + "\t base color `Test-set loss`, ");
         else
            conf.write("\t base color legend label \"Test-set error\";\n"
            + "\t base color `Test-set error`, ");
 
 
         conf.write("colors \"green\" \"yellow\" \"red\""
            + ", scale " + min + " " + medColor
            + " " + max + ", legend on;\n"
            + "\t base color legend \"low ("
            + MLJ.numberToString(min,2) + ")\" \"medium ("
            + MLJ.numberToString(medColor,2) + ")\" \"high ("
            + MLJ.numberToString(max,2) + ")\";\n");
      }
      conf.write("\t options rows 1;\n"
         + "\t options root label \"\";\n"
         + "\t options initial depth 4;\n"
            // Don't show bar labels, so the level of details is far
         + "\t options lod bar label 10000;\n"
         + "\t options zero outline;\n"
         + "\t options null hidden;\n");
 
      conf.write("\t base message \"Subtree weight:%.2f, ");
      String lossMetric = hasLossMatrix ? "loss" : "error";
      String shortLossMetric = hasLossMatrix ? "loss" : "err";
      if (hasNodeLosses)
         conf.write("test-set " + lossMetric + ":%.2f+-%.2f, "
            + " test-set weight:%.2f, ");
      if (dispBackfitDisks)
         conf.write("training-set weight: %.2f, ");
      conf.write("purity:%.2f\", `Subtree weight`, ");
         if (hasNodeLosses)
      conf.write("`Test-set " + lossMetric + "`, "
         + "`Mean " + shortLossMetric + " std-dev`, "
         + "`Test-set subtree weight`, ");
      if (dispBackfitDisks)
         conf.write("`OriginalDist`, ");
      conf.write("Purity;\n");
 
      conf.write("\t message \"Subtree weight for label value:%.2f, percent:%.2f");
      if (dispBackfitDisks)
         conf.write(", training-set weight:%.2f");
      conf.write("\", `Subtree weight`, Percent");
      if (dispBackfitDisks)
         conf.write(", `OriginalDist`");
      conf.write(";\n}\n");
   }
 */
}
