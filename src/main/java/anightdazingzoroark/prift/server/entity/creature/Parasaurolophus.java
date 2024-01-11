package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.config.ParasaurolophusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Parasaurolophus extends RiftCreature {
    public Parasaurolophus(World worldIn) {
        super(worldIn, RiftCreatureType.PARASAUROLOPHUS);
        this.minCreatureHealth = ParasaurolophusConfig.getMinHealth();
        this.maxCreatureHealth = ParasaurolophusConfig.getMaxHealth();
        this.setSize(2f, 2f);
        this.favoriteFood = ParasaurolophusConfig.parasaurolophusFavoriteFood;
        this.tamingFood = ParasaurolophusConfig.parasaurolophusTamingFood;
        this.experienceValue = 20;
        this.speed = 0.25D;
        this.attackWidth = 3.5f;
        this.saddleItem = ParasaurolophusConfig.parasaurolophusSaddleItem;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ParasaurolophusConfig.damage);
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

    @Override
    public void registerControllers(AnimationData data) {

    }
}
