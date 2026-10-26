package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums.CreatureDeployment;
import anightdazingzoroark.prift.server.block.RiftBlocks;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxTileEntity;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureBoxRecovery;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Helper for transferring creatures between storage types on le server
 * */
public class CreatureStorageTransfer {
    /**
     * What happens after an attempted transfer
     * */
    public enum Result {
        SUCCESS,
        NEEDS_CONFIRMATION,
        NO_SPACE,
        UNAVAILABLE,
        REVIVING,
        NO_DEPLOYMENT_SPACE
    }

    /**
     * Helper for defining a slot and its associated CreatureStorage
     * */
    public record Slot(@NotNull String identifier, @Nullable BlockPos blockPosition, int index) {
        public Slot {
            Objects.requireNonNull(identifier, "Storage identifier must not be null");
            if (blockPosition != null) blockPosition = blockPosition.toImmutable();
        }

        @NotNull
        public static Slot party(int index) {
            return new Slot(PlayerPartyProperties.STORAGE_IDENTIFIER, null, index);
        }

        @NotNull
        public static Slot box(int box, int index) {
            return new Slot(PlayerCreatureBoxProperties.getBoxIdentifier(box), null, index);
        }

        @NotNull
        public static Slot deployed(@NotNull BlockPos position, int index) {
            return new Slot(CreatureBoxTileEntity.STORAGE_IDENTIFIER, position, index);
        }

        /**
         * Keeps block ownership only for a storage belonging to a Creature Box block.
         * */
        @NotNull
        public static Slot fromStorageId(@NotNull String identifier, int index, @NotNull BlockPos position) {
            return new Slot(identifier, CreatureBoxTileEntity.STORAGE_IDENTIFIER.equals(identifier) ? position : null, index);
        }

        public boolean isBox() {
            return PlayerCreatureBoxProperties.getBoxIndex(this.identifier) >= 0;
        }
    }

    private record Endpoint(
            @NotNull Slot slot, @NotNull CreatureStorage storage,
            @NotNull Consumer<CreatureStorage> save
    ) {}

    /**
     * Carries information about the resulting transfer alongside
     * miscellaneous data associated with the result
     * */
    private record Transfer(
            @NotNull Result result, @NotNull CreatureNBT creature,
            @Nullable RiftCreature entity, @Nullable Vec3d spawn, boolean newEntity,
            @Nullable RiftLibInventoryHandler droppedInventory
    ) {
        @NotNull
        private static Transfer rejected(@NotNull Result result) {
            return new Transfer(result, CreatureNBT.EMPTY_NBT, null, null, false, null);
        }
    }

    /**
     * Moves into an empty slot or exchanges occupied slots. Expected UUIDs bind confirmations to
     * both selected creatures. No slots, live creatures or inventories change until both moves
     * and every required spawn have been validated.
     */
    @NotNull
    public static Result swap(
            @NotNull EntityPlayer player, @NotNull Slot first, @NotNull Slot second,
            @NotNull UUID expectedFirst, @Nullable UUID expectedSecond, boolean confirmed
    ) {
        if (player.world.isRemote || !player.isEntityAlive() || first.equals(second)) return Result.UNAVAILABLE;
        Endpoint source = resolve(player, first);
        Endpoint target = resolve(player, second);
        if (source == null || target == null || source.storage() == target.storage() && first.index() == second.index()) {
            return Result.UNAVAILABLE;
        }
        CreatureNBT firstCreature = source.storage().getCreature(first.index());
        CreatureNBT secondCreature = target.storage().getCreature(second.index());
        if (firstCreature.nbtTagCompound().isEmpty() || !firstCreature.isOwner(player)
                || !firstCreature.getUniqueID().equals(expectedFirst)
        ) {
            return Result.UNAVAILABLE;
        }
        if (expectedSecond == null) {
            if (!secondCreature.nbtTagCompound().isEmpty()) return Result.UNAVAILABLE;
        }
        else if (secondCreature.nbtTagCompound().isEmpty() || !secondCreature.isOwner(player)
                || !secondCreature.getUniqueID().equals(expectedSecond)
                || expectedFirst.equals(expectedSecond)
        ) {
            return Result.UNAVAILABLE;
        }

        // Reordering a storage or moving between personal boxes does not change deployment state.
        if (source.storage() == target.storage() || first.isBox() && second.isBox()) {
            source.storage().swapWith(first.index(), target.storage(), second.index());
            source.save().accept(source.storage());
            if (source.storage() != target.storage()) target.save().accept(target.storage());
            if (PlayerPartyProperties.STORAGE_IDENTIFIER.equals(first.identifier())
                    && PlayerPartyProperties.STORAGE_IDENTIFIER.equals(second.identifier())
            ) {
                PlayerPartyProperties party = PlayerPartyProperties.get(player);
                if (party != null) {
                    int selected = party.getSelectedPosition();
                    if (selected == first.index()) party.set("SelectedPosition", second.index());
                    else if (selected == second.index()) party.set("SelectedPosition", first.index());
                }
            }
            return Result.SUCCESS;
        }

        long now = CreatureBoxRecovery.currentTime(player);
        Transfer outgoing = prepare(player, source, target, firstCreature, now, confirmed);
        Transfer incoming = prepare(player, target, source, secondCreature, now, confirmed);
        // A revival or unavailable creature must not prompt for an exchange which cannot complete.
        for (Result failure : new Result[]{Result.UNAVAILABLE, Result.REVIVING, Result.NO_DEPLOYMENT_SPACE, Result.NEEDS_CONFIRMATION}) {
            if (outgoing.result() == failure || incoming.result() == failure) return failure;
        }

        List<Entity> spawned = new ArrayList<>();
        for (Transfer transfer : new Transfer[]{outgoing, incoming}) {
            if (transfer.newEntity() && transfer.entity() != null) {
                if (!player.world.spawnEntity(transfer.entity())) {
                    spawned.forEach(Entity::setDead);
                    return Result.NO_DEPLOYMENT_SPACE;
                }
                spawned.add(transfer.entity());
            }
            RiftLibInventoryHandler inventory = transfer.droppedInventory();
            if (inventory == null) continue;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                EntityItem drop = new EntityItem(player.world, player.posX, player.posY, player.posZ, stack.copy());
                if (!player.world.spawnEntity(drop)) {
                    spawned.forEach(Entity::setDead);
                    return Result.UNAVAILABLE;
                }
                spawned.add(drop);
            }
        }

