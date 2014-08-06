package arithmetic.shared;
import java.io.Writer;

/** Every file uses the members of the basics class.  Here we define some constants, an
 * initialization function and termination function. Note that since
 * initialization order of static instances is not defined within different
 * files, it is important that all initialization be done here, or at least
 * that any other initialization will not depend on anything initialized here.
 * This is especially a problem with constructors that call fatal_error and
 * output to err, since err may not be initialized at that stage. Note the
 * dependency between LONG_MAX and MAX_LONG_LEN.
 *
 * @author James Louis	8/16/2001	Ported to Java.
 * @author Eric Eros		5/16/97	Created MLC class
 * @author Ronny Kohavi	7/13/93	Initial revision (.c)
 * @author Ronny Kohavi	8/26/93	Initial revision (.h)
 */

public class Basics {
    //Compilation directives - JAL
    /** TRUE if DBG sections of code should be executed, FALSE otherwise. Default is FALSE**/
    public static boolean DBG = false;
    /** TRUE if DBGSLOW sections of code should be executed, FALSE otherwise. Default is FALSE**/
    public static boolean DBGSLOW = false;
    
    /** The probability interval value used for calculating confidence.
     */    
    public static double CONFIDENCE_INTERVAL_PROBABILITY;
    
    // MLC++ - Machine Learning Library in -*- C++ -*-
    // See Descrip.txt for terms and conditions relating to use and distribution.
    
    // This is an include file.  For a full description of the class and
    // functions, see the file "basicCore.c".
    
    
    /** Default value for opening protections.
     */
    public static int defaultOpenProt;
    /** The prefix character for displaying error strings.
     */
    public static char ERROR_PREFIX;
    /** The character used to display when a line is being wrapped.
     */
    public static char WRAP_INDENT;
    /** Bad exit status for program.
     */
    public static int BAD_STATUS;
    /** Value used for error catching.
     */
    public static String fatal_expected;
    /** TRUE if mineset is being used, FALSE otherwise.
     */
    public static boolean mineset; // for those messages specific to SGI's MineSet.
    //obs   public static char *minesetVersionStr;
    /** The version of mineset being used.
     */
    public static String minesetVersionStr;
    
    //obs   public static char *err_text;
    /** Standard error text.
     */
    public static String err_text;
    //obs   public static MLCOStream err;
    /** Writer for output of error information.
     */
    public static Writer err;
    
    // note that MString.c assumes that Real is typedef'd to a double.
    /** Maximum value for real numbers.
     */
    public static double REAL_MAX;
    /** Minimum number for real numbers.
     */
    public static double REAL_MIN;
    /** Maximum number for stored real numbers.
     */
    public static float STORED_REAL_MAX;
    /** Minimum number for stored real numbers.
     */
    public static float STORED_REAL_MIN;
    
    // For cases where a variable's value may be used as a flag to indicate
    //   it has not yet been set.  These are extreme negative values.
    //   See basicCore.c
    /** Number that designates undefined variance values.
     */
    public static double UNDEFINED_VARIANCE;
    /** Number that represents undefined real values.
     */
    public static double UNDEFINED_REAL;
    /** Number that represents undefined integer values.
     */
    public static int  UNDEFINED_INT;
    
