package forpdateam.ru.forpda.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import okhttp3.Cookie;

/**
 * Created by radiationx on 28.07.16.
 */
public class SerializableCookie implements Externalizable {

    private static final int NAME = 0x01;
    private static final int VALUE = 0x02;
    private static final int EXPIRY_DATE = 0x04;
    private static final int DOMAIN = 0x08;
    private static final int PATH = 0x10;

    private transient int nullMask = 0;
    private transient Cookie cookie;

    public SerializableCookie() {
        super();
    }

    public SerializableCookie(final Cookie cookie) {
        super();

        this.cookie = cookie;
    }

    public String getName() {
        return cookie.name();
    }

    public String getValue() {
        return cookie.value();
    }


    public long getExpiryDate() {
        return cookie.expiresAt();
    }

    public String getDomain() {
        return cookie.domain();
    }

    public String getPath() {
        return cookie.path();
    }

    public boolean isSecure() {
        return cookie.secure();
    }

    public void writeExternal(final ObjectOutput out) throws IOException {
        nullMask |= (getName() == null) ? NAME : 0;
        nullMask |= (getValue() == null) ? VALUE : 0;
        nullMask |= (getDomain() == null) ? DOMAIN : 0;
        nullMask |= (getPath() == null) ? PATH : 0;

        out.writeInt(nullMask);

        if ((nullMask & NAME) == 0) {
            out.writeUTF(getName());
        }

        if ((nullMask & VALUE) == 0) {
            out.writeUTF(getValue());
        }

        if ((nullMask & EXPIRY_DATE) == 0) {
            out.writeLong(getExpiryDate());
        }

        if ((nullMask & DOMAIN) == 0) {
            out.writeUTF(getDomain());
        }

        if ((nullMask & PATH) == 0) {
            out.writeUTF(getPath());
        }

        out.writeBoolean(isSecure());
    }


    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        nullMask = in.readInt();

        String name = null;
        String value = null;
        long expiryDate = 0;
        String domain = null;
        String path = null;
        boolean isSecure = false;

        if ((nullMask & NAME) == 0) {
            name = in.readUTF();
        }

        if ((nullMask & VALUE) == 0) {
            value = in.readUTF();
        }

        if ((nullMask & EXPIRY_DATE) == 0) {
            expiryDate = in.readLong();
        }

        if ((nullMask & DOMAIN) == 0) {
            domain = in.readUTF();
        }

        if ((nullMask & PATH) == 0) {
            path = in.readUTF();
        }

        isSecure = in.readBoolean();

        Cookie.Builder bc = new Cookie.Builder()
                .name(name)
                .value(value)
                .domain(domain)
                .expiresAt(expiryDate)
                .path(path)
                .secure();

        if (isSecure)
            bc.secure();

        this.cookie = bc.build();
    }

    @Override
    public String toString() {
        if (cookie == null) {
            return "null";
        } else {
            return cookie.toString();
        }
    }


}
