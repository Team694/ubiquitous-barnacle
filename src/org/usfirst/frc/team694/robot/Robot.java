/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team694.robot;

import org.usfirst.frc.team694.robot.commands.GamepadRumbleWhenCubeDetectedCommand;
import org.usfirst.frc.team694.robot.commands.auton.choosers.BlackHawksDoubleCubeSwitchAutonChooserCommand;
import org.usfirst.frc.team694.robot.commands.auton.choosers.CommandChooser;
import org.usfirst.frc.team694.robot.commands.auton.choosers.DoubleCubeSwitchThenGrabAnotherAutonChooserCommand;
import org.usfirst.frc.team694.robot.commands.auton.choosers.KillerTreesDoubleCubeSwitchAutonChooserCommand;
import org.usfirst.frc.team694.robot.commands.auton.choosers.KryptoniteAutonCommandChooser;
import org.usfirst.frc.team694.robot.commands.auton.choosers.RoboTigersMobilityAutonChooserCommand;
import org.usfirst.frc.team694.robot.commands.auton.choosers.SingleCube90DegreeScaleAutonChooserCommand;
import org.usfirst.frc.team694.robot.commands.auton.choosers.SingleCubeSwitchThenStartScaleAutonChooserCommand;
import org.usfirst.frc.team694.robot.commands.auton.choosers.TripleCubeScaleAutonChooserCommand;
import org.usfirst.frc.team694.robot.commands.auton.routines.MobilityAutonCommand;
import org.usfirst.frc.team694.robot.subsystems.Drivetrain;
import org.usfirst.frc.team694.robot.subsystems.Lift;
import org.usfirst.frc.team694.robot.subsystems.Quisitor;
import org.usfirst.frc.team694.util.ArduinoLED;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {

    // true:  Mildcard (Practice)
    // false: Wildcard (Competition)
    public static final boolean IS_MILDCARD = false;

    // The main instance of Robot
    private static Robot myInstance;

    public static Drivetrain drivetrain;
    public static Quisitor quisitor;
    public static Lift lift;

    public static OI oi;

    private static ArduinoLED liftLED;

    //    static boolean isRobotAtBottom;

    private String gameData;
    private static boolean isRobotOnRight;
    private static boolean isAllianceSwitchRight;
    private static boolean isScaleRight;
    private static boolean isInTeleop;

    private static SendableChooser<Command> autonChooser = new SendableChooser<>();
    private static SendableChooser<RobotStartPosition> sideChooser = new SendableChooser<>();

    private Command autonCommand; // Selected command run during auton

    //private GamepadRumbleWhenCubeDetectedCommand driverpadRumbleCommand;
    private GamepadRumbleWhenCubeDetectedCommand operatorpadRumbleCommand;

    //    private PowerDistributionPanel pdppanel;

    @Override
    public void robotInit() {
        myInstance = this;

        drivetrain = new Drivetrain();
        quisitor = new Quisitor();
        lift = new Lift();
        oi = new OI();

        liftLED = new ArduinoLED(RobotMap.LIFTLIGHTING_ADDRESS);

        liftLED.initialize();
        initSmartDashboard();
        
        //driverpadRumbleCommand = new GamepadRumbleWhenCubeDetectedCommand(Robot.oi.driverGamepad, 0.5, 0.5);
        operatorpadRumbleCommand = new GamepadRumbleWhenCubeDetectedCommand(Robot.oi.operatorGamepad, 0.5, 0.5);
    }

    public enum RobotStartPosition {
        RIGHT_SIDE_OF_DRIVER, LEFT_SIDE_OF_DRIVER
    }

    //Bottom means side closer to the scoring table
    public static FieldMapInterface getRobotQuadrant() {
        // TESTING ONLY
        return new FieldMapRedFarFromScoringTableQuadrant();

        /*if(DriverStation.getInstance().getAlliance() == DriverStation.Alliance.Red) {
            if(isRobotOnRight) {
                return new FieldMapRedFarFromScoringTableQuadrant(); 
            }
            return new FieldMapRedNearScoringTableQuadrant();      
        }
        if(isRobotOnRight) {
            return new FieldMapBlueNearScoringTableQuadrant();
        }
        return new FieldMapBlueFarFromScoringTableQuadrant();
        */
    }

    @Override
    public void disabledInit() {
        Scheduler.getInstance().removeAll();
    }

    @Override
    public void disabledPeriodic() {
        Scheduler.getInstance().run();
    }

    @Override
    public void autonomousInit() {
        Scheduler.getInstance().removeAll();
        isInTeleop = false;
        lift.enableRamping();
        liftLED.initialize();

        drivetrain.resetEncoders();
        drivetrain.resetGyro();
        drivetrain.resetGyroError();

        gameData = null;

        double timestamp = Timer.getFPGATimestamp();
        while ((Timer.getFPGATimestamp() - timestamp) < 5 && (gameData == null || gameData.isEmpty())) {
            gameData = DriverStation.getInstance().getGameSpecificMessage();
        }
        if (gameData == null || gameData.isEmpty()) {//If there is no field data run mobility
            //autonCommand = new MobilityAutonCommand();
            System.err.print("******* Field Data Problem!!!");
            System.err.println("Please yell at the field management crew to fix this");
        } else {
            isRobotOnRight = (sideChooser.getSelected() == RobotStartPosition.RIGHT_SIDE_OF_DRIVER);
            System.out.println(
                    "[Robot] SIDE CHOOSER: " + sideChooser.getSelected() + ", equals right? " + isRobotOnRight);
            isAllianceSwitchRight = gameData.charAt(0) == 'R';
            isScaleRight = gameData.charAt(1) == 'R';
            autonCommand = autonChooser.getSelected();
        }

        if (autonCommand != null) {
            System.out.println("[Robot] SELECTED AUTON: " + autonCommand.getName());
            autonCommand.start();
        }
    }

    @Override
    public void autonomousPeriodic() {
        Scheduler.getInstance().run();
    }

    @Override
    public void teleopInit() {
        isInTeleop = true;
        lift.enableRamping();
        liftLED.initialize();

        drivetrain.resetEncoders();
        drivetrain.resetGyro();
        drivetrain.resetGyroError();
        drivetrain.setRamp(0.0);

        if (autonCommand != null) {
            autonCommand.cancel();
        }

//        driverpadRumbleCommand.start();
        operatorpadRumbleCommand.start();
    }

    @Override
    public void teleopPeriodic() {
        Scheduler.getInstance().run();
    }

    @Override
    public void robotPeriodic() {
        updateSmartDashboard();
        liftLED.periodic();
    }

    private void initSmartDashboard() {
        // AUTON CHOOSER
        autonChooser.addDefault("Do Nothing", new CommandGroup());
        autonChooser.addObject("Mobility", new MobilityAutonCommand());
        autonChooser.addObject("1.5 switch go to scale", new SingleCubeSwitchThenStartScaleAutonChooserCommand());//new SingleCubeSwitchThenStartLeftScaleAutonCommand(false));
        autonChooser.addObject("2.5 center switch", new DoubleCubeSwitchThenGrabAnotherAutonChooserCommand());
        autonChooser.addObject("2.5 same side scale or 1.5 opposite side", new TripleCubeScaleAutonChooserCommand());
        autonChooser.addObject("Fit with Black Hawk/KillerTrees", 
                new CommandChooser(
                        new TripleCubeScaleAutonChooserCommand(), 
                        new BlackHawksDoubleCubeSwitchAutonChooserCommand(), 
                        new TripleCubeScaleAutonChooserCommand(), 
                        new KillerTreesDoubleCubeSwitchAutonChooserCommand()));
        autonChooser.addObject("Don't fit with Black Hawk/KillerTrees", 
                new CommandChooser(
                        new TripleCubeScaleAutonChooserCommand(), 
                        new BlackHawksDoubleCubeSwitchAutonChooserCommand(), 
                        new TripleCubeScaleAutonChooserCommand(), 
                        new RoboTigersMobilityAutonChooserCommand()));
        autonChooser.addObject("Fit with 195",
                new CommandChooser(
                        new SingleCube90DegreeScaleAutonChooserCommand(),
                        new KryptoniteAutonCommandChooser(), 
                        new SingleCube90DegreeScaleAutonChooserCommand(),
                        new KillerTreesDoubleCubeSwitchAutonChooserCommand()));
        autonChooser.addObject("Don't fit with 195", 
                new CommandChooser(
                        new SingleCube90DegreeScaleAutonChooserCommand(),
                        new KryptoniteAutonCommandChooser(), 
                        new SingleCube90DegreeScaleAutonChooserCommand(),
                        new RoboTigersMobilityAutonChooserCommand()));
        autonChooser.addObject("Fit with 2056",
                new CommandChooser(
                        new SingleCube90DegreeScaleAutonChooserCommand(),
                        new BlackHawksDoubleCubeSwitchAutonChooserCommand(), 
                        new SingleCube90DegreeScaleAutonChooserCommand(), 
                        new KillerTreesDoubleCubeSwitchAutonChooserCommand()));
        autonChooser.addObject("Don't fit with 2056", 
                new CommandChooser(
                        new SingleCube90DegreeScaleAutonChooserCommand(),
                        new BlackHawksDoubleCubeSwitchAutonChooserCommand(), 
                        new SingleCube90DegreeScaleAutonChooserCommand(), 
                        new RoboTigersMobilityAutonChooserCommand()));
        SmartDashboard.putData("Autonomous", autonChooser);

        // SIDE CHOOSER
        sideChooser.addDefault("Right", RobotStartPosition.RIGHT_SIDE_OF_DRIVER);
        sideChooser.addObject("Left", RobotStartPosition.LEFT_SIDE_OF_DRIVER);
        SmartDashboard.putData("Where is the bot starting?", sideChooser);

        // PDP Panel
        //        SmartDashboard.putData("PDP", pdppanel);

        SmartDashboard.putNumber("Lift P", 0.3);

        SmartDashboard.putNumber("DriveStraight RampSeconds", 0.0);

        // Drive Straight Distance PID
        SmartDashboard.putNumber("DriveDistanceEncodersPID P", 0.01);
        SmartDashboard.putNumber("DriveDistanceEncodersPID I", 0);
        SmartDashboard.putNumber("DriveDistanceEncodersPID D", 0.08);

        // Drive Straight Rotation PID
        SmartDashboard.putNumber("DriveStraightGyroPID P", 0.016);
        SmartDashboard.putNumber("DriveStraightGyroPID I", 0);
        SmartDashboard.putNumber("DriveStraightGyroPID D", 0.08);

        SmartDashboard.putNumber("RotateDegreesPID P", 0.02);
        SmartDashboard.putNumber("RotateDegreesPID I", 0.0);
        SmartDashboard.putNumber("RotateDegreesPID D", 0.05);

        SmartDashboard.putNumber("RotateDegreesPID RampSeconds", 0.0);

        SmartDashboard.putNumber("DriveStraight Encoder Vel", 0);
    }

    private void updateSmartDashboard() {

        //        SmartDashboard.putData(pdppanel);
        SmartDashboard.putData("Scheduler", Scheduler.getInstance());

        SmartDashboard.putBoolean("Lift: Top Limit Switch", Robot.lift.isAtTop());
        SmartDashboard.putNumber("Lift: Follower Encoder Values", Robot.lift.getFollowerEncoderDistance());
        SmartDashboard.putNumber("Lift: Master Encoder Values", Robot.lift.getMasterEncoderDistance());
        SmartDashboard.putBoolean("Lift: Bottom Limit Switch", Robot.lift.isAtBottom());
        SmartDashboard.putNumber("Lift Speed", Robot.lift.getSpeed());

        SmartDashboard.putBoolean("Drivetrain: Gear Shift", Robot.drivetrain.isGearShift());
        SmartDashboard.putNumber("Drivetrain: Left Encoder Values", Robot.drivetrain.getLeftEncoderDistance());
        SmartDashboard.putNumber("Drivetrain: Right Encoder Values", Robot.drivetrain.getRightEncoderDistance());
        SmartDashboard.putNumber("Drivetrain: RAW Left Encoder Values", Robot.drivetrain.getLeftRawEncoderDistance());
        SmartDashboard.putNumber("Drivetrain: RAW Right Encoder Values", Robot.drivetrain.getRightRawEncoderDistance());
        SmartDashboard.putNumber("Drivetrain: Gyro Value", Robot.drivetrain.getGyroAngle());
        SmartDashboard.putNumber("Drivetrain: Gyro ABSOLUTE", Robot.drivetrain.getAbsoluteGyroAngle());
        SmartDashboard.putNumber("Drivetrain: Sonar Front Value", Robot.drivetrain.getFrontInchesAway());
        SmartDashboard.putNumber("Drivetrain: Sonar Rear Value", Robot.drivetrain.getRearInchesAway());

        SmartDashboard.putBoolean("Drivetrain: Left Line Sensor On Line", Robot.drivetrain.leftIsOnLine());
        SmartDashboard.putBoolean("Drivetrain: Right Line Sensor On Line", Robot.drivetrain.rightIsOnLine());
        SmartDashboard.putNumber("Drivetrain: Raw Left Line Sensor", Robot.drivetrain.getRawLeftLineSensor());
        SmartDashboard.putNumber("Drivetrain: Raw Right Line Sensor", Robot.drivetrain.getRawRightLineSensor());

        SmartDashboard.putBoolean("Quisitor: Detect Cube", Robot.quisitor.isCubeDetected());

        SmartDashboard.putNumber("Lift Current", lift.getCurrent());
        SmartDashboard.putNumber("Drivetrain Current", drivetrain.getCurrent());
        SmartDashboard.putNumber("Lift + Drivetrain Current", lift.getCurrent() + drivetrain.getCurrent());

        SmartDashboard.putString("Drivetrain Current Command", drivetrain.getCurrentCommandName());
    }

    /**
     * S This function is called periodically during test mode.
     */
    @Override
    public void testPeriodic() {
    }

    public static boolean isRobotStartingOnRight() {
        return isRobotOnRight;
    }

    public static boolean isSwitchOnRight() {
        return isAllianceSwitchRight;
    }

    public static boolean isScaleOnRight() {
        return isScaleRight;
    }

    public static boolean isRobotOnSameSideScale() {
        return !(isRobotOnRight ^ isScaleRight);
    }

    public static boolean isSwitchOnSameSideScale() {
        return !(isAllianceSwitchRight ^ isScaleRight);
    }

    public static boolean isRobotSwitchScaleOnSameSide() {
        return isRobotOnSameSideScale() && isSwitchOnSameSideScale();
    }

    public static boolean isRobotOnSameSideSwitch() {
        return !(isRobotOnRight ^ isAllianceSwitchRight);
    }

    public static Robot getInstance() {
        return myInstance;
    }

    public static boolean isInTeleop() {
        return isInTeleop;
    }
}
