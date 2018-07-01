package net.draycia;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Weapon implements Serializable {

    private static final long serialVersionUID = 133499723390131045L;

    public static final String TAG = "Draycia_69973";

    public abstract String getName();

    public abstract List<String> getLore();

    public abstract ItemStack getItem();

    public abstract String getItemID();

    public abstract void use(Player p);

    public abstract ArrayList<String> getAllowedSpells();

    public abstract BarType getBarType();

    public ItemStack getProcessedItem() {
        ItemStack item = this.getItem();
        net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = stack.hasTag() ? stack.getTag() : new NBTTagCompound();
        tag.setString(Weapon.TAG, this.getItemID());
        stack.setTag(tag);

        return CraftItemStack.asBukkitCopy(stack);
    }

    public static String getWeaponTag(ItemStack item) {
        net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(item);
        if (!stack.hasTag())
            return null;
        NBTTagCompound tag = stack.getTag();
        return tag.getString(Weapon.TAG);
    }
}
