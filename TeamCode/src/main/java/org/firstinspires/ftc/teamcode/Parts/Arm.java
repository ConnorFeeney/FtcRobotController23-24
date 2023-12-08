package org.firstinspires.ftc.teamcode.Parts;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@Config
public class Arm implements Runnable{
    private Servo wristServo;
    private Servo jointServo;
    private DcMotorEx armMotor;

    private double time;
    private double lastReference;
    private double integralSUM;
    private double lastError;
    private double target;

    public static double Kp = 1.5;
    public static double Ki = 0.05;
    public static double Kd = 0.7;

    private Thread armThread;

    boolean run;
    public Arm(Servo wristServoIN, Servo jointServoIN, DcMotorEx armMotorIN){
        //Saves hardware
        this.wristServo = wristServoIN;
        this.jointServo = jointServoIN;
        this.armMotor = armMotorIN;

        //Initializes variables
        this.lastReference = 0;
        this.integralSUM = 0;
        this.lastError = 0;
        this.time = 0;
        this.target = 0;

        this.run = true;
    }

    /**<pre>
     * void init()
     *
     * Initializes arm
     * Resets encoder and blocks encoder from changing velocity
     * Sets servo ranges and positions and sets arm position
     * Should be called before every other arm function
     * Should be called prior to main loop to avoid recalling armThread
     * </pre>*/
    public void init(){
        //Sets usable range for servos (joint servo: 0 for up 1 for down, wrist servo: 1 for open 0 for closed)
        this.wristServo.scaleRange(0.73, 0.9);
        this.jointServo.scaleRange(0, 0.66);

        //resets encoders and prevents it from interfering with velocity
        this.armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.armMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.armMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //set initial arm position
        this.target = 50;
        this.wristServo.setPosition(1);
        this.jointServo.setPosition(0.92);

        //Starts arm update thread
        this.armThread = new Thread(this);
        this.armThread.start();
    }

    /**<pre>
     * void up()
     *
     * sets arm to up position
     * </pre>*/
    public void up(){
        this.target = 568;
        this.jointServo.setPosition(1);
    }

    /**<pre>
     * void down()
     *
     * sets arm to down position to pick up game piece
     * </pre>*/
    public void down(){
        //1440
        this.target = 25;
        this.jointServo.setPosition(0.92);
    }

    public void steadyDown(double dgr){
        this.target = this.target - dgr;
    }

    public void steadyUp(double dgr){
        this.target = this.target + dgr;
    }

    public void jointSteadyDown(){
        this.jointServo.setPosition(this.jointServo.getPosition() - 0.01);
    }

    public void jointSteadyUp(){
        this.jointServo.setPosition(this.jointServo.getPosition() + 0.01);
    }

    /**<pre>
     * void openClaw()
     *
     * Opens claw by setting wristServo pos to 1
     * </pre>*/
    public void openClaw(){
        this.wristServo.setPosition(1);
    }

    /**<pre>
     * void closeClaw()
     *
     * Closes claw by setting wristServo pos to 0
     * </pre>*/
    public void closeClaw(){
        this.wristServo.setPosition(0);
    }

    /**<pre>
     * double PIDController(double reference, double state)
     *
     * Custom PID function to control position of arm set motor power to this double
     * an encoder needs to be attached to the motor for this function to work.
     * needs to be called repeatedly to update or hold motor position
     * this can be done by calling "update(double deltaTime)" after initial call.
     *
     * Returns:
     *          Power needed to move or hold motor in position
     * Params:
     *          reference - position motor should be at
     *          state - current reading or "position" of the motors encoder
     * </pre>*/
    public double PIDController(double reference, double state){
        double error = reference - state;
        double derivative = (error - this.lastError) / this.time;
        this.integralSUM += (error * this.time);
        this.lastError = error;
        this.lastReference = reference;

        double output = (Kp * error) + (Ki * integralSUM) + (Kd * derivative);
        return output;
    }

    /**<pre>
     * void update(double deltaTime)
     *
     * Sets motor power to "PIDController(double reference, double state)"
     * return value based of of last reference, should be called in "run()" if using multithreading
     *
     * Params:
     *          deltaTime - time in seconds from each loop, often passed in "run()"
     * </pre>*/
    public void update(double deltaTime){
        this.time = deltaTime;
        armMotor.setPower(PIDController(this.target, this.armMotor.getCurrentPosition()));
    }

    /**<pre>
     * int getArmPos()
     *
     * Returns the current reading of the encoder for arm motor.
     * The units for this reading, that is, the number of ticks per revolution,
     * are specific to the motor/encoder in question, and thus are not specified here.
     *
     * Returns:
     *          the current reading of the encoder for the arms motor
     * </pre>*/

    public double getArmPos(){return this.armMotor.getCurrentPosition();}

    /**<pre>
     * double getWristPos()
     *
     * Returns the position to which the servo was last commanded to move.
     * Note that this function does NOT read a position from the servo through any electrical means
     *
     * Returns:
     *          the position to which the servo was last commanded to move,
     *          or Double.NaN if no such position is known
     * </pre>*/
    public double getWristPos(){return this.wristServo.getPosition();}

    /**<pre>
     * double getJointPos()
     *
     * Returns the position to which the servo was last commanded to move.
     * Note that this function does NOT read a position from the servo through any electrical means
     *
     * Returns:
     *          the position to which the servo was last commanded to move,
     *          or Double.NaN if no such position is known
     * </pre>*/
    public double getJointPos(){return this.jointServo.getPosition();}

    /**<pre>
     * double getLastReference()
     *
     * Returns last requested motor position
     *
     * Returns:
     *          last encoder reference
     * </pre>*/
    public double getLastReference(){return this.lastReference;}

    /**<pre>
     * double getArmPower()
     *
     * Returns the current configured power level of the motor.
     *
     * Returns:
     *          the current level of the motor, a value in the interval [0.0, 1.0]
     * </pre>*/
    public double getArmPower(){return this.armMotor.getPower();}

    /**<pre>
     * void stopArmThread()
     *
     * prevents armThread from continuing next loop
     * </pre>*/
    public void stopArmThread(){this.run = false;}

    @Override
    public void run() {
        ElapsedTime armTimer = new ElapsedTime();
        while (this.run == true){
            update(armTimer.seconds());
            armTimer.reset();
        }
        armMotor.setPower(0);
    }
}
