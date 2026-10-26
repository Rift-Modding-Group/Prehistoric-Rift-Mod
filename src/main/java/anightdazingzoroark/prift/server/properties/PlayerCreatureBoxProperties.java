package anightdazingzoroark.prift.server.properties;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureBoxRecovery;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiData;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiFactory;
import anightdazingzoroark.prift.util.RiftUtil;
import com.cleanroommc.modularui.screen.ModularContainer;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.AbstractEntityProperties;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.RiftLibProperty;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.ObjectPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.StringPropertyValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerCreatureBoxProperties extends AbstractEntityProperties<EntityPlayer> {
    public static final String PROPERTY_NAME = "PlayerCreatureBoxes";
    public static final int BOX_COUNT = 20;
    public static final int BOX_SIZE = 20;

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
            this.register(new ObjectPropertyValue<>(
                    "Box" + index, new CreatureStorage(BOX_SIZE), CreatureStorage.class,
                    CreatureStorage::getAsNBT,
                    tag -> {
                        CreatureStorage storage = new CreatureStorage(BOX_SIZE);
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

    /** Closed boxes are caught up only on access; this never visits another box. */
    public void updateBox(int box, boolean sync) {
        if (this.getEntityHolder().world.isRemote || box < 0 || box >= BOX_COUNT) return;
        CreatureStorage storage = this.getBox(box);
        long now = CreatureBoxRecovery.currentTime(this.getEntityHolder());
        boolean changed = false;
        for (int slot = 0; slot < BOX_SIZE; slot++) {
            if (CreatureBoxRecovery.update(storage.getCreature(slot), now)) changed = true;
        }
        if (sync || changed) this.set("Box" + box, storage);
    }

    @NotNull
    public CreatureStorage getBox(int index) {
        if (index < 0 || index >= BOX_COUNT) throw new IndexOutOfBoundsException(index);
        return this.get("Box" + index);
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
        this.set("Box" + box, storage);
    }

    public static boolean addTamedCreature(@NotNull EntityPlayer player, @NotNull RiftCreature creature) {
        PlayerCreatureBoxProperties boxes = get(player);
        if (player.world.isRemote || boxes == null || !creature.isOwner(player)) return false;
        for (int box = 0; box < BOX_COUNT; box++) {
            CreatureStorage storage = boxes.getBox(box);
            for (int slot = 0; slot < BOX_SIZE; slot++) {
                if (!storage.getCreature(slot).nbtTagCompound().isEmpty()) continue;
                creature.setDeploymentType(RiftCreatureEnums.CreatureDeployment.BASE_INACTIVE);
                NBTTagCompound tag = new NBTTagCompound();
                creature.writeToNBT(tag);
                CreatureNBT stored = new CreatureNBT(tag);
                CreatureBoxRecovery.update(stored, CreatureBoxRecovery.currentTime(player));
                storage.setCreature(slot, stored);
                boxes.set("Box" + box, storage);
                creature.setDead();
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the live inventory again when confirmation arrives, and never overwrites a slot.
     * */
    @NotNull
    public DepositResult depositPartyMember(int partySlot, int box, int slot, @NotNull UUID expectedCreature, boolean confirmed) {
        EntityPlayer player = this.getEntityHolder();
        PlayerPartyProperties party = PlayerPartyProperties.get(player);
        if (player.world.isRemote || party == null || partySlot < 0 || partySlot >= PlayerPartyProperties.MAX_SIZE
                || box < 0 || box >= BOX_COUNT || slot < 0 || slot >= BOX_SIZE) return DepositResult.UNAVAILABLE;
        CreatureStorage storage = this.getBox(box);
        if (!storage.getCreature(slot).nbtTagCompound().isEmpty()) return DepositResult.NO_SPACE;
        CreatureNBT stored = party.getCreatureStorage().getCreature(partySlot);
        if (stored.nbtTagCompound().isEmpty() || !stored.getUniqueID().equals(expectedCreature) || !stored.isOwner(player)) {
            return DepositResult.UNAVAILABLE;
        }

        return swapCreatures(player, new StoragePosition(-1, partySlot), new StoragePosition(box, slot), expectedCreature, null, confirmed);
    }

    /** A box index of -1 identifies a party slot. Empty destinations are represented by a null UUID. */
    public record StoragePosition(int box, int slot) {}

    @NotNull
    public static DepositResult swapCreatures(
            @NotNull EntityPlayer player, @NotNull StoragePosition first, @NotNull StoragePosition second,
            @NotNull UUID expectedFirst, @Nullable UUID expectedSecond, boolean confirmed
    ) {
        PlayerCreatureBoxProperties boxes = get(player);
        PlayerPartyProperties party = PlayerPartyProperties.get(player);
        if (player.world.isRemote || boxes == null || party == null || first.equals(second)) return DepositResult.UNAVAILABLE;
        for (StoragePosition position : new StoragePosition[]{first, second}) {
            int size = position.box() == -1 ? PlayerPartyProperties.MAX_SIZE : BOX_SIZE;
            if (position.box() < -1 || position.box() >= BOX_COUNT || position.slot() < 0 || position.slot() >= size) {
                return DepositResult.UNAVAILABLE;
            }
        }
        if (first.box() >= 0) boxes.updateBox(first.box(), true);
        if (second.box() >= 0 && second.box() != first.box()) boxes.updateBox(second.box(), true);
        CreatureStorage firstStorage = first.box() == -1 ? party.getCreatureStorage() : boxes.getBox(first.box());
        CreatureStorage secondStorage = second.box() == -1 ? party.getCreatureStorage() : boxes.getBox(second.box());
        CreatureNBT firstCreature = firstStorage.getCreature(first.slot());
        CreatureNBT secondCreature = secondStorage.getCreature(second.slot());
        if (firstCreature.nbtTagCompound().isEmpty() || !firstCreature.isOwner(player)
                || !firstCreature.getUniqueID().equals(expectedFirst)) return DepositResult.UNAVAILABLE;
        if (expectedSecond == null) {
            if (!secondCreature.nbtTagCompound().isEmpty()) return DepositResult.UNAVAILABLE;
        }
        else if (secondCreature.nbtTagCompound().isEmpty() || !secondCreature.isOwner(player)
                || !secondCreature.getUniqueID().equals(expectedSecond)) return DepositResult.UNAVAILABLE;

        if (first.box() == -1 && second.box() == -1) {
            party.swapPartyMembers(first.slot(), second.slot());
            return DepositResult.SUCCESS;
        }
        if (first.box() >= 0 && second.box() >= 0) {
            firstStorage.setCreature(first.slot(), secondCreature);
            secondStorage.setCreature(second.slot(), firstCreature);
            boxes.set("Box" + first.box(), firstStorage);
            if (first.box() != second.box()) boxes.set("Box" + second.box(), secondStorage);
            return DepositResult.SUCCESS;
        }

        StoragePosition partyPosition = first.box() == -1 ? first : second;
        StoragePosition boxPosition = first.box() == -1 ? second : first;
        CreatureNBT outgoing = party.getCreatureStorage().getCreature(partyPosition.slot());
        CreatureNBT incoming = boxes.getBox(boxPosition.box()).getCreature(boxPosition.slot());
        long now = CreatureBoxRecovery.currentTime(player);
        if (CreatureBoxRecovery.remainingSeconds(incoming, now) > 0) return DepositResult.REVIVING;
        Entity entity = null;
        if (!outgoing.nbtTagCompound().isEmpty()) {
            entity = RiftUtil.getEntityWithUUID(player.world, outgoing.getUniqueID());
            if (entity instanceof RiftCreature creature) {
                if (!creature.isOwner(player) || creature.isBeingRidden() || creature.isRiding() || creature.isDead) {
                    return DepositResult.UNAVAILABLE;
                }
                NBTTagCompound tag = new NBTTagCompound();
                creature.writeToNBT(tag);
                outgoing = new CreatureNBT(tag);
            }
            else {
                if (outgoing.getDeploymentType() != RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE) return DepositResult.UNAVAILABLE;
                outgoing = new CreatureNBT(outgoing.nbtTagCompound().copy());
            }
            RiftLibInventoryHandler inventory = outgoing.getCreatureInventory();
            if (!inventory.isEmpty() && !confirmed) return DepositResult.NEEDS_CONFIRMATION;
            for (int index = 0; index < inventory.getSlots(); index++) {
                ItemStack stack = inventory.extractItem(index, inventory.getStackInSlot(index).getCount(), false);
                if (!stack.isEmpty()) player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY, player.posZ, stack));
            }
            outgoing.setCreatureInventory(inventory);
            outgoing.setDeploymentType(RiftCreatureEnums.CreatureDeployment.BASE_INACTIVE);
            outgoing.nbtTagCompound().removeTag(CreatureBoxRecovery.TAG);
            CreatureBoxRecovery.update(outgoing, now);
            if (entity instanceof RiftCreature creature) {
                creature.setCreatureInventory(inventory);
                creature.setDeploymentType(RiftCreatureEnums.CreatureDeployment.BASE_INACTIVE);
                creature.setDead();
            }
        }
        if (!incoming.nbtTagCompound().isEmpty()) {
            incoming = new CreatureNBT(incoming.nbtTagCompound().copy());
            incoming.setDeploymentType(RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE);
            incoming.nbtTagCompound().removeTag(CreatureBoxRecovery.TAG);
        }
        party.getCreatureStorage().setCreature(partyPosition.slot(), incoming);
        boxes.getBox(boxPosition.box()).setCreature(boxPosition.slot(), outgoing);
        party.set("Creatures", party.getCreatureStorage());
        boxes.set("Box" + boxPosition.box(), boxes.getBox(boxPosition.box()));
        return DepositResult.SUCCESS;
    }

    public static boolean withdrawCreature(@NotNull EntityPlayer player, int box, int slot, @NotNull UUID expectedCreature) {
        PlayerCreatureBoxProperties boxes = get(player);
        PlayerPartyProperties party = PlayerPartyProperties.get(player);
        if (player.world.isRemote || party == null || boxes == null || box < 0 || box >= BOX_COUNT || slot < 0 || slot >= BOX_SIZE) return false;
        CreatureStorage storage = boxes.getBox(box);
        CreatureNBT creature = storage.getCreature(slot);
        if (creature.nbtTagCompound().isEmpty() || !creature.getUniqueID().equals(expectedCreature) || !creature.isOwner(player)) return false;
        for (int partySlot = 0; partySlot < PlayerPartyProperties.MAX_SIZE; partySlot++) {
            if (!party.getCreatureStorage().getCreature(partySlot).nbtTagCompound().isEmpty()) continue;
            return swapCreatures(player, new StoragePosition(box, slot), new StoragePosition(-1, partySlot),
                    expectedCreature, null, false) == DepositResult.SUCCESS;
        }
        return false;
    }

    public static boolean releaseCreature(@NotNull EntityPlayer player, int box, int slot, @NotNull UUID expectedCreature) {
        PlayerCreatureBoxProperties boxes = get(player);
        if (player.world.isRemote || boxes == null || box < 0 || box >= BOX_COUNT || slot < 0 || slot >= BOX_SIZE) return false;
        CreatureStorage storage = boxes.getBox(box);
        CreatureNBT creature = storage.getCreature(slot);
        if (creature.nbtTagCompound().isEmpty() || !creature.getUniqueID().equals(expectedCreature)
                || !creature.isOwner(player)) return false;
        storage.removeCreature(slot);
        boxes.set("Box" + box, storage);
        return true;
    }

    public enum DepositResult {
        SUCCESS, NEEDS_CONFIRMATION, NO_SPACE, UNAVAILABLE, REVIVING
    }
}
