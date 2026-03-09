package anightdazingzoroark.prift;

import net.minecraftforge.fml.common.Loader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class RiftMixinConfigPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinConfig) {
        System.out.println(mixinConfig+" has been queued");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        //divide the name into different portions
        String[] splitMixinClassName = mixinClassName.split("\\.");
        for (String splitMixinPortion : splitMixinClassName) {
            if (splitMixinPortion.equals(RiftInitialize.PYROTECH_MOD_ID)) return Loader.isModLoaded(RiftInitialize.PYROTECH_MOD_ID);
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
        System.out.println("preapply");
    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
        System.out.println("postapply");
    }
}
