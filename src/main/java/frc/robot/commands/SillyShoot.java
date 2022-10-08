package frc.robot.commands;

import java.util.Set;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.Constants.ShooterConstants;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;

public class SillyShoot implements Command {
    private Subsystem[] requirements = {Shooter.getInstance(), Intake.getInstance()};
    static final double defaultSpeed = ShooterConstants.flywheelSpeed;
    Timer encoderFallback;
    private double percentPower;
    
    public SillyShoot() {
        this(defaultSpeed);
    }

    public SillyShoot(double power) {
        percentPower = power;
        encoderFallback = new Timer();
    }

    @Override
    public void initialize() {
        encoderFallback.start();
        encoderFallback.reset();
        
    }
    @Override
    public void execute(){

        Shooter.getInstance().setOpenLoop(percentPower);

        if(Shooter.getShooterVelocity() > 3000 * (percentPower / defaultSpeed)){ //Rough RPM at .65 ~= 3520 - 3530 RPM
            //Intake.getInstance().intake(0.55);
            Intake.getInstance().setConveyor(0.50);     
            Shooter.getInstance().setStagingMotor(0.5);
            encoderFallback.reset();
        } else if(encoderFallback.hasElapsed(1.5)){
            //Intake.getInstance().intake(0.55);
            Intake.getInstance().setConveyor(0.50); 
            
            Shooter.getInstance().setStagingMotor(0.5);
            
        }
        
        
    }
    
    @Override
    public void end(boolean interrupted) {
        Shooter.getInstance().stop();
        Intake.getInstance().stopIntake();
        Intake.getInstance().stopConveyor();
    }
    
    @Override
    public Set<Subsystem> getRequirements() {
        return Set.of(requirements);
    }
}
