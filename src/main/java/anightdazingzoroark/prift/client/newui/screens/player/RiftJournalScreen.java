package anightdazingzoroark.prift.client.newui.screens.player;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.client.newui.UIThemes;
import anightdazingzoroark.prift.client.newui.panel.ModularPanelExitAffectable;
import anightdazingzoroark.prift.client.newui.value.NullableEnumValue;
import anightdazingzoroark.prift.client.newui.widget.JournalLeftPageWidget;
import anightdazingzoroark.prift.client.newui.widget.JournalRightPageWidget;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.properties.journalProgress.JournalProgressHelper;
import anightdazingzoroark.prift.server.properties.journalProgress.JournalProgressProperties;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.widget.ParentWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class RiftJournalScreen extends CustomModularScreen {
    private RiftCreatureType.CreatureCategory currentCategory;
    private RiftCreatureType currentCreature;

    public RiftJournalScreen() {
        super(RiftInitialize.MODID);
    }

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        JournalProgressProperties journalProgress = JournalProgressHelper.getJournalProgress(player);

        NullableEnumValue.Dynamic<RiftCreatureType.CreatureCategory> currentCategoryDynamic = new NullableEnumValue.Dynamic<>(
                RiftCreatureType.CreatureCategory.class,
                () -> this.currentCategory,
                value -> this.currentCategory = value
        );
        NullableEnumValue.Dynamic<RiftCreatureType> currentCreatureDynamic = new NullableEnumValue.Dynamic<>(
                RiftCreatureType.class,
                () -> this.currentCreature,
                value -> this.currentCreature = value
        );

        return new ModularPanelExitAffectable(UIPanelNames.JOURNAL_SCREEN)
                .onEscPressed(new Function<ModularPanelExitAffectable, Boolean>() {
                    @Override
                    public Boolean apply(ModularPanelExitAffectable panel) {
                        if (currentCategoryDynamic.getValue() != null) {
                            currentCategoryDynamic.setValue(null);
                            currentCreatureDynamic.setValue(null);
                            JournalLeftPageWidget leftPageWidget = this.getLeftPageWidget(panel);
                            if (leftPageWidget != null) leftPageWidget.updatePages();
                        }
                        else PlayerUIHelper.openUI(player, UIPanelNames.PARTY_SCREEN);
                        return true;
                    }

                    private JournalLeftPageWidget getLeftPageWidget(@NotNull ModularPanelExitAffectable panel) {
                        for (IWidget child : panel.getChildren()) {
                            if (child instanceof JournalLeftPageWidget leftPageWidget) return leftPageWidget;
                        }
                        return null;
                    }
                })
                .widgetTheme(UIThemes.JOURNAL_PANEL)
                //-----left page-----
                .child(new JournalLeftPageWidget(journalProgress, currentCategoryDynamic, currentCreatureDynamic)
                        .name("leftPage").size(189, 225).left(8).top(8)
                )
                //-----right page-----
                .child(new JournalRightPageWidget(currentCreatureDynamic)
                        .name("rightPage").size(189, 225).right(8).top(8)
                );
    }
}
