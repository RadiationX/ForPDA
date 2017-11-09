package forpdateam.ru.forpda.api.devdb;

import android.util.Pair;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.devdb.models.Brand;
import forpdateam.ru.forpda.api.devdb.models.Brands;
import forpdateam.ru.forpda.api.devdb.models.Device;
import forpdateam.ru.forpda.api.devdb.models.DeviceSearch;

/**
 * Created by radiationx on 06.08.17.
 */

public class DevDb {
    public final static Pattern MAIN_PATTERN = Pattern.compile("<div class=\"breadcrumbs-back\"><ul class=\"breadcrumbs\">([\\s\\S]*?)<\\/ul><\\/div>[^<]*?<div[^>]*?>[\\s\\S]*?<\\/div>[^<]*?<\\/div>(?:[^<]*?<div class=\"rating r\\d\">[^<]*?<div class=\"num\">(\\d+)<\\/div>[^<]*?<div class=\"text\">([\\s\\S]*?)<\\/div>[^<]*?<\\/div>)?[\\s\\S]*?<h1 class=\"product-name\">(?:<a[^>]*?>[^<]*?<\\/a>)? ?([\\s\\S]*?)<\\/h1>(?:<div class=\"version\"><span[^>]*?>[^<]*?<\\/span><a[^>]*?>(\\d+)<\\/a><span[^>]*?>[^<]*?<\\/span>*<a[^>]*?>(\\d+)<\\/a>)?");
    private final static Pattern BREADCRUMB_PATTERN = Pattern.compile("<a href=\"[^\"]*?devdb\\/([^\"\\/]+?)(?:\\/([^\"]+?))?\">([^<]*?)<\\/a>");
    private final static Pattern SPECS_PATTERN = Pattern.compile("<dl[^>]*?>[^<]*?<dt>([^<]*?)<\\/dt>[^<]*<dd>(?:<span[^>]*?>)?([^<]*?)(?:<\\/span>[\\s\\S]*?)?<\\/dd>");
    private final static Pattern SEARCH_PATTERN = Pattern.compile("<li[^>]*?>[^<]*?<div[^>]*?>[^<]*?<a[^>]*?>[^<]*?<img src=\"([^\"]*?)\"[^>]*?>[^<]*?<\\/a>[\\s\\S]*?<div class=\"name\"[^>]*?>[^<]*?<a href=\"[^\"]*?devdb\\/([^\"]*?)\"[^>]*?>([\\s\\S]*?)<\\/a>");


    public int getRatingCode(int rating) {
        return Math.max(Math.round(rating / 2.0f), 1);
    }

