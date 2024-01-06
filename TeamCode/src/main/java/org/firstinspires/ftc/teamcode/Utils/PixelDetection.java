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

public class PixelDetection extends OpenCvPipeline {

    //Enums
    enum PixelType{WHITE, YELLOW, GREEN, PURPLE};

    //Mats
    static Mat lastResult;
    static Mat originalFrame;

    //Lists
    public static List<Rect> pixels = new ArrayList<>();
    static List<MatOfPoint> filteredContours = new ArrayList<>();

    //HashMaps
    public static HashMap<PixelType, Rect> classifiedPixels = new HashMap<>();

    @Override
    public Mat processFrame(Mat input) {
        final Mat processed = new Mat(input.height(), input.width(), input.type());

        //Applies gaussian blur to reduces noise
        Imgproc.GaussianBlur(input, processed, new Size(7, 7), 1);
        //Changes image to grayscale
        Imgproc.cvtColor(processed, processed, Imgproc.COLOR_RGB2GRAY);
        //Applies canny edge detection algorithm
        Imgproc.Canny(processed, processed, 215, 30);
        //Dilates image to make sure-edges more visible and low threshold values less prominent
        Imgproc.dilate(processed, processed, new Mat(), new Point(-1, -1), 1);

        originalFrame = input;
        lastResult = processed;
        return processed;
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

    public static void classifyPixel(Mat input, PixelType type){
        //Converts image to Bitmap image
        Bitmap image = Bitmap.createBitmap(input.width(), input.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(input, image);

        //Creates List for useless pixels
        List<Rect> pixelsToRemove = new ArrayList<>();
        for (Rect rect: pixels){
            //Grabs all pixels in bounding box of pixel detection -interference pixels
            int[] imagePixels = new int[((int) ((double)rect.width)) * ((int) ((double)rect.height))];
            image.getPixels(imagePixels,
                            0,
                            (int) ((double)rect.width),
                            (int) ((double)rect.x),
                            rect.y,
                            (int) ((double)rect.width),
                            (int) ((double)rect.height));

            //Stores each pixel of each color in a HashMap
            HashMap<Integer, Integer> colorCountMap = new HashMap<>();
            for(int imagePixel : imagePixels){
                if(colorCountMap.containsKey(imagePixel)){
                    colorCountMap.put(imagePixel, colorCountMap.get(imagePixel) + 1);
                } else {
                    colorCountMap.put(imagePixel, 1);
                }
            }

            //Decides which color is found most often
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

            //Sets RGB values
            int r = Color.red(dominantColor);
            int g = Color.green(dominantColor);
            int b = Color.blue(dominantColor);

            //Converts RGB to HSV
            float HSV[] = new float[3];
            Color.RGBToHSV(r, g, b, HSV);

            //Checks Color
            float deg = HSV[0];

            boolean white = HSV[1] < 0.2 && HSV[2] > 0.8;
            boolean black = HSV[2] < 0.1;

            boolean purple = deg >= 240 && deg < 300 && !black && !white;
            boolean green = deg >= 90 && deg < 150 && !black && !white;
            boolean yellow = deg >= 30 && deg < 90 && !black && !white;

            //Classify Pixels
            if(white && !classifiedPixels.containsKey(PixelType.WHITE) && (type == PixelType.WHITE || type == null)){
                classifiedPixels.put(PixelType.WHITE, rect);
            }else if(purple && !classifiedPixels.containsKey(PixelType.PURPLE) && (type == PixelType.PURPLE || type == null)){
                classifiedPixels.put(PixelType.PURPLE, rect);
            }else if(green && !classifiedPixels.containsKey(PixelType.GREEN) && (type == PixelType.GREEN || type == null)){
                classifiedPixels.put(PixelType.GREEN, rect);
            }else if(yellow && !classifiedPixels.containsKey(PixelType.YELLOW) && (type == PixelType.YELLOW || type == null)){
                classifiedPixels.put(PixelType.YELLOW, rect);
            }else{
                pixelsToRemove.add(rect);
            }
        }
        pixels.removeAll(pixelsToRemove);
    }

    public static void checkForPixels(PixelType type){
        markContours(lastResult);

        if(pixels.size() > 0){
            classifyPixel(originalFrame, type);
        }
    }
}
