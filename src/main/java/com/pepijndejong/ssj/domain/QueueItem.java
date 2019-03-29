package com.pepijndejong.ssj.domain;

import lombok.Data;

@Data
public class QueueItem {

    private final String spotifyUri;
    private final String trackName;
    private final String userId;

    @Override
    public String toString() {
        return String.format("%s %s (added by %s)", trackName, spotifyUri, userId);
    }

}
