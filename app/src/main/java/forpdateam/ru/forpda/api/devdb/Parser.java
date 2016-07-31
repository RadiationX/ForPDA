package forpdateam.ru.forpda.api.devdb;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.devdb.models.DevCatalog;
import forpdateam.ru.forpda.client.Client;

/**
 * Created by isanechek on 30.07.16.
 */

public class Parser {

    public static ArrayList<DevCatalog> getStandardDevicesTypes() {
        ArrayList<DevCatalog> res = new ArrayList<>();
        res.add(new DevCatalog("http://4pda.ru/devdb/phones/", "Телефоны").setType(DevCatalog.DEVICE_TYPE));
        res.add(new DevCatalog("http://4pda.ru/devdb/pad/", "Планшеты").setType(DevCatalog.DEVICE_TYPE));
        res.add(new DevCatalog("http://4pda.ru/devdb/ebook/", "Электронные книги").setType(DevCatalog.DEVICE_TYPE));
        res.add(new DevCatalog("http://4pda.ru/devdb/smartwatch/", "Смарт часы").setType(DevCatalog.DEVICE_TYPE));
        return res;
    }

    public static ArrayList<DevCatalog> parseBrands(Client client, String devicesTypeUrl) {
        String pageBody;
        ArrayList<DevCatalog> res = new ArrayList<>();
        try {
            pageBody = client.get(devicesTypeUrl + "all");
            Document doc = Jsoup.parse(pageBody);

            Elements con = doc.getElementsByClass("word-list");
            Elements con1 = con.select("li");
            for (Element element1 : con1) {
                String brandsLink = element1.getElementsByTag("a").attr("href");
                String brandsName = element1.text();
                DevCatalog f = new DevCatalog(brandsLink, brandsName);
                f.setType(DevCatalog.DEVICE_BRAND);
                res.add(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }
}
