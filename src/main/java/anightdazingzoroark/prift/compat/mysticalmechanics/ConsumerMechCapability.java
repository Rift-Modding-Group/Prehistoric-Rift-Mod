package anightdazingzoroark.prift.compat.mysticalmechanics;

import mysticalmechanics.api.IMechCapability;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class ConsumerMechCapability implements IMechCapability {
    public double[] power = new double[6];
    double maxPower;
    boolean dirty = true;
    boolean additive = false; //Whether power input will be added together or the largest will be chosen

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public double getPower(EnumFacing enumFacing) {
        if (enumFacing == null) {
            if (this.dirty) {
                this.recalculateMax();
                this.dirty = false;
            }
            return maxPower;
        }
        return this.power[enumFacing.getIndex()];
    }

    private void recalculateMax() {
        this.maxPower = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
            double power = getPower(facing);
            if (this.additive) this.maxPower += power;
            else this.maxPower = Math.max(power, this.maxPower);
        }
    }

    @Override
    public void setPower(double value, EnumFacing enumFacing) {
        if (enumFacing == null) {
            for (int i = 0; i < 6; i++) this.power[i] = value;
        }
        else {
            double oldPower = power[enumFacing.getIndex()];
            this.power[enumFacing.getIndex()] = value;
            if (oldPower != value) {
                this.dirty = true;
                this.onPowerChange();
            }
        }
    }

    @Override
    public void onPowerChange() {

    }

    @Override
    public boolean isOutput(EnumFacing from) {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        for (int i = 0; i < 6; i++) {
            tag.setDouble("mech_power" + i, power[i]);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        for (int i = 0; i < 6; i++) {
            power[i] = tag.getDouble("mech_power" + i);
        }
        this.markDirty();
    }
}
