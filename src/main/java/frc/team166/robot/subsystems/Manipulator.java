/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.team166.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Subsystem;
import frc.team166.chopshoplib.commands.ActionCommand;
import frc.team166.chopshoplib.commands.SubsystemCommand;
import frc.team166.robot.RobotMap;

public class Manipulator extends Subsystem {
    // Put methods for controlling this subsystem
    // here. Call these from Commands.

    WPI_TalonSRX leftRoller = new WPI_TalonSRX(RobotMap.CAN.ROLLER_LEFT);
    WPI_TalonSRX rightRoller = new WPI_TalonSRX(RobotMap.CAN.ROLLER_RIGHT);
    SpeedControllerGroup rollers = new SpeedControllerGroup(leftRoller, rightRoller);

    DoubleSolenoid manipulatorSolenoid = new DoubleSolenoid(RobotMap.Solenoids.MANIPULATOR_SOLENOID_A,
            RobotMap.Solenoids.MANIPULATOR_SOLENOID_B);

    AnalogInput irSensor = new AnalogInput(RobotMap.AnalogInputs.IR);

    double ROLLER_RADIUS = 1.4375; //inches
    double DIST_PER_PULSE_INTAKE = (((ROLLER_RADIUS * 2.0 * Math.PI) / 1024.0) / 12.0); //feet
    double OPTIMAL_MOTOR_RATE = 6.81; //ft/s

    public Manipulator() {
        addChild(rollers);
        addChild(irSensor);

        leftRoller.setInverted(true);

        //Preferences Are Wanted In The Constructer So They Can Appear On Live Window
        Preferences.getInstance().getDouble(RobotMap.Preferences.MANIPULATOR_MOTOR_INTAKE_SPEED, 0.8);
        Preferences.getInstance().getDouble(RobotMap.Preferences.MANIPULATOR_MOTOR_DISCHARGE_SPEED, -0.8);
        Preferences.getInstance().getDouble(RobotMap.Preferences.CUBE_PICKUP_DISTANCE, 0.5);
        Preferences.getInstance().getDouble(RobotMap.Preferences.CUBE_EJECT_WAIT_TIME, 5.0);
    }

    /**
     * Gets IR voltage
     * <p>
     * Returns the voltage value output from the IR sensor
     * 
     * @return The voltage from the IR sensor
     */
    private double getIRDistance() {
        return irSensor.getVoltage(); //Manipulate this data pending experimentation
    }

    private void openManipulator() {
        manipulatorSolenoid.set(Value.kForward);
    }

    private void closeManipulator() {
        manipulatorSolenoid.set(Value.kReverse);
    }

    /**
     * Sets motors to intake mode
     * <p>
     * Sets the motors to 4/5 power inward to take in a cube on the field
     */
    private void setMotorsToIntake() {
        //change once you find optimal motor speed
        rollers.set(Preferences.getInstance().getDouble(RobotMap.Preferences.MANIPULATOR_MOTOR_INTAKE_SPEED, 0.8));
    }

    /**
     * Sets motors to discharge mode
     * <p>
     * Sets the motors to 4/5 power outward to eject a stored cube
     */
    private void setMotorsToDischarge() {
        rollers.set(Preferences.getInstance().getDouble(RobotMap.Preferences.MANIPULATOR_MOTOR_DISCHARGE_SPEED, -0.8)); //change once you find optimal motor speed
    }

    //COMMANDS
    public void initDefaultCommand() {
    };

    public Command DropCube() {
        return new ActionCommand("Drop Cube", this, this::openManipulator);
    }

    public Command CloseManipulator() {
        return new ActionCommand("Close Manipulator", this, this::closeManipulator);
    }

    public Command CubePickup() {
        return new SubsystemCommand("Pick Up Cube", this) {

            @Override
            protected void initialize() {
                setMotorsToIntake();
            }

            @Override
            protected boolean isFinished() {
                if (getIRDistance() < Preferences.getInstance().getDouble(RobotMap.Preferences.CUBE_PICKUP_DISTANCE,
                        0.5)) {
                    return true;
                }
                return false;
            }

            @Override
            protected void end() {
                rollers.stopMotor();
            }
        };
    }

    public Command CubeEject() {
        return new SubsystemCommand("Eject Cube", this) {

            @Override
            protected void initialize() {
                setTimeout(Preferences.getInstance().getDouble(RobotMap.Preferences.CUBE_EJECT_WAIT_TIME, 5.0));
                setMotorsToDischarge();
            }

            @Override
            protected void execute() {

            }

            @Override
            protected boolean isFinished() {

                return isTimedOut();
            }

            @Override
            protected void end() {
                rollers.stopMotor();
            }

            @Override
            protected void interrupted() {
                end();
            }
        };
    }
}