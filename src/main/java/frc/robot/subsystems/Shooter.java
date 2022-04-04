package frc.robot.subsystems;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.Util;
import frc.robot.Constants.ConveyorConstants;
import frc.robot.Constants.ShooterConstants;

public class Shooter implements Subsystem {
    private static final CANSparkMax master = new CANSparkMax(ShooterConstants.master, MotorType.kBrushless);
    private static final CANSparkMax slave = new CANSparkMax(ShooterConstants.slave, MotorType.kBrushless);
    private static CANSparkMax stagingMotor;
    public static PIDController PID_CONTROLLER; //TODO: May need to replace with SparkMAX built-in PID Controller
    public static SimpleMotorFeedforward FEEDFORWARD;
    private static RelativeEncoder encoder = master.getEncoder();
    private static final DigitalInput shooterPhotoelectric = new DigitalInput(ConveyorConstants.shooterPhotoelectric); //sensor closest to shooter

    private static Shooter instance = null;

    private Shooter() {

        PID_CONTROLLER = new PIDController(ShooterConstants.kP, ShooterConstants.kI, ShooterConstants.kD);
        FEEDFORWARD = new SimpleMotorFeedforward(ShooterConstants.kS, ShooterConstants.kV);
        stagingMotor = Util.createSparkMAX(ConveyorConstants.motor, MotorType.kBrushless);
        stagingMotor.setInverted(false);
        master.setInverted(false); //TODO: return to false
        //slave.follow(master); NOTE: this seems to stop the flywheel, hence the individual use of the master and slave motors
        slave.setInverted(!master.getInverted()); //Both motors are facing opposite directions
        register();
    }

    public static Shooter getInstance() {
        if(instance == null) instance = new Shooter();
        return instance;
    }

    @Override
    public void periodic() {
        SmartDashboard.putNumber("Shooter flywheel speed: ", getShooterVelocity());

    }

    public void setStagingMotor(double value) {
        stagingMotor.set(value);
    }

    public void setOpenLoop(final double value){
        master.set(value);
        slave.set(value);
    }

    public void stop(){
        master.stopMotor();
        slave.stopMotor();
        stagingMotor.stopMotor();
    }
    
    public boolean getShooterSensor() {
        return !shooterPhotoelectric.get();
    }
    public static double getShooterVelocity() {
        return encoder.getVelocity();
    }
}
