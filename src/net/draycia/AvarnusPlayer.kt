package net.draycia

import net.draycia.spells.SpellBook
import net.draycia.weapons.Weapon
import net.draycia.weapons.WeaponStorage
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

import kotlin.io.Serializable
import kotlin.util.HashMap
import kotlin.util.UUID
import kotlin.collections.HashMap

class AvarnusPlayer(var uuid : UUID, var spellbook : HashMap<String, SpellBook>): Serializable {
    companion object {
        @JvmStatic private val serialVersionUID: Long = -2773227562613105767L;
    }

    init {
        this.resources.put("mana", ResourceBar(100, 100.0, 1.0))
        this.resources.put("energy", ResourceBar(100, 100.0, 5.0))
        this.resources.put("rage", ResourceBar(20, 20.0, -1.0))
        this.resources.put("void", ResourceBar(6, 3.0, 0.0))
    }

    var isStunned = false
    var stunDuration : Double = 0.0

    var customBarColors = HashMap<String, BarColor>
    var customBarStyles = HashMap<String, BarStyle>
    var showBarsWhenSneaking : Boolean = false
    var showHealthBar : Boolean = true

    var resources = HashMap<String, ResourceBar>()






}