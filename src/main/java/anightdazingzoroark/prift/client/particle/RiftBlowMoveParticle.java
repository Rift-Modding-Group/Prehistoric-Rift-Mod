package anightdazingzoroark.prift.client.particle;

import anightdazingzoroark.prift.RiftUtil;
import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class RiftBlowMoveParticle extends ParticleCloud {
    public RiftBlowMoveParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double motX, double motY, double motZ) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, motX, motY, motZ);
        this.particleRed = 1f;
        this.particleGreen = 1f;
        this.particleBlue =  1f;
        this.particleMaxAge = RiftUtil.randomInRange(5, 40);

        //assume that motX, motY, and motZ have the lookVec
        Vec3d direction = this.getRandomConeDirection(motX, motY, motZ, 20.0, this.rand); // 20 degrees cone
        double speed = 0.2 + this.rand.nextDouble() * 0.1;
        this.motionX = direction.scale(speed).x;
        this.motionY = direction.scale(speed).y;
        this.motionZ = direction.scale(speed).z;
    }


    private Vec3d getRandomConeDirection(double motX, double motY, double motZ, double angleDegrees, Random rand) {
        Vec3d forward = new Vec3d(motX, motY, motZ).normalize();

        // Convert angle to radians
        double angleRadians = Math.toRadians(angleDegrees);

        // Generate random angles for spherical coordinates
        double theta = 2 * Math.PI * rand.nextDouble(); // rotation around the forward axis
        double phi = angleRadians * rand.nextDouble();  // deviation from the forward direction

        // Convert spherical to Cartesian
        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);

        // This gives a direction vector in the cone around the Z axis, so now rotate it to align with the `forward` vector
        Vec3d base = new Vec3d(x, y, z);

        // Create rotation matrix aligning Z axis to `forward`
        // If forward is already close to Z, just return
        Vec3d axis = new Vec3d(0, 0, 1).crossProduct(forward);
        double angle = Math.acos(forward.dotProduct(new Vec3d(0, 0, 1)));
        if (axis.lengthSquared() < 1e-6) return forward;

        axis = axis.normalize();
        return rotateVectorAroundAxis(base, axis, angle);
    }


    private Vec3d rotateVectorAroundAxis(Vec3d vec, Vec3d axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = axis.dotProduct(vec);
        return vec.scale(cos)
                .add(axis.crossProduct(vec).scale(sin))
                .add(axis.scale(dot * (1 - cos)));
    }
}