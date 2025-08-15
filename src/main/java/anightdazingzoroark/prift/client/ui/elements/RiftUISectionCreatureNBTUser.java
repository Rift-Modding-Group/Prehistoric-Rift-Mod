package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.NBTTagCompound;

public abstract class RiftUISectionCreatureNBTUser extends RiftLibUISection {
    protected CreatureNBT nbtTagCompound;

    public RiftUISectionCreatureNBTUser(String id, CreatureNBT nbtTagCompound, int guiWidth, int guiHeight, int width, int height, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        super(id, guiWidth, guiHeight, width, height, xPos, yPos, fontRenderer, minecraft);
        this.nbtTagCompound = nbtTagCompound;
    }

    public void setNBTTagCompound(CreatureNBT tagCompound) {
        this.nbtTagCompound = tagCompound;
    }

    public CreatureNBT getNBTTagCompound() {
        return this.nbtTagCompound;
    }
}