    // Default wrap width for an MStream, this guarantees that it is off initially
    /** Number of characters allowed before a line is word wrapped.
     */    
    public static int DEFAULT_WRAP_WIDTH;
    // Default wrap prefix for an MStream -3 spaces
    //obs extern const char* DEFAULT_WRAP_PREFIX;
    /** Prefix added to word wrapped lines.
     */    
    public static String DEFAULT_WRAP_PREFIX;
    
/*
 
// These are defined here because of a Bug in CFront that declared
//   INT_MIN wrong (without (int)).  What happens is that 2147483648
//   does not fit in int, so it is made unsigned, then unary minus subtracts
//   from max unsigned int to get the same number.
#ifdef CFRONT
#define SHORT_MAX      32767
#define LONG_MAX       2147483647
#define INT_MAX        LONG_MAX
 
// defining LONG_MIN as -2147483648 does not work because the minus is
//   a unary operator and the number causes it to be unsigned.  We can
//   cast to (long), but ObjectCenter gives a warning... This method
//   is adapted from GNU limits.h
#define SHORT_MIN      (-SHORT_MAX-1)
#define LONG_MIN       (-LONG_MAX-1)
#define INT_MIN        LONG_MIN
 
// MAX_LONG_LEN is used to define array dimensions so it cannot be
//    extern const int.
// LONG_MAX is +2147483647 which is 10 digits +1 for sign.
// Note that this is an upper bound and MAX_LONG can be lower.
#define MAX_LONG_LEN   11
// Similar to LONG_MAX, 5 digits +1 for sign.
#define MAX_SHORT_LEN 6
#define	UCHAR_MAX     255  // max value of an "unsigned char"
 
// Just use standard "limits.h" if we're not using CFRONT.
//   We still need to set some of the symbols
#else
#include <limits.h>
#define SHORT_MAX      32767
#define SHORT_MIN      (-SHORT_MAX-1)
#define MAX_LONG_LEN   11
#define MAX_SHORT_LEN 6
#endif
 
 
// In the following, DBL_DIG and FLT_DIG are defined in limits.h
// This is needed to set the precision of the stream when doing Real I/O
//   This value is trunc((DSIGNIF - 1) / log2(10)),
//   or trunc((FSIGNIF - 1) / log2(10)), where DSIGNIF is the
//   number of significant bits for a double (defined in values.h),
//   FSIGNIF is likewise defined for a float, and 1 is subtracted for
//   the sign bit.
   public static int REAL_MANTISSA_LEN = DBL_DIG; // 15
   public static int STORED_REAL_MANTISSA_LEN = FLT_DIG; //  6
   public static int MAX_ECVT_DIGITS = DBL_DIG+2; // 17
   public static int MAX_ECVT_WIDTH = 84;  // from man page for ecvt_r
 */
    
    // Note:  Should the size of Real and/or StoredReal change, the above
    //   numbers must change correspondingly.  Also, REAL_EPSILON and
    //   STORED_REAL_EPSILON (in basicCore.c) must change.
    
