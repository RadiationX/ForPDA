package forpdateam.ru.forpda.model.data.remote.api.attachments

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPost
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import forpdateam.ru.forpda.model.data.storage.parser.ParserPattern
import java.text.DecimalFormat

class AttachmentsParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.EditPost

    fun parseAttachments(response: String): List<EditPost.Attachment> = patternProvider
        .getParserPattern(scope.scope, scope.attachments)
        .map(response) { matcher ->
            val type = matcher.get(7)?.let { imageUrl ->
                EditPost.Attachment.Type.Image(
                    url = "https:$imageUrl",
                    width = matcher.require(8).toInt(),
                    height = matcher.require(9).toInt()
                )
            } ?: EditPost.Attachment.Type.File
            val size = matcher.require(5).toLong()
            EditPost.Attachment(
                id = matcher.require(1).toInt(),
                name = matcher.require(2),
                extension = matcher.require(3),
                size = size,
                sizeFormatted = readableFileSize(size),
                md5 = matcher.require(6),
                type = type
            )
        }

    fun parseAttachment(response: String, item: AttachmentItem?): AttachmentItem {
        val result = item ?: AttachmentItem()
        patternProvider
            .getParserPattern(scope.scope, scope.attachments)
            .findOnce(response) {
                fillAttachment(result, it)
            }
        return result
    }

    private fun fillAttachment(item: AttachmentItem, matcher: ParserPattern.Matcher): AttachmentItem {
        item.id = matcher.require(1).toInt()
        item.name = matcher.require(2)
        /*try {
            item.setName(URLDecoder.decode(matcher.group(2), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        item.extension = matcher.require(3)
        item.weight = readableFileSize(java.lang.Long.parseLong(matcher.require(5)))
        item.md5 = matcher.require(6)
        matcher.get(7)?.also {
            item.typeFile = AttachmentItem.TYPE_IMAGE
            item.imageUrl = "https:$it"
            item.width = matcher.require(8).toInt()
            item.height = matcher.require(9).toInt()
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