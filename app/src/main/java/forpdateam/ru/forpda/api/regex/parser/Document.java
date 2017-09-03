package forpdateam.ru.forpda.api.regex.parser;

/**
 * Created by radiationx on 13.08.17.
 */

public class Document extends Node {
    public final static String DOCTYPE_TAG = "!DOCTYPE";
    private String docType = "html";

    public Document() {
        super(NODE_DOCUMENT);
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }
}
