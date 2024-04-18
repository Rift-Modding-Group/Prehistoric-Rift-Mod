package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.server.enums.CreatureCategory;
import net.minecraft.client.gui.GuiButton;

public class RiftGuiJournalButton extends GuiButton {
    private final String triggerString;

    public RiftGuiJournalButton(String triggerString,int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.triggerString = triggerString;
    }

    public String getTriggerString() {
        return this.triggerString;
    }
}
