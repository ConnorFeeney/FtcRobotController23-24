package org.firstinspires.ftc.teamcode.Utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.android.Utils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShapeDetectionUtils extends OpenCvPipeline {

    //Mats
    static Mat lastResult;
    static Mat originalFrame;

    //Lists
    public static List<Rect> pixels = new ArrayList<>();
    public static HashMap<String, Rect> typePixels = new HashMap<>();
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

        originalFrame = input;
        lastResult = processed;
        return processed;
    }

    public static void checkForPixels(String type){
        markContours(lastResult);

        if(pixels.size() > 0){
            classifyPixel(originalFrame, type);
        }
    }

    public static void markContours(Mat input){
        //Finds all contours
        final List<MatOfPoint> allContours = new ArrayList<>();
        Imgproc.findContours(input,
                            allContours,
                            new Mat(input.size(), input.type()),
                            Imgproc.RETR_EXTERNAL,
                            Imgproc.CHAIN_APPROX_NONE);

        //Filters Contours to only detect pixels
        if(pixels.toArray().length < 10){
            filteredContours = allContours.stream().filter(contour -> {
                //Gets contour area
                final double value = Imgproc.contourArea(contour);
                //Gets object bounding rect
                final Rect rect = Imgproc.boundingRect(contour);
                //Checks if object is noise
                final boolean isNotNoise = value > 100;

                //Applies filter and records possible pixels
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

    public static void classifyPixel(Mat input, String type){
        HashMap<Rect, Integer> pixelColors = new HashMap<>();
        Bitmap image = Bitmap.createBitmap(input.width(), input.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(input, image);

        List<Rect> pixelsToRemove = new ArrayList<>();
        for (Rect rect: pixels){
            int[] imagePixels = new int[((int) ((double)rect.width * 0.57)) * ((int) ((double)rect.height * 0.29))];
            image.getPixels(imagePixels,
                            0,
                            (int) ((double)rect.width * 0.57),
                            (int) ((double)rect.x * 0.73),
                            rect.y,
                            (int) ((double)rect.width * 0.57),
                            (int) ((double)rect.height * 0.29));

            HashMap<Integer, Integer> colorCountMap = new HashMap<>();
            for(int imagePixel : imagePixels){
                if(colorCountMap.containsKey(imagePixel)){
                    colorCountMap.put(imagePixel, colorCountMap.get(imagePixel) + 1);
                } else {
                    colorCountMap.put(imagePixel, 1);
                }
            }

            int dominantColor = 0;
            int maxCount = 0;
            for(Map.Entry<Integer, Integer> entry : colorCountMap.entrySet()){
                int color = entry.getKey();
                int count = entry.getValue();

                if(count > maxCount){
                    maxCount = count;
                    dominantColor = color;
                }
            }

            int r = Color.red(dominantColor);
            int g = Color.green(dominantColor);
            int b = Color.blue(dominantColor);

            if (r >= 230 && g >=230 && b>= 230 && (type == "SPIKE_MARK" | type == null)){
                if(!typePixels.containsKey("SPIKE_MARK")){
                    typePixels.put("SPIKE_MARK", rect);
                }else if (type == "SPIKE_MARK"){
                    pixelsToRemove.add(rect);
                }
            }else{
                pixelsToRemove.add(rect);
            }
        }
        pixels.removeAll(pixelsToRemove);
    }
}
