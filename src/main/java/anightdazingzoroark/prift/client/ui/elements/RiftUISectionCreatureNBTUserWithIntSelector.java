package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public abstract class RiftUISectionCreatureNBTUserWithIntSelector extends RiftUISectionCreatureNBTUser {
    protected int selector = -1;

    public RiftUISectionCreatureNBTUserWithIntSelector(String id, CreatureNBT nbtTagCompound, int guiWidth, int guiHeight, int width, int height, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        super(id, nbtTagCompound, guiWidth, guiHeight, width, height, xPos, yPos, fontRenderer, minecraft);
    }

    public void setSelector(int value) {
        this.selector = value;
    }
}
