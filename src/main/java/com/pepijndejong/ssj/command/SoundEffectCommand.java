package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.MessageCreator;
import com.pepijndejong.ssj.service.SoundEffectService;
import com.pepijndejong.ssj.service.SpotifyHostPlayerService;
import com.pepijndejong.ssj.service.exception.SoundEffectNotFoundException;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SoundEffectCommand extends AbstractCommand {

    private final SpotifyHostPlayerService spotifyHostPlayerService;

    private final SoundEffectService soundEffectService;

    private final MessageCreator messageCreator;

    @Autowired
    public SoundEffectCommand(final SpotifyHostPlayerService spotifyHostPlayerService, final SoundEffectService soundEffectService, final MessageCreator messageCreator) {
        this.spotifyHostPlayerService = spotifyHostPlayerService;
        this.soundEffectService = soundEffectService;
        this.messageCreator = messageCreator;
    }

    @Override
    public String command() {
        return "sound ";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        try {
            spotifyHostPlayerService.getSpotifyPlayer().adjustVolume(80);
            soundEffectService.playSoundEffect(commandData);
        } catch (SoundEffectNotFoundException e) {
            return slackMessage(messageCreator.soundEffectNotFound(username, commandData));
        } finally {
            spotifyHostPlayerService.getSpotifyPlayer().adjustVolume(100);
        }
        return null;
    }
}
