package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.riftlib.mobFamily.MobFamily;
import anightdazingzoroark.riftlib.mobFamily.MobFamilyCreator;
import anightdazingzoroark.riftlib.mobFamily.MobFamilyManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RiftMobFamilies {
    public static void initMobFamilies(File directory) {
        //edit vanilla mob families
        MobFamilyCreator.addMembersToFamily("arthropod", RiftInitialize.MODID +":"+RiftCreatureType.ANOMALOCARIS.toString().toLowerCase());

        //make new mob families
        MobFamilyManager mobFamilyManager = MobFamilyCreator.createManager(directory, "prift/mob_families.json");

        //for herbivores that will be commonly targeted by carnivores
        MobFamily commonlyTargetedHerbivore = new MobFamily("commonlyTargetedHerbivore");
        List<String> cTargetedHerbivoresToAdd = Arrays.stream(RiftCreatureType.values())
                        .filter(c -> {
                            return c.getCreatureDiet() == RiftCreatureType.CreatureDiet.HERBIVORE
                                    //everything from this point forward will never targeted
                                    && c != RiftCreatureType.APATOSAURUS;
                        })
                        .map(RiftCreatureType::getIdentifier)
                        .collect(Collectors.toList());
        for (String id : cTargetedHerbivoresToAdd) commonlyTargetedHerbivore.addToFamilyMembers(id);
        mobFamilyManager.addMobFamily(commonlyTargetedHerbivore);

        //for carnivores in general
        MobFamily carnivore = new MobFamily("carnivore");
        List<String> carnivoreToAdd = Arrays.stream(RiftCreatureType.values())
                .filter(c -> c.getCreatureDiet() == RiftCreatureType.CreatureDiet.CARNIVORE)
                .map(RiftCreatureType::getIdentifier)
                .collect(Collectors.toList());
        for (String id : carnivoreToAdd) carnivore.addToFamilyMembers(id);
        mobFamilyManager.addMobFamily(carnivore);

        //for carnivores that are hostile to humans
        MobFamily carnivoreHostileToHuman = new MobFamily("carnivoreHostileToHuman");
        List<String> carnivoreHostileToHumanToAdd = Arrays.stream(RiftCreatureType.values())
                        .filter(c -> c.getCreatureDiet() == RiftCreatureType.CreatureDiet.CARNIVORE && c.getBehaviors().contains(RiftCreatureType.Behavior.AGGRESSIVE_TO_HUMANS))
                        .map(RiftCreatureType::getIdentifier)
                        .collect(Collectors.toList());
        for (String id : carnivoreHostileToHumanToAdd) carnivoreHostileToHuman.addToFamilyMembers(id);
        mobFamilyManager.addMobFamily(carnivoreHostileToHuman);

        mobFamilyManager.load();
    }
}
