package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.config.GeneralConfig;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

public class RiftCoremodLate implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();
        if (GeneralConfig.canUsePyrotech()) {
            configs.add("mixin.prift.pyrotechmixins.json");
            configs.add("mixin.prift.pyrotechinvokers.json");
        }
        return configs;
    }

    @Override
    public void onMixinConfigQueued(String mixinConfig) {
        System.out.println(mixinConfig+" has been queued late");
    }
}
