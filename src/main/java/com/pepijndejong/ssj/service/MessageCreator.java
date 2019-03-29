package com.pepijndejong.ssj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageCreator {

    @Value("${jukebox-name}")
    private String jukeboxName;

    private static final String SELECT_TRACK_FEEDBACK_FILE = "select_track_feedback.txt";
    private static final String TOO_MANY_REQUESTS_FILE = "too_many_requests.txt";
    private static final String TRACK_NOT_FOUND_FILE = "track_not_found.txt";

    private final RandomSentenceService randomSentenceService;

    @Autowired
    public MessageCreator(final RandomSentenceService randomSentenceService) {
        this.randomSentenceService = randomSentenceService;
    }

    public String playTrack(final String username) {
        final String randomSelectTrackFeedback =
                randomSentenceService.giveRandomSentence(SELECT_TRACK_FEEDBACK_FILE, username);

        return String.format("%s I will play it right away!", randomSelectTrackFeedback);
    }

    public String playPlaylist(final String username, final String userId, final String playlistName) {
        return String.format("%s lifting off by the beats of %s! The new default playlist is %s by %s.", jukeboxName, username, playlistName, userId);
    }

    public String trackNotFound(final String username) {
        return randomSentenceService.giveRandomSentence(TRACK_NOT_FOUND_FILE, username);
    }

    public String tooManyRequest(final String username) {
        return randomSentenceService.giveRandomSentence(TOO_MANY_REQUESTS_FILE, username);
    }

    public String nextTrackVotesNeeded(final String username, final int votesNeeded) {
        return String.format("You'll need %s more listener to vote for the next track %s!", votesNeeded, username);
    }

    public String alreadyVotedForNextTrack(final String username) {
        return String.format("You can only vote once %s ;-)", username);
    }

    public String addToQueue(final String username, final String fullTrackName, final int queueSize) {
        final String randomSelectTrackFeedback =
                randomSentenceService.giveRandomSentence(SELECT_TRACK_FEEDBACK_FILE, username);

        final String message;
        if (queueSize == 1) {
            message = String.format("%s %s is up next!", randomSelectTrackFeedback, fullTrackName);
        } else if (queueSize == 2) {
            message = String.format("%s %s will be played after 1 other.", randomSelectTrackFeedback, fullTrackName);
        } else {
            message = String.format("%s %s is number %s in the queue.", randomSelectTrackFeedback, fullTrackName, queueSize);
        }
        return message;
    }

    public String removeQueueUnknownNumber(final String username, final String queueNumber) {
        return String.format("Cannot find number %s in the queue %s", queueNumber, username);
    }

    public String removeQueueNotOwner(final String username) {
        return String.format("You can only remove your own tracks from the queue %s", username);
    }

    public String removeQueue(final String username, final String printableQueue) {
        return String.format("I'm sorry to see this beauty go %s. Here's the new queue:\n%s", username, printableQueue);
    }

    public String help() {
        return String.format("Welcome to %s! You are in control: \n\n" +
                "name\n" +
                "next\n" +
                "play [spotify track uri, playlist uri or track name]\n" +
                "queue [spotify track uri or track name]\n" +
                "show queue\n" +
                "remove queue [track number]\n" +
                "playlist [playlist uri]\n" +
                "show playlist\n" +
                "sound [sound effect]\n" +
                "show sound\n", jukeboxName);
    }

    public String setDefaultPlaylist(final String username, final String playlistName) {
        return String.format("New default playlist set: %s by %s", playlistName, username);
    }

    public String showDefaultPlaylist(final String username, final String playlistName, final String playlistUri) {
        return String.format("Current default playlist: %s by %s %s", playlistName, username, playlistUri);
    }

    public String backToDefaultPlaylist(final String username, final String playlistName) {
        return String.format("Back to the default playlist: %s by %s", playlistName, username);
    }

    public String playlistNotFound(final String username) {
        return String.format("I could not find this playlist %s :-(", username);
    }

    public String noSpotifyTokenFound(final String oauthUrl) {
        return String.format("No Spotify token found... Create one here: %s", oauthUrl);
    }

    public String soundEffectNotFound(final String username, final String sound) {
        return String.format("I don't have %s sound %s :-(", sound, username);
    }

    public String showSoundEffects(final List<String> sounds) {
        final StringBuilder result = new StringBuilder();
        for (String sound : sounds) {
            result.append(sound);
            result.append("\n");
        }

        return result.toString();
    }

    public String notSupportedChannel(final String channel) {
        return String.format("I'm too busy handling all the messages in the %s channel, please talk to me there!", channel);
    }

}
