package worlds.gregs.hestia.core.entity.mob.logic.systems.chunk

import net.mostlyoriginal.api.event.common.EventSystem
import worlds.gregs.hestia.core.entity.mob.api.Mob
import worlds.gregs.hestia.core.world.movement.api.systems.ChunkSubscription
import worlds.gregs.hestia.core.entity.mob.model.events.MobChunkChanged

class MobChunkSubscriptionSystem : ChunkSubscription(Mob::class) {
    private lateinit var es: EventSystem

    override fun changedChunk(entityId: Int, from: Int?, to: Int?) {
        es.dispatch(MobChunkChanged(entityId, from, to))
    }
}