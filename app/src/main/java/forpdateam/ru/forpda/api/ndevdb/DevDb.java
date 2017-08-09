package forpdateam.ru.forpda.api.ndevdb;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.ndevdb.models.Device;
import forpdateam.ru.forpda.api.ndevdb.models.Manufacturer;
import forpdateam.ru.forpda.api.ndevdb.models.Manufacturers;
import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.api.reputation.models.RepData;
import forpdateam.ru.forpda.api.reputation.models.RepItem;

/**
 * Created by radiationx on 06.08.17.
 */

public class DevDb {
    public final static Pattern MAIN_PATTERN = Pattern.compile("<div class=\"breadcrumbs-back\"><ul class=\"breadcrumbs\">([\\s\\S]*?)<\\/ul><\\/div>[^<]*?<div[^>]*?>[\\s\\S]*?<\\/div>[^<]*?<\\/div>(?:[^<]*?<div class=\"rating r\\d\">[^<]*?<div class=\"num\">(\\d+)<\\/div>[^<]*?<div class=\"text\">([\\s\\S]*?)<\\/div>[^<]*?<\\/div>)?[\\s\\S]*?<h1 class=\"product-name\">(?:<a[^>]*?>[^<]*?<\\/a>)? ?([\\s\\S]*?)<\\/h1>(?:<div class=\"version\"><span[^>]*?>[^<]*?<\\/span><a[^>]*?>(\\d+)<\\/a><span[^>]*?>[^<]*?<\\/span>*<a[^>]*?>(\\d+)<\\/a>)?");
    private final static Pattern BREADCRUMB_PATTERN = Pattern.compile("<a href=\"[^\"]*?devdb\\/([^\"\\/]+?)(?:\\/([^\"]+?))?\">([^<]*?)<\\/a>");
    private final static Pattern SPECS_PATTERN = Pattern.compile("<dl[^>]*?>[^<]*?<dt>([^<]*?)<\\/dt>[^<]*<dd>(?:<span[^>]*?>)?([^<]*?)(?:<\\/span>[\\s\\S]*?)?<\\/dd>");

    public Manufacturers getManufacturers(String catId) throws Exception {
        Manufacturers data = new Manufacturers();
        NetworkResponse response = Api.getWebClient().get("https://4pda.ru/devdb/" + catId + "/all");
        Matcher matcher = Manufacturers.LETTERS_PATTERN.matcher(response.getBody());
        while (matcher.find()) {
            String letter = matcher.group(1);
            ArrayList<Manufacturers.Item> items = new ArrayList<>();
            Matcher itemsMatcher = Manufacturers.ITEMS_IN_LETTER_PATTERN.matcher(matcher.group(2));
            while (itemsMatcher.find()) {
                Manufacturers.Item item = new Manufacturers.Item();
                item.setId(itemsMatcher.group(1));
                item.setTitle(Utils.fromHtml(itemsMatcher.group(2)));
                item.setCount(Integer.parseInt(itemsMatcher.group(3)));
                items.add(item);
            }
            data.putItems(letter, items);
        }
        matcher = MAIN_PATTERN.matcher(response.getBody());
        if (matcher.find()) {
            Matcher bcMatcher = BREADCRUMB_PATTERN.matcher(matcher.group(1));

            while (bcMatcher.find()) {
                if (bcMatcher.group(2) == null) {
                    data.setCatId(bcMatcher.group(1));
                    data.setCatTitle(bcMatcher.group(3));
                }
            }
            data.setActual(Integer.parseInt(matcher.group(5)));
            data.setAll(Integer.parseInt(matcher.group(6)));
        }
        return data;
    }

    public Manufacturer getManufacturer(String catId, String manId) throws Exception {
        Manufacturer data = new Manufacturer();
        NetworkResponse response = Api.getWebClient().get("https://4pda.ru/devdb/" + catId + "/" + manId + "/all");

        Log.d("MANAPI", "RESPONSE TUT " + response);
        Matcher matcher = Manufacturer.DEVICES_PATTERN.matcher(response.getBody());
        while (matcher.find()) {
            Manufacturer.DeviceItem item = new Manufacturer.DeviceItem();
            item.setImageSrc(matcher.group(1));
            item.setId(matcher.group(2));
            item.setTitle(Utils.fromHtml(matcher.group(3)));

            Matcher specsMatcher = SPECS_PATTERN.matcher(matcher.group(4));
            if (specsMatcher.find()) {
                item.addSpec(specsMatcher.group(1), specsMatcher.group(2));
            }

            if (matcher.group(5) != null)
                item.setPrice(matcher.group(5));
            if (matcher.group(6) != null) {
                item.setRating(Integer.parseInt(matcher.group(6)), matcher.group(7));
            }

            data.addDevice(item);
        }
        Log.d("MANAPI", "ADDED DEVICES " + data.getDevices().size());
        matcher = MAIN_PATTERN.matcher(response.getBody());
        Log.d("MANAPI", "MATCHER MAIN");
        if (matcher.find()) {
            Log.d("MANAPI", "FIND MAIN");
            Matcher bcMatcher = BREADCRUMB_PATTERN.matcher(matcher.group(1));
            while (bcMatcher.find()) {
                Log.d("MANAPI", "FIND BREADCRUMB");
                if (bcMatcher.group(2) == null) {
                    data.setCatId(bcMatcher.group(1));
                    data.setCatTitle(bcMatcher.group(3));
                } else {
                    data.setId(bcMatcher.group(2));
                    data.setTitle(bcMatcher.group(3));
                }
            }
            Log.d("MANAPI", "FILL MAIN");
            data.setTitle(matcher.group(4));
            data.setActual(Integer.parseInt(matcher.group(5)));
            data.setAll(Integer.parseInt(matcher.group(6)));
        }
        Log.d("MANAPI", "ADDED MAIN PART ");
        return data;
    }

    public Device getDevice(String devId) throws Exception {
        Device data = new Device();
        NetworkResponse response = Api.getWebClient().get("https://4pda.ru/devdb/" + devId);

        Matcher matcher = Device.PATTERN_1.matcher(response.getBody());
        if (matcher.find()) {
            data.setTitle(matcher.group(1));

            Matcher matcher1 = Device.IMAGES_PATTERN.matcher(matcher.group(2));
            while (matcher1.find()) {
                data.addImage(matcher1.group(2), matcher1.group(1));
            }

            matcher1 = Device.SPECS_TITLED_PATTERN.matcher(matcher.group(3));
            while (matcher1.find()) {
                String title = Utils.fromHtml(matcher1.group(1));
                Matcher specsMatcher = SPECS_PATTERN.matcher(matcher1.group(2));
                ArrayList<Pair<String, String>> specs = new ArrayList<>();
                while (specsMatcher.find()) {
                    specs.add(new Pair<>(specsMatcher.group(1), specsMatcher.group(2)));
                }
                data.addSpecs(title, specs);
            }
        }
        matcher = MAIN_PATTERN.matcher(response.getBody());
        if (matcher.find()) {
            Matcher bcMatcher = BREADCRUMB_PATTERN.matcher(matcher.group(1));
            while (bcMatcher.find()) {
                if (bcMatcher.group(2) == null) {
                    data.setCatId(bcMatcher.group(1));
                    data.setCatTitle(bcMatcher.group(3));
                } else {
                    data.setManId(bcMatcher.group(2));
                    data.setManTitle(bcMatcher.group(3));
                }
            }

            if (matcher.group(2) != null) {
                data.setRating(Integer.parseInt(matcher.group(2)), matcher.group(3));
            }
            data.setTitle(matcher.group(4));
            data.setId(devId);
        }
        return data;
    }
}
