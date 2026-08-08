package anightdazingzoroark.prift.api.creature.config;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * meant for each individual creature that has been registered
 * */
public class RiftCreatureConfig {
    //target groups or individual entities by their id this creature can target
    //target group prioritized first
    @SerializedName("targetWhitelist")
    public List<String> targetWhitelist = new ArrayList<>();

    //target groups or individual entities by their id this creature cannot target
    @SerializedName("targetBlacklist")
    public List<String> targetBlacklist = new ArrayList<>();

    //food groups or individual food items this creature can eat
    //individual food items are prioritized over entries w same itemId in groups
    @SerializedName("foodItemWhitelist")
    public List<RiftCreatureFood> foodItemWhitelist = new ArrayList<>();

    //food groups or individual food items this creature cannot eat
    //is strings as... well...
    //food group prioritized first
    @SerializedName("foodItemBlacklist")
    public List<String> foodItemBlacklist = new ArrayList<>();
}
