import java.io.IOException;
import java.util.ArrayList;

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
class GridRipperPage
{
   GridRipperPage()
   {
      rows = new ArrayList<GridRipperRow>(9999);
   }
   
   void add(final GridRipperRow row)
   {
      rows.add(row);
   }
   
   void writePage(final GridRipperController controller) throws IOException
   {
      if(!rows.isEmpty())
      {
         rows.sort(null);
         rows.get(0).smooth(null);
         for(int i = 1; i < rows.size(); i++)
         {
            rows.get(i).smooth(rows.get(i - 1));
         }
         rows.sort(null);
      }
      
      final GridRipperWriter out = controller.out;
      final int fileNumber = controller.getFileNumber();
      final String filePath = controller.getFilePath();
      final String fileName = controller.getFileName();
      final int pdfPage = controller.getPdfPage();
      final int totalPage = controller.getPid();
      
      out.startPage();
      for(final GridRipperRow r : rows)
      {
         out.startRow();
         
         if(controller.getPermission(0))
         {
            out.writeInt(controller.incrementId());
         }
         else
         {
            controller.incrementId();
         }
         
         if(controller.getPermission(1))
            out.writeInt(fileNumber);
         if(controller.getPermission(2))
            out.writeText(filePath);
         if(controller.getPermission(3))
            out.writeText(fileName);
         if(controller.getPermission(4))
            out.writeInt(pdfPage + 1);
         if(controller.getPermission(5))
            out.writeInt(totalPage + 1);
         if(controller.getPermission(6))
            out.writeFloat(r.y_start);
         if(controller.getPermission(7))
            out.writeFloat(r.y_smooth);
         if(controller.getPermission(8))
            out.writeFloat(r.x_start);
         if(controller.getPermission(9))
            out.writeFloat(r.x_end);
         if(controller.getPermission(10))
            out.writeFloat(r.font_size);
         if(controller.getPermission(11))
            out.writeInt(r.rotation);
         if(controller.getPermission(12))
            out.writeText(r.content);
         
         out.endRow();
      }
      rows.clear();
      out.endPage();
   }
   
   private final ArrayList<GridRipperRow> rows;
}
