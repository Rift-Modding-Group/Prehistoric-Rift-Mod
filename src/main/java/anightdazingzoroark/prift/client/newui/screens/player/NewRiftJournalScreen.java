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

public class NewRiftJournalScreen extends CustomModularScreen {
    private RiftCreatureType currentCreature;

    public NewRiftJournalScreen() {
        super(RiftInitialize.MODID);
    }

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        JournalProgressProperties journalProgress = JournalProgressHelper.getJournalProgress(player);

        NullableEnumValue.Dynamic<RiftCreatureType> currentCreatureDynamic = new NullableEnumValue.Dynamic<>(
                RiftCreatureType.class,
                () -> this.currentCreature,
                value -> this.currentCreature = value
        );

        return new ModularPanelExitAffectable(UIPanelNames.JOURNAL_SCREEN)
                .onEscPressed(panel -> {
                    PlayerUIHelper.openUI(player, UIPanelNames.PARTY_SCREEN);
                    return true;
                })
                .widgetTheme(UIThemes.JOURNAL_PANEL)
                //-----left page-----
                .child(new JournalLeftPageWidget(journalProgress, currentCreatureDynamic)
                        .name("leftPage").size(189, 225).left(8).top(8)
                )
                //-----right page-----
                .child(new JournalRightPageWidget(journalProgress, currentCreatureDynamic)
                        .name("rightPage").size(189, 225).right(8).top(8)
                );
    }

    private static IKey str(String value) {
        return IKey.str(value).scale(0.75f);
    }

    private static void tryAddChild(ParentWidget<?> parentWidget, IWidget childToAdd) {
        if (!parentWidget.getChildren().contains(childToAdd)) {
            parentWidget.child(childToAdd);
            parentWidget.scheduleResize();
        }
    }

    private static void tryRemoveChild(ParentWidget<?> parentWidget, IWidget childToRemove) {
        if (parentWidget.getChildren().contains(childToRemove)) {
            parentWidget.remove(childToRemove);
            parentWidget.scheduleResize();
        }
    }
}
