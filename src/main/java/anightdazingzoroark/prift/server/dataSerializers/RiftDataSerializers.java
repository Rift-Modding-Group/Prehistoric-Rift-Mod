package anightdazingzoroark.prift.server.dataSerializers;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraftforge.registries.DataSerializerEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiftDataSerializers {
    public static final DataSerializer<List<CreatureMove>> LIST_CREATURE_MOVE = new DataSerializer<List<CreatureMove>>() {
        public void write(PacketBuffer buf, List<CreatureMove> value) {
            buf.writeInt(value.size()); //for size
            for (CreatureMove i : value) buf.writeInt(i.ordinal());
        }

        public List<CreatureMove> read(PacketBuffer buf) throws IOException {
            int size = buf.readInt();
            List<CreatureMove> toReturn = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                toReturn.add(CreatureMove.values()[buf.readInt()]);
            }

            return toReturn;
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
            //for size
            buf.writeInt(value.size());
            //for contents
            for (CreatureMove i : value.getList()) {
                if (i != null) buf.writeInt(i.ordinal());
                else buf.writeInt(-1);
            }
        }

        @Override
        public FixedSizeList<CreatureMove> read(PacketBuffer buf) throws IOException {
            int size = buf.readInt();
            FixedSizeList<CreatureMove> toReturn = new FixedSizeList<>(size);
            for (int i = 0; i < size; i++) {
                int moveOrdinal = buf.readInt();
                if (moveOrdinal >= 0) toReturn.add(CreatureMove.values()[moveOrdinal]);
                else toReturn.add(null);
            }

            return toReturn;
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
