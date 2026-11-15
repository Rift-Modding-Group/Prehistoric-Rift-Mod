package anightdazingzoroark.prift.server.effect;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class RiftParalysisEffect extends Potion {
    public static final int ACTION_INTERVAL_TICKS = 5;
    public static final double SPEED_MULTIPLIER = 1D / ACTION_INTERVAL_TICKS;

    @NotNull
    private static final ResourceLocation iconTexture = new ResourceLocation(RiftInitialize.MODID, "textures/ui/effect_icons.png");

    protected RiftParalysisEffect() {
        super(true, 0xFFFF00);
        this.setPotionName("effect.paralysis");
        this.setRegistryName(RiftInitialize.MODID, "paralysis");
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.MOVEMENT_SPEED,
                "e7c20e2e-b8c5-4a32-9f82-65ac5f7dcb00",
                SPEED_MULTIPLIER - 1D, 2
        );
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.ATTACK_SPEED,
                "43ba417e-8a40-4ccd-ad05-f870ade97f23",
                SPEED_MULTIPLIER - 1D, 2
        );
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public double getAttributeModifierAmount(int amplifier, @NotNull AttributeModifier modifier) {
        return modifier.getAmount();
    }

    public static double getSpeedMultiplier(@NotNull EntityLivingBase entity) {
        return entity.isPotionActive(RiftEffects.PARALYSIS) ? SPEED_MULTIPLIER : 1D;
    }

    @Override
    public void performEffect(@NotNull EntityLivingBase entity, int amplifier) {
        if (entity.world.isRemote || entity instanceof RiftCreature
                || !(entity instanceof EntityLiving mob) || entity.ticksExisted % ACTION_INTERVAL_TICKS == 0) return;

        //Vanilla attack goals count down independently of the attack-speed attribute.
        for (EntityAITasks.EntityAITaskEntry entry : mob.tasks.executingTaskEntries) {
            switch (entry.action) {
                case EntityAIAttackMelee melee when melee.attackTick > 0 -> melee.attackTick++;
                case EntityAIAttackRanged ranged when ranged.rangedAttackTime > 0 -> ranged.rangedAttackTime++;
                case EntityAIAttackRangedBow<?> bow when bow.attackTime > 0 -> bow.attackTime++;
                default -> {}
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(@NotNull PotionEffect effect, @NotNull Gui gui, int x, int y, float z) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(iconTexture);
        Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 36, 0, 18, 18, 54, 54);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUDEffect(@NotNull PotionEffect effect, @NotNull Gui gui, int x, int y, float z, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(iconTexture);
        Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 36, 0, 18, 18, 54, 54);
    }
}
