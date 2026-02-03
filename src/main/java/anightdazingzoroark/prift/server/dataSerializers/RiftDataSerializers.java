package anightdazingzoroark.prift.server.dataSerializers;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.MoveListUtil;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.registries.DataSerializerEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiftDataSerializers {
    public static final DataSerializer<List<CreatureMove>> LIST_CREATURE_MOVE = new DataSerializer<List<CreatureMove>>() {
        public void write(PacketBuffer buf, List<CreatureMove> value) {
            NBTTagCompound listNBT = MoveListUtil.getNBTFromListCreatureMove(value);
            ByteBufUtils.writeTag(buf, listNBT);
        }

        public List<CreatureMove> read(PacketBuffer buf) throws IOException {
            NBTTagCompound listNBT = ByteBufUtils.readTag(buf);
            if (listNBT == null) return new ArrayList<>();
            return MoveListUtil.getListCreatureMoveFromNBT(listNBT);
        }

        public DataParameter<List<CreatureMove>> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        public List<CreatureMove> copyValue(List<CreatureMove> value) {
            return value;
        }
    };

    public static final DataSerializer<FixedSizeList<CreatureMove>> FIXED_SIZE_LIST_CREATURE_MOVE = new DataSerializer<FixedSizeList<CreatureMove>>() {
        @Override
        public void write(PacketBuffer buf, FixedSizeList<CreatureMove> value) {
            NBTTagCompound fixedSizeListNBT = MoveListUtil.getNBTFromFixedSizeListCreatureMove(value);
            ByteBufUtils.writeTag(buf, fixedSizeListNBT);
        }

        @Override
        public FixedSizeList<CreatureMove> read(PacketBuffer buf) throws IOException {
            NBTTagCompound fixedSizeListNBT = ByteBufUtils.readTag(buf);
            if (fixedSizeListNBT == null) return new FixedSizeList<>(0);
            return MoveListUtil.getFixedSizeListCreatureMoveFromNBT(fixedSizeListNBT);
        }

        @Override
        public DataParameter<FixedSizeList<CreatureMove>> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public FixedSizeList<CreatureMove> copyValue(FixedSizeList<CreatureMove> value) {
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
                byte acquisitionMethodByte = value.acquisitionMethod != null ? (byte) value.acquisitionMethod.ordinal() : (byte) -1;
                buf.writeByte(acquisitionMethodByte);
                buf.writeLong(value.acquisitionTime);
            }
        }

        @Override
        public CreatureAcquisitionInfo read(PacketBuffer buf) throws IOException {
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
        ServerProxy.registryPrimer.register(new DataSerializerEntry(LIST_CREATURE_MOVE).setRegistryName(RiftInitialize.MODID, "move_list"));
        ServerProxy.registryPrimer.register(new DataSerializerEntry(FIXED_SIZE_LIST_CREATURE_MOVE).setRegistryName(RiftInitialize.MODID, "fixed_size_move_list"));
        ServerProxy.registryPrimer.register(new DataSerializerEntry(ACQUISITION_INFO).setRegistryName(RiftInitialize.MODID, "acquisition_info"));
    }
}
