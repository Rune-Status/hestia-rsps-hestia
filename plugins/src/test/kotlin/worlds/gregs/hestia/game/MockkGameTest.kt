package worlds.gregs.hestia.game

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import io.mockk.spyk
import net.mostlyoriginal.api.event.common.EventSystem
import org.junit.jupiter.api.BeforeEach
import worlds.gregs.hestia.game.archetypes.EntityFactory

abstract class MockkGameTest(private val config: WorldConfigurationBuilder = WorldConfigurationBuilder()) {

    lateinit var world: World
    lateinit var es: EventSystem

    @BeforeEach
    open fun setup() {
        es = spyk(EventSystem())
        config.with(es)
        config(config)
        val config = config.build()
        world = spyk(World(config))
        EntityFactory.init(world)
        start()
    }

    open fun start() {
    }

    open fun config(config: WorldConfigurationBuilder) {
    }

    open fun tick() {
        world.process()
    }

}