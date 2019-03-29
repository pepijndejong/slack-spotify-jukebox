package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.*;
import com.pepijndejong.ssj.service.exception.AlreadyVotedException;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NextCommand extends AbstractCommand {

    private final SpotifyHostPlayerService spotifyHostPlayerService;

    private final UserActionRecorder userActionRecorder;

    private final NextTrackVotingCounter nextTrackVotingCounter;

    private final MessageCreator messageCreator;

    private final NextTrackService nextTrackService;

    private final HostPlayerStatusPoller hostPlayerStatusPoller;

    @Autowired
    public NextCommand(final SpotifyHostPlayerService spotifyHostPlayerService, final UserActionRecorder userActionRecorder, final NextTrackVotingCounter nextTrackVotingCounter, final MessageCreator messageCreator, final NextTrackService nextTrackService, final HostPlayerStatusPoller hostPlayerStatusPoller) {
        this.spotifyHostPlayerService = spotifyHostPlayerService;
        this.userActionRecorder = userActionRecorder;
        this.nextTrackVotingCounter = nextTrackVotingCounter;
        this.messageCreator = messageCreator;
        this.nextTrackService = nextTrackService;
        this.hostPlayerStatusPoller = hostPlayerStatusPoller;
    }

    @Override
    public String command() {
        return "next$";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        if (!userActionRecorder.mayDoActionAgain(username)) {
            final String message = messageCreator.tooManyRequest(username);
            return slackMessage(message);
        }
        try {
            final int nrOfNeededVotes = nextTrackVotingCounter.voteForNextTrack(username, spotifyHostPlayerService.getSpotifyPlayer().getState().getCurrentTrackId());
            if (nrOfNeededVotes == 0) {
                playNextTrack(username);
                return null;
            } else {
                final String message = messageCreator.nextTrackVotesNeeded(username, nrOfNeededVotes);
                return slackMessage(message);
            }
        } catch (AlreadyVotedException e) {
            final String message = messageCreator.alreadyVotedForNextTrack(username);
            return slackMessage(message);
        }
    }

    private void playNextTrack(final String username) {
        nextTrackService.selectAndPlayNextForUser();
        userActionRecorder.recordActivity(username);
        hostPlayerStatusPoller.reset();
    }
}
