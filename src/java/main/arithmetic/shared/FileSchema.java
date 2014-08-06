package arithmetic.shared;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/** This class represents an MLC++ names file. The FileSchema's main task is
 * to interpret the values in a .data file. Currently, a FileSchema maintains
 * a raw list of attribute infos or COLUMNS, information about which columns
 * should represent label or weight values in the final schema, and an
 * optional loss matrix.									<P>
 * FileSchemas may be created from a names file, or from a preexisting
 * array of attribute infos which may be built programmatically. The label
 * column, weight column, and loss matrix may all be set programatically.	<P>
 * At any time, a standard MLC++ Schema may be created from the FileSchema
 * through the create_schema() function.						<P>
 * Displaying a FileSchema will do so in the same format used to read
 * FileSchemas from names files.
 * @author James Louis	11/30/2001	Ported to Java.
 * @author Dan Sommerfield 2/26/97 Initial revision (.h, .c)
 */

public class FileSchema
{
/** Information on Attributes. **/
   AttrInfo[] attrInfos;

/** LossKeyword value. **/
   public static final byte nomatrix = 0;

/** LossKeyword value. **/
   public static final byte nodefault = 1;

/** LossKeyword value. **/
   public static final byte adefault = 2;

/** LossKeyword value. **/
   public static final byte distance = 3;

   /** Byte value indicating a character is a section delimeter.
    */
   public static final byte sectionDelimiter = 0;

   /** Byte value indicating an end-of-file character has been reached.
    */
   public static final byte sectionEscape = -1;

   /** Byte value indicating a character is an alpha-numerical character.
    */
   public static final byte sectionCharacter = 1;

   /** Maximum size for a String value.
    */
   public static final int MAX_INPUT_STRING_SIZE = 1000;

/** **/
   private byte lossKeyword;

   /** Loss arguments.
    */
   double[] lossArgs;

   /** Loss entries for this schema.
    */
   FSLossEntry[] lossEntries;

/** Number of the Label value column. **/
   int labelColumn;

/** Number of the weight column. **/
   int weightColumn;

/** TRUE if weights should be ignored, FALSE otherwise. **/
   boolean ignoreWeightColumn;

   /** Returns TRUE if the weight column is to  be ignored, FALSE otherwise.
    * @return TRUE if weight column is to be ignored, FALSE otherwise.
    */
   public boolean get_ignore_weight_column()
   {
      return ignoreWeightColumn;
   }

   /** Returns the column number of the column containing weight values.
    * @return A column number.
    */
   public int get_weight_column()
   {
      return weightColumn;
   }

   /** Returns the column number of the column containing labels.
    * @return A column number.
    */
   public int get_label_column()
   {
      return labelColumn;
   }

   /** Apply the loss specification stored in this FileSchema to the given schema. The
    * InstanceList corresponding to the schema should be fully read when this
    * function is called, to make sure that any non-fixed nominals in the schema have
    * all their values showing. Any InstanceList calling this function on its schema
    * MUST call set_schema with the new schema afterwards to ensure that all
    * instances still have the same schema.
    * @param s The schema to which the loss specification is to be applied.
    */
   public void apply_loss_spec(Schema s)
   {
      //just return if the loss matrix was never set
      if (lossKeyword == nomatrix)
         return;
      System.out.println("Warning-->FileSchema::apply_loss_spec: this function " 
             + "is not currently implemented, reaching this point may yield " 
             + "undesirable results");
   }

   /** Constructor.
    * @param namesFile Name of the namesfile containing the schema to be used.
    */
   public FileSchema(String namesFile)
   {
      lossKeyword = nomatrix;
      lossArgs = new double[3];
      labelColumn = -1;
      weightColumn = -1;
      ignoreWeightColumn = false;
      try
      {
         BufferedReader in = new BufferedReader(new FileReader(namesFile));
         attrInfos = new AttrInfo[0];
         read_names(in);
         check_for_duplicates();
      } catch(FileNotFoundException e)
      {
         e.printStackTrace();
      }
   }

   /** Copy constructor.
    * @param other The FileSchema to be copied.
    */
   public FileSchema(FileSchema other)
   {
      attrInfos = new AttrInfo[other.attrInfos.length];
      lossKeyword = other.lossKeyword;
      lossArgs = new double[other.lossArgs.length];
      for(int i = 0 ; i<lossArgs.length ; i++)
         lossArgs[i] = other.lossArgs[i];
      lossEntries = other.lossEntries;
      labelColumn = other.labelColumn;
      weightColumn = other.weightColumn;
      ignoreWeightColumn = other.ignoreWeightColumn;

      //because attrInfos is an array of references, we need to make a deep
      //copy instead of just copying the array
      try
      {
         for(int i=0 ; i<attrInfos.length ; i++)
            attrInfos[i] =(AttrInfo)other.attrInfos[i].clone();
      } catch(CloneNotSupportedException e)
      {
         Error.err("FileSchema:copyConstructor: Clone not" 
                + " supported exception caught");
      }

      OK();
   }

