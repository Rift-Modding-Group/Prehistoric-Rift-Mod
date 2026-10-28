package anightdazingzoroark.prift.server.dataSerializers;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureChargeHelper.RushState;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureStatsStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.registries.DataSerializerEntry;
import org.jetbrains.annotations.NotNull;

public class RiftDataSerializers {
    public static final DataSerializer<RushState> RUSH_STATE = new DataSerializer<>() {
        @Override
        public void write(@NotNull PacketBuffer buf, @NotNull RushState value) {
            buf.writeString(value.move());
            buf.writeDouble(value.speedMultiplier());
            buf.writeVarInt(value.maxTicks());
            buf.writeFloat(value.yaw());
        }

        @Override
        @NotNull
        public RushState read(@NotNull PacketBuffer buf) {
            return new RushState(buf.readString(32767), buf.readDouble(), buf.readVarInt(), buf.readFloat());
        }

        @Override
        @NotNull
        public DataParameter<RushState> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        @NotNull
        public RushState copyValue(@NotNull RushState value) {
            return value;
        }
    };

    public static final DataSerializer<CreatureMoveStorage> CREATURE_MOVE_STORAGE = new DataSerializer<CreatureMoveStorage>() {
        @Override
        public void write(PacketBuffer buf, CreatureMoveStorage value) {
            NBTTagCompound nbtTagCompound = value.getAsSyncNBT();
            ByteBufUtils.writeTag(buf, nbtTagCompound);
        }

        @Override
        public CreatureMoveStorage read(PacketBuffer buf) {
            CreatureMoveStorage toReturn = new CreatureMoveStorage();
            NBTTagCompound nbtTagCompound = ByteBufUtils.readTag(buf);
            if (nbtTagCompound == null) return toReturn;
            toReturn.readFromSyncNBT(nbtTagCompound);
            return toReturn;
        }

        @Override
        public DataParameter<CreatureMoveStorage> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public CreatureMoveStorage copyValue(CreatureMoveStorage value) {
            return value;
        }
    };

    public static final DataSerializer<CreatureStatsStorage> CREATURE_STATS_STORAGE = new DataSerializer<CreatureStatsStorage>() {
        @Override
        public void write(PacketBuffer buf, CreatureStatsStorage value) {
            NBTTagCompound nbtTagCompound = value.getAsNBT();
            ByteBufUtils.writeTag(buf, nbtTagCompound);
        }

        @Override
        public CreatureStatsStorage read(PacketBuffer buf) {
            CreatureStatsStorage toReturn = new CreatureStatsStorage();
            NBTTagCompound nbtTagCompound = ByteBufUtils.readTag(buf);
            if (nbtTagCompound == null) return toReturn;
            toReturn.readFromNBT(nbtTagCompound);
            return toReturn;
        }

        @Override
        public DataParameter<CreatureStatsStorage> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public CreatureStatsStorage copyValue(CreatureStatsStorage value) {
            return value;
        }
    };

    public static final DataSerializer<CreatureAcquisitionInfo> ACQUISITION_INFO = new DataSerializer<CreatureAcquisitionInfo>() {
        @Override
        public void write(PacketBuffer buf, CreatureAcquisitionInfo value) {
            if (value == null) {
                buf.writeByte(-1);
                buf.writeLong(0L);
            }
            else {
                byte acquisitionMethodByte = value.acquisitionMethod() != null ? (byte) value.acquisitionMethod().ordinal() : (byte) -1;
                buf.writeByte(acquisitionMethodByte);
                buf.writeLong(value.acquisitionTime());
            }
        }

        @Override
        public CreatureAcquisitionInfo read(PacketBuffer buf) {
            byte methodByte = buf.readByte();
            CreatureAcquisitionInfo.AcquisitionMethod method = methodByte >= 0 ? CreatureAcquisitionInfo.AcquisitionMethod.values()[methodByte] : null;

            long acquisitionTime = buf.readLong();

            return new CreatureAcquisitionInfo(method, acquisitionTime);
        }

        @Override
        public DataParameter<CreatureAcquisitionInfo> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public CreatureAcquisitionInfo copyValue(CreatureAcquisitionInfo value) {
            return value;
        }
    };

    public static void registerSerializers() {
        ServerProxy.registryPrimer.register(new DataSerializerEntry(RUSH_STATE).setRegistryName(RiftInitialize.MODID, "rush_state"));
        ServerProxy.registryPrimer.register(new DataSerializerEntry(CREATURE_MOVE_STORAGE).setRegistryName(RiftInitialize.MODID, "move_storage"));
        ServerProxy.registryPrimer.register(new DataSerializerEntry(CREATURE_STATS_STORAGE)).setRegistryName(RiftInitialize.MODID, "stats_storage");
        ServerProxy.registryPrimer.register(new DataSerializerEntry(ACQUISITION_INFO).setRegistryName(RiftInitialize.MODID, "acquisition_info"));
    }
}
