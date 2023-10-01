package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.client.ClientProxy;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
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
import net.minecraft.world.DifficultyInstance;
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
            creature.setHealth((float) this.getCreatureType().getMinHealth());
            creature.setAgeInDays(0);
            creature.setTamed(true);
            creature.setOwnerId(this.getOwnerId());
            creature.setTameStatus(TameStatusType.SIT);
            creature.setTameBehavior(TameBehaviorType.PASSIVE);
            creature.setLocationAndAngles(Math.floor(this.posX), Math.floor(this.posY) + 1, Math.floor(this.posZ), this.world.rand.nextFloat() * 360.0F, 0.0F);
            if (!this.world.isRemote) {
                List<EntityPlayer> nearby = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.posX - 50.0, this.posY - 50.0, this.posZ - 50.0, this.posX + 50.0, this.posY + 50.0, this.posZ + 50.0));
                for (EntityPlayer player : nearby) {
                    if (player.getUniqueID().equals(this.getOwnerId())) player.sendStatusMessage(new TextComponentTranslation("rift.notify.egg_hatched"), false);
                }
                this.world.spawnEntity(creature);
            }
            this.setDead();
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (this.getOwnerId().equals(player.getUniqueID())) {
            if (player.isSneaking()) {
                ItemStack eggStack = new ItemStack(this.getCreatureType().eggItem);
                if (!player.capabilities.isCreativeMode) player.inventory.addItemStackToInventory(eggStack);
                this.setDead();
            }
            else {
                ClientProxy.EGG = this;
                player.openGui(RiftInitialize.instance, ServerProxy.GUI_EGG, world, (int) posX, (int) posY, (int) posZ);
            }
            return true;
        }
        else {
            ITextComponent itextcomponent = new TextComponentString(this.getOwner().getName());
            player.sendStatusMessage(new TextComponentTranslation("reminder.not_egg_owner", itextcomponent), false);
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