   /** Returns the number of attributes in this FileSchema.
    * @return The number of attributes.
    */
   public int num_attr()
   {
      return attrInfos.length;
   }

   /** Sets whether the weighted column should be ignored.
    * @param i TRUE if the weight column should be ignored, FALSE otherwise.
    */
   private void set_ignore_weight_column(boolean i)
   {
      ignoreWeightColumn = i;
   }

   /** Reads the names file and builds this FileSchema. This is a support function to
    * the constructors.
    *
    * @param in The reader from which the file information is accessed.
    */
   private void read_names(BufferedReader in)
   {
      boolean weightIsAttribute = true;
      set_ignore_weight_column(!weightIsAttribute);

      //First, try to read an attribute info, with the name "Label".
      //If we get a single name "config", read a config section instead.
      //Otherwise, enter compatibility mode.

      skip_white_comments_same_line(in);
      boolean haveConfig = false;

      AttrInfo labelInfo = read_attr_info(in, "Label");

      MLJ.ASSERT(labelInfo != null,"FileSchema.read_names: labelInfo == null.");

      if (!labelInfo.can_cast_to_nominal())
         Error.err("FileSchema::read_names: Compatibility-" 
                + "mode label was specified as \'continous\' -->fatal_error");
      else if (labelInfo.cast_to_nominal().is_fixed()&& 
             labelInfo.cast_to_nominal().num_values()== 1)
      {
         String singleName = labelInfo.cast_to_nominal().get_value(Globals.FIRST_NOMINAL_VAL);
         Error.err("FileSchema:read_names: " 
                + "I don\'t think I should reach this, this case not handled!");
         if (singleName.equals("config"))
         {
            labelInfo = null;
            haveConfig = true;
         }
         else if (singleName.equals("nolobal"))
         {
            labelInfo = null;
         }
         else
         System.out.println("Warning-->FileSchema::read_names: " 
                + "compatibility mode label was specified with the single" 
                + " value " + singleName + ".  This" 
                + " is likely a mistake.");
      }

      //if we have a config section, read config, then attributes   

      if (haveConfig)
      {
         MLJ.ASSERT(labelInfo == null,"FileSchema.read_names:labelInfo != null.");
         OptionServer configOptions = new OptionServer();
         read_config(in, configOptions);
         read_attributes(in,true);
         apply_config(configOptions);
      }
      // if in compatibility mode, read attributes and tack a label on end
      else
      {
         read_attributes(in, false);
         if (labelInfo != null)
         {
            labelColumn = attrInfos.length;
            AttrInfo[] temp = new AttrInfo[attrInfos.length +1];
            for(int i=0 ; i<attrInfos.length ; i++)
               temp[i]=attrInfos[i];
            attrInfos = null;
            attrInfos = temp;
            temp = null;
            attrInfos[attrInfos.length -1] = labelInfo;
         }
      }
      OK();
   }

   /** Read a configuration section from a file and store it in the provided option
    * server.
    *
    * @param in Reader from which file data will be accessed.
    * @param configOptions The option server that stores option information.
    */
   private void read_config(BufferedReader in, OptionServer configOptions)
   {
      String optName;
      skip_white_comments_same_line(in);
      while((optName = read_section_ws(in, ":\n" , "\t\r"))!= "endconfig")
      {
         try
         {
            if ((char)in.read()!= ':')
               System.out.println("FileSchema::read_config:expecting a colon after the configuration option \"" + optName + "\"");
         } catch(IOException e)
         {
         }
         skip_white_comments_same_line(in);
         String optVal = read_section(in, "\n" , "\r");
         configOptions.set_option(optName,optVal);
         skip_white_comments_same_line(in);
      }
      skip_white_comments_same_line(in);
   }

   /** Ensures that the schema has no duplicate attributes. We use an n^2 algorithm--
    * this could be done faster by sorting the list first.
    *
    */
   private void check_for_duplicates()
   {
      boolean dups = false;
      for(int i=0 ; i<attrInfos.length-1 ; i++)
         for(int j=i+1 ; j<attrInfos.length ; j++)
            if (attrInfos[i].name().equals(attrInfos[j].name()))
            {
               if (!dups==true)
                  Error.err("FileSchema::" 
                         + "check_for_duplicates: duplicate attribute " 
                         + "names detected...");
               Error.err("Duplicate attribute: " 
                      + attrInfos[i].name());
               dups = true;
            }
      if (dups == true)
         Error.err("check_for_duplicates");
   }

