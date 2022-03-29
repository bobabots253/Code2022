// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.RobotContainer;
import frc.robot.Autonomous.Auto;
import frc.robot.Constants.DriverConstants;
import frc.robot.commands.CargoTrack;
import frc.robot.commands.Drive;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;


/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  public enum TestSubsystem {
    DRIVETRAIN, ARM, INTAKE, CONVEYOR, SHOOTER, CLIMB;
  }
  private TestSubsystem selected_subsystem;
  private String m_autoSelected;
  private final SendableChooser<TestSubsystem> t_subsystem = new SendableChooser<>();
  private final SendableChooser<Command> m_chooser = new SendableChooser<>();
  private final SendableChooser<Boolean> use_V3 = new SendableChooser<>(); //Use ColorSensorV3 over Photoelectric for conveyor queuing
  private RobotContainer robot;
  private PowerDistribution pdp = new PowerDistribution();
  private static boolean use_csV3 = false;
  public static boolean useV3() {
    return use_csV3; //prevent unwanted writing operations but allow reading
  }
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    robot = RobotContainer.getInstance();
    pdp.clearStickyFaults();
    t_subsystem.setDefaultOption("Test Drivetrain", TestSubsystem.DRIVETRAIN);
    t_subsystem.addOption("Test Arm", TestSubsystem.ARM);
    t_subsystem.addOption("Test Intake", TestSubsystem.INTAKE);
    t_subsystem.addOption("Test Shooter", TestSubsystem.SHOOTER);
    //t_subsystem.addOption("Test Climb", TestSubsystem.CLIMB);
    
    m_chooser.setDefaultOption("Shoot First", RobotContainer.getAutonomousCommand(Auto.Selection.SHOOTFIRST));
    m_chooser.addOption("Intake First", RobotContainer.getAutonomousCommand(Auto.Selection.INTAKEFIRST));
    use_V3.setDefaultOption("No", false);
    use_V3.addOption("Yes", true);
    SmartDashboard.putData("Subsystem choices", t_subsystem);
    SmartDashboard.putData("Auto choices", m_chooser);
    SmartDashboard.putData("Use ColorSensorV3 queuing?", use_V3);
    Drivetrain.getInstance().resetEncoders();
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    use_csV3 = use_V3.getSelected();
    CommandScheduler.getInstance().run();
    // SmartDashboard.putNumber("dt left enc", Drivetrain.getLeftEnc());
    // SmartDashboard.putNumber("dt right enc", Drivetrain.getRightEnc());
    SmartDashboard.putNumber("limelight distance", RobotContainer.getDistance());
    

  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  private Command auto;
  @Override
  public void autonomousInit() {
    CommandScheduler.getInstance().schedule(auto = m_chooser.getSelected());
    pdp.clearStickyFaults();
    //CommandScheduler.getInstance().schedule(new VisionTrack());
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);

  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    //
    /*switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }*/
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    if(auto != null) auto.cancel();

  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    selected_subsystem = t_subsystem.getSelected();
    switch(selected_subsystem) {
      case DRIVETRAIN:
        double left = RobotContainer.getThrottle() * DriverConstants.kDriveSens, right = RobotContainer.getAltThrottle() * DriverConstants.kDriveSens;
        Drivetrain.setOpenLoop(left, right);
        break;
      case ARM:
        double power = RobotContainer.getThrottle() * DriverConstants.kDriveSens;
        Arm.getInstance().setOpenLoop(power);
        break;
      case INTAKE:
        double conveyorPower = RobotContainer.getThrottle() * DriverConstants.kDriveSens;
        Intake.getInstance().intake(conveyorPower / DriverConstants.kDriveSens * 0.7);
        Intake.getInstance().setConveyor(0.0);
        break;
      case SHOOTER:
        double shooterPower = RobotContainer.getThrottle() * DriverConstants.kDriveSens, kickerPower = RobotContainer.getAltThrottle() * DriverConstants.kDriveSens;
        Shooter.getInstance().setOpenLoop(shooterPower);
        Shooter.getInstance().setStagingMotor(kickerPower);
        break;
    }

  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}
}
