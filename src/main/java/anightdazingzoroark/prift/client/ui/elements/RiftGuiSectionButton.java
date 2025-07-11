package anightdazingzoroark.prift.client.ui.elements;

import net.minecraft.client.gui.GuiButton;

public class RiftGuiSectionButton extends GuiButton {
    public final String buttonId;

    public RiftGuiSectionButton(String buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(0, x, y, widthIn, heightIn, buttonText);
        this.buttonId = buttonId;
    }
}
