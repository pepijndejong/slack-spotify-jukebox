package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.*;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlayCommand extends AbstractCommand {

    private final SpotifyService spotifyService;

    private final SpotifyHostPlayerService spotifyHostPlayerService;

    private final UserActionRecorder userActionRecorder;

    private final MessageCreator messageCreator;

    private final HostPlayerStatusPoller hostPlayerStatusPoller;

    @Autowired
    public PlayCommand(final SpotifyService spotifyService, final SpotifyHostPlayerService spotifyHostPlayerService, final UserActionRecorder userActionRecorder, final MessageCreator messageCreator, final HostPlayerStatusPoller hostPlayerStatusPoller) {
        this.spotifyService = spotifyService;
        this.spotifyHostPlayerService = spotifyHostPlayerService;
        this.userActionRecorder = userActionRecorder;
        this.messageCreator = messageCreator;
        this.hostPlayerStatusPoller = hostPlayerStatusPoller;
    }

    @Override
    public String command() {
        return "play ";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        if (!userActionRecorder.mayDoActionAgain(username)) {
            final String message = messageCreator.tooManyRequest(username);

            return slackMessage(message);
        }

        final boolean isPlaylist = commandData.contains("playlist");
        if (isPlaylist) {
            final Playlist playlist = spotifyService.getPlaylistByUri(commandData);
            if (playlist != null) {
                spotifyHostPlayerService.setAndPlayDefaultPlayList(commandData);
                userActionRecorder.recordActivity(username);
                final String message = messageCreator.playPlaylist(username, playlist.getOwner().getId(), playlist.getName());
                hostPlayerStatusPoller.reset();
                return slackMessage(message);
            } else {
                final String message = messageCreator.playlistNotFound(username);
                return slackMessage(message);
            }
        } else {
            final String trackUri;
            final Track track;
            if (commandData.contains("track:")) {
                final String trackId = commandData.replace("spotify:track:", "");
                track = spotifyService.searchTrackById(trackId);
            } else {
                //Track by name
                track = spotifyService.searchTrack(commandData);
            }
            if (track != null) {
                trackUri = track.getUri();
                spotifyHostPlayerService.playTrack(trackUri);
                userActionRecorder.recordActivity(username);

                final String message = messageCreator.playTrack(username);
                hostPlayerStatusPoller.reset();
                return slackMessage(message);
            } else {
                final String message = messageCreator.trackNotFound(username);
                return slackMessage(message);
            }
        }
    }
}
