package com.pepijndejong.ssj.service;

import com.pepijndejong.ssj.service.exception.SpotifyClientNotSetException;
import com.pepijndejong.ssj.service.exception.TokenNotAcceptedException;
import com.pepijndejong.ssj.service.exception.TokenNotFoundException;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SpotifyTokenRefresher {

    private static final int FIFTY_MINUTES_IN_MILLIS = 50 * 60 * 1000;

    private final SpotifyService spotifyService;
    private final SlackService slackService;
    private final MessageCreator messageCreator;

    @Autowired
    public SpotifyTokenRefresher(final SpotifyService spotifyService, final SlackService slackService, final MessageCreator messageCreator) {
        this.spotifyService = spotifyService;
        this.slackService = slackService;
        this.messageCreator = messageCreator;
    }

    @Scheduled(fixedDelay = FIFTY_MINUTES_IN_MILLIS)
    public void refreshAccessToken() {
        try {
            spotifyService.refreshAccessToken();
        } catch (TokenNotAcceptedException | TokenNotFoundException e) {
            final String oauthTokenUrl = spotifyService.getOauthTokenUrl();
            slackService.sendMessage(new SlackPreparedMessage.Builder()
                    .withMessage(messageCreator.noSpotifyTokenFound(oauthTokenUrl))
                    .build());
        } catch (SpotifyClientNotSetException e) {
            log.error("Spotify client details not set. Add them in application.properties", e);
        }
    }

}
