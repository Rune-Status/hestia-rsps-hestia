package world.gregs.hestia.game.component.update

import com.artemis.Component
import com.artemis.annotations.PooledWeaver

@PooledWeaver
class Transform : Component() {
    var mobId: Int = -1
}