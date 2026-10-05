package anightdazingzoroark.prift.server.properties;

import anightdazingzoroark.prift.server.entity.RiftDamage;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.AbstractEntityProperties;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.RiftLibProperty;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.BooleanPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.DoublePropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.IntegerPropertyValue;
import anightdazingzoroark.riftlib.util.MiscUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class OtherEntityProperties extends AbstractEntityProperties<EntityLivingBase> {
    public static final String PROPERTY_NAME = "OtherProperties";

    public OtherEntityProperties(@NotNull String propertyName, @NonNull EntityLivingBase entityHolder) {
        super(propertyName, entityHolder);
    }

    @Nullable
    public static OtherEntityProperties get(@Nullable EntityLivingBase entity) {
        if (entity == null) return null;
        return RiftLibProperty.getProperty(PROPERTY_NAME, entity);
    }

    @Override
    protected void registerDefaults(@NotNull EntityLivingBase entityLivingBase) {
        this.register(new BooleanPropertyValue("StinkBombed", false), false);
        this.register(new BooleanPropertyValue("HasStinkBombGoal", false), false);
        this.register(new IntegerPropertyValue("BleedingStrength", 0));
        this.register(new IntegerPropertyValue("BleedingDuration", 0));
    }

    @Override
    public void onTickProperty() {
        if (this.getEntityHolder().world.isRemote) return;

        //tick bleeding
        int bleedingDuration = this.get("BleedingDuration");
        if (this.isBleeding()) {
            //deal damage based on if entity was moving horizontally
            if (bleedingDuration % 20 == 0) {
                float damage = ((int) this.get("BleedingStrength") + 1) * (MiscUtils.getEntityHorizontalSpeed(this.getEntityHolder()) > 0 ? 2 : 1);
                this.getEntityHolder().attackEntityFrom(RiftDamage.RIFT_BLEED, damage);
            }

            //tick down
            this.set("BleedingDuration", bleedingDuration - 1);
        }
    }

    //-----stink bombing-----
    public boolean isStinkBombed() {
        return this.get("StinkBombed");
    }

    public void setStinkBombed(boolean value) {
        this.set("StinkBombed", value);
    }

    public boolean hasStinkBombGoal() {
        return this.get("HasStinkBombGoal");
    }

    public void setHasStinkBombGoal(boolean value) {
        this.set("HasStinkBombGoal", value, false);
    }

    //-----bleeding-----
    public boolean isBleeding() {
        return (int) this.get("BleedingDuration") > 0;
    }

    public void setBleeding(int duration) {
        this.setBleeding(0, duration);
    }

    public void setBleeding(int strength, int duration) {
        this.set("BleedingStrength", strength);
        this.set("BleedingDuration", duration);
    }
}