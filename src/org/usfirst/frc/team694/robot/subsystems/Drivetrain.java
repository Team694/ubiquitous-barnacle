/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.            */
/* Open Source Software - may be modified and shared by FRC teams. The code  */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team694.robot.subsystems;

import org.usfirst.frc.team694.robot.RobotMap;
import org.usfirst.frc.team694.robot.commands.DrivetrainPiotrDriveCommand;
import org.usfirst.frc.team694.util.LineSensor;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 * An example subsystem. You can replace me with your own Subsystem.
 */
public class Drivetrain extends Subsystem {
    private WPI_TalonSRX leftTopMotor;
    private WPI_TalonSRX leftMiddleMotor;
    private WPI_TalonSRX leftBottomMotor;
    private WPI_TalonSRX rightTopMotor;
    private WPI_TalonSRX rightMiddleMotor;
    private WPI_TalonSRX rightBottomMotor;

    private DifferentialDrive differentialDrive;

    private LineSensor leftLineSensor;
    private LineSensor rightLineSensor;

    private Solenoid gearShift;

    private ADXRS450_Gyro gyro;


    public Drivetrain() {
        //TODO: Remove magic numbers: Add in RobotMap
        leftTopMotor = new WPI_TalonSRX(RobotMap.LEFT_FRONT_MOTOR_PORT);
        leftMiddleMotor = new WPI_TalonSRX(RobotMap.LEFT_MIDDLE_MOTOR_PORT);
        leftBottomMotor = new WPI_TalonSRX(RobotMap.LEFT_BOTTOM_MOTOR_PORT);

        //master-follower, leftTopMotor designated master
        leftMiddleMotor.follow(leftTopMotor);
        leftBottomMotor.follow(leftTopMotor);

        rightTopMotor = new WPI_TalonSRX(RobotMap.RIGHT_FRONT_MOTOR_PORT);
        rightMiddleMotor = new WPI_TalonSRX(RobotMap.RIGHT_MIDDLE_MOTOR_PORT);
        rightBottomMotor = new WPI_TalonSRX(RobotMap.RIGHT_REAR_MOTOR_PORT);

        //master-follower, rightTopMotor designated master
        rightMiddleMotor.follow(rightTopMotor);
        rightBottomMotor.follow(rightTopMotor);

        rightTopMotor.setInverted(true);
        rightMiddleMotor.setInverted(true);
        rightBottomMotor.setInverted(true);

        leftTopMotor.setNeutralMode(NeutralMode.Coast);
        leftMiddleMotor.setNeutralMode(NeutralMode.Coast);
        leftBottomMotor.setNeutralMode(NeutralMode.Coast);
        rightTopMotor.setNeutralMode(NeutralMode.Coast);
        rightMiddleMotor.setNeutralMode(NeutralMode.Coast);
        rightBottomMotor.setNeutralMode(NeutralMode.Coast);

        leftBottomMotor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
        rightBottomMotor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);

        leftLineSensor = new LineSensor(RobotMap.DRVETRAIN_LINE_SENSOR_LEFT_PORT);
        rightLineSensor = new LineSensor(RobotMap.DRVETRAIN_LINE_SENSOR_RIGHT_PORT);

        gearShift = new Solenoid(RobotMap.GEAR_SHIFT_CHANNEL);

        //leftEncoder.setDistancePerPulse(RobotMap.DRIVETRAIN_ENCODER_INCHES_PER_PULSE);
        //rightEncoder.setDistancePerPulse(RobotMap.DRIVETRAIN_ENCODER_INCHES_PER_PULSE);

        differentialDrive = new DifferentialDrive(leftTopMotor, rightTopMotor);

        gyro = new ADXRS450_Gyro();

    }

    public double getLeftSpeed() {
        return leftBottomMotor.getSelectedSensorVelocity(0);
    }

    public double getRightSpeed() {
        return rightBottomMotor.getSelectedSensorVelocity(0);
    }

    public double getSpeed() {
        return Math.max(Math.abs(getLeftSpeed()), Math.abs(getRightSpeed()));
    }

    public double getEncoderDistance() {
        return Math.abs(Math.max(getLeftEncoderDistance(), getRightEncoderDistance()));
    }

    public double getLeftEncoderDistance() {
        return leftBottomMotor.getSelectedSensorPosition(0)  * RobotMap.DRIVETRAIN_RAW_MULTIPLIER;
    }

    public double getRightEncoderDistance() {
        return rightBottomMotor.getSelectedSensorPosition(0) * RobotMap.DRIVETRAIN_RAW_MULTIPLIER;
    }

    public double getLeftRawEncoderDistance() {
        return leftBottomMotor.getSelectedSensorPosition(0);
    }

    public double getRightRawEncoderDistance() {
        return rightBottomMotor.getSelectedSensorPosition(0);
    }

    public void resetEncoders() {
        leftBottomMotor.setSelectedSensorPosition(0, 0, 0);
        rightBottomMotor.setSelectedSensorPosition(0, 0, 0);
    }

    public void tankDrive(double left, double right) {
        differentialDrive.tankDrive(left, right);
    }

    public void arcadeDrive(double speed, double rotation) {
        differentialDrive.arcadeDrive(speed, rotation);
    }

    public void stop() {
        tankDrive(0, 0);
    }

    public void highGearShift() {
        gearShift.set(false);
    }

    public void lowGearShift() {
        gearShift.set(true);
    }

    public void toggleGearShift() {
        boolean m = !(gearShift.get());
        gearShift.set(m);
    }
    public void resetLineSensors(){
        leftLineSensor.resetAmbient();
        rightLineSensor.resetAmbient();
    }
    public double getGyroAngle(){
        return gyro.getAngle();
    }
    public void updateSensors(){
        rightLineSensor.mainLoop();
        leftLineSensor.mainLoop();
    }
    public boolean isOnLine(int mode){
        return leftLineSensor.basicFind(mode) || rightLineSensor.basicFind(mode);
    }
    public boolean rightIsOnLine(int mode){
        return rightLineSensor.basicFind(mode);
    }
    public boolean leftIsOnLine(int mode){
        return leftLineSensor.basicFind(mode);
    }

    public void initDefaultCommand() {
        //setDefaultCommand(new DriveCommand());
        setDefaultCommand(new DrivetrainPiotrDriveCommand());
    }

    public void resetGyro() {
        // TODO Auto-generated method stub
        gyro.reset();
    }

}
