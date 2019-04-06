package com.pepijndejong.ssj.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SpotifyHostPlayerService {

    private final SpotifyService spotifyService;

    private final PlayerStateService playerStateService;

    @Autowired
    public SpotifyHostPlayerService(final PlayerStateService playerStateService, final SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
        this.playerStateService = playerStateService;
    }

    public SpotifyPlayer getSpotifyPlayer() {
        final SpotifyPlayer spotifyPlayer = new SpotifyPlayer();
        spotifyPlayer.setAccessToken(spotifyService.getAccessToken());
        return spotifyPlayer;
    }

    private void startPlayList(final String playListUri) {
        playPlaylist(playListUri);
    }

    public void playDefaultPlayList() {
        boolean onShuffle = getSpotifyPlayer().getState().isOnShuffle();
        if (!onShuffle) {
            log.info("Player is not on shuffle, enabling it first...");
            getSpotifyPlayer().enableShuffle();
        }
        final String defaultPlayList = playerStateService.getDefaultPlayList();
        playPlaylist(defaultPlayList);
        playerStateService.setDefaultPlayListCurrentPlaylist();
    }

    public void setAndPlayDefaultPlayList(final String defaultPlayList) {
        playerStateService.setDefaultPlayList(defaultPlayList);
        startPlayList(defaultPlayList);
        playerStateService.setDefaultPlayListCurrentPlaylist();
    }

    public boolean makeSureDefaultPlaylistIsPlaying() {
        if (!playerStateService.isDefaultPlaylistCurrentPlaylist()) {
            playPlaylist(playerStateService.getDefaultPlayList());
            playerStateService.setDefaultPlayListCurrentPlaylist();
            return false;
        }
        return true;
    }

    public boolean playNextTrackFromQueue() {
        final String nextSongFromQueue = playerStateService.getNextFromQueue();
        if (nextSongFromQueue != null) {
            playTrack(nextSongFromQueue);
            return true;
        }
        return false;
    }

    public void playTrack(final String trackUri) {
        getSpotifyPlayer().playTrack(trackUri);
    }

    public void playNextTrack() {
        getSpotifyPlayer().playNextTrack();
    }

    private void playPlaylist(final String playList) {
        getSpotifyPlayer().playPlaylist(playList);
    }

}
