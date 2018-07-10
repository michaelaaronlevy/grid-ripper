import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * This is the Graphical User Interface for the GridRipper program.
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
public class GridRipperGUI extends JFrame implements ActionListener, ComponentListener, Runnable
{
   /**
    * 
    * @param args
    *           empty/null: open the GUI for human access; 1 argument: the file
    *           with a list of instructions; 2...n arguments: a list of
    *           instructions to run (note: as of v0.2a, only the GUI works)
    */
   public static void main(String[] args)
   {
      new GridRipperGUI();
   }
   
   private GridRipperGUI()
   {
      this.addComponentListener(this);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setTitle(_TITLE);
      setVisible(false);
      
      final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      screenWidth = gd.getDisplayMode().getWidth();
      screenHeight = gd.getDisplayMode().getHeight();
      dimensionMain = new Dimension(screenWidth / 2, (screenHeight * 9) / 10);
      dimensionJFC = new Dimension(screenWidth / 2, (screenHeight * 7) / 10);
      loadFonts();
      
      jfcAdd = new JFileChooser();
      jfcAdd.setPreferredSize(dimensionJFC);
      jfcAdd.setDialogTitle(_FILE_CHOOSER_ADD_MESSAGE);
      jfcAdd.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfcAdd.addChoosableFileFilter(_PDF_ONLY);
      jfcAdd.setFileFilter(_PDF_ONLY);
      jfcAdd.setMultiSelectionEnabled(true);
      
      jfcOut = new JFileChooser();
      jfcOut.setPreferredSize(dimensionJFC);
      jfcOut.setDialogTitle(_FILE_CHOOSER_OUT_MESSAGE);
      jfcOut.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfcOut.setMultiSelectionEnabled(false);
      
      upButton.addActionListener(this);
      downButton.addActionListener(this);
      removeButton.addActionListener(this);
      addButton.addActionListener(this);
      csvButton.addActionListener(this);
      wordsButton.addActionListener(this);
      runButton.addActionListener(this);
      helpButton.addActionListener(this);
      returnButton.addActionListener(this);
      
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      
      final JPanel fileButtons = new JPanel();
      fileButtons.add(upButton);
      fileButtons.add(downButton);
      fileButtons.add(removeButton);
      fileButtons.add(addButton);
      
      final JPanel runButtons = new JPanel();
      runButtons.add(helpButton);
      runButtons.add(csvButton);
      runButtons.add(wordsButton);
      runButtons.add(runButton);
      
      fileView.setLayout(new BorderLayout());
      fileView.add(fileButtons, BorderLayout.NORTH);
      fileView.add(filesDisplay, BorderLayout.CENTER);
      fileView.add(runButtons, BorderLayout.SOUTH);
      
      statusView.setLayout(new BoxLayout(statusView, BoxLayout.Y_AXIS));
      statusView.add(run1);
      statusView.add(run2);
      
      helpView.setLayout(new BorderLayout());
      helpView.add(buffer1, BorderLayout.WEST);
      helpView.add(help, BorderLayout.CENTER);
      helpView.add(buffer2, BorderLayout.EAST);
      helpView.add(returnButton, BorderLayout.SOUTH);
      returnButton.addActionListener(this);
      
      this.setFont(mainFont);
      setFonts(fileView.getComponents(), mainFont);
      setFonts(helpView.getComponents(), mainFont);
      setFonts(statusView.getComponents(), mainFont);
      jfcAdd.setFont(mainFont);
      jfcOut.setFont(mainFont);
      setFonts(jfcAdd.getComponents(), listFont);
      setFonts(jfcOut.getComponents(), listFont);
      
      currentView = fileView;
      add(currentView);
      revalidate();
      
      setVisible(true);
      setSize(dimensionMain);
      setPreferredSize(dimensionMain);
      
      wordsButton.setPreferredSize(wordsButton.getSize());
      wordsButton.setText(_PHRASES_MESSAGE);
      csvButton.setPreferredSize(csvButton.getSize());
      
      final Dimension d = getMinimumSize();
      d.height *= 3;
      d.height /= 2;
      d.width *= 3;
      d.width /= 2;
      minHeight = d.height;
      minWidth = d.width;
      setMinimumSize(d);
      setPreferredSize(d);
      fileView.setMinimumSize(d);
      fileView.setPreferredSize(d);
      helpView.setMinimumSize(d);
      helpView.setPreferredSize(d);
      statusView.setMinimumSize(d);
      statusView.setPreferredSize(d);
      
      new Thread(this).start();
   }
   
