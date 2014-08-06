package arithmetic.fss;

import java.io.Writer;

import arithmetic.shared.Basics;
import arithmetic.shared.CatTestResult;
import arithmetic.shared.DoubleRef;
import arithmetic.shared.Error;
import arithmetic.shared.Globals;
import arithmetic.shared.MLJ;
import arithmetic.shared.StatData;

public class ErrorData extends StatData {
	private double testSetError;
	private double totalTestWeight;

	// flag determines if values stored here are bounded from 0-1
	private boolean isPercent;

	public static int defaultPrecision;

	public ErrorData() {
		testSetError = Globals.UNDEFINED_REAL;
		totalTestWeight = 0;
		isPercent = true;
	}

	public ErrorData(boolean isPer) {
		testSetError = Globals.UNDEFINED_REAL;
		totalTestWeight = 0;
		isPercent = isPer;
	}

	public void set_test_set(double realError, double testWeight) {
		if (MLJ.approx_greater((float) 0, (float) testWeight))
			Error.fatalErr("ErrorData::set_test_set: test set weight ("
					+ testWeight + ") must not be negative");
		testSetError = realError;
		totalTestWeight = testWeight;
	}

	public double error(double trim) {
		if (size() != 0)
			return mean(trim);
		else
			return test_set_error();
	}

	public double error() {
		if (size() != 0)
			return mean(0);
		else
			return test_set_error();
	}

	public double std_dev(double trim) {
		if (size() != 0) {
			check_std_dev();
			return std_dev_of_mean(trim);
		} else
			return theo_std_dev();
	}

	public double std_dev() {
		if (size() != 0) {
			check_std_dev();
			return std_dev_of_mean(0);
		} else
			return theo_std_dev();
	}

	public void confidence(DoubleRef low, DoubleRef high) {
		if (size() != 0) {
			check_std_dev();
			if (isPercent)
				percentile(Basics.CONFIDENCE_INTERVAL_PROBABILITY, low, high);
			else {
				low.value = mean() - std_dev_of_mean() * 1.96;
				high.value = mean() + std_dev_of_mean() * 1.96;
			}
		} else
			theo_confidence(low, high);
	}

	public double test_set_error() {
		check_test_set();
		return testSetError;
	}

	public double theo_std_dev() {
		check_theo_std_dev();
		return CatTestResult.theoretical_std_dev(testSetError, totalTestWeight);
	}

	public void theo_confidence(DoubleRef low, DoubleRef high) {
		check_theo_std_dev();
		CatTestResult.confidence(low, high, testSetError, totalTestWeight);
	}

	public double bias(double trim) {
		return error(trim) - test_set_error();
	}

	public double bias() {
		return error(0) - test_set_error();
	}

	public boolean has_test_set() {
		return testSetError >= 0.0;
	}

	public void check_test_set() {
		if (!has_test_set())
			Error.fatalErr("ErrorData::check_test_set: test set error is undefined");
	}

	public boolean has_estimated() {
		return size() > 0;
	}

	public void check_estimated() {
		if (!has_estimated())
			Error.fatalErr("ErrorData::check_estimated: estimated error is undefined"
					+ " (no data has been inserted)");
	}

	public boolean has_std_dev() {
		return size() > 1;
	}

	public void check_std_dev() {
		if (!has_std_dev())
			Error.fatalErr("ErrorData::check_std_dev: standard deviation is undefined"
					+ " (not enough data to compute variance)");
	}

	public boolean has_theo_std_dev() {
		return (isPercent && has_test_set() && MLJ.approx_greater(
				totalTestWeight, 1.0));
	}

	public void check_theo_std_dev() {
		if (!has_theo_std_dev())
			Error.fatalErr("ErrorData::check_theo_std_dev: theoretical standard deviation "
					+ " is undefined (not enough test instances)");
	}

	public void clear() {
		super.clear();
		testSetError = Globals.UNDEFINED_REAL;
		totalTestWeight = 0;
	}

	public void append(StatData other) {
		super.append(other);
	}

	public void append(ErrorData other) {
		super.append(other);
		if (has_test_set() && other.has_test_set()) {
			if (!MLJ.approx_equal(testSetError, other.testSetError))
				Error.fatalErr("ErrorData::append: "
						+ "Attempting to append non-matching ErrorData: Test set error "
						+ testSetError + " != " + other.testSetError);
			if (!MLJ.approx_equal((float) totalTestWeight,
					(float) other.totalTestWeight))
				Error.fatalErr("ErrorData::append: "
						+ "Attempting to append non-matching ErrorData:  total test weight "
						+ totalTestWeight + " != " + other.totalTestWeight);
		} else if (other.has_test_set()) {
			testSetError = other.testSetError;
			totalTestWeight = other.totalTestWeight;
		}
	}

	public int class_id() {
		return CLASS_ERROR_DATA;
	}

	public ErrorData assign(ErrorData rhs) {
		if (this != rhs) {
			super.assign(rhs);
			testSetError = rhs.testSetError;
			totalTestWeight = rhs.totalTestWeight;
			isPercent = rhs.isPercent;
		}
		return this;
	}

	public boolean equals(StatData rhs) {
		if (!(class_id() == rhs.class_id()))
			return false;
		// obs boolean isEqual = (StatData::operator == (rhs));
		boolean isEqual = (super.equals(rhs));

		ErrorData other = (ErrorData) rhs; // safe
		isEqual = isEqual
				&& testSetError == other.testSetError
				&& MLJ.approx_equal((float) (totalTestWeight),
						(float) (other.totalTestWeight))
				&& isPercent == other.isPercent;
		return isEqual;
	}

	public void display() {
		display(Globals.Mcout, 0, defaultPrecision);
	}

