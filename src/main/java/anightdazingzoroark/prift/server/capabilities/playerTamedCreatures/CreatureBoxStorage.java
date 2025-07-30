package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.helper.FixedSizeList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;

//this is a helper class of sorts where how storage of creatures stored in the
//creature box are figured out
public class CreatureBoxStorage {
    public static final int maxBoxAmnt = 20;
    public static final int maxBoxStorableCreatures = 20;
    private final List<String> creatureBoxNames = new ArrayList<>();
    private final List<FixedSizeList<NBTTagCompound>> creatureBoxContents = new ArrayList<>();

    public CreatureBoxStorage() {
        for (int x = 0; x < maxBoxAmnt; x++) {
            //initialize names
            this.creatureBoxNames.add("Box "+(x + 1));

            //initialize contents of each box
            this.creatureBoxContents.add(new FixedSizeList<>(maxBoxStorableCreatures, new NBTTagCompound()));
        }
    }

    //this is for making a new CreatureBoxStorage instance
    //from saved NBT
    public CreatureBoxStorage(NBTTagList savedNBT) {
        for (int x = 0; x < savedNBT.tagCount(); x++) {
            NBTTagCompound nbtFromList = savedNBT.getCompoundTagAt(x);
            //initialize names
            this.creatureBoxNames.add(nbtFromList.getString("BoxName"));

            //initialize creatures
            NBTTagList storedCreaturesNBT = nbtFromList.getTagList("BoxCreatures", 10);
            FixedSizeList<NBTTagCompound> storedCreaturesToAdd = new FixedSizeList<>(storedCreaturesNBT.tagCount(), new NBTTagCompound());
            for (int y = 0; y < storedCreaturesNBT.tagCount(); y++) {
                NBTTagCompound storedCreature = storedCreaturesNBT.getCompoundTagAt(y);
                storedCreaturesToAdd.set(y, storedCreature);
            }
            this.creatureBoxContents.add(storedCreaturesToAdd);
        }
    }

    public String getBoxName(int index) {
        if (index < 0 || index >= maxBoxAmnt) throw new UnsupportedOperationException("Cannot get value beyond bounds");
        return this.creatureBoxNames.get(index);
    }

    public void setBoxName(int index, String newName) {
        if (index < 0 || index >= maxBoxAmnt) throw new UnsupportedOperationException("Cannot get value beyond bounds");
        this.creatureBoxNames.set(index, newName);
    }

    public FixedSizeList<NBTTagCompound> getBoxContents(int index) {
        if (index < 0 || index >= maxBoxAmnt) throw new UnsupportedOperationException("Cannot get value beyond bounds");
        return this.creatureBoxContents.get(index);
    }

    public void setBoxCreature(int index, int indexInBox, NBTTagCompound creatureNBT) {
        if (index < 0 || index >= maxBoxAmnt) throw new UnsupportedOperationException("Cannot get value beyond bounds");
        FixedSizeList<NBTTagCompound> changedList = this.creatureBoxContents.get(index);
        changedList.set(indexInBox, creatureNBT);
        this.creatureBoxContents.set(index, changedList);
    }

    public void addCreatureToBox(NBTTagCompound creatureNBT) {
        for (int x = 0; x < maxBoxAmnt; x++) {
            int validSpace = validSpaceInBox(x);
            if (validSpace >= 0) {
                FixedSizeList<NBTTagCompound> box = this.creatureBoxContents.get(x);
                box.set(validSpace, creatureNBT);
                this.creatureBoxContents.set(x, box);
                break;
            }
        }
    }

    public int validSpaceInBox(int box) {
       int toReturn = -1;
       for (int x = 0; x < this.creatureBoxContents.get(box).size(); x++) {
           NBTTagCompound nbtToTest = this.creatureBoxContents.get(box).get(x);
           if (!nbtToTest.isEmpty()) {
               toReturn = x;
               break;
           }
       }
       return toReturn;
    }

    public NBTTagList writeNBTList() {
        NBTTagList toReturn = new NBTTagList();

        for (int x = 0; x < maxBoxAmnt; x++) {
            NBTTagCompound tagToAdd = new NBTTagCompound();

            //save name
            tagToAdd.setString("BoxName", this.creatureBoxNames.get(x));

            //save creatures
            NBTTagList creaturesNBT = new NBTTagList();
            for (int y = 0; y < maxBoxStorableCreatures; y++) {
                creaturesNBT.appendTag(this.creatureBoxContents.get(x).get(y));
            }
            tagToAdd.setTag("BoxCreatures", creaturesNBT);

            //add to final tag list
            toReturn.appendTag(tagToAdd);
        }

        return toReturn;
    }
}
