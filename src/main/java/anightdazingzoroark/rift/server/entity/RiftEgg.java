package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.client.ClientProxy;
import anightdazingzoroark.rift.server.ServerProxy;
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
    public RiftCreatureType creatureType;
    private static final DataParameter<Integer> HATCH_TIME = EntityDataManager.<Integer>createKey(RiftEgg.class, DataSerializers.VARINT);
    public AnimationFactory factory = new AnimationFactory(this);

    public RiftEgg(World worldIn) {
        this(worldIn, RiftCreatureType.TYRANNOSAURUS);
    }

    public RiftEgg(World worldIn, RiftCreatureType creatureType) {
        super(worldIn);
        this.creatureType = creatureType;
        this.setSize(1F, 1F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HATCH_TIME, Integer.valueOf(1 * 20));
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
            RiftCreature creature = this.creatureType.invokeClass(this.world);
            if (creature != null) {
                creature.setGrowingAge(-24000);
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
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (this.getOwnerId().equals(player.getUniqueID())) {
            if (player.isSneaking()) {
                ItemStack eggStack = new ItemStack(this.creatureType.eggItem);
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
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setHatchTime(compound.getInteger("HatchTime"));
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

    @Override
    public void registerControllers(AnimationData data) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
