package anightdazingzoroark.prift.api.projectile;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.api.util.QuadConsumer;
import anightdazingzoroark.prift.api.util.TriConsumer;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.BooleanPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.IntegerPropertyValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ProjectileBuilder {
    //important
    private String name = "";

    //can be left alone
    @Nullable
    private QuadConsumer<ICreature, IProjectile, EntityLivingBase, Vec3d> onImpactFromCreatureEffect;
    @Nullable
    private QuadConsumer<EntityPlayer, IProjectile, EntityLivingBase, Vec3d> onImpactFromPlayerEffect;
    @Nullable
    private TriConsumer<IProjectile, EntityLivingBase, Vec3d> onImpactFromDispenserEffect;
    @Nullable
    private Consumer<IProjectile> updateEffect;
    private boolean stayAfterImpact;
    private boolean rotateAlongPitch;
    private boolean useCubeModel;
    private boolean hasParticleTrail;
    @Nullable
    private SoundEvent impactSoundEvent;
    @Nullable
    private Map<String, AbstractPropertyValue<?>> propertyValueMap;

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
     * Set what happens when the projectile hits and is launched by an entity
     * */
    public ProjectileBuilder setOnImpactFromCreatureEffect(@NotNull QuadConsumer<ICreature, IProjectile, EntityLivingBase, Vec3d> onImpactFromCreatureEffect) {
        this.onImpactFromCreatureEffect = onImpactFromCreatureEffect;
        return this;
    }

    @Nullable
    public QuadConsumer<ICreature, IProjectile, EntityLivingBase, Vec3d> getOnImpactFromCreatureEffect() {
        return this.onImpactFromCreatureEffect;
    }

    /**
     * Set what happens when the projectile hits and is launched by a player
     * */
    public ProjectileBuilder setOnImpactFromPlayerEffect(@NotNull QuadConsumer<EntityPlayer, IProjectile, EntityLivingBase, Vec3d> onImpactFromPlayerEffect) {
        this.onImpactFromPlayerEffect = onImpactFromPlayerEffect;
        return this;
    }

    @Nullable
    public QuadConsumer<EntityPlayer, IProjectile, EntityLivingBase, Vec3d> getOnImpactFromPlayerEffect() {
        return this.onImpactFromPlayerEffect;
    }

    /**
     * Set what happens when the projectile hits and is launched by a dispenser
     * */
    public ProjectileBuilder setOnImpactForDispenserEffect(@NotNull TriConsumer<IProjectile, EntityLivingBase, Vec3d> onImpactFromDispenserEffect) {
        this.onImpactFromDispenserEffect = onImpactFromDispenserEffect;
        return this;
    }

    @Nullable
    public TriConsumer<IProjectile, EntityLivingBase, Vec3d> getOnImpactFromDispenserEffect() {
        return this.onImpactFromDispenserEffect;
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

    public ProjectileBuilder setImpactSoundEvent(@Nullable SoundEvent soundEvent) {
        this.impactSoundEvent = soundEvent;
        return this;
    }

    @Nullable
    public SoundEvent getImpactSoundEvent() {
        return this.impactSoundEvent;
    }

    //-----for additional values to this particle. they do sync from server to client, but they do not persist.-----
    public ProjectileBuilder registerIntegerValue(@NotNull String name, int initVal) {
        if (this.propertyValueMap == null) this.propertyValueMap = new HashMap<>();
        this.propertyValueMap.put(name, new IntegerPropertyValue(name, initVal));
        return this;
    }

    public ProjectileBuilder registerBooleanValue(@NotNull String name, boolean initVal) {
        if (this.propertyValueMap == null) this.propertyValueMap = new HashMap<>();
        this.propertyValueMap.put(name, new BooleanPropertyValue(name, initVal));
        return this;
    }

    @Nullable
    public Map<String, AbstractPropertyValue<?>> getPropertyValueMap() {
        return this.propertyValueMap;
    }
}
