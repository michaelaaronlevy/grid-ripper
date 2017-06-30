/**
 * Extension of PDFTextStripper to output the words along with their respective
 * coordinates on the page. This is useful for converting PDFs into Excel
 * spreadsheets. It can also be used to convert a pdf into a monospaced .txt
 * file where the characters line up with how they were originally intended to
 * line up.
 * 
 * Most users will want to use mode "Fixed"
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class TextStripperCoordinates extends PDFTextStripper
{
   /**
    * If args is null, the program asks the user (through Swing dialogs) to
    * input the mode, the output file, and to choose 1+ files to rip.
    * 
    * If args has length of 3 or more, index 0 is the mode, index 1 is the
    * output file, and indices 2+ are the inputs
    * 
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws IOException
   {
      int mode = _MODE_FIXED;
      File output = null;
      final ArrayList<File> inputs = new ArrayList<File>();
      
      if(args == null || args.length < 3)
      {
         Object[] possibleValues = { "Default", "Fixed", "Atomic", "Verbose", "Error Tracking" };
         Object selectedValue = JOptionPane.showInputDialog(null, "Mode:", "Select Output Mode",
               JOptionPane.QUESTION_MESSAGE, null, possibleValues, possibleValues[mode]);
         if(selectedValue == null)
         {
            return;
         }
         for(int i = 0; i < possibleValues.length; i++)
         {
            if(selectedValue == possibleValues[i])
            {
               mode = i;
            }
         }
         final JFileChooser jfc = new JFileChooser();
         jfc.setDialogType(JFileChooser.SAVE_DIALOG);
         jfc.setDialogTitle("Select Output File");
         int selection;
         
         while(jfc.getSelectedFile() == null || jfc.getSelectedFile().isDirectory())
         {
            selection = jfc.showSaveDialog(null);
            if(selection == JFileChooser.CANCEL_OPTION)
            {
               return;
            }
            output = jfc.getSelectedFile();
         }
         
         jfc.setDialogTitle("Select PDFs From Which to Extract Text");
         jfc.setFileFilter(new FileNameExtensionFilter("Portable Document Format", "pdf"));
         jfc.setDialogType(JFileChooser.OPEN_DIALOG);
         jfc.setMultiSelectionEnabled(true);
         boolean flag = true;
         while(flag)
         {
            selection = jfc.showOpenDialog(null);
            if(selection == JFileChooser.CANCEL_OPTION)
            {
               selection = JOptionPane.showConfirmDialog(null, "Finished Adding Files?");
               if(selection == JOptionPane.CANCEL_OPTION)
               {
                  return;
               }
               else if(selection == JOptionPane.YES_OPTION)
               {
                  flag = false;
               }
            }
            else
            {
               final File[] files = jfc.getSelectedFiles();
               for(final File f : files)
               {
                  for(int i = 0; i < inputs.size(); i++)
                  {
                     if(inputs.get(i).equals(f))
                     {
                        inputs.remove(i--);
                     }
                  }
                  inputs.add(f);
               }
               jfc.setSelectedFiles(new File[] {});
            }
         }
      }
      else
      {
         mode = Integer.parseInt(args[0]);
         output = new File(args[1]);
         for(int i = 2; i < args.length; i++)
         {
            inputs.add(new File(args[i]));
         }
      }
      final TextStripperCoordinates tsc = new TextStripperCoordinates(mode);
      tsc.writeManyFiles(inputs, output, true);
      System.out.println("Finished.");
   }
   
   public TextStripperCoordinates(final int mode) throws IOException
   {
      super();
      setSortByPosition(true);
      setMode(mode);
      currentFileName = "";
      csvNewline = System.lineSeparator();
      setArticleEnd("");
      setArticleStart("");
      setLineSeparator("");
      setPageEnd("");
      setPageStart("");
      setParagraphEnd("");
      setParagraphStart("");
      setWordSeparator("");
   }
   
   public void writeOneFile(final File input, final File output) throws IOException
   {
      final ArrayList<File> f = new ArrayList<File>(1);
      f.add(input);
      writeManyFiles(f, output, false);
   }
   
   public void writeManyFiles(final List<File> input, final File output, final boolean includePath) throws IOException
   {
      final BufferedWriter b = new BufferedWriter(new FileWriter(output));
      for(final File in : input)
      {
         currentFileName = includePath ? in.getPath() : in.getName();
         PDDocument doc = PDDocument.load(in);
         writeText(doc, b);
         doc.close();
      }
      b.close();
   }
   
   protected void writePrefix() throws IOException
   {
      output.write(counter++ + ",");
      if(currentFileName != null && currentFileName.length() > 0)
      {
         output.write(currentFileName + ",");
      }
      output.write(getCurrentPageNo() + ",");
   }
   
   protected void setCoordinates(final TextPosition tp)
   {
      values[0] = tp.getY();
      values[1] = tp.getX();
      values[2] = tp.getEndX();
      values[3] = tp.getFontSize();
      values[4] = tp.getDir();
   }
   
   protected void setCoordinates(final ArrayList<TextPosition> ttpp)
   {
      setCoordinates(ttpp.get(0));
      values[0] = Float.MAX_VALUE;
      values[1] = Float.MAX_VALUE;
      values[2] = Float.MIN_VALUE;
      
      for(final TextPosition tp : ttpp)
      {
         if(values[0] > tp.getY())
         {
            values[0] = tp.getY();
         }
         if(values[1] > tp.getX())
         {
            values[1] = tp.getX();
         }
         if(values[2] < tp.getEndX())
         {
            values[2] = tp.getEndX();
         }
      }
   }
   
   protected void writeCoordinates() throws IOException
   {
      for(int i = 0; i < values.length; i++)
      {
         if(printPermissions[i])
         {
            output.write(values[i] + ",");
         }
      }
   }
   
   protected void writeString(String text, List<TextPosition> textPositions) throws IOException
   {
      if(mode == _MODE_FIXED)
      {
         writeStringFixed(text, textPositions);
      }
      else if(mode == _MODE_ATOMIC)
      {
         writeStringAtomic(text, textPositions);
      }
      else if(mode == _MODE_VERBOSE)
      {
         writeStringDefault(text, textPositions);
         writeStringFixed(text, textPositions);
         writeStringAtomic(text, textPositions);
      }
      else if(mode == _MODE_ERROR_TRACKING)
      {
         if(isError(text, textPositions))
         {
            writeStringDefault(text, textPositions);
            writeStringFixed(text, textPositions);
            writeStringAtomic(text, textPositions);
         }
      }
      else
      {
         writeStringDefault(text, textPositions);
      }
   }
   
   protected void writeStringDefault(String text, List<TextPosition> textPositions) throws IOException
   {
      writePrefix();
      setCoordinates(textPositions.get(0));
      writeCoordinates();
      output.write("\"");
      for(int i = 0; i < text.length(); i++)
      {
         output.write(literalCharacter(text.charAt(i)));
      }
      output.write("\"");
      output.write(csvNewline);
   }
   
   protected void writeStringFixed(String text, List<TextPosition> textPositions) throws IOException
   {
      final float spaceWidth = textPositions.get(0).getWidthOfSpace();
      final float[] diff = new float[textPositions.size() - 1];
      for(int i = 1; i < textPositions.size(); i++)
      {
         final TextPosition one = textPositions.get(i - 1);
         final TextPosition two = textPositions.get(i);
         diff[i - 1] = two.getXDirAdj() - (one.getXDirAdj() + one.getWidthDirAdj());
      }
      
      final ArrayList<ArrayList<TextPosition>> words = new ArrayList<ArrayList<TextPosition>>();
      ArrayList<TextPosition> current = new ArrayList<TextPosition>();
      words.add(current);
      current.add(textPositions.get(0));
      
      for(int i = 1; i < textPositions.size(); i++)
      {
         final TextPosition tp = textPositions.get(i);
         if(diff[i - 1] < -spaceWidth || diff[i - 1] > separateWords * spaceWidth)
         {
            current = new ArrayList<TextPosition>();
            words.add(current);
         }
         current.add(tp);
      }
      
      Collections.sort(words, new Comparator<ArrayList<TextPosition>>()
      {
         public int compare(final ArrayList<TextPosition> one, final ArrayList<TextPosition> two)
         {
            final TextPosition a = one.get(0);
            final TextPosition b = two.get(0);
            final float ax = a.getXDirAdj();
            final float bx = b.getXDirAdj();
            if(ax == bx)
            {
               return a.hashCode() < b.hashCode() ? -1 : 1;
            }
            else
            {
               return ax < bx ? -1 : 1;
            }
         }
      });
      
      for(final ArrayList<TextPosition> ttpp : words)
      {
         writePrefix();
         setCoordinates(ttpp);
         writeCoordinates();
         output.write("\"");
         for(final TextPosition tp : ttpp)
         {
            final String s = tp.getUnicode();
            for(int i = 0; i < s.length(); i++)
            {
               output.write(literalCharacter(s.charAt(i)));
            }
         }
         output.write("\"");
         output.write(csvNewline);
      }
   }
   
   protected void writeStringAtomic(String text, List<TextPosition> textPositions) throws IOException
   {
      for(final TextPosition tp : textPositions)
      {
         writePrefix();
         setCoordinates(tp);
         writeCoordinates();
         output.write("\"");
         final String s = tp.getUnicode();
         for(int i = 0; i < s.length(); i++)
         {
            output.write(literalCharacter(s.charAt(i)));
         }
         output.write("\"");
         output.write(csvNewline);
      }
   }
   
   protected boolean isError(final String text, final List<TextPosition> textPositions)
   {
      final float spaceWidth = textPositions.get(0).getWidthOfSpace();
      final float[] diff = new float[textPositions.size() - 1];
      for(int i = 1; i < textPositions.size(); i++)
      {
         final TextPosition one = textPositions.get(i - 1);
         final TextPosition two = textPositions.get(i);
         diff[i - 1] = two.getXDirAdj() - (one.getXDirAdj() + one.getWidthDirAdj());
      }
      
      for(int i = 1; i < textPositions.size(); i++)
      {
         if(diff[i - 1] < -spaceWidth)
         {
            return true;
         }
      }
      return false;
   }
   
   protected void writeString(String text)
   {
      throw new RuntimeException("This method should never be called, so this exception should never be thrown");
   }
   
   private static String literalCharacter(final char c)
   {
      if(c == '\"')
      {
         return "\"\"";
      }
      else
      {
         return "" + c;
      }
   }
   
   public int getMode()
   {
      return mode;
   }
   
   public boolean setMode(final int newMode)
   {
      if(newMode < 0 || newMode > _MODE_ERROR_TRACKING)
      {
         mode = _MODE_DEFAULT;
         return false;
      }
      else
      {
         mode = newMode;
         return true;
      }
   }
   
   public float getSeparateWords()
   {
      return separateWords;
   }
   
   public boolean setSeparateWords(final float newS)
   {
      if(newS > 0)
      {
         separateWords = newS;
         return true;
      }
      return false;
   }
   
   protected String currentFileName;
   protected String csvNewline;
   protected long counter = 1;
   
   protected int mode;
   protected float[] values = new float[5];
   
   /**
    * change these values to disable printing certain columns.
    */
   public final boolean[] printPermissions = new boolean[] { true, true, true, true, true, true, true, true };
   
   protected float separateWords = _DEFAULT_SEPARATE_WORDS;
   
   /**
    * use the PDFTextStripper output, with setSortByPosition(true)
    */
   public static final int _MODE_DEFAULT = 0;
   
   /**
    * use the PDFTextStripper output, with setSortByPosition(true) and
    * additional fixes
    */
   public static final int _MODE_FIXED = 1;
   
   /**
    * a separate row for each TextPosition object - which is usually one
    * character per line
    */
   public static final int _MODE_ATOMIC = 2;
   
   /**
    * print output for default, fixed, and atomic (3x redundant)
    */
   public static final int _MODE_VERBOSE = 3;
   
   /**
    * this is deprecated - unnecessary - should return empty csv files
    */
   public static final int _MODE_ERROR_TRACKING = 4;
   
   /**
    * if the distance between two characters is more than this many space
    * characters, in FIXED mode the words will be broken into separate rows
    */
   public static final float _DEFAULT_SEPARATE_WORDS = 2.0f;
}
