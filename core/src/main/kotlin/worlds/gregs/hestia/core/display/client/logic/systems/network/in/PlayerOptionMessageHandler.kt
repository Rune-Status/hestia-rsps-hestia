package worlds.gregs.hestia.core.display.client.logic.systems.network.`in`

import com.artemis.ComponentMapper
import net.mostlyoriginal.api.event.common.EventSystem
import org.slf4j.LoggerFactory
import worlds.gregs.hestia.GameServer
import worlds.gregs.hestia.core.display.client.model.components.Viewport
import worlds.gregs.hestia.core.display.window.api.Requests
import worlds.gregs.hestia.core.display.window.model.PlayerOptions
import worlds.gregs.hestia.core.display.window.model.Request
import worlds.gregs.hestia.core.display.window.model.events.PlayerOption
import worlds.gregs.hestia.game.entity.MessageHandlerSystem
import worlds.gregs.hestia.network.client.decoders.messages.PlayerOptionMessage

class PlayerOptionMessageHandler : MessageHandlerSystem<PlayerOptionMessage>() {

    private lateinit var es: EventSystem

    private val logger = LoggerFactory.getLogger(PlayerOptionMessageHandler::class.java)!!
    private lateinit var viewportMapper: ComponentMapper<Viewport>
    private lateinit var requests: Requests

    override fun initialize() {
        super.initialize()
        GameServer.gameMessages.bind(this)
    }

    override fun handle(entityId: Int, message: PlayerOptionMessage) {
        val (playerIndex, option) = message

        //Find player
        val viewport = viewportMapper.get(entityId)
        val playerId = viewport.localPlayers().getEntity(playerIndex)
        if(playerId == -1) {
            return logger.warn("Invalid player index $message")
        }

        //Find option
        val choice = PlayerOptions.getOption(option)
        val request = Request.getRequest(option)
        if(choice == null && request == null) {
            return logger.warn("Cannot find player option $message")
        }

        if(request != null && requests.hasRequest(playerId, entityId, request)) {
            requests.respond(playerId, entityId, request)
        } else if(choice != null) {
            es.dispatch(PlayerOption(entityId, playerId, choice))
        }
    }
}