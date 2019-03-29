package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.SpotifyHostPlayerService;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NameCommand extends AbstractCommand {

    private final SpotifyHostPlayerService spotifyHostPlayerService;

    @Autowired
    public NameCommand(final SpotifyHostPlayerService spotifyHostPlayerService) {
        this.spotifyHostPlayerService = spotifyHostPlayerService;
    }

    @Override
    public String command() {
        return "name$";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        final String fullTrackTitle = spotifyHostPlayerService.getSpotifyPlayer().getState().getFullTrackTitle();

        return slackMessage(fullTrackTitle);
    }
}