   /** Checks Integrity constraints:                                               <BR>
    * The labelColumn must be either -1, or refer to a NominalAttrInfo.           <BR>
    * The weightColumn must be either -1, or refer to a RealAttrInfo.             <BR>
    * Duplicate column names are not permitted.                                   <BR>
    *  We should check that the number of label values > 0. However, we cannot make
    * the check at this stage because the label info may be a non-fixed value set
    * which has not yet accumulated any values (i.e. before reading the list!)
    *
    */
   private void OK()
   {
      if (labelColumn == -1)
      {
         if (lossKeyword != nomatrix || lossEntries.length != 0)
            Error.err("FileSchema::OK:not OK");
      }
      else
      {
         if (!attrInfos[labelColumn].can_cast_to_nominal())
            Error.err("FileSchema::OK:not OK");
      }

      if (weightColumn!=-1)
         if (!attrInfos[weightColumn].can_cast_to_real())
            Error.err("FileSchema::OK:not OK");
      //check_for_duplicates()	 
   }

   /** Reads attributes from the names file into an array of attributes maintained by
    * this FileSchema class. If lossOK is TRUE, than the word "loss" may appear as at
    * the top of the attribute list.  The section between loss and endloss will be
    * interpreted as a loss matrix specification.
    * @param namesFile Reader allowing access to the namesfile.
    * @param lossOK TRUE if the the loss is correct. FALSE sets of a parse error.
    */
   private void read_attributes(BufferedReader namesFile, boolean lossOK)
   {
      // FileSchema should have no attributes
      if (attrInfos.length != 0)
         Error.err("attributes already in file schema, it has" +attrInfos.length + " as a length");
      String attrName;
      skip_white_comments_same_line(namesFile);
      try
      {
         while(namesFile.ready()!= false)
         {
            boolean[] sameLine = new boolean[1];

            attrName = read_word(namesFile, false, sameLine);
            if (attrName.equals("loss")&&(char)namesFile.read()!= ':')
            {
               if (!lossOK == true)
               {
                  Error.err("The loss specification must appear " 
                         + "between the config section and the list of " 
                         + "attributes.  It may not be used in compatibility " 
                         + "mode");
               }
               read_loss_spec(namesFile);
               lossOK = false;
               // read the word following the loss specification
               attrName = read_word(namesFile, false, sameLine);
            }
            if ((char)namesFile.read()!= ':')
               Error.err(" " 
                      + "Expecting a \':\' following " 
                      + "attribute name " + attrName);
            skip_white_comments_same_line(namesFile);
            AttrInfo ai = read_attr_info(namesFile, attrName);

            AttrInfo[] temp = new AttrInfo[attrInfos.length+1];
            for(int i=0 ; i<attrInfos.length ; i++)
               temp[i]=attrInfos[i];
            attrInfos = null;
            attrInfos = temp;
            temp = null;
            attrInfos[attrInfos.length-1] = ai;

            skip_white_comments_same_line(namesFile);
         }
      } catch(IOException e)
      {
         Error.err("file can\'t be read");
      }
   }

   /** Set an attribute info. Makes a copy of the attribute info which is passed in.
    * @param i Number of the attribute.
    * @param a Attribute information.
    */
   public void set_attr_info(int i, AttrInfo a)
   {
      if (i<0 || i >= attrInfos.length)
         Error.err("FileSchema::set_attr_info: index " 
                + i + " is out of range -->fatal_error");
      attrInfos[i] = null;
      attrInfos[i] = a;
      OK();
   }

