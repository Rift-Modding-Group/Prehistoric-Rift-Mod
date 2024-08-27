package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftMakeNewParts extends AbstractMessage<RiftMakeNewParts> {
    private int creatureId;
    private int hitboxListLength;
    private int[] hitboxIdList;

    public RiftMakeNewParts() {}

    public RiftMakeNewParts(RiftCreature creature, RiftCreaturePart[] creatureParts) {
        this.creatureId = creature.getEntityId();
        this.hitboxListLength = creatureParts.length;
        this.hitboxIdList = new int[this.hitboxListLength];
        for (int x = 0; x < creatureParts.length; x++) {
            this.hitboxIdList[x] = creatureParts[x].getEntityId();
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.hitboxIdList = this.readIntArray(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.hitboxListLength);
        for (int id : this.hitboxIdList) buf.writeInt(id);
    }

    public int[] readIntArray(ByteBuf buf) {
        int length = buf.readInt();
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = buf.readInt();
        }
        return array;
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftMakeNewParts message, EntityPlayer entityPlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature)entityPlayer.world.getEntityByID(message.creatureId);
        RiftCreaturePart[] creatureParts = this.getCreatureParts(entityPlayer.world, message.hitboxIdList);
        creature.hitboxArray = creatureParts;
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftMakeNewParts message, EntityPlayer entityPlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature)entityPlayer.world.getEntityByID(message.creatureId);
        RiftCreaturePart[] creatureParts = this.getCreatureParts(entityPlayer.world, message.hitboxIdList);
        creature.hitboxArray = creatureParts;
    }

    private RiftCreaturePart[] getCreatureParts(World world, int[] intArray) {
        RiftCreaturePart[] toOutput = new RiftCreaturePart[intArray.length];
        for (int x = 0; x < intArray.length; x++) {
            toOutput[x] = (RiftCreaturePart)world.getEntityByID(intArray[x]);
        }
        return toOutput;
    }
}
