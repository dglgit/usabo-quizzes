import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.image.BufferedImage;
import java.util.List;

public class SingleQuestion {
    public List<BufferedImage> imgs;
    public String question;
    public String solution;
    SingleQuestion(List<String> qlines, List<String> slines, List<BufferedImage> imgs){

        question=String.join("\n",qlines);

        this.imgs=imgs;
    }

}
