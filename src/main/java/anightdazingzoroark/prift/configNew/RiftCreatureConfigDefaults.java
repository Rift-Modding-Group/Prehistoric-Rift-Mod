package anightdazingzoroark.prift.configNew;

import anightdazingzoroark.prift.RiftUtil;

import java.util.Arrays;
import java.util.List;

public class RiftCreatureConfigDefaults {
    public static List<RiftCreatureConfig.Food> defaultHerbivoreFoods = Arrays.asList(
            new RiftCreatureConfig.Food("minecraft:apple", 0.025),
            new RiftCreatureConfig.Food("minecraft:wheat", 0.025),
            new RiftCreatureConfig.Food("minecraft:carrot", 0.025),
            new RiftCreatureConfig.Food("minecraft:potato", 0.025),
            new RiftCreatureConfig.Food("minecraft:beetroot", 0.025)
    );
    public static List<RiftCreatureConfig.Food> defaultCarnivoreFoods = Arrays.asList(
            new RiftCreatureConfig.Food("minecraft:beef", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_beef", 0.075),
            new RiftCreatureConfig.Food("minecraft:porkchop", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_porkchop", 0.075),
            new RiftCreatureConfig.Food("minecraft:chicken", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_chicken", 0.075),
            new RiftCreatureConfig.Food("minecraft:mutton", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_mutton", 0.075),
            new RiftCreatureConfig.Food("minecraft:rabbit", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_rabbit", 0.075),
            new RiftCreatureConfig.Food("minecraft:rotten_flesh", 0.075),
            new RiftCreatureConfig.Food("prift:raw_exotic_meat", 0.05),
            new RiftCreatureConfig.Food("prift:cooked_exotic_meat", 0.075),
            new RiftCreatureConfig.Food("prift:raw_fibrous_meat", 0),
            new RiftCreatureConfig.Food("prift:cooked_fibrous_meat", 0),
            new RiftCreatureConfig.Food("prift:raw_hadrosaur_meat", 0.05),
            new RiftCreatureConfig.Food("prift:cooked_hadrosaur_meat", 0.075)
    );
    public static List<RiftCreatureConfig.Food> defaultPiscivoreFoods = Arrays.asList(
            new RiftCreatureConfig.Food("minecraft:beef", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_beef", 0.075),
            new RiftCreatureConfig.Food("minecraft:porkchop", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_porkchop", 0.075),
            new RiftCreatureConfig.Food("minecraft:chicken", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_chicken", 0.075),
            new RiftCreatureConfig.Food("minecraft:mutton", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_mutton", 0.075),
            new RiftCreatureConfig.Food("minecraft:rabbit", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_rabbit", 0.075),
            new RiftCreatureConfig.Food("minecraft:rotten_flesh", 0.075),
            new RiftCreatureConfig.Food("prift:raw_exotic_meat", 0.05),
            new RiftCreatureConfig.Food("prift:cooked_exotic_meat", 0.075),
            new RiftCreatureConfig.Food("prift:raw_fibrous_meat", 0),
            new RiftCreatureConfig.Food("prift:cooked_fibrous_meat", 0),
            new RiftCreatureConfig.Food("prift:raw_hadrosaur_meat", 0.05),
            new RiftCreatureConfig.Food("prift:cooked_hadrosaur_meat", 0.075),
            new RiftCreatureConfig.Food("minecraft:fish:0", 0.05),
            new RiftCreatureConfig.Food("minecraft:fish:1", 0.05),
            new RiftCreatureConfig.Food("minecraft:fish:2", 0.05),
            new RiftCreatureConfig.Food("minecraft:cooked_fish", 0.075)
    );
}
