package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.client.ui.UIPanelNames;
import anightdazingzoroark.prift.client.ui.widget.EntityWidget;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.properties.journalProgress.JournalProgressHelper;
import anightdazingzoroark.prift.server.properties.journalProgress.JournalProgressProperties;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxHelper;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.EntityGuiData;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.layout.Flow;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
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

import javax.annotation.Nullable;

public class RiftSac extends EntityTameable implements IAnimatable<AnimationDataEntity>, IGuiHolder<EntityGuiData> {
    private static final DataParameter<Integer> HATCH_TIME = EntityDataManager.<Integer>createKey(RiftSac.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> SAC_TYPE = EntityDataManager.createKey(RiftSac.class, DataSerializers.BYTE);
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);

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
                if (owner == null) return;

                //update journal
                JournalProgressProperties journalProgress = JournalProgressHelper.getJournalProgress(owner);
                if (journalProgress.getEncounteredCreatures().containsKey(this.getCreatureType())
                        && !journalProgress.getEncounteredCreatures().get(this.getCreatureType())) {
                    journalProgress.unlockCreature(this.getCreatureType());
                    owner.sendStatusMessage(new TextComponentTranslation("reminder.unlocked_journal_entry", this.getCreatureType().getTranslatedName(), RiftControls.openParty.getDisplayName()), false);
                }

                //update party of owner
                PlayerPartyProperties playerParty = PlayerPartyHelper.getPlayerParty(owner);
                PlayerCreatureBoxProperties playerCreatureBox = PlayerCreatureBoxHelper.getPlayerCreatureBox(owner);

                if (playerParty.canAddToParty()) {
                    creature.setDeploymentType(CreatureDeployment.PARTY);
                    playerParty.addPartyMember(creature);
                    owner.sendStatusMessage(new TextComponentTranslation("prift.notify.sac_hatched_to_party", new TextComponentString(this.getName())), false);
                }
                //update box of owner
                else if (playerCreatureBox.canAddCreatureToBox()) {
                    creature.setDeploymentType(CreatureDeployment.BASE_INACTIVE);
                    playerCreatureBox.addCreatureToBox(creature);
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
            if (this.getOwnerId() != null && this.getOwnerId().equals(player.getUniqueID())) {
                if (!this.world.isRemote) GuiFactories.entity().open(player, this);
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

    public String getHatchTimeString() {
        if (this.isInWater()) {
            String timeString = RiftUtil.ticksToMinSecTimeExpression(this.getHatchTime());
            return I18n.format("prift.egg.remaining_hatch_time", timeString);
        }
        else return I18n.format("prift.sac.not_wet");
    }

    public int getHatchTime() {
        return this.dataManager.get(HATCH_TIME);
    }

    public void setHatchTime(int time) {
        this.dataManager.set(HATCH_TIME, time);
    }

    public RiftCreatureType getCreatureType() {
        return RiftCreatureType.values()[this.dataManager.get(SAC_TYPE)];
    }

    public void setCreatureType(RiftCreatureType type) {
        this.dataManager.set(SAC_TYPE, (byte) type.ordinal());
    }

    @Override
    public boolean isInWater() {
        return this.world.getBlockState(this.getPosition()).getMaterial() == Material.WATER;
    }

    @Override
    public void registerControllers(AnimationDataEntity animationData) {}

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }

    @Override
    public ModularPanel buildUI(EntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings().disable();
        RiftSac sacData = (RiftSac) data.getGuiHolder();

        return ModularPanel.defaultPanel("sacScreen").size(176, 166)
                .child(Flow.column().coverChildrenHeight().align(Alignment.Center)
                        .childPadding(15)
                        .child(IKey.lang("item."+sacData.getCreatureType().name().toLowerCase()+"_sac.name").asWidget())
                        .child(new EntityWidget<>(sacData, 60f).size(60).rotateEntity())
                        .child(IKey.dynamic(this::getHatchTimeString).asWidget())
                );
    }
}
