package forpdateam.ru.forpda.views.messagepanel.advanced;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.views.messagepanel.advanced.adapters.PanelItemAdapter;

/**
 * Created by radiationx on 08.01.17.
 */

@SuppressLint("ViewConstructor")
public class SmilesPanelItem extends BasePanelItem {
    private static List<ButtonData> smiles = null;
    private static List<String> urlToAssets = null;

    public SmilesPanelItem(Context context, MessagePanel panel) {
        super(context, panel, "Смайлы");
        PanelItemAdapter adapter = new PanelItemAdapter(getSmiles(), getUrlToAssets(), PanelItemAdapter.TYPE_ASSET);
        adapter.setOnItemClickListener(item -> {
            messagePanel.insertText(" ".concat(item.getText()).concat(" "));
        });
        recyclerView.setAdapter(adapter);
    }

    public static List<ButtonData> getSmiles() {
        if (smiles != null) return smiles;
        smiles = new ArrayList<>();
        smiles.add(new ButtonData(":happy:", "happy.gif"));
        smiles.add(new ButtonData(";)", "wink.gif"));
        smiles.add(new ButtonData(":P", "tongue.gif"));
        smiles.add(new ButtonData(":-D", "biggrin.gif"));
        smiles.add(new ButtonData(":lol:", "laugh.gif"));
        smiles.add(new ButtonData(":rolleyes:", "rolleyes.gif"));
        smiles.add(new ButtonData(":)", "smile_good.gif"));
        smiles.add(new ButtonData(":beee:", "beee.gif"));
        smiles.add(new ButtonData(":rofl:", "rofl.gif"));
        smiles.add(new ButtonData(":sveta:", "sveta.gif"));
        smiles.add(new ButtonData(":thank_you:", "thank_you.gif"));
        smiles.add(new ButtonData("}-)", "devil.gif"));
        smiles.add(new ButtonData(":girl_cray:", "girl_cray.gif"));
        smiles.add(new ButtonData(":blush:", "blush.gif"));
        smiles.add(new ButtonData(":mellow:", "mellow.gif"));
        smiles.add(new ButtonData(":huh:", "huh.gif"));
        smiles.add(new ButtonData("B)", "cool.gif"));
        smiles.add(new ButtonData("-_-", "sleep.gif"));
        smiles.add(new ButtonData("&lt;_&lt;", "dry.gif"));
        smiles.add(new ButtonData(":wub:", "wub.gif"));
        smiles.add(new ButtonData(":angry:", "angry.gif"));
        smiles.add(new ButtonData(":(", "sad.gif"));
        smiles.add(new ButtonData(":unsure:", "unsure.gif"));
        smiles.add(new ButtonData(":wacko:", "wacko.gif"));
        smiles.add(new ButtonData(":blink:", "blink.gif"));
        smiles.add(new ButtonData(":ph34r:", "ph34r.gif"));
        smiles.add(new ButtonData(":banned:", "banned.gif"));
        smiles.add(new ButtonData(":antifeminism:", "antifeminism.gif"));
        smiles.add(new ButtonData(":beta:", "beta.gif"));
        smiles.add(new ButtonData(":boy_girl:", "boy_girl.gif"));
        smiles.add(new ButtonData(":butcher:", "butcher.gif"));
        smiles.add(new ButtonData(":bubble:", "bubble.gif"));
        smiles.add(new ButtonData(":censored:", "censored.gif"));
        smiles.add(new ButtonData(":clap:", "clap.gif"));
        smiles.add(new ButtonData(":close_tema:", "close_tema.gif"));
        smiles.add(new ButtonData(":clapping:", "clapping.gif"));
        smiles.add(new ButtonData(":coldly:", "coldly.gif"));
        smiles.add(new ButtonData(":comando:", "comando.gif"));
        smiles.add(new ButtonData(":dance:", "dance.gif"));
        smiles.add(new ButtonData(":daisy:", "daisy.gif"));
        smiles.add(new ButtonData(":dancer:", "dancer.gif"));
        smiles.add(new ButtonData(":derisive:", "derisive.gif"));
        smiles.add(new ButtonData(":dinamo:", "dinamo.gif"));
        smiles.add(new ButtonData(":dirol:", "dirol.gif"));
        smiles.add(new ButtonData(":diver:", "diver.gif"));
        smiles.add(new ButtonData(":drag:", "drag.gif"));
        smiles.add(new ButtonData(":download:", "download.gif"));
        smiles.add(new ButtonData(":drinks:", "drinks.gif"));
        smiles.add(new ButtonData(":first_move:", "first_move.gif"));
        smiles.add(new ButtonData(":feminist:", "feminist.gif"));
        smiles.add(new ButtonData(":flood:", "flood.gif"));
        smiles.add(new ButtonData(":fool:", "fool.gif"));
        smiles.add(new ButtonData(":friends:", "friends.gif"));
        smiles.add(new ButtonData(":foto:", "foto.gif"));
        smiles.add(new ButtonData(":girl_blum:", "girl_blum.gif"));
        smiles.add(new ButtonData(":girl_crazy:", "girl_crazy.gif"));
        smiles.add(new ButtonData(":girl_curtsey:", "girl_curtsey.gif"));
        smiles.add(new ButtonData(":girl_dance:", "girl_dance.gif"));
        smiles.add(new ButtonData(":girl_flirt:", "girl_flirt.gif"));
        smiles.add(new ButtonData(":girl_hospital:", "girl_hospital.gif"));
        smiles.add(new ButtonData(":girl_hysterics:", "girl_hysterics.gif"));
        smiles.add(new ButtonData(":girl_in_love:", "girl_in_love.gif"));
        smiles.add(new ButtonData(":girl_kiss:", "girl_kiss.gif"));
        smiles.add(new ButtonData(":girl_pinkglassesf:", "girl_pinkglassesf.gif"));
        smiles.add(new ButtonData(":girl_parting:", "girl_parting.gif"));
        smiles.add(new ButtonData(":girl_prepare_fish:", "girl_prepare_fish.gif"));
        smiles.add(new ButtonData(":good:", "good.gif"));
        smiles.add(new ButtonData(":girl_spruce_up:", "girl_spruce_up.gif"));
        smiles.add(new ButtonData(":girl_tear:", "girl_tear.gif"));
        smiles.add(new ButtonData(":girl_tender:", "girl_tender.gif"));
        smiles.add(new ButtonData(":girl_teddy:", "girl_teddy.gif"));
        smiles.add(new ButtonData(":girl_to_babruysk:", "girl_to_babruysk.gif"));
        smiles.add(new ButtonData(":girl_to_take_umbrage:", "girl_to_take_umbrage.gif"));
        smiles.add(new ButtonData(":girl_triniti:", "girl_triniti.gif"));
        smiles.add(new ButtonData(":girl_tongue:", "girl_tongue.gif"));
        smiles.add(new ButtonData(":girl_wacko:", "girl_wacko.gif"));
        smiles.add(new ButtonData(":girl_werewolf:", "girl_werewolf.gif"));
        smiles.add(new ButtonData(":girl_witch:", "girl_witch.gif"));
        smiles.add(new ButtonData(":grabli:", "grabli.gif"));
        smiles.add(new ButtonData(":good_luck:", "good_luck.gif"));
        smiles.add(new ButtonData(":guess:", "guess.gif"));
        smiles.add(new ButtonData(":hang:", "hang.gif"));
        smiles.add(new ButtonData(":heart:", "heart.gif"));
        smiles.add(new ButtonData(":help:", "help.gif"));
        smiles.add(new ButtonData(":helpsmilie:", "helpsmilie.gif"));
        smiles.add(new ButtonData(":hemp:", "hemp.gif"));
        smiles.add(new ButtonData(":heppy_dancing:", "heppy_dancing.gif"));
        smiles.add(new ButtonData(":hysterics:", "hysterics.gif"));
        smiles.add(new ButtonData(":indeec:", "indeec.gif"));
        smiles.add(new ButtonData(":i-m_so_happy:", "i-m_so_happy.gif"));
        smiles.add(new ButtonData(":kindness:", "kindness.gif"));
        smiles.add(new ButtonData(":king:", "king.gif"));
        smiles.add(new ButtonData(":laugh_wild:", "laugh_wild.gif"));
        smiles.add(new ButtonData(":4PDA:", "love_4PDA.gif"));
        smiles.add(new ButtonData(":nea:", "nea.gif"));
        smiles.add(new ButtonData(":moil:", "moil.gif"));
        smiles.add(new ButtonData(":no:", "no.gif"));
        smiles.add(new ButtonData(":nono:", "nono.gif"));
        smiles.add(new ButtonData(":offtopic:", "offtopic.gif"));
        smiles.add(new ButtonData(":ok:", "ok.gif"));
        smiles.add(new ButtonData(":papuas:", "papuas.gif"));
        smiles.add(new ButtonData(":party:", "party.gif"));
        smiles.add(new ButtonData(":pioneer_smoke:", "pioneer_smoke.gif"));
        smiles.add(new ButtonData(":pipiska:", "pipiska.gif"));
        smiles.add(new ButtonData(":protest:", "protest.gif"));
        smiles.add(new ButtonData(":popcorm:", "popcorm.gif"));
        smiles.add(new ButtonData(":rabbi:", "rabbi.gif"));
        smiles.add(new ButtonData(":resent:", "resent.gif"));
        smiles.add(new ButtonData(":roll:", "roll.gif"));
        smiles.add(new ButtonData(":rtfm:", "rtfm.gif"));
        smiles.add(new ButtonData(":russian_garmoshka:", "russian_garmoshka.gif"));
        smiles.add(new ButtonData(":russian:", "russian.gif"));
        smiles.add(new ButtonData(":russian_ru:", "russian_ru.gif"));
        smiles.add(new ButtonData(":scratch_one-s_head:", "scratch_one-s_head.gif"));
        smiles.add(new ButtonData(":scare:", "scare.gif"));
        smiles.add(new ButtonData(":search:", "search.gif"));
        smiles.add(new ButtonData(":secret:", "secret.gif"));
        smiles.add(new ButtonData(":skull:", "skull.gif"));
        smiles.add(new ButtonData(":shok:", "shok.gif"));
        smiles.add(new ButtonData(":sorry:", "sorry.gif"));
        smiles.add(new ButtonData(":smoke:", "smoke.gif"));
        smiles.add(new ButtonData(":spiteful:", "spiteful.gif"));
        smiles.add(new ButtonData(":stop_flood:", "stop_flood.gif"));
        smiles.add(new ButtonData(":suicide:", "suicide.gif"));
        smiles.add(new ButtonData(":stop_holywar:", "stop_holywar.gif"));
        smiles.add(new ButtonData(":superman:", "superman.gif"));
        smiles.add(new ButtonData(":superstition:", "superstition.gif"));
        smiles.add(new ButtonData(":tablet_za:", "tablet_protiv.gif"));
        smiles.add(new ButtonData(":tablet_protiv:", "tablet_za.gif"));
        smiles.add(new ButtonData(":this:", "this.gif"));
        smiles.add(new ButtonData(":tomato:", "tomato.gif"));
        smiles.add(new ButtonData(":to_clue:", "to_clue.gif"));
        smiles.add(new ButtonData(":tommy:", "tommy.gif"));
        smiles.add(new ButtonData(":tongue3:", "tongue3.gif"));
        smiles.add(new ButtonData(":umnik:", "umnik.gif"));
        smiles.add(new ButtonData(":victory:", "victory.gif"));
        smiles.add(new ButtonData(":vinsent:", "vinsent.gif"));
        smiles.add(new ButtonData(":wallbash:", "wallbash.gif"));
        smiles.add(new ButtonData(":whistle:", "whistle.gif"));
        smiles.add(new ButtonData(":wink_kind:", "wink_kind.gif"));
        smiles.add(new ButtonData(":yahoo:", "yahoo.gif"));
        smiles.add(new ButtonData(":yes:", "yes.gif"));
        smiles.add(new ButtonData(":&#91;", "confusion.gif"));
        smiles.add(new ButtonData("&#93;-:{", "girl_devil.gif"));
        smiles.add(new ButtonData(":*", "kiss.gif"));
        smiles.add(new ButtonData("@}-'-,-", "give_rose.gif"));
        smiles.add(new ButtonData(":'(", "cry.gif"));
        smiles.add(new ButtonData(":-{", "mad.gif"));
        smiles.add(new ButtonData("=^.^=", "kitten.gif"));
        smiles.add(new ButtonData("(-=", "girl_hide.gif"));
        smiles.add(new ButtonData("(-;", "girl_wink.gif"));
        smiles.add(new ButtonData(")-:{", "girl_angry.gif"));
        smiles.add(new ButtonData("*-:", "girl_chmok.gif"));
        smiles.add(new ButtonData(")-:", "girl_sad.gif"));
        smiles.add(new ButtonData(":girl_mad:", "girl_mad.gif"));
        smiles.add(new ButtonData("(-:", "girl_smile.gif"));
        smiles.add(new ButtonData(":acute:", "acute.gif"));
        smiles.add(new ButtonData(":aggressive:", "aggressive.gif"));
        smiles.add(new ButtonData(":air_kiss:", "air_kiss.gif"));
        smiles.add(new ButtonData(":lol_girl:", "girl_haha.gif"));
        smiles.add(new ButtonData(":ohmy:", "ohmy.gif"));
        smiles.add(new ButtonData(":smile:", "smile.gif"));
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
        return SmilesPanelItem.smiles;
    }

    public static List<String> getUrlToAssets() {
        if (urlToAssets != null) return urlToAssets;
        urlToAssets = new ArrayList<>();
        for (ButtonData data : smiles)
            urlToAssets.add("assets://smiles/".concat(data.getIcon()));

        return urlToAssets;
    }
}
