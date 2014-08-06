package arithmetic.nb;

import arithmetic.shared.MLJ;

/** NBNorm is a helper class to hold the 3rd dimension of
  * an Array2 containing the parameters of the Normal
  * Density for each (continuous attribute,label) combination.
  * The hd (hasData) argument defaults to FALSE.
  */
public class NBNorm {
  /** stores the mean value of the data.
    */
  public double mean;

  /** stores the variance of the data.
    */
  public double var;

  /** If true the data in this NBNorm is valid, if false there is no valid data 
    * stored in this object.
    */ 
  public boolean hasData;

  /** This is the default constructor. All fields set to default.
    */
  public NBNorm() {
    mean = 0.0;
    var = 0.0;
    hasData = false;
 }

  /** This constructor sets fields to parameter values.
    * @param m - the mean value.
    * @param v - the variance.
    * @param hd - the has data boolean.
    */
  public NBNorm(double m, double v, boolean hd) {
    mean = m;
    var = v;
    hasData = hd;
  }

  /** The copy constructor for NBNorm.
    * @param rhs - the NBNorm to copy.
    */
  public NBNorm(NBNorm rhs) {
    this.mean = rhs.mean;
    this.var = rhs.var;
    this.hasData = rhs.hasData;
  }

  /** Sets the mean and variance.
    * @param m - the new mean.
    * @param v - the new variance.
    */
  public void set_mean_and_var(double m, double v) {
    mean = m;
    var = v;
    hasData = true;
  }

  /** Equivalent to operator==. Provided for use by NaiveBayesCat.equals.
    * @param rhs - the NBNorm to test equalitly.
    * @return true if the this is equal to rhs; false else.
    */
  public boolean equals(NBNorm rhs) {
    return (hasData == rhs.hasData && MLJ.approx_equal(mean, rhs.mean)
                && MLJ.approx_equal(var, rhs.var));
  }

}