	public void display(Writer stream) {
		display(stream, 0, defaultPrecision);
	}

	public void display(Writer stream, double trim) {
		display(stream, trim, defaultPrecision);
	}

	public void display(Writer stream, double trim, int precision) {
		/*
		 * try{
		 * 
		 * stream.write(push_opts);
		 * stream.write(setprecision(precision)+fixed_reals);
		 * 
		 * // if no real or estimated, indicate empty ErrorData
		 * if(!has_test_set() && !has_estimated()) stream.write("(empty)");
		 * 
		 * // otherwise display estimated error else { if(isPercent)
		 * stream.write(error(trim)*100+"%"); else stream.write(error(trim));
		 * 
		 * if(!has_estimated()) stream.write(" (test set)"); if(has_std_dev()) {
		 * DoubleRef low = new DoubleRef(); DoubleRef high = new DoubleRef();
		 * confidence(low, high); stream.write(" +- "+std_dev(trim)*100+"% (");
		 * if(isPercent) stream.write(low.value*100+"% - "+high.value*100+"%)");
		 * else stream.write(low +" - "+high+")"); } if(has_test_set()) {
		 * stream.write(".\n"); if(isPercent)
		 * stream.write("  Test Set: "+test_set_error()*100+"%"); else
		 * stream.write("  Test Set: "+test_set_error());
		 * 
		 * if(has_theo_std_dev()) { DoubleRef low = new DoubleRef(); DoubleRef
		 * high = new DoubleRef(); theo_confidence(low, high);
		 * stream.write(" +- "+theo_std_dev()*100 +"% [" +low.value*100 +"% - "
		 * +high.value*100 +"%]"); } stream.write(".  Bias: ");
		 * 
		 * if(isPercent) stream.write(bias(trim)*100+"%"); else
		 * stream.write(bias(trim)); } }
		 * 
		 * stream.write(pop_opts); }catch(IOException e){e.printStackTrace();
		 * System.exit(1);}
		 */
	}

	/*
	 * public void dot_display(MLCOStream& stream = Mcout, double trim = 0, int
	 * precision = defaultPrecision) { stream << push_opts <<
	 * setprecision(precision) << fixed_reals;
	 * 
	 * // if no real or estimated, indicate empty ErrorData if(!has_test_set()
	 * && !has_estimated()) stream << "(empty)";
	 * 
	 * // otherwise display estimated error else { stream << "est: "; stream <<
	 * trim*100 << "%"; if(has_std_dev()) stream << " +- " << std_dev(trim)*100
	 * << "%"; if(has_test_set()) { stream << "\\n" << "test set: " <<
	 * test_set_error()*100 << "%"; if(has_theo_std_dev()) stream << " +- " <<
	 * theo_std_dev() * 100 << "%"; } }
	 * 
	 * stream << pop_opts; }
	 * 
	 * public void dot_display(MLCOStream& stream = Mcout, double trim = 0, int
	 * precision = defaultPrecision) { stream << push_opts <<
	 * setprecision(precision) << fixed_reals;
	 * 
	 * // if no real or estimated, indicate empty ErrorData if(!has_test_set()
	 * && !has_estimated()) stream << "(empty)";
	 * 
	 * // otherwise display estimated error else { stream << "est: "; stream <<
	 * trim*100 << "%"; if(has_std_dev()) stream << " +- " << std_dev(trim)*100
	 * << "%"; if(has_test_set()) { stream << "\\n" << "test set: " <<
	 * test_set_error()*100 << "%"; if(has_theo_std_dev()) stream << " +- " <<
	 * theo_std_dev() * 100 << "%"; } }
	 * 
	 * stream << pop_opts; }
	 * 
	 * void dot_display(MLCOStream& stream = Mcout, double trim = 0, int
	 * precision = defaultPrecision) { stream << push_opts <<
	 * setprecision(precision) << fixed_reals;
	 * 
	 * // if no real or estimated, indicate empty ErrorData if(!has_test_set()
	 * && !has_estimated()) stream << "(empty)";
	 * 
	 * // otherwise display estimated error else { stream << "est: "; stream <<
	 * trim*100 << "%"; if(has_std_dev()) stream << " +- " << std_dev(trim)*100
	 * << "%"; if(has_test_set()) { stream << "\\n" << "test set: " <<
	 * test_set_error()*100 << "%"; if(has_theo_std_dev()) stream << " +- " <<
	 * theo_std_dev() * 100 << "%"; } }
	 * 
	 * stream << pop_opts; }
	 * 
	 * public void dot_display(MLCOStream& stream = Mcout, double trim = 0, int
	 * precision = defaultPrecision) { stream << push_opts <<
	 * setprecision(precision) << fixed_reals;
	 * 
	 * // if no real or estimated, indicate empty ErrorData if(!has_test_set()
	 * && !has_estimated()) stream << "(empty)";
	 * 
	 * // otherwise display estimated error else { stream << "est: "; stream <<
	 * trim*100 << "%"; if(has_std_dev()) stream << " +- " << std_dev(trim)*100
	 * << "%"; if(has_test_set()) { stream << "\\n" << "test set: " <<
	 * test_set_error()*100 << "%"; if(has_theo_std_dev()) stream << " +- " <<
	 * theo_std_dev() * 100 << "%"; } }
	 * 
	 * stream << pop_opts; }
	 * 
	 * 
	 * 
	 * 
	 * /*************************************************************************
	 * ** This class has no access to a copy constructor.
	 * *************************************************************************
	 */
	private ErrorData(ErrorData source) {
	}

	/***************************************************************************
	 * This class has no access to an assign method.
	 ***************************************************************************/
	// private void assign(ErrorData source){}
}