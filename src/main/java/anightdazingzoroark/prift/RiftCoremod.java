package anightdazingzoroark.prift;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class RiftCoremod implements IFMLLoadingPlugin {
    @Override
    public @Nullable String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public @Nullable String getModContainerClass() {
        return null;
    }

    @Override
    public @Nullable String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {
        System.out.println(map);
    }

    @Override
    public @Nullable String getAccessTransformerClass() {
        return null;
    }
}
