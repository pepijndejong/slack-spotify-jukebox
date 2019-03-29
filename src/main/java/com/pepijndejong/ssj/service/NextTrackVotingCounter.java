package com.pepijndejong.ssj.service;

import com.pepijndejong.ssj.service.exception.AlreadyVotedException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class NextTrackVotingCounter {

    private static final int MINIMUM_NUMBER_OF_NEXT_VOTES = 2;

    private String lastKnownTrackId = null;
    private Set<String> votesForNextTrack = new HashSet<>();

    public synchronized int voteForNextTrack(final String username, final String trackId) {
        if (lastKnownTrackId == null || !lastKnownTrackId.equals(trackId)) {
            votesForNextTrack.clear();
        }
        lastKnownTrackId = trackId;

        if (votesForNextTrack.contains(username)) {
            throw new AlreadyVotedException();
        }

        votesForNextTrack.add(username);
        final int newNrOfVotes = votesForNextTrack.size();

        return MINIMUM_NUMBER_OF_NEXT_VOTES - newNrOfVotes;
    }

}
