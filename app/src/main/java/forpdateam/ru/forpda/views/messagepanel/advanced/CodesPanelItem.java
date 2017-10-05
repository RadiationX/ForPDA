package forpdateam.ru.forpda.views.messagepanel.advanced;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.views.messagepanel.SimpleInstruction;
import forpdateam.ru.forpda.views.messagepanel.advanced.adapters.ItemDragCallback;
import forpdateam.ru.forpda.views.messagepanel.advanced.adapters.PanelItemAdapter;
import forpdateam.ru.forpda.views.messagepanel.colorpicker.ColorPicker;
import forpdateam.ru.forpda.views.messagepanel.inserthelper.InsertHelper;


/**
 * Created by radiationx on 08.01.17.
 */

@SuppressLint("ViewConstructor")
public class CodesPanelItem extends BasePanelItem {
    private static List<ButtonData> codes = null;
    private static Map<String, String> colors = null;
    private List<String> openedCodes = new ArrayList<>();
    private PanelItemAdapter.OnItemClickListener clickListener = item -> {
        switch (item.getText()) {
            case "URL": {
                urlInsert(item);
                break;
            }
            case "QUOTE": {
                quoteInsert(item);
                break;
            }
            case "CODE": {
                codeInsert(item);
                break;
            }
            case "SPOILER": {
                spoilerInsert(item);
                break;
            }
            case "LIST": {
                listInsert(item, false);

                break;
            }
            case "NUMLIST": {
                listInsert(item, true);
                break;
            }
            case "COLOR": {
                colorInsert(item);
                break;
            }
            case "BACKGROUND": {
                colorInsert(item);
                break;
            }
            case "SIZE": {
                sizeInsert(item);
                break;
            }
            default:
                simpleInsertText(item);
        }
    };

