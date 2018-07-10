import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * rather than printing the information to a file, this class prints the
 * information to a PrintStream, such as System.out. This class was primarily
 * intended to support testing/debugging, but it could also be used to send text
 * output to a file, or you could copy the output and paste it into another
 * document.
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
public class GridRipperPrintWriter implements GridRipperWriter
{
   public GridRipperPrintWriter(final PrintStream out)
   {
      this.out = out;
   }
   
   public void open(final GridRipperController c)
   {
      status = GridRipperWriter._RUN_STATUS_RUNNING;
   }
   
   public void writeBlank()
   {
      out.print(_SEPARATOR);
   }
   
   public void writeText(String s)
   {
      out.print(s);
      out.print(_SEPARATOR);
   }
   
   public void writeInt(int i)
   {
      out.print(i);
      out.print(_SEPARATOR);
   }
   
   public void writeFloat(float f)
   {
      out.print(f);
      out.print(_SEPARATOR);
   }
   
   public void writeDate(long d)
   {
      out.print(df.format(new Date(d)));
      out.print(_SEPARATOR);
   }
   
   public void startRow()
   {
      out.print(">>\t");
   }
   
   public void endRow()
   {
      out.println();
   }
   
   public void startPage()
   {
      // no action necessary.
   }
   
   public void endPage()
   {
      // no action necessary.
   }
   
   public void startPDF()
   {
      // no action necessary.
   }
   
   public void endPDF()
   {
      // no action necessary.
   }
   
   public void close()
   {
      out.flush();
      status = GridRipperWriter._RUN_STATUS_DONE;
   }
   
   public int getRunStatus()
   {
      return status;
   }
   
   public int getErrorStatus()
   {
      return errors;
   }
   
   public void declareError()
   {
      if(errors == GridRipperWriter._ERROR_STATUS_NO_ERROR)
      {
         errors = GridRipperWriter._ERROR_STATUS_ERROR;
      }
   }
   
   public void declareFatalError()
   {
      errors = GridRipperWriter._ERROR_STATUS_CRASH;
   }
   
   private final PrintStream out;
   private int errors = GridRipperWriter._ERROR_STATUS_NO_ERROR;
   private int status = GridRipperWriter._RUN_STATUS_NOT_STARTED;
   
   public static final String _SEPARATOR = ", ";
   public static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
}
