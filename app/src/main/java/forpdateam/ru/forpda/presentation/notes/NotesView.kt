package forpdateam.ru.forpda.presentation.notes

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.notes.NoteItem

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface NotesView : IBaseView {
    fun showNotes(items: List<NoteItem>, info: List<CloseableInfo>)

    @StateStrategyType(SkipStrategy::class)
    fun showNotesEditPopup(item: NoteItem)

    @StateStrategyType(SkipStrategy::class)
    fun showNotesAddPopup()

    @StateStrategyType(SkipStrategy::class)
    fun onImportNotes()

    @StateStrategyType(SkipStrategy::class)
    fun onExportNotes(path: String)
}
