package kollus.test.media.kollusapi.jwt;

//import com.akamai.edgeauth.EdgeAuth;
//import com.akamai.edgeauth.EdgeAuthBuilder;
//import com.akamai.edgeauth.EdgeAuthException;

//import org.apache.commons.codec.binary.Base64;

public class McGenerator {
    private Mc mc;
    private String akamai_token;

    public McGenerator() {
        this.mc = new Mc();
    }

    public McGenerator mckey(String mckey) {
        this.mc.setMckey(mckey);
        return this;
    }

    public McGenerator mcpf(String mcpf) {
        this.mc.setMcpf(mcpf);
        return this;
    }

    public McGenerator title(String title) {
        this.mc.setTitle(title);
        return this;
    }

    public McGenerator intr(boolean intr) {
        this.mc.setIntr(intr);
        return this;
    }

    public McGenerator seek(boolean seek) {
        this.mc.setSeek(seek);
        return this;
    }

    public McGenerator seekable_end(int seekable_end) {
        this.mc.setSeekable_end(seekable_end);
        return this;
    }

    public McGenerator disable_playrate(boolean disable_playrate) {
        this.mc.setDisable_playrate(disable_playrate);
        return this;
    }

    public McGenerator disable_nscreen(boolean disable_nscreen) {
        this.mc.setDisable_nscreen(disable_nscreen);
        return this;
    }

    public McGenerator start_time(int start_time) {
        if (this.mc.getPlay_section() == null) {
            this.mc.setPlay_section(new PlaySection());
        }
        this.mc.getPlay_section().setStart_time(start_time);
        return this;
    }

    public McGenerator end_time(int end_time) {
        if (this.mc.getPlay_section() == null) {
            this.mc.setPlay_section(new PlaySection());
        }
        this.mc.getPlay_section().setEnd_time(end_time);
        return this;
    }

    public McGenerator thumbnail_enable(boolean enable) {
        if (this.mc.getThumbnail() == null) {
            this.mc.setThumbnail(new Thumbnail());
        }
        this.mc.getThumbnail().setEnable(enable);
        return this;
    }

    public McGenerator thumbnail_thread(boolean thread) {
        if (this.mc.getThumbnail() == null) {
            this.mc.setThumbnail(new Thumbnail());
        }
        this.mc.getThumbnail().setThread(thread);
        return this;
    }

    public McGenerator thumbnail_type(String type) {
        if (this.mc.getThumbnail() == null) {
            this.mc.setThumbnail(new Thumbnail());
        }
        this.mc.getThumbnail().setType(type);
        return this;
    }

    public McGenerator subtitle_policy_filter_name(String name) {
        if (this.mc.getSubtitle_policy() == null) {
            this.mc.setSubtitle_policy(new SubtitlePolicy());
        }
        if (this.mc.getSubtitle_policy().getFilter() == null) {
            this.mc.getSubtitle_policy().setFilter(new SubtitleFilter());
        }
        this.mc.getSubtitle_policy().getFilter().setName(name);
        return this;
    }

    public McGenerator subtitle_policy_filter_language_code(String language_code) {
        if (this.mc.getSubtitle_policy() == null) {
            this.mc.setSubtitle_policy(new SubtitlePolicy());
        }
        if (this.mc.getSubtitle_policy().getFilter() == null) {
            this.mc.getSubtitle_policy().setFilter(new SubtitleFilter());
        }
        this.mc.getSubtitle_policy().getFilter().setLanguage_code(language_code);
        return this;
    }

    public McGenerator drm_policy_kind(String kind) {
        if (this.mc.getDrm_policy() == null) {
            this.mc.setDrm_policy(new DrmPolicy());
        }
        this.mc.getDrm_policy().setKind(kind);
        return this;
    }

    public McGenerator drm_policy_streaming_type(String streaming_type) {
        if (this.mc.getDrm_policy() == null) {
            this.mc.setDrm_policy(new DrmPolicy());
        }
        this.mc.getDrm_policy().setStreaming_type(streaming_type);
        return this;
    }

    public McGenerator drm_policy_data_license_url(String license_url) {
        if (this.mc.getDrm_policy() == null) {
            this.mc.setDrm_policy(new DrmPolicy());
        }
        if (this.mc.getDrm_policy().getData() == null) {
            this.mc.getDrm_policy().setData(new DrmPolicyData());
        }
        this.mc.getDrm_policy().getData().setLicense_url(license_url);
        return this;
    }

    public McGenerator drm_policy_data_certificate_url(String certificate_url) {
        if (this.mc.getDrm_policy() == null) {
            this.mc.setDrm_policy(new DrmPolicy());
        }
        if (this.mc.getDrm_policy().getData() == null) {
            this.mc.getDrm_policy().setData(new DrmPolicyData());
        }
        this.mc.getDrm_policy().getData().setCertificate_url(certificate_url);
        return this;
    }

    public McGenerator drm_policy_data_custom_header_key(String custom_header_key) {
        if (this.mc.getDrm_policy() == null) {
            this.mc.setDrm_policy(new DrmPolicy());
        }
        if (this.mc.getDrm_policy().getData() == null) {
            this.mc.getDrm_policy().setData(new DrmPolicyData());
        }
        if (this.mc.getDrm_policy().getData().getCustom_header() == null) {
            this.mc.getDrm_policy().getData().setCustom_header(new CustomHeader());
        }
        this.mc.getDrm_policy().getData().getCustom_header().setKey(custom_header_key);
        return this;
    }

