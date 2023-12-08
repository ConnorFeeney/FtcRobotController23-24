package org.firstinspires.ftc.teamcode.Utils;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.opencv.core.Rect;

import java.util.List;

public class AprilTag{
    private VisionPortal visionPortal;
    private final AprilTagProcessor aprilTag;
    private static WebcamName Camera;
    public static AprilTagDetection aprilTagLoc;
    public AprilTag(WebcamName Cam){
        aprilTag = AprilTagProcessor.easyCreateWithDefaults();
        Camera = Cam;
    }

    public boolean checkForAprilTag(int id){
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        for(AprilTagDetection detection : currentDetections){
            if (detection.id == id){
                aprilTagLoc = detection;
                return true;
            }
        }
        return false;
    }

    public void startStream(){
        visionPortal = VisionPortal.easyCreateWithDefaults(Camera, aprilTag);
    }

    public void stopStream(){
        visionPortal.close();
    }
}
