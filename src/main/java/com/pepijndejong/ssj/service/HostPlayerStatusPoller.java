package com.pepijndejong.ssj.service;

import com.pepijndejong.ssj.domain.PlayerPlayingState;
import com.pepijndejong.ssj.service.exception.SpotifyApiCallFailedException;
import com.pepijndejong.ssj.service.exception.SpotifyPlayerNotRunningException;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class HostPlayerStatusPoller {

    private static final int MILLIS_BEFORE_TRACK_ENDS = 3000;
    private static final int MAX_POLL_TIME_IN_MILLIS = 30000;
    private static final int INITIAL_DELAY_IN_MILLIS = 5000;
    private static final int AFTER_PLAY_DELAY_IN_MILLIS = 1000;
    private static final int TRACK_OVERLAP_BUFFER_IN_MILLIS = 500;
    private String lastTrackId = null;
    private UUID activeStartEventId = null;

    private final SlackService slackService;

    private final SpotifyService spotifyService;

    private final SpotifyHostPlayerService spotifyHostPlayerService;

    private final NextTrackService nextTrackService;

    private final TaskScheduler taskScheduler;

    @Autowired
    public HostPlayerStatusPoller(final SlackService slackService, final SpotifyService spotifyService, final SpotifyHostPlayerService spotifyHostPlayerService, final NextTrackService nextTrackService, final TaskScheduler taskScheduler) {
        this.slackService = slackService;
        this.spotifyService = spotifyService;
        this.spotifyHostPlayerService = spotifyHostPlayerService;
        this.nextTrackService = nextTrackService;
        this.taskScheduler = taskScheduler;
    }

    public void checkSpotifyStatus(final UUID startEventId) {
        try {
            if (!spotifyService.isReady()) {
                log.info("Sevice not ready yet...");
                scheduleNextPoll(INITIAL_DELAY_IN_MILLIS, startEventId);
                return;
            }

            final PlayerPlayingState playingState = spotifyHostPlayerService.getSpotifyPlayer().getState();
            if (!playingState.isPlaying()) {
                spotifyHostPlayerService.playDefaultPlayList();
                scheduleNextPoll(AFTER_PLAY_DELAY_IN_MILLIS, startEventId);
            } else {
                final String trackId = playingState.getCurrentTrackId();
                if (!trackId.equals(lastTrackId)) {
                    final String fullTrackTitle = playingState.getFullTrackTitle();
                    final SlackPreparedMessage slackMessage = new SlackPreparedMessage.Builder()
                            .withMessage(fullTrackTitle)
                            .withUnfurl(false)
                            .build();
                    slackService.sendMessage(slackMessage);
                }

                final double millisecondsLeftForCurrentTrack = playingState.getMillisecondsLeftForCurrentTrack();
                final boolean trackIsAlmostFinished = millisecondsLeftForCurrentTrack < MILLIS_BEFORE_TRACK_ENDS;
                if (trackIsAlmostFinished) {
                    log.info("Track is almost at the end...");
                    scheduleNextTrack((int) millisecondsLeftForCurrentTrack - TRACK_OVERLAP_BUFFER_IN_MILLIS, startEventId);
                } else {
                    final int nextPollInMillis = (int) millisecondsLeftForCurrentTrack - MILLIS_BEFORE_TRACK_ENDS;
                    scheduleNextPoll(nextPollInMillis, startEventId);
                }

                lastTrackId = trackId;
            }
        } catch (SpotifyPlayerNotRunningException e) {
            handleSpotifyPlayerNotRunning();
            scheduleNextPoll(INITIAL_DELAY_IN_MILLIS, startEventId);
        } catch (RuntimeException e) {
            log.error("Error while polling status.", e);
            scheduleNextPoll(INITIAL_DELAY_IN_MILLIS, startEventId);
        }
    }

    private void startNextTrackIfNeeded(final UUID startEventId) {
        nextTrackService.selectAndPlayNextTrack();
        scheduleNextPoll(AFTER_PLAY_DELAY_IN_MILLIS, startEventId);
    }

    private synchronized void scheduleNextPoll(final int waitPeriodInMillis, final UUID startEventId) {
        if (!startEventId.equals(activeStartEventId)) {
            log.info("This event id is no longer active.");
            return;
        }

        final int nextPollInSeconds;
        if (waitPeriodInMillis >= MAX_POLL_TIME_IN_MILLIS) {
            nextPollInSeconds = MAX_POLL_TIME_IN_MILLIS;
        } else {
            nextPollInSeconds = waitPeriodInMillis;
        }
        log.info("Scheduling next poll in {} ms", nextPollInSeconds);
        taskScheduler.schedule(() -> {
            checkSpotifyStatus(startEventId);
        }, Date.from(Instant.now().plusMillis(nextPollInSeconds)));
    }

    private synchronized void scheduleNextTrack(final int waitPeriodInMillis, final UUID startEventId) {
        if (!startEventId.equals(activeStartEventId)) {
            log.info("This event id is no longer active.");
            return;
        }

        log.info("Scheduling next track in {} ms", waitPeriodInMillis);
        taskScheduler.schedule(() -> {
            startNextTrackIfNeeded(startEventId);
        }, Date.from(Instant.now().plusMillis(waitPeriodInMillis)));
    }

    public synchronized void reset() {
        activeStartEventId = UUID.randomUUID();

        scheduleNextPoll(AFTER_PLAY_DELAY_IN_MILLIS, activeStartEventId);
    }

    @PostConstruct
    public void init() {
        taskScheduler.schedule(this::startPlayer, Date.from(Instant.now().plusMillis(INITIAL_DELAY_IN_MILLIS)));
    }

    public synchronized void startPlayer() {
        activeStartEventId = UUID.randomUUID();

        try {
            spotifyHostPlayerService.playDefaultPlayList();
            scheduleNextPoll(AFTER_PLAY_DELAY_IN_MILLIS, activeStartEventId);
        } catch (SpotifyApiCallFailedException e) {
            log.error("Failed to start, will try again in 5 seconds.");
            scheduleNextPoll(INITIAL_DELAY_IN_MILLIS, activeStartEventId);
        } catch (SpotifyPlayerNotRunningException e) {
            handleSpotifyPlayerNotRunning();
            scheduleNextPoll(INITIAL_DELAY_IN_MILLIS, activeStartEventId);
        }
    }

    private void handleSpotifyPlayerNotRunning() {
        try {
            log.error("It seems like the Spotify player is not running.");
            slackService.sendMessage(new SlackPreparedMessage.Builder()
                    .withMessage("It seems like your Spotify player is not running. Make sure it is playing a song (any song). Will try again in 5 seconds...")
                    .build());
        } catch (RuntimeException e) {
            log.error("Could not send error message to Slack :-(", e);
        }
    }
}
