package worlds.gregs.hestia.core.display.update.logic.sync.npc.stages

import world.gregs.hestia.core.network.codec.packet.PacketBuilder
import worlds.gregs.hestia.game.update.SyncStage

object NpcFinishStage : SyncStage {

    override fun encode(builder: PacketBuilder) {
        builder.writeBits(15, 32767)
    }

}