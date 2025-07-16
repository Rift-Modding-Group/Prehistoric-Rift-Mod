package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.helper.RiftUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class RiftGuiScrollableSectionContents {
    private final List<Element> list = new ArrayList<>();

    public RiftGuiScrollableSectionContents addTextElement(TextElement element) {
        this.list.add(element);
        return this;
    }

    public RiftGuiScrollableSectionContents addImageElement(ImageElement element) {
        this.list.add(element);
        return this;
    }

    public RiftGuiScrollableSectionContents addButtonElement(ButtonElement element) {
        this.list.add(element);
        return this;
    }

    public RiftGuiScrollableSectionContents addTextBoxElement(TextBoxElement element) {
        this.list.add(element);
        return this;
    }

    public RiftGuiScrollableSectionContents addTabElement(TabElement element) {
        this.list.add(element);
        return this;
    }

    public RiftGuiScrollableSectionContents addProgressBarElement(ProgressBarElement element) {
        this.list.add(element);
        return this;
    }

    public List<Element> getContents() {
        return this.list;
    }

    public static class Element {}

    public static class TextElement extends Element {
        private String contents;
        private int textColor = 0x000000;
        private float scale = 1f;
        private int bottomSpace;

        public TextElement setContents(String value) {
            this.contents = value;
            return this;
        }

        public TextElement setTextColor(int color) {
            this.textColor = color;
            return this;
        }

        public String getContents() {
            return this.contents;
        }

        public int getTextColor() {
            return this.textColor;
        }

        public TextElement setScale(float value) {
            this.scale = value;
            return this;
        }

        public float getScale() {
            return this.scale;
        }

        public TextElement setBottomSpace(int value) {
            this.bottomSpace = value;
            return this;
        }

        public int getBottomSpace() {
            return this.bottomSpace;
        }
    }

    public static class ImageElement extends Element {
        private ResourceLocation imageLocation;
        private int[] imageSize;
        private float imageScale = 1f;
        private int bottomSpace = 9;

        public ImageElement setImageLocation(ResourceLocation imageLocation) {
            this.imageLocation = imageLocation;
            return this;
        }

        public ResourceLocation getImageLocation() {
            return this.imageLocation;
        }

        public ImageElement setImageSize(int width, int height) {
            this.imageSize = new int[]{width, height};
            return this;
        }

        public int[] getImageSize() {
            return this.imageSize;
        }

        public ImageElement setImageScale(float scale) {
            this.imageScale = scale;
            return this;
        }

        public float getImageScale() {
            return this.imageScale;
        }

        public ImageElement setBottomSpace(int value) {
            this.bottomSpace = value;
            return this;
        }

        public int getBottomSpace() {
            return this.bottomSpace;
        }
    }

    public static class ButtonElement extends Element {
        private String name = ""; //display name
        private String id = ""; //id, which is to be used for performing operations or changing stuff among other things
        private int[] size = {60, 20};
        private int bottomSpace;

        public ButtonElement setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return this.name;
        }

        public ButtonElement setId(String id) {
            this.id = id;
            return this;
        }

        public String getId() {
            return this.id;
        }

        public ButtonElement setSize(int width, int height) {
            this.size = new int[]{width, height};
            return this;
        }

        public int[] getSize() {
            return this.size;
        }

        public ButtonElement setBottomSpaceSize(int value) {
            this.bottomSpace = value;
            return this;
        }

        public int getBottomSpaceSize() {
            return this.bottomSpace;
        }
    }

    public static class TabElement extends Element {
        private String id = "";
        private int width; //no width means that it will inherit the width of the section
        private final Map<String, List<Element>> tabContents = new HashMap<>();
        private final List<String> tabOrder = new ArrayList<>();

        public TabElement setId(String id) {
            this.id = id;
            return this;
        }

        public String getId() {
            return this.id;
        }

        public TabElement setWidth(int value) {
            this.width = value;
            return this;
        }

        public int getWidth() {
            return this.width;
        }

        public TabElement addTab(String name, List<Element> contents) {
            this.tabContents.put(name, contents);
            this.tabOrder.add(name);
            return this;
        }

        public Map<String, List<Element>> getTabContents() {
            return this.tabContents;
        }

        public List<String> getTabOrder() {
            return this.tabOrder;
        }
    }

    public static class ItemListElement extends Element {
        private String headerText = "";
        private List<String> itemIds = new ArrayList<>();
        private int headerTextColor = 0x000000;

        public ItemListElement setHeaderText(String value) {
            this.headerText = value;
            return this;
        }

        public String getHeaderText() {
            return this.headerText;
        }

        public ItemListElement addItem(String itemId) {
            this.itemIds.add(itemId);
            return this;
        }

        public ItemListElement addItems(List<String> toAdd) {
            this.itemIds = RiftUtil.uniteTwoLists(this.itemIds, toAdd);
            return this;
        }

        public List<String> getItemsById() {
            return this.itemIds;
        }

        public ItemListElement setHeaderColor(int value) {
            this.headerTextColor = value;
            return this;
        }

        public int getHeaderTextColor() {
            return this.headerTextColor;
        }
    }

    public static class MiningLevelListElement extends Element {
        private String headerText = "";
        private List<String> miningLevels = new ArrayList<>();
        private int headerTextColor = 0x000000;

        public MiningLevelListElement setHeaderText(String value) {
            this.headerText = value;
            return this;
        }

        public String getHeaderText() {
            return this.headerText;
        }

        public MiningLevelListElement addMiningLevels(List<String> toAdd) {
            this.miningLevels = RiftUtil.uniteTwoLists(this.miningLevels, toAdd);
            return this;
        }

        public List<String> getMiningLevels() {
            return this.miningLevels;
        }

        public MiningLevelListElement setHeaderColor(int value) {
            this.headerTextColor = value;
            return this;
        }

        public int getHeaderTextColor() {
            return this.headerTextColor;
        }
    }

    public static class TextBoxElement extends Element {
        private int width = 0;
        private String defaultText = "";
        private String id = "";

        public TextBoxElement setWidth(int value) {
            this.width = value;
            return this;
        }

        public int getWidth() {
            return this.width;
        }

        public TextBoxElement setDefaultText(String value) {
            this.defaultText = value;
            return this;
        }

        public String getDefaultText() {
            return this.defaultText;
        }

        public TextBoxElement setId(String value) {
            this.id = value;
            return this;
        }

        public String getId() {
            return this.id;
        }
    }

    public static class ProgressBarElement extends Element {
        private String headerText = "";
        private int width;
        private int overlayColor;
        private float percentage = 1f;
        private int backgroundColor;
        private int headerTextColor = 0x000000;
        private float headerScale = 1f;

        public ProgressBarElement setHeaderText(String value) {
            this.headerText = value;
            return this;
        }

        public String getHeaderText() {
            return this.headerText;
        }

        public ProgressBarElement setColors(int overlayColor, int backgroundColor) {
            this.overlayColor = overlayColor;
            this.backgroundColor = backgroundColor;
            return this;
        }

        public int getOverlayColor() {
            return this.overlayColor;
        }

        public int getBackgroundColor() {
            return this.backgroundColor;
        }

        public ProgressBarElement setWidth(int value) {
            this.width = value;
            return this;
        }

        public int getWidth() {
            return this.width;
        }

        public ProgressBarElement setPercentage(float value) {
            this.percentage = MathHelper.clamp(value, 0, 1f);
            return this;
        }

        public float getPercentage() {
            return this.percentage;
        }

        public ProgressBarElement setHeaderColor(int value) {
            this.headerTextColor = value;
            return this;
        }

        public int getHeaderTextColor() {
            return this.headerTextColor;
        }

        public ProgressBarElement setHeaderScale(float value) {
            this.headerScale = value;
            return this;
        }

        public float getHeaderScale() {
            return this.headerScale;
        }
    }
}
