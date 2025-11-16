package anightdazingzoroark.prift.server.effect;

import anightdazingzoroark.prift.server.entity.ai.RiftGetTargets;
import anightdazingzoroark.prift.server.entity.ai.RiftRageMeleeAttack;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

//todo: make the rage effect take away control from the player to make them attack random entities
//and make their pov have red tint
public class RiftRageEffect extends RiftEffectBase {
    private static final Map<UUID, EntityAIBase> rageTasks = new HashMap<>();

    public RiftRageEffect() {
        super(true, 0xFF0000);

        this.setIconUVs(0, 0);

        //increase damage
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.ATTACK_DAMAGE,
                "6b128384-6ae3-40a1-9808-7ceb26a33640",
                0.8,
                0
        );
    }

    @Override
    public String name() {
        return "rage";
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        if (!(entityLivingBaseIn instanceof EntityCreature)) return;
        EntityCreature entityCreature = (EntityCreature) entityLivingBaseIn;

        //now make the entity look for targets
        if (entityCreature.getAttackTarget() == null || !entityCreature.getAttackTarget().isEntityAlive()) {
            AxisAlignedBB area = entityCreature.getEntityBoundingBox().grow(8D, 8D, 8D);
            List<EntityLivingBase> nearbyEntities = entityCreature.world.getEntitiesWithinAABB(EntityLivingBase.class, area, new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(@Nullable EntityLivingBase entityLivingBase) {
                    return entityLivingBase != null && !entityLivingBase.equals(entityCreature);
                }
            });
            if (!nearbyEntities.isEmpty()) {
                int randomPos = new Random().nextInt(nearbyEntities.size());
                entityCreature.setAttackTarget(nearbyEntities.get(randomPos));
            }
        }
    }

    @Override
    public void onEffectAdded(EntityLivingBase entityLivingBase) {
        if (!(entityLivingBase instanceof EntityCreature)) return;
        EntityCreature entityCreature = (EntityCreature) entityLivingBase;

        //if entity already has attacking capabilities, block everything onwards
        if (this.canAttack(entityCreature)) return;

        if (!this.hasAttackDamage(entityCreature)) {
            //add damage attribute to entities that dont have it
            //1 heart of damage should be good
            entityCreature.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2D);

            //add the goal for attacking
            RiftRageMeleeAttack rageAI = new RiftRageMeleeAttack(entityCreature, 1.0D, false);
            entityCreature.targetTasks.addTask(0, rageAI);
            rageTasks.put(entityCreature.getUniqueID(), rageAI);
        }
    }

    @Override
    public void onEffectRemoved(EntityLivingBase entityLivingBase) {
        if (!(entityLivingBase instanceof EntityCreature)) return;
        EntityCreature entityCreature = (EntityCreature) entityLivingBase;

        //remove any target the entity to apply to has
        entityCreature.setAttackTarget(null);

        if (!this.hasAttackDamage(entityCreature)) {
            //remove the rage attack goal
            EntityAIBase rageAI = rageTasks.remove(entityCreature.getUniqueID());
            if (rageAI != null) entityCreature.targetTasks.removeTask(rageAI);

            //unregister that damage attribute
            entityCreature.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        }
    }

    //check if the entity can attack by seeing it has goals for target detection
    private boolean canAttack(EntityCreature entityCreature) {
        return entityCreature.targetTasks.taskEntries.stream()
                .anyMatch(e -> e.action instanceof EntityAINearestAttackableTarget
                        || e.action instanceof EntityAIHurtByTarget
                        || e.action instanceof RiftGetTargets);
    }

    private boolean hasAttackDamage(EntityCreature entityCreature) {
        return entityCreature.getAttributeMap().getAllAttributes().stream()
                .anyMatch(a -> a.getAttribute() == SharedMonsterAttributes.ATTACK_DAMAGE);
    }
}
