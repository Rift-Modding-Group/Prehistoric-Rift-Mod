package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftUpdateAllCreatureBoxDeployedMems extends RiftLibMessage<RiftUpdateAllCreatureBoxDeployedMems> {
    private int x, y, z;

    public RiftUpdateAllCreatureBoxDeployedMems() {}

    public RiftUpdateAllCreatureBoxDeployedMems(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftUpdateAllCreatureBoxDeployedMems message, EntityPlayer messagePlayer, MessageContext messageContext) {
        BlockPos creatureBoxPos = new BlockPos(message.x, message.y, message.z);

        //find creature box
        TileEntity te = messagePlayer.world.getTileEntity(creatureBoxPos);
        if (!(te instanceof RiftTileEntityCreatureBox)) return;
        RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) te;

        //create entity list
        for (int i = 0; i < teCreatureBox.getDeployedCreatures().size(); i++) {
            //get creature in the world
            CreatureNBT creatureNBT = teCreatureBox.getDeployedCreatures().get(i);
            RiftCreature creatureInWorld = creatureNBT.findCorrespondingCreature(messagePlayer.world);
            if (creatureInWorld == null) continue;

            //get nbt from said creature
            CreatureNBT newCreatureNBT = new CreatureNBT(creatureInWorld);

            //now replace the old data
            teCreatureBox.setCreatureInPos(i, newCreatureNBT);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftUpdateAllCreatureBoxDeployedMems message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
