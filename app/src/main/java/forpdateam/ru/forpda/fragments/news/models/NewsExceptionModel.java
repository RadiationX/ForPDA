package forpdateam.ru.forpda.fragments.news.models;

import android.support.annotation.NonNull;

/**
 * Created by isanechek on 24.01.17.
 */

public class NewsExceptionModel extends RuntimeException {

    private String message;

    public NewsExceptionModel(@NonNull String message) {
        super();
        this.message = message;
    }

    @NonNull
    public String getValidateError() {
        return message;
    }
}
