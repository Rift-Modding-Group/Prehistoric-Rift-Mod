package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PartyMemberButtonWidget extends Widget<PartyMemberButtonWidget> implements Interactable {
    private final CreatureNBT creatureNBT;
    private IGuiAction.MousePressed mousePressed;

    public PartyMemberButtonWidget(CreatureNBT creatureNBT) {
        super();
        this.creatureNBT = creatureNBT;
        this.size(80, 48);
    }

    public PartyMemberButtonWidget onMousePressed(IGuiAction.MousePressed mousePressed) {
        this.mousePressed = mousePressed;
        return getThis();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (this.mousePressed != null && this.mousePressed.press(mouseButton)) {
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());
        float textScale = 0.5f;

        if (!this.creatureNBT.nbtIsEmpty()) {
            //draw border, color changes depending on whether or not its hovered
            int borderHoverColor = this.isHovering() ? 0xFFFFFFFF : 0xFF000000;
            new Rectangle().setColor(borderHoverColor).setCornerRadius(5).drawAtZero(context, this.getArea(), theme);

            //draw background
            Area bgArea = new Area(this.getArea());
            bgArea.h(bgArea.h() - 2);
            bgArea.w(bgArea.w() - 2);
            new Rectangle().setColor(0xFFC6C6C6).setCornerRadius(5).draw(context, 1, 1, bgArea.w(), bgArea.h(), theme);

            //draw container for creature icon
            int iconHoverColor = this.isHovering() ? 0xFFFFFFFF : 0xFF000000;
            new Rectangle().setColor(iconHoverColor).setCornerRadius(5).draw(context, 2, 2, 28, 28, theme);
            new Rectangle().setColor(this.getIconBGColor()).setCornerRadius(5).draw(context, 3, 3, 26, 26, theme);

            //draw creature icon
            Icon creatureIcon = UITexture.fullImage(
                    new ResourceLocation(
                            RiftInitialize.MODID,
                            "textures/icons/"+this.creatureNBT.getCreatureType().toString().toLowerCase()+"_icon.png"
                    )
            ).asIcon();
            creatureIcon.draw(context, 4, 4, 24, 24, theme);

            //define strings and other values
            IKey creatureNameString = IKey.str(this.creatureNBT.getCreatureName(false)).alignment(Alignment.CenterLeft);
            IKey creatureLevelString = IKey.lang("tametrait.level", this.creatureNBT.getCreatureLevel()).alignment(Alignment.CenterLeft);
            int longestWidth = RiftUtil.maxWithinMultiple(
                    creatureNameString.asTextIcon().getWidth(),
                    creatureLevelString.asTextIcon().getWidth()
            );
            int xTextOff = 64;

            //draw creature name
            creatureNameString.scale(textScale)
                    .color(theme.getTextColor())
                    .draw(context, (int) (xTextOff * textScale), (int) (8 * textScale),
                            longestWidth,
                            creatureNameString.asTextIcon().getHeight(),
                            theme
                    );

            //draw creature level
            creatureLevelString.scale(textScale)
                    .color(theme.getTextColor())
                    .draw(context, (int) (xTextOff * textScale), (int) (24 * textScale),
                            longestWidth,
                            creatureLevelString.asTextIcon().getHeight(),
                            theme
                    );

            //bar initialization
            int barWidth = this.getArea().w() - 8;

            //-----health bar-----
            //background
            new Rectangle().setColor(0xFF000000).draw(context, 3, 32, this.getArea().w() - 6, 3, theme);

            //health
            float healthPercentage = this.creatureNBT.getCreatureHealth()[0] / this.creatureNBT.getCreatureHealth()[1];
            int healthBar = (int) (healthPercentage * barWidth);
            new Rectangle().setColor(0xFFFF0000).draw(context, 4, 33, healthBar, 1, theme);

            //-----energy bar-----
            //background
            new Rectangle().setColor(0xFF000000).draw(context, 3, 36, this.getArea().w() - 6, 3, theme);

            //energy
            float energyPercentage = (float) this.creatureNBT.getCreatureEnergy()[0] / this.creatureNBT.getCreatureEnergy()[1];
            int energyBar = (int) (energyPercentage * barWidth);
            new Rectangle().setColor(0xFFFFFF00).draw(context, 4, 37, energyBar, 1, theme);

            //-----xp bar-----
            //background
            new Rectangle().setColor(0xFF000000).draw(context, 3, 40, this.getArea().w() - 6, 3, theme);

            //xp
            float xpPercentage = (float) this.creatureNBT.getCreatureXP()[0] / this.creatureNBT.getCreatureXP()[1];
            int xpBar = (int) (xpPercentage * barWidth);
            new Rectangle().setColor(0xFF98D06B).draw(context, 4, 41, xpBar, 1, theme);
        }
        else new Rectangle().setColor(0xFF212121).setCornerRadius(5).drawAtZero(context, this.getArea(), theme);
    }

    private int getIconBGColor() {
        if (this.creatureNBT.nbtIsEmpty()) return -1;
        else if (this.creatureNBT.getCreatureHealth()[0] <= 0) return 0xFFF33F3F;
        else if (this.creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) return 0xFF208620;
        return 0xFFC6C6C6;
    }
}
