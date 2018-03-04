package org.usfirst.frc.team694.robot.commands.auton;

import org.usfirst.frc.team694.robot.Robot;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.ConditionalCommand;

/**
 *
 */
public class SideSwitchAutonChooserCommand extends ConditionalCommand {

    private static final double SPATULA_DEACQUIRE_TIME = 1.5;

    public SideSwitchAutonChooserCommand() {
        super(new RightSideSwitchAutonCommand(), new LeftSideSwitchAutonCommand()); 
    }

    @Override
    protected boolean condition() {
        return Robot.isRobotAndSwitchOnSameSide();
    }

    // To be used in the two switch commands: Runs the spatula deacquire for a period of time
    static class SpatulaDeacquireTimeCommand extends Command {
        public SpatulaDeacquireTimeCommand() {
            requires(Robot.spatula);
            setTimeout(SPATULA_DEACQUIRE_TIME);
        }

        @Override
        public void initialize() {
            Robot.spatula.deacquire();
        }

        @Override
        public void end() {
            Robot.spatula.stop();
            System.out.println("[SpatulaDeacquireTimeCommand] done");
        }

        @Override
        protected boolean isFinished() {
            return isTimedOut();
        }
    }

}