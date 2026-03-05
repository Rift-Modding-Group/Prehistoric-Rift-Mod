package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.client.newui.RiftUIIcons;
import anightdazingzoroark.prift.client.newui.value.NullableEnumValue;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.properties.journalProgress.JournalProgressProperties;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.TabTexture;
import com.cleanroommc.modularui.drawable.text.LangKey;
import com.cleanroommc.modularui.drawable.text.StringKey;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class JournalRightPageWidget extends ParentWidget<JournalRightPageWidget> {
    @NotNull
    private final JournalProgressProperties journalProgress;
    private final NullableEnumValue.Dynamic<RiftCreatureType> currentCreature;

    private boolean markForWidgetUpdate = true;

    public JournalRightPageWidget(@NotNull JournalProgressProperties journalProgress, NullableEnumValue.Dynamic<RiftCreatureType> currentCreature) {
        this.journalProgress = journalProgress;
        this.currentCreature = currentCreature;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.markForWidgetUpdate) return;

        this.removeAll();
        if (this.currentCreature.getValue() != null) this.child(this.creaturePageContents());
        else this.child(this.noCreaturePageContents());
        this.markForWidgetUpdate = false;
    }

    //todo: maybe add statistics on how many creatures unlocked? idk
    private Flow noCreaturePageContents() {
        return new Column().sizeRel(1f).childPadding(5)
                .child(IKey.str("The journal is where you get to read about creatures you have encountered.").asWidget()
                        .scale(0.75f).left(0));
    }

    private Flow creaturePageContents() {
        PagedWidget.Controller tabController = new PagedWidget.Controller();

        return new Column().sizeRel(1f).childPadding(5)
                .child(IKey.str(this.currentCreature.getValue().getTranslatedName()).asWidget().left(0))
                .child(RiftUIIcons.creatureIllustration(this.currentCreature.getValue()).asWidget().size(120, 90))
                .child(new Row().childPadding(3).coverChildren().left(0)
                        .child(new JournalTabButton(0, tabController)
                                .overlay(IKey.lang("journal.tab.description"))
                                .background(false, new Rectangle().hollow().color(0xFF000000))
                                .background(true, new Rectangle().hollow().color(0xFFFFFFFF))
                        )
                        .child(new JournalTabButton(1, tabController)
                                .overlay(IKey.lang("journal.tab.info"))
                                .background(false, new Rectangle().hollow().color(0xFF000000))
                                .background(true, new Rectangle().hollow().color(0xFFFFFFFF))
                        )
                )
                .child(new PagedWidget<>().controller(tabController).widthRel(1f).height(93)
                        .background(new Rectangle().hollow().color(0xFF000000))
                        //creature description
                        .addPage(this.creatureDescriptionTab())
                        //creature info
                        .addPage(IKey.str("The favorite foods of the creature!").scale(0.75f).asWidget().margin(3))
                );
    }

    private Flow creatureDescriptionTab() {
        if (this.currentCreature.getValue() == null) return new Column();
        if (!this.journalProgress.getEncounteredCreatures().get(this.currentCreature.getValue())) {
            return new Column().sizeRel(1f).margin(3).childPadding(3)
                    .child(IKey.lang("journal.entry.locked")
                            .scale(0.75f).alignment(Alignment.CenterLeft).asWidget().left(0)
                    );
        }
        return new Column().sizeRel(1f).margin(3).childPadding(3)
                .child(IKey.lang("journal.description.behaviors", this.currentCreature.getValue().listBehaviorsAsString())
                        .scale(0.75f).alignment(Alignment.CenterLeft).asWidget().left(0)
                )
                .child(IKey.lang("journal.description.diet", this.currentCreature.getValue().getCreatureDiet().getTranslatedName())
                        .scale(0.75f).alignment(Alignment.CenterLeft).asWidget().left(0)
                )
                .child(IKey.str(this.currentCreature.getValue().getJournalEntry())
                        .scale(0.75f).alignment(Alignment.CenterLeft).asWidget().left(0)
                );
    }

    public void updatePage() {
        this.markForWidgetUpdate = true;
    }

    private static class JournalTabButton extends PageButton {
        public JournalTabButton(int index, PagedWidget.Controller controller) {
            super(index, controller);
        }

        @Override
        public PageButton overlay(IDrawable... drawable) {
            //get longest drawable and use that info to set this button's width
            int longestWidth = 0;
            for (IDrawable iDrawable : drawable) {
                if (!(iDrawable instanceof LangKey langKey)) continue;
                int currentWidth = langKey.getDefaultWidth();
                if (currentWidth > longestWidth) longestWidth = currentWidth;
            }
            this.width(longestWidth + 6);

            return super.overlay(drawable);
        }
    }
}
