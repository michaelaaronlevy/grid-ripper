import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class outputs the information to the .csv format. .csv files can be
 * opened directly by Microsoft Excel (as long as they are not too large). All
 * of the output goes into a single .csv file, which you might need to break up
 * into smaller files in order to import it to Excel.
 * 
 * Copyright 2017-2018 Michael A. Levy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @author Michael
 *
 */
class GridRipperCsvWriter implements GridRipperWriter
{
   GridRipperCsvWriter(final File target)
   {
      runStatus = GridRipperWriter._RUN_STATUS_NOT_STARTED;
      errorStatus = GridRipperWriter._ERROR_STATUS_NO_ERROR;
      BufferedWriter o = null;
      try
      {
         o = new BufferedWriter(new FileWriter(target));
      }
      catch(final IOException iox)
      {
         iox.printStackTrace();
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
      out = o;
   }
   
   public void open(final GridRipperController c)
   {
      runStatus = GridRipperWriter._RUN_STATUS_RUNNING;
   }
   
   public void writeBlank()
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      try
      {
         out.write(_SEPARATOR);
      }
      catch(final IOException iox)
      {
         iox.printStackTrace();
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
   }
   
   public void writeText(String s)
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      try
      {
         out.write("\"");
         for(int i = 0; i < s.length(); i++)
         {
            out.write(literalCharacter(s.charAt(i)));
         }
         out.write("\"");
         out.write(_SEPARATOR);
      }
      catch(final IOException iox)
      {
         iox.printStackTrace();
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
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
   
   public void writeInt(int i)
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      try
      {
         out.write(Integer.toString(i));
         out.write(_SEPARATOR);
      }
      catch(final IOException iox)
      {
         iox.printStackTrace();
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
   }
   
   public void writeFloat(float f)
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      try
      {
         out.write(Float.toString(f));
         out.write(_SEPARATOR);
      }
      catch(final IOException iox)
      {
         iox.printStackTrace();
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
   }
   
   public void writeDate(long d)
   {
      writeText(sdf.format(new Date(d)));
   }
   
   public void startRow()
   {
      // no action needed.
   }
   
   public void endRow()
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      try
      {
         out.newLine();
      }
      catch(final IOException iox)
      {
         iox.printStackTrace();
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
   }
   
   public void startPage()
   {
      // no action needed.
   }
   
   public void endPage()
   {
      // no action needed.
   }
   
   public void startPDF()
   {
      // no action needed.
   }
   
   public void endPDF()
   {
      // no action needed.
   }
   
   public void close()
   {
      runStatus = GridRipperWriter._RUN_STATUS_DONE;
      try
      {
         out.close();
      }
      catch(final IOException iox)
      {
         iox.printStackTrace();
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
   }
   
   public int getRunStatus()
   {
      return runStatus;
   }
   
   public int getErrorStatus()
   {
      return errorStatus;
   }
   
   public void declareError()
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_NO_ERROR)
      {
         errorStatus = GridRipperWriter._ERROR_STATUS_ERROR;
      }
   }
   
   public void declareFatalError()
   {
      errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
   }
   
   private final BufferedWriter out;
   
   private int runStatus;
   private int errorStatus;
   
   public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");
   public static final String _SEPARATOR = ",";
}
