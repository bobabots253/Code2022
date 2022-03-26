package frc.robot;

import java.util.*;
import java.util.function.BiConsumer;

import frc.robot.Constants;
import frc.robot.Autonomous.Auto;
import frc.robot.Autonomous.Auto.Selection;
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.VisionConstants;
import frc.robot.Constants.ConveyorConstants;
import frc.robot.Constants.DrivetrainConstants;
import frc.robot.commands.Drive;
import frc.robot.commands.DriveXMeters;
import frc.robot.commands.HubTrack;
import frc.robot.commands.Shoot;
import frc.robot.commands.SillyShoot;
import frc.robot.commands.SmartShoot;
import frc.robot.commands.StagingQueue;
import frc.robot.commands.TurnXDegrees;
import frc.robot.commands.ActuateArm;
import frc.robot.commands.CargoTrack;
import frc.robot.commands.ConveyorQueue;
import frc.robot.subsystems.*;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.ParallelRaceGroup;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;

import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.ColorSensorV3;

import org.photonvision.PhotonCamera;
import org.photonvision.targeting.PhotonPipelineResult;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj2.command.Subsystem;


public class RobotContainer {
    private static RobotContainer instance = null;
    private static final XboxController driverController = new XboxController(Constants.InputPorts.driverController);
    private static final XboxController operatorController = new XboxController(Constants.InputPorts.operatorController);
    private static final JoystickButton driver_A = new JoystickButton(driverController, 1),
            driver_B = new JoystickButton(driverController, 2), driver_X = new JoystickButton(driverController, 3),
            driver_Y = new JoystickButton(driverController, 4), driver_LB = new JoystickButton(driverController, 5),
            driver_RB = new JoystickButton(driverController, 6), driver_VIEW = new JoystickButton(driverController, 7),
            driver_MENU = new JoystickButton(driverController, 8);
    private static final JoystickButton operator_A = new JoystickButton(operatorController, 1),
    operator_B = new JoystickButton(operatorController, 2), operator_X = new JoystickButton(operatorController, 3),
    operator_Y = new JoystickButton(operatorController, 4), operator_LB = new JoystickButton(operatorController, 5),
    operator_RB = new JoystickButton(operatorController, 6), operator_VIEW = new JoystickButton(operatorController, 7),
    operator_MENU = new JoystickButton(operatorController, 8);
    private static final POVButton operator_DPAD_UP = new POVButton(operatorController, 0),
    operator_DPAD_RIGHT = new POVButton(operatorController, 90), operator_DPAD_DOWN = new POVButton(operatorController, 180),
    operator_DPAD_LEFT = new POVButton(operatorController, 270);
    public static NetworkTable limelight;

    public static Drivetrain drivetrain;
    public static Intake intake;
    public static Climber climber;
    public static Arm arm;
    public static Shooter shooter;
    public static ColorSensorV3 colorSensorV3;
    public static AHRS navX; 
    public static Shoot shootCommand;
    private RobotContainer() {
        navX = new AHRS(Port.kMXP);
        drivetrain = Drivetrain.getInstance();
        drivetrain.setDefaultCommand(new Drive(Drive.State.CurvatureDrive2019));
        //drivetrain.setDefaultCommand(new Drive(Drive.State.TankDrive));
        arm = Arm.getInstance();
        intake = Intake.getInstance();
        climber = Climber.getInstance();
        shooter = Shooter.getInstance();
        //shooter.setDefaultCommand(new StagingQueue());
        limelight = NetworkTableInstance.getDefault().getTable("limelight-intake");
        setLEDMode(LEDMode.OFF);

        bindOI();
    }
    