   /** Reads a list of "words" (as described in read_word()) that correspond to
    * nominal values. Certain special words denote ignored attributes, non-fixed
    * nominals, real attribute infos, or linear attribute infos. If these are
    * encountered, the correct AttrInfo type will be created.
    * @param namesFile Reader allowing access to the namesfile.
    * @param attrName The name of the attrbute.
    * @return The newly created AttrInfo whose values were read.
    */
   private AttrInfo read_attr_info(BufferedReader namesFile, String attrName)
   {
      LinkedList attrVals = new LinkedList();
      AttrInfo ai = null;
      int discreteHint = -1;
      // A comma is required before each value other than the first.
      // Therefor we need to keep a flag to indicate whether we are 
      // executing the first run of the loop

      boolean firstVal = true;

      try
      {
         namesFile.mark(1);
         char c =(char)namesFile.read();
         namesFile.reset();
         while(c != '.' && c != '\n' 
                && c != '|' && c != -1)
         {
            if (!firstVal && c == ',')
               namesFile.read();
            boolean[] sameLine = new boolean[1];
            sameLine[0] = false;
            String attrValue = read_word(namesFile, false, sameLine);
            attrVals.add(attrValue);
            firstVal = false;
            namesFile.mark(1);
            c =(char)namesFile.read();
            namesFile.reset();
         }
         if (attrVals.size()==0)
            Error.err("Missing values or type specifier for" 
                   + "attribute" + attrName);
         if (attrVals.size()==1 && attrVals.getFirst().equals("continuous"))
         {
            attrVals = null;
            ai = new RealAttrInfo(attrName);
         }
         else if (attrVals.size()==1 && 
                attrVals.getFirst().equals("ignore-attribute"))
         {
            attrVals = null;
            ai = new NominalAttrInfo(attrName,0);
            ai.set_ignore(true);
         }
         else if (attrVals.size()==1 && 
               (discreteHint = read_discrete_and_hint((String)attrVals.getFirst()))!= -1)
         {
            attrVals = null;
            ai = new NominalAttrInfo(attrName, discreteHint);
         }
         else ai = new NominalAttrInfo(attrName, attrVals);

         namesFile.mark(1);
         if ((char)namesFile.read()!= '.')
            namesFile.reset();
         return ai;

      } catch(IOException e)
      {
         Error.err("FileSchema::read_attr_info - if reached here!");
      }

      return null;
   }

   /** Determine if the given name is 'discrete', possibly with an optional hint
    * number. If the name is 'discrete', then the hint number (0 if none given) is
    * returned.  If not, then -1 is returned. Placing anything other than a hint
    * number after 'discrete' and a space is an error.
    *
    * @param str String to be read.
    * @return The hint number if discrete, -1 otherwise.
    */
   private int read_discrete_and_hint(String str)
   {
      int lengthOfDiscrete = 9;

      if (str.equals("discrete"))
         return 0;
      if (str.length()< lengthOfDiscrete)
         return -1;
      String leftHalf = str.substring(0, lengthOfDiscrete);
      if (leftHalf != "discrete ")
         return -1;
      String hintString = str.substring(lengthOfDiscrete, 
             str.length()-lengthOfDiscrete);

      long val;
      val = new Long(hintString).longValue();
      if (val <0)
         Error.err("read_discrete_and_hint: " 
                + "illegal value given for " 
                + "\"discrete n\" syntax; word following \"discrete\" must be " 
                + "a nonnegative integer.  You supplied \"" + hintString + "\"");
      int v = new Long(val).intValue();
      return v;
   }

    /** Skips white space and comments.
     * @param stream Reader allowing access to the namesfile.
     * @return TRUE if the current line contains no comments, FALSE otherwise.
     */
   public boolean skip_white_comments_same_line(BufferedReader stream)
   {
      boolean sameLine = true;
      try
      {
         stream.mark(1);
         char c =(char)stream.read();
         stream.reset();
         while(Character.isWhitespace(c)|| c == '|')
         {
            if (c== '|')
            {
               sameLine = false;
               while(c!= '\n')
               {
                  c =(char)stream.read();
               }
            }
            else
            {
               if (c== '\n')
                  sameLine = false;
               stream.skip(1);
            }
            stream.mark(1);
            c =(char)stream.read();
            stream.reset();
         }
      } catch(IOException e)
      {
      }
      return sameLine;
   }

   /** Reads the loss specification as read from the stream "in". The specification
    * ends at EOF OR if "endloss" is encountered.
    * @param in Reader from which loss specification is read.
    */
   private void read_loss_spec(BufferedReader in)
   {
      skip_blank_space(in);

      // read the first line of the specification.
      read_loss_default_spec(in);
      skip_blank_space(in);

      // read each line of the file until EOF or endloss
      // the apply_loss_override_spec function will return FALSE
      // when it is done
      while(read_loss_override_spec(in))
      skip_blank_space(in);

      // if there's more to the file, skip whitespace/comments for the
      // rest of the last line
      try
      {
         if (in.ready()==true)
            skip_white_comments_same_line(in);
      } catch(IOException e)
      {
      }
   }

