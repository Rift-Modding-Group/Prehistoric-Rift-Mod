package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.config.RiftCreatureConfig;
import anightdazingzoroark.prift.helper.RiftUtil;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class RiftGuiScrollableSectionContents {
    private final List<Element> list = new ArrayList<>();
    private int totalHeight;

    private void getTotalHeight() {}

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

    public RiftGuiScrollableSectionContents addTabElement(TabElement element) {
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
        private final Map<String, List<Element>> tabContents = new HashMap<>();
        private final List<String> tabOrder = new ArrayList<>();

        public TabElement setId(String id) {
            this.id = id;
            return this;
        }

        public String getId() {
            return this.id;
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
}
