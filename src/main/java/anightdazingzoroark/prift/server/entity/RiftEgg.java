package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.List;

public class RiftEgg extends EntityTameable implements IAnimatable {
    private static final DataParameter<Integer> HATCH_TIME = EntityDataManager.<Integer>createKey(RiftEgg.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> EGG_TYPE = EntityDataManager.createKey(RiftEgg.class, DataSerializers.BYTE);

    public AnimationFactory factory = new AnimationFactory(this);

    public RiftEgg(World worldIn) {
        super(worldIn);
        this.setSize(1F, 1F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HATCH_TIME, 20);
        this.dataManager.register(EGG_TYPE, (byte) RiftCreatureType.TYRANNOSAURUS.ordinal());
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        this.setHatchTime(this.getHatchTime() - 1);
        if (this.getHatchTime() == 0) {
            RiftCreature creature = this.getCreatureType().invokeClass(this.world);
            creature.setHealth((float) creature.minCreatureHealth);
            creature.setAgeInDays(0);

            if (this.getOwnerId() != null) {
                creature.setTamed(true);
                creature.setOwnerId(this.getOwnerId());
                creature.setTameStatus(TameStatusType.SIT);
                creature.setTameBehavior(TameBehaviorType.PASSIVE);
            }

            creature.setLocationAndAngles(Math.floor(this.posX), Math.floor(this.posY) + 1, Math.floor(this.posZ), this.world.rand.nextFloat() * 360.0F, 0.0F);
            if (!this.world.isRemote) {
                List<EntityPlayer> nearby = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.posX - 50.0, this.posY - 50.0, this.posZ - 50.0, this.posX + 50.0, this.posY + 50.0, this.posZ + 50.0));
                for (EntityPlayer player : nearby) {
                    if (player.getUniqueID().equals(this.getOwnerId())) player.sendStatusMessage(new TextComponentTranslation("prift.notify.egg_hatched"), false);
                }
                this.world.spawnEntity(creature);
            }
            this.setDead();
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            if (this.getCreatureType() == RiftCreatureType.DODO) {
                ItemStack eggStack = new ItemStack(this.getCreatureType().eggItem);
                if (!player.capabilities.isCreativeMode) player.inventory.addItemStackToInventory(eggStack);
                this.setDead();
            }
            else {
                if (this.getOwnerId().equals(player.getUniqueID())) {
                    ItemStack eggStack = new ItemStack(this.getCreatureType().eggItem);
                    if (!player.capabilities.isCreativeMode) player.inventory.addItemStackToInventory(eggStack);
                    this.setDead();
                    return true;
                }
                else {
                    ITextComponent itextcomponent = new TextComponentString(this.getOwner().getName());
                    player.sendStatusMessage(new TextComponentTranslation("reminder.not_egg_owner", itextcomponent), false);
                }
            }
            return true;
        }
        else {
            try {
                if (this.getOwnerId().equals(player.getUniqueID())) {
                    ClientProxy.EGG = this;
                    player.openGui(RiftInitialize.instance, ServerProxy.GUI_EGG, world, (int) posX, (int) posY, (int) posZ);
                    return true;
                }
            }
            catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        this.setDead();
        return super.hitByEntity(entityIn);
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("HatchTime", this.getHatchTime());
        compound.setByte("CreatureType", (byte)this.getCreatureType().ordinal());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setHatchTime(compound.getInteger("HatchTime"));
        if (compound.hasKey("CreatureType")) this.setCreatureType(RiftCreatureType.values()[compound.getByte("CreatureType")]);
    }

    public int getHatchTime() {
        return this.dataManager.get(HATCH_TIME).intValue();
    }

    public void setHatchTime(int time) {
        this.dataManager.set(HATCH_TIME, time);
    }

    public int[] getHatchTimeMinutes() {
        int minutes = (int)((float)this.getHatchTime() / 1200F);
        int seconds = (int)((float)this.getHatchTime() / 20F);
        seconds = seconds - (minutes * 60);
        return new int[]{minutes, seconds};
    }

    public RiftCreatureType getCreatureType() {
        return RiftCreatureType.values()[this.dataManager.get(EGG_TYPE).byteValue()];
    }

    public void setCreatureType(RiftCreatureType type) {
        this.dataManager.set(EGG_TYPE, (byte) type.ordinal());
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
