package frc.robot.commands;

import java.util.Set;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.Constants;
import frc.robot.RobotContainer;
import frc.robot.Units;
import frc.robot.Constants.ShooterConstants;
import frc.robot.CustomUtil.Timeframe;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Shoot implements Command {
    private Shooter shooter = Shooter.getInstance();
    private Subsystem[] requirements = {shooter};
    private double speed;
    private Timeframe<Integer> timeframe;

    public Shoot(double speed) {
        this.speed = speed;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void execute() {
        double ff = Shooter.FEEDFORWARD.calculate(this.speed);
        double output = Shooter.PID_CONTROLLER.calculate(
            Units.RotationsPerMinuteToRadiansPerSecond(Shooter.getShooterVelocity())*ShooterConstants.wheelDiameter/2, 
            this.speed
        );
        SmartDashboard.putNumber("% flywheel", (ff + output) / 12);
        //shooter.setOpenLoop((ff + output) / Constants.kMaxVoltage); //TODO: uncomment
    }
    
    @Override
    public void end(boolean interrupted) {
        shooter.stop();
    }
    
    public Set<Subsystem> getRequirements() {
        return Set.of(requirements);
    }
}