   /** Reads a single line of the loss specification. The single line specifies a pair
    * of two label values and an associated loss, in the format:                  <BR>
    * <predicted value>, <actual value>: <loss>                                   <BR>
    * @param in Reader from which line of specification will be read.
    * @return FALSE if we just encountered the end of the specification, TRUE otherwise.
    */
   private boolean read_loss_override_spec(BufferedReader in)
   {
      try
      {
         String predName = read_section(in, "|:,.\n" , " \t\r");
         char c =(char)in.read();

         // check if the line ends after one word
         if (c == '\n' || c == '|')
         {
            if (predName.equals("endloss"))
               return false;
            else
            Error.err("in loss specification; line contains a " 
                   + "single word: " + predName);
         }
         else if (c != ',')
            Error.err("in loss specification; a comma (,) must follow " 
                   + "the predicted value " + predName);

         // read the actual value
         String actName = read_section(in, "|:,.\n" , " \t\r");
         c =(char)in.read();

         // error if the line ends here
         if (c == '\n' || c == '|')
            Error.err("in loss specification; missing loss value");
         else if (c != ':')
            Error.err("in loss specification; a colon (:) must follow " 
                   + "the predicted/actual value pair");

         // read the loss
         String lossStr = read_section(in, "|\n" , " \t\r");

         double loss = new Double(lossStr).doubleValue();

         if (new Double(lossStr).isNaN()== true)
            Error.err("in loss specification: illegal loss value " 
                   + lossStr);
         // Call add_loss_entry to store this line for later use
         add_loss_entry(predName, actName, loss);
         return true;
      } catch(IOException e)
      {
      }

      return false;
   }

/***************************************************************************
***************************************************************************/
   private void add_loss_entry(String predVal,String actVal, double loss)
   {
      // if the keyword is set to "nomatrix", set it to "nodefault"
      // instead, to signal an undefined matrix
      if (lossKeyword == nomatrix)
         lossKeyword = nodefault;
      int size = lossEntries.length;
      lossEntries[size].predName = predVal;
      lossEntries[size].actName = actVal;
      lossEntries[size].loss = loss;
   }

/***************************************************************************
***************************************************************************/
   private void skip_blank_space(BufferedReader in)
   {
      try
      {
         skip_white_comments_same_line(in);
         while((char)in.read()== '\n')
         {
            skip_white_comments_same_line(in);
         }
      } catch(IOException e)
      {
      }
   }

/***************************************************************************
***************************************************************************/
   private void read_loss_default_spec(BufferedReader in)
   {
      try
      {
         // first step: read the loss keyword from the lossStr
         String keyword = read_section(in, "|\n:,.-0123456789" , " \t\r");
         in.mark(1);
         char c =(char)in.read();
         in.reset();
         if (c != ':' && c != '.')
            Error.err("in loss specification; " 
                   + "first line must begin with a keyword " 
                   + "{nodefault, default, distance} followed by a colon " 
                   + "(:) or period (.)");
         byte key = 0;
         if (keyword.equals("nodefault"))
            key = nodefault;
         else if (keyword.equals("default"))
            key = adefault;
         else if (keyword.equals("distance"))
            key = distance;
         else
         {
            Error.err("in loss specification; " 
                   + "unrecognized keyword \"" + keyword + "\".  Keyword " 
                   + "must be one of {nodefault, default, distance}");
         }

         // second step: read the array of arguments (unless the keyword is
         // followed by a period or end of line)
         double[] args;
         args = new double[0];
         if (c == '.' || c == '\n')
         {
            in.read();
         }
         else
         args = process_loss_args(in, "," , "|\n");
         set_loss_default(key, args);
      } catch(IOException e)
      {
      }
   }

/***************************************************************************
***************************************************************************/
   private void set_loss_default(byte keyword, double[] args)
   {
      lossKeyword = keyword;

      switch (keyword)
      {
      case  nomatrix:
            Error.err("FileSchema::set_loss_default: keyword may not be set " 
                   + "to nomatrix");
            break;

      case  nodefault:
            if (args.length != 0)
               Error.err("FileSchema::set_loss_default: nodefault takes no " 
                      + "arguments");
            lossArgs[0] = 0;
            lossArgs[1] = 0;
            lossArgs[2] = 0;
            break;
      case  adefault:
            if (args.length == 0)
            {
               lossArgs[0] = 0;
               lossArgs[1] = 1;
            }
            else if (args.length == 1)
            {
               lossArgs[0] = args[0];
               lossArgs[1] = 1;
            }
            else if (args.length == 2)
            {
               lossArgs[0] = args[0];
               lossArgs[1] = args[1];
            }
            else
            {
               Error.err("FileSchema::set_loss_default: default takes up to " 
                      + "two arguments.  You supplied " + args.length);
            }
            lossArgs[2] = 0;
            break;
      case  distance:
            if (args.length == 0)
            {
               lossArgs[0] = 0;
               lossArgs[1] = 1;
               lossArgs[2] = 1;
            }
            else if (args.length == 1)
            {
               lossArgs[0] = args[0];
               lossArgs[1] = 1;
               lossArgs[2] = 1;
            }
            else if (args.length == 2)
            {
               lossArgs[0] = args[0];
               lossArgs[1] = args[1];
               lossArgs[2] = args[1];
            }
            else if (args.length == 3)
            {
               lossArgs[0] = args[0];
               lossArgs[1] = args[1];
               lossArgs[2] = args[2];
            }
            else
            {
               Error.err("FileSchema::set_loss_default: distance takes up to " 
                      + "three arguments.  You supplied " + args.length);
            }
            break;
      default:
            Error.err("ABORT_IF_REACHED");
      }
   }

/***************************************************************************
***************************************************************************/
   private double[] process_loss_args(BufferedReader in, String sepChars, 
          String termChars)
   {
      double[] arr = new double[0];
      try
      {
         for(; ;)
         {
            in.read();
            String arg = read_section(in, sepChars + termChars, 
                   " \t\r\n");
            // check for a termination character
            in.mark(1);
            char c =(char)in.read();
            in.reset();
            boolean isTerm =
               (termChars.indexOf(new Character(c).toString())!= -1);

            // allow termination immediately on a termChar
            if (arg == "" && arr.length == 0 && isTerm == true)
               return arr;
            double argReal;
            argReal = new Double(arg).doubleValue();
            if (new Double(arg).isNaN()== false)
               arr[arr.length] = argReal;
            else
            Error.err("FileSchema::process_loss_args: in loss specification; " 
                   + "argument \"" + arg + "\" in list cannot be " 
                   + "converted to a Real");

            // if we hit a termination character, stop here
            if (isTerm == true)
               return arr;
         }
      } catch(IOException e)
      {
      }
      Error.err("Shouldn\'t read here!");
      return null;
   }

