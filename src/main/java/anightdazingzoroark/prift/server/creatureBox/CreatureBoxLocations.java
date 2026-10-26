package anightdazingzoroark.prift.server.creatureBox;

import anightdazingzoroark.prift.server.block.RiftBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Persist claims independently of loaded tile entities. Each dimension owns its own index.
 * */
public class CreatureBoxLocations extends WorldSavedData {
    private static final String NAME = "prift_creature_box_locations";
    private final Set<BlockPos> positions = new HashSet<>();

    public CreatureBoxLocations() {
        super(NAME);
    }

    public CreatureBoxLocations(String name) {
        super(name);
    }

    @NotNull
    public static CreatureBoxLocations get(@NotNull World world) {
        CreatureBoxLocations locations = (CreatureBoxLocations) world.getPerWorldStorage().getOrLoadData(CreatureBoxLocations.class, NAME);
        if (locations == null) {
            locations = new CreatureBoxLocations();
            world.getPerWorldStorage().setData(NAME, locations);
        }
        return locations;
    }

    public void setPresent(@NotNull BlockPos position, boolean present) {
        boolean changed;
        if (present) changed = this.positions.add(position.toImmutable());
        else changed = this.positions.remove(position);
        if (changed) this.markDirty();
    }

    public static boolean overlaps(@NotNull World world, @NotNull BlockPos position) {
        CreatureBoxLocations locations = get(world);
        if (locations.positions.removeIf(existing -> world.isBlockLoaded(existing)
                && world.getBlockState(existing).getBlock() != RiftBlocks.CREATURE_BOX)) locations.markDirty();
        AxisAlignedBB range = CreatureBoxTileEntity.getRange(position);
        return locations.positions.stream().anyMatch(existing -> !existing.equals(position)
                && range.intersects(CreatureBoxTileEntity.getRange(existing)));
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        this.positions.clear();
        NBTTagList boxes = compound.getTagList("Boxes", 10);
        for (int index = 0; index < boxes.tagCount(); index++) this.positions.add(BlockPos.fromLong(boxes.getCompoundTagAt(index).getLong("Position")));
    }

    @Override
    @NotNull
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        NBTTagList boxes = new NBTTagList();
        this.positions.forEach(position -> {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setLong("Position", position.toLong());
            boxes.appendTag(entry);
        });
        compound.setTag("Boxes", boxes);
        return compound;
    }
}