    private void bindOI() {
        driver_RB.whenHeld(new RunCommand(() -> arm.setOpenLoop(0.05), arm).withTimeout(1.7).andThen(arm::stopArm, arm))
            .whileHeld(new RunCommand(() -> intake.intake(0.7), intake)
                .alongWith(new RunCommand(() -> intake.setConveyor(0.3))))
            .whenReleased(new RunCommand(() -> arm.setOpenLoop(-0.05), arm).withTimeout(1.7)
                .andThen(arm::stopArm, arm))
            .whenReleased(new InstantCommand(() -> intake.stopIntake()));
        //driver_LB.whileHeld(new SillyShoot());
        driver_X.whileHeld(new HubTrack());

        for(JoystickButton button : Set.of(driver_B, operator_X)) {
            button.whenHeld(new RunCommand(() -> Arm.getInstance().setOpenLoop(0.05), Arm.getInstance()).withTimeout(1.7).andThen(arm::stopArm)) //Can reimplement with StartEndCommand
                .whileHeld(new RunCommand(() -> intake.intake(-0.7), intake)
                    .alongWith(new RunCommand(() -> intake.setConveyor(-0.3)))
                    .alongWith(new RunCommand(() -> shooter.setStagingMotor(-0.2))))
                .whenReleased(new InstantCommand(() -> intake.stopIntake())
                    .alongWith(new RunCommand(() -> Arm.getInstance().setOpenLoop(-0.05), Arm.getInstance()).withTimeout(1.7).andThen(arm::stopArm, arm))
                    .alongWith(new RunCommand(() -> shooter.setStagingMotor(0.0))));
        }
        

        /*operator_X.whenHeld(new RunCommand(() -> Arm.getInstance().setOpenLoop(0.05), Arm.getInstance()).withTimeout(1.7))
            .whileHeld(new RunCommand(() -> intake.intake(-0.7), intake)
                .alongWith(new RunCommand(() -> intake.setConveyor(-0.3)))
                .alongWith(new RunCommand(() -> shooter.setStagingMotor(-0.2))))
            .whenReleased(new InstantCommand(() -> intake.stopIntake())
                .alongWith(new RunCommand(() -> Arm.getInstance().setOpenLoop(-0.05), Arm.getInstance()).withTimeout(1.7))
                .alongWith(new RunCommand(() -> shooter.setStagingMotor(0.0))));*/

        driver_LB.whileHeld(new RunCommand(() -> Shooter.getInstance().setStagingMotor(0.3)) //NOTE: requiring shooter will cancel the default command keeping the flywheel spinning
            .alongWith(new RunCommand(() -> Intake.getInstance().setConveyor(0.5), Intake.getInstance())))
            .whenReleased(new RunCommand(() -> Shooter.getInstance().setStagingMotor(0.0)).alongWith(new RunCommand(() -> Intake.getInstance().setConveyor(0.0))));
        operator_B.whileHeld(new RunCommand(() -> intake.setConveyor(0.5), intake)).whenReleased(new InstantCommand(()-> intake.stopIntake(), intake));
        operator_DPAD_UP.whileHeld(new RunCommand(() -> climber.climb(0.5), climber)).whenReleased(new InstantCommand(() -> climber.stop()));
        operator_DPAD_DOWN.whileHeld(new RunCommand(() -> climber.climb(-0.5), climber)).whenReleased(new InstantCommand(() -> climber.stop()));
        operator_DPAD_LEFT.whileHeld(new RunCommand(() -> arm.setOpenLoop(0.05), arm)).whenReleased(new RunCommand(()->arm.setOpenLoop(0.0)));
        operator_DPAD_RIGHT.whileHeld(new RunCommand(() -> arm.setOpenLoop(-0.05), arm)).whenReleased(new RunCommand(()->arm.setOpenLoop(0.0)));
        operator_VIEW.whileHeld(new RunCommand(() -> climber.setLeftMotor(0.5), climber)).whenReleased(climber::stop);
        operator_MENU.whileHeld(new RunCommand(() -> climber.setRightMotor(0.5), climber)).whenReleased(climber::stop);
    }

