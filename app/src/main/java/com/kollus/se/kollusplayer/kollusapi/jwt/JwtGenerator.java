package com.kollus.se.kollusplayer.kollusapi.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class JwtGenerator {
    private JwtPayload jwtPayload;
    private String secret_key;

    public JwtGenerator() {
        this.jwtPayload = new JwtPayload();
    }

    public JwtGenerator secret_key(String secret_key){
        this.secret_key = secret_key;
        return this;
    }

    public JwtGenerator cuid(String cuid) {
        this.jwtPayload.setCuid(cuid);
        return this;
    }

    public JwtGenerator awtc(String awtc) {
        this.jwtPayload.setAwtc(awtc);
        return this;
    }

    public JwtGenerator expt(long expt) {
        this.jwtPayload.setExpt(expt);
        return this;
    }

    public JwtGenerator mc(Mc mc) {
        if (this.jwtPayload.getMc() == null) {
            this.jwtPayload.setMc(new ArrayList<Mc>());
        }
        this.jwtPayload.getMc().add(mc);
        return this;
    }

    public JwtGenerator video_watermarking_code_policy(VideoWatermarkingCodePolicy video_watermarking_code_policy) {
        this.jwtPayload.setVideo_watermarking_code_policy(video_watermarking_code_policy);
        return this;
    }

    public JwtGenerator pc_skin(PcSkin pc_skin) {
        this.jwtPayload.setPc_skin(pc_skin);
        return this;
    }

    public String generate() throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException {
        JwtUtil util = new JwtUtil();
        ObjectMapper mapper = new ObjectMapper();
        return util.createJwt(mapper.writeValueAsString(this.jwtPayload), this.secret_key);
    }

}
