package anightdazingzoroark.prift.client.ui.creatureBoxInfoScreen.elements;

import anightdazingzoroark.prift.client.ui.elements.RiftGuiScrollableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiScrollableSectionContents;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

public class RiftCreatureBoxInfoButtons extends RiftGuiScrollableSection {
    private NBTTagCompound creatureNBT;

    public RiftCreatureBoxInfoButtons(int guiWidth, int guiHeight, FontRenderer fontRenderer, Minecraft minecraft) {
        super(100, 60, guiWidth, guiHeight, -62, 48, fontRenderer, minecraft);
    }

    public void setCreatureNBT(NBTTagCompound tagCompound) {
        this.creatureNBT = tagCompound;
    }

    public NBTTagCompound getCreatureNBT() {
        return this.creatureNBT;
    }

    @Override
    public RiftGuiScrollableSectionContents defineSectionContents() {
        RiftGuiScrollableSectionContents toReturn = new RiftGuiScrollableSectionContents();

        //show creature name and level
        if (this.creatureNBT != null && !this.creatureNBT.isEmpty()) {
            RiftCreatureType creatureType = RiftCreatureType.values()[this.creatureNBT.getByte("CreatureType")];
            String partyMemName = (this.creatureNBT.hasKey("CustomName") && !this.creatureNBT.getString("CustomName").isEmpty()) ? this.creatureNBT.getString("CustomName") : creatureType.getTranslatedName();
            toReturn.addTextElement(new RiftGuiScrollableSectionContents.TextElement()
                    .setContents(I18n.format("journal.party_member.name", partyMemName, this.creatureNBT.getInteger("Level")))
                    .setScale(0.5f)
                    .setBottomSpace(-9)
                    .setTextCentered()
                    .setSingleLine()
            );
        }

        //back to box button
        toReturn.addButtonElement(new RiftGuiScrollableSectionContents.ButtonElement()
                .setSize(100, 20)
                .setName(I18n.format("creature_box.back_to_box"))
                .setId("backToBox")
                .setBottomSpaceSize(7)
        );

        //release button
        toReturn.addButtonElement(new RiftGuiScrollableSectionContents.ButtonElement()
                .setId("release")
                .setName(I18n.format("creature_box.release"))
                .setSize(100, 20)
        );

        return toReturn;
    }
}