   private void loadFonts()
   {
      final int v = screenWidth / 2 > screenHeight ? screenHeight : screenWidth / 2;
      float bigSize = ((v + 280) / 50);
      float smallSize = ((v + 800) / 120);
      float mainSize = (int) ((bigSize + smallSize) / 2);
      bigSize = (int) bigSize;
      smallSize = (int) smallSize;
      
      try
      {
         Font lib = Font.createFont(Font.PLAIN, getClass().getResourceAsStream("/ttf/LinLibertine_DRah.ttf"));
         listFont = lib.deriveFont(Font.PLAIN, smallSize);
         mainFont = lib.deriveFont(Font.PLAIN, mainSize);
      }
      catch(final Exception exc)
      {
         listFont = new Font(Font.SERIF, Font.PLAIN, 12);
         mainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
      }
   }
   
   private void setFonts(final Component[] c, final Font font)
   {
      for(int x = 0; x < c.length; x++)
      {
         boolean flag = true;
         for(int i = 0; i < components.size(); i++)
         {
            if(components.get(i) == c[x])
            {
               flag = false;
            }
         }
         if(flag)
         {
            components.add(this);
            if(c[x] instanceof Container)
            {
               setFonts(((Container) c[x]).getComponents(), font);
            }
            try
            {
               c[x].setFont(font);
            }
            catch(Exception e)
            {
               // do nothing
            }
         }
      }
   }
   
