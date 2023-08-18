import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import javax.imageio.ImageIO;

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
        PDDocument answers17=Loader.loadPDF(new File("pdfs/USABO 17 Semifinal Final Key web.pdf"));
        Splitter splitter= new Splitter();
        List<PDDocument> pages=splitter.split(document);
        /*
        PDFTextStripper textGetter= new PDFTextStripper();
        LocationGetter locator = new LocationGetter();
        locator.setSortByPosition(true);
        locator.setStartPage(6);
        locator.setEndPage(6);
        locator.getText(answers17);
        List<Float> ycoords=locator.getYcoords();
        List<String> lines=locator.getLines();
        for(int f=0;f< ycoords.size();++f){
            System.out.println(ycoords.get(f)+" "+lines.get(f));
        }
        //processPage(document,6,0,"semis17");//pageidx starts at 1
        System.out.println("----------");
        printList(getHighlightedFromPage(answers17,6));

         */
        List<String> allQuestions=new ArrayList<>();
        for(int i=0;i<35;++i) {
            List<String> pageQuestions=processPageTextOnly(document, i+1);
            allQuestions.addAll(pageQuestions);
        }
        text2file("./parsed/text17semis.txt",allQuestions);
    }
    public static void testCropping(PDDocument doc, int idx) throws IOException{
        PDFTextStripper texter=new PDFTextStripper();
        texter.setStartPage(idx);
        texter.setEndPage(idx);
        PDDocument newDoc=doc;
        newDoc.getPage(idx).setCropBox(new PDRectangle(0,200,300,200));
        System.out.println(texter.getText(newDoc));
    }
    public static void getQuestions() throws IOException{
        String fname="./pdfs/USABO 17 Semifinal Final.Web.pdf";
        PDDocument document = Loader.loadPDF(new File(fname));
        int startIdx;
    }
    public static List<String> processPageTextOnly(PDDocument doc, int pageIdx) throws IOException{
        LocationGetter locator = new LocationGetter();
        locator.setSortByPosition(true);
        locator.setStartPage(pageIdx);
        locator.setEndPage(pageIdx);
        locator.getText(doc);
        List<String> lines = locator.getLines();
        List<String> questions = new ArrayList<String>();
        List<Integer> questionNumbers=new ArrayList<Integer>();
        String currentQuestion="";
        Integer currentQuestionNumber=-2;
        int i=0;
        while(i<lines.size()){
            String line = lines.get(i);
            if(startsWithNumber(line)){
                currentQuestionNumber=getQNum(line);
                break;//gets to the first question
            }
            ++i;
        }
        while(i<lines.size()){
            String line = lines.get(i);
            if(startsWithNumber(line)){
                if(currentQuestion.length()>0) {
                    questions.add(currentQuestion);
                    questionNumbers.add(currentQuestionNumber);
                    currentQuestion="";
                    currentQuestionNumber=getQNum(line);
                }
            }
            if(line.length()>2) {
                currentQuestion += line+"{}";//{} symbolizes newline
            }
            ++i;
        }
        if(currentQuestion.length()>0){
            questions.add(currentQuestion);
        }
        for(int q=0;q<questionNumbers.size();++q){
            System.out.println("Found Question number "+questionNumbers.get(q)+": ");
            System.out.println(questions.get(q));
            System.out.println();
        }
        return questions;
    }
    public static void text2file(String fname,List<String> questions) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(fname));
        for(int i=0;i<questions.size();++i){
            writer.write(questions.get(i)+"\n");
        }
        writer.close();
    }
    public static void processPage(PDDocument doc,int pageIdx, int startLine,String testName) throws IOException{
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
        List<Integer> questionNumbers=new ArrayList<>();
        List<List<String>> questions = new ArrayList<>();
        List<List<BufferedImage>> imgsByQuestion=new ArrayList<>();
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
            questions.add(question);
            List<BufferedImage> qImgs=new ArrayList<BufferedImage>();
            for(int i=0;i<imgs.size();++i){
                float y=imgYs.get(i);
                if(y<positions.get(nextEnd)&& y>positions.get(startLine)){
                    qImgs.add(imgs.get(i));
                }
            }
            imgsByQuestion.add(qImgs);
            printList(question);
            startLine=nextEnd;
            nextEnd=getEnd(lines,startLine);

        }
        for(int q=0;q<questions.size();++q){
            int number=-1;
            List<String> question= questions.get(q);
            for(int l=0;l<question.size();++l){
                int potentialNum=getQNum(question.get(l));
                if(potentialNum!=-1){
                    number=potentialNum;
                    break;
                }
            }
            if(number==-1){
                continue;
            }
            BufferedWriter writer= new BufferedWriter(new FileWriter("parsed/"+testName+"_q"+number+".txt"));
            writer.write(String.join("\n",question));
            writer.close();
            List<BufferedImage> images=imgsByQuestion.get(q);
            for(int i=0;i<images.size();++i){
                File destination= new File("parsed/"+testName+"_q"+number+"_i"+i+".jpg");
                destination.createNewFile();
                ImageIO.write(images.get(i),"jpg",destination);
            }
        }

    }

    public static int getQNum(String line){
        String num="";
        for(int i=0;i<line.length();++i){
            if(line.charAt(i)=='.'){
                return Integer.parseInt(num);
            }else{
                num+=line.charAt(i);
            }
        }
        return -1;
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
                //System.out.println("found E, end line should be "+lines.get(i));
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
                        //System.out.println("cannot find D before next question, lines.get(k) is "+lines.get(k));
                        return k;
                    }
                    //else
                    int j=dIdx;
                    while(lines.get(j).replaceAll("\\s","").length()>1){
                        ++j;
                    }
                    //System.out.println("could not find E before next question, lines.get(j) is "+lines.get(j));
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
                //System.out.println("starts with number: "+line);
                return unbroken;
            }else if(!numbers.contains(line.substring(i,i+1))){
                return false;
            }
        }
        return false;
    }
    public static List<String> getHighlightedFromPage(PDDocument doc, int pageNum) throws IOException{
        LocationGetter lg = new LocationGetter();
        lg.setStartPage(pageNum);
        lg.setEndPage(pageNum);
        lg.getText(doc);
        List<Integer> numbers=new ArrayList<>();
        List<String> highlighted=new ArrayList<>();
        int lineIdx=0;
        List<String> lines = lg.getLines();
        List<Float> ycoords=lg.getYcoords();
        PDPage page=doc.getPage(pageNum);
        List<PDAnnotation> annotations = page.getAnnotations();
        List<PDAnnotation> highlightPositions=new ArrayList<>();
        for(int a=0;a< annotations.size();++a){
            System.out.println(annotations.get(a).getSubtype());
            if(annotations.get(a).getSubtype().equals("Highlight")) {
                highlightPositions.add(annotations.get(a));
            }
        }
        System.out.println(page.getAnnotations().size()+" ygolygpu");
        int rectanglePos=0;
        while(true){
            boolean hitEnd=true;

            for(;lineIdx<lines.size();++lineIdx){
                if(startsWithNumber(lines.get(lineIdx))){
                    int qNum=getQNum(lines.get(lineIdx));
                    numbers.add(qNum);
                    hitEnd=false;
                    break;
                }
            }

            if(hitEnd){
                return highlighted;
            }
            int endIdx=getEnd(lines,lineIdx);
            //printList(lines.subList(lineIdx,endIdx));
            List<String> localHighlightedRegions=new ArrayList<>();
            for(;rectanglePos<highlightPositions.size();++rectanglePos){
                PDRectangle rect=highlightPositions.get(rectanglePos).getRectangle();
                if(rect.getUpperRightY()>ycoords.get(lineIdx) && rect.getUpperRightX()<ycoords.get(endIdx)){
                    System.out.println("printing rectangle contents: "+highlightPositions.get(rectanglePos).getContents());
                    localHighlightedRegions.add(highlightPositions.get(rectanglePos).getContents());
                    /*
                    stripper.addRegion("0", new Rectangle2D.Float(rect.getLowerLeftX(),
                            rect.getUpperRightY(),rect.getWidth(),rect.getHeight()));
                    stripper.extractRegions(page);

                    stripper.removeRegion("0");
                    */
                }
            }
            highlighted.add(String.join("\n",localHighlightedRegions));
            ++lineIdx;

        }
    }
    public static List<String> getHighlightedFrom(PDDocument doc, int startIdx, int endIdx) throws IOException{
        List<String> result=new ArrayList<>();
        PDPage page=doc.getPage(startIdx);
        List<PDAnnotation> annotations = page.getAnnotations();
        for(int a=0;a< annotations.size();++a){
            if(annotations.get(a).getSubtype().equals("Highlight")){
                PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea();

                COSArray quadsArray = (COSArray) annotations.get(startIdx).getCOSObject().getDictionaryObject(COSName.getPDFName("QuadPoints"));
                String str = null;

                for(int j=1, k=0; j<=(quadsArray.size()/8); j++) {

                    COSFloat ULX = (COSFloat) quadsArray.get(0+k);
                    COSFloat ULY = (COSFloat) quadsArray.get(1+k);
                    COSFloat URX = (COSFloat) quadsArray.get(2+k);
                    COSFloat URY = (COSFloat) quadsArray.get(3+k);
                    COSFloat LLX = (COSFloat) quadsArray.get(4+k);
                    COSFloat LLY = (COSFloat) quadsArray.get(5+k);
                    COSFloat LRX = (COSFloat) quadsArray.get(6+k);
                    COSFloat LRY = (COSFloat) quadsArray.get(7+k);

                    k+=8;

                    float ulx = ULX.floatValue() - 1;                           // upper left x.
                    float uly = ULY.floatValue();                               // upper left y.
                    float width = URX.floatValue() - LLX.floatValue();          // calculated by upperRightX - lowerLeftX.
                    float height = URY.floatValue() - LLY.floatValue();         // calculated by upperRightY - lowerLeftY.

                    PDRectangle pageSize = page.getMediaBox();
                    uly = pageSize.getHeight() - uly;

                    Rectangle2D.Float rectangle_2 = new Rectangle2D.Float(ulx, uly, width, height);
                    stripperByArea.addRegion("highlightedRegion", rectangle_2);
                    stripperByArea.extractRegions(page);
                    String highlightedText = stripperByArea.getTextForRegion("highlightedRegion");

                    if(j > 1) {
                        str = str.concat(highlightedText);
                    } else {
                        str = highlightedText;
                    }
                }
                result.add(str);
            }
        }
        return result;
    }
}
