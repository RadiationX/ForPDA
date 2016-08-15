package forpdateam.ru.forpda.client;

/**
 * Created by RadiationX on 14.08.2016.
 */
public class OkHttpResponseException extends Exception {
    private int code;
    private String name;
    private String url;

    public OkHttpResponseException(int code, String name, String url) {
        this.code = code;
        this.name = name;
        this.url = url;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return "Response {code=" + code + ", message=" + name + "}";
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
