package forpdateam.ru.forpda.model.data.remote.api.attachments

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPost
import forpdateam.ru.forpda.extensions.findOnce
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import java.text.DecimalFormat
import java.util.regex.Matcher

class AttachmentsParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.EditPost

    fun parseAttachments(response: String): List<EditPost.Attachment> = patternProvider
        .getPattern(scope.scope, scope.attachments)
        .matcher(response)
        .map { matcher ->
            val type = matcher.group(7)?.let { imageUrl ->
                EditPost.Attachment.Type.Image(
                    url = "https:$imageUrl",
                    width = matcher.group(8).toInt(),
                    height = matcher.group(9).toInt()
                )
            } ?: EditPost.Attachment.Type.File
            val size = matcher.group(5).toLong()
            EditPost.Attachment(
                id = matcher.group(1).toInt(),
                name = matcher.group(2),
                extension = matcher.group(3),
                size = size,
                sizeFormatted = readableFileSize(size),
                md5 = matcher.group(6),
                type = type
            )
        }

    fun parseAttachment(response: String, item: AttachmentItem?): AttachmentItem {
        val result = item ?: AttachmentItem()
        patternProvider
            .getPattern(scope.scope, scope.attachments)
            .matcher(response)
            .findOnce {
                fillAttachment(result, it)
            }
        return result
    }

    private fun fillAttachment(item: AttachmentItem, matcher: Matcher): AttachmentItem {
        item.id = matcher.group(1).toInt()
        item.name = matcher.group(2)
        /*try {
            item.setName(URLDecoder.decode(matcher.group(2), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        item.extension = matcher.group(3)
        var temp: String? = readableFileSize(java.lang.Long.parseLong(matcher.group(5)))
        item.weight = temp
        item.md5 = matcher.group(6)
        temp = matcher.group(7)

        if (temp != null) {
            item.typeFile = AttachmentItem.TYPE_IMAGE
            item.imageUrl = "https:$temp"
            item.width = matcher.group(8).toInt()
            item.height = matcher.group(9).toInt()
        }
        item.loadState = AttachmentItem.STATE_LOADED
        return item
    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.##").format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
    }
}