package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import com.cleanroommc.modularui.factory.GuiData;
import net.minecraft.entity.player.EntityPlayer;
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
        if (this.isClient() || !this.isPartyMember()) return;
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
