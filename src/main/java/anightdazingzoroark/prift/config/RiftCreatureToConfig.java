package anightdazingzoroark.prift.config;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;

public class RiftCreatureToConfig {
    public static RiftConfigList creatureToConfig(RiftCreatureType creatureType) {
        switch (creatureType) {
            case TYRANNOSAURUS:
                return RiftConfigList.TYRANNOSAURUS;
            case STEGOSAURUS:
                return RiftConfigList.STEGOSAURUS;
            case DODO:
                return RiftConfigList.DODO;
            case TRICERATOPS:
                return RiftConfigList.TRICERATOPS;
        }
        return RiftConfigList.TYRANNOSAURUS;
    }
}
