package com.pepijndejong.ssj.service;

import com.google.gson.Gson;
import com.pepijndejong.ssj.domain.PlayerPlayingState;
import com.pepijndejong.ssj.domain.generated.PlayerStateResponse;
import com.pepijndejong.ssj.service.exception.SpotifyApiCallFailedException;
import com.pepijndejong.ssj.service.exception.SpotifyPlayerNotRunningException;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

@Slf4j
public class SpotifyPlayer {

    private static final String API_URL = "https://api.spotify.com";

    @Setter
    private String accessToken;

    private OkHttpClient client = new OkHttpClient();

    public void playTrack(final String trackUri) {
        final String payload = String.format("{\"uris\": [\"%s\"]}", trackUri);

        final Request request = createAuthorizedRequest("/v1/me/player/play")
                .put(RequestBody.create(MediaType.parse("application/json"),
                        payload))
                .build();

        doRequest(request);
    }

    public void playPlaylist(final String playlistUri) {
        final String payload = String.format("{\"context_uri\": \"%s\"}", playlistUri);

        final Request request = createAuthorizedRequest("/v1/me/player/play")
                .put(RequestBody.create(MediaType.parse("application/json"),
                        payload))
                .build();

        doRequest(request);
    }

    public void playNextTrack() {
        final Request request = createAuthorizedRequest("/v1/me/player/next")
                .post(RequestBody.create(MediaType.parse("application/json"),
                        ""))
                .build();

        doRequest(request);
    }

    public PlayerPlayingState getState() {
        final Request request = createAuthorizedRequest("/v1/me/player")
                .get()
                .build();

        final SpotifyResponse spotifyResponse = doRequest(request);

        if (spotifyResponse.getCode() == 204) {
            throw new SpotifyPlayerNotRunningException();
        }

        final PlayerStateResponse playerStateResponse = new Gson().fromJson(spotifyResponse.getBody(), PlayerStateResponse.class);
        return new PlayerPlayingState(playerStateResponse);
    }

    public void enableShuffle() {
        final Request request = createAuthorizedRequest("/v1/me/player/shuffle?state=true")
                .put(RequestBody.create(MediaType.parse("application/json"),
                        ""))
                .build();

        try {
            doRequest(request);
        } catch (SpotifyApiCallFailedException e) {
            log.warn("Failed to enable shuffle on the Spotify player.");
        }
    }

    public void adjustVolume(final int level) {
        final String payload = String.format("volume_percent=%s", level);

        final Request request = createAuthorizedRequest("/v1/me/player/volume?" + payload)
                .put(RequestBody.create(MediaType.parse("application/json"),
                        ""))
                .build();

        try {
            doRequest(request);
        } catch (SpotifyApiCallFailedException e) {
            log.warn("Failed to adjust the volume of the Spotify player.", e);
        }
    }

    private SpotifyResponse doRequest(final Request request) {
        try {
            final Response response = client.newCall(request).execute();
            final ResponseBody body = response.body();
            try {
                final String bodyPayload = body.string();
                if (response.code() >= 500) {
                    throw new SpotifyApiCallFailedException(
                            String.format("API call returned %s code with body %s",
                                    response.code(), bodyPayload));
                }
                return new SpotifyResponse(bodyPayload, response.code());
            } finally {
                body.close();
            }
        } catch (IOException e) {
            throw new SpotifyApiCallFailedException(e);
        }
    }

    private Request.Builder createAuthorizedRequest(final String apiPath) {
        if (accessToken == null) {
            throw new SpotifyApiCallFailedException("No access token found.");
        }
        return new Request.Builder()
                .header("Authorization", String.format("Bearer %s", accessToken))
                .url(API_URL + apiPath);
    }

    @Data
    private static class SpotifyResponse {
        private final String body;
        private final int code;
    }

}