   public void actionPerformed(final ActionEvent aev)
   {
      final Object source = aev.getSource();
      if(source == returnButton)
      {
         updateView(fileView);
      }
      else if(source == csvButton)
      {
         csv = !(csv);
         csvButton.setText(csv ? _CSV_MESSAGE : _ODS_MESSAGE);
      }
      else if(source == wordsButton)
      {
         mode++;
         if(mode == 3)
         {
            mode = 0;
         }
         if(mode == 0)
         {
            wordsButton.setText(_PHRASES_MESSAGE);
         }
         else if(mode == 1)
         {
            wordsButton.setText(_WORDS_MESSAGE);
         }
         else
         {
            wordsButton.setText(_CHARACTERS_MESSAGE);
         }
      }
      else if(source == upButton)
      {
         final int i = list.getSelectedIndex();
         if(i > 0)
         {
            final File one = filesToRip.getElementAt(i);
            final File two = filesToRip.getElementAt(i - 1);
            filesToRip.set(i, two);
            filesToRip.set(i - 1, one);
            list.setSelectedIndex(i - 1);
         }
      }
      else if(source == downButton)
      {
         final int i = list.getSelectedIndex();
         if(i >= 0 && i < filesToRip.size() - 1)
         {
            final File one = filesToRip.getElementAt(i);
            final File two = filesToRip.getElementAt(i + 1);
            filesToRip.set(i, two);
            filesToRip.set(i + 1, one);
            list.setSelectedIndex(i + 1);
         }
      }
      else if(source == removeButton)
      {
         final int i = list.getSelectedIndex();
         if(i == -1)
         {
            return;
         }
         filesToRip.remove(i);
         list.validate();
      }
      else if(source == addButton)
      {
         final int x = jfcAdd.showOpenDialog(this);
         if(x == JFileChooser.APPROVE_OPTION)
         {
            final File[] ff = jfcAdd.getSelectedFiles();
            for(final File f : ff)
            {
               if(!filesToRip.contains(f))
               {
                  filesToRip.addElement(f);
               }
            }
            list.validate();
         }
      }
      else if(source == runButton)
      {
         if(filesToRip.isEmpty())
         {
            JOptionPane.showMessageDialog(this, "No PDF files selected to rip.", "Error", JOptionPane.ERROR_MESSAGE);
         }
         else
         {
            if(jfcOut.showDialog(this, "Output File") != JFileChooser.APPROVE_OPTION)
            {
               return;
            }
            File targetOut = jfcOut.getSelectedFile();
            if(targetOut == null)
            {
               return;
            }
            final int dot = targetOut.getName().indexOf(".");
            if(dot == -1)
            {
               targetOut = new File(targetOut.getParentFile(), targetOut.getName() + (csv ? ".csv" : ".ods"));
            }
            
            if(targetOut.exists())
            {
               if(!targetOut.canWrite())
               {
                  JOptionPane.showMessageDialog(this, "Cannot write to: " + targetOut.getName(), "File Access Error",
                        JOptionPane.ERROR_MESSAGE);
                  return;
               }
               if(JOptionPane.showConfirmDialog(this, "Overwrite Existing File?", "Confirm Overwrite",
                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
               {
                  return;
               }
            }
            
            updateView(statusView);
            
            final Object[] filesOb = filesToRip.toArray();
            final File[] files = new File[filesOb.length];
            for(int i = 0; i < files.length; i++)
            {
               files[i] = (File) filesOb[i];
            }
            
            final GridRipperWriter out = csv ? new GridRipperCsvWriter(targetOut)
                  : new GridRipperOdsStreamWriter(targetOut);
            c = new GridRipperController(files, out, mode);
            new Thread(c).start();
         }
      }
      else if(source == helpButton)
      {
         updateView(helpView);
      }
   }
   
   public void run()
   {
      while(c == null || c.out.getRunStatus() != GridRipperWriter._RUN_STATUS_DONE)
      {
         try
         {
            Thread.sleep(500);
         }
         catch(final InterruptedException iex)
         {
            
         }
         if(c != null && currentView == statusView)
         {
            updateStatus(c);
         }
      }
      if(c != null)
      {
         updateStatus(c);
      }
      if(c.out.getErrorStatus() == GridRipperWriter._ERROR_STATUS_NO_ERROR)
      {
         JOptionPane.showMessageDialog(this, "Execution Complete with no errors. Press okay to exit GridRipper.",
               "Execution Complete", JOptionPane.INFORMATION_MESSAGE);
         System.exit(0);
      }
      else if(c.out.getErrorStatus() == GridRipperWriter._ERROR_STATUS_ERROR)
      {
         JOptionPane.showMessageDialog(this,
               "Execution Complete but there were errors. The output may be corrupt or incomplete.",
               "Execution Complete", JOptionPane.WARNING_MESSAGE);
      }
      else
      {
         JOptionPane.showMessageDialog(this,
               "GridRipper could not complete the requested action because of a fatal error.", "Execution Aborted",
               JOptionPane.WARNING_MESSAGE);
      }
   }
   
   private void updateView(final JComponent newView)
   {
      this.remove(currentView);
      currentView = newView;
      this.add(currentView);
      pack();
      setSize(dimensionMain);
   }
   
   private void updateStatus(final GridRipperController c)
   {
      final int fileNumber = c.getFileNumber();
      String progress = c.getPdfPercent();
      if(progress.charAt(0) == '.')
      {
         progress = "0" + progress;
      }
      final int runStatus = c.out.getRunStatus();
      final int errorStatus = c.out.getErrorStatus();
      
      if(runStatus == GridRipperWriter._RUN_STATUS_RUNNING)
      {
         if(errorStatus == GridRipperWriter._ERROR_STATUS_NO_ERROR)
         {
            run1.setText(_RUNNING_NO_ERRORS);
         }
         else if(errorStatus == GridRipperWriter._ERROR_STATUS_ERROR)
         {
            run1.setText(_RUNNING_WITH_ERRORS);
         }
         else
         {
            run1.setText(_RUNNING_FATAL_ERROR);
         }
         if(filesToRip.size() == 1)
         {
            run2.setText(_PROCESSING_FILE + progress);
         }
         else
         {
            run2.setText(_PROCESSING_FILE + fileNumber + " of " + filesToRip.size() + " - " + progress);
         }
      }
      else if(runStatus == GridRipperWriter._RUN_STATUS_NOT_STARTED)
      {
         run1.setText(_NOT_STARTED);
         run2.setText(_NOT_STARTED);
      }
      else
      {
         if(errorStatus == GridRipperWriter._ERROR_STATUS_NO_ERROR)
         {
            run1.setText(_NOT_RUNNING_NO_ERRORS);
         }
         else if(errorStatus == GridRipperWriter._ERROR_STATUS_ERROR)
         {
            run1.setText(_NOT_RUNNING_WITH_ERRORS);
         }
         else
         {
            run1.setText(_NOT_RUNNING_FATAL_ERROR);
         }
         run2.setText("");
      }
   }
   
   public void componentResized(final ComponentEvent cev)
   {
      Dimension d = getSize();
      if(d.width < minWidth)
      {
         d.width = minWidth;
      }
      if(d.height < minHeight)
      {
         d.height = minHeight;
      }
      setSize(d);
   }
   
   public void componentHidden(final ComponentEvent cev)
   {
      // do nothing
   }
   
   public void componentMoved(final ComponentEvent cev)
   {
      // do nothing
   }
   
   public void componentShown(final ComponentEvent cev)
   {
      // do nothing
   }
   
   private JComponent currentView = null;
   
   private final JPanel fileView = new JPanel();
   private final JPanel statusView = new JPanel();
   private final JPanel helpView = new JPanel();
   
   private final DefaultListModel<File> filesToRip = new DefaultListModel<File>();
   private final JList<File> list = new JList<File>(filesToRip);
   private final JScrollPane filesDisplay = new JScrollPane(list);
   private final JButton upButton = new JButton(_UP_BUTTON);
   private final JButton downButton = new JButton(_DOWN_BUTTON);
   private final JButton removeButton = new JButton(_REMOVE_BUTTON);
   private final JButton addButton = new JButton(_ADD_BUTTON);
   private final JButton csvButton = new JButton(_ODS_MESSAGE);
   private final JButton wordsButton = new JButton(_CHARACTERS_MESSAGE);
   private final JButton runButton = new JButton(_RUN_MESSAGE);
   private final JButton helpButton = new JButton(_HELP_MESSAGE);
   private final JLabel buffer1 = new JLabel("\u2003\u2003\u2003");
   private final JLabel buffer2 = new JLabel("\u2003\u2003\u2003");
   private final JFileChooser jfcAdd;
   private final JFileChooser jfcOut;
   
   private final JLabel help = new JLabel(_HELP);
   private final JButton returnButton = new JButton(_RETURN);
   
   private final JLabel run1 = new JLabel(_NOT_STARTED);
   private final JLabel run2 = new JLabel(_NOT_STARTED);
   
   private final int screenHeight;
   private final int screenWidth;
   private final int minHeight;
   private final int minWidth;
   private final Dimension dimensionMain;
   private final Dimension dimensionJFC;
   
   private boolean csv = false;
   private int mode = 0;
   private GridRipperController c = null;
   
   private final FileNameExtensionFilter _PDF_ONLY = new FileNameExtensionFilter("PDF (Paper Description Format) Files",
         "pdf");
   public static final String _TITLE = "GridRipper v1.0 - Michael Levy, Civil Rights Lawyer - michael@levycivilrights.com";
   
   private Font listFont;
   private Font mainFont;
   private final ArrayList<Component> components = new ArrayList<Component>(99);
   
   private static final String _UP_BUTTON = "\u2191";
   private static final String _DOWN_BUTTON = "\u2193";
   private static final String _REMOVE_BUTTON = "-";
   private static final String _ADD_BUTTON = "+";
   private static final String _FILE_CHOOSER_ADD_MESSAGE = "Select PDF(s) to Rip.";
   private static final String _FILE_CHOOSER_OUT_MESSAGE = "Select Output File.";
   private static final String _ODS_MESSAGE = ".ods";
   private static final String _CSV_MESSAGE = ".csv";
   private static final String _PHRASES_MESSAGE = "Phrases";
   private static final String _WORDS_MESSAGE = "Words";
   private static final String _CHARACTERS_MESSAGE = "Characters";
   private static final String _RUN_MESSAGE = "Execute";
   private static final String _HELP_MESSAGE = "Help/About";
   
   private static final String _NOT_STARTED = "Not Started.";
   private static final String _RUNNING_NO_ERRORS = "Running - No Errors.";
   private static final String _RUNNING_WITH_ERRORS = "Running - There are errors, which means the data output may be incomplete or corrupted. (GridRipper does not alter the input PDFs.)";
   private static final String _RUNNING_FATAL_ERROR = "Fatal Error - GridRipper will stop running and there will be no output. (GridRipper does not alter the input PDFs.)";
   private static final String _NOT_RUNNING_NO_ERRORS = "Finished Running - No Errors.";
   private static final String _NOT_RUNNING_WITH_ERRORS = "Finished Running - There are errors, which means the data output may be incomplete or corrupted. (GridRipper does not alter the input PDFs.)";
   private static final String _NOT_RUNNING_FATAL_ERROR = "Fatal Error - GridRipper has stopped running and there will be no output. (GridRipper does not alter the input PDFs.)";
   private static final String _PROCESSING_FILE = "Processing File: ";
   
   private static final String _HELP = "<html>\u2003<p><b><u>Using GridRipper</u>:</b><ul><li>Select the PDF(s) to rip.  (Ripping does not change the PDFs.)  Use the [ + ] button to add PDFs.</li><li>You can remove PDFs from the list with the [ - ] button, or re-order them with the [ \u2191 ] and [ \u2193 ] buttons.</li><li>Choose the output format (.ods or .csv – both are spreadsheet formats that can be opened with Microsoft Excel, Apache OpenOffice, or LibreOffice).</li><li>Choose whether the content is arranged as phrases, words, or letters (“phrases” is recommended).</li><li>Press “Execute” to begin ripping. You will be asked to choose an output file. If this file exists, you will be asked whether to overwrite it.</ul><br><p><b><u>About GridRipper</u>:</b><br>\u2003\u2003This tool was designed to address a very specific problem: during litigation, data is commonly provided in PDF format (or in paper, that you can scan into PDF format). The data in the PDFs may be critical to your case, but you can only “access” it by eyeballing it, or by copying it out in an awkward way, such as “selecting” everything on a page, copying it, and pasting it into a Word document. Where the PDF has columns of data, usually the Word document won’t have the data lined up in neat columns: the data will be a jumbled mess. Un-jumbling it can take hours. Typing the data into a spreadsheet by hand can take hours.<br>\u2003<p>\u2003\u2003GridRipper pulls the content of the PDFs out, but saves contextual information (the location on the page of each phrase/word).  With this contextual information, you can easily, quickly, and systematically determine which row/column each datum belongs in – so you can quickly and reliably create a spreadsheet with the data organized the same way it was in the PDF (or, more to the point: the same way it was organized in the document that was used to generate the PDF).<br>\u2003<p>\u2003\u2003GridRipper can only “see” text that is recognized by Acrobat. If your document is a scan of a printed page, Acrobat will not see the words unless Optical Character Recognition (“OCR”) is performed. The OCR process is rarely perfect and often results in errors. But I have used GridRipper successfully when OCR is high quality.<br>\u2003<p><b><u>About Michael Levy</u>:</b><p>\u2003\u2003Mr. Levy is an employment lawyer in the San Francisco Bay Area/East Bay.  He represents employees against abusive employers in claims for unpaid or underpaid wages, harassment, discrimination, wrongful termination, and other workplace grievances.  For questions or technical support, contact michael@levycivilrights.com.  Please include “GridRipper” in the subject line of your email.<p>\u2003<p>\u2003<p>\u2003</html>";
   
   private static final String _RETURN = "Return";
   
   public static final int _MODE_PHRASES = 0;
   public static final int _MODE_WORDS = 1;
   public static final int _MODE_CHARACTERS = 2;
}
