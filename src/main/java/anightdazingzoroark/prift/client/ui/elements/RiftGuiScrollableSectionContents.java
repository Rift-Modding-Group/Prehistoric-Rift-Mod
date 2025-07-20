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

    public RiftGuiScrollableSectionContents addClickableSectionElement(ClickableSectionElement element) {
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
        private int widthOffset = 0;
        private int heightOffset = 0;
        private ResourceLocation bgImage;
        private int[] bgImageSize; //size of the entire texture
        private int[] bgImageUV; //uv start pos of texture
        private int[] bgImageSelectedUV; //uv start pos of texture when selected
        private int[] bgUVSize; //size of the portion of the texture to be used
        private float bgImageScale = 1f;
        private boolean bgCentered = false;

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

        public TextElement setWidthOffset(int value) {
            this.widthOffset = value;
            return this;
        }

        public int getWidthOffset() {
            return this.widthOffset;
        }

        public TextElement setHeightOffset(int value) {
            this.heightOffset = value;
            return this;
        }

        public int getHeightOffset() {
            return this.heightOffset;
        }

        public TextElement setBackground(ResourceLocation bgImage, int bgTextureWidth, int bgTextureHeight, int bgUVWidth, int bgUVHeight, int bgUVX, int bgUVY) {
            this.bgImage = bgImage;
            this.bgImageSize = new int[]{bgTextureWidth, bgTextureHeight};
            this.bgImageUV = new int[]{bgUVX, bgUVY};
            this.bgUVSize = new int[]{bgUVWidth, bgUVHeight};
            return this;
        }

        public ResourceLocation getBackground() {
            return this.bgImage;
        }

        public int[] getBGImageSize() {
            return this.bgImageSize;
        }

        public int[] getBGImageUV() {
            return this.bgImageUV;
        }

        public int[] getBGUVSize() {
            return this.bgUVSize;
        }

        public TextElement setBGCentered() {
            this.bgCentered = true;
            return this;
        }

        public boolean getBGCentered() {
            return this.bgCentered;
        }
    }

    public static class ImageElement extends Element {
        private ResourceLocation imageLocation;
        private int[] imageSize;
        private float imageScale = 1f;
        private int bottomSpace = 9;
        private String textContents;
        private float textScale = 1f;

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

        public ImageElement setTextContents(String value) {
            this.textContents = value;
            return this;
        }

        public String getTextContents() {
            return this.textContents;
        }

        public ImageElement setTextScale(float value) {
            this.textScale = value;
            return this;
        }

        public float getTextScale() {
            return this.textScale;
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

    public static class ClickableSectionElement extends Element {
        private int[] size; //size of clickable element
        private String id = "";
        private int bottomSpace = 0;
        private boolean sectionCentered = false;
        private String textContent;
        private int textColor = 0x000000;
        private int textHoveredColor = -1;
        private int textSelectedColor = -1;
        private float textScale = 1f;
        private int[] textOffsets = {0, 0};
        private ResourceLocation imageContent;
        private int[] imageSize; //size of the entire texture
        private int[] imageUV; //uv start pos of texture
        private int[] imageHoveredUV; //uv start pos of texture when hovered
        private int[] imageSelectedUV;
        private int[] uvSize; //size of the portion of the texture to be used
        private float imageScale = 1f;

        public ClickableSectionElement setSize(int width, int height) {
            this.size = new int[]{width, height};
            return this;
        }

        public int[] getSize() {
            return this.size;
        }

        public ClickableSectionElement setID(String value) {
            this.id = value;
            return this;
        }

        public String getID() {
            return this.id;
        }

        public ClickableSectionElement setBottomSpace(int value) {
            this.bottomSpace = value;
            return this;
        }

        public int getBottomSpace() {
            return this.bottomSpace;
        }

        public ClickableSectionElement setCentered() {
            this.sectionCentered = true;
            return this;
        }

        public boolean getSectionCentered() {
            return this.sectionCentered;
        }

        public ClickableSectionElement setTextContent(String value) {
            this.textContent = value;
            return this;
        }

        public String getTextContent() {
            return this.textContent;
        }

        public ClickableSectionElement setTextColor(int value) {
            this.textColor = value;
            return this;
        }

        public int getTextColor() {
            return this.textColor;
        }

        public ClickableSectionElement setTextHoveredColor(int value) {
            this.textHoveredColor = value;
            return this;
        }

        public int getTextHoveredColor() {
            return this.textHoveredColor;
        }

        public ClickableSectionElement setTextSelectedColor(int value) {
            this.textSelectedColor = value;
            return this;
        }

        public int getTextSelectedColor() {
            return this.textSelectedColor;
        }

        public ClickableSectionElement setTextScale(float value) {
            this.textScale = value;
            return this;
        }

        public float getTextScale() {
            return this.textScale;
        }

        public ClickableSectionElement setTextOffsets(int x, int y) {
            this.textOffsets = new int[]{x, y};
            return this;
        }

        public int[] getTextOffsets() {
            return this.textOffsets;
        }

        public ClickableSectionElement setImage(ResourceLocation location, int textureWidth, int textureHeight, int uvWidth, int uvHeight, int uvX, int uvY, int uvHoveredX, int uvHoveredY) {
            this.imageContent = location;
            this.imageSize = new int[]{textureWidth, textureHeight};
            this.imageUV = new int[]{uvX, uvY};
            this.imageHoveredUV = new int[]{uvHoveredX, uvHoveredY};
            this.uvSize = new int[]{uvWidth, uvHeight};
            return this;
        }

        public ResourceLocation getImage() {
            return this.imageContent;
        }

        public int[] getImageSize() {
            return this.imageSize;
        }

        public int[] getImageUV() {
            return this.imageUV;
        }

        public int[] getImageHoveredUV() {
            return this.imageHoveredUV;
        }

        public int[] getUVSize() {
            return this.uvSize;
        }

        public ClickableSectionElement setImageSelectedUV(int uvX, int uvY) {
            this.imageSelectedUV = new int[]{uvX, uvY};
            return this;
        }

        public int[] getImageSelectedUV() {
            return this.imageSelectedUV;
        }

        public ClickableSectionElement setImageScale(float value) {
            this.imageScale = value;
            return this;
        }

        public float getImageScale() {
            return this.imageScale;
        }
    }
}
