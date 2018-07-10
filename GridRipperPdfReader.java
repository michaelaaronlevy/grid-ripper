import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * this class, based on the PDFTextStripper class from PDFBox, handles reading
 * (ripping) data from PDFs and passing the data on to a writer
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
class GridRipperPdfReader extends PDFTextStripper
{
   /**
    * @param mode
    *           indicates whether the reader should obtain output in the form of
    *           phrases/words/characters
    * @param controller
    *           the object overseeing the operation of this reader
    * @throws IOException
    */
   GridRipperPdfReader(final int mode, final GridRipperController controller) throws IOException
   {
      super();
      setSortByPosition(true);
      this.mode = mode == _MODE_WORDS ? _MODE_WORDS : (mode == _MODE_CHARACTERS ? _MODE_CHARACTERS : _MODE_PHRASES);
      setArticleEnd("");
      setArticleStart("");
      setLineSeparator("");
      setPageEnd("");
      setPageStart("");
      setParagraphEnd("");
      setParagraphStart("");
      setWordSeparator("");
      this.controller = controller;
   }
   
   /**
    * open a PDF, read/rip its contents, and send to a GridRipperWriter
    * 
    * @param f
    *           the PDF file to be read/ripped (of course, the file is not
    *           modified by this method)
    * @throws IOException
    */
   void processFile(final File f) throws IOException
   {
      if(controller.out.getErrorStatus() == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      final BufferedWriter b = new BufferedWriter(new OutputStreamWriter(System.err));
      PDDocument doc = PDDocument.load(f);
      controller.setPdfPages(doc.getNumberOfPages());
      writeText(doc, b);
      doc.close();
      b.flush();
   }
   
   protected void writeString(String text, List<TextPosition> textPositions) throws IOException
   {
      if(controller.out.getErrorStatus() == GridRipperWriter._ERROR_STATUS_CRASH)
      {
         return;
      }
      else if(mode == _MODE_PHRASES)
      {
         writeStringPhrases(text, textPositions);
      }
      else if(mode == _MODE_WORDS)
      {
         writeStringWords(text, textPositions);
      }
      else
      {
         writeStringCharacters(text, textPositions);
      }
   }
   
   /**
    * this method captures each phrase as a separate entry and sends them to the
    * GridRipperPage object, which holds them until the page is completely
    * processed by this PDF reader.
    */
   private void writeStringPhrases(String text, List<TextPosition> textPositions) throws IOException
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
         if(diff[i - 1] < -spaceWidth || diff[i - 1] > 2.0 * spaceWidth)
         {
            current = new ArrayList<TextPosition>();
            words.add(current);
         }
         current.add(tp);
      }
      
      for(final ArrayList<TextPosition> ttpp : words)
      {
         final GridRipperRow r = new GridRipperRow(ttpp);
         if(r.content.trim().length() > 0)
         {
            p.add(r);
         }
      }
   }
   
   private void writeStringWords(String text, List<TextPosition> textPositions) throws IOException
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
      
      for(int i = 0; i < textPositions.size(); i++)
      {
         final TextPosition tp = textPositions.get(i);
         final char c = tp.getUnicode().charAt(0);
         if(c < 32 || Character.isWhitespace(c))
         {
            if(!current.isEmpty())
            {
               current = new ArrayList<TextPosition>();
               words.add(current);
            }
         }
         else
         {
            if(!current.isEmpty() && (diff[i - 1] < -spaceWidth || diff[i - 1] > 2.0 * spaceWidth))
            {
               current = new ArrayList<TextPosition>();
               words.add(current);
            }
            current.add(tp);
         }
      }
      
      for(final ArrayList<TextPosition> ttpp : words)
      {
         if(!ttpp.isEmpty())
         {
            final GridRipperRow r = new GridRipperRow(ttpp);
            if(r.content.trim().length() > 0)
            {
               p.add(r);
            }
         }
      }
   }
   
   /**
    * this method captures each character as a separate entry and sends them to
    * the GridRipperPage object, which holds them until the page is completely
    * processed by this PDF reader.
    */
   private void writeStringCharacters(String text, List<TextPosition> textPositions) throws IOException
   {
      for(final TextPosition tp : textPositions)
      {
         p.add(new GridRipperRow(tp));
      }
   }
   
   protected void startPage(final PDPage page)
   {
      controller.startPage();
   }
   
   /**
    * when the reader is almost done processing a page, the last thing to do is
    * to invoke the GridRipperPage object's writePage method. The
    * GridRipperWriter does not actually start writing data from the PDF page
    * until this point.
    */
   protected void endPage(final PDPage page) throws IOException
   {
      p.writePage(controller);
   }
   
   protected float[] values = new float[5];
   private final GridRipperController controller;
   private final GridRipperPage p = new GridRipperPage();
   private final int mode;
   
   private static final int _MODE_PHRASES = 0;
   private static final int _MODE_WORDS = 1;
   private static final int _MODE_CHARACTERS = 2;
}
