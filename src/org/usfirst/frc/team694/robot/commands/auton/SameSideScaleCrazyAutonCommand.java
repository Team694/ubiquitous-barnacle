package org.usfirst.frc.team694.robot.commands.auton;

import org.usfirst.frc.team694.robot.RobotMap;
import org.usfirst.frc.team694.robot.commands.GrabberOpenCommand;
import org.usfirst.frc.team694.robot.commands.LiftMoveToBottomCommand;
import org.usfirst.frc.team694.robot.commands.LiftMoveToHeightCommand;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 * Totally tentative swerving command
 */
public class SameSideScaleCrazyAutonCommand extends CommandGroup {

    private static final double DISTANCE_TOTAL = 283;
    private static final double DISTANCE_TO_SWERVE = 130;

    public SameSideScaleCrazyAutonCommand() {

        // Move lift when we're kinda close
        addParallel(new ConditionalDistanceEncodersCommand(new LiftMoveToHeightCommand(89 - RobotMap.MIN_HEIGHT_OF_LIFT), DISTANCE_TOTAL - 100));
        addSequential(new DrivetrainRampSwerveCommand(DISTANCE_TOTAL, DISTANCE_TO_SWERVE, -24));

        addSequential(new GrabberOpenCommand());

        addSequential(new DrivetrainMoveInchesEncoderCommand(-0.5, 10));

//        addSequential(new LiftMoveToBottomCommand());
    }
}
