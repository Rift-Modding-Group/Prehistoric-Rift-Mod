package anightdazingzoroark.prift.api.projectile;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.util.QuadConsumer;
import anightdazingzoroark.prift.api.util.TriConsumer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ProjectileBuilder {
    //important
    private String name = "";

    //can be left alone
    @Nullable
    private QuadConsumer<ICreature, IProjectile, EntityLivingBase, Vec3d> onImpactEffect;
    @Nullable
    private Consumer<IProjectile> updateEffect;
    private boolean stayAfterImpact;
    private boolean rotateAlongPitch;
    private boolean useCubeModel;
    private boolean hasParticleTrail;

    /**
     * Set the name of the projectile, is to be required
     * */
    public ProjectileBuilder setName(@NotNull String name) {
        this.name = name;
        return this;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    /**
     * Set what happens when the projectile hits
     * */
    public ProjectileBuilder setOnImpactEffect(@NotNull QuadConsumer<ICreature, IProjectile, EntityLivingBase, Vec3d> onImpactEffect) {
        this.onImpactEffect = onImpactEffect;
        return this;
    }

    @Nullable
    public QuadConsumer<ICreature, IProjectile, EntityLivingBase, Vec3d> getOnImpactEffect() {
        return this.onImpactEffect;
    }

    /**
     * Set what happens every tick while the projectile exists in the world
     * note that it will only be executed on server
     * */
    public ProjectileBuilder setUpdateEffect(@NotNull Consumer<IProjectile> updateEffect) {
        this.updateEffect = updateEffect;
        return this;
    }


    @Nullable
    public Consumer<IProjectile> getUpdateEffect() {
        return this.updateEffect;
    }

    /**
     * By default, projectiles despawn after hitting, but this can be used to make them
     * linger for a while upon making contact
     * */
    public ProjectileBuilder setStayAfterImpact() {
        this.stayAfterImpact = true;
        return this;
    }

    public boolean getStayAfterImpact() {
        return this.stayAfterImpact;
    }

    /**
     * Makes the projectile rotate along pitch based on the trajectory its goin in
     * */
    public ProjectileBuilder setRotateAlongPitch() {
        this.rotateAlongPitch = true;
        return this;
    }

    public boolean getRotateAlongPitch() {
        return this.rotateAlongPitch;
    }

    /**
     * Makes the projectile's model be a default 8 x 8 x 8 cube
     * Textures still have to be supplied by you
     * */
    public ProjectileBuilder setUseCubeModel() {
        this.useCubeModel = true;
        return this;
    }

    public boolean getUseCubeModel() {
        return this.useCubeModel;
    }

    /**
     * Makes the projectile leave a trail of particles
     * Note that you have to define it yourself by making a particle file of the same name in the resources folder
     * */
    public ProjectileBuilder setHasParticleTrail() {
        this.hasParticleTrail = true;
        return this;
    }

    public boolean getHasParticleTrail() {
        return this.hasParticleTrail;
    }
}
