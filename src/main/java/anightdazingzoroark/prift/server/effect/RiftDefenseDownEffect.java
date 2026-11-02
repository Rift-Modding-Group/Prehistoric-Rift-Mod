package anightdazingzoroark.prift.server.effect;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class RiftDefenseDownEffect extends Potion {
    @NotNull
    private static final ResourceLocation iconTexture = new ResourceLocation(RiftInitialize.MODID, "textures/ui/effect_icons.png");

    protected RiftDefenseDownEffect() {
        super(true, 0xAD431F);
        this.setPotionName("effect.defense_down");
        this.setRegistryName(RiftInitialize.MODID, "defense_down");
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(@NotNull PotionEffect effect, @NotNull Gui gui, int x, int y, float z) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(iconTexture);
        Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 18, 18, 18, 54, 54);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUDEffect(@NotNull PotionEffect effect, @NotNull Gui gui, int x, int y, float z, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(iconTexture);
        Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 18, 18, 18, 54, 54);
    }
}
