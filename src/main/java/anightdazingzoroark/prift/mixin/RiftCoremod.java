package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.config.GeneralConfig;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.*;

@IFMLLoadingPlugin.Name("prift")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class RiftCoremod implements IEarlyMixinLoader, IFMLLoadingPlugin {
    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();
        configs.add("mixin.prift.settargetafterhitboxhit.json");
        configs.add("mixin.prift.hypnotizedtargeting.json");
        configs.add("mixin.prift.polarbearaiattackplayer.json");
        return configs;
    }

    public void onMixinConfigQueued(String mixinConfig) {
        System.out.println(mixinConfig+" has been queued");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
