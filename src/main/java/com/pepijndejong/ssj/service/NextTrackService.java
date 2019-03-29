package com.pepijndejong.ssj.service;

import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.wrapper.spotify.model_objects.specification.Playlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NextTrackService {

    private final SlackService slackService;

    private final SpotifyService spotifyService;

    private final PlayerStateService playerStateService;

    private final MessageCreator messageCreator;

    private final SpotifyHostPlayerService spotifyHostPlayerService;

    @Autowired
    public NextTrackService(final SlackService slackService, final SpotifyService spotifyService, final PlayerStateService playerStateService, final MessageCreator messageCreator, final SpotifyHostPlayerService spotifyHostPlayerService) {
        this.slackService = slackService;
        this.spotifyService = spotifyService;
        this.playerStateService = playerStateService;
        this.messageCreator = messageCreator;
        this.spotifyHostPlayerService = spotifyHostPlayerService;
    }

    public boolean selectAndPlayNextTrack() {
        final boolean trackFromQueuStarted = spotifyHostPlayerService.playNextTrackFromQueue();
        if (!trackFromQueuStarted) {
            final boolean wasDefaultPlaying = spotifyHostPlayerService.makeSureDefaultPlaylistIsPlaying();
            if (!wasDefaultPlaying) {
                sendDefaultPlaylistMessage();
                return true;
            }
            return false;
        }
        return true;
    }

    public void selectAndPlayNextForUser() {
        final boolean didStartATrack = selectAndPlayNextTrack();
        if (!didStartATrack) {
            spotifyHostPlayerService.playNextTrack();
        }
    }

    private void sendDefaultPlaylistMessage() {
        final String defaultPlayList = playerStateService.getDefaultPlayList();

        final Playlist playlist = spotifyService.getPlaylistByUri(defaultPlayList);
        if (playlist != null) {
            final String message = messageCreator.backToDefaultPlaylist(playlist.getOwner().getId(), playlist.getName());
            slackService.sendMessage(new SlackPreparedMessage.Builder()
                    .withMessage(message)
                    .build());
        }
    }

}
