package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftPartyMemButton;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RiftPartyScreen extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/party_background.png");
    protected final int xSize = 373;
    protected final int ySize = 157;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc != null && this.mc.world != null) this.drawDefaultBackground();
        else return;

        //draw screen
        this.drawGuiContainerBackgroundLayer();

        //draw party label
        String partyLabel = "Party";
        int partyLabelX = (this.width - this.fontRenderer.getStringWidth(partyLabel)) / 2 - 165;
        int partyLabelY = (this.height - this.fontRenderer.FONT_HEIGHT) / 2 - 68;
        this.fontRenderer.drawString(partyLabel, partyLabelX, partyLabelY, 0x000000);

        List<NBTTagCompound> playerPartyNBT = new ArrayList(PlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player));

        //draw party tabs
        for (int x = 0; x < playerPartyNBT.size(); x++) {
            int xOffset = -154 + (x % 2) * 60;
            int yOffset = -41 + (x / 2) * 40;
            RiftPartyMemButton partyMemButton = new RiftPartyMemButton(this.width, this.height, xOffset, yOffset, this.fontRenderer, this.mc);
            partyMemButton.drawSection(mouseX, mouseY);
        }

        //draw selected creature
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, 400F, 300F);
    }

}
