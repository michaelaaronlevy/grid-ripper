/**
 * Classes that implement this interface receive information (collected by a PDF
 * reader directed by the controller object). They are responsible for
 * processing that information in order to present the content of the PDF in a
 * new format, a chart that lists out the content as well as information showing
 * where the content was (which file it is from, which page it was on, and where
 * the content was located on the page).
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
public interface GridRipperWriter
{
   /**
    * this method is called by the controller
    * 
    * @param c
    */
   public void open(final GridRipperController c);
   
   /**
    * the writer should write an empty cell (and then be ready to write the next
    * data)
    */
   public void writeBlank();
   
   /**
    * the writer should write the text (and then be ready to write the next
    * data)
    * 
    * @param s
    *           the text to write
    */
   public void writeText(final String s);
   
   /**
    * the writer should write the integer (and then be ready to write the next
    * data)
    * 
    * @param i
    *           the integer to write
    */
   public void writeInt(final int i);
   
   /**
    * the writer should write the floating point number (and then be ready to
    * write the next data)
    * 
    * @param f
    *           the floating-point number to write
    */
   public void writeFloat(final float f);
   
   /**
    * the writer should write the date (and then be ready to write the next
    * data)
    * 
    * @param d
    *           the date to be written
    */
   public void writeDate(final long d);
   
   /**
    * this method is called whenever the writer should start writing to a new
    * row.
    */
   public void startRow();
   
   /**
    * this method is called whenever the writer is at the end of a row.
    */
   public void endRow();
   
   /**
    * this method is called whenever the PDF reader is at the start of a new PDF
    * page.
    */
   public void startPage();
   
   /**
    * this method is called whenever the PDF reader is at the end of a PDF page.
    */
   public void endPage();
   
   /**
    * this method is called whenever the PDF reader is at the start of a new
    * PDF.
    */
   public void startPDF();
   
   /**
    * this method is called whenever the PDF reader is at the end of a PDF.
    */
   public void endPDF();
   
   /**
    * this method is called when there is no more content to add and the writer
    * needs to finish.
    */
   public void close();
   
   /**
    * 
    * @return a digit representing the run status (0 - not started; 1 - running;
    *         2 - finished running)
    */
   public int getRunStatus();
   
   /**
    * 
    * @return a digit representing the error status (0 - no error; 1 - error(s)
    *         in project but there may still be output; 2 - fatal error)
    */
   public int getErrorStatus();
   
   /**
    * if the error status is "_ERROR_STATUS_NO_ERROR" it is changed to
    * "_ERROR_STATUS_ERROR"
    */
   public void declareError();
   
   /**
    * changes the error status to _ERROR_STATUS_CRASH
    */
   public void declareFatalError();
   
   public static final int _RUN_STATUS_NOT_STARTED = 0;
   public static final int _RUN_STATUS_RUNNING = 1;
   public static final int _RUN_STATUS_DONE = 2;
   
   public static final int _ERROR_STATUS_NO_ERROR = 0;
   public static final int _ERROR_STATUS_ERROR = 1;
   public static final int _ERROR_STATUS_CRASH = 2;
   
}
