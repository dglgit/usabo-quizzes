import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.PDFStreamEngine;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;

import javax.imageio.ImageIO;

public class ImageGetter extends PDFStreamEngine {
    private List<Float> imgYs;
    private List<BufferedImage> imgs;
    public ImageGetter() throws IOException{
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
        imgYs=new ArrayList<Float>();
        imgs=new ArrayList<BufferedImage>();
    }
    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException{
        String operation = operator.getName();
        if( "Do".equals(operation) )
        {
            COSName objectName = (COSName) operands.get( 0 );
            // get the PDF object
            PDXObject xobject = getResources().getXObject( objectName );
            // check if the object is an image object
            if( xobject instanceof PDImageXObject)
            {
                PDImageXObject image = (PDImageXObject)xobject;
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                System.out.println("\nImage [" + objectName.getName() + "]");
                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();

                float imageXScale = ctmNew.getScalingFactorX();
                float imageYScale = ctmNew.getScalingFactorY();

                // position of image in the pdf in terms of user space units
                System.out.println("position in PDF = " + ctmNew.getTranslateX() + ", " + ctmNew.getTranslateY() + " in user space units");
                // raw size in pixels
                System.out.println("raw image size  = " + imageWidth + ", " + imageHeight + " in pixels");
                try {
                    BufferedImage fimage = image.getImage();
                    imgYs.add(ctmNew.getTranslateY());
                    imgs.add(fimage);
                    //ImageIO.write(fimage, "jpg", new File("./imgs/" + objectName.getName() + ".jpg"));
                }catch(IllegalArgumentException e){
                    System.out.println("error, reading "+objectName.getName()+" failed for some reason");
                }
                // displayed size in user space units
                //System.out.println("displayed size  = " + imageXScale + ", " + imageYScale + " in user space units");
            }
            else if(xobject instanceof PDFormXObject)
            {
                PDFormXObject form = (PDFormXObject)xobject;
                showForm(form);
            }
        }
        else
        {
            super.processOperator( operator, operands);
        }
    }
    public List<Float> getImgYs(){
        return imgYs;
    }
    public List<BufferedImage> getImgs(){
        return imgs;
    }
}