    // Because Reals can be displayed with full digits,
    // MAX_REAL_LEN is set to a large number
    // MAX_REAL_DIGITS specifies the maximum number of digits which may
    //   appear before the decimal point in a Real.
    /** Maximum number of digits for real numbers.
     */    
    public static int MAX_REAL_DIGITS = 800;
    /** Maximum length for real number display.
     */    
    public static int MAX_REAL_LEN = 2048;
    /** Maximum length for stored real number display.
     */    
    public static int MAX_STORED_REAL_LEN = 2048;
    /** MAX_WIDTH is the maximum width allowed for real output.
     */    
    public static int MAX_WIDTH = 2048;
    
    
    
    
    
/*
#ifndef _basic_h
#define _basic_h 1
 
#include <machine.h>
char is_compiled_fast(); // returns TRUE if library compiled in fast mode
 
#include <stdlib.h>
#include <math.h>
 
// There's sometimes a need to give a reference to something that
//   is invalid.  the SGI compiler warns about NULL ref, so this
//   avoids the warning and is just as bad, i.e., an access will
//   cause program to abort.
#define NULL_REF 1
 
// make sure nobody has defined Bool via #define.  Xlib.h does this.
#ifdef Bool
#error Bool is already defined.  Please undefine before including basics.h
#endif
 
// knock out any alternate TRUE or FALSE if defined
#ifdef TRUE
#undef TRUE
#endif
#ifdef FALSE
#undef FALSE
#endif
 
#ifdef _BOOL
  typedef bool Bool;  // After George Boole
# define TRUE true
# define FALSE false
#else
  typedef char Bool;
# define TRUE Bool(!0)
# define FALSE Bool(0)
#endif
 
#ifndef FAST
#define DBG(stmts) if (GlobalOptions::debugLevel >= 1) {stmts;} else
#else
#define DBG(stmts)
#endif
 
#ifdef TEMPL_MAIN
   #define TEMPL_GENERATOR(name) int main()
#else
   #define TEMPL_GENERATOR(name) int name()
#endif
 
 
// DBGSLOW() should only be used for especially expensive code.
#ifndef FAST
#define DBGSLOW(stmts) if (GlobalOptions::debugLevel >= 2) {stmts;} else
#else
#define DBGSLOW(stmts)
#endif
 
// DBG_DECLARE() is intended for use in class or function declarations,
//   where "if" statements are not allowed.
// Note that when using this macro, the semicolon must be INSIDE,
//   since empty declarations are not allowed (r9.2)
#ifndef FAST
#define DBG_DECLARE(stmts) stmts
#else
#define DBG_DECLARE(stmts)
#endif
 
 
 
typedef double Real;
typedef float StoredReal; // for arrays etc.
 
// To prevent accidental copy construction, most copy constructors
// take a second "dummy" argument.  Initially this dummy argument was
// forced to be an integer and that integer was checked to be 0,
// The new standard is that it would be much simpler to have the dummy
// argument be an actual enumerated type.
// Example usage:
//	Foo.h: Foo(const Foo& foo, CtorDummy);
//	Client.h: Foo foo2(foo1, ctorDummy);
enum CtorDummy {ctorDummy=0};
 
// Also, there is an enumerated type for dummy arguments.
enum ArgDummy {argDummy=0};
 
// Declarations for the parallel execution using pthread library.  Currently
// only entropy discretizors are parallelized.  Do not define this constant if
// you do not want to use pthreads
 
// #define PTHREADS
 
#ifdef PTHREADS
#include <malloc.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/shm.h>
#include <sys/types.h>
#include <sys/resource.h>
#include <sys/prctl.h>
#define PTHR_DECL(stmts)	stmts
#else
#define PTHR_DECL(stmts)
#endif // PTHREADS
 
#include <time.h>
#include <MString.h>
#include <GlobalOptions.h>
 
// Due to archaic 8+3 PC naming conventions, strstream.h
// is sometimes called strstrea.h
#ifdef PC_MSVC
#include <strstrea.h>
#else
#include <strstream.h>
#endif
 
#ifdef __GNUC__
typedef timespec timespec_t;
#endif
 
/* G++ and other smart and proper compilers define null as a variable, to
 * escape type sensitive situations. However, this breaks operator overloading
 * for comparason operators that require NULL to be 0, (traditional C++)
 *
#undef NULL
#define NULL 0
 
 
#include <error.h>
 
// MLCInit class to force consistent initialization order
class MLCInit {
   NO_DEFAULT_OPS(MLCInit);
public:
   // Public data
   static int count;
   // Methods
   MLCInit() { if(count++ == 0) startup(); }
   ~MLCInit() { if(--count == 0) shutdown(); }
 
   void startup();
   void shutdown();
};
 
// static instance of MLCInit:  used for force initialization order.
// note that this instance is DEFINED here in the header file; this
// forces its definition to occur BEFORE all other definitions in
// each file which includes basics.h, and thus before all other
// static initialization.
static MLCInit mlcInit;
 
// Enumerated type to declare constructors which indicate that a static
// instance should be initialized externally
enum UseExternalCtor { useExternalCtor = 1 };
 
// This macro causes a fatal_error if the given condition is FALSE.
// The behavior is similar to the C/C++ assert (see include/assert.h).
// The macro is executed as a DBG statement.
 
// These allow sharing the same message string across files to
//   save space (also help with gp_overflow(5) errors.
extern const char *ASSERT_FAILURE_MSG;
extern const char *ASSERT_FILE_MSG;
extern const char *ASSERT_LINE_MSG;
 
// ASSERTs for debugging purposes should be in DBG().
#define ASSERT(stmt) \
      ((stmt)?((void)0): \
       (void)(err << ASSERT_FAILURE_MSG << # stmt <<  \
        ASSERT_FILE_MSG << __FILE__ << \
        ASSERT_LINE_MSG << __LINE__ << fatal_error))
 
// Old version below caused compiler core dump on SGI and ConstCat
//      (void)((stmt) ||                                                     \
//      ((err << "MLC++ internal error: assertion failed: " # stmt   \
//	", file " << __FILE__ << ", line " << __LINE__ << fatal_error), 0))
 
 
#define ABORT_IF_REACHED \
   err << "MLC++ internal error: unexpected condition in file " \
       << __FILE__ << ", line " << __LINE__ << fatal_error
 
 
#include <MLCStream.h>
extern MLCIStream Mcin;
extern MLCOStream Mcout;
extern MLCOStream Mcerr;
 
// DBG*(code) needs a trailing semicolon outside, i.e. DBG(x=3);
// The last statement inside does not need a semicolon.  The semicolon
//   outside allows proper indentation in Emacs C++ mode.
//   and is also required inside IFs to generate an empty statement.
 
// The "else" is so DBG will work even if it is inside an if statement.
//   (the else in the definition assures proper matching of the else
//    in the code, and also makes use of the semicolon...):
//
//       if (foo)
//          DBG(bar);
//       else
//          kuku();
 
// This is the size of the buffer used for reading and writing files.
// It must be defined "const" because it is used to dimension arrays.
static const int IO_BUFFER_SIZE = 1024;
 
// This is the maximum length of a string read from a file
static const int MAX_INPUT_STRING_SIZE = 1000;
 
extern const int        DEFAULT_PRECISION;
 
extern const Real       REAL_EPSILON;
extern const StoredReal STORED_REAL_EPSILON;
 
 
// This is the maximum length of a quoted string we can read from a file
static const int MAX_QUOTED_STRING = 200000;
 
// See basicCore.c for these values.
extern const char *WHITE_SPACE;
extern const float  DEFAULT_PAGE_X;
extern const float  DEFAULT_PAGE_Y;
extern const float  DEFAULT_GRAPH_X;
extern const float  DEFAULT_GRAPH_Y;
extern MLCOStream*  globalLogStream;
extern const Real CONFIDENCE_INTERVAL_PROBABILITY;
extern const Real CONFIDENCE_INTERVAL_Z; // value such that area under standard
                                       // normal curve going left & right Z,
                                       // has CONFIDENCE_INTERVAL_PROBABILITY
 
class OptionServer;
extern OptionServer *optionServer;
 
#  if defined(__CENTERLINE__)
   extern "C" { int centerline_true(void); }
#  endif
 
#define RCSID(str)
 
// DECLARE_DISPLAY declares operator<< for the given class (.h file)
// DEF_DISPLAY defines it (for use in .c file)
// For templated classes do
//   template <class T> DEF_DISPLAY(DblLinkList<T>)
//
// Note: you should NOT place a semicolon after either DECLARE_DISPLAY or
//   DEF_DISPLAY when you use it in code.
#define DECLARE_DISPLAY(class) \
   MLCOStream& operator<<(MLCOStream& s, const class& c);
 
#define DEF_DISPLAY(class) \
   MLCOStream& operator<<(MLCOStream& s, const class& c) \
   {c.display(s); return s;}
 
extern const MString TRUE_STRING;
extern const MString FALSE_STRING;
extern const MString EMPTY_STRING;
const MString& bool_to_string(Bool boolValue);
 
inline Real square_real(Real x) {return x*x;}
inline double square_double(double x) {return x*x;}
Real Mround(Real x, int digits);
 
MString get_env_default(const MString& envVarName, const MString& defaultName);
MString get_env(const MString& envVarName);
int get_env_int(const MString& envVarName, int val);
Bool get_env_bool(const MString& envVarName, Bool val);
 
 
// Log base 2.  Solaris doesn't have log2().
double log_bin(double num);
 
// Lose the least significant digits of Reals in an array.  Sometimes things
// just don't add up right...
template <class Element> class Array;
template <class Element> class Array2;
extern void lose_lsb_array(Array<Real>& a);
 
template<class T> inline void mlc_swap(T& a, T& b)
   {T temp; temp = a; a = b; b = temp;}
 
// MLCThreads.h is included in all cases in order to define some
//   stub functions.
#include <MLCThreads.h>
 
// Note:  The following concerns performance in using the MLC methods that
//   are passed strings.
// The following class (MLC) has several methods that are passed
//   error messages.  These error messages used to be passed as MString&s.
//   If normal invocation is:
//      class_method(other_args, "This is an error message");
//   the MString(const char*) constructor/destructor pair was invoked on
//   each call.  When this occured inside a tight loop the overhead
//   could be quite high.  If function a was calling function b in a tight
//   loop, and b was invoking one of the said class methods, we couldn't
//   even expect any help from a smart compiler to speed things up.
//   What was more disappointing about the performance loss was that no more
//   than one MString ever needed to be constructed--no such MString was used
//   unless the method immediately aborted.  There were only 2 ways to speed
//   this up, either
//   a) invoke the methods with static const MStrings, or
//   b) declare the calling arguments to be const char* const, so an MString
//   constructor was not called more than once.
//   We selected b.
 
// Name-space class for min/max, and clamping reals within ranges (with
//   abort on more than clampingEpsilon from the range), and comparison of
//   rational numbers with epsilons used to nullify small differences.
class MLC {
   NO_DEFAULT_OPS(MLC);
private:
   // Member data
   // Amount by which values to be clamped my exceed the range, without
   //   aborting.
   Real clampingEpsilon;
   StoredReal storedClampingEpsilon;
   static Real realEpsilon;
   static StoredReal storedRealEpsilon;
 
   static int dribbleState;
   // dribble each 10% = 100% / 10
   //@@ removed from kit
   // static const int DRIBBLE_SCALE = 10;
 
   // performance measurement
   timespec_t start, stop;
   const MString* perfExpName;
 
public:
   //@@ GNU gets very upset when real_epsilon() and stored_real_epsilon()
   //     in the constructor.  They don't seem to be defined at that point.
   MLC(Real clampEpsilon = realEpsilon * 10,
       StoredReal storedClampEpsilon = storedRealEpsilon * 10)
      : clampingEpsilon(clampEpsilon),
        storedClampingEpsilon(storedClampEpsilon) {}
   void set_epsilon_multiplier(int mult)
       {ASSERT(mult >= 0);
       clampingEpsilon = mult * real_epsilon();
       storedClampingEpsilon = mult * stored_real_epsilon();}
 
   static Real real_epsilon() { return realEpsilon;}
   static StoredReal stored_real_epsilon() { return storedRealEpsilon;}
   static void get_epsilon(Real & val) {val = realEpsilon;}
   static void get_epsilon(StoredReal & val) {val = storedRealEpsilon;}
   // Amount by which values to be clamped may exceed the range, without
   //   aborting.
   Real clamping_epsilon() const {return clampingEpsilon;}
   StoredReal stored_clamping_epsilon() const {return storedClampingEpsilon;}
 
   void get_clamping_epsilon(Real& val) const {val = clampingEpsilon;}
   void get_clamping_epsilon(StoredReal& val) const
      {val = storedClampingEpsilon;}
 
   // approx_less() and approx_greater() are disjoint with approx_equal().
   Bool approx_equal(Real lhs, Real rhs, int precMultiplier = 1) const
      {DBG(ASSERT(precMultiplier >= 0));
      return (fabs(lhs - rhs) <=
              clampingEpsilon * precMultiplier *
              MLC::max(Real(1), MLC::min(fabs(lhs), fabs(rhs))));
      }
   Bool approx_greater(Real lhs, Real rhs, int precMultiplier = 1) const
      {return (approx_equal(lhs, rhs, precMultiplier) ? FALSE : (lhs > rhs));}
   Bool approx_less(Real lhs, Real rhs, int precMultiplier = 1) const
      {return (approx_equal(lhs, rhs, precMultiplier) ? FALSE : (lhs < rhs));}
   void verify_approx_equal(Real lhs, Real rhs, const char* const errMsg,
                            int precMultiplier = 1) const
   {if (!approx_equal(lhs, rhs, precMultiplier))
      err << errMsg << endl << lhs << " versus " << rhs << fatal_error;
   }
   Bool approx_equal(StoredReal lhs, StoredReal rhs,
                     int precMultiplier = 1) const
   {DBG(ASSERT(precMultiplier >= 0));
   return (fabs(lhs - rhs) <=
           storedClampingEpsilon * precMultiplier *
           MLC::max(StoredReal(1),
                    StoredReal(MLC::min(fabs(lhs), fabs(rhs)))));
   }
   Bool approx_greater(StoredReal lhs, StoredReal rhs,
                       int precMultiplier = 1) const
      {return (approx_equal(lhs, rhs, precMultiplier) ? FALSE : (lhs > rhs));}
   Bool approx_less(StoredReal lhs, StoredReal rhs,
                    int precMultiplier = 1) const
      {return (approx_equal(lhs, rhs, precMultiplier) ? FALSE : (lhs < rhs));}
   void verify_approx_equal(StoredReal lhs, StoredReal rhs,
                            const char* const errMsg,
                            int precMultiplier = 1) const
   {if (!approx_equal(lhs, rhs, precMultiplier))
      err << errMsg << endl << lhs << " versus " << rhs << fatal_error;
   }
 
   Real real_max(const Array<Real> &realArray);
   Real real_max(const Array<Real> &realArray, int &idx);
   Real real_min(const Array<Real> &realArray);
   Real real_min(const Array<Real> &realArray, int &idx);
 
   // These are approximate equality functions for Arrays of Reals
   // and StoredReals.
   Bool approx_equal(const Array<Real>& a1, const Array<Real>& a2,
                     int precMult = 1);
   Bool approx_equal(const Array<StoredReal>& a1,
                     const Array<StoredReal>& a2, int precMult = 1);
   //void verify_approx_equal(const Array<Real>& a1, const Array<Real>& a2,
   //		    const char* const errMsg, int precMult = 1);
   //void verify_approx_equal(const Array<StoredReal>& a1,
   //		    const Array<StoredReal>& a2,
   //		    const char* const errMsg, int precMult = 1);
 
   // These are approximate equality functions for Array2s of Reals
   // and StoredReals.
   Bool approx_equal(const Array2<Real>& a1, const Array2<Real>& a2,
                     int precMult = 1);
   Bool approx_equal(const Array2<StoredReal>& a1,
                     const Array2<StoredReal>& a2, int precMult = 1);
   //void verify_approx_equal(const Array2<Real>& a1, const Array2<Real>& a2,
   //		    const char* const errMsg, int precMult = 1);
   //void verify_approx_equal(const Array2<StoredReal>& a1,
   //		    const Array2<StoredReal>& a2,
   //		    const char* const errMsg, int precMult = 1);
 
   // The verify_strictly_XXX functions do not use "greater than" or
   //   "less than".  They use "must exceed by epsilon" and
   //   "must be at least epsilon less than".
   // They are not mutually disjoint.
   static void verify_strictly_in_range(Real source, Real lowBound,
                                        Real highBound,
                                        const char* const
                                        additionalErrMsg = NULL);
   static void verify_strictly_greater(Real lhs, Real rhs,
                                       const char* const
                                       additionalErrMsg = NULL);
   static void verify_strictly_less(Real lhs, Real rhs,
                                    const char* const additionalErrMsg = NULL);
   static void verify_strictly_in_range(StoredReal source,
                                        StoredReal lowBound,
                                        StoredReal highBound,
                                        const char* const
                                        additionalErrMsg = NULL);
   static void verify_strictly_greater(StoredReal lhs, StoredReal rhs,
                                       const char* const
                                       additionalErrMsg = NULL);
   static void verify_strictly_less(StoredReal lhs, StoredReal rhs,
                                    const char* const additionalErrMsg = NULL);
 
   // The clampXXX functions will take a value that is outside, by epsilon,
   //   that desired, and clamp it to the range.
   void clamp_to_range(Real& source, Real lowBound, Real highBound,
                       const char* const additionalErrMsg = NULL,
                       int precMultiplier = 1) const;
   void clamp_above(Real& source, Real lowBound,
                    const char* const additionalErrMsg = NULL,
                    int precMultiplier = 1) const;
   void clamp_below(Real& source, Real highBound,
                    const char* const additionalErrMsg = NULL,
                    int precMultiplier = 1) const;
   void clamp_to_range(StoredReal& source, StoredReal lowBound,
                       StoredReal highBound,
                       const char* const additionalErrMsg = NULL,
                       int precMultiplier = 1) const;
   void clamp_above(StoredReal& source, StoredReal lowBound,
                    const char* const additionalErrMsg = NULL,
                    int precMultiplier = 1) const;
   void clamp_below(StoredReal& source, StoredReal highBound,
                    const char* const additionalErrMsg = NULL,
                    int precMultiplier = 1) const;
   // @@ These should be template members once the compiler supports them.
   static int max(int lhs, int rhs)
      {return (lhs >= rhs) ? lhs : rhs;}
   static int min(int lhs, int rhs)
      {return (lhs <= rhs) ? lhs : rhs;}
   static Real max(Real lhs, Real rhs)
      {return (lhs >= rhs) ? lhs : rhs;}
   static Real min(Real lhs, Real rhs)
      {return (lhs <= rhs) ? lhs : rhs;}
   static StoredReal max(StoredReal lhs, StoredReal rhs)
      {return (lhs >= rhs) ? lhs : rhs;}
   static StoredReal min(StoredReal lhs, StoredReal rhs)
      {return (lhs <= rhs) ? lhs : rhs;}
 
   // dribble
   static void reset_dribble();
   static void dribble_wrapper(Real dribble);
   static void finish_dribble();
 
#ifdef TEST_PERFORMANCE
 
   // performance measurement; this is in basics.h so that we can redefine
   // TEST_PERFORMANCE in a separate file
 
   void start_performance_meas(const MString& str) {
      clock_gettime(CLOCK_REALTIME, &start);
      Mcout << "\n--- " << str << " started at "
            << start.tv_sec << " sec " << start.tv_nsec << " nanosec ...\n";
      perfExpName = &str;
   }
 
   void stop_performance_meas() {
      clock_gettime(CLOCK_REALTIME, &stop);
      // need unbuffered output for debugging
      Mcout << "\n ... finished  at "
            << stop.tv_sec << " sec " << stop.tv_nsec << " nanosec ---\n";
      Mcout << *perfExpName << " took " << (stop.tv_sec - start.tv_sec) +
         1.e-9 * (stop.tv_nsec - start.tv_nsec) << " seconds\n";
   }
 
#else
 
   // start_performance_meas does not compile without an assignment
   void start_performance_meas(const MString&) { }
   void stop_performance_meas() { }
 
#endif // TEST_PERFORMANCE
 
};
 
extern MLC mlc;
 
// Compare two Array<Real>s for equality, where the desired
//   precision multiplier.  Always treat as StoredRead for comparison
//   purposes.
Bool stored_real_equal(const Array<Real>& a1, const Array<Real>& a2,
                       int precMultiplier = 1);
Bool stored_real_equal(const Array<Real>& a1, const Array<StoredReal>& a2,
                       int precMultiplier = 1);
Bool stored_real_equal(const Array<StoredReal>& a1, const Array<Real>& a2,
                       int precMultiplier = 1);
Bool stored_real_equal(const Array<StoredReal>& a1,
                       const Array<StoredReal>& a2,
                       int precMultiplier = 1);
Bool stored_real_equal(const Array2<Real>& a1, const Array2<Real>& a2,
                       int precMultiplier = 1);
 
// This class contains flags which can be used to determine if other
//   static classes in basics are fully initialized.
class StaticInit {
   NO_DEFAULT_OPS(StaticInit);
public:
   // Public member data
   Bool is_initialized;  // Using an accessor function may cause a crash
                         //   if this is accessed at the wrong time.
 
   // Methods
   StaticInit() { is_initialized = TRUE; }
 
   // The following line is just a way of using variables so we
   //   don't get variable declared but never used.
  //  iostream_init doesn't appear in this form under MSVC
#if defined(PC_MSVC) || defined(GNU)
   void use_vars() { (void)mlcInit; }
#elif defined(MIPS_PRO)
   // Since 7.21, the iostream_init has been fixed and actually
   //   our statement causes a warning if it's there.
#  if (_COMPILER_VERSION <= 720)
   void use_vars() { (void)mlcInit; (void)iostream_init; }
#  else
   void use_vars() { (void)mlcInit; }
#  endif
#else
#error "compiler not supported"
#endif
 
   ~StaticInit() { is_initialized = FALSE; }
};
 
// Ternary value that extends Bool
class NYU {
   // Member data
   enum {unset = -1, no = 0, yes = 1} value;
public:
   NYU() {value = unset;}
   NYU(Bool b) {if (b) value = yes; else value = no;}
   NYU(int b) {if (b == 1) value = yes; else if (b == 0) value = no;
               else err << "NYU::int constructor not 0 or 1" << fatal_error;}
   const NYU& operator=(const NYU& nyu) { value = nyu.value; return *this;}
   Bool operator=(Bool b) {return b ? (value = yes) : (value = no);}
   Bool operator=(int b) {if (b == 1) return value = yes;
                          else if (b == 0) return value = no;
     else {err << "NYU::int constructor not 0 or 1" << fatal_error; return 0;}}
   Bool operator!() const {return !Bool(*this);}
   Bool is_unset() const {return value == unset;}
   operator Bool() const {if (value == unset)
                      err << "NYU::attempt to use unset value" << fatal_error;
                      return value == yes;}
   void display(MLCOStream& stream = Mcout) const;
};
DECLARE_DISPLAY(NYU)
 
// Check this member to see if initialization is complete.
extern StaticInit basicStatics;
 
 */
}