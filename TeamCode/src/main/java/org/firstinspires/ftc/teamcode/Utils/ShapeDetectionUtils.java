package org.firstinspires.ftc.teamcode.Utils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShapeDetectionUtils extends OpenCvPipeline {
    static Mat lastResult;
    public static List<Rect> pixels = new ArrayList<>();
    static List<MatOfPoint> filteredContours = new ArrayList<>();

    @Override
    public Mat processFrame(Mat input) {
        final Mat processed = new Mat(input.height(), input.width(), input.type());

        //Applies gaussian blur to reduces noise
        Imgproc.GaussianBlur(input, processed, new Size(7, 7), 1);
        //Changes image to grayscale
        Imgproc.cvtColor(processed, processed, Imgproc.COLOR_RGB2GRAY);
        //applies canny edge detection algorithm
        Imgproc.Canny(processed, processed, 215, 30);
        //Dilates image to make sure-edges more visible and low threshold values less prominent
        Imgproc.dilate(processed, processed, new Mat(), new Point(-1, -1), 1);

        lastResult = processed;
        return processed;
    }

    public static void checkForPixels(){
        markContours(lastResult);
    }
    public static void markContours(Mat input){
        final List<MatOfPoint> allContours = new ArrayList<>();
        Imgproc.findContours(input,
                            allContours,
                            new Mat(input.size(), input.type()),
                            Imgproc.RETR_EXTERNAL,
                            Imgproc.CHAIN_APPROX_NONE);
        if(pixels.toArray().length < 1){
            filteredContours = allContours.stream().filter(contour -> {
                final double value = Imgproc.contourArea(contour);
                final Rect rect = Imgproc.boundingRect(contour);
                final boolean isNotNoise = value > 100;

                if(isNotNoise){
                    MatOfPoint2f dst = new MatOfPoint2f();
                    contour.convertTo(dst, CvType.CV_32F);
                    Imgproc.approxPolyDP(dst, dst, 0.04 * Imgproc.arcLength(dst, true), true);

                    if(dst.toArray().length == 6){
                        pixels.add(rect);
                    }
                }

                return isNotNoise;
            }).collect(Collectors.toList());
        }
    }
}
