package forpdateam.ru.forpda.entity.app.profile

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.common.Html
import forpdateam.ru.forpda.entity.EntityWrapper
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.extensions.nullString
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject

class UserHolder(
        private val sharedPreferences: SharedPreferences
) : IUserHolder {

    private val currentUserRelay = BehaviorRelay.createDefault(EntityWrapper(user))

    override var user: ProfileModel?
        get() {
            return sharedPreferences
                    .getString("current_user", null)
                    ?.let {
                        val jsonProfile = JSONObject(it)
                        ProfileModel().apply {
                            id = jsonProfile.getInt("id")
                            sign = ApiUtils.coloredFromHtml(jsonProfile.nullString("sign"))
                            about = ApiUtils.spannedFromHtml(jsonProfile.nullString("about"))
                            avatar = jsonProfile.nullString("avatar")
                            nick = jsonProfile.nullString("nick")
                            status = jsonProfile.nullString("status")
                            group = jsonProfile.nullString("group")
                            note = jsonProfile.nullString("note")

                            jsonProfile.getJSONArray("contacts")?.also {
                                for (i in 0 until it.length()) {
                                    val jsonContact = it.getJSONObject(i)
                                    contacts.add(ProfileModel.Contact().apply {
                                        type = ProfileModel.ContactType.valueOf(jsonContact.getString("type"))
                                        url = jsonContact.nullString("url")
                                        title = jsonContact.nullString("title")
                                    })
                                }
                            }

                            jsonProfile.getJSONArray("info")?.also {
                                for (i in 0 until it.length()) {
                                    val jsonInfo = it.getJSONObject(i)
                                    info.add(ProfileModel.Info().apply {
                                        type = ProfileModel.InfoType.valueOf(jsonInfo.getString("type"))
                                        value = jsonInfo.nullString("value")
                                    })
                                }
                            }

                            jsonProfile.getJSONArray("stats")?.also {
                                for (i in 0 until it.length()) {
                                    val jsonStat = it.getJSONObject(i)
                                    stats.add(ProfileModel.Stat().apply {
                                        type = ProfileModel.StatType.valueOf(jsonStat.getString("type"))
                                        url = jsonStat.nullString("url")
                                        value = jsonStat.nullString("value")
                                    })
                                }
                            }

                            jsonProfile.getJSONArray("devices")?.also {
                                for (i in 0 until it.length()) {
                                    val jsonDevice = it.getJSONObject(i)
                                    devices.add(ProfileModel.Device().apply {
                                        url = jsonDevice.nullString("url")
                                        name = jsonDevice.nullString("name")
                                        accessory = jsonDevice.nullString("accessory")
                                    })
                                }
                            }

                            jsonProfile.getJSONArray("warnings")?.also {
                                for (i in 0 until it.length()) {
                                    val jsonContact = it.getJSONObject(i)
                                    warnings.add(ProfileModel.Warning().apply {
                                        type = ProfileModel.WarningType.valueOf(jsonContact.getString("type"))
                                        date = jsonContact.nullString("date")
                                        title = jsonContact.nullString("title")
                                        content = ApiUtils.spannedFromHtml(jsonContact.nullString("content"))
                                    })
                                }
                            }
                        }
                    }
        }
        set(value) {
            currentUserRelay.accept(EntityWrapper(value))
            val result = value?.let { profile ->
                JSONObject().apply {
                    put("id", profile.id)
                    put("sign", profile.sign?.let { Html.toHtml(it) })
                    put("about", profile.about?.let { Html.toHtml(it) })
                    put("avatar", profile.avatar)
                    put("nick", profile.nick)
                    put("status", profile.status)
                    put("group", profile.group)
                    put("note", profile.note)

                    put("contacts", JSONArray().apply {
                        profile.contacts.forEach { contact ->
                            put(JSONObject().apply {
                                put("type", contact.type.toString())
                                put("url", contact.url)
                                put("title", contact.title)
                            })
                        }
                    })

                    put("info", JSONArray().apply {
                        profile.info.forEach { info ->
                            put(JSONObject().apply {
                                put("type", info.type.toString())
                                put("value", info.value)
                            })
                        }
                    })

                    put("stats", JSONArray().apply {
                        profile.stats.forEach { stat ->
                            put(JSONObject().apply {
                                put("type", stat.type.toString())
                                put("url", stat.url)
                                put("value", stat.value)
                            })
                        }
                    })

                    put("devices", JSONArray().apply {
                        profile.devices.forEach { device ->
                            put(JSONObject().apply {
                                put("url", device.url)
                                put("name", device.name)
                                put("accessory", device.accessory)
                            })
                        }
                    })

                    put("warnings", JSONArray().apply {
                        profile.warnings.forEach { warning ->
                            put(JSONObject().apply {
                                put("type", warning.type)
                                put("date", warning.date)
                                put("title", warning.title)
                                put("content", warning.content?.let { Html.toHtml(it) })
                            })
                        }
                    })
                }
            }
            if (result == null) {
                sharedPreferences.edit().remove("current_user").apply()
            } else {
                sharedPreferences.edit().putString("current_user", result.toString()).apply()
            }
        }

    override fun observeCurrentUser(): Observable<EntityWrapper<ProfileModel?>> = currentUserRelay
}
