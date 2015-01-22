package imakers.classes;

import java.util.Timer;

public class Pair {

    String hash;
    Campaign campaign;
    Long time;
    Integer distance;
    Boolean isStarted;
    Timer timer;
    int counter;

    public Pair(String hash, Campaign campaign, Long time) {
        this.hash = hash;
        this.campaign = campaign;
        this.time = time;
        timer = new Timer();
        isStarted = false;
    }

    public Pair(String hash, Campaign campaign) {
        this.hash = hash;
        this.campaign = campaign;
        timer = new Timer();
        isStarted = false;
    }

    public Pair(String hash, Campaign campaign, Integer distance) {
        this.hash = hash;
        this.campaign = campaign;
        this.distance = distance;
        timer = new Timer();
        isStarted = false;
    }

    public Boolean getIsStarted() {
        return isStarted;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void setIsStarted(Boolean isStarted) {
        this.isStarted = isStarted;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }
}
