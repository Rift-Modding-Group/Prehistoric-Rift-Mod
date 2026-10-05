package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import testdummy.TestDummy;
import testdummy.entity.EntityDummy;
import testdummy.entity.EntityFloatingNumber;
import testdummy.handlers.ConfigHandler;
import testdummy.network.DamageMessage;

/**
 * for making creatures able to deal damage to MmmMmmMmmMmm's target dummies
 * codex was being retarded trying to implement this so fuck it
 * just copypaste original code, fudge it a bit, then boom
 * */
@Mixin(value = EntityDummy.class)
public abstract class RiftMixinAllowRiddenCreatureDamageToDummy {
    @Shadow
    private int lastDamageTick;
    @Shadow
    private EntityFloatingNumber myLittleNumber;
    @Shadow
    private float maxDamage;
    @Shadow
    private int damageCounter;
    @Shadow
    private float damageTaken;
    @Shadow
    private int firstDamageTick;
    @Shadow
    private EntityPlayer lastAttacker;

    @Inject(method = "attackEntityFrom", at = @At("HEAD"), cancellable = true)
    private void attackEntityFrom(DamageSource damageSource, float damage, CallbackInfoReturnable<Boolean> callback) {
        if (!(damageSource.getTrueSource() instanceof RiftCreature creature && creature.getControllingPassenger() instanceof EntityPlayer controllingPlayer)) return;
        System.out.println("creature: "+creature);
        EntityDummy thisEntityDummy = (EntityDummy) (Object) this;

        if (thisEntityDummy.world.isRemote) return;
        if (thisEntityDummy.isDead) return;
        if (!ForgeHooks.onLivingAttack(thisEntityDummy, damageSource, damage)) return;
        if (thisEntityDummy.isEntityInvulnerable(damageSource)) return;
        if (damageSource.getTrueSource() == null) return;

        this.lastAttacker = controllingPlayer;

        thisEntityDummy.setHealth(thisEntityDummy.getMaxHealth());

        //Vanilla dmg calc
        if (ConfigHandler.server.useIframes) {
            if ((float) thisEntityDummy.hurtResistantTime > (float) thisEntityDummy.maxHurtResistantTime / 2f) {
                if (damage <= thisEntityDummy.lastDamage) {
                    callback.setReturnValue(false);
                    return;
                }
                float fullDamage = damage;
                damage = damage - thisEntityDummy.lastDamage;
                thisEntityDummy.lastDamage = fullDamage;
            }
            else {
                thisEntityDummy.lastDamage = damage;
                thisEntityDummy.hurtResistantTime = thisEntityDummy.maxHurtResistantTime;
            }
        }

        if (ConfigHandler.server.useLivingHurtEvent) {
            damage = ForgeHooks.onLivingHurt(thisEntityDummy, damageSource, damage);
            if (damage <= 0) {
                callback.setReturnValue(false);
                return;
            }
        }
        if (ConfigHandler.server.useArmorCalc) damage = thisEntityDummy.applyArmorCalculations(damageSource, damage);
        if (ConfigHandler.server.useResistanceCalc) damage = thisEntityDummy.applyPotionDamageCalculations(damageSource, damage);
        if (ConfigHandler.server.useLivingDamageEvent) damage = net.minecraftforge.common.ForgeHooks.onLivingDamage(thisEntityDummy, damageSource, damage);

        //Compat for mods like SME reducing health outside the usual pathways
        if (ConfigHandler.server.useNoEventDmg) {
            float healthLost = thisEntityDummy.getMaxHealth() - thisEntityDummy.getHealth();
            damage += healthLost;
        }
        thisEntityDummy.setHealth(thisEntityDummy.getMaxHealth());

        thisEntityDummy.shake = Math.min(damage, 30.0F);
        this.lastDamageTick = thisEntityDummy.ticksExisted;

        thisEntityDummy.hurtTime = thisEntityDummy.maxHurtTime = 10;

        if (ConfigHandler.server.showHearts) damage /= 2f;

        if (this.myLittleNumber != null && !this.myLittleNumber.isDead) {
            this.myLittleNumber.setDead();
        }

        EntityFloatingNumber number = new EntityFloatingNumber(thisEntityDummy.world, damage, thisEntityDummy.posX, thisEntityDummy.posY + 2D, thisEntityDummy.posZ);
        this.myLittleNumber = number;
        thisEntityDummy.world.spawnEntity(number);
        TestDummy.proxy.network.sendToAllAround(new DamageMessage(damage, thisEntityDummy.shake, thisEntityDummy, this.myLittleNumber), new NetworkRegistry.TargetPoint(thisEntityDummy.dimension, thisEntityDummy.posX, thisEntityDummy.posY, thisEntityDummy.posZ, 20.0D));
        this.maxDamage = Math.max(damage, this.maxDamage);
        this.damageTaken += damage;
        this.damageCounter++;
        if (this.firstDamageTick == 0) this.firstDamageTick = thisEntityDummy.ticksExisted;

        callback.setReturnValue(true);
    }
}
