package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.NBTTagCompound;

public abstract class RiftUISectionNBTUser extends RiftLibUISection {
    protected NBTTagCompound nbtTagCompound;

    public RiftUISectionNBTUser(String id, NBTTagCompound nbtTagCompound, int guiWidth, int guiHeight, int width, int height, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        super(id, guiWidth, guiHeight, width, height, xPos, yPos, fontRenderer, minecraft);
        this.nbtTagCompound = nbtTagCompound;
    }

    public void setNBTTagCompound(NBTTagCompound tagCompound) {
        this.nbtTagCompound = tagCompound;
    }

    public NBTTagCompound getNBTTagCompound() {
        return this.nbtTagCompound;
    }
}
