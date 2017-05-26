package forpdateam.ru.forpda.views.messagepanel.advanced;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.views.messagepanel.advanced.adapters.PanelItemAdapter;


/**
 * Created by radiationx on 08.01.17.
 */

@SuppressLint("ViewConstructor")
public class CodesPanelItem extends BasePanelItem {
    private static List<ButtonData> codes = null;
    private List<String> openedCodes = new ArrayList<>();

    public CodesPanelItem(Context context, MessagePanel panel) {
        super(context, panel, "Оформление");
        PanelItemAdapter adapter = new PanelItemAdapter(getCodes(), null, PanelItemAdapter.TYPE_DRAWABLE);

        adapter.setOnItemClickListener(item -> {
            int indexOf = openedCodes.indexOf(item.getText());
            String startText, endText = null, tag = item.getText();
            startText = "[".concat(indexOf >= 0 ? "/" : "").concat(tag).concat("]");
            if (indexOf < 0)
                endText = "[/".concat(tag).concat("]");

            if (messagePanel.insertText(startText, endText)) return;

            if (indexOf >= 0) {
                openedCodes.remove(indexOf);
            } else {
                openedCodes.add(item.getText());
            }
        });
        recyclerView.setColumnWidth(App.getInstance().dpToPx(96));
        recyclerView.setAdapter(adapter);
    }


    public static List<ButtonData> getCodes() {
        if (codes != null) return codes;
        codes = new ArrayList<>();
        String[] ji = new String[]{"B", "I", "U", "S", "SUB", "SUP", "LEFT", "CENTER",
                "RIGHT", "URL", "QUOTE", "OFFTOP", "CODE", "SPOILER", "HIDE", "LIST", "NUMLIST", "COLOR", "BACKGROUND",
                "SIZE", "CUR"};
        codes.add(new ButtonData("B", R.drawable.ic_code_bold, "Жирный"));
        codes.add(new ButtonData("I", R.drawable.ic_code_italic, "Курсив"));
        codes.add(new ButtonData("U", R.drawable.ic_code_underline, "Подчеркнуть"));
        codes.add(new ButtonData("S", R.drawable.ic_code_s, "Зачеркнуть"));
        codes.add(new ButtonData("SUB", R.drawable.ic_code_sub, "Снизу"));
        codes.add(new ButtonData("SUP", R.drawable.ic_code_sup, "Сверху"));
        codes.add(new ButtonData("LEFT", R.drawable.ic_code_left, "Влево"));
        codes.add(new ButtonData("CENTER", R.drawable.ic_code_center, "По центру"));
        codes.add(new ButtonData("RIGHT", R.drawable.ic_code_right, "Вправо"));
        codes.add(new ButtonData("OFFTOP", R.drawable.ic_code_offtop, "Оффтоп"));
        codes.add(new ButtonData("HIDE", R.drawable.ic_code_hide, "Скрытый"));
        codes.add(new ButtonData("CUR", R.drawable.ic_code_cur, "Куратор"));

        codes.add(new ButtonData("URL", R.drawable.ic_code_url, "Ссылка"));
        codes.add(new ButtonData("QUOTE", R.drawable.ic_code_quote, "Цитата"));
        codes.add(new ButtonData("CODE", R.drawable.ic_code_code, "Код"));
        codes.add(new ButtonData("SPOILER", R.drawable.ic_code_spoiler, "Спойлер"));
        codes.add(new ButtonData("LIST", R.drawable.ic_code_list, "Обычный список"));
        codes.add(new ButtonData("NUMLIST", R.drawable.ic_code_numlist, "Нумерованный список"));
        codes.add(new ButtonData("COLOR", R.drawable.ic_code_color, "Цвет текста"));
        codes.add(new ButtonData("BACKGROUND", R.drawable.ic_code_background, "Цвет фона"));
        codes.add(new ButtonData("SIZE", R.drawable.ic_code_size, "Размер текста"));
        return codes;
    }

}
