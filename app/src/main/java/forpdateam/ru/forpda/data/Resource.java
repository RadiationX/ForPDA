package forpdateam.ru.forpda.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static forpdateam.ru.forpda.data.Status.ERROR;
import static forpdateam.ru.forpda.data.Status.LOADING;
import static forpdateam.ru.forpda.data.Status.MSG;
import static forpdateam.ru.forpda.data.Status.SUCCESS;

public class Resource<T> {
    @NonNull
    public final Status status;
    @Nullable
    public final T data;
    @Nullable public final String message;
    public final int count;
    public final boolean progress;
    private Resource(@NonNull Status status,
                     @Nullable T data,
                     @Nullable String message,
                     int count,
                     boolean progress) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.count = count;
        this.progress = progress;
    }


    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(SUCCESS, data, null, 0, false);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(ERROR, data, msg, 0, false);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(LOADING, data, null, 0, false);
    }
    public static <T> Resource<T> loading(@Nullable T data, @Nullable String message) {
        return new Resource<>(LOADING, data, message, 0, false);
    }

    public static <T> Resource<T> message(@Nullable String message, int count) {
        return new Resource<>(MSG, null, message, count, false);
    }

    public static <T> Resource<T> message(@Nullable String message) {
        return new Resource<>(MSG, null, message, 0, false);
    }

    public static <T> Resource<T> progress(boolean background) {
        return new Resource<>(LOADING, null, null, 0, background);
    }


    // helper connect child to parent


}
