package net.draycia.spells;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

public abstract class Spell implements Serializable {
    private static final long serialVersionUID = 3577290846703799661L;

    public static final String TAG = "Draycia_24689";

    public abstract String getSpellID();

    public abstract String getName();

    public abstract void cast(Player p, int duration, int damage, int range);

    public ItemStack getSpellBook() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK, 1);
        net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = stack.hasTag() ? stack.getTag() : new NBTTagCompound();
        tag.setString(Spell.TAG, this.getSpellID());
        stack.setTag(tag);

        return CraftItemStack.asBukkitCopy(stack);
    }

    public static String getSpellTag(ItemStack item) {
        net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(item);
        if (!stack.hasTag())
            return null;
        NBTTagCompound tag = stack.getTag();
        return tag.getString(Spell.TAG);
    }
}
