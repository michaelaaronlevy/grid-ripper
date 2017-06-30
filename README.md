# grid-ripper
Extract text from PDFs to .csv format, content is in one column, other columns have info like coordinates on the page for the text

Dependencies: PDFBox 2.X - assuming you are using the Eclipse IDE, you will need to download PDFBox and drag the jar file into the "lib" folder of your Eclipse project

GridRipper uses PDFBox to get the text from 1-n PDFs, and outputs this information to a .csv file.  There is only a single class, which extends the PDFTextStripper class of PDFBox.

This class was developed to process documents that litigants produce only in PDF format (as opposed to: native electronic format).  They do this intentionally, to deny us the ability to put the data into a spreadsheet where we can analyze it programmatically (calculating things like overtime, doubletime, etc.).  With the .csv output from this program, you can attempt to create a spreadsheet containing the information held by the PDF.

For example, you can sort by the y-coordinate of the text, and then delete all text on the top 1 inch of the page (if every page has a header that you want to get rid of).  You can use the x-coordinate to determine which column the data goes into.  You can use the y-coordinates (along with the page number) to determine whether two words are on the same line.  With that information, you can try to build a spreadsheet manually, or you could use a Java program to parse the .csv file.

michael@levycivilrights.com



