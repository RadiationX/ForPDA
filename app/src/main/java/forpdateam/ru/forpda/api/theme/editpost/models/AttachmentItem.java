package forpdateam.ru.forpda.api.theme.editpost.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.regex.Pattern;

/**
 * Created by radiationx on 09.01.17.
 */

public class AttachmentItem implements Parcelable {
    private final static Pattern imageTypes = Pattern.compile("gif|jpg|jpeg|png", Pattern.CASE_INSENSITIVE);
    public final static int TYPE_FILE = 0;
    public final static int TYPE_IMAGE = 1;

    public final static int STATE_NOT_LOADED = 0;
    public final static int STATE_LOADING = 1;
    public final static int STATE_LOADED = 2;

    public final static int STATUS_REMOVED = 0;
    public final static int STATUS_NO_FILE = 1;
    public final static int STATUS_UPLOADED = 2;
    public final static int STATUS_READY = 3;
    public final static int STATUS_UNKNOWN = 4;

    private boolean isError = false;
    private boolean selected = false;

    private int id = -1;
    private int typeFile = TYPE_FILE;
    private int loadState = STATE_LOADING;
    private int status = STATUS_READY;

    private String name;
    private String format;
    private String weight;
    private String imageUrl;

    public AttachmentItem(String name) {
        this.name = name;
    }

    public AttachmentItem() {
    }

    public boolean isSelected() {
        return selected;
    }

    public void toggle() {
        selected = !selected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
        if (imageTypes.matcher(format).matches())
            this.typeFile = TYPE_IMAGE;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public int getTypeFile() {
        return typeFile;
    }

    public void setTypeFile(int typeFile) {
        this.typeFile = typeFile;
    }

    public int getLoadState() {
        return loadState;
    }

    public void setLoadState(int loadState) {
        this.loadState = loadState;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public boolean isError() {
        return isError;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    //PARCELABLE !!!!!!!!AAA!!!!!
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        Log.d("FORPDA_LOG", "writeToParcel");
        parcel.writeByte((byte) (isError ? 1 : 0));
        parcel.writeByte((byte) (selected ? 1 : 0));
        parcel.writeInt(id);
        parcel.writeInt(typeFile);
        parcel.writeInt(loadState);
        parcel.writeInt(status);
        writeStringToParcel(parcel, name);
        writeStringToParcel(parcel, format);
        writeStringToParcel(parcel, weight);
        writeStringToParcel(parcel, imageUrl);
    }

    public static final Parcelable.Creator<AttachmentItem> CREATOR = new Parcelable.Creator<AttachmentItem>() {
        public AttachmentItem createFromParcel(Parcel in) {
            Log.d("FORPDA_LOG", "createFromParcel");
            return new AttachmentItem(in);
        }

        public AttachmentItem[] newArray(int size) {
            return new AttachmentItem[size];
        }
    };

    private AttachmentItem(Parcel parcel) {
        isError = parcel.readByte() != 0;
        selected = parcel.readByte() != 0;
        id = parcel.readInt();
        typeFile = parcel.readInt();
        loadState = parcel.readInt();
        status = parcel.readInt();
        name = readStringFromParcel(parcel);
        format = readStringFromParcel(parcel);
        weight = readStringFromParcel(parcel);
        imageUrl = readStringFromParcel(parcel);
    }

    private void writeStringToParcel(Parcel parcel, String string) {
        parcel.writeByte((byte) (string != null ? 1 : 0));
        parcel.writeString(string);
    }

    private String readStringFromParcel(Parcel parcel) {
        return parcel.readByte() != 0 ? parcel.readString() : null;
    }
}
