package anightdazingzoroark.prift.server.item;

import anightdazingzoroark.prift.api.projectile.IProjectile;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RiftTranqBomb extends RiftThrowableItem {
    public RiftTranqBomb() {
        super();
        this.setMaxStackSize(16);
    }

    @Override
    @NotNull
    public ProjectileBuilder getProjectileBuilder() {
        return new ProjectileBuilder().setName("tranq_bomb")
                .setUseCubeModel().setHasParticleTrail()
                .registerBooleanValue("HasHitHitbox", false)
                .setOnImpactFromPlayerEffect((player, projectile, hitEntity, hitPos) -> {
                    this.onImpactEffect(player, projectile, hitPos);
                })
                .setOnImpactForDispenserEffect((projectile, hitEntity, hitPos) -> {
                    this.onImpactEffect(null, projectile, hitPos);
                })
                .setImpactSoundEvent(RiftSounds.getSound("tranq_bomb.impact"));
    }

    private void onImpactEffect(@Nullable EntityPlayer player, @NotNull IProjectile projectile, @NotNull Vec3d hitPos) {
        //make particle
        RiftLibParticleHelper.createParticle(
                "prift:tranq_bomb_impact",
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
            this.onHitEntity(player, entity, projectile, false);
        }
    }

    private void onHitEntity(@Nullable EntityPlayer player, @NotNull Entity entity, @NotNull IProjectile projectile, boolean fromHitbox) {
        boolean canHitFromHitbox = !fromHitbox || !(boolean) projectile.getProperty("HasHitHitbox").getValue();

        if (entity instanceof MultiPartEntityPart entityPart) {
            this.onHitEntity(player, (Entity) entityPart.parent, projectile, true);
        }
        else if (entity instanceof RiftCreature hitCreature && !hitCreature.isTamed() && canHitFromHitbox) {
            int tiredness = projectile.getEntityWorld().rand.nextInt(20, 36);
            hitCreature.addTiredness(Math.max(1, Math.round(tiredness * hitCreature.getTamingEffectivenessForLevel())));
        }
        else if (RiftUtil.entityInTargetGroup(entity, "animal") && canHitFromHitbox) {
            entity.attackEntityFrom(DamageSource.causeThrownDamage((Entity) projectile, player), 10f);
        }

        //extra lock
        if (fromHitbox && !(boolean) projectile.getProperty("HasHitHitbox").getValue()) projectile.setProperty("HasHitHitbox", true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag tooltipFlag) {
        tooltip.add(TextFormatting.GRAY + I18n.format("item.tranq_bomb.tooltip"));
    }
}
