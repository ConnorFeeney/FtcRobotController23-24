package org.firstinspires.ftc.teamcode.Utils;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;

public class Camera {
    OpenCvPipeline pipeline;
    OpenCvCamera camera;
    int x;
    int y;

    public Camera(WebcamName Cam, OpenCvPipeline pipelineIN, int xIN, int yIN){
        camera = OpenCvCameraFactory.getInstance().createWebcam(Cam);
        x = xIN;
        y = yIN;

        if(pipelineIN != null){
            pipeline = pipelineIN;
        }

        init();
    }

    public void init(){
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                //Starts Stream
            }
            @Override
            public void onError(int errorCode)
            {
                //handles failed camera launch
            }
        });
    }

    public void startStream(){
        if (pipeline != null){
            camera.setPipeline(pipeline);
        }
        camera.startStreaming(x, y, OpenCvCameraRotation.UPRIGHT);
    }

    public void stopStream(){
        camera.stopStreaming();
    }

    public void close(){
        camera.closeCameraDevice();
    }

}
