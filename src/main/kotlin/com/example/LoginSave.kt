package com.example

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.text.Text
import net.minecraft.world.PersistentState
import net.minecraft.world.PersistentStateManager

class LoginState : PersistentState() {
    val logins = mutableMapOf<String, Boolean>()

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

        // Objectiveが存在しない場合、新たに作成
        if (objective == null) {
            objective = scoreboard.addObjective(
                "any_logged_in",
                ScoreboardCriterion.DUMMY,
                Text.literal("Any Login"),
                ScoreboardCriterion.RenderType.INTEGER
            )
            scoreboard.setObjectiveSlot(0, objective)
        }

        // プレイヤーのスコアを取得または作成
        val score = scoreboard.getPlayerScore("global", objective)
        score.score = if (hasAnyoneLoggedIn()) 1 else 0
    }

    companion object {
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
            return manager.getOrCreate(
                ::createFromNbt,
                ::LoginState,
                "login_state"
            )
        }
    }
}
