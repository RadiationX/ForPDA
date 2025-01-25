package forpdateam.ru.forpda.entity.remote.editpost

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import forpdateam.ru.forpda.model.data.remote.IWebClient
import java.util.regex.Pattern

/**
 * Created by radiationx on 09.01.17.
 */
class AttachmentItem : Parcelable {
    var isError: Boolean = false
    var isSelected: Boolean = false
        private set

    var id: Int = -1
    var typeFile: Int = TYPE_FILE
    var loadState: Int = STATE_LOADING
    var status: Int = STATUS_READY
    var width: Int = 0
    var height: Int = 0

    var name: String? = null
    var extension: String? = null
        set(value) {
            field = value
            if (imageExtensions.matcher(value).matches()) this.typeFile = TYPE_IMAGE
        }
    var weight: String? = null
    var imageUrl: String? = null
    var md5: String? = null
    var url: String? = null

    var progress: Int = -1
        private set

    val itemProgressListener: IWebClient.ProgressListener =
        IWebClient.ProgressListener { percent ->
            this@AttachmentItem.progress =
                progress
            if (progressListener != null) progressListener!!.onProgress(percent)
        }
    var progressListener: IWebClient.ProgressListener? = null

    constructor(name: String?) {
        this.name = name
    }

    constructor()


    fun toggle() {
        isSelected = !isSelected
    }

    //PARCELABLE !!!!!!!!AAA!!!!!
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Log.d("FORPDA_LOG", "writeToParcel")
        parcel.writeByte((if (isError) 1 else 0).toByte())
        parcel.writeByte((if (isSelected) 1 else 0).toByte())
        parcel.writeInt(id)
        parcel.writeInt(typeFile)
        parcel.writeInt(loadState)
        parcel.writeInt(status)
        parcel.writeInt(progress)
        writeStringToParcel(parcel, name)
        writeStringToParcel(parcel, extension)
        writeStringToParcel(parcel, weight)
        writeStringToParcel(parcel, imageUrl)
        writeStringToParcel(parcel, url)
    }

    private constructor(parcel: Parcel) {
        isError = parcel.readByte().toInt() != 0
        isSelected = parcel.readByte().toInt() != 0
        id = parcel.readInt()
        typeFile = parcel.readInt()
        loadState = parcel.readInt()
        status = parcel.readInt()
        progress = parcel.readInt()
        name = readStringFromParcel(parcel)
        extension = readStringFromParcel(parcel)
        weight = readStringFromParcel(parcel)
        imageUrl = readStringFromParcel(parcel)
        url = readStringFromParcel(parcel)
    }

    private fun writeStringToParcel(parcel: Parcel, string: String?) {
        parcel.writeByte((if (string != null) 1 else 0).toByte())
        parcel.writeString(string)
    }

    private fun readStringFromParcel(parcel: Parcel): String? {
        return if (parcel.readByte().toInt() != 0) parcel.readString() else null
    }

    companion object {
        private val imageExtensions: Pattern =
            Pattern.compile("gif|jpg|jpeg|png", Pattern.CASE_INSENSITIVE)
        const val TYPE_FILE: Int = 0
        const val TYPE_IMAGE: Int = 1

        const val STATE_NOT_LOADED: Int = 0
        const val STATE_LOADING: Int = 1
        const val STATE_LOADED: Int = 2

        const val STATUS_REMOVED: Int = 0
        const val STATUS_NO_FILE: Int = 1
        const val STATUS_UPLOADED: Int = 2
        const val STATUS_READY: Int = 3
        const val STATUS_UNKNOWN: Int = 4

        @JvmField
        val CREATOR: Parcelable.Creator<AttachmentItem> =
            object : Parcelable.Creator<AttachmentItem> {
                override fun createFromParcel(`in`: Parcel): AttachmentItem {
                    Log.d("FORPDA_LOG", "createFromParcel")
                    return AttachmentItem(`in`)
                }

                override fun newArray(size: Int): Array<AttachmentItem?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
