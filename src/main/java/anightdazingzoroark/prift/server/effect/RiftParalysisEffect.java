package anightdazingzoroark.prift.server.effect;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class RiftParalysisEffect extends Potion {
    @NotNull
    private static final ResourceLocation iconTexture = new ResourceLocation(RiftInitialize.MODID, "textures/ui/effect_icons.png");

    protected RiftParalysisEffect() {
        super(true, 0xFFFF00);
        this.setPotionName("effect.paralysis");
        this.setRegistryName(RiftInitialize.MODID, "paralysis");
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.MOVEMENT_SPEED,
                "e7c20e2e-b8c5-4a32-9f82-65ac5f7dcb00",
                -1D, 2
        );
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.ATTACK_DAMAGE,
                "43ba417e-8a40-4ccd-ad05-f870ade97f23",
                -1D, 2
        );
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    @Override
    public void applyAttributesModifiersToEntity(
            @NotNull EntityLivingBase entity, @NotNull AbstractAttributeMap attributes, int amplifier
    ) {
        super.applyAttributesModifiersToEntity(entity, attributes, amplifier);
        if (entity instanceof RiftCreature creature) creature.interruptActionsForParalysis();
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
