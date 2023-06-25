import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

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
        processPage(document,7,0);//pageidx starts at 1
    }
    public static void getQuestions() throws IOException{
        String fname="./pdfs/USABO 17 Semifinal Final.Web.pdf";
        PDDocument document = Loader.loadPDF(new File(fname));
        int startIdx;


    }
    public static void processPage(PDDocument doc,int pageIdx, int startLine) throws IOException{
        LocationGetter locator = new LocationGetter();
        locator.setSortByPosition(true);
        locator.setStartPage(pageIdx);
        locator.setEndPage(pageIdx);
        locator.getText(doc);
        ImageGetter ig=new ImageGetter();
        ig.processPage(doc.getPage(pageIdx));
        List<BufferedImage> imgs=ig.getImgs();
        List<Float> imgYs=ig.getImgYs();
        List<String> lines = locator.getLines();
        List<Float> positions = locator.getYcoords();
        List<SingleQuestion> questions = new ArrayList<SingleQuestion>();
        int lineIdx=0;
        if(startLine==-1) {// if first page
            for (; lineIdx < lines.size(); ++lineIdx) {
                String line = lines.get(lineIdx);
                if (line.toLowerCase().contains("part a")){
                    break;
                }
                //startline should be the line that contains the newline right after the previous question
            }
            startLine=lineIdx;
        }
        //System.out.println("start line: "+lines.get(startLine)+" lines length: "+lines.size());
        int nextEnd=getEnd(lines,startLine);
        //System.out.println("next end: "+lines.get(nextEnd));
        int rlim=0;
        //int imgIdx=0;
        while(nextEnd!=-1 && rlim++<20){
            List<String> question=lines.subList(startLine,nextEnd+1);
            List<BufferedImage> qImgs=new ArrayList<BufferedImage>();
            for(int i=0;i<imgs.size();++i){
                float y=imgYs.get(i);
                if(y<positions.get(nextEnd+1)&& y>positions.get(startLine)){
                    qImgs.add(imgs.get(i));
                }
            }
            SingleQuestion qObj=new SingleQuestion(question,qImgs);
            printList(question);
            startLine=nextEnd;
            nextEnd=getEnd(lines,startLine);
        }

    }
    public static void printList(List<String> q){
        System.out.println("printing list: ");
        for(String line:q){
            System.out.println(line);
        }
        System.out.println("printed list end");
    }
    public static int getEnd(List<String> lines,int startLine){

        //+1 on startLine to skip the first question bit with the number
        int startsWithNumberCount=0;
        int dIdx=-1;
        for(int i=startLine+1;i<lines.size();++i){
            //System.out.println("buns "+lines.get(i)+"; numbers found: "+startsWithNumberCount+", dIdx is: "+dIdx+"; line num is "+i);
            if(lines.get(i).contains("D.")){
                dIdx=i;
            }
            if(lines.get(i).contains("E.")){
                ++i;
                while(lines.get(i).replaceAll("\\s","").length()>1){
                    ++i;
                }
                System.out.println("found E, end line should be "+lines.get(i));
                return i;//index of blank line
            }
            else if(startsWithNumber(lines.get(i))) {
                ++startsWithNumberCount;
                if(startsWithNumberCount==2){
                    if(dIdx==-1){
                        //System.out.println("dIdx is -1 tripped");
                        int k=startLine+1;
                        while(lines.get(k).replaceAll("\\s","").length()>1){
                            ++k;
                        }
                        System.out.println("cannot find D before next question, lines.get(k) is "+lines.get(k));
                        return k;
                    }
                    //else
                    int j=dIdx+1;
                    while(lines.get(j).replaceAll("\\s","").length()>1){
                        ++j;
                    }
                    System.out.println("could not find E before next question, lines.get(j) is "+lines.get(j));
                    return j;
                }
            }
        }
        System.out.println("couldnt find anything, returning -1");
        return -1;
    }

    public static boolean startsWithNumber(String line){
        String numbers=" 0123456789";
        boolean unbroken=true;
        for(int i=0;i<line.length();++i){
            if(line.charAt(i) == '.' && i>0){
                System.out.println("starts with number: "+line);
                return unbroken;
            }else if(!numbers.contains(line.substring(i,i+1))){
                return false;
            }
        }
        return false;
    }
}
