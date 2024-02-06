package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.vision.Components.Camera;
import frc.robot.subsystems.vision.Constants.Variables;

public class VisionSubsystem extends SubsystemBase {
    private final Camera camera;

    public VisionSubsystem() {
        camera = new Camera();
        System.out.println("VisionSubsystem initiated");
    }


    public void periodic() {
        SmartDashboard.putBoolean("Target Locked", camera.checkNote());

    }

    public Translation2d findNote() {
        double xAxis = Variables.BackCam.tx;
        double yAxis = Variables.BackCam.ty;

        return new Translation2d(xAxis, yAxis);
    }

}