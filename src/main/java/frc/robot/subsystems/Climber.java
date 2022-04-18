package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.Util;
import frc.robot.Constants.ClimbConstants;

public class Climber implements Subsystem {
    private static TalonFX right = Util.createTalonFX(ClimbConstants.rightMotor), left = Util.createTalonFX(ClimbConstants.leftMotor);
    private static Climber instance = null;
    private Climber() {
        right.setInverted(false);
        left.setInverted(false);
        resetEncoders();
        register();
    }

    @Override
    public void periodic() {

    }

    public static Climber getInstance() {
        if(instance == null) instance = new Climber();
        return instance;
    }
    public void setLeftMotor(double value) {
        left.set(ControlMode.PercentOutput, value);
    }

    public void setRightMotor(double value) {
        right.set(ControlMode.PercentOutput, value);
    }

    public void climb(double value) {

        setRightMotor(value);
        setLeftMotor(value);
    }

    public void stop() {
        climb(0.0);
    }

    public static double getRightTicks(){
        return right.getSelectedSensorPosition();
    }

    public static double getLeftTicks(){
        return left.getSelectedSensorPosition();
    }

    public void resetEncoders() {
        resetEncoders(0, 0);
    }
    
    /**
     * Sets encoders to a specific value
     * @param left  left wheel value
     * @param right right wheel value
     */
    public void resetEncoders(int left, int right) {
        this.right.setSelectedSensorPosition(right);
        this.left.setSelectedSensorPosition(left);
    }
}
