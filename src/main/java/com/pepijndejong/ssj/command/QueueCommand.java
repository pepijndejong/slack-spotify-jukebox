package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.MessageCreator;
import com.pepijndejong.ssj.service.PlayerStateService;
import com.pepijndejong.ssj.service.SpotifyService;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.wrapper.spotify.model_objects.specification.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueueCommand extends AbstractCommand {

    private final SpotifyService spotifyService;

    private final PlayerStateService playerStateService;

    private final MessageCreator messageCreator;

    @Autowired
    public QueueCommand(final SpotifyService spotifyService, final PlayerStateService playerStateService, final MessageCreator messageCreator) {
        this.spotifyService = spotifyService;
        this.playerStateService = playerStateService;
        this.messageCreator = messageCreator;
    }

    @Override
    public String command() {
        return "queue ";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        final Track track;
        if (commandData.contains("spotify:track:")) {
            final String trackId = commandData.replace("spotify:track:", "");

            track = spotifyService.searchTrackById(trackId);
        } else {
            //Search by name
            track = spotifyService.searchTrack(commandData);
        }

        if (track != null) {
            final String name = track.getName();
            final String artist = track.getArtists()[0].getName();

            final String fullTrackName = String.format("%s - %s", artist, name);

            final int queueSize = playerStateService.addToQueue(track.getUri(), username, fullTrackName);

            final String message = messageCreator.addToQueue(username, fullTrackName, queueSize);

            return slackMessageWithoutUnfurl(message);
        } else {
            final String message = messageCreator.trackNotFound(username);
            return slackMessage(message);
        }
    }
}
