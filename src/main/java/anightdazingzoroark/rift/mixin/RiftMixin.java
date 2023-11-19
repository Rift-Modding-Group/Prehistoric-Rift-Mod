package anightdazingzoroark.rift.mixin;

import anightdazingzoroark.rift.RiftInitialize;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(-5000)
public class RiftMixin implements IFMLLoadingPlugin {
    private static final Map<String, String> configMap = setupMap();
    private static Map<String, String> setupMap() {
        Map<String, String> map = new HashMap<>();
        return Collections.unmodifiableMap(map);
    }

    public RiftMixin() {
        MixinBootstrap.init();
//        for(Map.Entry<String, String> entry : configMap.entrySet()) {
//            RiftInitialize.logger.log(Level.INFO, "RiftMixin Early Loading: " + entry.getKey());
//            Mixins.addConfiguration(entry.getValue());
//        }
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
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
