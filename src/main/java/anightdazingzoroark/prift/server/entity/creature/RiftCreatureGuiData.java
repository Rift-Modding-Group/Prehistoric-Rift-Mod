package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import com.cleanroommc.modularui.factory.GuiData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Data used to build a creature UI for either a deployed creature entity or a stored creature.
 */
public class RiftCreatureGuiData extends GuiData {
    @NotNull
    private final IRiftCreature creature;
    private final int partyIndex;
    private final int initialPage;
    private int boxIndex = -1;
    private int boxSlot = -1;
    @Nullable
    private BlockPos boxPosition;

    public RiftCreatureGuiData(@NotNull EntityPlayer player, @NotNull IRiftCreature creature) {
        this(player, creature, -1, 0);
    }

    public RiftCreatureGuiData(
            @NotNull EntityPlayer player, @NotNull IRiftCreature creature,
            int partyIndex, int initialPage
    ) {
        super(player);
        this.creature = Objects.requireNonNull(creature);
        this.partyIndex = partyIndex;
        this.initialPage = Math.clamp(initialPage, 0, 3);
    }

    public RiftCreatureGuiData(
            @NotNull EntityPlayer player, @NotNull CreatureNBT creature,
            int boxIndex, int boxSlot, int initialPage, @NotNull BlockPos boxPosition
    ) {
        this(player, creature, -1, Math.clamp(initialPage, 1, 3));
        this.boxIndex = boxIndex;
        this.boxSlot = boxSlot;
        this.boxPosition = boxPosition;
    }

    public boolean isBoxed() {
        return this.boxIndex >= 0;
    }

    public int getBoxIndex() {
        return this.boxIndex;
    }

    public int getBoxSlot() {
        return this.boxSlot;
    }

    @Nullable
    public BlockPos getBoxPosition() {
        return this.boxPosition;
    }

    @NotNull
    public IRiftCreature getCreature() {
        return this.creature;
    }

    public int getPartyIndex() {
        return this.partyIndex;
    }

    public int getInitialPage() {
        return this.initialPage;
    }

    public boolean isPartyMember() {
        return this.partyIndex >= 0;
    }

    public void saveChanges() {
        if (this.isClient()) return;
        if (this.isBoxed() && this.creature instanceof CreatureNBT storedCreature) {
            if (!RiftCreatureGuiFactory.INSTANCE.canInteractWith(this.getPlayer(), this)) return;
            PlayerCreatureBoxProperties boxes = PlayerCreatureBoxProperties.get(this.getPlayer());
            if (boxes != null) boxes.saveCreature(this.boxIndex, this.boxSlot, storedCreature);
            return;
        }
        if (!this.isPartyMember()) return;
        PlayerPartyProperties playerParty = PlayerPartyProperties.get(this.getPlayer());
        if (playerParty != null) playerParty.savePartyMember(this.partyIndex, this.creature);
    }

    public void saveInventory(@NotNull RiftLibInventoryHandler inventory) {
        this.creature.setCreatureInventory(inventory);
        this.saveChanges();
    }

    public void saveGear(@NotNull RiftLibInventoryHandler gear) {
        this.creature.setCreatureGear(gear);
        this.saveChanges();
    }
}