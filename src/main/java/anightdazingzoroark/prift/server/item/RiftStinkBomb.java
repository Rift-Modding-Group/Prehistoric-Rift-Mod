package anightdazingzoroark.prift.server.item;

import anightdazingzoroark.prift.api.projectile.IProjectile;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RiftStinkBomb extends RiftThrowableItem {
    public RiftStinkBomb() {
        super();
        this.setMaxStackSize(16);
    }

    @Override
    @NotNull
    public ProjectileBuilder getProjectileBuilder() {
        return new ProjectileBuilder().setName("stink_bomb")
                .setUseCubeModel().setHasParticleTrail()
                .registerBooleanValue("HasHitHitbox", false)
                .setOnImpactFromPlayerEffect((player, projectile, hitEntity, hitPos) -> {
                    this.onImpactEffect(projectile, hitPos);
                })
                .setOnImpactForDispenserEffect((projectile, hitEntity, hitPos) -> {
                    this.onImpactEffect(projectile, hitPos);
                })
                .setImpactSoundEvent(RiftSounds.getSound("stink_bomb.impact"));
    }

    private void onImpactEffect(@NotNull IProjectile projectile, @NotNull Vec3d hitPos) {
        //make particle
        RiftLibParticleHelper.createParticle(
                "prift:stink_bomb_impact",
                hitPos.x, hitPos.y, hitPos.z,
                0, 0
        );

        //3x3 aoe
        AxisAlignedBB affectedRange = new AxisAlignedBB(
                hitPos.x - 1.5D, hitPos.y - 1.5D, hitPos.z - 1.5D,
                hitPos.x + 1.5D, hitPos.y + 1.5D, hitPos.z + 1.5D
        );
        List<Entity> affectedEntities = projectile.getEntityWorld().getEntitiesWithinAABB(Entity.class, affectedRange);
        for (Entity entity : affectedEntities) {
            if (entity == null) continue;
            this.onHitEntity(entity, projectile, hitPos, false);
        }
    }

    private void onHitEntity(@NotNull Entity entity, @NotNull IProjectile projectile, @NotNull Vec3d hitPos, boolean fromHitbox) {
        boolean canHitFromHitbox = !fromHitbox || !(boolean) projectile.getProperty("HasHitHitbox").getValue();

        if (entity instanceof MultiPartEntityPart entityPart) {
            this.onHitEntity((Entity) entityPart.parent, projectile, hitPos, true);
        }
        else if (entity instanceof EntityLiving entityLiving && (!(entity instanceof EntityTameable tameable) || !tameable.isTamed())
                && !RiftUtil.entityInTargetGroup(entity, "human") && canHitFromHitbox
        ) {
            OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(entityLiving);
            if (otherEntityProperties != null) {
                otherEntityProperties.applyStinkBomb(hitPos);
                entityLiving.setAttackTarget(null);
                entityLiving.setRevengeTarget(null);
                entityLiving.getNavigator().clearPath();
            }
        }

        //extra lock
        if (fromHitbox && !(boolean) projectile.getProperty("HasHitHitbox").getValue()) projectile.setProperty("HasHitHitbox", true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag tooltipFlag) {
        tooltip.add(TextFormatting.GRAY + I18n.format("item.stink_bomb.tooltip"));
    }
}
