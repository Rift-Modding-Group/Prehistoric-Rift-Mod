package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.config.ApatosaurusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Apatosaurus extends RiftCreature {
    private static final DataParameter<Byte> WEAPON = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BYTE);

    public Apatosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.APATOSAURUS);
        this.minCreatureHealth = ApatosaurusConfig.getMinHealth();
        this.maxCreatureHealth = ApatosaurusConfig.getMaxHealth();
        this.setSize(3f, 3f);
        this.favoriteFood = ApatosaurusConfig.apatosaurusFavoriteFood;
        this.tamingFood = ApatosaurusConfig.apatosaurusBreedingFood;
        this.experienceValue = 50;
        this.speed = 0.10D;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(WEAPON, (byte)0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ApatosaurusConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {

    }

    @Override
    public boolean hasLeftClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return false;
    }

    public int getWeapon() {
        return this.dataManager.get(WEAPON);
    }

    public void setWeapon(int value) {
        this.dataManager.set(WEAPON, (byte)value);
    }

    @Override
    public void registerControllers(AnimationData data) {

    }
}
