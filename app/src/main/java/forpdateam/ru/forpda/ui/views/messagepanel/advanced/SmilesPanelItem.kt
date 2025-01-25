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
        val adapter = PanelItemAdapter(
            getSmiles(),
            urlToAssets, PanelItemAdapter.TYPE_ASSET
        )
        adapter.setOnItemClickListener { item: ButtonData ->
            messagePanel.insertText(" " + item.text + " ")
        }
        recyclerView.adapter = adapter
    }

    companion object {
        private var smiles: MutableList<ButtonData>? = null
        var urlToAssets: MutableList<String>? = null
            get() {
                if (field != null) return field
                val urls = ArrayList<String>()
                field = urls
                for (data in getSmiles()) {
                    urls.add("assets://smiles/" + data.icon)
                }

                return urls
            }
            private set

        fun getSmiles(): List<ButtonData> {
            if (smiles != null) return smiles!!
            val smiles = ArrayList<ButtonData>()
            this.smiles = smiles
            smiles.add(ButtonData(":happy:", "happy.gif"))
            smiles.add(ButtonData(";)", "wink.gif"))
            smiles.add(ButtonData(":P", "tongue.gif"))
            smiles.add(ButtonData(":-D", "biggrin.gif"))
            smiles.add(ButtonData(":lol:", "laugh.gif"))
            smiles.add(ButtonData(":rolleyes:", "rolleyes.gif"))
            smiles.add(ButtonData(":)", "smile_good.gif"))
            smiles.add(ButtonData(":beee:", "beee.gif"))
            smiles.add(ButtonData(":rofl:", "rofl.gif"))
            smiles.add(ButtonData(":sveta:", "sveta.gif"))
            smiles.add(ButtonData(":thank_you:", "thank_you.gif"))
            smiles.add(ButtonData("}-)", "devil.gif"))
            smiles.add(ButtonData(":girl_cray:", "girl_cray.gif"))
            smiles.add(ButtonData(":blush:", "blush.gif"))
            smiles.add(ButtonData(":mellow:", "mellow.gif"))
            smiles.add(ButtonData(":huh:", "huh.gif"))
            smiles.add(ButtonData("B)", "cool.gif"))
            smiles.add(ButtonData("-_-", "sleep.gif"))
            smiles.add(ButtonData("&lt;_&lt;", "dry.gif"))
            smiles.add(ButtonData(":wub:", "wub.gif"))
            smiles.add(ButtonData(":angry:", "angry.gif"))
            smiles.add(ButtonData(":(", "sad.gif"))
            smiles.add(ButtonData(":unsure:", "unsure.gif"))
            smiles.add(ButtonData(":wacko:", "wacko.gif"))
            smiles.add(ButtonData(":blink:", "blink.gif"))
            smiles.add(ButtonData(":ph34r:", "ph34r.gif"))
            smiles.add(ButtonData(":banned:", "banned.gif"))
            smiles.add(ButtonData(":antifeminism:", "antifeminism.gif"))
            smiles.add(ButtonData(":beta:", "beta.gif"))
            smiles.add(ButtonData(":boy_girl:", "boy_girl.gif"))
            smiles.add(ButtonData(":butcher:", "butcher.gif"))
            smiles.add(ButtonData(":bubble:", "bubble.gif"))
            smiles.add(ButtonData(":censored:", "censored.gif"))
            smiles.add(ButtonData(":clap:", "clap.gif"))
            smiles.add(ButtonData(":close_tema:", "close_tema.gif"))
            smiles.add(ButtonData(":clapping:", "clapping.gif"))
            smiles.add(ButtonData(":coldly:", "coldly.gif"))
            smiles.add(ButtonData(":comando:", "comando.gif"))
            smiles.add(ButtonData(":dance:", "dance.gif"))
            smiles.add(ButtonData(":daisy:", "daisy.gif"))
            smiles.add(ButtonData(":dancer:", "dancer.gif"))
            smiles.add(ButtonData(":derisive:", "derisive.gif"))
            smiles.add(ButtonData(":dinamo:", "dinamo.gif"))
            smiles.add(ButtonData(":dirol:", "dirol.gif"))
            smiles.add(ButtonData(":diver:", "diver.gif"))
            smiles.add(ButtonData(":drag:", "drag.gif"))
            smiles.add(ButtonData(":download:", "download.gif"))
            smiles.add(ButtonData(":drinks:", "drinks.gif"))
            smiles.add(ButtonData(":first_move:", "first_move.gif"))
            smiles.add(ButtonData(":feminist:", "feminist.gif"))
            smiles.add(ButtonData(":flood:", "flood.gif"))
            smiles.add(ButtonData(":fool:", "fool.gif"))
            smiles.add(ButtonData(":friends:", "friends.gif"))
            smiles.add(ButtonData(":foto:", "foto.gif"))
            smiles.add(ButtonData(":girl_blum:", "girl_blum.gif"))
            smiles.add(ButtonData(":girl_crazy:", "girl_crazy.gif"))
            smiles.add(ButtonData(":girl_curtsey:", "girl_curtsey.gif"))
            smiles.add(ButtonData(":girl_dance:", "girl_dance.gif"))
            smiles.add(ButtonData(":girl_flirt:", "girl_flirt.gif"))
            smiles.add(ButtonData(":girl_hospital:", "girl_hospital.gif"))
            smiles.add(ButtonData(":girl_hysterics:", "girl_hysterics.gif"))
            smiles.add(ButtonData(":girl_in_love:", "girl_in_love.gif"))
            smiles.add(ButtonData(":girl_kiss:", "girl_kiss.gif"))
            smiles.add(ButtonData(":girl_pinkglassesf:", "girl_pinkglassesf.gif"))
            smiles.add(ButtonData(":girl_parting:", "girl_parting.gif"))
            smiles.add(ButtonData(":girl_prepare_fish:", "girl_prepare_fish.gif"))
            smiles.add(ButtonData(":good:", "good.gif"))
            smiles.add(ButtonData(":girl_spruce_up:", "girl_spruce_up.gif"))
            smiles.add(ButtonData(":girl_tear:", "girl_tear.gif"))
            smiles.add(ButtonData(":girl_tender:", "girl_tender.gif"))
            smiles.add(ButtonData(":girl_teddy:", "girl_teddy.gif"))
            smiles.add(ButtonData(":girl_to_babruysk:", "girl_to_babruysk.gif"))
            smiles.add(ButtonData(":girl_to_take_umbrage:", "girl_to_take_umbrage.gif"))
            smiles.add(ButtonData(":girl_triniti:", "girl_triniti.gif"))
            smiles.add(ButtonData(":girl_tongue:", "girl_tongue.gif"))
            smiles.add(ButtonData(":girl_wacko:", "girl_wacko.gif"))
            smiles.add(ButtonData(":girl_werewolf:", "girl_werewolf.gif"))
            smiles.add(ButtonData(":girl_witch:", "girl_witch.gif"))
            smiles.add(ButtonData(":grabli:", "grabli.gif"))
            smiles.add(ButtonData(":good_luck:", "good_luck.gif"))
            smiles.add(ButtonData(":guess:", "guess.gif"))
            smiles.add(ButtonData(":hang:", "hang.gif"))
            smiles.add(ButtonData(":heart:", "heart.gif"))
            smiles.add(ButtonData(":help:", "help.gif"))
            smiles.add(ButtonData(":helpsmilie:", "helpsmilie.gif"))
            smiles.add(ButtonData(":hemp:", "hemp.gif"))
            smiles.add(ButtonData(":heppy_dancing:", "heppy_dancing.gif"))
            smiles.add(ButtonData(":hysterics:", "hysterics.gif"))
            smiles.add(ButtonData(":indeec:", "indeec.gif"))
            smiles.add(ButtonData(":i-m_so_happy:", "i-m_so_happy.gif"))
            smiles.add(ButtonData(":kindness:", "kindness.gif"))
            smiles.add(ButtonData(":king:", "king.gif"))
            smiles.add(ButtonData(":laugh_wild:", "laugh_wild.gif"))
            smiles.add(ButtonData(":4PDA:", "love_4PDA.gif"))
            smiles.add(ButtonData(":nea:", "nea.gif"))
            smiles.add(ButtonData(":moil:", "moil.gif"))
            smiles.add(ButtonData(":no:", "no.gif"))
            smiles.add(ButtonData(":nono:", "nono.gif"))
            smiles.add(ButtonData(":offtopic:", "offtopic.gif"))
            smiles.add(ButtonData(":ok:", "ok.gif"))
            smiles.add(ButtonData(":papuas:", "papuas.gif"))
            smiles.add(ButtonData(":party:", "party.gif"))
            smiles.add(ButtonData(":pioneer_smoke:", "pioneer_smoke.gif"))
            smiles.add(ButtonData(":pipiska:", "pipiska.gif"))
            smiles.add(ButtonData(":protest:", "protest.gif"))
            smiles.add(ButtonData(":popcorm:", "popcorm.gif"))
            smiles.add(ButtonData(":rabbi:", "rabbi.gif"))
            smiles.add(ButtonData(":resent:", "resent.gif"))
            smiles.add(ButtonData(":roll:", "roll.gif"))
            smiles.add(ButtonData(":rtfm:", "rtfm.gif"))
            smiles.add(ButtonData(":russian_garmoshka:", "russian_garmoshka.gif"))
            smiles.add(ButtonData(":russian:", "russian.gif"))
            smiles.add(ButtonData(":russian_ru:", "russian_ru.gif"))
            smiles.add(ButtonData(":scratch_one-s_head:", "scratch_one-s_head.gif"))
            smiles.add(ButtonData(":scare:", "scare.gif"))
            smiles.add(ButtonData(":search:", "search.gif"))
            smiles.add(ButtonData(":secret:", "secret.gif"))
            smiles.add(ButtonData(":skull:", "skull.gif"))
            smiles.add(ButtonData(":shok:", "shok.gif"))
            smiles.add(ButtonData(":sorry:", "sorry.gif"))
            smiles.add(ButtonData(":smoke:", "smoke.gif"))
            smiles.add(ButtonData(":spiteful:", "spiteful.gif"))
            smiles.add(ButtonData(":stop_flood:", "stop_flood.gif"))
            smiles.add(ButtonData(":suicide:", "suicide.gif"))
            smiles.add(ButtonData(":stop_holywar:", "stop_holywar.gif"))
            smiles.add(ButtonData(":superman:", "superman.gif"))
            smiles.add(ButtonData(":superstition:", "superstition.gif"))
            smiles.add(ButtonData(":tablet_za:", "tablet_protiv.gif"))
            smiles.add(ButtonData(":tablet_protiv:", "tablet_za.gif"))
            smiles.add(ButtonData(":this:", "this.gif"))
            smiles.add(ButtonData(":tomato:", "tomato.gif"))
            smiles.add(ButtonData(":to_clue:", "to_clue.gif"))
            smiles.add(ButtonData(":tommy:", "tommy.gif"))
            smiles.add(ButtonData(":tongue3:", "tongue3.gif"))
            smiles.add(ButtonData(":umnik:", "umnik.gif"))
            smiles.add(ButtonData(":victory:", "victory.gif"))
            smiles.add(ButtonData(":vinsent:", "vinsent.gif"))
            smiles.add(ButtonData(":wallbash:", "wallbash.gif"))
            smiles.add(ButtonData(":whistle:", "whistle.gif"))
            smiles.add(ButtonData(":wink_kind:", "wink_kind.gif"))
            smiles.add(ButtonData(":yahoo:", "yahoo.gif"))
            smiles.add(ButtonData(":yes:", "yes.gif"))
            smiles.add(ButtonData(":&#91;", "confusion.gif"))
            smiles.add(ButtonData("&#93;-:{", "girl_devil.gif"))
            smiles.add(ButtonData(":*", "kiss.gif"))
            smiles.add(ButtonData("@}-'-,-", "give_rose.gif"))
            smiles.add(ButtonData(":'(", "cry.gif"))
            smiles.add(ButtonData(":-{", "mad.gif"))
            smiles.add(ButtonData("=^.^=", "kitten.gif"))
            smiles.add(ButtonData("(-=", "girl_hide.gif"))
            smiles.add(ButtonData("(-;", "girl_wink.gif"))
            smiles.add(ButtonData(")-:{", "girl_angry.gif"))
            smiles.add(ButtonData("*-:", "girl_chmok.gif"))
            smiles.add(ButtonData(")-:", "girl_sad.gif"))
            smiles.add(ButtonData(":girl_mad:", "girl_mad.gif"))
            smiles.add(ButtonData("(-:", "girl_smile.gif"))
            smiles.add(ButtonData(":acute:", "acute.gif"))
            smiles.add(ButtonData(":aggressive:", "aggressive.gif"))
            smiles.add(ButtonData(":air_kiss:", "air_kiss.gif"))
            smiles.add(ButtonData(":lol_girl:", "girl_haha.gif"))
            smiles.add(ButtonData(":ohmy:", "ohmy.gif"))
            smiles.add(ButtonData(":smile:", "smile.gif"))

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
