package anightdazingzoroark.prift.client.ui.widget;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;

public class ItemDisplayOpenToJEIWidget extends ItemDisplayWidget implements Interactable {
    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFallback();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!Loader.isModLoaded(RiftInitialize.JEI_MOD_ID)) return Result.ACCEPT;
        if (this.getIngredient() == null || !(this.getIngredient() instanceof ItemStack itemStack)) return Result.ACCEPT;
        Interactable.playButtonClickSound();
        RiftJEI.showRecipesForItemStack(itemStack, false);
        return Result.SUCCESS;
    }

    @Override
    public ItemDisplayWidget item(IValue<ItemStack> itemSupplier) {
        this.setSyncOrValue(ISyncOrValue.orEmpty(itemSupplier));
        this.addTooltipLine(IKey.lang(itemSupplier.getValue().getTranslationKey()+".name"));
        if (Loader.isModLoaded(RiftInitialize.JEI_MOD_ID)) this.addTooltipLine(IKey.lang("journal.open_in_jei"));
        return this;
    }
}
