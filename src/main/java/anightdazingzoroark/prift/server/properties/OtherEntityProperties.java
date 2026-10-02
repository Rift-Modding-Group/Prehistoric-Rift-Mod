package anightdazingzoroark.prift.server.properties;

import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.AbstractEntityProperties;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.RiftLibProperty;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.BooleanPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.DoublePropertyValue;
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
        this.register(new BooleanPropertyValue("StinkBombed", false));
        this.register(new DoublePropertyValue("StinkBombX", 0D));
        this.register(new DoublePropertyValue("StinkBombY", 0D));
        this.register(new DoublePropertyValue("StinkBombZ", 0D));
        this.register(new BooleanPropertyValue("HasStinkBombGoal", false), false);
    }

    @Override
    public void onTickProperty() {}

    public boolean isStinkBombed() {
        return this.get("StinkBombed");
    }

    public void applyStinkBomb(@NotNull Vec3d origin) {
        this.set("StinkBombX", origin.x, false);
        this.set("StinkBombY", origin.y, false);
        this.set("StinkBombZ", origin.z, false);
        this.set("StinkBombed", true, false);
        this.syncToClientMultiple("StinkBombX", "StinkBombY", "StinkBombZ", "StinkBombed");
    }

    @NotNull
    public Vec3d getStinkBombOrigin() {
        return new Vec3d(this.get("StinkBombX"), this.get("StinkBombY"), this.get("StinkBombZ"));
    }

    public void clearStinkBomb() {
        this.set("StinkBombed", false);
    }

    public boolean hasStinkBombGoal() {
        return this.get("HasStinkBombGoal");
    }

    public void setHasStinkBombGoal(boolean value) {
        this.set("HasStinkBombGoal", value, false);
    }
}
