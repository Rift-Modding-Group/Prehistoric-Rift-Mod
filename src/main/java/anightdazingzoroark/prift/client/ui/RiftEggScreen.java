package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.RiftSac;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.other.RiftEmbryo;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RiftEggScreen extends RiftLibUI {
    private final EggType eggType;
    private final Entity entityToDisplay;
    private final RiftCreature impregnable;

    public RiftEggScreen(NBTTagCompound nbtTagCompound, int x, int y, int z) {
        super(nbtTagCompound, x, y, z);
        this.eggType = nbtTagCompound.hasKey("EggType") ? EggType.values()[nbtTagCompound.getInteger("EggType")] : EggType.EGG;

        this.entityToDisplay = nbtTagCompound.hasKey("HatchableID") ?
                Minecraft.getMinecraft().world.getEntityByID(nbtTagCompound.getInteger("HatchableID")) :
                this.eggType == EggType.EMBRYO ? new RiftEmbryo(Minecraft.getMinecraft().world) : null;

        this.impregnable = nbtTagCompound.hasKey("ImpregnableID") ?
                (RiftCreature) Minecraft.getMinecraft().world.getEntityByID(nbtTagCompound.getInteger("ImpregnableID")) : null;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public List<RiftLibUISection> uiSections() {
        return Arrays.asList(this.createMainSection());
    }

    private RiftLibUISection createMainSection() {
        RiftLibUISection toReturn = new RiftLibUISection("mainSection", this.width, this.height, 166, 156, 0, 0, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.TextElement textElement = new RiftLibUIElement.TextElement();
                textElement.setText(headerText());
                textElement.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                textElement.setBottomSpace(20);
                toReturn.add(textElement);

                RiftLibUIElement.RenderedEntityElement entityElement = new RiftLibUIElement.RenderedEntityElement();
                entityElement.setEntity(entityToDisplay);
                entityElement.setScale(70);
                entityElement.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                entityElement.setBottomSpace(20);
                toReturn.add(entityElement);

                for (String bottomString : bottomTexts()) {
                    RiftLibUIElement.TextElement bottomTextElement = new RiftLibUIElement.TextElement();
                    bottomTextElement.setText(bottomString);
                    bottomTextElement.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                    toReturn.add(bottomTextElement);
                }

                return toReturn;
            }
        };
        toReturn.setContentsCenteredVertically(true);

        return toReturn;
    }

    private String headerText() {
        if (this.eggType == EggType.EGG) {
            RiftEgg riftEgg = (RiftEgg) this.entityToDisplay;
            return I18n.format("item."+riftEgg.getCreatureType().name().toLowerCase()+"_egg.name");
        }
        else if (this.eggType == EggType.SAC) {
            RiftSac riftSac = (RiftSac) this.entityToDisplay;
            return I18n.format("item."+riftSac.getCreatureType().name().toLowerCase()+"_sac.name");
        }
        else if (this.eggType == EggType.EMBRYO) {
            if (this.impregnable != null) {
                return I18n.format("prift.pregnancy.name", this.impregnable.creatureType.friendlyName);
            }
        }
        return "";
    }

    private String[] bottomTexts() {
        if (this.eggType == EggType.EGG) {
            RiftEgg riftEgg = (RiftEgg) this.entityToDisplay;

            if (riftEgg.isInRightTemperature()) {
                int minutes = riftEgg.getHatchTimeMinutes()[0];
                int seconds = riftEgg.getHatchTimeMinutes()[1];
                String minutesString = (minutes < 10 ? "0" : "")+minutes;
                String secondsString = (seconds < 10 ? "0" : "")+seconds;
                String timeString = minutesString+":"+secondsString;

                return new String[]{I18n.format("prift.egg.remaining_hatch_time"), timeString};
            }
            else {
                if (riftEgg.getTemperature().getTempStrength() > riftEgg.getCreatureType().getEggTemperature().getTempStrength()) {
                    return new String[]{I18n.format("prift.egg.too_warm")};
                }
                else {
                    return new String[]{I18n.format("prift.egg.too_cold")};
                }
            }
        }
        else if (this.eggType == EggType.SAC) {
            RiftSac riftSac = (RiftSac) this.entityToDisplay;

            if (riftSac.isInWater()) {
                int minutes = riftSac.getHatchTimeMinutes()[0];
                int seconds = riftSac.getHatchTimeMinutes()[1];
                String minutesString = (minutes < 10 ? "0" : "")+minutes;
                String secondsString = (seconds < 10 ? "0" : "")+seconds;
                String timeString = minutesString+":"+secondsString;

                return new String[]{I18n.format("prift.egg.remaining_hatch_time"), timeString};
            }
            else return new String[]{I18n.format("prift.sac.not_wet")};
        }
        else if (this.eggType == EggType.EMBRYO) {
            if (this.impregnable != null) {
                int minutes = this.impregnable.getBirthTimeMinutes()[0];
                int seconds = this.impregnable.getBirthTimeMinutes()[1];
                String minutesString = (minutes < 10 ? "0" : "")+minutes;
                String secondsString = (seconds < 10 ? "0" : "")+seconds;
                String timeString = minutesString+":"+secondsString;

                return new String[]{I18n.format("prift.pregnancy.remaining_gestation_time"), timeString};
            }
        }
        return new String[0];
    }

    @Override
    public ResourceLocation drawBackground() {
        return new ResourceLocation(RiftInitialize.MODID, "textures/ui/generic_screen.png");
    }

    @Override
    public int[] backgroundTextureSize() {
        return new int[]{176, 166};
    }

    @Override
    public int[] backgroundUV() {
        return new int[]{0, 0};
    }

    @Override
    public int[] backgroundSize() {
        return new int[]{176, 166};
    }

    @Override
    public RiftLibUIElement.Element modifyUISectionElement(RiftLibUISection riftLibUISection, RiftLibUIElement.Element element) {
        return element;
    }

    @Override
    public RiftLibUISection modifyUISection(RiftLibUISection riftLibUISection) {
        return riftLibUISection;
    }

    @Override
    public void onButtonClicked(RiftLibButton riftLibButton) {}

    @Override
    public void onClickableSectionClicked(RiftLibClickableSection riftLibClickableSection) {}

    @Override
    public void onElementHovered(RiftLibUISection riftLibUISection, RiftLibUIElement.Element element) {}

    public enum EggType {
        EGG,
        SAC,
        EMBRYO
    }
}
