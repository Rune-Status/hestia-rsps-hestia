package worlds.gregs.hestia.core.display.client.logic.systems.network.`in`

import com.artemis.ComponentMapper
import com.artemis.EntitySubscription
import net.mostlyoriginal.api.event.common.EventSystem
import org.slf4j.LoggerFactory
import worlds.gregs.hestia.GameServer
import worlds.gregs.hestia.artemis.Aspect
import worlds.gregs.hestia.artemis.toArray
import worlds.gregs.hestia.core.display.widget.api.UserInterface
import worlds.gregs.hestia.core.entity.`object`.model.components.GameObject
import worlds.gregs.hestia.core.entity.entity.model.components.Position
import worlds.gregs.hestia.core.entity.item.container.model.Inventory
import worlds.gregs.hestia.core.entity.item.floor.model.events.ItemOnObject
import worlds.gregs.hestia.core.world.land.model.components.LandObjects
import worlds.gregs.hestia.core.world.region.model.components.RegionIdentifier
import worlds.gregs.hestia.game.entity.MessageHandlerSystem
import worlds.gregs.hestia.network.client.decoders.messages.WidgetOnObject

class WidgetOnObjectHandler : MessageHandlerSystem<WidgetOnObject>() {

    private lateinit var es: EventSystem

    private lateinit var inventoryMapper: ComponentMapper<Inventory>
    private val logger = LoggerFactory.getLogger(WidgetOnObjectHandler::class.java)!!
    private lateinit var regionIdentifierMapper: ComponentMapper<RegionIdentifier>
    private lateinit var landObjectsMapper: ComponentMapper<LandObjects>
    private lateinit var gameObjectMapper: ComponentMapper<GameObject>
    private lateinit var positionMapper: ComponentMapper<Position>
    private lateinit var regions: EntitySubscription
    private lateinit var ui: UserInterface

    override fun initialize() {
        super.initialize()
        GameServer.gameMessages.bind(this)
        regions = world.aspectSubscriptionManager.get(Aspect.all(RegionIdentifier::class, LandObjects::class))
    }

    override fun handle(entityId: Int, message: WidgetOnObject) {
        val (_, y, slot, hash, type, x, id) = message
        val inventory = inventoryMapper.get(entityId) ?: return logger.warn("Unhandled widget on object $message")

        if(!ui.validate(entityId, hash)) {
            return logger.warn("Invalid widget on object hash $message")
        }

        val inventoryItem = inventory.items.getOrNull(slot)

        if(inventoryItem == null || inventoryItem.type != type) {
            return logger.warn("Invalid widget on object item $message")
        }

        //Find region
        val position = positionMapper.get(entityId)
        val objectPosition = Position.create(x, y, position.plane)
        val regionId = regions.entities.toArray().firstOrNull { regionIdentifierMapper.get(it).regionX == objectPosition.regionX && regionIdentifierMapper.get(it).regionY == objectPosition.regionY }
                ?: return logger.warn("Invalid widget on object region $message")

        //Find object
        val landObjects = landObjectsMapper.get(regionId)
        val pos = Position.hash18Bit(objectPosition.xInRegion, objectPosition.yInRegion, objectPosition.plane)
        val objectId = landObjects.list[pos]?.firstOrNull { gameObjectMapper.get(it).id == id }
                ?: return logger.warn("Invalid widget on object message $message")

        es.dispatch(ItemOnObject(entityId, objectId, hash, slot, type))
    }
}