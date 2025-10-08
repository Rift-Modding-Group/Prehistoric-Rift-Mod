package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectileEntity;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class RiftCreatureProjectileModel extends AnimatedGeoModel<RiftCreatureProjectileEntity> {
    @Override
    public ResourceLocation getModelLocation(RiftCreatureProjectileEntity riftCreatureProjectile) {
        if (riftCreatureProjectile.hasNoModel()) {
            //this is a placeholder
            //soon i will make this just return null
            //and a null model location will mean the entity will be invisible
            return new ResourceLocation(RiftInitialize.MODID, "geo/flat_projectile.model.json");
        }
        else if (riftCreatureProjectile.hasFlatModel()) {
            return new ResourceLocation(RiftInitialize.MODID, "geo/flat_projectile.model.json");
        }
        else {
            String name = riftCreatureProjectile.getProjectileBuilder().projectileEnum.name().toLowerCase();
            return new ResourceLocation(RiftInitialize.MODID, "geo/"+name+".model.json");
        }
    }

    @Override
    public ResourceLocation getTextureLocation(RiftCreatureProjectileEntity riftCreatureProjectile) {
        if (riftCreatureProjectile.hasNoModel()) {
            //this is a placeholder
            //soon i will make this just return null
            //and a null model location will mean the entity will be invisible
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/projectiles/mudball.png");
        }
        else if (riftCreatureProjectile.getHasVariants()) {
            String name = riftCreatureProjectile.getProjectileBuilder().projectileEnum.name().toLowerCase();
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/projectiles/"+name+"_"+(riftCreatureProjectile.getVariant() + 1)+".png");
        }
        else {
            String name = riftCreatureProjectile.getProjectileBuilder().projectileEnum.name().toLowerCase();
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/projectiles/"+name+".png");
        }
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftCreatureProjectileEntity riftCreatureProjectile) {
        if (riftCreatureProjectile.getHasAnimation()) {
            return new ResourceLocation(RiftInitialize.MODID, "animations/creature_projectile.animation.json");
        }
        return null;
    }
}
