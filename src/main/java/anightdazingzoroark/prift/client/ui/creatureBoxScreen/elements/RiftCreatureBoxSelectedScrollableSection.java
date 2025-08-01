package anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements;

import anightdazingzoroark.prift.client.ui.elements.RiftGuiScrollableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiScrollableSectionContents;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

public class RiftCreatureBoxSelectedScrollableSection extends RiftGuiScrollableSection {
    private NBTTagCompound creatureNBT;

    public RiftCreatureBoxSelectedScrollableSection(int guiWidth, int guiHeight, FontRenderer fontRenderer, Minecraft minecraft) {
        super(100, 114, guiWidth, guiHeight, 110, 35, fontRenderer, minecraft);
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

        if (this.creatureNBT != null && !this.creatureNBT.isEmpty()) {
            RiftCreatureType creatureType = RiftCreatureType.values()[this.creatureNBT.getByte("CreatureType")];
            int creatureLevel = this.creatureNBT.getInteger("Level");

            //creature name and level
            String creatureName = this.creatureNBT.hasKey("CustomName") && !this.creatureNBT.getString("CustomName").isEmpty() ? this.creatureNBT.getString("CustomName") : creatureType.getTranslatedName();
            String finalCreatureName = I18n.format("journal.party_member.name", creatureName, creatureLevel);
            toReturn.addTextElement(new RiftGuiScrollableSectionContents.TextElement()
                    .setContents(finalCreatureName)
                    .setScale(0.5f)
                    .setBottomSpace(-7)
            );

            //creature health
            float health = this.creatureNBT.getFloat("Health");
            float maxHealth = health;
            for (NBTBase nbtBase: this.creatureNBT.getTagList("Attributes", 10).tagList) {
                if (nbtBase instanceof NBTTagCompound) {
                    NBTTagCompound tagCompound = (NBTTagCompound) nbtBase;

                    if (!tagCompound.hasKey("Name") || !tagCompound.getString("Name").equals("generic.maxHealth")) continue;

                    maxHealth = (float) tagCompound.getDouble("Base");
                }
            }
            toReturn.addProgressBarElement(new RiftGuiScrollableSectionContents.ProgressBarElement()
                    .setHeaderText(I18n.format("tametrait.health", Math.round(health), maxHealth))
                    .setHeaderScale(0.5f)
                    .setPercentage(MathHelper.clamp(health / maxHealth, 0, 1))
                    .setColors(0xff0000, 0x868686)
                    .setWidth(96)
                    .setFactor(0.1f)
            );

            //creature energy
            float energy = this.creatureNBT.getInteger("Energy");
            float maxEnergy = RiftConfigHandler.getConfig(creatureType).stats.maxEnergy;
            toReturn.addProgressBarElement(new RiftGuiScrollableSectionContents.ProgressBarElement()
                    .setHeaderText(I18n.format("tametrait.energy", energy, maxEnergy))
                    .setHeaderScale(0.5f)
                    .setPercentage(MathHelper.clamp(energy / maxEnergy, 0, 1))
                    .setColors(0xffff00, 0x868686)
                    .setWidth(96)
                    .setFactor(0.1f)
            );

            //creature xp
            float xp = this.creatureNBT.getInteger("XP");
            float maxXP = creatureType.getMaxXP(this.creatureNBT.getInteger("Level"));
            toReturn.addProgressBarElement(new RiftGuiScrollableSectionContents.ProgressBarElement()
                    .setHeaderText(I18n.format("tametrait.xp", xp, maxXP))
                    .setHeaderScale(0.5f)
                    .setPercentage(MathHelper.clamp(xp / maxXP, 0, 1))
                    .setColors(0x98d06b, 0x868686)
                    .setWidth(96)
                    .setFactor(0.1f)
            );

            //more info button
            toReturn.addButtonElement(new RiftGuiScrollableSectionContents.ButtonElement()
                    .setId("moreInfo")
                    .setName(I18n.format("creature_box.more_info"))
                    .setBottomSpaceSize(7)
                    .setSize(100, 20)
            );

            //release button
            toReturn.addButtonElement(new RiftGuiScrollableSectionContents.ButtonElement()
                    .setId("release")
                    .setName(I18n.format("creature_box.release"))
                    .setSize(100, 20)
            );

        }
        return toReturn;
    }
}
