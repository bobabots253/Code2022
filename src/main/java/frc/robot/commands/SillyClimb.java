package frc.robot.commands;

import java.util.Set;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.RobotContainer;
import frc.robot.Constants.ClimbConstants;
import frc.robot.subsystems.Climber;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class SillyClimb implements Command {
    private boolean reachThresh = false;
    private Subsystem[] requirements = {Climber.getInstance()};

    public SillyClimb() {

    }

    @Override
    public void initialize(){
        reachThresh = false;
    }

    @Override
    public void execute() {
        double left, right;
        left = RobotContainer.getLeftClimb() * ClimbConstants.climbSens;
        right = RobotContainer.getRightClimb() * ClimbConstants.climbSens;
        if(TicksToMeters(getRightTicks()) < kMeterSoftLimit){
            //it Climber.getInstance().setLeftMotor(left);
        }
        else{
            reachThresh = true;
            if(left > 0) left = 0;
            if(right > 0) right = 0;
        }

        Climber.getInstance().setRightMotor(right);
        SmartDashboard.putString("Climb threshold reached", (reachThresh) ? "true" : "false");
    }

    @Override
    public void end(boolean interrupted) {
        Climber.getInstance().stop();
    }

    @Override
    public Set<Subsystem> getRequirements() {
        return Set.of(requirements);
    }
}
