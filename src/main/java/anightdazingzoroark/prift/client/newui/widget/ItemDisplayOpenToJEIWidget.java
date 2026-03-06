package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;

public class ItemDisplayOpenToJEIWidget extends ItemDisplayWidget implements Interactable {
    private boolean useMiningLevel;
    private int miningLevel;

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (!this.useMiningLevel) {
            super.draw(context, widgetTheme);
            return;
        }
        if (this.getIngredient() == null || !(this.getIngredient() instanceof ItemStack itemStack)) return;
        if (!Platform.isStackEmpty(itemStack)) {
            GuiDraw.drawItem(itemStack, 1, 1, 16, 16, context.getCurrentDrawingZ());
            IKey.str(String.valueOf(this.miningLevel)).color(0xFFFFFFFF)
                    .drawAligned(
                        context,
                        0, 0,
                        this.getArea().width, this.getArea().height,
                        widgetTheme.getTheme(), Alignment.BottomRight
                    );
        }
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFallback();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (this.useMiningLevel) return Result.ACCEPT;
        if (!Loader.isModLoaded(RiftInitialize.JEI_MOD_ID)) return Result.ACCEPT;
        if (this.getIngredient() == null || !(this.getIngredient() instanceof ItemStack itemStack)) return Result.ACCEPT;
        Interactable.playButtonClickSound();
        RiftJEI.showRecipesForItemStack(itemStack, false);
        return Result.SUCCESS;
    }

    @Override
    public ItemDisplayWidget item(IValue<ItemStack> itemSupplier) {
        this.setSyncOrValue(ISyncOrValue.orEmpty(itemSupplier));
        if (!this.useMiningLevel) {
            this.addTooltipLine(IKey.lang(itemSupplier.getValue().getTranslationKey()+".name"));
            if (Loader.isModLoaded(RiftInitialize.JEI_MOD_ID)) this.addTooltipLine(IKey.lang("journal.open_in_jei"));
        }
        return this;
    }

    public ItemDisplayOpenToJEIWidget setForMiningLevel(String toolName, int miningLevel) {
        this.useMiningLevel = true;

        //set tool
        ItemStack tool = ItemStack.EMPTY;
        switch (toolName) {
            case "pickaxe": {
                tool = new ItemStack(Items.STONE_PICKAXE);
                break;
            }
            case "axe": {
                tool = new ItemStack(Items.STONE_AXE);
                break;
            }
            case "shovel": {
                tool = new ItemStack(Items.STONE_SHOVEL);
                break;
            }
        }
        this.item(tool);

        //set mining level
        this.miningLevel = miningLevel;

        return this;
    }
}
