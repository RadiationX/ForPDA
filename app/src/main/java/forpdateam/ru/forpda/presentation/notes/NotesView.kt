package forpdateam.ru.forpda.presentation.notes

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.app.notes.NoteItem

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface NotesView : IBaseView {
    fun showNotes(items: List<NoteItem>)
    @StateStrategyType(SkipStrategy::class)
    fun showNotesEditPopup(item: NoteItem)

    @StateStrategyType(SkipStrategy::class)
    fun showNotesAddPopup()

    @StateStrategyType(SkipStrategy::class)
    fun onImportNotes()

    @StateStrategyType(SkipStrategy::class)
    fun onExportNotes(path: String)
}
