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
    var spellBook = HashMap<String, SpellBook>()

    @Transient var bossBars = HashMap<String, BossBar>()

    var weapons = WeaponStorage()

    fun stun(duration: Double) {
        if(stunDuration < duration) {
            isStunned = true
            stunDuration = duration
            val player = Bukkit.getPlayer(this.uuid)
            player.setGlowing(true)
            Main.scoreboard.getTeam("StunStatus").addPlayer(player)
        }
    }

    fun poison(duration : Int, amplifier : Int) {
        val player = Bukkit.getPlayer(this.uuid)
        player.addPotionEffect(PotionEffect(PotionEffectType.POISON, duration, amplifier))
    }

    fun hideAllResourceBars() {
        /* If bossBars.values() doesn't work, try bossBars.values, ignoring the () */
        for(value in bossBars.values()) {
            value.setVisible(false)
        }
        if(this.showHealthBar) this.bossBars.get("health").setVisible(true)
    }

    fun handleResourceRegen() {
        /* The same applies here. Try ignoring the () */
        for(entry in resources.entrySet()) {
            var resourceBar = entry.getValue()
            var regen : Double = resourceBar.regen / 4

            if (regen < 0 && resourceBar.current + regen <= 0)
                resourceBar.current = 0.0
            else if (regen > 0 && resourceBar.current + regen >= resourceBar.cap)
                resourceBar.current = resourceBar.cap.toDouble()
            else
                resourceBar.current += regen

            System.out.println("Current: [" + resourceBar.current + "], Cap: [" + resourceBar.cap + "], Percentage: [" + resourceBar.current / resourceBar.cap)

            this.bossBars.get(entry.getKey()).setProgress(resourceBar.current / resourceBar.cap)
        }
    }

    fun storeWeapon(player : Player) {
        var itemStack = player.getInventory().getItemInMainHand()
        var itemTag = Weapon.getWeaponTag(ItemStack)
        if(itemTag === null || itemTag.isEmpty()) return
        if(!Main.instance.weapons.containsKey(itemTag)) return
        var weapon = Main.instance.weapons.get(itemTag)
        weapons.storeWeapon(weapon)
        player.getInventory().setItemInMainHand(null)
    }

    fun getWeapon(player : Player) {
        var itemStack = this.weapons.getWeapon().getProcessedItem()
        player.getInventory().setItemInMainHand(itemStack)
    }

    fun nextWeapon() {

    }

    fun previousWeapon() {

    }

    fun updateWeapon() {

    }
}