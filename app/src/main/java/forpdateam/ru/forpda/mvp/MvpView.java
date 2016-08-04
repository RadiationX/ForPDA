package forpdateam.ru.forpda.mvp;

public interface MvpView {
    void onError(int errorCode, String errorText);
    void setProgress(boolean visible);
}
