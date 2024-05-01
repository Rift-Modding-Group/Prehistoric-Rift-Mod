package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public interface IGrabber {
    EntityLivingBase getGrabVictim();
    void setGrabVictim(EntityLivingBase entity);
    void manageGrabVictim();
    Vec3d grabLocation();
}