    public Brands getBrands(String catId) throws Exception {
        Brands data = new Brands();
        NetworkResponse response = Api.getWebClient().get("https://4pda.ru/devdb/" + catId + "/all");
        Matcher matcher = Brands.LETTERS_PATTERN.matcher(response.getBody());
        while (matcher.find()) {
            String letter = matcher.group(1);
            ArrayList<Brands.Item> items = new ArrayList<>();
            Matcher itemsMatcher = Brands.ITEMS_IN_LETTER_PATTERN.matcher(matcher.group(2));
            while (itemsMatcher.find()) {
                Brands.Item item = new Brands.Item();
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

    public Brand getBrand(String catId, String brandId) throws Exception {
        Brand data = new Brand();
        NetworkResponse response = Api.getWebClient().get("https://4pda.ru/devdb/" + catId + "/" + brandId + "/all");

        Matcher matcher = Brand.DEVICES_PATTERN.matcher(response.getBody());
        while (matcher.find()) {
            Brand.DeviceItem item = new Brand.DeviceItem();
            item.setImageSrc(matcher.group(1));
            item.setId(matcher.group(2));
            item.setTitle(Utils.fromHtml(matcher.group(3)));

            Matcher specsMatcher = SPECS_PATTERN.matcher(matcher.group(4));
            if (specsMatcher.find()) {
                item.addSpec(specsMatcher.group(1), specsMatcher.group(2));
            }

            if (matcher.group(5) != null)
                item.setPrice(matcher.group(5));
            if (matcher.group(7) != null) {
                item.setRating(Integer.parseInt(matcher.group(7)));
            }

            data.addDevice(item);
        }
        matcher = MAIN_PATTERN.matcher(response.getBody());
        if (matcher.find()) {
            Matcher bcMatcher = BREADCRUMB_PATTERN.matcher(matcher.group(1));
            while (bcMatcher.find()) {
                if (bcMatcher.group(2) == null) {
                    data.setCatId(bcMatcher.group(1));
                    data.setCatTitle(bcMatcher.group(3));
                } else {
                    data.setId(bcMatcher.group(2));
                    data.setTitle(bcMatcher.group(3));
                }
            }
            data.setTitle(matcher.group(4));
            data.setActual(Integer.parseInt(matcher.group(5)));
            data.setAll(Integer.parseInt(matcher.group(6)));
        }
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
                    data.setBrandId(bcMatcher.group(2));
                    data.setBrandTitle(bcMatcher.group(3));
                }
            }

            if (matcher.group(2) != null) {
                data.setRating(Integer.parseInt(matcher.group(2)));
            }
            data.setTitle(matcher.group(4));
            data.setId(devId);
        }

        matcher = Device.COMMENTS_PATTERN.matcher(response.getBody());
        while (matcher.find()) {
            Device.Comment comment = new Device.Comment();
            comment.setId(Integer.parseInt(matcher.group(1)));
            comment.setRating(Integer.parseInt(matcher.group(3)));
            comment.setUserId(Integer.parseInt(matcher.group(4)));
            comment.setNick(Utils.fromHtml(matcher.group(5)));
            comment.setDate(matcher.group(6));
            String text = matcher.group(9);
            if (text == null) {
                text = matcher.group(7);
            }
            comment.setText(text.trim());
            comment.setLikes(Integer.parseInt(matcher.group(10)));
            comment.setDislikes(Integer.parseInt(matcher.group(11)));
            data.addComment(comment);
        }

        matcher = Device.REVIEWS_PATTERN.matcher(response.getBody());
        while (matcher.find()) {
            Device.PostItem item = new Device.PostItem();
            item.setId(Integer.parseInt(matcher.group(1)));
            item.setImage(matcher.group(2));
            item.setTitle(Utils.fromHtml(matcher.group(3)));
            item.setDate(matcher.group(4));
            if (matcher.group(5) != null) {
                item.setDesc(Utils.fromHtml(matcher.group(5)));
            }
            data.addNews(item);
        }

        matcher = Device.DISCUSSIONS_PATTERN.matcher(response.getBody());
        if (matcher.find()) {
            Matcher matcher1 = Device.DISCUSS_AND_FIRM_PATTERN.matcher(matcher.group(1));
            while (matcher1.find()) {
                Device.PostItem item = new Device.PostItem();
                item.setId(Integer.parseInt(matcher1.group(1)));
                item.setTitle(Utils.fromHtml(matcher1.group(2)));
                item.setDate(matcher1.group(3));
                if (matcher1.group(4) != null) {
                    item.setDesc(Utils.fromHtml(matcher1.group(4)));
                }
                data.addDiscussion(item);
            }
        }

        matcher = Device.FIRMwARES_PATTERN.matcher(response.getBody());
        if (matcher.find()) {
            Matcher matcher1 = Device.DISCUSS_AND_FIRM_PATTERN.matcher(matcher.group(1));
            while (matcher1.find()) {
                Device.PostItem item = new Device.PostItem();
                item.setId(Integer.parseInt(matcher1.group(1)));
                item.setTitle(Utils.fromHtml(matcher1.group(2)));
                item.setDate(matcher1.group(3));
                if (matcher1.group(4) != null) {
                    item.setDesc(Utils.fromHtml(matcher1.group(4)));
                }
                data.addFirmware(item);
            }
        }
        return data;
    }

    public DeviceSearch search(String query) throws Exception {
        DeviceSearch searchResult = new DeviceSearch();
        try {
            query = URLDecoder.decode(query, "windows-1251");
        } catch (Exception ignore) {
        }

        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/devdb/search?s=" + query);
        Matcher matcher = SEARCH_PATTERN.matcher(response.getBody());
        while (matcher.find()) {
            DeviceSearch.DeviceItem item = new DeviceSearch.DeviceItem();
            item.setImageSrc(matcher.group(1));
            item.setId(matcher.group(2));
            item.setTitle(Utils.fromHtml(matcher.group(3)));
            searchResult.addDevice(item);
        }
        searchResult.setAll(searchResult.getDevices().size());
        searchResult.setActual(searchResult.getAll());
        return searchResult;
    }
}
