package frc.robot.commands;

import java.util.Set;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.RobotContainer;
import frc.robot.Constants.ClimbConstants;
import frc.robot.subsystems.Climber;

public class SillyClimb implements Command {
    private Subsystem[] requirements = {Climber.getInstance()};

    public SillyClimb() {

    }

    @Override
    public void execute() {
        double left, right;
        left = RobotContainer.getLeftClimb() * ClimbConstants.climbSens;
        right = RobotContainer.getRightClimb() * ClimbConstants.climbSens;
        Climber.getInstance().setLeftMotor(left);
        Climber.getInstance().setRightMotor(right);
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
