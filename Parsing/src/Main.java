import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class Main {
    public static void main(String[] args){
        System.out.println(System.getProperty("user.dir"));
        try{
            TestPDFBox();
        }catch (IOException e){
            System.out.println("file not found probably");
        }
    }
    public static void TestPDFBox() throws IOException {
        File usabos17 = new File("./pdfs/USABO 17 Semifinal Final.Web.pdf");
        PDDocument document = Loader.loadPDF(usabos17);
        Splitter splitter= new Splitter();
        List<PDDocument> pages=splitter.split(document);
        PDFTextStripper textGetter= new PDFTextStripper();
        System.out.println(textGetter.getText(pages.get(0)));
        LocationGetter locator=new LocationGetter();
        locator.setSortByPosition(true);
        locator.setStartPage(0);
        locator.setEndPage(2);
        locator.getText(document);
        List<String> lines = locator.getLines();
        List<Float> ycoords=locator.getYcoords();
        
        for(int i=0;i<lines.size();++i){
            System.out.println(ycoords.get(i)+": "+lines.get(i));
        }
    }
}
