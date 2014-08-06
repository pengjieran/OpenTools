package arithmetic.shared;

/** The Matrix class contains functions useful for manipulation of double arrays
 * in MLJ.
 * @author James Louis Java Implemtation.
 */
public class Matrix {
    
    /** Sums the columns of a double array of double values and returns the sums as an
     * array of summations.
     * @param returnRow The array containing the summations. Altered as a result of this function.
     * @param matrix The double array containing the values to be summed.
     */
    public static void sum_cols(double[] returnRow,double[][] matrix) {
        int numRows = matrix.length;
        if (!(numRows == 0)) {
            int numCols = matrix[0].length;
            if (!(numCols == 0)) {
                if (numCols != returnRow.length)
                    Error.fatalErr( "SplitScore::sum_cols(): output array of wrong size--"+returnRow.length+ " instead of "+numCols);
                for (int col = 0; col < numCols; col++)
                    for (int row = 0; row < numRows; row++)
                        returnRow[col] += matrix[row][col];
            }
            else
                Error.fatalErr( "SplitScore::sum_cols(): empty array");
        }
        else
            Error.fatalErr( "SplitScore::sum_cols(): empty array");
    }
    
    /** Sums the rows of a double array of double values and returns the sums as an
     * array of summations.
     * @param returnColumn The array containing the summations. Altered as a result of this function.
     * @param matrix The double array containing the values to be summed.
     */
    public static void sum_rows(double[] returnColumn, double[][] matrix) {
        int numRows = matrix.length;
        if (!(numRows == 0)) {
            int numCols = matrix[0].length;
            if (!(numCols == 0)) {
                
                if (numRows != returnColumn.length)
                    Error.fatalErr( "Matrix::sum_rows(): output array of wrong size--"+returnColumn.length+ " instead of "+numRows);
                for (int row = 0; row < numRows; row++)
                    for (int col = 0; col < numCols; col++)
                        returnColumn[row] += matrix[row][col];
            }
            else
                Error.fatalErr( "Matrix::sum_rows(): empty array");
        }
        else
            Error.fatalErr( "Matrix::sum_rows(): empty array");
    }
    
    /** Sums all of the values in a double array of double values.
     * @param matrix The double array to be summed.
     * @return The summation of all the elements in the matrix.
     */
    public static double total_sum(double[][] matrix) {
        double total = 0;
        int numRows = matrix.length;
        if (!(numRows == 0)) {
            int numCols = matrix[0].length;
            if (!(numCols == 0)) {
                int col;
                for(int row = 0; row < numRows; row++)
                    for(col = 0; col < numCols; col++)
                        total += matrix[row][col];
                return total;
            }
        }
        Error.fatalErr( "Array2<Element>::total_sum(): empty array");
        return total;
    }
    
    /** Initializes the double array to the given value.
     * @param init_value The value to be initialized to.
     * @param matrix The double array to be initialized.
     */
    public static void initialize(double init_value, double[][] matrix) {
        int i,j;
        if (matrix.length > 0)
            if (matrix[0].length > 0) {
                for(j = 0; j < matrix.length; j++)
                    for(i = 0; i < matrix[0].length; i++)
                        matrix[j][i] = init_value;
            }
    }
    
    /** Copies the double array into a new double array.
     * @param matrix The double array to be copied.
     * @return The new copy of the matrix.
     */
    public static double[][] copy(double[][] matrix) {
        double[][] thecopy = new double[matrix.length][matrix[0].length];
        for(int i = 0; i < matrix.length; i++)
            for(int j = 0; j < matrix[0].length; j++)
                thecopy[i][j] = matrix[i][j];
        return thecopy;
    }
    
    /** Returns the maximum value in the specified row of the given double array.
     * @param row The row to be searched.
     * @param idx The index value of the greatest value.
     * @param matrix The double array containing the row searched.
     * @return The maximum value.
     */
    static public double max_in_row(int row, int idx, double[][] matrix) {
        if (matrix.length == 0 || matrix[0].length == 0)
            Error.fatalErr( "Array2<Element>::max_in_row() - empty array");
        double max = matrix[row][0];
        idx = 0;
        for (int i = 1; i < matrix[row].length; i++)
            if (matrix[row][i] > max) {
                max = matrix[row][i];
                idx = i;
            }
        return(max);
    }
    
    
    /** Returns the minimum value in the specified row of the given double array.
     * @param row The row to be searched.
     * @param idx The index value of the smallest value.
     * @param matrix The double array containing the row searched.
     * @return The minimum value.
     */
    static public double min_in_row(int row, int idx, double[][] matrix) {
        if (matrix.length == 0 || matrix[0].length == 0)
            Error.fatalErr( "Array2<Element>::min_in_row() - empty array");
        double min = matrix[row][0];
        idx = 0;
        for (int i = 1; i < matrix[row].length; i++)
            if (matrix[row][i] < min) {
                min = matrix[row][i];
                idx = i;
            }
        return(min);
    }
}
