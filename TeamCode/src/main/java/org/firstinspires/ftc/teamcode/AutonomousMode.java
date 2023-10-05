package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.Utils.*;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

@Autonomous(name = "FTC Autonomous Mode (23-24)")
public class AutonomousMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        //adds telemetry
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        //Initializes camera
        initCamera();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        if(opModeIsActive()){
            //adds telemetry
            telemetry.addData("Status", "Running");
            telemetry.update();
        }
        while (opModeIsActive()) {
            //Main Code Loop
        }
    }

    private void initCamera(){
        //Creates Camera
        WebcamName webcamName = hardwareMap.get(WebcamName.class, "Webcam1");
        OpenCvCamera camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                //Starts Stream
                camera.startStreaming(1280, 720, OpenCvCameraRotation.UPRIGHT);
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
