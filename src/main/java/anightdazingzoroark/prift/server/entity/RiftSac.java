package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.client.ui.RiftEggMenu;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public class RiftSac extends EntityTameable implements IAnimatable {
    private static final DataParameter<Integer> HATCH_TIME = EntityDataManager.<Integer>createKey(RiftSac.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> SAC_TYPE = EntityDataManager.createKey(RiftSac.class, DataSerializers.BYTE);
    public AnimationFactory factory = new AnimationFactory(this);

    public RiftSac(World worldIn) {
        super(worldIn);
        this.setSize(1F, 1F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HATCH_TIME, 20);
        this.dataManager.register(SAC_TYPE, (byte) RiftCreatureType.ANOMALOCARIS.ordinal());
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isInWater() && this.getHatchTime() > 0) this.setHatchTime(this.getHatchTime() - 1);
        if (this.getHatchTime() == 0) {
            RiftCreature creature = this.getCreatureType().invokeClass(this.world);
            creature.setHealth((float) (creature.minCreatureHealth + (0.1) * (creature.getLevel()) * (creature.minCreatureHealth)));
            creature.setAgeInDays(0);
            creature.setHealth(creature.getMaxHealth());
            creature.setEnergy(creature.getMaxEnergy());

            if (this.getOwnerId() != null) {
                creature.setTamed(true);
                creature.setOwnerId(this.getOwnerId());
                creature.setTameBehavior(TameBehaviorType.PASSIVE);
                creature.setAcquisitionInfo(CreatureAcquisitionInfo.AcquisitionMethod.BORN, System.currentTimeMillis() / 1000L);
            }

            creature.setLocationAndAngles(Math.floor(this.posX), Math.floor(this.posY) + 1, Math.floor(this.posZ), this.world.rand.nextFloat() * 360.0F, 0.0F);
            if (!this.world.isRemote) {
                EntityPlayer owner = (EntityPlayer) this.getOwner();

                //update journal
                if (PlayerJournalProgressHelper.getUnlockedCreatures(owner).containsKey(this.getCreatureType()) && !PlayerJournalProgressHelper.getUnlockedCreatures(owner).get(this.getCreatureType())) {
                    PlayerJournalProgressHelper.unlockCreature(owner, this.getCreatureType());
                    owner.sendStatusMessage(new TextComponentTranslation("reminder.unlocked_journal_entry", this.getCreatureType().getTranslatedName(), RiftControls.openParty.getDisplayName()), false);
                }

                //update party of owner
                if (PlayerTamedCreaturesHelper.canAddToParty(owner)) {
                    creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                    PlayerTamedCreaturesHelper.addCreatureToParty(owner, creature);
                    owner.sendStatusMessage(new TextComponentTranslation("prift.notify.sac_hatched_to_party", new TextComponentString(this.getName())), false);
                }
                //update box of owner
                else if (PlayerTamedCreaturesHelper.canAddCreatureToBox(owner)) {
                    creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                    PlayerTamedCreaturesHelper.addCreatureToBox(owner, creature);
                    owner.sendStatusMessage(new TextComponentTranslation("prift.notify.sac_hatched_to_box", new TextComponentString(this.getName())), false);
                }
            }
            this.setDead();
        }
    }


    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            if (this.getOwnerId().equals(player.getUniqueID())) {
                ItemStack sacStack = new ItemStack(this.getCreatureType().sacItem);
                if (!player.capabilities.isCreativeMode) player.inventory.addItemStackToInventory(sacStack);
                this.setDead();
                return true;
            }
            else {
                ITextComponent itextcomponent = new TextComponentString(this.getOwner().getName());
                player.sendStatusMessage(new TextComponentTranslation("reminder.not_sac_owner", itextcomponent), false);
            }
            return true;
        }
        else {
            try {
                if (this.getOwnerId().equals(player.getUniqueID())) {
                    RiftLibUIHelper.showUI(player, new RiftEggMenu(this));
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

    @Override
    public boolean canBreatheUnderwater() {
        return true;
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

    public int[] getHatchTimeMinutes() {
        int minutes = (int)((float)this.getHatchTime() / 1200F);
        int seconds = (int)((float)this.getHatchTime() / 20F);
        seconds = seconds - (minutes * 60);
        return new int[]{minutes, seconds};
    }

    public int getHatchTime() {
        return this.dataManager.get(HATCH_TIME).intValue();
    }

    public void setHatchTime(int time) {
        this.dataManager.set(HATCH_TIME, time);
    }

    public RiftCreatureType getCreatureType() {
        return RiftCreatureType.values()[this.dataManager.get(SAC_TYPE).byteValue()];
    }

    public void setCreatureType(RiftCreatureType type) {
        this.dataManager.set(SAC_TYPE, (byte) type.ordinal());
    }

    @Override
    public boolean isInWater() {
        return this.world.getBlockState(this.getPosition()).getMaterial() == Material.WATER;
    }

    @Override
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
