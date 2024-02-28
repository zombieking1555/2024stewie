// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.intake;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.intake.Wrist;

public class RotateWristPID extends Command {
  /** Creates a new wristIn. */
  private final Wrist c_intake;
  private final double c_target;

  public RotateWristPID(Wrist intake, double target) {
    c_intake = intake;
    c_target = target;
    addRequirements(c_intake);
    // Use addRequirements() here to declare subsystem dependencies.

  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    c_intake.setPIDTarget(c_target);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    c_intake.rotateWristPID();
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    c_intake.setWristVoltage(0.0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    // return c_intake.pidAtSetpoint();
    return false;
  }
}
