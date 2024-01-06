package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.Parts.Arm;
import org.firstinspires.ftc.teamcode.Parts.Camera;

import org.firstinspires.ftc.teamcode.Utils.ButtonToggle;
import org.firstinspires.ftc.teamcode.Utils.PixelDetection;

@TeleOp(name = "FTC Driver Mode (23-24)")
public class DriverMode extends LinearOpMode {
    enum Mode{MANUAL, AUTOMATIC}
    @Override
    public void runOpMode() throws InterruptedException {
        //Sends telemetry to FTC dashboard
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        //Creates runtime timer
        ElapsedTime runtime = new ElapsedTime();

        //Creates button toggle maps
        ButtonToggle Xtoggle = new ButtonToggle();

        //Sets up all hardware
        DcMotor leftMotor = hardwareMap.get(DcMotor.class, "leftMotor");
        DcMotor rightMotor = hardwareMap.get(DcMotor.class, "rightMotor");

        DcMotorEx armMotorLeft = hardwareMap.get(DcMotorEx.class, "armMotorLeft");
        DcMotorEx armMotorRight = hardwareMap.get(DcMotorEx.class, "armMotorRight");
        Servo jointServo = hardwareMap.get(Servo.class, "jointServo");
        Servo wristServo = hardwareMap.get(Servo.class, "wristServo");
        Arm arm = new Arm(wristServo, jointServo, armMotorLeft, armMotorRight);
        PixelDetection pipeline = new PixelDetection();
        Camera camera = new Camera(hardwareMap.get(WebcamName.class, "camera"), pipeline, 1280, 720);
        camera.init();
        camera.startStream();

        //Initial telemetry status
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        Mode mode = Mode.AUTOMATIC;

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        //run arm setup
        arm.init();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            //Calculates drive base motor power based on game-pad joystick positions
            double axial = gamepad1.left_stick_y;
            double yawn = -gamepad1.right_stick_x;
            double leftMotorPower = axial - yawn;
            double rightMotorPower = axial + yawn;
            //Clamps motor power to max at 1.00 to avoid locked up steering
            double max = Math.max(Math.abs(leftMotorPower), Math.abs(rightMotorPower));
            if(max > 1.0){
                leftMotorPower  /= max;
                rightMotorPower /= max;
            }
            //sets motor power
            leftMotor.setPower(-leftMotorPower/1.5);
            rightMotor.setPower(rightMotorPower/1.5);


            //Game Pad Controls
            if(gamepad1.a){
                arm.openClaw();
            }
            if (gamepad1.b){
                arm.closeClaw();
            }

            if(gamepad1.right_bumper && mode == Mode.AUTOMATIC){
                arm.up();
            }
            if(gamepad1.left_bumper && mode == Mode.AUTOMATIC){
                arm.down();
            }

            if(Xtoggle.status(gamepad1.x) == ButtonToggle.Status.COMPLETE){
                if(mode == Mode.MANUAL){
                    mode = Mode.AUTOMATIC;
                } else if (mode == Mode.AUTOMATIC) {
                    mode = Mode.MANUAL;
                }
            }

            if(gamepad1.dpad_up && mode == Mode.MANUAL){
                arm.steadyUp(5);
            }
            if(gamepad1.dpad_down && mode == Mode.MANUAL){
                arm.steadyDown(5);
            }

            if(gamepad1.right_bumper && mode == Mode.MANUAL){
                arm.jointSteadyUp();
            }
            if(gamepad1.left_bumper && mode == Mode.MANUAL){
                arm.jointSteadyDown();
            }

            //Telemetry Updates
            telemetry.addData("Status", "Run Time: " + runtime.toString());

            telemetry.addData("Left Motor Power", "%4.2f", leftMotorPower);
            telemetry.addData("Right Motor Power", "%4.2f", rightMotorPower);

            telemetry.addData("Arm Motor Position", "%4.2f", arm.getArmPos());
            telemetry.addData("Arm Motor Reference", "%4.2f", arm.getLastReference());
            telemetry.addData("Arm Motor Power", "%4.2f", arm.getArmPower());

            telemetry.addData("Joint Position (Degrees)", "%4.2f/198", arm.getJointPos()*198);
            if(arm.getWristPos() == 1){
                telemetry.addData("Claw Setting", "Opened");
            }else{
                telemetry.addData("Claw Setting", "Closed");
            }
            if(mode == Mode.MANUAL){
                telemetry.addData("Arm Setting", "Manual");
            }else{
                telemetry.addData("Arm Setting", "Automatic");
            }

            telemetry.update();

            //Stops arm thread when stop is pressed to prevent stuck in stop error
            if(!opModeIsActive()){
                arm.stopArmThread();
            }
        }
    }
}
