package forpdateam.ru.forpda.test.regexparser;

/**
 * Created by radiationx on 26.08.16.
 */
public class Document {
    private Element root;

    public void setRoot(Element element) {
        root = element;
    }

    public void add(Element children) {
        if (children.getLevel() == 0) {
            setRoot(children);
            return;
        }
        findToAdd(root, children);
    }

    private void findToAdd(Element root, Element children) {
        if (children.getLevel() - 1 == root.getLevel()) {
            root.add(children);
        } else {
            findToAdd(root.getLast(), children);
        }
    }

    public Element getRoot() {
        return root;
    }

    public String getHtml(){
        return root.getHtml();
    }

    public String getAllText(){
        return root.getAllText();
    }
}