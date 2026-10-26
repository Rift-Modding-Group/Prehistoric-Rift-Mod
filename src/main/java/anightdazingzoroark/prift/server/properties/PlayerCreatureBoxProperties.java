package anightdazingzoroark.prift.server.properties;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorageTransfer;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorageTransfer.Result;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorageTransfer.Slot;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureBoxRecovery;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiData;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiFactory;
import com.cleanroommc.modularui.screen.ModularContainer;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.AbstractEntityProperties;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.RiftLibProperty;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.ObjectPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.StringPropertyValue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class PlayerCreatureBoxProperties extends AbstractEntityProperties<EntityPlayer> {
    public static final String PROPERTY_NAME = "PlayerCreatureBoxes";
    public static final int BOX_COUNT = 20;
    public static final int BOX_SIZE = 20;
    private static final List<String> BOX_IDENTIFIERS = IntStream.range(0, BOX_COUNT).mapToObj(index -> "Box" + index).toList();

    public PlayerCreatureBoxProperties(@NotNull String propertyName, @NotNull EntityPlayer player) {
        super(propertyName, player);
    }

    @Nullable
    public static PlayerCreatureBoxProperties get(@Nullable EntityPlayer player) {
        return player == null ? null : RiftLibProperty.getProperty(PROPERTY_NAME, player);
    }

    @Override
    protected void registerDefaults(@NotNull EntityPlayer entity) {
        for (int index = 0; index < BOX_COUNT; index++) {
            String identifier = getBoxIdentifier(index);
            this.register(new ObjectPropertyValue<>(
                    identifier, new CreatureStorage(identifier, BOX_SIZE), CreatureStorage.class,
                    CreatureStorage::getAsNBT,
                    tag -> {
                        CreatureStorage storage = new CreatureStorage(identifier, BOX_SIZE);
                        if (tag instanceof NBTTagCompound compound) storage.readFromNBT(compound);
                        return storage;
                    }
            ));
            // An empty custom name is displayed as the localized "Box %s".
            this.register(new StringPropertyValue("BoxName" + index, ""));
        }
    }

    @Override
    public void onTickProperty() {
        EntityPlayer player = this.getEntityHolder();
        if (player.world.isRemote) return;
        if (player.openContainer instanceof ModularContainer container
                && container.getGuiData() instanceof CreatureBoxGuiData data
                && CreatureBoxGuiFactory.INSTANCE.canInteractWith(player, data)) {
            this.updateBox(data.selectedBox, false);
        }
    }

    /**
     * Closed boxes are caught up only on access; this never visits another box.
     * */
    public void updateBox(int box, boolean sync) {
        if (this.getEntityHolder().world.isRemote || box < 0 || box >= BOX_COUNT) return;
        CreatureStorage storage = this.getBox(box);
        long now = CreatureBoxRecovery.currentTime(this.getEntityHolder());
        boolean changed = false;
        for (int slot = 0; slot < BOX_SIZE; slot++) {
            if (CreatureBoxRecovery.update(storage.getCreature(slot), now)) changed = true;
        }
        if (sync || changed) this.set(storage.getIdentifier(), storage);
    }

    @NotNull
    public static String getBoxIdentifier(int index) {
        return BOX_IDENTIFIERS.get(index);
    }

    /**
     * Returns -1 for an identifier that is not one of this player's personal boxes.
     * */
    public static int getBoxIndex(@NotNull String identifier) {
        return BOX_IDENTIFIERS.indexOf(identifier);
    }

    @NotNull
    public CreatureStorage getBox(int index) {
        if (index < 0 || index >= BOX_COUNT) throw new IndexOutOfBoundsException(index);
        return this.get(getBoxIdentifier(index));
    }

    @NotNull
    public String getBoxName(int index) {
        if (index < 0 || index >= BOX_COUNT) throw new IndexOutOfBoundsException(index);
        return this.get("BoxName" + index);
    }

    public void setBoxName(int index, @NotNull String name) {
        if (this.getEntityHolder().world.isRemote || index < 0 || index >= BOX_COUNT) return;
        String cleanedName = name.strip();
        this.set("BoxName" + index, cleanedName.substring(0, Math.min(32, cleanedName.length())));
    }

    public void saveCreature(int box, int slot, @NotNull CreatureNBT creature) {
        if (this.getEntityHolder().world.isRemote || box < 0 || box >= BOX_COUNT || slot < 0 || slot >= BOX_SIZE) return;
        this.updateBox(box, false);
        CreatureStorage storage = this.getBox(box);
        CreatureNBT current = storage.getCreature(slot);
        if (current.nbtTagCompound().isEmpty() || !current.getUniqueID().equals(creature.getUniqueID())
                || !creature.isOwner(this.getEntityHolder())) return;
        NBTTagCompound edited = creature.nbtTagCompound().copy();
        // Detail pages hold a snapshot. Editing a nickname must not roll back recovery or revival.
        for (String key : new String[]{"Health", "DeathTime", "HurtTime", "Stamina", "InactiveStaminaRegen", "LeapCooldown", "SprintCooldown", "CreatureMoves", CreatureBoxRecovery.TAG}) {
            if (current.nbtTagCompound().hasKey(key)) edited.setTag(key, current.nbtTagCompound().getTag(key).copy());
        }
        storage.setCreature(slot, new CreatureNBT(edited));
        this.set(storage.getIdentifier(), storage);
    }

    public static boolean addTamedCreature(@NotNull EntityPlayer player, @NotNull RiftCreature creature) {
        PlayerCreatureBoxProperties boxes = get(player);
        if (player.world.isRemote || boxes == null || !creature.isOwner(player)) return false;
        for (int box = 0; box < BOX_COUNT; box++) {
            CreatureStorage storage = boxes.getBox(box);
            int slot = storage.getFirstEmptySlot();
            if (slot < 0) continue;

            creature.setDeploymentType(RiftCreatureEnums.CreatureDeployment.BASE_INACTIVE);
            NBTTagCompound tag = new NBTTagCompound();
            creature.writeToNBT(tag);
            CreatureNBT stored = new CreatureNBT(tag);
            CreatureBoxRecovery.update(stored, CreatureBoxRecovery.currentTime(player));
            storage.setCreature(slot, stored);
            boxes.set(storage.getIdentifier(), storage);
            creature.setDead();
            return true;
        }
        return false;
    }

    /**
     * Checks the live inventory again when confirmation arrives, and never overwrites a slot.
     * */
    @NotNull
    public Result depositPartyMember(int partySlot, int box, int slot, @NotNull UUID expectedCreature, boolean confirmed) {
        EntityPlayer player = this.getEntityHolder();
        PlayerPartyProperties party = PlayerPartyProperties.get(player);
        if (player.world.isRemote || party == null || partySlot < 0 || partySlot >= PlayerPartyProperties.MAX_SIZE
                || box < 0 || box >= BOX_COUNT || slot < 0 || slot >= BOX_SIZE) return Result.UNAVAILABLE;
        CreatureStorage storage = this.getBox(box);
        if (!storage.getCreature(slot).nbtTagCompound().isEmpty()) return Result.NO_SPACE;
        CreatureNBT stored = party.getCreatureStorage().getCreature(partySlot);
        if (stored.nbtTagCompound().isEmpty() || !stored.getUniqueID().equals(expectedCreature) || !stored.isOwner(player)) {
            return Result.UNAVAILABLE;
        }

        return CreatureStorageTransfer.swap(player, Slot.party(partySlot), Slot.box(box, slot), expectedCreature, null, confirmed);
    }

    public static boolean withdrawCreature(@NotNull EntityPlayer player, int box, int slot, @NotNull UUID expectedCreature) {
        PlayerCreatureBoxProperties boxes = get(player);
        PlayerPartyProperties party = PlayerPartyProperties.get(player);
        if (player.world.isRemote || party == null || boxes == null || box < 0 || box >= BOX_COUNT || slot < 0 || slot >= BOX_SIZE) return false;
        CreatureStorage storage = boxes.getBox(box);
        CreatureNBT creature = storage.getCreature(slot);
        if (creature.nbtTagCompound().isEmpty() || !creature.getUniqueID().equals(expectedCreature) || !creature.isOwner(player)) return false;
        int partySlot = party.getCreatureStorage().getFirstEmptySlot();
        if (partySlot < 0) return false;
        return CreatureStorageTransfer.swap(player, Slot.box(box, slot), Slot.party(partySlot),
                expectedCreature, null, false) == Result.SUCCESS;
    }

    public static boolean releaseCreature(@NotNull EntityPlayer player, int box, int slot, @NotNull UUID expectedCreature) {
        PlayerCreatureBoxProperties boxes = get(player);
        if (player.world.isRemote || boxes == null || box < 0 || box >= BOX_COUNT || slot < 0 || slot >= BOX_SIZE) return false;
        CreatureStorage storage = boxes.getBox(box);
        CreatureNBT creature = storage.getCreature(slot);
        if (creature.nbtTagCompound().isEmpty() || !creature.getUniqueID().equals(expectedCreature) || !creature.isOwner(player)) {
            return false;
        }
        storage.removeCreature(slot);
        boxes.set(storage.getIdentifier(), storage);
        return true;
    }
}