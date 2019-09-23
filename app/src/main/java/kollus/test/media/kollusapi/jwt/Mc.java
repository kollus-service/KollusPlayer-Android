package kollus.test.media.kollusapi.jwt;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Mc {
    private String mckey;
    private String mcpf;
    private String title;
    private boolean intr;
    private boolean seek = true;
    private int seekable_end = -1;
    private boolean disable_playrate = false;
    private boolean disable_nscreen = false;
    private PlaySection play_section;
    private Thumbnail thumbnail;
    private SubtitlePolicy subtitle_policy;
    private DrmPolicy drm_policy;
    private Live live;

    public Mc(){}

    public String getMckey() {
        return mckey;
    }

    public void setMckey(String mckey) {
        this.mckey = mckey;
    }

    public String getMcpf() {
        return mcpf;
    }

    public void setMcpf(String mcpf) {
        this.mcpf = mcpf;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isIntr() {
        return intr;
    }

    public void setIntr(boolean intr) {
        this.intr = intr;
    }

    public boolean isSeek() {
        return seek;
    }

    public void setSeek(boolean seek) {
        this.seek = seek;
    }

    public int getSeekable_end() {
        return seekable_end;
    }

    public void setSeekable_end(int seekable_end) {
        this.seekable_end = seekable_end;
    }

    public boolean isDisable_playrate() {
        return disable_playrate;
    }

    public void setDisable_playrate(boolean disable_playrate) {
        this.disable_playrate = disable_playrate;
    }

    public boolean isDisable_nscreen() {
        return disable_nscreen;
    }

    public void setDisable_nscreen(boolean disable_nscreen) {
        this.disable_nscreen = disable_nscreen;
    }

    public PlaySection getPlay_section() {
        return play_section;
    }

    public void setPlay_section(PlaySection play_section) {
        this.play_section = play_section;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public SubtitlePolicy getSubtitle_policy() {
        return subtitle_policy;
    }

    public void setSubtitle_policy(SubtitlePolicy subtitle_policy) {
        this.subtitle_policy = subtitle_policy;
    }

    public DrmPolicy getDrm_policy() {
        return drm_policy;
    }

    public void setDrm_policy(DrmPolicy drm_policy) {
        this.drm_policy = drm_policy;
    }

    public Live getLive() {
        return live;
    }

    public void setLive(Live live) {
        this.live = live;
    }
}
