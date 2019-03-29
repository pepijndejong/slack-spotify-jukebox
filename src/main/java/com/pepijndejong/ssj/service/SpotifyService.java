package com.pepijndejong.ssj.service;

import com.pepijndejong.ssj.service.exception.InvalidOAuthStateException;
import com.pepijndejong.ssj.service.exception.SpotifyClientNotSetException;
import com.pepijndejong.ssj.service.exception.TokenNotAcceptedException;
import com.pepijndejong.ssj.service.exception.TokenNotFoundException;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SpotifyService {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUrl;

    private static final String SECRET_STATE_PREFIX = "host-";
    private final static String PLAYLIST_REGEX = "spotify:user:(?<userId>[a-zA-Z0-9]+):playlist:(?<playlistId>[a-zA-Z0-9]+)";
    private static final String SECRET_STATE = SECRET_STATE_PREFIX + UUID.randomUUID().toString();
    private static final String TOKEN_PROPERTIES = "./token.properties";
    private SpotifyApi api;
    @Getter
    private String accessToken;

    public SpotifyService(@Value("${spotify.clientId}") final String clientId,
                          @Value("${spotify.clientSecret}") final String clientSecret,
                          @Value("${spotify.redirectUrl}") final String redirectUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
    }

    @PostConstruct
    public void init() {
        api = SpotifyApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(URI.create(redirectUrl))
                .build();
    }

    public boolean isReady() {
        return accessToken != null;
    }

    public void refreshAccessToken() {
        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            throw new SpotifyClientNotSetException();
        }

        final AuthorizationCodeCredentials authorizationCodeCredentials = loadToken();
        api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

        final AuthorizationCodeRefreshRequest refreshAccessTokenRequest =
                api.authorizationCodeRefresh().refresh_token(authorizationCodeCredentials.getRefreshToken()).build();

        try {
            final AuthorizationCodeCredentials refreshAccessTokenCredentials = refreshAccessTokenRequest.execute();
            api.setAccessToken(refreshAccessTokenCredentials.getAccessToken());
            accessToken = refreshAccessTokenCredentials.getAccessToken();

            log.info("Access token refreshed successful: {}", refreshAccessTokenCredentials.getAccessToken());
        } catch (IOException | SpotifyWebApiException e) {
            log.error("Failed to refresh token.", e);
            throw new TokenNotAcceptedException(e);
        }
    }

    public String getOauthTokenUrl() {
        final AuthorizationCodeUriRequest authorizationCodeUriRequest = api.authorizationCodeUri()
                .state(SECRET_STATE)
                .scope("user-read-private," +
                        "user-read-email," +
                        "playlist-modify," +
                        "playlist-modify-private," +
                        "user-modify-playback-state," +
                        "user-read-currently-playing," +
                        "user-read-playback-state")
                .show_dialog(true)
                .build();

        return authorizationCodeUriRequest.execute().toString();
    }

    public void createAccessToken(final String receivedState, final String code) {
        if (!receivedState.equals(SECRET_STATE)) {
            throw new InvalidOAuthStateException();
        }

        try {
            AuthorizationCodeCredentials authorizationCodeCredentials = api.authorizationCode(code).build().execute();

            log.info("Successfully retrieved an access token! " + authorizationCodeCredentials.getAccessToken());
            log.info("The access token expires in " + authorizationCodeCredentials.getExpiresIn() + " seconds");
            log.info("Luckily, I can refresh it using this refresh token! " + authorizationCodeCredentials.getRefreshToken());

            accessToken = authorizationCodeCredentials.getAccessToken();
            api.setAccessToken(accessToken);

            storeToken(authorizationCodeCredentials);
        } catch (IOException | SpotifyWebApiException e) {
            log.error("Failed to get the token..", e);
        }
    }

    private void storeToken(final AuthorizationCodeCredentials authorizationCodeCredentials) {
        final Properties prop = new Properties();
        prop.put("accessToken", authorizationCodeCredentials.getAccessToken());
        prop.put("refreshToken", authorizationCodeCredentials.getRefreshToken());
        try {
            prop.store(new FileOutputStream(TOKEN_PROPERTIES), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthorizationCodeCredentials loadToken() {
        final Properties prop = new Properties();
        try {
            final InputStream inputStream = new FileInputStream(TOKEN_PROPERTIES);
            prop.load(inputStream);

            final String accessToken = prop.getProperty("accessToken");
            final String refreshToken = prop.getProperty("refreshToken");
            if (refreshToken == null) {
                throw new TokenNotFoundException();
            }

            return new AuthorizationCodeCredentials.Builder()
                    .setAccessToken(accessToken)
                    .setRefreshToken(refreshToken).build();
        } catch (IOException e) {
            throw new TokenNotFoundException(e);
        }
    }

    public Playlist getPlayList(final String playlistId) {
        final GetPlaylistRequest playlistRequest = api.getPlaylist(playlistId).build();

        try {
            return playlistRequest.execute();
        } catch (IOException | SpotifyWebApiException e) {
            log.error("Failed to get playlist.", e);
        }
        return null;
    }

    public Track searchTrack(final String query) {
        final SearchTracksRequest trackSearchRequest = api.searchTracks(query).build();

        try {
            final Paging<Track> trackPage = trackSearchRequest.execute();

            if (trackPage.getItems().length > 0) {
                return trackPage.getItems()[0];
            } else {
                log.info("No track found for {}", query);
            }
        } catch (IOException | SpotifyWebApiException e) {
            log.error("Failed to search track.", e);
        }
        return null;
    }

    public Track searchTrackById(final String trackId) {
        final GetTrackRequest trackRequest = api.getTrack(trackId).build();

        try {
            return trackRequest.execute();
        } catch (IOException | SpotifyWebApiException e) {
            log.error("Failed to get track.", e);
        }
        return null;
    }

    public Playlist getPlaylistByUri(final String defaultPlayList) {
        final Pattern pattern = Pattern.compile(PLAYLIST_REGEX);
        final Matcher matcher = pattern.matcher(defaultPlayList);
        if (matcher.find()) {
            final String playlistId = matcher.group("playlistId");

            return getPlayList(playlistId);
        }
        return null;
    }

}
