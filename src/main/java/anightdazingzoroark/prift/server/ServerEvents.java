package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.server.config.RiftGeneralConfig;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import anightdazingzoroark.prift.server.entity.ai.RiftAvoidStinkBomb;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.world.BlockEvent;
import anightdazingzoroark.prift.server.block.CreatureBoxBlock;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums.CreatureDeployment;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class ServerEvents {
    @SubscribeEvent
    public void onBreakSpeed(@NotNull net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        //make paralysis stop block breaking
        if (event.getEntityPlayer().isPotionActive(RiftEffects.PARALYSIS)) {
            event.setNewSpeed(0f);
        }

        //same with being snowed
        OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(event.getEntityPlayer());
        if (otherEntityProperties != null && otherEntityProperties.isSnowed()) event.setNewSpeed(0f);
    }

    @SubscribeEvent
    public void onLivingAttack(@NotNull LivingAttackEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityLivingBase attacker)) return;
        OtherEntityProperties properties = OtherEntityProperties.get(attacker);
        if (properties != null && properties.isSnowed()) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onHitEntity(LivingHurtEvent event) {
        if (event.getEntityLiving() == null) return;

        //grant player immunity to entity attacks when riding
        if (event.getEntityLiving() instanceof EntityPlayer && event.getEntityLiving().getRidingEntity() instanceof RiftCreature
                && !(event.getSource().getTrueSource() instanceof EntityPlayer)
        ) {
            event.setCanceled(true);
        }

        //make defense down increase damage taken
        if (event.getEntityLiving().isPotionActive(RiftEffects.DEFENSE_DOWN)) {
            PotionEffect defenseDown = event.getEntityLiving().getActivePotionEffect(RiftEffects.DEFENSE_DOWN);
            if (defenseDown == null) return;
            float damageMultiplier = (defenseDown.getAmplifier() + 1f) / 10f + 1f;
            event.setAmount(event.getAmount() * damageMultiplier);
        }

        //for increasing bide damage
        if (event.getEntityLiving() instanceof RiftCreature creature && creature.getCurrentMove().equals("bide")
                && creature.getCreatureMoves().isWindingUp()
        ) {
            double newDamage = (double) creature.getProperty("BideReceivedDamage").getValue() + Math.max(event.getAmount(), 0);
            creature.setProperty("BideReceivedDamage", newDamage);
        }
    }

    @SubscribeEvent
    public void blockBreakEvent(@NotNull BlockEvent.BreakEvent event) {
        OtherEntityProperties properties = OtherEntityProperties.get(event.getPlayer());
        if (event.getPlayer().isPotionActive(RiftEffects.PARALYSIS) || properties != null && properties.isSnowed()) {
            event.setCanceled(true);
            return;
        }


        //make sure that a creature box cannot be broken
        //no matter what when it has creatures inside
        if (event.getState().getBlock() instanceof CreatureBoxBlock && CreatureBoxBlock.isOccupied(event.getWorld(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    //make sure creatures cannot enter portals when
    //deployed from a creature box
    @SubscribeEvent
    public void restrictDeployedCreatureTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof RiftCreature creature && creature.getDeploymentType() == CreatureDeployment.BASE) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void addStinkBombGoal(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !(event.getEntity() instanceof EntityLiving entityLiving)) return;

        OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(entityLiving);
        if (otherEntityProperties == null || otherEntityProperties.hasStinkBombGoal()) return;
        entityLiving.tasks.addTask(-1, new RiftAvoidStinkBomb(entityLiving));
        otherEntityProperties.setHasStinkBombGoal(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void setAttackTargeting(LivingSetAttackTargetEvent event) {
        if (!(event.getEntityLiving() instanceof EntityLiving entityLiving) || event.getTarget() == null) return;

        //make sure that when a player is riding, entities that would attack it will
        //be redirected to the creature it is riding on
        if (event.getTarget() instanceof EntityPlayer player && player.getRidingEntity() instanceof RiftCreature riddenCreature) {
            event.setNewTarget(riddenCreature);
        }

        //make sure stink bombing clears targets
        OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(entityLiving);
        if (otherEntityProperties != null && otherEntityProperties.isStinkBombed()) {
            event.setNewTarget(null);
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        //make people join le discord
        if (!RiftGeneralConfig.other.showJoinDiscordMessage) return;
        TextComponentString message = new TextComponentString("Click here to join the Discord server for this mod to hang out and receive updates! We beg you!");
        message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/JnjQtkVt8R"));
        message.getStyle().setUnderlined(true);
        event.player.sendMessage(message);
    }

    @SubscribeEvent
    public void livingDropsEvent(LivingDropsEvent event) {
        //to reduce potential lag, mobs killed by wild creatures will not drop items
        if (event.getSource().getTrueSource() instanceof RiftCreature creature
                && !creature.isTamed() && RiftGeneralConfig.creatures.creatureKillNoLoot
        ) {
            Entity attacked = event.getEntity();
            boolean tameableFlag = attacked instanceof EntityTameable tameable && tameable.isTamed();
            boolean playerFlag = attacked instanceof EntityPlayer;
            event.setCanceled(!tameableFlag && !playerFlag);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void livingDeathEvent(LivingDeathEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        if (event.getSource().getTrueSource() instanceof RiftCreature creature && creature.isTamed()) {
            EntityLivingBase defeatedEntity = event.getEntityLiving();
            EntityPlayer owner = creature.getOwner() instanceof EntityPlayer player ? player : null;

            //alert owner about killing
            //vanilla death message in place of this
            if (owner != null && (!(defeatedEntity instanceof EntityTameable tameable) || !tameable.isTamed())) {
                owner.sendStatusMessage(
                        new TextComponentTranslation("party.warning.killed_mob", creature.getName(), defeatedEntity.getName()),
                        false
                );
            }

            //give xp from dead entity to creature
            creature.addXP(defeatedEntity.getExperiencePoints(owner));
        }
    }

    @SubscribeEvent
    public void redirectCreatureKillXP(LivingExperienceDropEvent event) {
        if (event.getEntityLiving().getLastDamageSource() != null
                && event.getEntityLiving().getLastDamageSource().getTrueSource() instanceof RiftCreature creature
                && creature.isTamed()
        ) {
            event.setDroppedExperience(0);
        }
    }
}
