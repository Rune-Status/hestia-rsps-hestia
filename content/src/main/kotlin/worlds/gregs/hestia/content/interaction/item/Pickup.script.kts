package worlds.gregs.hestia.content.interaction.item

import worlds.gregs.hestia.core.display.client.model.events.Chat
import worlds.gregs.hestia.core.display.update.model.components.direction.Face
import worlds.gregs.hestia.core.display.update.model.components.direction.Facing
import worlds.gregs.hestia.core.entity.entity.model.components.Position
import worlds.gregs.hestia.core.entity.entity.model.components.Type
import worlds.gregs.hestia.core.entity.entity.model.events.Animate
import worlds.gregs.hestia.core.entity.item.container.api.ItemResult
import worlds.gregs.hestia.core.entity.item.floor.model.components.Amount
import worlds.gregs.hestia.core.entity.item.floor.model.events.FloorItemOption
import worlds.gregs.hestia.core.task.logic.systems.RouteSuspension
import worlds.gregs.hestia.core.task.logic.systems.TickSuspension
import worlds.gregs.hestia.core.world.movement.model.components.calc.Route

on<FloorItemOption> {
    where { option == "Take" }
    fun FloorItemOption.task() = queue(TaskPriority.High) {
        val route = entity create Route::class
        route.entityId = target
        route.alternative = true

        val route2 = await(RouteSuspension())
        val interact = canInteract(route2, entity get Position::class, target get Position::class, target)
        await(TickSuspension(1))
        if(!interact) {
            val position = target.get(Position::class)
            entity.get(Face::class).apply {
                x = position.x
                y = position.y
            }
            entity create Facing::class
            entity perform Chat("You can't reach that.")
            return@queue
        }

        //Get floor item info
        val amount = target get Amount::class
        val type = target get Type::class

        //Attempt to add to inventory
        when(entity overflow false transform add(type.id, amount.amount)) {
            is ItemResult.Success -> {
                if (!entity.get(Position::class).same(target.get(Position::class))) {
                    entity perform(Animate(832))//or 3864
                }
                //Remove floor item
                world.delete(target)
            }
            is ItemResult.Issue ->
                entity perform Chat("You don't have enough inventory space to hold that item.")
        }
    }
    then(FloorItemOption::task)
}