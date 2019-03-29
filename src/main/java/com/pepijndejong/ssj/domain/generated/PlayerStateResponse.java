
package com.pepijndejong.ssj.domain.generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlayerStateResponse {

    @SerializedName("timestamp")
    @Expose
    private Long timestamp;
    @SerializedName("progress_ms")
    @Expose
    private Integer progressMs;
    @SerializedName("is_playing")
    @Expose
    private Boolean isPlaying;
    @SerializedName("item")
    @Expose
    private Item item;
    @SerializedName("context")
    @Expose
    private Context context;
    @SerializedName("device")
    @Expose
    private Device device;
    @SerializedName("repeat_state")
    @Expose
    private String repeatState;
    @SerializedName("shuffle_state")
    @Expose
    private Boolean shuffleState;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getProgressMs() {
        return progressMs;
    }

    public void setProgressMs(Integer progressMs) {
        this.progressMs = progressMs;
    }

    public Boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(Boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getRepeatState() {
        return repeatState;
    }

    public void setRepeatState(String repeatState) {
        this.repeatState = repeatState;
    }

    public Boolean getShuffleState() {
        return shuffleState;
    }

    public void setShuffleState(Boolean shuffleState) {
        this.shuffleState = shuffleState;
    }

}
