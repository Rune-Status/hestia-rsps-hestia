package worlds.gregs.hestia.core.display.update.logic.block.factory.player

import com.artemis.ComponentMapper
import worlds.gregs.hestia.game.update.blocks.player.AppearanceBlock
import worlds.gregs.hestia.core.display.update.api.BlockFactory
import worlds.gregs.hestia.core.display.update.model.components.Appearance
import worlds.gregs.hestia.core.display.update.model.components.AppearanceData
import worlds.gregs.hestia.core.display.update.model.components.Renderable
import worlds.gregs.hestia.artemis.Aspect

class AppearancePlayerBlockFactory(flag: Int, mob: Boolean = false) : BlockFactory<AppearanceBlock>(Aspect.all(Renderable::class, Appearance::class), true, flag = flag, mob = mob) {

    private lateinit var appearanceDataMapper: ComponentMapper<AppearanceData>

    override fun create(player: Int, other: Int): AppearanceBlock {
        val data = appearanceDataMapper.get(other)?.data
        return AppearanceBlock(flag, data ?: byteArrayOf())
    }

}