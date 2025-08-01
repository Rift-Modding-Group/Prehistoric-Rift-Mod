package anightdazingzoroark.prift.server.dataSerializers;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveCondition;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveConditionStack;
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

    public static final DataSerializer<CreatureMoveConditionStack> MOVE_CONDITION_STACK = new DataSerializer<CreatureMoveConditionStack>() {
        @Override
        public void write(PacketBuffer buf, CreatureMoveConditionStack value) {
            buf.writeInt(value.getConditions().size());
            for (CreatureMoveCondition.Condition c : value.getConditions())
                buf.writeInt(c.ordinal());
        }

        @Override
        public CreatureMoveConditionStack read(PacketBuffer buf) throws IOException {
            CreatureMoveConditionStack toReturn = new CreatureMoveConditionStack();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                toReturn.addCondition(CreatureMoveCondition.Condition.values()[buf.readInt()]);
            }
            return toReturn;
        }

        @Override
        public DataParameter<CreatureMoveConditionStack> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public CreatureMoveConditionStack copyValue(CreatureMoveConditionStack value) {
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
        ServerProxy.registryPrimer.register(new DataSerializerEntry(MOVE_CONDITION_STACK).setRegistryName(RiftInitialize.MODID, "move_condition_stack"));
        ServerProxy.registryPrimer.register(new DataSerializerEntry(ACQUISITION_INFO).setRegistryName(RiftInitialize.MODID, "acquisition_info"));
    }
}
