package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

public class RiftPartyMemScrollableSection extends RiftGuiScrollableSection {
    private NBTTagCompound creatureNBT;

    public RiftPartyMemScrollableSection(int guiWidth, int guiHeight, FontRenderer fontRenderer, Minecraft minecraft) {
        super(120, 124, guiWidth, guiHeight, 127, -13, fontRenderer, minecraft);
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

            //species name
            toReturn.addTextElement(new RiftGuiScrollableSectionContents.TextElement()
                    .setContents(I18n.format("tametrait.species", creatureType.getTranslatedName()))
                    .setScale(0.5f)
                    .setBottomSpace(-7)
            );

            //health bar
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
                    .setWidth(100)
            );

            //energy bar
            float energy = this.creatureNBT.getInteger("Energy");
            float maxEnergy = RiftConfigHandler.getConfig(creatureType).stats.maxEnergy;
            toReturn.addProgressBarElement(new RiftGuiScrollableSectionContents.ProgressBarElement()
                    .setHeaderText(I18n.format("tametrait.energy", energy, maxEnergy))
                    .setHeaderScale(0.5f)
                    .setPercentage(MathHelper.clamp(energy / maxEnergy, 0, 1))
                    .setColors(0xffff00, 0x868686)
                    .setWidth(100)
            );

            //exp bar
            float xp = this.creatureNBT.getInteger("XP");
            float maxXP = creatureType.getMaxXP(this.creatureNBT.getInteger("Level"));
            toReturn.addProgressBarElement(new RiftGuiScrollableSectionContents.ProgressBarElement()
                    .setHeaderText(I18n.format("tametrait.xp", xp, maxXP))
                    .setHeaderScale(0.5f)
                    .setPercentage(MathHelper.clamp(xp / maxXP, 0, 1))
                    .setColors(0x98d06b, 0x868686)
                    .setWidth(100)
            );

            //soon more info like personality and all that stuff would be added
        }

        return toReturn;
    }

    private void setOwnerName() {}
}
