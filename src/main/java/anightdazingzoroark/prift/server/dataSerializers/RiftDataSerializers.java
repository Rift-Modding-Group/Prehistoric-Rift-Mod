package anightdazingzoroark.prift.server.dataSerializers;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraftforge.registries.DataSerializerEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiftDataSerializers {
    public static final DataSerializer<List<CreatureMove>> LIST_CREATURE_MOVE = new DataSerializer<List<CreatureMove>>() {
        public void write(PacketBuffer buf, List<CreatureMove> value) {
            buf.writeVarInt(value.size()); //for size
            for (CreatureMove i : value) buf.writeInt(i.ordinal());
        }
        public List<CreatureMove> read(PacketBuffer buf) throws IOException {
            int size = buf.readVarInt();
            List<CreatureMove> toReturn = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                toReturn.add(CreatureMove.values()[buf.readVarInt()]);
            }

            return toReturn;
        }
        public DataParameter<List<CreatureMove>> createKey(int id) {
            return new DataParameter<List<CreatureMove>>(id, this);
        }
        public List<CreatureMove> copyValue(List<CreatureMove> value) {
            return value;
        }
    };

    public static void registerSerializers() {
        ServerProxy.registryPrimer.register(new DataSerializerEntry(LIST_CREATURE_MOVE).setRegistryName(RiftInitialize.MODID, "int_list"));
    }
}
