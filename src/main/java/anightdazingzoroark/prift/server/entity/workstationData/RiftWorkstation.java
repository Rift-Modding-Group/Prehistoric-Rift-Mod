package anightdazingzoroark.prift.server.entity.workstationData;


import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public enum RiftWorkstation {
    ANVIL_PYROTECH(
            new String[]{
                    "pyrotech:anvil_granite",
                    "pyrotech:anvil_iron_plated",
                    "pyrotech:anvil_obsidian"
            },
            RiftPyrotechAnvilWorkstation.class,
            new CreatureMove.MoveAnimType[]{CreatureMove.MoveAnimType.TAIL}
    ),
    CHOPPING_BLOCK_PYROTECH(new String[]{
                "pyrotech:chopping_block"
            },
            RiftPyrotechChoppingBlockWorkstation.class,
            new CreatureMove.MoveAnimType[]{CreatureMove.MoveAnimType.TAIL}
    ),
    COMBUSTION_WORKER_PYROTECH(new String[]{
                "pyrotech:stone_kiln",
                "pyrotech:stone_oven",
                "pyrotech:stone_sawmill",
                "pyrotech:stone_crucible",
                "pyrotech:brick_kiln",
                "pyrotech:brick_oven",
                "pyrotech:brick_sawmill",
                "pyrotech:brick_crucible"
            },
            RiftPyrotechCombustionWorkerWorkstation.class,
            new CreatureMove.MoveAnimType[]{CreatureMove.MoveAnimType.BLOW, CreatureMove.MoveAnimType.ROAR}
    ),
    BLOOMERY_PYROTECH(new String[]{
                "pyrotech:bloomery"
            },
            RiftPyrotechBloomeryWorkstation.class,
            new CreatureMove.MoveAnimType[]{CreatureMove.MoveAnimType.BLOW, CreatureMove.MoveAnimType.ROAR}
    ),
    BLOW_POWERED_TURBINE_MM(new String[]{
                "prift:blow_powered_turbine"
            },
            RiftMMBlowPoweredTurbineWorkstation.class,
            new CreatureMove.MoveAnimType[]{CreatureMove.MoveAnimType.BLOW, CreatureMove.MoveAnimType.ROAR}
    ),
    SEMI_MANUAL_MACHINE_MM(new String[]{
                "prift:semi_manual_extractor",
                "prift:semi_manual_presser",
                "prift:semi_manual_extruder",
                "prift:semi_manual_hammerer"
            },
            RiftMMSemiManualMachineWorkstation.class,
            new CreatureMove.MoveAnimType[]{CreatureMove.MoveAnimType.STOMP}
    );

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