    public CodesPanelItem(Context context, MessagePanel panel) {
        super(context, panel, App.get().getString(R.string.codes_title));
        PanelItemAdapter adapter = new PanelItemAdapter(getCodes(), null, PanelItemAdapter.TYPE_DRAWABLE);
        adapter.setOnItemClickListener(clickListener);

        recyclerView.setColumnWidth(App.get().dpToPx(96));
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemDragCallback(adapter));
        touchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);

        if (App.get().getPreferences().getBoolean("message_panel.tooltip.user_sorting", true)) {
            SimpleInstruction instruction = new SimpleInstruction(getContext());
            instruction.setText(App.get().getString(R.string.code_panel_instruction));
            instruction.setOnCloseClick((v) -> {
                App.get().getPreferences().edit().putBoolean("message_panel.tooltip.user_sorting", false).apply();
            });
            addView(instruction);
        }
    }

    private void listInsert(ButtonData item, boolean num) {
        String selected = messagePanel.getSelectedText();
        List<String> listLines = new ArrayList<>();
        String tag = "LIST";
        if (selected.length() > 0) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.transform_string_to_list)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        String[] lines = TextUtils.split(selected, "\n");
                        Collections.addAll(listLines, lines);
                        messagePanel.deleteSelected();
                    })
                    .setNegativeButton(R.string.no, null)
                    .setOnDismissListener(dialog -> listInsert(tag, num, listLines))
                    .show();
        } else {
            listInsert(tag, num, listLines);
        }
    }

    private void listInsert(String tag, boolean num, List<String> listLines) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.report_layout, null);
        assert layout != null;
        final EditText messageField = (EditText) layout.findViewById(R.id.report_text_field);
        final TextInputLayout inputLayout = (TextInputLayout) layout.findViewById(R.id.report_input_layout);
        final int[] i = {listLines.size() + 1};
        inputLayout.setHint(String.format(App.get().getString(R.string.codes_list_item_Pos), i[0]));
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(layout)
                .setPositiveButton(R.string.add, null)
                .setNegativeButton(R.string.close, (dialog, which) -> {
                    StringBuilder body = new StringBuilder();
                    for (String line : listLines) {
                        body.append("[*]").append(line).append('\n');
                    }
                    List<Pair<String, String>> resultHeaders = new ArrayList<>();
                    if (num) {
                        resultHeaders.add(new Pair<>(null, "1"));
                    }
                    String[] bbcodes = createBbCode(tag, resultHeaders, body.toString());
                    messagePanel.insertText(bbcodes[0], bbcodes[1], false);
                })
                .show();
        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);
        positiveButton.setOnClickListener(v -> {
            i[0]++;
            listLines.add(messageField.getText().toString());
            messageField.setText("");
            inputLayout.setHint(String.format(App.get().getString(R.string.codes_list_item_Pos), i[0]));
        });
        messageField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                positiveButton.setEnabled(s.length() > 0);
            }
        });
    }

    private void colorInsert(ButtonData item) {
        new ColorPicker(getContext(), i -> {
            String color = Integer.toHexString(i).toUpperCase();
            if (color.length() > 6) {
                color = color.substring(2);
            }
            color = "#".concat(color);
            color = getHtmlColor(color);

            List<Pair<String, String>> resultHeaders = new ArrayList<>();
            resultHeaders.add(new Pair<>(null, color));
            String[] bbcodes = createBbCode(item.getText(), resultHeaders, null);
            messagePanel.insertText(bbcodes[0], bbcodes[1]);
        });
    }

    private void sizeInsert(ButtonData item) {
        CharSequence[] items = {
                "1 (8pt)",
                "2 (10pt)",
                "3 (12pt)",
                "4 (14pt)",
                "5 (18pt)",
                "6 (24pt)",
                "7 (36pt)"
        };
        for (int i = 0; i < items.length; i++) {
            items[i] = String.format(App.get().getString(R.string.codes_text_size_item_Size), items[i]);
        }
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.codes_text_size)
                .setItems(items, (dialog, which) -> {
                    List<Pair<String, String>> resultHeaders = new ArrayList<>();
                    resultHeaders.add(new Pair<>(null, Integer.toString(which + 1)));
                    String[] bbcodes = createBbCode(item.getText(), resultHeaders, null);
                    messagePanel.insertText(bbcodes[0], bbcodes[1]);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void urlInsert(ButtonData item) {
        String selected = messagePanel.getSelectedText();
        InsertHelper insertHelper = new InsertHelper(getContext());
        insertHelper.addHeader(App.get().getString(R.string.codes_link), null);
        if (selected.length() == 0)
            insertHelper.setBody(App.get().getString(R.string.codes_link_text), null);
        insertHelper.setInsertListener((resultHeaders, bodyResult) -> {
            String body = null;
            if (resultHeaders != null && !resultHeaders.isEmpty()) {
                body = resultHeaders.get(0).second;
            }
            String[] bbcodes = createBbCode(item.getText(), resultHeaders, body);
            messagePanel.insertText(bbcodes[0], bbcodes[1]);
        });
        insertHelper.show();
    }

    private void spoilerInsert(ButtonData item) {
        String selected = messagePanel.getSelectedText();
        InsertHelper insertHelper = new InsertHelper(getContext());
        insertHelper.addHeader(App.get().getString(R.string.codes_block_title), null);
        if (selected.length() == 0)
            insertHelper.setBody(App.get().getString(R.string.codes_spoiler_text), null);
        insertHelper.setInsertListener((resultHeaders, bodyResult) -> {
            String[] bbcodes = createBbCode(item.getText(), resultHeaders, bodyResult);
            messagePanel.insertText(bbcodes[0], bbcodes[1]);
        });
        insertHelper.show();
    }

    private void codeInsert(ButtonData item) {
        String selected = messagePanel.getSelectedText();
        InsertHelper insertHelper = new InsertHelper(getContext());
        insertHelper.addHeader(App.get().getString(R.string.codes_block_title), null);
        if (selected.length() == 0)
            insertHelper.setBody(App.get().getString(R.string.codes_code_text), null);
        insertHelper.setInsertListener((resultHeaders, bodyResult) -> {
            String[] bbcodes = createBbCode(item.getText(), resultHeaders, bodyResult);
            messagePanel.insertText(bbcodes[0], bbcodes[1]);
        });
        insertHelper.show();
    }

    private void quoteInsert(ButtonData item) {
        String selected = messagePanel.getSelectedText();
        InsertHelper insertHelper = new InsertHelper(getContext());
        insertHelper.addHeader(App.get().getString(R.string.codes_block_title), "name");
        /*insertHelper.addHeader("Дата", "date");
        insertHelper.addHeader("ID поста", "post");*/
        if (selected.length() == 0)
            insertHelper.setBody(App.get().getString(R.string.codes_quote_text), null);
        insertHelper.setInsertListener((resultHeaders, bodyResult) -> {
            String[] bbcodes = createBbCode(item.getText(), resultHeaders, bodyResult);
            messagePanel.insertText(bbcodes[0], bbcodes[1]);
        });
        insertHelper.show();
    }

    private String[] createBbCode(String tag, List<Pair<String, String>> headers, String body) {
        String start = null;
        String end = null;

        start = "[" + tag;
        if (headers != null) {
            for (Pair<String, String> header : headers) {
                if (header.first == null && header.second != null) {
                    start += "=" + header.second;
                    break;
                }
            }
            for (Pair<String, String> header : headers) {
                if (header.first == null || header.second == null) continue;
                start += " " + header.first + "=" + header.second;
            }
        }
        start += "]";

        if (body != null) {
            start += body;
        }
        end = "[/" + tag + "]";

        //Log.d("FORPDA_LOG", "CREATE BB CODE " + start + " : " + end);
        return new String[]{start, end};
    }

    private void simpleInsertText(ButtonData item) {
        String[] bbcodes = createBbCode(item.getText(), null, null);
        messagePanel.insertText(bbcodes[0], bbcodes[1]);
    }

    private void defaultInsertText(ButtonData item) {
        String tag = item.getText();
        String startText = null;
        String endText = null;
        int indexOf = openedCodes.indexOf(tag);


        startText = "[".concat(indexOf >= 0 ? "/" : "").concat(tag).concat("]");
        if (indexOf < 0)
            endText = "[/".concat(tag).concat("]");

        if (messagePanel.insertText(startText, endText)) return;

        if (indexOf >= 0) {
            openedCodes.remove(indexOf);
        } else {
            openedCodes.add(tag);
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        List<String> listCodes = new ArrayList<>();
        for (ButtonData item : codes) {
            listCodes.add(item.getText());
        }
        String sorted = TextUtils.join(",", listCodes);
        App.get().getPreferences().edit().putString("message_panel.bb_codes.sorted", sorted).apply();
        super.onDetachedFromWindow();
    }

    public static List<ButtonData> getCodes() {
        if (codes != null) return codes;
        codes = new ArrayList<>();
        ArrayList<ButtonData> tempCodes = new ArrayList<>();
        tempCodes.add(new ButtonData("B", R.drawable.ic_code_bold, App.get().getString(R.string.codes_name_bold)));
        tempCodes.add(new ButtonData("I", R.drawable.ic_code_italic, App.get().getString(R.string.codes_name_italic)));
        tempCodes.add(new ButtonData("U", R.drawable.ic_code_underline, App.get().getString(R.string.codes_name_underline)));
        tempCodes.add(new ButtonData("S", R.drawable.ic_code_s, App.get().getString(R.string.codes_name_s)));
        tempCodes.add(new ButtonData("URL", R.drawable.ic_code_url, App.get().getString(R.string.codes_name_link)));
        tempCodes.add(new ButtonData("SPOILER", R.drawable.ic_code_spoiler, App.get().getString(R.string.codes_name_spoiler)));
        tempCodes.add(new ButtonData("OFFTOP", R.drawable.ic_code_offtop, App.get().getString(R.string.codes_name_offtop)));
        tempCodes.add(new ButtonData("QUOTE", R.drawable.ic_code_quote, App.get().getString(R.string.codes_name_quote)));
        tempCodes.add(new ButtonData("CODE", R.drawable.ic_code_code, App.get().getString(R.string.codes_name_code)));
        tempCodes.add(new ButtonData("COLOR", R.drawable.ic_code_color, App.get().getString(R.string.codes_name_text_color)));
        tempCodes.add(new ButtonData("SIZE", R.drawable.ic_code_size, App.get().getString(R.string.codes_name_text_size)));

        tempCodes.add(new ButtonData("HIDE", R.drawable.ic_code_hide, App.get().getString(R.string.codes_name_hide)));
        tempCodes.add(new ButtonData("BACKGROUND", R.drawable.ic_code_background, App.get().getString(R.string.codes_name_bg_color)));
        tempCodes.add(new ButtonData("LIST", R.drawable.ic_code_list, App.get().getString(R.string.codes_name_list)));
        tempCodes.add(new ButtonData("NUMLIST", R.drawable.ic_code_numlist, App.get().getString(R.string.codes_name_numlist)));

        tempCodes.add(new ButtonData("LEFT", R.drawable.ic_code_left, App.get().getString(R.string.codes_name_left)));
        tempCodes.add(new ButtonData("CENTER", R.drawable.ic_code_center, App.get().getString(R.string.codes_name_center)));
        tempCodes.add(new ButtonData("RIGHT", R.drawable.ic_code_right, App.get().getString(R.string.codes_name_right)));
        tempCodes.add(new ButtonData("SUB", R.drawable.ic_code_sub, App.get().getString(R.string.codes_name_sub)));
        tempCodes.add(new ButtonData("SUP", R.drawable.ic_code_sup, App.get().getString(R.string.codes_name_sup)));
        tempCodes.add(new ButtonData("CUR", R.drawable.ic_code_cur, App.get().getString(R.string.codes_name_curator)));


        String sorted = App.get().getPreferences().getString("message_panel.bb_codes.sorted", null);
        if (sorted != null) {
            for (String code : TextUtils.split(sorted, ",")) {
                for (ButtonData item : tempCodes) {
                    if (item.getText().equals(code)) {
                        codes.add(item);
                        break;
                    }
                }
            }
        } else {
            codes.addAll(tempCodes);
        }
        tempCodes.clear();

        return codes;
    }


    private String getHtmlColor(String hexColor) {
        if (colors == null) {
            colors = new HashMap<>();
            colors.put("#000000", "black");
            colors.put("#FFFFFF", "white");
            colors.put("#82CEE8", "skyblue");
            colors.put("#426AE6", "royalblue");
            colors.put("#0000FF", "blue");
            colors.put("#07008C", "darkblue");
            colors.put("#FDA500", "orange");
            colors.put("#FF4300", "orangered");
            colors.put("#E1133A", "crimson");
            colors.put("#FF0000", "red");
            colors.put("#8C0000", "darkred");
            colors.put("#008000", "green");
            colors.put("#41A317", "limegreen");
            colors.put("#4E8975", "seagreen");
            colors.put("#F52887", "deeppink");
            colors.put("#FF6245", "tomato");
            colors.put("#F76541", "coral");
            colors.put("#800080", "purple");
            colors.put("#440087", "indigo");
            colors.put("#E3B382", "burlywood");
            colors.put("#EE9A4D", "sandybrown");
            colors.put("#C35817", "sienna");
            colors.put("#C85A17", "chocolate");
            colors.put("#037F81", "teal");
            colors.put("#C0C0C0", "silver");
            colors.put("#808080", "gray");
        }
        String res = colors.get(hexColor);
        if (res == null)
            return hexColor;
        return res;
    }
}
