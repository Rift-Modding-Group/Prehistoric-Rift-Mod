package anightdazingzoroark.prift.client.ui.widgets;

import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiData;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.widgets.layout.Grid;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A grid of creature cards backed by one synced storage with a fixed identity and capacity.
 * */
public class CreatureStorageGrid extends Grid {
    public CreatureStorageGrid(
            @NotNull CreatureBoxGuiData data, @NotNull GenericSyncValue<CreatureStorage> storage,
            int columns, int cellSize, @NotNull IPanelHandler inventoryWarning
    ) {
        if (columns <= 0) throw new IllegalArgumentException("A creature storage grid must have at least one column");
        if (cellSize <= 0) throw new IllegalArgumentException("Creature storage cells must have a positive size");
        Objects.requireNonNull(storage.getValue(), "Creature storage must not be null");
        this.setSyncOrValue(storage);
        this.coverChildren().mapTo(
                columns, storage.getValue().getSize(),
                index -> new CreatureBoxSlot(
                        "box_creature_" + storage.getValue().getIdentifier() + "_" + index,
                        data, storage, index, columns,
                        inventoryWarning
                ).size(cellSize).margin(1)
        );
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof GenericSyncValue<?> && syncOrValue.isValueOfType(CreatureStorage.class);
    }

    /**
     * Server-owned contents are copied deeply so edits to creature NBT trigger synchronization.
     * */
    @NotNull
    public static GenericSyncValue<CreatureStorage> syncValue(@NotNull Supplier<CreatureStorage> getter) {
        CreatureStorage initial = Objects.requireNonNull(getter.get(), "Creature storage must not be null");
        String identifier = initial.getIdentifier();
        int size = initial.getSize();
        return GenericSyncValue.builder(CreatureStorage.class)
                .getter(getter)
                .serializer((buffer, storage) -> buffer.writeCompoundTag(storage.getAsNBT()))
                .deserializer(buffer -> {
                    CreatureStorage storage = new CreatureStorage(identifier, size);
                    NBTTagCompound tag = buffer.readCompoundTag();
                    if (tag != null) storage.readFromNBT(tag);
                    return storage;
                })
                .equals((first, second) -> first.getIdentifier().equals(second.getIdentifier())
                        && first.getSize() == second.getSize() && first.getAsNBT().equals(second.getAsNBT()))
                .copy(storage -> {
                    CreatureStorage copy = new CreatureStorage(storage.getIdentifier(), storage.getSize());
                    copy.readFromNBT(storage.getAsNBT().copy());
                    return copy;
                }).build();
    }
}
