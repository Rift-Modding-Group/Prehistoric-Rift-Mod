package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftPartyMemButton;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RiftPartyScreen extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/party_background.png");
    private final List<RiftPartyMemButton> partyMemButtons = new ArrayList<>();
    private RiftCreature creatureToDraw;
    protected final int xSize = 373;
    protected final int ySize = 157;

    @Override
    public void initGui() {
        super.initGui();

        //create party buttons
        this.partyMemButtons.clear();
        List<NBTTagCompound> playerPartyNBT = new ArrayList(PlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player));
        for (int x = 0; x < playerPartyNBT.size(); x++) {
            int xOffset = -154 + (x % 2) * 60;
            int yOffset = -41 + (x / 2) * 40;
            RiftPartyMemButton partyMemButton = new RiftPartyMemButton(playerPartyNBT.get(x), this.width, this.height, xOffset, yOffset, this.fontRenderer, this.mc);
            this.partyMemButtons.add(partyMemButton);
        }

        //by default selected button is the first one
        PlayerTamedCreaturesHelper.setLastSelected(this.mc.player, 0);

        //set creature to draw
        this.creatureToDraw = PlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, playerPartyNBT.get(0));
    }

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

        //draw party buttons
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButton partyMemButton = this.partyMemButtons.get(x);
            partyMemButton.drawSection(mouseX, mouseY);
            partyMemButton.setSelected(x == PlayerTamedCreaturesHelper.getLastSelected(this.mc.player));
        }

        //draw selected creature
        GlStateManager.pushMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.translate(this.width / 2f, this.height / 2f, 180f);
        GlStateManager.rotate(180, 1f, 0f, 0f);
        GlStateManager.rotate(150, 0f, 1f, 0f);
        GlStateManager.scale(20f, 20f, 20f);
        this.mc.getRenderManager().renderEntity(this.creatureToDraw, 0.0D, 0.0D, 0.0D, 0.0F, 0F, false);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //manage party button clicking
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButton partyMemButton = this.partyMemButtons.get(x);
            if (partyMemButton.isHovered(mouseX, mouseY)) {
                PlayerTamedCreaturesHelper.setLastSelected(this.mc.player, x);
                this.creatureToDraw = partyMemButton.getCreatureFromNBT();
            }
        }
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, 400F, 300F);
    }
}
