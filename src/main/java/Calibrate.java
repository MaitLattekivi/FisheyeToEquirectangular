import boofcv.abst.fiducial.calib.ConfigGridDimen;
import boofcv.abst.geo.calibration.CalibrateMonoPlanar;
import boofcv.abst.geo.calibration.DetectorFiducialCalibration;
import boofcv.factory.fiducial.FactoryFiducialCalibration;
import boofcv.io.UtilIO;
import boofcv.io.calibration.CalibrationIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.calib.CameraUniversalOmni;
import boofcv.struct.image.GrayF32;

import java.awt.image.BufferedImage;
import java.util.List;

public class Calibrate {
    public static void main( String[] args ) {
        DetectorFiducialCalibration detector;
        List<String> images;


        detector = FactoryFiducialCalibration.chessboardX(null,new ConfigGridDimen(8, 8, 30));
        images = UtilIO.listAll(UtilIO.path("images"));


        // Declare and setup the calibration algorithm
        CalibrateMonoPlanar calibrationAlg = new CalibrateMonoPlanar(detector.getLayout());

        // tell it type type of target and which parameters to estimate
        calibrationAlg.configureUniversalOmni( true, 2, false);

        for( String n : images ) {
            BufferedImage input = UtilImageIO.loadImage(n);
            if( input != null ) {
                GrayF32 image = ConvertBufferedImage.convertFrom(input,(GrayF32)null);
                if( detector.process(image)) {
                    calibrationAlg.addImage(detector.getDetectedPoints().copy());
                } else {
                    System.err.println("Failed to detect target in " + n);
                }
            }
        }
        // process and compute intrinsic parameters
        CameraUniversalOmni intrinsic = calibrationAlg.process();

        // save results to a file and print out
        CalibrationIO.save(intrinsic, "fisheye.yaml");

        calibrationAlg.printStatistics();
        System.out.println();
        System.out.println("--- Intrinsic Parameters ---");
        System.out.println();
        intrinsic.print();
    }
}
