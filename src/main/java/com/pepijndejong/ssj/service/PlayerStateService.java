package com.pepijndejong.ssj.service;

import com.pepijndejong.ssj.service.exception.NotOwnerOfQueueNumberException;
import com.pepijndejong.ssj.service.exception.UnknownQueueNumberException;
import com.pepijndejong.ssj.domain.QueueItem;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

@Service
public class PlayerStateService {

    @Setter
    @Getter
    @Value("${spotify.defaultPlaylistUri}")
    private String defaultPlayList;

    @Getter
    private String currentPlaylist;

    private Queue<QueueItem> trackQueue = new LinkedList<>();

    public void setDefaultPlayListCurrentPlaylist() {
        this.currentPlaylist = defaultPlayList;
    }

    public synchronized int addToQueue(final String spotifyUri, final String userId, final String trackName) {
        final QueueItem queueItem = new QueueItem(spotifyUri, trackName, userId);
        trackQueue.add(queueItem);
        return trackQueue.size();
    }

    public synchronized void removeFromQueue(final int queueNumber, final String userId) {
        final int queueIndex = queueNumber - 1;
        if (queueNumber > trackQueue.size() || queueNumber <= 0) {
            throw new UnknownQueueNumberException();
        }

        final Iterator<QueueItem> queueIterator = trackQueue.iterator();
        int index = 0;
        while (queueIterator.hasNext()) {
            final QueueItem queueItem = queueIterator.next();
            if (queueIndex == index) {
                if (userId.equals(queueItem.getUserId())) {
                    queueIterator.remove();
                    return;
                } else {
                    throw new NotOwnerOfQueueNumberException();
                }
            }

            index++;
        }
    }

    public synchronized String getNextFromQueue() {
        final QueueItem queueItem = trackQueue.poll();
        if (queueItem != null) {
            currentPlaylist = null;
            return queueItem.getSpotifyUri();
        }
        return null;
    }

    public synchronized String getPrintableQueue() {
        if (trackQueue.size() == 0) {
            return "The queue is empty, give me some!";
        }

        final StringBuilder queueAsString = new StringBuilder();
        int index = 1;
        for (QueueItem queueItem : trackQueue) {
            queueAsString.append(index);
            queueAsString.append(" : ");
            queueAsString.append(queueItem.toString());
            queueAsString.append("\n");

            index++;
        }

        return queueAsString.toString();
    }

    public boolean isDefaultPlaylistCurrentPlaylist() {
        return defaultPlayList.equals(currentPlaylist);
    }

}
