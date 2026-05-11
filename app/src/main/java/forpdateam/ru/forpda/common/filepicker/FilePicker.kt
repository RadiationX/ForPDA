package forpdateam.ru.forpda.common.filepicker

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import forpdateam.ru.forpda.model.data.remote.api.RequestFile


class FilePicker(
    private val resultLauncher: ActivityResultLauncher<Array<String>>
) {

    fun launch(onlyImages: Boolean = false) {
        val type = if (onlyImages) {
            "image/*"
        } else {
            "*/*"
        }
        resultLauncher.launch(arrayOf(type))
    }
}

fun Fragment.registerFilePicker(callback: (RequestFile) -> Unit): FilePicker {
    val launcher: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            callback(RequestFile(it))
        }
    }
    return FilePicker(launcher)
}

fun Fragment.registerFilesPicker(callback: (List<RequestFile>) -> Unit): FilePicker {
    val launcher: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        callback(it.map { RequestFile(it) })
    }
    return FilePicker(launcher)
}

