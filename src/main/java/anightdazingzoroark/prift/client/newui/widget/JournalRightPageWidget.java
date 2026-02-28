package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.client.newui.value.NullableEnumSyncValue;
import anightdazingzoroark.prift.client.newui.value.NullableEnumValue;
import anightdazingzoroark.prift.client.newui.value.PlayerJournalProgressSyncValue;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;

public class JournalRightPageWidget extends ParentWidget<JournalRightPageWidget> {
    private final IPlayerJournalProgress journalProgress;
    private final NullableEnumValue.Dynamic<RiftCreatureType> currentCreature;

    private boolean markForWidgetUpdate = true;

    public JournalRightPageWidget(IPlayerJournalProgress journalProgress, NullableEnumValue.Dynamic<RiftCreatureType> currentCreature) {
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
        return new Column().sizeRel(1f).childPadding(5)
                .child(IKey.str(this.currentCreature.getValue().getTranslatedName()).asWidget().left(0));
    }

    public void updatePage() {
        this.markForWidgetUpdate = true;
    }
}
