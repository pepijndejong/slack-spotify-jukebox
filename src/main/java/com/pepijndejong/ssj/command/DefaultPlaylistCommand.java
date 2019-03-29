package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.MessageCreator;
import com.pepijndejong.ssj.service.PlayerStateService;
import com.pepijndejong.ssj.service.SpotifyService;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.wrapper.spotify.model_objects.specification.Playlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultPlaylistCommand extends AbstractCommand {

    private final SpotifyService spotifyService;

    private final PlayerStateService playerStateService;

    private final MessageCreator messageCreator;

    @Autowired
    public DefaultPlaylistCommand(final SpotifyService spotifyService, final PlayerStateService playerStateService, final MessageCreator messageCreator) {
        this.spotifyService = spotifyService;
        this.playerStateService = playerStateService;
        this.messageCreator = messageCreator;
    }

    @Override
    public String command() {
        return "playlist ";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        final Playlist playlist = spotifyService.getPlaylistByUri(commandData);
        final String message;
        if (playlist != null) {
            playerStateService.setDefaultPlayList(commandData);

            message = messageCreator.setDefaultPlaylist(playlist.getOwner().getId(), playlist.getName());
        } else {
            message = messageCreator.playlistNotFound(username);
        }
        return slackMessage(message);
    }
}
