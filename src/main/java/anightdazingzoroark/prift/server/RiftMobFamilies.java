package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.riftlib.mobFamily.MobFamilyCreator;

public class RiftMobFamilies {
    public static void initMobFamilies() {
        //edit vanilla mob families
        MobFamilyCreator.addMembersToFamily("arthropod", RiftInitialize.MODID +":"+RiftCreatureType.ANOMALOCARIS.toString().toLowerCase());
    }
}
