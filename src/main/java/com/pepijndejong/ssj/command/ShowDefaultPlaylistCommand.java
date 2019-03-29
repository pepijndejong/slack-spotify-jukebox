package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.MessageCreator;
import com.pepijndejong.ssj.service.PlayerStateService;
import com.pepijndejong.ssj.service.SpotifyService;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.wrapper.spotify.model_objects.specification.Playlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowDefaultPlaylistCommand extends AbstractCommand {

    private final PlayerStateService playerStateService;
    private final MessageCreator messageCreator;
    private final SpotifyService spotifyService;

    @Autowired
    public ShowDefaultPlaylistCommand(final PlayerStateService playerStateService, final MessageCreator messageCreator, final SpotifyService spotifyService) {
        this.playerStateService = playerStateService;
        this.messageCreator = messageCreator;
        this.spotifyService = spotifyService;
    }

    @Override
    public String command() {
        return "show playlist$";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        final String playlistUri = playerStateService.getDefaultPlayList();
        final Playlist playlist = spotifyService.getPlaylistByUri(playlistUri);

        final String message = messageCreator.showDefaultPlaylist(playlist.getOwner().getId(), playlist.getName(), playlistUri);

        return slackMessage(message);
    }
}