    public McGenerator drm_policy_data_custom_header_value(String custom_header_value) {
        if (this.mc.getDrm_policy() == null) {
            this.mc.setDrm_policy(new DrmPolicy());
        }
        if (this.mc.getDrm_policy().getData() == null) {
            this.mc.getDrm_policy().setData(new DrmPolicyData());
        }
        if (this.mc.getDrm_policy().getData().getCustom_header() == null) {
            this.mc.getDrm_policy().getData().setCustom_header(new CustomHeader());
        }
        this.mc.getDrm_policy().getData().getCustom_header().setValue(custom_header_value);
        return this;
    }


    public McGenerator live_url(String url) {
        if (this.mc.getLive() == null) {
            this.mc.setLive(new Live());
        }
        if(this.akamai_token != null && this.akamai_token.trim() != ""){
            String temp_url = url;
            if(temp_url.indexOf('?') > -1){
                temp_url += "&" + this.akamai_token;
            }
            else {
                temp_url += "?" + this.akamai_token;
            }
            this.mc.getLive().setUrl(temp_url);
        }
        else {
            this.mc.getLive().setUrl(url);
        }
        return this;
    }

    /***
     *
     * @param encryption_key
     * @param transit_key
     * @param salt
     * @param token_name
     * @param path
     * @param duration
     * @return
     */
//    public McGenerator live_url_token(String encryption_key, String transit_key, String salt, String token_name, String path, long duration) {
//        EdgeAuthBuilder edgeAuthBuilder = new EdgeAuthBuilder();
//        EdgeAuth edgeAuth = null;
//        try {
//            edgeAuth = edgeAuthBuilder
//                    .verbose(true)
//                    .tokenName(token_name)
//                    .startTime(EdgeAuth.NOW)
//                    .key(encryption_key)
//                    //.key(transit_key)
//                    //.salt(salt)
//                    .windowSeconds(duration)
//                    .escapeEarly(true)
//                    .build();
//
//
//            this.akamai_token = String.format("%s=%s", token_name, URLEncoder.encode(edgeAuth.generateACLToken(path),"utf-8"));
//        } catch (EdgeAuthException | UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        if (this.mc.getLive().getUrl() != null && this.mc.getLive().getUrl().trim() != ""){
//            String temp_url = this.mc.getLive().getUrl();
//            if(temp_url.indexOf('?') > -1){
//                temp_url += "&" + this.akamai_token;
//            }
//            else {
//                temp_url += "?" + this.akamai_token;
//            }
//            this.mc.getLive().setUrl(temp_url);
//        }
//            return this;
//    }

    public McGenerator live_poster_url(String poster_url) {
        if (this.mc.getLive() == null) {
            this.mc.setLive(new Live());
        }
        this.mc.getLive().setPoster_url(poster_url);
        return this;
    }

    public McGenerator live_auth_type(String auth_tytpe) {
        if (this.mc.getLive() == null) {
            this.mc.setLive(new Live());
        }
        this.mc.getLive().setAuth_type(auth_tytpe);
        return this;
    }

    public McGenerator live_use_ip_validation(boolean use_ip_validation) {
        if (this.mc.getLive() == null) {
            this.mc.setLive(new Live());
        }
        this.mc.getLive().setUse_ip_validation(use_ip_validation);
        return this;
    }

    public McGenerator live_use_kollus_token(boolean use_kollus_token) {
        if (this.mc.getLive() == null) {
            this.mc.setLive(new Live());
        }
        this.mc.getLive().setUse_kollus_token(use_kollus_token);
        return this;
    }

    public McGenerator live_cdn_type(String type) {
        if (this.mc.getLive() == null) {
            this.mc.setLive(new Live());
        }
        if (this.mc.getLive().getCdn() == null) {
            this.mc.getLive().setCdn(new Cdn());
        }
        this.mc.getLive().getCdn().setType(type);
        return this;
    }

    public McGenerator live_cdn_short(String shortpassword) {
        if (this.mc.getLive() == null) {
            this.mc.setLive(new Live());
        }
        if (this.mc.getLive().getCdn() == null) {
            this.mc.getLive().setCdn(new Cdn());
        }
        if (this.mc.getLive().getCdn().getPassword() == null) {
            this.mc.getLive().getCdn().setPassword(new CdnPassword());
        }
        this.mc.getLive().getCdn().getPassword().setShortpassword(shortpassword);
        return this;
    }

    public McGenerator live_cdn_long(String longpassword) {
        if (this.mc.getLive() == null) {
            this.mc.setLive(new Live());
        }
        if (this.mc.getLive().getCdn() == null) {
            this.mc.getLive().setCdn(new Cdn());
        }
        if (this.mc.getLive().getCdn().getPassword() == null) {
            this.mc.getLive().getCdn().setPassword(new CdnPassword());
        }
        this.mc.getLive().getCdn().getPassword().setLongpassword(longpassword);
        return this;
    }

    public Mc build() {
        return this.mc;
    }

}
