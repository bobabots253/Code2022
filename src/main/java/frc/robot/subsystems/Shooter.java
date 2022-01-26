package frc.robot.subsystems;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.Util;
import frc.robot.Constants.ShooterConstants;

public class Shooter implements Subsystem {
    private static final CANSparkMax master = Util.createSparkMAX(ShooterConstants.master, MotorType.kBrushless);
    private static final CANSparkMax slave = Util.createSparkMAX(ShooterConstants.slave, MotorType.kBrushless);
    private static RelativeEncoder encoder = master.getEncoder();

    private static Shooter instance = null;

    private Shooter() {
        slave.follow(master);
    }

    public static Shooter getInstance() {
        if(instance == null) instance = new Shooter();
        return instance;
    }

    public void setOpenLoop(final double value){
        master.set(value);
    }

    public void stop(){
        master.stopMotor();
    }

    public double getShooterVelocity() {
        return encoder.getVelocity();
    }
}
