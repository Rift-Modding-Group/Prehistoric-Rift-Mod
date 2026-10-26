package anightdazingzoroark.prift.server.creatureBox;

import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorageTransfer.Slot;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureBoxRecovery;
import com.cleanroommc.modularui.factory.PosGuiData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Selection and pending confirmation belong to one open container, never to the block.
 * */
public class CreatureBoxGuiData extends PosGuiData {
    public int selectedBox;
    public boolean swapping;
    @NotNull
    public String selectedStorage = "";
    public int selectedSlot = -1;
    @Nullable
    public UUID selectedCreature;
    @Nullable
    public Slot pendingSource;
    @Nullable
    public Slot pendingDestination;
    @Nullable
    public UUID pendingCreature;
    @Nullable
    public UUID pendingDestinationCreature;
    @NotNull
    public String renameDraft = "";
    @NotNull
    public RevivalSnapshot revivalSnapshot = RevivalSnapshot.NONE;

    public CreatureBoxGuiData(@NotNull EntityPlayer player, @NotNull BlockPos position, int box) {
        super(player, position);
        this.selectedBox = Math.clamp(box, 0, PlayerCreatureBoxProperties.BOX_COUNT - 1);
    }

    public static void write(@NotNull PacketBuffer buffer, @NotNull CreatureBoxGuiData data) {
        buffer.writeBlockPos(data.getBlockPos());
        buffer.writeByte(data.selectedBox);
        buffer.writeBoolean(data.swapping);
        buffer.writeBoolean(data.selectedCreature != null);
        if (data.selectedCreature != null) {
            buffer.writeString(data.selectedStorage);
            buffer.writeByte(data.selectedSlot);
            buffer.writeUniqueId(data.selectedCreature);
        }
    }

    @NotNull
    public static CreatureBoxGuiData read(@NotNull EntityPlayer player, @NotNull PacketBuffer buffer) {
        CreatureBoxGuiData data = new CreatureBoxGuiData(player, buffer.readBlockPos(), buffer.readUnsignedByte());
        data.swapping = buffer.readBoolean();
        if (buffer.readBoolean()) {
            data.selectedStorage = buffer.readString(64);
            data.selectedSlot = buffer.readUnsignedByte();
            data.selectedCreature = buffer.readUniqueId();
        }
        return data;
    }

    public record RevivalCountdown(@Nullable UUID creature, int seconds) {
        public static final RevivalCountdown NONE = new RevivalCountdown(null, 0);

        public static void write(@NotNull PacketBuffer buffer, @NotNull RevivalCountdown countdown) {
            buffer.writeBoolean(countdown.creature() != null);
            if (countdown.creature() != null) buffer.writeUniqueId(countdown.creature());
            buffer.writeVarInt(countdown.seconds());
        }

        @NotNull
        public static RevivalCountdown read(@NotNull PacketBuffer buffer) {
            UUID creature = buffer.readBoolean() ? buffer.readUniqueId() : null;
            return new RevivalCountdown(creature, Math.max(0, buffer.readVarInt()));
        }
    }

    /**
     * One packet replaces every slot together, with seconds sampled from one server clock.
     * */
    public record RevivalSnapshot(@NotNull List<RevivalCountdown> countdowns) {
        public static final RevivalSnapshot NONE = new RevivalSnapshot(
                Collections.nCopies(PlayerCreatureBoxProperties.BOX_SIZE, RevivalCountdown.NONE));

        public RevivalSnapshot {
            if (countdowns.size() != PlayerCreatureBoxProperties.BOX_SIZE) {
                throw new IllegalArgumentException("A revival snapshot must contain every slot in one box!");
            }
            countdowns = List.copyOf(countdowns);
        }

        @NotNull
        public static RevivalSnapshot capture(@NotNull CreatureStorage storage, long currentTime) {
            return new RevivalSnapshot(IntStream.range(0, PlayerCreatureBoxProperties.BOX_SIZE).mapToObj(slot -> {
                CreatureNBT creature = storage.getCreature(slot);
                int seconds = CreatureBoxRecovery.remainingSeconds(creature, currentTime);
                return seconds <= 0 ? RevivalCountdown.NONE : new RevivalCountdown(creature.getUniqueID(), seconds);
            }).toList());
        }

        public static void write(@NotNull PacketBuffer buffer, @NotNull RevivalSnapshot snapshot) {
            snapshot.countdowns().forEach(countdown -> RevivalCountdown.write(buffer, countdown));
        }

        @NotNull
        public static RevivalSnapshot read(@NotNull PacketBuffer buffer) {
            return new RevivalSnapshot(IntStream.range(0, PlayerCreatureBoxProperties.BOX_SIZE)
                    .mapToObj(slot -> RevivalCountdown.read(buffer)).toList());
        }
    }
}
