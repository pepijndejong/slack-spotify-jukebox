package com.pepijndejong.ssj.domain;

import com.pepijndejong.ssj.domain.generated.PlayerStateResponse;

public class PlayerPlayingState {

    private final PlayerStateResponse playerStateResponse;

    public PlayerPlayingState(final PlayerStateResponse playerStateResponse) {
        this.playerStateResponse = playerStateResponse;
    }

    public boolean isPlaying() {
        return playerStateResponse.getIsPlaying();
    }

    public boolean isOnShuffle() {
        return playerStateResponse.getShuffleState();
    }

    public String getFullTrackTitle() {
        final String name = playerStateResponse.getItem().getName();
        final String artist = playerStateResponse.getItem().getArtists().get(0).getName();
        final String spotifyUri = playerStateResponse.getItem().getUri();

        return String.format("%s - %s %s", artist, name, spotifyUri);
    }

    public double getMillisecondsLeftForCurrentTrack() {
        final int durationInMillis = playerStateResponse.getItem().getDurationMs();
        final int progressInMillis = playerStateResponse.getProgressMs();

        return durationInMillis - progressInMillis;
    }

    public String getCurrentTrackId() {
        return playerStateResponse.getItem().getUri();
    }

}