   /** Reads a single word from the supplied BufferedReader.
    * @param stream The BufferedReader to be read from.
    * @param qMark TRUE if question marks are an acceptable name, FALSE otherwise.
    * @param sameLine Set to TRUE if the line has not changed in the process of reading this word, FALSE
    * otherwise.
    * @return The word read.
    */
   public String read_word(BufferedReader stream, boolean qMark, boolean[] sameLine)
   {
      try
      {
         boolean periodAllowed = false;
         char[] word = new char[MAX_INPUT_STRING_SIZE + 1];
         int wordLen = 0;
         sameLine[0] = skip_white_comments_same_line(stream);
         boolean whitespace = false;
         char[] c = new char[1];

         while(legal_attr_char(stream, c, periodAllowed))
         {

            if (c[0] == ' ' || c[0] == '\t' || c[0] == '\r')
            {
               whitespace = true;
               stream.skip(1);
            }
            else
            {
               if (whitespace)
               {
                  word[wordLen] =' ';
                  wordLen = inc_word_len(wordLen);
                  whitespace = false;
               }
               word[wordLen] =(char)stream.read();
               if (word[wordLen] == '\\')
                  word[wordLen] =(char)stream.read();
               wordLen = inc_word_len(wordLen);
            }
            // indicates problem w/legal_attr_char()
            if (stream.ready()==false)
               Error.err("FileSchema::read_word: stream not ready");
         }
         // Note that since c is a peeked character, we don't update
         //   sameLine here, since we haven't actually read it.
         if (wordLen < 1 && !periodAllowed)
            System.out.println("FileSchema::read_word: Unable to read word.  Perhaps you forgot to supply it.");
         if (!qMark && wordLen == 1 && word[0] == '?')
            System.out.println("Illegal name \'?\'");
         return new String(word).trim();
      } catch(IOException e)
      {
      }
      return null;
   }

