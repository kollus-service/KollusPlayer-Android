package kollus.test.media.kollusapi.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CdnPassword {
    @JsonProperty("short")
    String shortpassword;
    @JsonProperty("long")
    String longpassword;

    public CdnPassword() {
    }

    public String getShortpassword() {
        return shortpassword;
    }

    public void setShortpassword(String shortpassword) {
        this.shortpassword = shortpassword;
    }

    public String getLongpassword() {
        return longpassword;
    }

    public void setLongpassword(String longpassword) {
        this.longpassword = longpassword;
    }
}
