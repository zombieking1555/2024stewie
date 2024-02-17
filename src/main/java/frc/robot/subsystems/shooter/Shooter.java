// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.shooter;

import java.util.Map;
import java.util.function.DoubleSupplier;

import com.ctre.phoenix6.SignalLogger;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.StrictFollower;
import com.ctre.phoenix6.controls.VelocityDutyCycle;
import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.DoubleEntry;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;

import frc.robot.constants.CANConstants;
import frc.robot.constants.DIOConstants;
import frc.robot.constants.ShooterConstants;
import frc.robot.subsystems.SubsystemABC;

public class Shooter extends SubsystemABC {
  // Motors
  private final TalonFX shooterTopMain; // Falcon
  private final TalonFX shooterBottomFollower; // Falcon
  private final TalonFX shooterRotate; // Kraken

  // Encoder
  private final DutyCycleEncoder shooterRotateEncoder; // Through Bore Encoder
  private final DoubleSupplier currentArmRotationSupplier;

  private final PIDController rotatePID = new PIDController(ShooterConstants.kRotateP, ShooterConstants.kRotateI,
      ShooterConstants.kRotateD);

  private final DoubleEntry shootVelocity;
  private final DoubleEntry shootVoltage;
  private final DoubleEntry rotateVoltage;
  private final DoubleEntry rotateTarget ;
  private final DoubleEntry encoderValue ;
  private final DoubleEntry encoderAngleWithoutOffset; 
  private final DoubleEntry encoderAngle ;

  public Shooter(DoubleSupplier currentArmRotationSupplier) {
    super("Shooter");
    shooterTopMain = new TalonFX(CANConstants.Shooter.kShooterTop);
    shooterBottomFollower = new TalonFX(CANConstants.Shooter.kShooterBottom);
    shooterRotate = new TalonFX(CANConstants.Shooter.kShooterPivot);
    shooterRotateEncoder = new DutyCycleEncoder(DIOConstants.Shooter.kShooterRotateEncoder); // FIXME: WHAT IS THIS
                                                                                             // ENCODER VALUE
    this.currentArmRotationSupplier = currentArmRotationSupplier;

    rotatePID.setTolerance(ShooterConstants.kRotateTolerance);
    rotatePID.setIZone(ShooterConstants.kRotateIZone);

    SignalLogger.start();
    SignalLogger.setPath("/media/sda1/ctre-logs/");

    shooterTopMain.getConfigurator().apply(ShooterConstants.GetShooterConfiguration());
    shooterBottomFollower.setControl(new StrictFollower(shooterTopMain.getDeviceID()));

    
    shootVelocity = ntTable.getDoubleTopic("shoot_velocity").getEntry(0);
    shootVoltage = ntTable.getDoubleTopic("shoot_voltage").getEntry(0);
    rotateVoltage = ntTable.getDoubleTopic("rotate_angle").getEntry(0);
    rotateTarget = ntTable.getDoubleTopic("rotate_target").getEntry(0);
    encoderValue = ntTable.getDoubleTopic("encoder_value").getEntry(0);
    encoderAngleWithoutOffset = ntTable.getDoubleTopic("encoder_angle_no_offset").getEntry(0);
    encoderAngle = ntTable.getDoubleTopic("encoder_angle").getEntry(0);
    
    
    seedNetworkTables();
  }

  @Override
  public void setupShuffleboard() {
    tab.add("shooter rotate encoder", shooterRotateEncoder);
    tab.add("shooter top main", shooterTopMain);
    tab.add("shooter botom follower", shooterBottomFollower);
    tab.add("shoote rotate motor", shooterRotate);

    tab.add("rotation pid controller", rotatePID);
  }

  @Override
  public void writePeriodicOutputs() {
    readEncoderAngle();
    readEncoderAngleWithoutOffset();
    readEncoderValue();
  }

  @Override
  public void setupTestCommands() {
    
  }

  @Override
  public void seedNetworkTables() {
    setRotateTarget(0);
    setRotateVoltage(0);
    setShootVelocity(0);
    setShootVoltage(0);
    getRotateAngle();
    getRotateTarget();
    getShootVelocity();
    getShootVoltage();
  }

  public void setPIDTarget(double target) {
    setRotateTarget(target);

    rotatePID.setSetpoint(target);
  }

  public boolean pidAtSetpoint() {
    return rotatePID.atSetpoint();
  }

  public void rotateShooterPID() {
    double output = rotatePID.calculate(getEncoderAngle(), getRotateTarget());
    setRotateVoltage(output);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    writePeriodicOutputs();
  }

  // GETTERS

  public double getShootVelocity() {
    return shootVelocity.get();
  }

  public double getShootVoltage() {
    return shootVoltage.get();
  }

  public double getRotateAngle() {
    return rotateVoltage.get();
  }

  public double getRotateTarget() {
    return rotateTarget.get();
  }

  public double getEncoderValue() {
    return encoderValue.get();
  }

  public double getEncoderAngleWithoutOffset() {
    return encoderAngleWithoutOffset.get();
  }

  public double getEncoderAngle() {
    return encoderAngle.get();
  }

  private final DoubleLogEntry shootVelocityLog = new DoubleLogEntry(log, "/shooter/shoot_velocity");
  private final DoubleLogEntry shootVoltageLog = new DoubleLogEntry(log, "/shooter/shoot_voltage");
  private final DoubleLogEntry rotateVoltageLog = new DoubleLogEntry(log, "/shooter/rotate_angle");
  private final DoubleLogEntry rotateTargetLog = new DoubleLogEntry(log, "/shooter/rotate_target");
  private final DoubleLogEntry encoderValueLog = new DoubleLogEntry(log, "/shooter/encoder_value");
  private final DoubleLogEntry encoderAngleWithoutOffsetLog = new DoubleLogEntry(log,
      "/shooter/encoder_angle_no_offset");
  private final DoubleLogEntry encoderAngleLog = new DoubleLogEntry(log, "/shooter/encoder_angle");

  public void setShootVelocity(double velocity) {
    shootVelocity.set(velocity);
    shootVelocityLog.append(velocity);

    VelocityDutyCycle velocityOut = new VelocityDutyCycle(0);
    shooterTopMain.setControl(velocityOut.withVelocity(velocity));
  }

  public void setShootVoltage(double voltage) {
    shootVoltage.set(voltage);
    shootVoltageLog.append(voltage);

    DutyCycleOut voltageOut = new DutyCycleOut(0);
    shooterTopMain.setControl(voltageOut.withOutput(voltage));
  }

  public void setRotateVoltage(double voltage) {
    rotateVoltage.set(voltage);
    rotateVoltageLog.append(voltage);

    DutyCycleOut angleVolts = new DutyCycleOut(0);
    shooterRotate.setControl(angleVolts.withOutput(voltage));
  }

  public void setRotateTarget(double target) {
    rotateTarget.set(target);
    rotateTargetLog.append(target);
  }

  public void readEncoderValue() {
    encoderValue.set(shooterRotateEncoder.get());
    encoderValueLog.append(shooterRotateEncoder.get());
  }

  public void readEncoderAngleWithoutOffset() {
    encoderAngleWithoutOffset.set(shooterRotateEncoder.get() * 360);
    encoderAngleWithoutOffsetLog.append(shooterRotate.get() * 360);
  }

  public void readEncoderAngle() {
    encoderAngle.set((shooterRotateEncoder.get() - currentArmRotationSupplier.getAsDouble()) * 360);
    encoderAngleLog.append((shooterRotateEncoder.get() - currentArmRotationSupplier.getAsDouble()) * 360);
  }
}