        CreatureNBT toTarget = finish(outgoing, second);
        CreatureNBT toSource = finish(incoming, first);
        source.storage().setCreature(first.index(), toSource);
        target.storage().setCreature(second.index(), toTarget);
        source.save().accept(source.storage());
        target.save().accept(target.storage());
        return Result.SUCCESS;
    }

    @Nullable
    private static Endpoint resolve(@NotNull EntityPlayer player, @NotNull Slot slot) {
        if (slot.index() < 0 || slot.blockPosition() != null
                && !CreatureBoxTileEntity.STORAGE_IDENTIFIER.equals(slot.identifier())
        ) {
            return null;
        }
        Endpoint endpoint;
        switch (slot.identifier()) {
            case PlayerPartyProperties.STORAGE_IDENTIFIER -> {
                PlayerPartyProperties party = PlayerPartyProperties.get(player);
                if (party == null) return null;
                endpoint = new Endpoint(slot, party.getCreatureStorage(), storage -> party.set(storage.getIdentifier(), storage));
            }
            case CreatureBoxTileEntity.STORAGE_IDENTIFIER -> {
                BlockPos position = slot.blockPosition();
                if (position == null || player.getDistanceSqToCenter(position) > 64D || !player.world.isBlockLoaded(position)
                        || player.world.getBlockState(position).getBlock() != RiftBlocks.CREATURE_BOX
                        || !(player.world.getTileEntity(position) instanceof CreatureBoxTileEntity tile)) return null;
                tile.refreshCreatures();
                endpoint = new Endpoint(slot, tile.getDeployedCreatures(), tile::setDeployedCreatures);
            }
            default -> {
                PlayerCreatureBoxProperties boxes = PlayerCreatureBoxProperties.get(player);
                int box = PlayerCreatureBoxProperties.getBoxIndex(slot.identifier());
                if (boxes == null || box < 0) return null;
                boxes.updateBox(box, true);
                endpoint = new Endpoint(slot, boxes.getBox(box), storage -> boxes.set(storage.getIdentifier(), storage));
            }
        }
        return slot.identifier().equals(endpoint.storage().getIdentifier())
                && slot.index() < endpoint.storage().getSize() ? endpoint : null;
    }

    @NotNull
    private static Transfer prepare(
            @NotNull EntityPlayer player, @NotNull Endpoint source, @NotNull Endpoint target,
            @NotNull CreatureNBT stored, long now, boolean confirmed
    ) {
        if (stored.nbtTagCompound().isEmpty()) return new Transfer(Result.SUCCESS, CreatureNBT.EMPTY_NBT, null, null, false, null);
        Entity found = RiftUtil.getEntityWithUUID(player.world, stored.getUniqueID());
        RiftCreature live = null;
        CreatureNBT snapshot;
        if (found instanceof RiftCreature creature) {
            if (source.slot().isBox() || !creature.isOwner(player) || creature.isDead
                    || creature.isBeingRidden() || creature.isRiding()) return Transfer.rejected(Result.UNAVAILABLE);
            BlockPos home = source.slot().blockPosition();
            if (CreatureBoxTileEntity.STORAGE_IDENTIFIER.equals(source.slot().identifier())
                    && (home == null || !home.equals(creature.getCreatureBoxHome()))) return Transfer.rejected(Result.UNAVAILABLE);
            if (PlayerPartyProperties.STORAGE_IDENTIFIER.equals(source.slot().identifier())
                    && creature.getCreatureBoxHome() != null
            ) {
                return Transfer.rejected(Result.UNAVAILABLE);
            }
            live = creature;
            NBTTagCompound tag = new NBTTagCompound();
            live.writeToNBT(tag);
            snapshot = new CreatureNBT(tag);
        }
        else {
            if (stored.getDeploymentType() == CreatureDeployment.PARTY || stored.getDeploymentType() == CreatureDeployment.BASE) {
                return Transfer.rejected(Result.UNAVAILABLE);
            }
            snapshot = new CreatureNBT(stored.nbtTagCompound().copy());
        }
        if (!target.slot().isBox() && CreatureBoxRecovery.remainingSeconds(snapshot, now) > 0) {
            return Transfer.rejected(Result.REVIVING);
        }

        RiftLibInventoryHandler droppedInventory = null;
        Vec3d spawn = null;
        boolean newEntity = false;
        switch (target.storage().getIdentifier()) {
            case PlayerPartyProperties.STORAGE_IDENTIFIER -> {
                snapshot.setDeploymentType(CreatureDeployment.PARTY_INACTIVE);
                snapshot.nbtTagCompound().removeTag(CreatureBoxTileEntity.HOME_TAG);
                snapshot.nbtTagCompound().removeTag(CreatureBoxRecovery.TAG);
            }
            case CreatureBoxTileEntity.STORAGE_IDENTIFIER -> {
                if (snapshot.getHealth() <= 0f || snapshot.getCreatureType() == null || target.slot().blockPosition() == null) {
                    return Transfer.rejected(Result.UNAVAILABLE);
                }
                if (live == null) {
                    live = RiftCreatureRegistry.createCreature(player.world, snapshot.getCreatureType().getName());
                    live.readFromNBT(snapshot.nbtTagCompound().copy());
                    newEntity = true;
                }
                spawn = CreatureBoxTileEntity.findDeploymentPosition(live, target.slot().blockPosition());
                if (spawn == null) return Transfer.rejected(Result.NO_DEPLOYMENT_SPACE);
                if (newEntity) {
                    live.setDeploymentType(CreatureDeployment.BASE);
                    live.setCreatureBoxHome(target.slot().blockPosition());
                    live.setPosition(spawn.x, spawn.y, spawn.z);
                }
            }
            default -> {
                droppedInventory = snapshot.getCreatureInventory();
                if (droppedInventory == null) return Transfer.rejected(Result.UNAVAILABLE);
                if (!droppedInventory.isEmpty() && !confirmed) return Transfer.rejected(Result.NEEDS_CONFIRMATION);
                snapshot.setDeploymentType(CreatureDeployment.BASE_INACTIVE);
                snapshot.nbtTagCompound().removeTag(CreatureBoxTileEntity.HOME_TAG);
                snapshot.nbtTagCompound().removeTag(CreatureBoxRecovery.TAG);
                CreatureBoxRecovery.update(snapshot, now);
            }
        }
        return new Transfer(Result.SUCCESS, snapshot, live, spawn, newEntity, droppedInventory);
    }

    @NotNull
    private static CreatureNBT finish(@NotNull Transfer transfer, @NotNull Slot destination) {
        if (transfer.creature().nbtTagCompound().isEmpty()) return CreatureNBT.EMPTY_NBT;
        RiftCreature live = transfer.entity();
        if (CreatureBoxTileEntity.STORAGE_IDENTIFIER.equals(destination.identifier()) && live != null && transfer.spawn() != null) {
            live.setDeploymentType(CreatureDeployment.BASE);
            live.setCreatureBoxHome(destination.blockPosition());
            live.getNavigator().clearPath();
            live.getCreatureMoveHelper().stopMovement();
            live.setAttackTarget(null);
            live.setPositionAndUpdate(transfer.spawn().x, transfer.spawn().y, transfer.spawn().z);
            NBTTagCompound tag = new NBTTagCompound();
            live.writeToNBT(tag);
            tag.removeTag(CreatureBoxRecovery.TAG);
            return new CreatureNBT(tag);
        }

        RiftLibInventoryHandler inventory = transfer.droppedInventory();
        if (inventory != null) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                inventory.extractItem(slot, inventory.getStackInSlot(slot).getCount(), false);
            }
            transfer.creature().setCreatureInventory(inventory);
            if (live != null) live.setCreatureInventory(inventory);
        }
        if (live != null) {
            live.setDeploymentType(transfer.creature().getDeploymentType());
            live.setCreatureBoxHome(null);
            live.setDead();
        }
        return transfer.creature();
    }
}
