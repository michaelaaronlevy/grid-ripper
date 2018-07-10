import java.util.ArrayList;

import org.apache.pdfbox.text.TextPosition;

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
class GridRipperRow implements Comparable<GridRipperRow>
{
   GridRipperRow(final TextPosition tp)
   {
      y_start = tp.getY();
      x_start = tp.getX();
      x_end = tp.getEndX();
      font_size = tp.getFontSize();
      rotation = (int) (tp.getDir() + 0.1f);
      content = tp.getUnicode();
   }
   
   GridRipperRow(final ArrayList<TextPosition> ttpp)
   {
      float ys = Float.MAX_VALUE;
      float xs = Float.MAX_VALUE;
      float xe = Float.MIN_VALUE;
      final StringBuilder s = new StringBuilder();
      
      for(final TextPosition tp : ttpp)
      {
         if(ys > tp.getY())
         {
            ys = tp.getY();
         }
         if(xs > tp.getX())
         {
            xs = tp.getX();
         }
         if(xe < tp.getEndX())
         {
            xe = tp.getEndX();
         }
         s.append(tp.getUnicode());
      }
      
      y_start = ys;
      x_start = xs;
      x_end = xe;
      font_size = ttpp.get(0).getFontSize();
      rotation = (int) (ttpp.get(0).getDir() + 0.1f);
      content = s.toString();
   }
   
   public int compareTo(final GridRipperRow that)
   {
      if(this == that)
      {
         return 0;
      }
      if(this.y_smooth == that.y_smooth)
      {
         if(this.y_start == that.y_start)
         {
            if(this.x_start == that.x_start)
            {
               if(this.x_end == that.x_end)
               {
                  if(this.font_size == that.font_size)
                  {
                     if(this.rotation == that.rotation)
                     {
                        return this.content.compareTo(that.content);
                     }
                     else
                     {
                        return this.rotation < that.rotation ? -1 : 1;
                     }
                     
                  }
                  else
                  {
                     return this.font_size < that.font_size ? -1 : 1;
                  }
                  
               }
               else
               {
                  return this.x_end < that.x_end ? -1 : 1;
               }
            }
            else
            {
               return this.x_start < that.x_start ? -1 : 1;
            }
         }
         else
         {
            return this.y_start < that.y_start ? -1 : 1;
         }
      }
      else
      {
         return this.y_smooth < that.y_smooth ? -1 : 1;
      }
   }
   
   void smooth(final GridRipperRow above)
   {
      if(above == null)
      {
         this.y_smooth = this.y_start;
      }
      else if(this.y_start - above.y_smooth < _SMOOTH_FACTOR)
      {
         this.y_smooth = above.y_smooth;
      }
      else
      {
         this.y_smooth = this.y_start;
      }
   }
   
   final float y_start;
   float y_smooth = _NO_SMOOTH;
   final float x_start;
   final float x_end;
   final float font_size;
   final int rotation;
   final String content;
   
   private static final float _NO_SMOOTH = -999999.0f;
   private static final float _SMOOTH_FACTOR = 2.0f;
}
