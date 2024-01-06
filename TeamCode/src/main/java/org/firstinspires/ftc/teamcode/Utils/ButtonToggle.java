package org.firstinspires.ftc.teamcode.Utils;

public class ButtonToggle {
    public enum Status{NOT_BEGUN, IN_PROGRESS, COMPLETE};

    private Status status = Status.NOT_BEGUN;

    final public Status status(boolean ButtonStatus){
        //Checks if button is being held down
        if(ButtonStatus && this.status == Status.NOT_BEGUN){
            this.status = Status.IN_PROGRESS;
        }
        //Checks if button is not being held and toggle was hit
        else if (!ButtonStatus && this.status == Status.IN_PROGRESS) {
           this.status = Status.COMPLETE;
        }
        //Checks if toggle is finished
        else if (this.status == Status.COMPLETE) {
            this.status = Status.NOT_BEGUN;
        }
        return status;
    }

}