    public static Command getAutonomousCommand(Auto.Selection selectedAuto) { //TODO: change auto based on selected strategy
        Command auto;
        if(selectedAuto == Auto.Selection.INTAKEFIRST) { //Start facing cargo, drive, intake, shoot
            auto = new SequentialCommandGroup(
                new ParallelCommandGroup(
                    Auto.extendIntake(),
                    new DriveXMeters(AutoConstants.distToCargo, AutoConstants.DXMConstraints[0], AutoConstants.DXMConstraints[1])),
                new ParallelCommandGroup(
                    Auto.retractIntake(),
                    new TurnXDegrees(180, AutoConstants.TXDConstraints[0], AutoConstants.TXDConstraints[1])
                ),
                new HubTrack(),
                new DriveXMeters(AutoConstants.hubXOffset, AutoConstants.DXMConstraints[0], AutoConstants.DXMConstraints[1]),
                new SillyShoot()
            );
            
        } else if(selectedAuto == Auto.Selection.SHOOTFIRST) {
            auto = new SequentialCommandGroup( //Start facing hub, shoot, reverse, get near cargo
                new HubTrack(),
                new DriveXMeters(AutoConstants.hubXOffset, AutoConstants.DXMConstraints[0], AutoConstants.DXMConstraints[1]),
                new SillyShoot(),
                new DriveXMeters(-AutoConstants.backupDistance, AutoConstants.DXMConstraints[0], AutoConstants.DXMConstraints[1]),
                new TurnXDegrees(180, AutoConstants.TXDConstraints[0], AutoConstants.TXDConstraints[1]),
                new DriveXMeters(AutoConstants.distToCargo + AutoConstants.hubXOffset - AutoConstants.backupDistance, AutoConstants.DXMConstraints[0], AutoConstants.DXMConstraints[1])
            ); 

        } else if(selectedAuto == Auto.Selection.SILLY) {
            auto = Auto.getSillyAuto();
        } else {
            auto = null;
        }
        return auto;
    }

    public static RobotContainer getInstance() {
        if(instance == null) instance = new RobotContainer();
        return instance;
    }

    /*
        @param trajectory: the trajectory to follow
    */
    public static Command getPathweaverCommand(Trajectory trajectory) {
        // Create a voltage constraint to ensure we don't accelerate too fast
        var autoVoltageConstraint =
            new DifferentialDriveVoltageConstraint(
                Drivetrain.FEEDFORWARD,
                Drivetrain.KINEMATICS,
                10);

        // Create config for trajectory
        TrajectoryConfig config =
            new TrajectoryConfig(
                    DrivetrainConstants.kMaxSpeedMPS,
                    DrivetrainConstants.kMaxAcceleration)
                // Add kinematics to ensure max speed is actually obeyed
                .setKinematics(Drivetrain.KINEMATICS)
                // Apply the voltage constraint
                .addConstraint(autoVoltageConstraint);

        // An example trajectory to follow.  All units in meters.
        RamseteCommand ramseteCommand =
            new RamseteCommand(
                trajectory,
                (()-> {return Drivetrain.ODOMETRY.getPoseMeters();}),
                new RamseteController(),
                Drivetrain.FEEDFORWARD,
                Drivetrain.KINEMATICS,
                Drivetrain::getWheelSpeeds,
                new PIDController(DrivetrainConstants.kPV, 0, 0),
                new PIDController(DrivetrainConstants.kPV, 0, 0),
                // RamseteCommand passes volts to the callback
                Drivetrain::setVoltages,
                Drivetrain.getInstance());
        drivetrain.resetEncoders();
        // Reset odometry to the starting pose of the trajectory.
        Drivetrain.ODOMETRY.resetPosition(trajectory.getInitialPose(), navX.getRotation2d());

        // Run path following command, then stop at the end.
        return ramseteCommand.andThen(() -> Drivetrain.setOpenLoop(0, 0));
    }

     /**
     * Deadbands an input to [-1, -deadband], [deadband, 1], rescaling inputs to be
     * linear from (deadband, 0) to (1,1)
     * 
     * @param input    The input value to rescale
     * @param deadband The deadband
     * @return the input rescaled and to fit [-1, -deadband], [deadband, 1]
     */
    public static double deadbandX(double input, double deadband) {
        if(Math.abs(input) <= deadband) {
            return 0;
        } else if(Math.abs(input) == 1) {
            return input;
        } else {
            return (1 / (1 - deadband) * (input + Math.signum(-input) * deadband));
        }
    }

