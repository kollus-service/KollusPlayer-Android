package kollus.test.media.kollusapi.jwt;

public class VideoWatermarkingCodePolicy {
    String code_kind;
    int fot_size;
    String font_color;
    int show_time;
    int hide_time;
    int alpha;
    boolean enable_html5_player;
    public VideoWatermarkingCodePolicy(){};

    public String getCode_kind() {
        return code_kind;
    }

    public void setCode_kind(String code_kind) {
        this.code_kind = code_kind;
    }

    public int getFot_size() {
        return fot_size;
    }

    public void setFot_size(int fot_size) {
        this.fot_size = fot_size;
    }

    public String getFont_color() {
        return font_color;
    }

    public void setFont_color(String font_color) {
        this.font_color = font_color;
    }

    public int getShow_time() {
        return show_time;
    }

    public void setShow_time(int show_time) {
        this.show_time = show_time;
    }

    public int getHide_time() {
        return hide_time;
    }

    public void setHide_time(int hide_time) {
        this.hide_time = hide_time;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public boolean isEnable_html5_player() {
        return enable_html5_player;
    }

    public void setEnable_html5_player(boolean enable_html5_player) {
        this.enable_html5_player = enable_html5_player;
    }
}
