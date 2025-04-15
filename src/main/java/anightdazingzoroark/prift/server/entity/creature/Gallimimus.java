package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Gallimimus extends RiftCreature {
    public Gallimimus(World worldIn) {
        super(worldIn, RiftCreatureType.GALLIMIMUS);
        this.setSize(1.25f, 1.5f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 10;
        this.speed = 0.35D;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
    }

    @Override
    public List<CreatureMove> learnableMoves() {
        return Arrays.asList(CreatureMove.PECK, CreatureMove.LEAP, CreatureMove.KICK);
    }

    @Override
    public List<CreatureMove> initialMoves() {
        return Arrays.asList(CreatureMove.PECK, CreatureMove.LEAP, CreatureMove.KICK);
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        return Collections.emptyMap();
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 0.9f};
    }

    @Override
    public float attackWidth() {
        return 2f;
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }
}
