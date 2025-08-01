package anightdazingzoroark.prift.server.entity;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CreatureAcquisitionInfo {
    public final AcquisitionMethod acquisitionMethod;
    public final long acquisitionTime;

    public CreatureAcquisitionInfo(AcquisitionMethod method, long acquisitionTime) {
        this.acquisitionMethod = method;
        this.acquisitionTime = acquisitionTime;
    }

    public CreatureAcquisitionInfo(NBTTagCompound nbt) {
        if (nbt == null || nbt.isEmpty()) {
            this.acquisitionMethod = null;
            this.acquisitionTime = 0L;
        }
        else {
            byte acquisitionMethodByte = nbt.getByte("AcquisitionMethod");
            this.acquisitionMethod = acquisitionMethodByte >= 0 ? AcquisitionMethod.values()[acquisitionMethodByte] : null;
            this.acquisitionTime = nbt.getLong("AcquisitionTime");
        }
    }

    public String acquisitionTimeString() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(this.acquisitionTime), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    public String acquisitionInfoString() {
        if (this.acquisitionTime <= 0L || this.acquisitionMethod == null) {
            return I18n.format("acquisition.unknown");
        }
        return I18n.format("acquisition."+this.acquisitionMethod.toString().toLowerCase(), this.acquisitionTimeString());
    }

    public NBTTagCompound getNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        //save acquisition method
        byte acquisitionMethodByte = this.acquisitionMethod != null ? (byte) this.acquisitionMethod.ordinal() : -1;
        toReturn.setByte("AcquisitionMethod", acquisitionMethodByte);
        //save acquisition time
        toReturn.setLong("AcquisitionTime", this.acquisitionTime);
        return toReturn;
    }

    public enum AcquisitionMethod {
        TAMED_FROM_WILD,
        HATCHED,
        BORN,
        PURCHASED,
        RECEIVED
    }
}
