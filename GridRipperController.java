import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
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
public class GridRipperController implements Runnable
{
   public GridRipperController(final File[] files, final GridRipperWriter out, final int mode)
   {
      permissionsCount = columnNames.length;
      this.files = files;
      this.currentFileNumber = -1;
      this.out = out;
      GridRipperPdfReader reader = null;
      try
      {
         reader = new GridRipperPdfReader(mode, this);
      }
      catch(final IOException iox)
      {
         iox.printStackTrace();
         out.declareFatalError();
      }
      this.in = reader;
      this.id = -1;
      this.pdfPage = -1;
      this.pid = -1;
   }
   
   public void run()
   {
      if(out.getErrorStatus() == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      
      out.open(this);
      
      out.startRow();
      out.writeText(GridRipperGUI._TITLE);
      
      int permissionsCount = -3;
      for(int i = 0; i < printPermissions.length; i++)
      {
         if(printPermissions[i])
         {
            permissionsCount++;
         }
      }
      for(int i = 0; i < permissionsCount; i++)
      {
         out.writeBlank();
      }
      out.writeText("Run Date:");
      out.writeDate(System.currentTimeMillis());
      out.endRow();
      for(int i = 0; i < files.length; i++)
      {
         out.startRow();
         out.writeText("File " + (i + 1) + ":");
         out.writeText(getFilePath(i));
         out.writeText(getFileName(i));
         out.endRow();
      }
      out.startRow();
      out.endRow();
      writeHeader();
      
      for(final File f : files)
      {
         if(out.getErrorStatus() == GridRipperWriter._ERROR_STATUS_CRASH)
         {
            return;
         }
         
         currentFileNumber++;
         try
         {
            pdfPage = -1;
            out.startPDF();
            in.processFile(f);
            out.endPDF();
         }
         catch(final IOException iex)
         {
            out.declareError();
            iex.printStackTrace();
         }
      }
      out.close();
   }
   
   private void writeHeader()
   {
      out.startRow();
      for(int i = 0; i < printPermissions.length; i++)
      {
         if(getPermission(i))
         {
            out.writeText(getColumnName(i));
         }
      }
      out.endRow();
   }
   
   void startPage()
   {
      pdfPage++;
      pid++;
   }
   
   public boolean getPermission(final int id)
   {
      return printPermissions[id];
   }
   
   public String getColumnName(final int id)
   {
      return columnNames[id];
   }
   
   public int getFileNumber()
   {
      return currentFileNumber + 1;
   }
   
   public String getFilePath()
   {
      return files[currentFileNumber].getParent();
   }
   
   public String getFileName()
   {
      return files[currentFileNumber].getName();
   }
   
   public String getFilePath(final int id)
   {
      return files[id].getParent();
   }
   
   public String getFileName(final int id)
   {
      return files[id].getName();
   }
   
   /**
    * 
    * @return the number of PDF files that this controller will attempt to
    *         process
    */
   public int getFileCount()
   {
      return files.length;
   }
   
   /**
    * 
    * @return a unique identifier, that increases incrementally, for each
    *         separate bit of content from the PDFs being read
    */
   public int getId()
   {
      return id;
   }
   
   /**
    * 
    * @return
    */
   public int getPdfPage()
   {
      return pdfPage;
   }
   
   public int getPid()
   {
      return pid;
   }
   
   /**
    * the ods stream writer is only compatible with these particular
    * permissions.
    */
   void odsStreamPrintPermissions()
   {
      Arrays.fill(printPermissions, true);
      printPermissions[2] = false;
      printPermissions[3] = false;
   }
   
   int incrementId()
   {
      return ++id;
   }
   
   void setPdfPages(final int p)
   {
      pdfPagesTotal = p;
   }
   
   /**
    * @return 10 times the progress percentage; should only be 1000 if the file
    *         is entirely complete.
    */
   String getPdfPercent()
   {
      final int p = pdfPagesTotal <= 0 ? 0 : ((pdfPage < 0 ? 0 : pdfPage) * 1000) / pdfPagesTotal;
      String s = (p < 100 ? " " : "") + (p == 0 ? "0" : "") + p + "%.";
      return s.substring(0, s.length() - 3) + "." + s.substring(s.length() - 3);
   }
   
   public final GridRipperWriter out;
   
   private int id;
   private int pdfPage;
   private int pdfPagesTotal = 0;
   private int pid;
   
   private int currentFileNumber;
   private final File[] files;
   private final GridRipperPdfReader in;
   
   private final boolean[] printPermissions = { true, true, false, false, true, true, true, true, true, true, true,
         true, true };
   
   private final String[] columnNames = { "row_id", "file_number", "file_path", "file_name", "pdf_page", "total_page",
         "y_start", "y_smooth", "x_start", "x_end", "font_size", "rotation", "content" };
   
   public final int permissionsCount;
}
