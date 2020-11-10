import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request

val cachedNames = hashMapOf<String, String>() // UUID, name

fun cachedName(uuid: String?): String? {
    if (uuid == null) return null
    if (!uuid.isUUID()) return uuid

    return cachedNames[uuid] ?: run {
        val user = getFromUUID(uuid) ?: return@run null
        cachedNames[user.uuid] = user.currentMojangName.name

        user.currentMojangName.name
    }
}

fun getFromUUID(uuid: String): User? {
    if (!uuid.isUUID()) return null

    val names = getNamesFromUUID(uuid) ?: return null

    cachedNames[uuid] = names.last().name
    return User(uuid, names)
}

fun getFromName(name: String): User? {
    if (name.isUUID()) return getFromUUID(name)

    val profile = getProfileFromName(name) ?: return null
    val names = getNamesFromUUID(profile.uuid) ?: return null

    cachedNames[profile.uuid] = profile.name
    return User(profile.uuid, names)
}

fun getProfileFromName(name: String): MojangProfile? {
    val foundProfile = getProfileFromName0(name) ?: return null
    return MojangProfile(foundProfile.name, foundProfile.uuidWithoutDash) // used because Gson doesn't serialize uuidWithoutDash.insertDashes()
}

fun getNamesFromUUID(uuid: String): List<MojangName>? {
    if (!uuid.isUUID()) return null

    val url = "https://api.mojang.com/user/profiles/$uuid/names".replace("-", "")
    val request = Request.Builder().url(url).build()
    val response = OkHttpClient().newCall(request).execute()
    val json = response.body()?.string() ?: return null

    if (json.isEmpty()) return null // uuid doesn't exist

    // this will just return null if it somehow fails, no exception thrown
    return Gson().fromJson(json, object : TypeToken<List<MojangName>>() {}.type)
}

private fun getProfileFromName0(name: String): MojangProfile? {
    if (name.isUUID()) return null

    val url = "https://api.mojang.com/users/profiles/minecraft/$name"
    val request = Request.Builder().url(url).build()
    val response = OkHttpClient().newCall(request).execute()
    val json = response.body()?.string() ?: return null

    if (json.isEmpty()) return null // name doesn't exist

    return Gson().fromJson(json, MojangProfile::class.java)
}

fun String.insertDashes() = StringBuilder(this)
    .insert(8, '-')
    .insert(13, '-')
    .insert(18, '-')
    .insert(23, '-')
    .toString()

fun String.isUUID() = Regex("[a-z0-9].{7}-[a-z0-9].{3}-[a-z0-9].{3}-[a-z0-9].{3}-[a-z0-9].{11}").matches(this)

/** @return a properly formatted UUID, null if can't be formatted */
fun String.fixedUUID(): String? {
    if (this.isUUID()) return this
    if (length < 32) return null
    val fixed = this.insertDashes()
    if (fixed.isUUID()) return fixed
    return null
}