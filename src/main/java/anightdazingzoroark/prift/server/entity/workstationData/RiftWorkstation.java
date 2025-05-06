package anightdazingzoroark.prift.server.entity.workstationData;


import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;

public enum RiftWorkstation {
    ANVIL_PYROTECH(
            new String[]{"pyrotech:anvil_granite", "pyrotech:anvil_iron_plated", "pyrotech:anvil_obsidian"},
            RiftPyrotechAnvilWorkstation.class,
            new CreatureMove.MoveAnimType[]{CreatureMove.MoveAnimType.TAIL}),
    CHOPPING_BLOCK_PYROTECH(new String[]{"pyrotech:chopping_block"},
            null,
            new CreatureMove.MoveAnimType[]{CreatureMove.MoveAnimType.TAIL});

    public final String[] workstationId;
    private final Class<? extends RiftWorkstationData> workstationData;
    public final CreatureMove.MoveAnimType[] moveAnimTypes;

    RiftWorkstation(String[]  workstationId, Class<? extends RiftWorkstationData> workstationData, CreatureMove.MoveAnimType[] moveAnimTypes) {
        this.workstationId = workstationId;
        this.workstationData = workstationData;
        this.moveAnimTypes = moveAnimTypes;
    }

    public RiftWorkstationData invokedWorkstationData() {
        RiftWorkstationData workstationDataToInvoke = null;
        if (RiftWorkstationData.class.isAssignableFrom(this.workstationData)) {
            try {
                workstationDataToInvoke = this.workstationData.getDeclaredConstructor().newInstance();
            }
            catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        return workstationDataToInvoke;
    }

    public static RiftWorkstation getWorkstation(RiftCreature creature, BlockPos workstationPos) {
        if (!(creature instanceof IWorkstationUser)) return null;
        for (RiftWorkstation workstation : RiftWorkstation.values()) {
            Block workstationBlock = creature.world.getBlockState(workstationPos).getBlock();
            String stringOfWorkstationPos = RiftUtil.getStringIdFromBlock(workstationBlock);
            for (String testWorkstationString : workstation.workstationId) {
                if (testWorkstationString.equals(stringOfWorkstationPos)
                        && ((IWorkstationUser)creature).getWorkstations().containsKey(stringOfWorkstationPos)) return workstation;
            }
        }
        return null;
    }

    public static CreatureMove getMoveForWorkstationUse(RiftWorkstation workstation, RiftCreature creature) {
        for (CreatureMove creatureMove : creature.getLearnedMoves()) {
            if (Arrays.asList(workstation.moveAnimTypes).contains(creatureMove.moveAnimType))
                return creatureMove;
        }
        return null;
    }
}
