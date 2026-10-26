package anightdazingzoroark.prift.server.creatureBox;

import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.ObjectPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.tileEntity.RiftTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreatureBoxTileEntity extends RiftTileEntity implements ITickable {
    /**
     * Side length in 16 x 16 x 16 chunk sections, centered on the box's section.
     * */
    public static final int RANGE_CHUNKS = 5;
    public static final int CAPACITY = 20;
    public static final String STORAGE_IDENTIFIER = "DeployedCreatures";
    public static final String HOME_TAG = "CreatureBoxHome";
    private static final int DEPLOYMENT_RADIUS = 8;

    @Override
    public void registerValues() {
        this.registerValue(
                new ObjectPropertyValue<>(
                        STORAGE_IDENTIFIER, new CreatureStorage(STORAGE_IDENTIFIER, CAPACITY), CreatureStorage.class,
                        CreatureStorage::getAsNBT,
                        tag -> {
                            CreatureStorage storage = new CreatureStorage(STORAGE_IDENTIFIER, CAPACITY);
                            if (tag instanceof NBTTagCompound compound) storage.readFromNBT(compound);
                            return storage;
                        }
                )
        );
    }

    @NotNull
    public CreatureStorage getDeployedCreatures() { return this.getValue(STORAGE_IDENTIFIER); }

    public void setDeployedCreatures(@NotNull CreatureStorage storage) { this.setValue(storage.getIdentifier(), storage); }

    public boolean hasCreatures() {
        for (int slot = 0; slot < CAPACITY; slot++) {
            if (!this.getDeployedCreatures().getCreature(slot).nbtTagCompound().isEmpty()) return true;
        }
        return false;
    }

    @NotNull
    public static AxisAlignedBB getRange(@NotNull BlockPos position) {
        int offset = (Math.max(1, RANGE_CHUNKS) - 1) / 2;
        int minX = ((position.getX() >> 4) - offset) << 4;
        int minY = ((position.getY() >> 4) - offset) << 4;
        int minZ = ((position.getZ() >> 4) - offset) << 4;
        int side = Math.max(1, RANGE_CHUNKS) * 16;
        return new AxisAlignedBB(minX, minY, minZ, minX + side, minY + side, minZ + side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.world.isRemote) {
            CreatureBoxLocations.get(this.world).setPresent(this.pos, true);
            if (this.hasCreatures()) this.refreshCreatures();
        }
    }

    @Override
    public void update() {
        if (!this.world.isRemote && this.world.getTotalWorldTime() % 20 == 0) this.refreshCreatures();
    }

    public void refreshCreatures() {
        boolean changed = false;
        CreatureStorage storage = this.getDeployedCreatures();
        for (int slot = 0; slot < CAPACITY; slot++) {
            CreatureNBT stored = storage.getCreature(slot);
            if (stored.nbtTagCompound().isEmpty()) continue;
            Entity entity;
            if (this.world instanceof WorldServer serverWorld) entity = serverWorld.getEntityFromUuid(stored.getUniqueID());
            else entity = RiftUtil.getEntityWithUUID(this.world, stored.getUniqueID());
            if (!(entity instanceof RiftCreature creature) || creature.isDead || !this.pos.equals(creature.getCreatureBoxHome())) continue;
            NBTTagCompound snapshot = new NBTTagCompound();
            creature.writeToNBT(snapshot);
            if (!snapshot.equals(stored.nbtTagCompound())) {
                storage.setCreature(slot, new CreatureNBT(snapshot));
                changed = true;
            }
        }
        if (changed) this.setDeployedCreatures(storage);
    }

    public void updateCreature(@NotNull RiftCreature creature) {
        CreatureStorage storage = this.getDeployedCreatures();
        for (int slot = 0; slot < CAPACITY; slot++) {
            CreatureNBT stored = storage.getCreature(slot);
            if (stored.nbtTagCompound().isEmpty() || !stored.getUniqueID().equals(creature.getUniqueID())) continue;
            NBTTagCompound snapshot = new NBTTagCompound();
            creature.writeToNBT(snapshot);
            storage.setCreature(slot, new CreatureNBT(snapshot));
            this.setDeployedCreatures(storage);
            return;
        }
    }

    @Nullable
    public static Vec3d findDeploymentPosition(@NotNull RiftCreature creature, @NotNull BlockPos home) {
        AxisAlignedBB range = getRange(home);
        double halfWidth = creature.width * 0.5D;
        List<BlockPos> candidates = new ArrayList<>();
        for (int x = -DEPLOYMENT_RADIUS; x <= DEPLOYMENT_RADIUS; x++) {
            for (int z = -DEPLOYMENT_RADIUS; z <= DEPLOYMENT_RADIUS; z++) {
                // Keep the creature's entire footprint off the box, even at a different height.
                if (Math.abs(x) < halfWidth + 0.5D && Math.abs(z) < halfWidth + 0.5D) continue;
                candidates.add(home.add(x, 0, z));
            }
        }
        Collections.shuffle(candidates, creature.getRNG());
        for (BlockPos candidate : candidates) {
            for (int offset = 0; offset <= 8; offset++) {
                int y = (offset + 1) / 2 * (offset % 2 == 0 ? -1 : 1);
                BlockPos position = candidate.add(0, y, 0);
                double targetX = position.getX() + 0.5D;
                double targetZ = position.getZ() + 0.5D;
                if (!creature.world.isBlockLoaded(position) || targetX - halfWidth < range.minX || targetX + halfWidth > range.maxX
                        || position.getY() < range.minY || position.getY() + creature.height > range.maxY
                        || targetZ - halfWidth < range.minZ || targetZ + halfWidth > range.maxZ
                        || !creature.world.getBlockState(position.down()).getMaterial().isSolid()) continue;
                AxisAlignedBB body = creature.getEntityBoundingBox().offset(targetX - creature.posX,
                        position.getY() - creature.posY, targetZ - creature.posZ);
                if (creature.world.getCollisionBoxes(creature, body).isEmpty()
                        && !creature.world.containsAnyLiquid(body) && creature.world.checkNoEntityCollision(body, creature)) {
                    return new Vec3d(targetX, position.getY(), targetZ);
                }
            }
        }
        return null;
    }

}
