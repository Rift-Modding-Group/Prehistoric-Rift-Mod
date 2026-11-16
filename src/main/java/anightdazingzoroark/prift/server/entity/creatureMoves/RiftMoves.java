package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.api.creature.CreatureMoveRegistry;
import anightdazingzoroark.prift.server.entity.creatureMoves.builders.*;

/**
 * Registers the built-in creature moves and their AI rules.
 * */
public class RiftMoves {
    private static boolean registered;

    public static void registerMoves() {
        if (registered) return;

        CreatureMoveRegistry.registerMove("placeholder", new PlaceholderBuilder());
        CreatureMoveRegistry.registerMove("bite", new BiteBuilder(), new BiteBuilder.Rule());
        CreatureMoveRegistry.registerMove("crunch", new CrunchBuilder(), new CrunchBuilder.Rule());
        CreatureMoveRegistry.registerMove("fire_fang", new FireFangBuilder(), new FireFangBuilder.Rule());
        CreatureMoveRegistry.registerMove("thunder_fang", new ThunderFangBuilder(), new ThunderFangBuilder.Rule());
        CreatureMoveRegistry.registerMove("ice_fang", new IceFangBuilder(), new IceFangBuilder.Rule());
        CreatureMoveRegistry.registerMove("aqua_fang", new AquaFangBuilder(), new AquaFangBuilder.Rule());
        CreatureMoveRegistry.registerMove("stomp", new StompBuilder(), new StompBuilder.Rule());
        CreatureMoveRegistry.registerMove("tremor", new TremorBuilder(), new TremorBuilder.Rule());
        CreatureMoveRegistry.registerMove("flame_spit", new FlameSpitBuilder(), new FlameSpitBuilder.Rule());
        CreatureMoveRegistry.registerMove("snarl", new SnarlBuilder(), new SnarlBuilder.Rule());
        CreatureMoveRegistry.registerMove("fireball", new FireballBuilder(), new FireballBuilder.Rule());
        CreatureMoveRegistry.registerMove("willowisp", new WillOWispBuilder(), new WillOWispBuilder.Rule());
        CreatureMoveRegistry.registerMove("power_roar", new PowerRoarBuilder(), new PowerRoarBuilder.Rule());
        CreatureMoveRegistry.registerMove("flamethrower", new FlamethrowerBuilder(), new FlamethrowerBuilder.Rule());
        CreatureMoveRegistry.registerMove("sonic_blast", new SonicBlastBuilder(), new SonicBlastBuilder.Rule());
        CreatureMoveRegistry.registerMove("toxic_haze", new ToxicHazeBuilder(), new ToxicHazeBuilder.Rule());
        CreatureMoveRegistry.registerMove("tail_stab", new TailStabBuilder(), new TailStabBuilder.Rule());
        CreatureMoveRegistry.registerMove("tail_slam", new TailSlamBuilder(), new TailSlamBuilder.Rule());
        CreatureMoveRegistry.registerMove("thagomize", new ThagomizeBuilder(), new ThagomizeBuilder.Rule());
        CreatureMoveRegistry.registerMove("plate_fling", new PlateFlingBuilder(), new PlateFlingBuilder.Rule());
        CreatureMoveRegistry.registerMove("peck", new PeckBuilder(), new PeckBuilder.Rule());
        CreatureMoveRegistry.registerMove("headbutt", new HeadbuttBuilder(), new HeadbuttBuilder.Rule());
        CreatureMoveRegistry.registerMove("charge", new ChargeBuilder(), new ChargeBuilder.Rule());
        CreatureMoveRegistry.registerMove("flame_charge", new FlameChargeBuilder(), new FlameChargeBuilder.Rule());
        CreatureMoveRegistry.registerMove("volt_tackle", new VoltTackleBuilder(), new VoltTackleBuilder.Rule());
        CreatureMoveRegistry.registerMove("slush_rush", new SlushRushBuilder(), new SlushRushBuilder.Rule());
        CreatureMoveRegistry.registerMove("belch", new BelchBuilder(), new BelchBuilder.Rule());
        CreatureMoveRegistry.registerMove("magma_storm", new MagmaStormBuilder(), new MagmaStormBuilder.Rule());
        CreatureMoveRegistry.registerMove("seed_bomb", new SeedBombBuilder(), new SeedBombBuilder.Rule());
        CreatureMoveRegistry.registerMove("scale_shed", new ScaleShedBuilder());
        CreatureMoveRegistry.registerMove("hyper_cutter", new HyperCutterBuilder(), new HyperCutterBuilder.Rule());
        CreatureMoveRegistry.registerMove("bide", new BideBuilder(), new BideBuilder.Rule());
        CreatureMoveRegistry.registerMove("weakness_exploiter", new WeaknessExploiterBuilder(), new WeaknessExploiterBuilder.Rule());
        CreatureMoveRegistry.registerMove("bullet_seed", new BulletSeedBuilder(), new BulletSeedBuilder.Rule());
        CreatureMoveRegistry.registerMove("drill_peck", new DrillPeckMove(), new DrillPeckMove.Rule());
        CreatureMoveRegistry.registerMove("shield_bash", new ShieldBashBuilder(), new ShieldBashBuilder.Rule());
        CreatureMoveRegistry.registerMove("heavy_slam", new HeavySlamBuilder(), new HeavySlamBuilder.Rule());
        CreatureMoveRegistry.registerMove("drill_rush", new DrillRushBuilder(), new DrillRushBuilder.Rule());
        CreatureMoveRegistry.registerMove("sludge_wave", new SludgeWaveBuilder(), new SludgeWaveBuilder.Rule());

        registered = true;
    }
}
