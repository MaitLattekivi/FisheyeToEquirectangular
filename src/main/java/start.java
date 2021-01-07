import boofcv.abst.tracker.PointTrack;
import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.webcamcapture.UtilWebcamCapture;
import boofcv.struct.image.GrayF32;
import com.github.sarxos.webcam.Webcam;

import java.awt.*;
import java.awt.image.BufferedImage;

class start{
    public static void main(String[] args) {

        Webcam webcam = UtilWebcamCapture.openDefault(1920,1080);
        // Create the panel used to display the image and
        ImagePanel gui = new ImagePanel();
        Dimension viewSize = webcam.getViewSize();
        gui.setPreferredSize(viewSize);


        ShowImages.showWindow(gui,"Gradient",true);

        while (true){
            BufferedImage image = webcam.getImage();
            GrayF32 gray = ConvertBufferedImage.convertFrom(image,(GrayF32)null);
            gui.setImage(image);




        }

    }
}