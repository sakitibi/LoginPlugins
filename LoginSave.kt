import net.minecraft.nbt.NbtCompound
import net.minecraft.world.PersistentState
import net.minecraft.world.PersistentStateManager
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier

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