   /** Reads a single word from the supplied BufferedReader without crossing lines.
    * @param stream The BufferedReader to be read from.
    * @param qMark TRUE if question marks are an acceptable name, FALSE otherwise.
    * @param periodAllowed TRUE if periods are allowed as words, FALSE otherwise. Automatically set to
    * FALSE in this function.
    * @return The word read.
    */
   public String read_word_on_same_line(BufferedReader stream, boolean qMark, boolean periodAllowed)
   {
      periodAllowed = false;
      boolean[] sameLine = new boolean[1];
      String word = read_word(stream, qMark, sameLine);
      if (!sameLine[0])
         Error.err("Parse Error->FileSchema:read_word_same_line" 
                + " Another word expected");
      return word;
   }

/***************************************************************************
***************************************************************************/
   private boolean legal_attr_char(BufferedReader stream, char[] c, boolean periodAllowed)
   {
      try
      {
         if (stream.ready()== false)
            Error.err("Unexpected end of file.");
         stream.mark(1);
         char ch =(char)stream.read();
         stream.reset();
         c[0] = ch;
         switch (ch)
         {
         case '\\' :
               return true;
         case ',' :
         case ':' :
         case '|' :
         case '\n' :
               return false;
         case '.' :
               return(periodAllowed);
         default:
               return true;
         }
      } catch(IOException e)
      {
      }
      return false;
   }

/***************************************************************************
***************************************************************************/
   private int inc_word_len(int wordlen)
   {
      wordlen++;
      if (wordlen > MAX_INPUT_STRING_SIZE)
      {
         System.out.println("mlcIO::read_word: word overflow.  More than " 
                + MAX_INPUT_STRING_SIZE + " characters for word " 
                + "fatal_error");
         return wordlen;
      }
      else return wordlen;
   }

/***************************************************************************
***************************************************************************/
   private String read_section_ws(BufferedReader stream, String delims, String wsChars)
   {
      boolean allowEOF = false;
      char[] word = new char[MAX_INPUT_STRING_SIZE + 1];
      int wordLen = 0;
      boolean whitespace = false;
      char[] c = new char[1];

      try
      {
         while(is_section_char(stream, c, delims, allowEOF)!= sectionDelimiter)
         {
            if (wsChars.indexOf(new Character(c[0]).toString())!= -1)
            {
               whitespace = true;
               stream.skip(1);
            }
            else
            {
               if (whitespace)
               {
                  word[wordLen] =' ';
                  inc_word_len(wordLen);
                  whitespace = false;
               }
               word[wordLen] =(char)stream.read();
               wordLen = inc_word_len(wordLen);
            }

            if (stream.ready()== false)
               Error.err("FileSchema::read_section_ws: unexpected end of file");
         }
      } catch(IOException e)
      {
      }
      return new String(word, 0, wordLen).toString().trim();
   }

/***************************************************************************
***************************************************************************/
   private byte is_section_char(BufferedReader stream, char[] c, String delims, boolean allowEOF)
   {
      try
      {
         if (stream.ready()== false)
         {
            if (!allowEOF)
            {
               Error.err("FileSchema::is_section_char:unexpected end of file");
               return sectionDelimiter;
            }
            else
            {
               c[0] = 0;
               return sectionDelimiter;
            }
         }
         stream.mark(1);
         char ch =(char)stream.read();
         stream.reset();
         c[0] = ch;
         if (ch == '\\')
         {
            stream.read();
            stream.mark(1);
            ch =(char)stream.read();
            stream.reset();
            if (stream.ready()== false)
               Error.err("FileSchema::is_section_char:unexpected end of file after backslash");
            c[0] = ch;
            return sectionEscape;
         }
         else if (delims.indexOf(new Character(c[0]).toString())!= -1)
         {
            return sectionDelimiter;
         }
         else
         return sectionCharacter;

      } catch(IOException e)
      {
         Error.err("shouldn\'t get here in FileSchema.java");
      }
      return sectionCharacter;
   }

/***************************************************************************
***************************************************************************/
   private String read_section(BufferedReader stream, String delims, String ignoreChars)
   {
      boolean allowEOF = false;
      char[] word = new char[MAX_INPUT_STRING_SIZE + 1];
      int wordLen = 0;

      char[] c = new char[1];
      byte sectionType;
      try
      {
         while((sectionType = is_section_char(stream, c, delims, allowEOF))!= sectionDelimiter)
         {
            if ((ignoreChars.indexOf(new Character(c[0]).toString())!= -1)&& sectionType != sectionEscape)
               stream.read();
            else
            {
               int i = stream.read();
               word[wordLen] =(char)i;
               wordLen = inc_word_len(wordLen);
            }
            if (stream.ready()==false)
               Error.err("FileSchema::read_section: stream not ready!");
         }

      } catch(IOException e)
      {
         Error.err("Error :)");
      }
      return new String(word).trim();
   }

/***************************************************************************
***************************************************************************/
   private void apply_config(OptionServer configOptions)
   {
      String[] labelName = new String[1];
      if (configOptions.get_option("label" , labelName))
      {
         labelColumn = find_attribute(labelName, false);
         if (labelColumn < 0)
            Error.err("FileSchema::apply_config: The " 
                   + "requested label attribute \"" + labelName[0] + "\" was never" 
                   + " declared --> fatal error");
         if (attrInfos[labelColumn].can_cast_to_real())
            Error.err("FileSchema::apply_config: The " 
                   + "requested label attribute \"" + labelName[0] + "\" must be " 
                   + "a Nominal type --> fatal error");
      }
      String[] weightName = new String[1];
      if (configOptions.get_option("weight" , weightName))
      {
         weightColumn = find_attribute(weightName, false);
         if (weightColumn < 0)
            Error.err("FileSchema::apply_config: The " 
                   + "requested weight attribute \"" + weightName[0] + "\" was never" 
                   + " declared --> fatal error");
         if (attrInfos[labelColumn].can_cast_to_real())
            Error.err("FileSchema::apply_config: The " 
                   + "requested weight attribute \"" + weightName[0] + "\" must be " 
                   + "a Nominal type --> fatal error");
      }
   }

