package org.firstinspires.ftc.teamcode.Utils;

import static java.lang.Math.abs;
import static java.lang.Math.atan;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.opencv.core.Rect;

import java.util.Date;

public class autonomousRobotController {

    //Distances
    public static double distance;
    public static double offset;
    public static double turnAngle;

    //Turn Direction
    private static boolean left = false;

    //Motors
    private static DcMotor leftMotor;
    private static DcMotor rightMotor;
    public static double currentBearing;

    public autonomousRobotController(DcMotor leftMotorIN, DcMotor rightMotorIN){
        leftMotor = leftMotorIN;
        rightMotor = rightMotorIN;
        currentBearing = 0;
    }

    public static void goTo(Rect objectRect){
        calculateDistanceTo(objectRect);
        turn(turnAngle, left);
        drive(distance);
    }
    public static void calculateDistanceTo(Rect objectRect){

        //Focal Length (For Logitech c270 streaming 1280x720)
        final double focalLength = 4.0;
        final double sensorWidth = 3.58;

        distance = (focalLength * 76.2 * 1280) / (objectRect.width * sensorWidth);

        final double width = (double) objectRect.width;
        final double location = (double) objectRect.width/2 + objectRect.x;
        offset = ((location - (1280/2))/ width) * 76.2;

        turnAngle = atan(abs(offset) / distance);
        turnAngle = Math.toDegrees(turnAngle);

        if (offset < 0) {
            left = true;
        }
    }

    public static void turn(double turnAngleIN, boolean leftD){
        final double turnCircle = 358.14 * Math.PI;
        final double wheelCircumference = 102 * Math.PI;
        final double RPM = 100 * (64/32);
        final double deltaRotate360D =  turnCircle/(wheelCircumference * (RPM/60));
        final double deltaRotate1D = deltaRotate360D/360;
        final double deltaTime = deltaRotate1D * turnAngleIN;

        final double power[][] = {{0.5, 0.3, 0.2}, {2/3, 1/6, 1/6}};
        for (int i = 0; i < power.length; i++){
            if (leftD) {
                leftMotor.setPower(power[0][i]);
                rightMotor.setPower(power[0][i]);
            }else{
                leftMotor.setPower(-power[0][i]);
                rightMotor.setPower(-power[0][i]);
            }

            double startTime = System.currentTimeMillis();
            double elapsedTime = 0;
            while (elapsedTime < ((deltaTime*(power[1][i]))/power[0][i]) * 1000){
                elapsedTime = (new Date()).getTime() - startTime;
            }
        }
        leftMotor.setPower(0);
        rightMotor.setPower(0);

        if(currentBearing + turnAngleIN > 360 && leftD){
            double tempBearing = currentBearing + turnAngleIN;
            currentBearing = tempBearing - 360;
        }else if(currentBearing + turnAngleIN < -360 && !leftD){
            double tempBearing = currentBearing + turnAngleIN;
            currentBearing = tempBearing + 360;
        }else if (currentBearing + turnAngleIN == 360) {
            currentBearing = 0;
        }else if(leftD){
            currentBearing += turnAngleIN;
        }else{
            currentBearing -= turnAngleIN;
        }

    }

    public static void drive(double distanceToOBJmm){
        final double RPM = 100* (64/32);
        final double wheelCircumference = 102 * Math.PI;
        final double deltaTime = distanceToOBJmm/(wheelCircumference * (RPM/60));

        leftMotor.setPower(-0.5);
        rightMotor.setPower(0.5);
        final double startTime = System.currentTimeMillis();
        double elapsedTime = 0;
        while(elapsedTime < (deltaTime/0.5)){
            elapsedTime = (new Date()).getTime() - startTime;
        }
        leftMotor.setPower(0);
        rightMotor.setPower(0);
    }
}
