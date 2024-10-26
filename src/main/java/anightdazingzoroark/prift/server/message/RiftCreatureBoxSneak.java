package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftCreatureBoxSneak extends AbstractMessage<RiftCreatureBoxSneak> {
    private int creatureBoxX;
    private int creatureBoxY;
    private int creatureBoxZ;

    public RiftCreatureBoxSneak() {}

    public RiftCreatureBoxSneak(BlockPos pos) {
        this.creatureBoxX = pos.getX();
        this.creatureBoxY = pos.getY();
        this.creatureBoxZ = pos.getZ();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureBoxX = buf.readInt();
        this.creatureBoxY = buf.readInt();
        this.creatureBoxZ = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureBoxX);
        buf.writeInt(this.creatureBoxY);
        buf.writeInt(this.creatureBoxZ);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftCreatureBoxSneak message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftCreatureBoxSneak message, EntityPlayer player, MessageContext messageContext) {
        BlockPos creatureBoxPos = new BlockPos(message.creatureBoxX, message.creatureBoxY, message.creatureBoxZ);
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) player.world.getTileEntity(creatureBoxPos);
        if (creatureBox != null) {
            //highlight all creatures deployed
            for (RiftCreature creature : creatureBox.getCreatures()) {
                if (creature != null) {
                    creature.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 100));
                }
            }
        }
    }
}
