import boofcv.alg.distort.AdjustmentType;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.LensDistortionOps;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.calibration.CalibrationIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.border.BorderType;
import boofcv.struct.calib.CameraPinhole;
import boofcv.struct.calib.CameraPinholeBrown;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RemoveLensDistortion {

    public static void main(String[] args) throws IOException {
        String calibDir = UtilIO.path("src/main/py/pildid/");
        String imageDir = UtilIO.path("src/main/py/pildid/");

        // load calibration parameters from the previously calibrated camera
        CameraPinhole param = CalibrationIO.load(new File(calibDir , "fisheye.yaml"));
        // Specify a transform that has no lens distortion that you wish to re-render the image as having
        CameraPinhole desired = new CameraPinhole(param);

        // load images and convert the image into a color BoofCV format
        BufferedImage orig = UtilImageIO.loadImage(imageDir , "(6).jpg");
        Planar<GrayF32> distortedImg =
                ConvertBufferedImage.convertFromPlanar(orig, null,true, GrayF32.class);

        int numBands = distortedImg.getNumBands();

        // create new transforms which optimize view area in different ways.
        // EXPAND makes sure there are no black outside of image pixels inside the image
        // FULL_VIEW will include the entire original image
        // The border is VALUE, which defaults to black, just so you can see it
        ImageDistort allInside = LensDistortionOps.changeCameraModel(AdjustmentType.EXPAND, BorderType.ZERO,
                param, desired,null, ImageType.pl(numBands, GrayF32.class));
        ImageDistort fullView = LensDistortionOps.changeCameraModel(AdjustmentType.FULL_VIEW, BorderType.ZERO,
                param, desired, null, ImageType.pl(numBands, GrayF32.class));



        // NOTE: After lens distortion has been removed the intrinsic parameters is changed.  If you pass
        //       in  a set of IntrinsicParameters to the 4th variable it will save it there.
        // NOTE: Type information was stripped from ImageDistort simply because it becomes too verbose with it here.
        //       Would be nice if this verbosity issue was addressed by the Java language.

        // render and display the different types of views in a window
        displayResults(orig, distortedImg, allInside, fullView );
    }

    /**
     * Displays results in a window for easy comparison..
     */
    private static void displayResults(BufferedImage orig,
                                       Planar<GrayF32> distortedImg,
                                       ImageDistort allInside, ImageDistort fullView ) throws IOException {
        // render the results
        Planar<GrayF32> undistortedImg = new Planar<>(GrayF32.class,
                distortedImg.getWidth(),distortedImg.getHeight(),distortedImg.getNumBands());

        allInside.apply(distortedImg, undistortedImg);
        BufferedImage out1 = ConvertBufferedImage.convertTo(undistortedImg, null,true);

        fullView.apply(distortedImg,undistortedImg);
        BufferedImage out2 = ConvertBufferedImage.convertTo(undistortedImg, null,true);

        File otput = new File("undist2.jpg");
        ImageIO.write(out1,"jpg", otput);

        // display in a single window where the user can easily switch between images
        ListDisplayPanel panel = new ListDisplayPanel();
        panel.addItem(new ImagePanel(orig), "Original");
        panel.addItem(new ImagePanel(out1), "Undistorted All Inside");
        panel.addItem(new ImagePanel(out2), "Undistorted Full View");

        ShowImages.showWindow(panel, "Removing Lens Distortion", true);
    }
}