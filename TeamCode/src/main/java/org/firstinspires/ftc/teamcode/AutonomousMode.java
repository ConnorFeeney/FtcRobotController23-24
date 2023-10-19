package org.firstinspires.ftc.teamcode;

import android.graphics.drawable.shapes.Shape;

import org.firstinspires.ftc.teamcode.Utils.ShapeDetectionUtils;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;

@Autonomous(name = "FTC Autonomous Mode (23-24)")
public class AutonomousMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        //Initializes camera
        initCamera();

        //adds telemetry
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        if(opModeIsActive()){
            //adds telemetry
            telemetry.addData("Status", "Running");
            telemetry.update();

            ShapeDetectionUtils.pixels.clear();
            ShapeDetectionUtils.typePixels.clear();
        }

        while(opModeIsActive()){
            //Main Code Loop
        }
    }

    private void initCamera(){
        //Creates Camera
        WebcamName webcamName = hardwareMap.get(WebcamName.class, "Webcam1");
        OpenCvCamera camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName);
        OpenCvPipeline pipeline = new ShapeDetectionUtils();
        camera.setPipeline(pipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                //Starts Stream
                camera.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
                telemetry.log().add("Camera | Streaming");
                telemetry.update();
            }
            @Override
            public void onError(int errorCode)
            {
                //handles failed camera launch
                telemetry.log().add("Camera | Failed Streaming");
                telemetry.update();
            }
        });
    }
}