   /** Find an attribute in the file schema by name. If the attribute is not found,
    * aborts if fatalOnNotFound is set.  Otherwise returns -1. Assumes the schema has
    * no duplicate attributes.
    *
    * @param name Name of the attribute.
    * @param fatalOnNotFound TRUE if an error message should be displayed if there is no attribute matching
    * that name, FALSE otherwise.
    * @return The integer value corresponding to the attribute with the specified name or -1
    * if an attribute with a matching name is not found.
    */
   public int find_attribute(String[] name, boolean fatalOnNotFound)
   {
      for(int i=0 ; i<attrInfos.length ; i++)
         if (attrInfos[i].name().equals(name[0]))
            return i;
      if (fatalOnNotFound)
         Error.err("FileSchema::find_attribute " 
                + name + "does not exist in this schema --> fatal error");
      return -1;
   }

   /** Create an MLJ style schema from all the information stored in this class.
    * This schema is used to create lists, use InstanceReaders, etc.
    *
    * @return Schema object containing information generated from this FileSchema object.
    */
   public Schema create_schema()
   {
      AttrInfo labelInfo = null;
      LinkedList schemaNames = new LinkedList();
      if (attrInfos == null)
         System.out.println("attrInfos is null");
      for(int i=0 ; i<attrInfos.length ; i++)
      {
         if (i==labelColumn)
         {
            labelInfo = attrInfos[i];
         }
         else if (i==weightColumn && ignoreWeightColumn)
         {
         }
         else
         {
            AttrInfo aip = attrInfos[i];
            schemaNames.add(aip);
         }
      }
      if (labelInfo!=null)
      {
         Schema sch = new Schema(schemaNames, labelInfo);
         return sch;
      }
      else
      {
         Schema sch = new Schema(schemaNames);
         return sch;
      }
   }


   /** Display this FileSchema.  This is done in .names file format so this can be
    * used for file conversion.
    *
    */
   public void display()
   {
      System.out.println("config");
      if (labelColumn != -1)
         System.out.println("label: " +attrInfos[labelColumn].name());
      if (weightColumn != -1)
         System.out.println("weight: " +attrInfos[weightColumn].name());
      System.out.println("endconfig\n");

      if (lossKeyword!=nomatrix)
      {
         System.out.println("loss");
         switch (lossKeyword)
         {
         case  nodefault:
               System.out.println("nodefault");
               break;
         case  adefault:
               System.out.println("default: " +lossArgs[0]+ ", " +lossArgs[1]);
               break;
         case  distance:
               System.out.println("distance: " +lossArgs[0]+ ", " + 
                      lossArgs[1]+ ", " + lossArgs[2]);
               break;
         default:
         }
         for(int i=0 ; i<lossEntries.length ; i++)
         {
            System.out.println(lossEntries[i].predName+ ", " 
                   +lossEntries[i].actName+ ": " 
                   +lossEntries[i].loss);
         }
         System.out.println("endloss");
      }
      for(int i=0 ; i<attrInfos.length ; i++)
      {
         System.out.print(attrInfos[i].name()+ ": ");
         if (attrInfos[i].can_cast_to_real())
            System.out.println("continuous");
         else if (attrInfos[i].can_cast_to_nominal())
         {
            NominalAttrInfo nai = attrInfos[i].cast_to_nominal();
            if (nai.is_fixed())
            {

               for(int val=Globals.FIRST_NOMINAL_VAL ; val<=nai.num_values(); val++)
               {
                  System.out.print(nai.get_value(val));
                  if (val <= nai.num_values()-1)
                     System.out.print(", ");
               }
               System.out.println();
            }
            else System.out.println("discrete");
         }
      }
   }
}
