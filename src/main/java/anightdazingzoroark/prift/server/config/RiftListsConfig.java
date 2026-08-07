package anightdazingzoroark.prift.server.config;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RiftListsConfig {
    /**
     * A configurable common list of entities that can be targeted by a creature.
     * */
    @SerializedName("targetGroups")
    public Map<String, List<String>> targetGroups = new LinkedHashMap<>();

    /**
     * A configurable common list of foods a creature can eat.
     * */
    @SerializedName("foodGroups")
    public Map<String, List<RiftCreatureFood>> foodGroups = new LinkedHashMap<>();
}
