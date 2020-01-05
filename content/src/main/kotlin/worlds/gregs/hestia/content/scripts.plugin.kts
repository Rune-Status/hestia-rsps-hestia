package worlds.gregs.hestia.content

import worlds.gregs.hestia.content.activity.combat.CombatStyles_script
import worlds.gregs.hestia.content.activity.combat.MagicSpellbook_script
import worlds.gregs.hestia.content.activity.combat.PrayerList_script
import worlds.gregs.hestia.content.activity.quest.QuestJournals_script
import worlds.gregs.hestia.content.activity.skill.Stats_script
import worlds.gregs.hestia.content.command.*
import worlds.gregs.hestia.content.display.windows.Minimap_script
import worlds.gregs.hestia.content.display.windows.fullscreen.Gameframe_script
import worlds.gregs.hestia.content.display.windows.fullscreen.WorldMap_script
import worlds.gregs.hestia.content.display.windows.main.Emotes_script
import worlds.gregs.hestia.content.display.windows.main.Notes_script
import worlds.gregs.hestia.content.display.windows.main.Options_script
import worlds.gregs.hestia.content.interaction.`object`.Counter_script
import worlds.gregs.hestia.content.interaction.item.Inventory_script
import worlds.gregs.hestia.content.interaction.item.Pickup_script
import worlds.gregs.hestia.content.interaction.item.WornEquipment_script
import worlds.gregs.hestia.content.interaction.mob.Banker_script
import worlds.gregs.hestia.content.interaction.mob.Hans_script
import worlds.gregs.hestia.content.interaction.player.Follow_script
import worlds.gregs.hestia.content.interaction.player.ReqAssist_script
import worlds.gregs.hestia.content.interaction.player.Trade_script
import worlds.gregs.hestia.content.display.windows.main.ChatSettings_script
import worlds.gregs.hestia.content.display.windows.main.Logout_script

val scripts = listOf(Logout_script(),
        ChatSettings_script(), MagicSpellbook_script(), Options_script(), Emotes_script(), Inventory_script(), Notes_script(), PrayerList_script(), QuestJournals_script(), Stats_script(), WorldMap_script(), WornEquipment_script(), CombatStyles_script(), Gameframe_script(), Minimap_script(), ReqAssist_script(), Follow_script(), Trade_script(), Item_script(), Pickup_script(), Test_script(), Counter_script(), Banker_script(), Hans_script(), Appearance_script(), BotControl_script(), EntityCount_script(), Interface_script(), Run_script(), Teleport_script())

setup {
    scripts.forEach {
        it.build(this)
    }
}

init { world, dispatcher ->
    scripts.forEach {
        it.build(world, dispatcher)
    }
}