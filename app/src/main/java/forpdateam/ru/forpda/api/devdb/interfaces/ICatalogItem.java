package forpdateam.ru.forpda.api.devdb.interfaces;

public interface ICatalogItem {
    CharSequence getId();

    CharSequence getTitle();

    CharSequence getSubTitle();

    ICatalogItem getParent();

    public void setParent(ICatalogItem catalogItem);

    ICatalogItem clone();


}