    public static double getThrottle() {
        return -deadbandX(driverController.getLeftY(), Constants.DriverConstants.kJoystickDeadband);
    }
    public static double getAltThrottle() {
        return -deadbandX(driverController.getRightY(), Constants.DriverConstants.kJoystickDeadband);
    }

    public static double getTurn() {
        return deadbandX(driverController.getRightX(), Constants.DriverConstants.kJoystickDeadband);
    }

    /*public static Color getColor() {
        return colorSensorV3.getColor();
    }     

    public int getProximity() {
        return colorSensorV3.getProximity();
    }*/

    /**
     * Set the LED mode on the Limelight
     * 
     * @param ledMode The mode to set the Limelight LEDs to
     */
    public void setLEDMode(LEDMode ledMode) {
        limelight.getEntry("ledMode").setNumber(ledMode.val);
    }
    /**
     * Sets the appearance of the Limelight camera stream
     * 
     * @param stream Stream mode to set the Limelight to
     */
    public void setStreamMode(StreamMode stream) {
        limelight.getEntry("stream").setNumber(stream.val);
    }
    /**
     * Sets Limelight vision pipeline
     * 
     * @param pipeline The pipeline to use
     */
    public void setPipeline(IntakeVisionPipeline pipeline) {
        limelight.getEntry("pipeline").setNumber(pipeline.val);
    }
    /**
     * Returns the horizontal offset between the target and the crosshair in degrees
     * 
     * @return the horizontal offset between the target and the crosshair in degrees
     */
    public static double getXOffset() {
        return -limelight.getEntry("tx").getDouble(0);
    }
    
    public static double getDistance() {
        double offsetAngle = limelight.getEntry("ty").getDouble(0.0);
        double angleGoalRads = (VisionConstants.mountAngle + offsetAngle) * (Math.PI/180);
        return Units.InchesToMeters(VisionConstants.goalHeightInches - VisionConstants.limelightHeightInches)/(Math.tan(angleGoalRads));
    }
    /**
     * Returns the vertical offset between the target and the crosshair in degrees
     * 
     * @return the vertical offset between the target and the crosshair in degrees
     */
    public static double getYOffset() {
        return -limelight.getEntry("ty").getDouble(0.0);
    }
    /**
     * Enum representing the different possible Limelight LED modes
     */
    public enum LEDMode {
        PIPELINE(0), OFF(1), BLINK(2), ON(3);

        public int val;

        LEDMode(int val) {
            this.val = val;
        }
    }

    /**
     * Enum representing the different possible Limelight stream modes
     */
    public enum StreamMode {
        SIDE_BY_SIDE(0), PIP_MAIN(1), PIP_SECONDARY(2);

        public int val;

        StreamMode(int val) {
            this.val = val;
        }
    }

    /**
     * Enum representing the different possible Limelight vision pipelines
     */
    public enum VisionPipeline {
        VISION(0), DRIVER(1);

        public int val;

        VisionPipeline(int val) {
            this.val = val;
        }
    }

    public enum IntakeVisionPipeline {
        RED(2), BLUE(1), ROBOT(0), DRIVER(3), INVALID(4);

        public int val;

        IntakeVisionPipeline(int val) {
            this.val = val;
        }
        
    }
    public enum ShooterVisionPipeline {
        ROBOT(0);

        public int val;

        ShooterVisionPipeline(int val) {
            this.val = val;
        }
        
    }
    public static IntakeVisionPipeline allianceToPipeline() {
        Alliance alliance = DriverStation.getAlliance();
        switch(alliance) {
            case Blue:
                return IntakeVisionPipeline.BLUE;
            case Red:
                return IntakeVisionPipeline.RED;
            default:
                return IntakeVisionPipeline.INVALID;
        }
    }
    /*public static PhotonPipelineResult getSnapshot() {
        return camera.getLatestResult();
    }*/
}
