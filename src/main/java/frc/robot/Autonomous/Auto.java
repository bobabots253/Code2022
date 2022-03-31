package frc.robot.Autonomous;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.RunCommand;
import frc.robot.RobotContainer;
import frc.robot.Units;
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.VisionConstants;
import frc.robot.commands.CargoTrack;
import frc.robot.commands.DriveXMeters;
import frc.robot.commands.HubTrack;
import frc.robot.commands.Shoot;
import frc.robot.commands.SillyDriveX;
import frc.robot.commands.SillyShoot;
import frc.robot.commands.SmartShoot;
import frc.robot.commands.TurnXDegrees;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Intake;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

/*
    Class to store autonomous sequences, including sequences such as intake
*/

public class Auto {
    private static Arm arm = Arm.getInstance();
    private static Intake intake = Intake.getInstance();
    public enum Selection {
        COMPLEX(1), SILLY(2);
        public int val;
        private Selection(int val) {
            this.val = val;
        }
    }
    public static Command extendIntake() { //Arm down and spin conveyor
            /*new WaitCommand(0.3), */ // move balls in storage if needed
            return new ParallelCommandGroup(
                new RunCommand( ()->arm.rotate(0.1), arm).withTimeout(2),
                new RunCommand( ()->intake.intake(0.7), intake), 
                new RunCommand( ()->intake.setConveyor(0.5))); //TODO: update conveyor/staging during intake

    }
    public static Command retractIntake() { //Arm down and spin conveyor
        return
            /*new WaitCommand(0.3), */ // move balls in storage if needed
            new ParallelCommandGroup(
                new RunCommand( ()->arm.rotate(-0.05), arm).withTimeout(2),
                new InstantCommand(intake::stopIntake, intake));
    }

    public static Command getShootCommand() { //Drive up and shoot
        return new SequentialCommandGroup(
            new HubTrack(),
            //new DriveXMeters(AutoConstants.hubXOffset, AutoConstants.DXMConstraints[0], AutoConstants.DXMConstraints[1]), 
            new SmartShoot(RobotContainer.getDistance()).withTimeout(4) //TODO: adjust shooter velocity based on distance
        );
    }

    public static Command getSillyShootCommand() {
        return new SequentialCommandGroup(
            new HubTrack().withTimeout(3),
            new DriveXMeters(AutoConstants.backupDistance, AutoConstants.DXMConstraints[0], AutoConstants.DXMConstraints[1]),
            new SillyShoot()
        );
    }

    public static Command getBackupCommand() { //Back up and find new ball
        return new SequentialCommandGroup(
            new DriveXMeters(-AutoConstants.backupDistance, AutoConstants.DXMConstraints[0], AutoConstants.DXMConstraints[1]),
            new TurnXDegrees(180, AutoConstants.TXDConstraints[0], AutoConstants.TXDConstraints[1]),
            new CargoTrack()
        );
    }

    public static Command getSillyAuto() { //SFR auto
        return new ParallelCommandGroup(
            new SillyDriveX(Units.InchesToMeters(33.8), true).andThen(new HubTrack().withTimeout(3.0)).andThen((new SillyShoot()).withTimeout(3).andThen(new RunCommand(() -> Drivetrain.setOpenLoop(-0.2, -0.2), Drivetrain.getInstance()).withTimeout(2))),
            new RunCommand(() -> Arm.getInstance().setOpenLoop(0.05), Arm.getInstance()).withTimeout(1.5).andThen(new InstantCommand(() -> Arm.getInstance().stopArm()))
        );
    }
}
