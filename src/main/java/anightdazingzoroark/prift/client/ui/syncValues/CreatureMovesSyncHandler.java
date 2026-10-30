package anightdazingzoroark.prift.client.ui.syncValues;

import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiData;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiFactory;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Server-owned move lists; clients send slot selections rather than replacement move IDs.
 *
 */
public class CreatureMovesSyncHandler extends SyncHandler {
    private RiftCreatureGuiData data;
    private Snapshot snapshot;
    private long revision;
    private boolean swapping;
    private boolean selectedBank;
    private int selectedIndex = -1;
    private boolean pending;

    public CreatureMovesSyncHandler(@NotNull RiftCreatureGuiData data) {
        this.data = data;
        CreatureMoveStorage storage = data.getCreature().getCreatureMoves();
        storage.setCreatureUser(data.getCreature().getCreatureType());
        this.snapshot = Snapshot.fromStorage(storage);
    }

    @NotNull
    public Snapshot getSnapshot() {
        return this.snapshot;
    }

    public boolean isSwapping() {
        return this.swapping;
    }

    public boolean canSwap() {
        return !this.pending && !this.snapshot.busy();
    }

    public void setSwapping(boolean swapping) {
        if (!this.canSwap()) return;
        this.swapping = swapping;
        this.selectedIndex = -1;
    }

    public boolean isSelected(boolean bank, int index) {
        return this.swapping && this.selectedIndex == index && this.selectedBank == bank;
    }

    public boolean hasSelection() {
        return this.selectedIndex >= 0;
    }

    public static void selectSlot(@NotNull CreatureMovesSyncHandler handler, boolean bank, int index) {
        if (!handler.swapping || !handler.canSwap()) return;
        List<String> moves = bank ? handler.snapshot.bank() : handler.snapshot.equipped();
        if (!handler.hasSelection()) {
            if (index >= moves.size()) return;
            handler.selectedBank = bank;
            handler.selectedIndex = index;
            return;
        }
        if (handler.isSelected(bank, index)) {
            handler.selectedIndex = -1;
            return;
        }
        handler.syncToServer(1, buffer -> {
            buffer.writeLong(handler.revision);
            buffer.writeBoolean(handler.selectedBank);
            buffer.writeVarInt(handler.selectedIndex);
            buffer.writeBoolean(bank);
            buffer.writeVarInt(index);
        });
        handler.selectedIndex = -1;
        handler.pending = true;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buffer) throws IOException {
        if (id != 0) return;
        this.revision = buffer.readLong();
        String phase = buffer.readString(256);
        int maximumSlots = buffer.readVarInt();
        boolean busy = buffer.readBoolean();
        List<List<String>> lists = new ArrayList<>();
        for (int list = 0; list < 2; list++) {
            int count = buffer.readVarInt();
            if (count < 0 || count > 4096) throw new IOException("Invalid move list size");
            List<String> moves = new ArrayList<>();
            for (int index = 0; index < count; index++) moves.add(buffer.readString(256));
            lists.add(moves);
        }
        this.snapshot = new Snapshot(phase, maximumSlots, busy, lists.get(0), lists.get(1));
        this.pending = false;
        this.selectedIndex = -1;
    }

    @Override
    public void readOnServer(int id, PacketBuffer buffer) throws IOException {
        if (id != 1) return;
        long expectedRevision = buffer.readLong();
        boolean sourceBank = buffer.readBoolean();
        int source = buffer.readVarInt();
        boolean destinationBank = buffer.readBoolean();
        int destination = buffer.readVarInt();
        if (RiftCreatureGuiFactory.INSTANCE.canInteractWith(this.data.getPlayer(), this.data)
                && this.data.getCreature().isOwner(this.data.getPlayer()) && expectedRevision == this.revision) {
            CreatureMoveStorage storage = this.data.getCreature().getCreatureMoves();
            if (storage.getCreaturePhase().equals(this.snapshot.phase())
                    && Snapshot.fromStorage(storage).equals(this.snapshot)
                    && storage.swapMoves(sourceBank, source, destinationBank, destination)) {
                this.data.getCreature().setCreatureMoves(storage);
                this.data.saveChanges();
            }
        }
        this.detectAndSendChanges(true);
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        CreatureMoveStorage storage = this.data.getCreature().getCreatureMoves();
        if (storage.unlockLevelupMoves(this.data.getCreature().getLevel())) {
            this.data.getCreature().setCreatureMoves(storage);
            this.data.saveChanges();
        }
        Snapshot next = Snapshot.fromStorage(storage);
        if (!init && next.equals(this.snapshot)) return;
        this.snapshot = next;
        this.revision++;
        this.syncToClient(0, buffer -> {
            buffer.writeLong(this.revision);
            buffer.writeString(next.phase());
            buffer.writeVarInt(next.maximumSlots());
            buffer.writeBoolean(next.busy());
            for (List<String> moves : List.of(next.equipped(), next.bank())) {
                buffer.writeVarInt(moves.size());
                for (String move : moves) buffer.writeString(move);
            }
        });
    }

    public record Snapshot(@NotNull String phase, int maximumSlots, boolean busy,
                           @NotNull List<String> equipped, @NotNull List<String> bank) {
        public Snapshot {
            equipped = List.copyOf(equipped);
            bank = List.copyOf(bank);
        }

        @NotNull
        public static Snapshot fromStorage(@NotNull CreatureMoveStorage storage) {
            return new Snapshot(storage.getCreaturePhase(), storage.getMaximumMoveSlots(), !storage.getCurrentMove().isEmpty(),
                    storage.getUsableMoves().stream().map(move -> move.getKey()).toList(), storage.getMoveBank());
        }
    }
}
