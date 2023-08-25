package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.client.ClientProxy;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.entity.ai.RiftAggressiveModeGetTargets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.List;

public class RiftCreature extends EntityTameable implements IAnimatable {
    public static final DataParameter<Boolean> APEX = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    public final RiftCreatureType creatureType;
    public AnimationFactory factory = new AnimationFactory(this);
    public TameStatusType tameStatus;
    public TameBehaviorType tameBehavior;
    public final RiftAggressiveModeGetTargets getAggressiveModeTargets = new RiftAggressiveModeGetTargets(this, true);
    public final EntityAIOwnerHurtByTarget defendOwner =  new EntityAIOwnerHurtByTarget(this);
    public final EntityAIOwnerHurtTarget attackForOwner = new EntityAIOwnerHurtTarget(this);
    public boolean isRideable;
    public List<RiftTameRadialChoice> radialChoices;
    public int radialChoiceMenu;

    public RiftCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn);
        this.creatureType = creatureType;
        this.tameStatus = TameStatusType.STAND;
        this.tameBehavior = TameBehaviorType.ASSIST;
        this.radialChoices = RiftTameRadius.getMain();
        this.radialChoiceMenu = 0; //0 is main, 1 is state, 2 is behavior
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACKING, Boolean.valueOf(false));
        this.dataManager.register(VARIANT, Integer.valueOf(rand.nextInt(4)));
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));

        if (flag) {
            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    @Override
    protected boolean canDespawn() {
        return !this.isTamed();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (this.getOwnerId().equals(player.getUniqueID())) {
            if (this.isFavoriteFood(itemstack) && this.isChild() && this.getHealth() == this.getMaxHealth()) {
                this.consumeItemFromStack(player, itemstack);
                this.ageUp((int)((float)(-this.getGrowingAge() / 20) * 0.1F), true);
            }
            else if (this.isFavoriteFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                this.consumeItemFromStack(player, itemstack);
                this.heal((float)((ItemFood)itemstack.getItem()).getHealAmount(itemstack) * 3F);
            }
            else if (itemstack.isEmpty()) {
                ClientProxy.CREATURE = this;
                player.openGui(RiftInitialize.instance, ServerProxy.GUI_DIAL, world, (int) posX, (int) posY, (int) posZ);
            }
//            else if (itemstack.isEmpty() && player.isSneaking()) {
//                if (!this.world.isRemote) {
//                    this.getNavigator().clearPath();
//                    this.tameStatus = this.tameStatus.next();
//                    this.sendTameStatusMessage(this.tameStatus);
//                }
//            }
//            else if (itemstack.getItem().equals(RiftItems.COMMAND_STAFF) && player.isSneaking() && !this.isChild()) {
//                if (!this.world.isRemote) {
//                    this.setAttackTarget((EntityLivingBase)null);
//                    this.tameBehavior = this.tameBehavior.next();
//                    this.sendTameBehaviorMessage(this.tameBehavior);
//                }
//            }
//            else if (!this.isFavoriteFood(itemstack) && !player.isSneaking() && !this.isChild()) {
//                if (!this.world.isRemote && this.isRideable) {
//                    System.out.println("Rideableee!!!");
//                }
//            }
            return true;
        }
        return false;
    }

    public boolean isFavoriteFood(ItemStack stack) {
        return false;
    }

    private void sendTameStatusMessage(TameStatusType tameStatus) {
        String tameStatusName = "tame_status."+tameStatus.name().toLowerCase();
        ITextComponent itextcomponent = new TextComponentString(this.getName());
        if (this.getOwner() instanceof EntityPlayer) {
            ((EntityPlayer) this.getOwner()).sendStatusMessage(new TextComponentTranslation(tameStatusName, itextcomponent), false);
        }
    }

    private void sendTameBehaviorMessage(TameBehaviorType tameBehavior) {
        String tameBehaviorName = "tame_behavior."+tameBehavior.name().toLowerCase();
        ITextComponent itextcomponent = new TextComponentString(this.getName());
        if (this.getOwner() instanceof EntityPlayer) {
            ((EntityPlayer) this.getOwner()).sendStatusMessage(new TextComponentTranslation(tameBehaviorName, itextcomponent), false);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
        compound.setByte("TameStatus", (byte) this.tameStatus.ordinal());
        compound.setByte("TameBehavior", (byte) this.tameBehavior.ordinal());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
        if (compound.hasKey("TameStatus")) this.setTameStatus(TameStatusType.values()[compound.getByte("TameStatus")]);
        if (compound.hasKey("TameBehavior")) this.setTameBehavior(TameBehaviorType.values()[compound.getByte("TameBehavior")]);
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT).intValue();
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }


    public boolean isAttacking() {
        return this.dataManager.get(ATTACKING);
    }

    public void setAttacking(boolean value) {
        this.dataManager.set(ATTACKING, Boolean.valueOf(value));
    }

    public void setTameStatus(TameStatusType tameStatus) {
        this.tameStatus = tameStatus;
    }

    public void setTameBehavior(TameBehaviorType tameBehavior) {
        this.tameBehavior = tameBehavior;
    }

    public boolean isApexPredator() {
        return this.dataManager.get(APEX).booleanValue();
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public void registerControllers(AnimationData data) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
