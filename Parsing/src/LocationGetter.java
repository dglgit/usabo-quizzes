import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocationGetter extends PDFTextStripper{
    private List<String> lines;
    private List<Float> ycoords;
    private float lastY;
    private String lastLine;
    public LocationGetter() throws IOException {
        super();
        lastLine="";
        lastY=-1;
        lines=new ArrayList<String>();
        ycoords=new ArrayList<Float>();
    }
    @Override
    protected void writeString(String string, List<TextPosition> positions) throws IOException{
        lastLine="";
        for(TextPosition text: positions){
            /*
            if(text.getYDirAdj()==lastY){
                lastLine+=text.getUnicode();
            }else{
                lines.add(lastLine);
                ycoords.add(lastY);
                lastLine=text.getUnicode();
                lastY=text.getYDirAdj();
            }*/
            lastY=text.getYDirAdj();
            lastLine+=text.getUnicode();
        }
        //System.out.println("called, got line "+lines.get(lines.size()-1)+", index "+lastY);
        lines.add(lastLine);
        ycoords.add(lastY);//sometimes will result in extra newline
    }
    public List<Float> getYcoords(){
        return ycoords;
    }
    public List<String> getLines(){
        return lines;
    }
}
