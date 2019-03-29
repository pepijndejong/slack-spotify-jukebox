package com.pepijndejong.ssj.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserActionRecorder {

    private static final int SECONDS_TO_THROTTLE_ACTIVITY = 60;

    private Map<String, Instant> userActivityTimes = new HashMap<>();

    public void recordActivity(final String username) {
        userActivityTimes.put(username, Instant.now());
    }

    public boolean mayDoActionAgain(final String username) {
        if (userActivityTimes.containsKey(username)) {
            final Instant lastActivity = userActivityTimes.get(username);
            if (Instant.now().minusSeconds(SECONDS_TO_THROTTLE_ACTIVITY).isBefore(lastActivity)) {
                return false;
            }
        }
        return true;
    }

}
