package anightdazingzoroark.prift.client.ui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

public class RiftPopupFromPlayerPartyChangeName extends RiftGuiScrollableSection {
    public RiftPopupFromPlayerPartyChangeName(int guiWidth, int guiHeight, FontRenderer fontRenderer, Minecraft minecraft) {
        super(176, 84, guiWidth, guiHeight, 0, 10, fontRenderer, minecraft);
    }

    @Override
    public RiftGuiScrollableSectionContents defineSectionContents() {
        RiftGuiScrollableSectionContents toReturn = new RiftGuiScrollableSectionContents();

        toReturn.addTextElement(new RiftGuiScrollableSectionContents.TextElement()
                .setContents(I18n.format("radial.popup_choice.change_name"))
                .setBottomSpace(-9)
                .setTextCentered()
        );

        toReturn.addTextBoxElement(new RiftGuiScrollableSectionContents.TextBoxElement()
                .setWidth(120)
                .setId("NewName")
        );

        toReturn.addButtonRowElement(new RiftGuiScrollableSectionContents.ButtonRowElement()
                .addButtonElement(new RiftGuiScrollableSectionContents.ButtonElement()
                        .setSize(60, 20)
                        .setName(I18n.format("radial.popup_button.confirm"))
                        .setId("SetNewName")
                )
                .addButtonElement(new RiftGuiScrollableSectionContents.ButtonElement()
                        .setSize(60, 20)
                        .setName(I18n.format("radial.popup_button.cancel"))
                        .setId("Exit")
                )
        );

        return toReturn;
    }
}
