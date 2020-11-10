import CapeType.*
import com.google.gson.annotations.SerializedName
import java.util.*

data class CapeUser(
    val id: Long,
    var capes: ArrayList<Cape>,
    @SerializedName("is_premium")
    var isPremium: Boolean = false
)

data class Cape(
    @SerializedName("player_uuid")
    var playerUUID: String?,
    @SerializedName("cape_uuid")
    val capeUUID: String = UUID.randomUUID().toString().substring(0, 5),
    val type: CapeType,
    var color: CapeColor = getColor(type)
)

data class MojangName(
    val name: String,
    val changedToAt: Long?
)

data class MojangProfile(
    val name: String,
    @SerializedName("id")
    val uuidWithoutDash: String,
    val uuid: String = uuidWithoutDash.insertDashes()
)

data class User(
    val uuid: String,
    val names: List<MojangName>,
    val currentMojangName: MojangName = names.last(),
    val currentName: String = currentMojangName.name
)

data class CapeColor(
    val primary: String,
    val border: String
) {
    override fun toString(): String {
        return "#$primary, #$border"
    }
}

enum class CapeType(val realName: String, val imageKey: String) {
    BOOSTER("Booster", "booster"),
    CONTEST("Contest", "contest"),
    CONTRIBUTOR("Contributor", "github1"),
    DONOR("Donor", "donator2"),
    INVITER("Inviter", "inviter"),
    SPECIAL("Special", "special")
}

fun getColor(type: CapeType) = when (type) {
    BOOSTER -> CapeColor("e68cc8", "ffa0e6")
    CONTEST -> CapeColor("90b3ff", "3869d1")
    CONTRIBUTOR -> CapeColor("333333", "211f1f")
    INVITER -> CapeColor("de90ff", "9c30c9") // todo need better colors
    else -> CapeColor("9b90ff", "8778ff") // DONOR and SPECIAL
}
