import boofcv.alg.distort.*;
import boofcv.alg.distort.pinhole.LensDistortionPinhole;
import boofcv.alg.distort.universal.LensDistortionUniversalOmni;
import boofcv.alg.interpolate.InterpolatePixel;
import boofcv.alg.interpolate.InterpolationType;
import boofcv.factory.distort.FactoryDistort;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.calibration.CalibrationIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.border.BorderType;
import boofcv.struct.calib.CameraPinhole;
import boofcv.struct.calib.CameraUniversalOmni;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.geometry.ConvertRotation3D_F32;
import georegression.struct.EulerType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FisheyeToPinhole {
    public static void main(String[] args) throws IOException {
        // Path to image data and calibration data
        String fisheyePath = UtilIO.path("src/main/py/pildid/");

        // load the fisheye camera parameters
        CameraUniversalOmni fisheyeModel = CalibrationIO.load(new File(fisheyePath,"fisheye.yaml"));

        // Specify what the pinhole camera should look like
        CameraPinhole pinholeModel = new CameraPinhole(2151,2161,0,1894,968,3840,2160);

        // Create the transform from pinhole to fisheye views
        LensDistortionNarrowFOV pinholeDistort = new LensDistortionPinhole(pinholeModel);
        LensDistortionWideFOV fisheyeDistort = new LensDistortionUniversalOmni(fisheyeModel);
        NarrowToWidePtoP_F32 transform = new NarrowToWidePtoP_F32(pinholeDistort,fisheyeDistort);

        // Load fisheye RGB image
        BufferedImage bufferedFisheye = UtilImageIO.loadImage(fisheyePath,"(7).jpg");
        Planar<GrayU8> fisheyeImage = ConvertBufferedImage.convertFrom(
                bufferedFisheye, true, ImageType.pl(3,GrayU8.class));

        // Create the image distorter which will render the image
        InterpolatePixel<Planar<GrayU8>> interp = FactoryInterpolation.
                createPixel(0, 255, InterpolationType.BILINEAR, BorderType.ZERO, fisheyeImage.getImageType());
        ImageDistort<Planar<GrayU8>,Planar<GrayU8>> distorter =
                FactoryDistort.distort(false,interp,fisheyeImage.getImageType());

        // Pass in the transform created above
        distorter.setModel(new PointToPixelTransform_F32(transform));

        // Render the image.  The camera will have a rotation of 0 and will thus be looking straight forward
        Planar<GrayU8> pinholeImage = fisheyeImage.createNew(pinholeModel.width, pinholeModel.height);

        distorter.apply(fisheyeImage,pinholeImage);
        BufferedImage bufferedPinhole0 = ConvertBufferedImage.convertTo(pinholeImage,null,true);


        File otput = new File("kalibreeritudpilt.jpg");
        ImageIO.write(bufferedPinhole0,"jpg", otput);
        ListDisplayPanel panel = new ListDisplayPanel();
        panel.addImage(bufferedPinhole0,"Pinehole Forward");
        panel.addImage(bufferedFisheye,"Fisheye");
        panel.setPreferredSize(new Dimension(1920,1080));

        ShowImages.showWindow(panel, "Fisheye to Pinhole", true);
    }
}
