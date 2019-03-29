package com.pepijndejong.ssj.service;

import com.pepijndejong.ssj.service.exception.SoundEffectNotFoundException;
import com.pepijndejong.ssj.config.AutoSoundConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AutoSoundService {

    private final AutoSoundConfig autoSoundConfig;

    private final SoundEffectService soundEffectService;

    private final SpotifyHostPlayerService spotifyHostPlayerService;

    public AutoSoundService(final AutoSoundConfig autoSoundConfig, final SoundEffectService soundEffectService, final SpotifyHostPlayerService spotifyHostPlayerService) {
        this.autoSoundConfig = autoSoundConfig;
        this.soundEffectService = soundEffectService;
        this.spotifyHostPlayerService = spotifyHostPlayerService;
    }

    public AutoSoundConfig.AutoSound searchForAutoSounds(final String messageContent) {
        for (AutoSoundConfig.AutoSound autosound : autoSoundConfig.getAutosound()) {
            final Pattern pattern = Pattern.compile(autosound.getRegex(), Pattern.CASE_INSENSITIVE);
            final Matcher matcher = pattern.matcher(messageContent);
            if (matcher.find()) {
                log.info("Autosound found for: {} soundeffect {}.", messageContent, autosound.getSoundeffect());
                return autosound;
            }
        }
        return null;
    }

    public void triggerAutoSound(final String soundEffectName) {
        try {
            spotifyHostPlayerService.getSpotifyPlayer().adjustVolume(60);
            soundEffectService.playSoundEffect(soundEffectName);
        } catch (SoundEffectNotFoundException e) {
            log.error("Sound effect not found: {}", soundEffectName, e);
        } finally {
            spotifyHostPlayerService.getSpotifyPlayer().adjustVolume(100);
        }
    }

}
