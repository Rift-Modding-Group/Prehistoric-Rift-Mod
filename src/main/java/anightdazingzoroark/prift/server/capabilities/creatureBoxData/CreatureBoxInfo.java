package anightdazingzoroark.prift.server.capabilities.creatureBoxData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

//helper class to store info involving creature box positions and owners
public class CreatureBoxInfo {
    public final BlockPos creatureBoxPos;
    public final String ownerName;
    public final UUID ownerUUID;

    public CreatureBoxInfo(BlockPos creatureBoxPos, EntityPlayer owner) {
        this.creatureBoxPos = creatureBoxPos;
        this.ownerName = owner.getName();
        this.ownerUUID = owner.getUniqueID();
    }

    public CreatureBoxInfo(NBTTagCompound nbtTagCompound) {
        this.creatureBoxPos = new BlockPos(
                nbtTagCompound.getInteger("BoxPosX"),
                nbtTagCompound.getInteger("BoxPosY"),
                nbtTagCompound.getInteger("BoxPosZ")
        );
        this.ownerName = nbtTagCompound.getString("OwnerName");
        this.ownerUUID = nbtTagCompound.getUniqueId("OwnerUUID");
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();

        toReturn.setInteger("BoxPosX", this.creatureBoxPos.getX());
        toReturn.setInteger("BoxPosY", this.creatureBoxPos.getY());
        toReturn.setInteger("BoxPosZ", this.creatureBoxPos.getZ());
        toReturn.setString("OwnerName", this.ownerName);
        toReturn.setUniqueId("OwnerUUID", this.ownerUUID);

        return toReturn;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (!(object instanceof CreatureBoxInfo)) return false;
        CreatureBoxInfo info = (CreatureBoxInfo) object;
        return this.creatureBoxPos.equals(info.creatureBoxPos) && this.ownerUUID.equals(info.ownerUUID);
    }
}
