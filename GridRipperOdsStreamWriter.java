import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
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
class GridRipperOdsStreamWriter implements GridRipperWriter
{
   GridRipperOdsStreamWriter(final File target)
   {
      this.target = target;
   }
   
   public void open(GridRipperController c)
   {
      runStatus = GridRipperWriter._RUN_STATUS_RUNNING;
      controller = c;
      c.odsStreamPrintPermissions();
      ZipOutputStream zos = null;
      try
      {
         zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(target)));
         out = zos;
         
         zos.putNextEntry(new ZipEntry("Configurations2/accelerator/"));
         zos.putNextEntry(new ZipEntry("Configurations2/floater/"));
         zos.putNextEntry(new ZipEntry("Configurations2/images/"));
         zos.putNextEntry(new ZipEntry("Configurations2/menubar/"));
         zos.putNextEntry(new ZipEntry("Configurations2/popupmenu/"));
         zos.putNextEntry(new ZipEntry("Configurations2/progressbar/"));
         zos.putNextEntry(new ZipEntry("Configurations2/statusbar/"));
         zos.putNextEntry(new ZipEntry("Configurations2/toolbar/"));
         zos.putNextEntry(new ZipEntry("Configurations2/toolpanel/"));
         zos.putNextEntry(new ZipEntry("META-INF/manifest.xml"));
         resourceToZip("/ods/manifest.xml");
         zos.closeEntry();
         zos.putNextEntry(new ZipEntry("manifest.rdf"));
         resourceToZip("/ods/manifest.rdf");
         zos.closeEntry();
         zos.putNextEntry(new ZipEntry("meta.xml"));
         resourceToZip("/ods/meta.xml");
         zos.closeEntry();
         zos.putNextEntry(new ZipEntry("mimetype"));
         resourceToZip("/ods/mimetype");
         zos.closeEntry();
         zos.putNextEntry(new ZipEntry("settings.xml"));
         resourceToZip("/ods/settings.xml");
         zos.closeEntry();
         zos.putNextEntry(new ZipEntry("styles.xml"));
         resourceToZip("/ods/styles.xml");
         zos.closeEntry();
         zos.putNextEntry(new ZipEntry("Thumbnails/thumbnail.png"));
         resourceToZip("/ods/thumbnail.png");
         zos.closeEntry();
         zos.putNextEntry(new ZipEntry("content.xml"));
         resourceToZip("/ods/content-heading.txt");
         newSheet();
      }
      catch(final IOException iox)
      {
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
   }
   
   private void resourceToZip(final String resource) throws IOException
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      final InputStream is = getClass().getResourceAsStream(resource);
      while(is.available() > _BYTE_ARRAY_LENGTH)
      {
         is.read(b);
         out.write(b);
      }
      final int av = is.available();
      if(av > 0)
      {
         is.read(b, 0, av);
         out.write(b, 0, av);
      }
      is.close();
   }
   
   private void newSheet()
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      sheetStart = controller.getId();
      sheetCount++;
      String s = _SHEET_NAME + (sheetCount + 1);
      if(sheetCount == 9999)
      {
         namelen = 5;
      }
      try
      {
         if(sheetCount != 0)
         {
            out.write(_CONTENT_SHEET_END, 0, _CONTENT_SHEET_END.length);
         }
         out.write(_CONTENT_SHEET_START, 0, _CONTENT_SHEET_START.length);
         out.write(s.substring(s.length() - namelen).getBytes(), 0, namelen);
         resourceToZip("/ods/content-newsheet.txt");
         
         if(sheetCount != 0)
         {
            startRow();
            for(int i = 0; i < controller.permissionsCount; i++)
            {
               if(controller.getPermission(i))
               {
                  writeText(controller.getColumnName(i));
               }
            }
            endRow();
         }
      }
      catch(final IOException iox)
      {
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
   }
   
   public void writeBlank()
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      write(_CONTENT_BLANK);
   }
   
   public void writeText(final String s)
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      
      builder.delete(0, builder.length());
      for(int i = 0; i < s.length(); i++)
      {
         boolean flag = true;
         final char c = s.charAt(i);
         
         for(int j = 0; j < 5; j++)
         {
            if(c == _REPLACEES[j])
            {
               builder.append(_REPLACERS[j]);
               flag = false;
               break;
            }
         }
         if(flag)
         {
            builder.append(c < 32 || Character.isWhitespace(c) ? ' ' : c);
         }
      }
      write(_CONTENT_STRING_BEFORE);
      write(builder.toString());
      write(_CONTENT_STRING_AFTER);
      builder.delete(0, builder.length());
   }
   
   public void writeInt(int i)
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      final byte[] b = Integer.toString(i).getBytes(_UTF8);
      write(_CONTENT_NUMBER_BEFORE);
      write(b);
      write(_CONTENT_NUMBER_MIDDLE);
      write(b);
      write(_CONTENT_NUMBER_AFTER);
   }
   
   public void writeFloat(float f)
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      write(_CONTENT_NUMBER_BEFORE);
      write(Float.toString(f));
      write(_CONTENT_NUMBER_MIDDLE);
      write(String.format("%.2f", f));
      write(_CONTENT_NUMBER_AFTER);
   }
   
   public void writeDate(long d)
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      final Date dd = new Date(d);
      final String date = _SDF_DATE.format(dd);
      final String time = _SDF_TIME.format(dd);
      
      write(_CONTENT_DATE_BEFORE);
      write(date + "T" + time);
      write(_CONTENT_DATE_MIDDLE);
      write(date + " " + time);
      write(_CONTENT_DATE_AFTER);
   }
   
   public void startRow()
   {
      write(_CONTENT_ROW_START);
   }
   
   public void endRow()
   {
      write(_CONTENT_ROW_END);
   }
   
   public void startPage()
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      else if(sheetStart + _NEW_WORKSHEET_PAGE < controller.getId())
      {
         newSheet();
      }
   }
   
   public void endPage()
   {
      // no action is required.
   }
   
   public void startPDF()
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      else if(sheetStart + _NEW_WORKSHEET_PDF < controller.getId())
      {
         newSheet();
      }
   }
   
   public void endPDF()
   {
      // no action is required.
   }
   
   public void close()
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         runStatus = GridRipperWriter._RUN_STATUS_DONE;
         return;
      }
      try
      {
         out.write(_CONTENT_SHEET_END, 0, _CONTENT_SHEET_END.length);
         out.write(_CONTENT_END, 0, _CONTENT_END.length);
         out.closeEntry();
         out.close();
      }
      catch(final IOException iox)
      {
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
      runStatus = GridRipperWriter._RUN_STATUS_DONE;
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
   
   private void write(final byte[] toWrite)
   {
      if(errorStatus == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      try
      {
         out.write(toWrite, 0, toWrite.length);
      }
      catch(final IOException iex)
      {
         iex.printStackTrace();
         errorStatus = GridRipperWriter._ERROR_STATUS_CRASH;
      }
   }
   
   private void write(final String toWrite)
   {
      write(toWrite.getBytes(_UTF8));
   }
   
   private final File target;
   private ZipOutputStream out = null;
   private final byte[] b = new byte[_BYTE_ARRAY_LENGTH];
   private static final int _BYTE_ARRAY_LENGTH = 256;
   private final StringBuilder builder = new StringBuilder(999);
   
   private GridRipperController controller = null;
   private int runStatus = GridRipperWriter._RUN_STATUS_NOT_STARTED;
   private int errorStatus = GridRipperWriter._ERROR_STATUS_NO_ERROR;
   
   private int sheetCount = -1;
   private int sheetStart = 0;
   private int namelen = 4;
   private int _NEW_WORKSHEET_PDF = 600000;
   private int _NEW_WORKSHEET_PAGE = 800000;
   
   private static final Charset _UTF8 = org.apache.pdfbox.util.Charsets.UTF_8;
   private static final String _SHEET_NAME = "0000";
   private static final byte[] _CONTENT_SHEET_START = "<table:table table:name=\"".getBytes(_UTF8);
   private static final byte[] _CONTENT_SHEET_END = "</table:table>".getBytes(_UTF8);
   private static final byte[] _CONTENT_END = "<table:named-expressions/></office:spreadsheet></office:body></office:document-content>"
         .getBytes(_UTF8);
   private static final byte[] _CONTENT_ROW_START = "<table:table-row table:style-name=\"ro1\">".getBytes(_UTF8);
   private static final byte[] _CONTENT_ROW_END = "</table:table-row>".getBytes(_UTF8);
   
   private static final byte[] _CONTENT_NUMBER_BEFORE = "<table:table-cell calcext:value-type=\"float\" office:value-type=\"float\" office:value=\""
         .getBytes(_UTF8);
   private static final byte[] _CONTENT_NUMBER_MIDDLE = "\"><text:p>".getBytes(_UTF8);
   private static final byte[] _CONTENT_NUMBER_AFTER = "</text:p></table:table-cell>".getBytes(_UTF8);
   
   private static final byte[] _CONTENT_DATE_BEFORE = "<table:table-cell table:style-name=\"ce3\" calcext:value-type=\"date\" office:value-type=\"date\" office:date-value=\""
         .getBytes(_UTF8);
   private static final byte[] _CONTENT_DATE_MIDDLE = "\"><text:p>".getBytes(_UTF8);
   private static final byte[] _CONTENT_DATE_AFTER = _CONTENT_NUMBER_AFTER;
   
   private static final byte[] _CONTENT_STRING_BEFORE = "<table:table-cell calcext:value-type=\"string\" office:value-type=\"string\"><text:p>"
         .getBytes(_UTF8);
   private static final byte[] _CONTENT_STRING_AFTER = _CONTENT_NUMBER_AFTER;
   
   private static final byte[] _CONTENT_BLANK = "<table:table-cell/>".getBytes(_UTF8);
   
   private static final char[] _REPLACEES = { '\"', '\'', '&', '<', '>' };
   private static final String[] _REPLACERS = { "&quot;", "&apos;", "&amp;", "&lt;", "&gt;" };
   
   private static final SimpleDateFormat _SDF_DATE = new SimpleDateFormat("yyyy-MM-dd");
   private static final SimpleDateFormat _SDF_TIME = new SimpleDateFormat("HH:mm:ss");
}
