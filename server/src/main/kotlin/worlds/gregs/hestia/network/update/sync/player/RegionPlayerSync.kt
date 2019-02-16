package worlds.gregs.hestia.network.update.sync.player

import world.gregs.hestia.core.network.codec.packet.PacketBuilder
import worlds.gregs.hestia.game.entity.Position
import worlds.gregs.hestia.game.update.Direction
import worlds.gregs.hestia.game.update.sync.SyncStage
import worlds.gregs.hestia.game.update.sync.SyncStage.Companion.REGION

data class RegionPlayerSync(val position: Position, val lastPosition: Position) : SyncStage {

    override fun encode(builder: PacketBuilder) {
        builder.writeBits(1, true)//View Update
        builder.writeBits(2, REGION)//Movement type
        //Calculate movement since last seen position
        Position.regionDelta(position, lastPosition) { deltaX, deltaY, deltaPlane ->
            val direction = Direction.getDirection(deltaX, deltaY)
            builder.writeBits(5, (deltaPlane shl 3) + (direction and 0x7))
        }
    }

}