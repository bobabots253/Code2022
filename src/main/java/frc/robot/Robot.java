// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.RobotContainer;
import frc.robot.Autonomous.Auto;
import frc.robot.Constants.DriverConstants;
import frc.robot.RobotContainer.LEDMode;
import frc.robot.commands.CargoTrack;
import frc.robot.commands.Drive;
import frc.robot.commands.HubTrack;
import frc.robot.commands.Shoot;
import frc.robot.commands.SillyDriveX;
import frc.robot.commands.SillyShoot;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;

import java.io.IOException;
import java.nio.file.Path;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryUtil;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;


/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private RobotContainer robot;
  private PowerDistribution pdp = new PowerDistribution();
  private static boolean use_csV3 = false;
  private enum TeleopStrat {
    OFFENSE, DEFENSE
  }
  
  public static Trajectory smallTraj = new Trajectory();
  private static final String smallJSON = "paths/Small.wpilib.json";
  
  private static final String[] auto1JSON = {"paths/Auto1.wpilib.json", "paths/Auto2.wpilib.json", "paths/Auto3.wpilib.json"};
  public static Trajectory[] autoGroup1 = new Trajectory[3];
  private final SendableChooser<Command> m_chooser = new SendableChooser<>();
  private final SendableChooser<Boolean> use_V3 = new SendableChooser<>(); //Use ColorSensorV3 over Photoelectric for conveyor queuing
  private final SendableChooser<TeleopStrat> teleopStrat = new SendableChooser<>();
  public static boolean useV3() {
    return use_csV3; //prevent unwanted writing operations but allow reading
  }
  
  public Trajectory initializeTrajectory(final String tjson) {
    Trajectory t = null;
    Path tPath = Filesystem.getDeployDirectory().toPath().resolve(tjson);
    try {
      t = TrajectoryUtil.fromPathweaverJson(tPath);
    } catch (IOException e) {
      System.out.println("silly pathweaver bad");
      e.printStackTrace();
    }
    return t;
  }

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    robot = RobotContainer.getInstance();
    pdp.clearStickyFaults();
    m_chooser.setDefaultOption("Shoot First", RobotContainer.getAutonomousCommand(Auto.Selection.SHOOTFIRST));
    m_chooser.addOption("Intake First", RobotContainer.getAutonomousCommand(Auto.Selection.INTAKEFIRST));
    m_chooser.addOption("Be Silly", RobotContainer.getAutonomousCommand(Auto.Selection.SILLY));
    use_V3.setDefaultOption("Use photoelectric indexing", false);
    use_V3.addOption("Use colorsensorV3 indexing", true);
    teleopStrat.setDefaultOption("Offense", TeleopStrat.OFFENSE);
    teleopStrat.addOption("Defense", TeleopStrat.DEFENSE);
    SmartDashboard.putData("Auto choices", m_chooser);
    SmartDashboard.putData("Use ColorSensorV3 queuing?", use_V3);
    SmartDashboard.putData("Teleop Strategy", teleopStrat);

    /*Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajectoryJSON);
    try {
      trajectory = TrajectoryUtil.fromPathweaverJson(trajectoryPath);
    } catch (IOException e) {
      System.out.println("silly pathweaver bad");
      e.printStackTrace();
    }*/
    smallTraj = initializeTrajectory(smallJSON);
    for(int i = 0; i < autoGroup1.length; i++) {
      autoGroup1[i] = initializeTrajectory(auto1JSON[i]);
    }
    //Arm.getInstance().resetEncoders();
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
    CommandScheduler.getInstance().run();
    use_csV3 = use_V3.getSelected();
    SmartDashboard.putNumber("pdp channel 10", pdp.getCurrent(10));
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
    pdp.clearStickyFaults();
    CommandScheduler.getInstance().schedule();
    //CommandScheduler.getInstance().schedule(new SillyDriveX(0.5, true));
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
    robot.setLEDMode(LEDMode.OFF);
    Shooter.getInstance().setDefaultCommand(new Shoot(18.965)); //TODO: test RPM (should be > 3500)
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    switch(teleopStrat.getSelected()) {
      case OFFENSE:
        Drivetrain.setInverted(false);
        break;
      case DEFENSE:
        Drivetrain.setInverted(true);
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
