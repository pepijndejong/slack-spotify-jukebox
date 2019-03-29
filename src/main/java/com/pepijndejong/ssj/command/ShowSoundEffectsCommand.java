package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.MessageCreator;
import com.pepijndejong.ssj.service.SoundEffectService;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShowSoundEffectsCommand extends AbstractCommand {

    private final SoundEffectService soundEffectService;

    private final MessageCreator messageCreator;

    @Autowired
    public ShowSoundEffectsCommand(final SoundEffectService soundEffectService, final MessageCreator messageCreator) {
        this.soundEffectService = soundEffectService;
        this.messageCreator = messageCreator;
    }

    @Override
    public String command() {
        return "show sound$";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        final List<String> sounds = soundEffectService.getSounds();

        return slackMessage(messageCreator.showSoundEffects(sounds));
    }
}
