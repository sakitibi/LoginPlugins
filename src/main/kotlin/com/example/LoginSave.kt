package com.example

import java.util.UUID
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.text.Text
import net.minecraft.world.PersistentState
import net.minecraft.world.PersistentStateManager

class LoginState : PersistentState() {
    val logins: MutableMap<String, Boolean> = mutableMapOf()
    val waitingForConfirmation: MutableSet<UUID> = mutableSetOf()

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        val players = NbtCompound()
        for ((uuid, loggedIn) in logins) {
            players.putBoolean(uuid, loggedIn)
        }
        nbt.put("players", players)
        return nbt
    }

    fun hasAnyoneLoggedIn(): Boolean {
        return logins.values.any { it }
    }

    fun updateGlobalLoginScore(server: MinecraftServer) {
        val scoreboard: Scoreboard = server.scoreboard
        var objective: ScoreboardObjective? = scoreboard.getObjective("any_logged_in")

        if (objective == null) {
            objective = scoreboard.addObjective(
                "any_logged_in",
                ScoreboardCriterion.DUMMY,
                Text.literal("Any Login"),
                ScoreboardCriterion.RenderType.INTEGER
            )
            scoreboard.setObjectiveSlot(0, objective)
        }

        val score = scoreboard.getPlayerScore("global", objective)
        score.score = if (hasAnyoneLoggedIn()) 1 else 0
    }

    companion object {
        private const val STATE_KEY = "login_state"

        fun createFromNbt(nbt: NbtCompound): LoginState {
            val state = LoginState()
            val players = nbt.getCompound("players")
            for (key in players.keys) {
                state.logins[key] = players.getBoolean(key)
            }
            return state
        }

        fun get(server: MinecraftServer): LoginState {
            val manager: PersistentStateManager = server.overworld.persistentStateManager
            return manager.getOrCreate(::createFromNbt, ::LoginState, STATE_KEY)
        }
    }
}
