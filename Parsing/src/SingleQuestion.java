import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.image.BufferedImage;
import java.util.List;

public class SingleQuestion {
    public List<BufferedImage> imgs;
    public String text;
    SingleQuestion(List<String> lines, List<BufferedImage> imgs){
        text=String.join("\n",lines);
        this.imgs=imgs;
    }

}
