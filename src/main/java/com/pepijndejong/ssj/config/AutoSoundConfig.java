package com.pepijndejong.ssj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "jukebox")
@Component
public class AutoSoundConfig {

    private List<AutoSound> autosound = new ArrayList<>();

    @Data
    public static class AutoSound {
        private String regex;
        private String soundeffect;
        private String message;
    }

}
