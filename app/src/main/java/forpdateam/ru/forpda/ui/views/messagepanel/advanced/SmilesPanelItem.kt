package forpdateam.ru.forpda.ui.views.messagepanel.advanced

import android.annotation.SuppressLint
import android.content.Context
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.adapters.PanelItemAdapter

/**
 * Created by radiationx on 08.01.17.
 */
@SuppressLint("ViewConstructor")
class SmilesPanelItem(context: Context, panel: MessagePanel) :
    BasePanelItem(context, panel, get().getString(R.string.smiles_title)) {
    init {
        val adapter = PanelItemAdapter(getSmiles().toMutableList()) {
            if (it is PanelListItem.Smile) {
                messagePanel.insertText(" " + it.text + " ")
            }
        }
        recyclerView.adapter = adapter
    }

    companion object {
        private var smiles: MutableList<PanelListItem.Smile>? = null

        fun getSmiles(): MutableList<PanelListItem.Smile> {
            if (smiles != null) return smiles!!
            val smiles = ArrayList<PanelListItem.Smile>()
            this.smiles = smiles
            smiles.add(PanelListItem.Smile(":happy:", "happy.gif"))
            smiles.add(PanelListItem.Smile(";)", "wink.gif"))
            smiles.add(PanelListItem.Smile(":P", "tongue.gif"))
            smiles.add(PanelListItem.Smile(":-D", "biggrin.gif"))
            smiles.add(PanelListItem.Smile(":lol:", "laugh.gif"))
            smiles.add(PanelListItem.Smile(":rolleyes:", "rolleyes.gif"))
            smiles.add(PanelListItem.Smile(":)", "smile_good.gif"))
            smiles.add(PanelListItem.Smile(":beee:", "beee.gif"))
            smiles.add(PanelListItem.Smile(":rofl:", "rofl.gif"))
            smiles.add(PanelListItem.Smile(":sveta:", "sveta.gif"))
            smiles.add(PanelListItem.Smile(":thank_you:", "thank_you.gif"))
            smiles.add(PanelListItem.Smile("}-)", "devil.gif"))
            smiles.add(PanelListItem.Smile(":girl_cray:", "girl_cray.gif"))
            smiles.add(PanelListItem.Smile(":blush:", "blush.gif"))
            smiles.add(PanelListItem.Smile(":mellow:", "mellow.gif"))
            smiles.add(PanelListItem.Smile(":huh:", "huh.gif"))
            smiles.add(PanelListItem.Smile("B)", "cool.gif"))
            smiles.add(PanelListItem.Smile("-_-", "sleep.gif"))
            smiles.add(PanelListItem.Smile("&lt;_&lt;", "dry.gif"))
            smiles.add(PanelListItem.Smile(":wub:", "wub.gif"))
            smiles.add(PanelListItem.Smile(":angry:", "angry.gif"))
            smiles.add(PanelListItem.Smile(":(", "sad.gif"))
            smiles.add(PanelListItem.Smile(":unsure:", "unsure.gif"))
            smiles.add(PanelListItem.Smile(":wacko:", "wacko.gif"))
            smiles.add(PanelListItem.Smile(":blink:", "blink.gif"))
            smiles.add(PanelListItem.Smile(":ph34r:", "ph34r.gif"))
            smiles.add(PanelListItem.Smile(":banned:", "banned.gif"))
            smiles.add(PanelListItem.Smile(":antifeminism:", "antifeminism.gif"))
            smiles.add(PanelListItem.Smile(":beta:", "beta.gif"))
            smiles.add(PanelListItem.Smile(":boy_girl:", "boy_girl.gif"))
            smiles.add(PanelListItem.Smile(":butcher:", "butcher.gif"))
            smiles.add(PanelListItem.Smile(":bubble:", "bubble.gif"))
            smiles.add(PanelListItem.Smile(":censored:", "censored.gif"))
            smiles.add(PanelListItem.Smile(":clap:", "clap.gif"))
            smiles.add(PanelListItem.Smile(":close_tema:", "close_tema.gif"))
            smiles.add(PanelListItem.Smile(":clapping:", "clapping.gif"))
            smiles.add(PanelListItem.Smile(":coldly:", "coldly.gif"))
            smiles.add(PanelListItem.Smile(":comando:", "comando.gif"))
            smiles.add(PanelListItem.Smile(":dance:", "dance.gif"))
            smiles.add(PanelListItem.Smile(":daisy:", "daisy.gif"))
            smiles.add(PanelListItem.Smile(":dancer:", "dancer.gif"))
            smiles.add(PanelListItem.Smile(":derisive:", "derisive.gif"))
            smiles.add(PanelListItem.Smile(":dinamo:", "dinamo.gif"))
            smiles.add(PanelListItem.Smile(":dirol:", "dirol.gif"))
            smiles.add(PanelListItem.Smile(":diver:", "diver.gif"))
            smiles.add(PanelListItem.Smile(":drag:", "drag.gif"))
            smiles.add(PanelListItem.Smile(":download:", "download.gif"))
            smiles.add(PanelListItem.Smile(":drinks:", "drinks.gif"))
            smiles.add(PanelListItem.Smile(":first_move:", "first_move.gif"))
            smiles.add(PanelListItem.Smile(":feminist:", "feminist.gif"))
            smiles.add(PanelListItem.Smile(":flood:", "flood.gif"))
            smiles.add(PanelListItem.Smile(":fool:", "fool.gif"))
            smiles.add(PanelListItem.Smile(":friends:", "friends.gif"))
            smiles.add(PanelListItem.Smile(":foto:", "foto.gif"))
            smiles.add(PanelListItem.Smile(":girl_blum:", "girl_blum.gif"))
            smiles.add(PanelListItem.Smile(":girl_crazy:", "girl_crazy.gif"))
            smiles.add(PanelListItem.Smile(":girl_curtsey:", "girl_curtsey.gif"))
            smiles.add(PanelListItem.Smile(":girl_dance:", "girl_dance.gif"))
            smiles.add(PanelListItem.Smile(":girl_flirt:", "girl_flirt.gif"))
            smiles.add(PanelListItem.Smile(":girl_hospital:", "girl_hospital.gif"))
            smiles.add(PanelListItem.Smile(":girl_hysterics:", "girl_hysterics.gif"))
            smiles.add(PanelListItem.Smile(":girl_in_love:", "girl_in_love.gif"))
            smiles.add(PanelListItem.Smile(":girl_kiss:", "girl_kiss.gif"))
            smiles.add(PanelListItem.Smile(":girl_pinkglassesf:", "girl_pinkglassesf.gif"))
            smiles.add(PanelListItem.Smile(":girl_parting:", "girl_parting.gif"))
            smiles.add(PanelListItem.Smile(":girl_prepare_fish:", "girl_prepare_fish.gif"))
            smiles.add(PanelListItem.Smile(":good:", "good.gif"))
            smiles.add(PanelListItem.Smile(":girl_spruce_up:", "girl_spruce_up.gif"))
            smiles.add(PanelListItem.Smile(":girl_tear:", "girl_tear.gif"))
            smiles.add(PanelListItem.Smile(":girl_tender:", "girl_tender.gif"))
            smiles.add(PanelListItem.Smile(":girl_teddy:", "girl_teddy.gif"))
            smiles.add(PanelListItem.Smile(":girl_to_babruysk:", "girl_to_babruysk.gif"))
            smiles.add(PanelListItem.Smile(":girl_to_take_umbrage:", "girl_to_take_umbrage.gif"))
            smiles.add(PanelListItem.Smile(":girl_triniti:", "girl_triniti.gif"))
            smiles.add(PanelListItem.Smile(":girl_tongue:", "girl_tongue.gif"))
            smiles.add(PanelListItem.Smile(":girl_wacko:", "girl_wacko.gif"))
            smiles.add(PanelListItem.Smile(":girl_werewolf:", "girl_werewolf.gif"))
            smiles.add(PanelListItem.Smile(":girl_witch:", "girl_witch.gif"))
            smiles.add(PanelListItem.Smile(":grabli:", "grabli.gif"))
            smiles.add(PanelListItem.Smile(":good_luck:", "good_luck.gif"))
            smiles.add(PanelListItem.Smile(":guess:", "guess.gif"))
            smiles.add(PanelListItem.Smile(":hang:", "hang.gif"))
            smiles.add(PanelListItem.Smile(":heart:", "heart.gif"))
            smiles.add(PanelListItem.Smile(":help:", "help.gif"))
            smiles.add(PanelListItem.Smile(":helpsmilie:", "helpsmilie.gif"))
            smiles.add(PanelListItem.Smile(":hemp:", "hemp.gif"))
            smiles.add(PanelListItem.Smile(":heppy_dancing:", "heppy_dancing.gif"))
            smiles.add(PanelListItem.Smile(":hysterics:", "hysterics.gif"))
            smiles.add(PanelListItem.Smile(":indeec:", "indeec.gif"))
            smiles.add(PanelListItem.Smile(":i-m_so_happy:", "i-m_so_happy.gif"))
            smiles.add(PanelListItem.Smile(":kindness:", "kindness.gif"))
            smiles.add(PanelListItem.Smile(":king:", "king.gif"))
            smiles.add(PanelListItem.Smile(":laugh_wild:", "laugh_wild.gif"))
            smiles.add(PanelListItem.Smile(":4PDA:", "love_4PDA.gif"))
            smiles.add(PanelListItem.Smile(":nea:", "nea.gif"))
            smiles.add(PanelListItem.Smile(":moil:", "moil.gif"))
            smiles.add(PanelListItem.Smile(":no:", "no.gif"))
            smiles.add(PanelListItem.Smile(":nono:", "nono.gif"))
            smiles.add(PanelListItem.Smile(":offtopic:", "offtopic.gif"))
            smiles.add(PanelListItem.Smile(":ok:", "ok.gif"))
            smiles.add(PanelListItem.Smile(":papuas:", "papuas.gif"))
            smiles.add(PanelListItem.Smile(":party:", "party.gif"))
            smiles.add(PanelListItem.Smile(":pioneer_smoke:", "pioneer_smoke.gif"))
            smiles.add(PanelListItem.Smile(":pipiska:", "pipiska.gif"))
            smiles.add(PanelListItem.Smile(":protest:", "protest.gif"))
            smiles.add(PanelListItem.Smile(":popcorm:", "popcorm.gif"))
            smiles.add(PanelListItem.Smile(":rabbi:", "rabbi.gif"))
            smiles.add(PanelListItem.Smile(":resent:", "resent.gif"))
            smiles.add(PanelListItem.Smile(":roll:", "roll.gif"))
            smiles.add(PanelListItem.Smile(":rtfm:", "rtfm.gif"))
            smiles.add(PanelListItem.Smile(":russian_garmoshka:", "russian_garmoshka.gif"))
            smiles.add(PanelListItem.Smile(":russian:", "russian.gif"))
            smiles.add(PanelListItem.Smile(":russian_ru:", "russian_ru.gif"))
            smiles.add(PanelListItem.Smile(":scratch_one-s_head:", "scratch_one-s_head.gif"))
            smiles.add(PanelListItem.Smile(":scare:", "scare.gif"))
            smiles.add(PanelListItem.Smile(":search:", "search.gif"))
            smiles.add(PanelListItem.Smile(":secret:", "secret.gif"))
            smiles.add(PanelListItem.Smile(":skull:", "skull.gif"))
            smiles.add(PanelListItem.Smile(":shok:", "shok.gif"))
            smiles.add(PanelListItem.Smile(":sorry:", "sorry.gif"))
            smiles.add(PanelListItem.Smile(":smoke:", "smoke.gif"))
            smiles.add(PanelListItem.Smile(":spiteful:", "spiteful.gif"))
            smiles.add(PanelListItem.Smile(":stop_flood:", "stop_flood.gif"))
            smiles.add(PanelListItem.Smile(":suicide:", "suicide.gif"))
            smiles.add(PanelListItem.Smile(":stop_holywar:", "stop_holywar.gif"))
            smiles.add(PanelListItem.Smile(":superman:", "superman.gif"))
            smiles.add(PanelListItem.Smile(":superstition:", "superstition.gif"))
            smiles.add(PanelListItem.Smile(":tablet_za:", "tablet_protiv.gif"))
            smiles.add(PanelListItem.Smile(":tablet_protiv:", "tablet_za.gif"))
            smiles.add(PanelListItem.Smile(":this:", "this.gif"))
            smiles.add(PanelListItem.Smile(":tomato:", "tomato.gif"))
            smiles.add(PanelListItem.Smile(":to_clue:", "to_clue.gif"))
            smiles.add(PanelListItem.Smile(":tommy:", "tommy.gif"))
            smiles.add(PanelListItem.Smile(":tongue3:", "tongue3.gif"))
            smiles.add(PanelListItem.Smile(":umnik:", "umnik.gif"))
            smiles.add(PanelListItem.Smile(":victory:", "victory.gif"))
            smiles.add(PanelListItem.Smile(":vinsent:", "vinsent.gif"))
            smiles.add(PanelListItem.Smile(":wallbash:", "wallbash.gif"))
            smiles.add(PanelListItem.Smile(":whistle:", "whistle.gif"))
            smiles.add(PanelListItem.Smile(":wink_kind:", "wink_kind.gif"))
            smiles.add(PanelListItem.Smile(":yahoo:", "yahoo.gif"))
            smiles.add(PanelListItem.Smile(":yes:", "yes.gif"))
            smiles.add(PanelListItem.Smile(":&#91;", "confusion.gif"))
            smiles.add(PanelListItem.Smile("&#93;-:{", "girl_devil.gif"))
            smiles.add(PanelListItem.Smile(":*", "kiss.gif"))
            smiles.add(PanelListItem.Smile("@}-'-,-", "give_rose.gif"))
            smiles.add(PanelListItem.Smile(":'(", "cry.gif"))
            smiles.add(PanelListItem.Smile(":-{", "mad.gif"))
            smiles.add(PanelListItem.Smile("=^.^=", "kitten.gif"))
            smiles.add(PanelListItem.Smile("(-=", "girl_hide.gif"))
            smiles.add(PanelListItem.Smile("(-;", "girl_wink.gif"))
            smiles.add(PanelListItem.Smile(")-:{", "girl_angry.gif"))
            smiles.add(PanelListItem.Smile("*-:", "girl_chmok.gif"))
            smiles.add(PanelListItem.Smile(")-:", "girl_sad.gif"))
            smiles.add(PanelListItem.Smile(":girl_mad:", "girl_mad.gif"))
            smiles.add(PanelListItem.Smile("(-:", "girl_smile.gif"))
            smiles.add(PanelListItem.Smile(":acute:", "acute.gif"))
            smiles.add(PanelListItem.Smile(":aggressive:", "aggressive.gif"))
            smiles.add(PanelListItem.Smile(":air_kiss:", "air_kiss.gif"))
            smiles.add(PanelListItem.Smile(":lol_girl:", "girl_haha.gif"))
            smiles.add(PanelListItem.Smile(":ohmy:", "ohmy.gif"))
            smiles.add(PanelListItem.Smile(":smile:", "smile.gif"))

            //Повторяющиеся
            //smiles.add(new ButtonData(":D", "biggrin.gif"));
            //smiles.add(new ButtonData("o.O", "blink.gif"));
            //smiles.add(new ButtonData(":yes2:", "yes.gif"));
            //smiles.add(new ButtonData(":o", "ohmy.gif"));
            //smiles.add(new ButtonData("o_O", "blink.gif"));
            //smiles.add(new ButtonData(":-&#91;", "confusion.gif"));
            //smiles.add(new ButtonData(":'-(", "cry.gif"));
            //smiles.add(new ButtonData(")-':", "girl_cray.gif"));
            //smiles.add(new ButtonData("(;", "girl_wink.gif"));
            //smiles.add(new ButtonData(":-*", "kiss.gif"));
            //smiles.add(new ButtonData(":laugh:", "laugh.gif"));
            //smiles.add(new ButtonData(":-(", "sad.gif"));
            //smiles.add(new ButtonData("8-)", "rolleyes.gif"));
            //smiles.add(new ButtonData(":-)", "smile.gif"));
            //smiles.add(new ButtonData(":-P", "tongue.gif"));
            //smiles.add(new ButtonData(";-)", "wink.gif"));

            //Слишком широкий. Прям ваще. Как С. Барецкий, только еще шире...
            //datas.add(new ButtonData(":congratulate:", "congratulate.gif"));
            return smiles
        }
    }
}
