package arithmetic.chc;

import arithmetic.shared.Inducer;

/** Depot functions as a wrapper for multiple DataDistributors for
  * job farms. On the first call for a data file the data is loaded into
  * memory, for every call after that the DataDistributor with the data
  * returned. This accomplishes two things.
  *    1) The overhead for each job farm is reduced.
  *    2) Communication between Farmers and the Job Farm is simplified. */ 
public class Depot {
  
//  private Inducer[] inducers;
  /** An array to hold multiple DataDistributors. */
  private DataDistributor[] data = new DataDistributor[0];
  /** The file path for the data directory on the local system. */
  private String filepath;

  /** Creates a depot with the filepath specified.
    * @param filepath - the filepath of the local data directory. */
  public Depot(String filepath) {
    this.filepath = filepath;
  }

  /** Returns an inducer determined by the name given.
    * @param name - the inducers name.
    * @return the inducer object equivalent to the name. */
  public Inducer getInducer(String name) {
    return CHC.determineInducerSoft(name);
  }

  /** Searches the array of stored DataDistributors for the
    * one that matches the name given. If a match is not found
    * makeData is called to create and store a new DataDistributor
    * for the file name.
    * @param name - the name of the data file.
    * @return a DataDistributor with the data. */
  public DataDistributor getData(String name) {
    DataDistributor result = null;
    for (int i = 0; i < data.length; i++) {
      if ( (data[i].file.getName().equals(name)) && (result == null) ) {
//        System.out.println("" + data[i].file.getName());
        result = data[i];
      }
    }
    if (result == null) {
      result = makeData(name);
    }
    return result;
  }

  /** Called to make a new DataDistributor of the data in file name.
    * The DataDistributor is also added to the array of Distributors 
    * kept by class Depot.
    * @param name - the name of the datafile base and path.
    * @return the DataDistributor with the data requested by name. */
  public DataDistributor makeData(String name) {
    DataDistributor temp = new DataDistributor(filepath +"/" +name, true);
    DataDistributor[] array = new DataDistributor[data.length+1];
    for (int i = 0; i < data.length; i++) {
      array[i] = data[i];
    }
    array[array.length - 1] = temp;
    data = array;
    return temp;
  }
}
