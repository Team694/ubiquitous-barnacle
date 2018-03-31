package org.usfirst.frc.team694.robot.commands.auton.routines;

import org.usfirst.frc.team694.robot.commands.LiftMoveToBottomCommand;
import org.usfirst.frc.team694.robot.commands.LiftMoveToHeightCommand;
import org.usfirst.frc.team694.robot.commands.QuisitorAcquireCommand;
import org.usfirst.frc.team694.robot.commands.QuisitorDeacquireCommand;
import org.usfirst.frc.team694.robot.commands.auton.DrivetrainMoveInchesEncoderCommand;
import org.usfirst.frc.team694.robot.commands.auton.DrivetrainRotateAbsoluteDegreesPIDCommand;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
public class TripleCubeSwitchAutonCommand extends CommandGroup {

    public TripleCubeSwitchAutonCommand() {
        addSequential(new DoubleCubeSwitchAutonCommand());
        addSequential(new DrivetrainMoveInchesEncoderCommand(48, -0.25));
        addSequential(new LiftMoveToBottomCommand());
        addSequential(new QuisitorAcquireCommand());
        addParallel(new DrivetrainRotateAbsoluteDegreesPIDCommand(-45));
        addSequential(new DrivetrainMoveInchesEncoderCommand(60, 0.25));
        addSequential(new DrivetrainMoveInchesEncoderCommand(-60, -0.25));  
        addSequential(new DrivetrainRotateAbsoluteDegreesPIDCommand(0));
        addSequential(new LiftMoveToHeightCommand(20));
        addSequential(new DrivetrainMoveInchesEncoderCommand(48, 1));
        addSequential(new QuisitorDeacquireCommand(), 1.0);
    }
}