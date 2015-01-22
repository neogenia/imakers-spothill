package imakers.classes;

import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Collection;

public class SpotInitiation {
    private ArrayList<Occurence> confirmOccurrence;
    private ArrayList<Campaign> groupCampaigns;
    private ArrayList<Campaign> campaigns;
    private Ignore ignore;
    private String hashSpot;
    private Integer major;
    private Integer minor;
    private Double distance;

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Integer getMajor() {
        return major;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public Integer getMinor() {
        return minor;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public String getHashSpot() {
        return hashSpot;
    }

    public void setHashSpot(String hashSpot) {
        this.hashSpot = hashSpot;
    }

    public Collection<Occurence> getConfirmOccurrence() {
        return confirmOccurrence;
    }

    public void setConfirmOccurrence(ArrayList<Occurence> confirmOccurrence) {
        this.confirmOccurrence = confirmOccurrence;
    }

    public ArrayList<Campaign> getGroupCampaigns()
    {
        return groupCampaigns;
    }

    public void setGroupCampaigns(ArrayList<Campaign> groupCampaigns) {
        this.groupCampaigns = groupCampaigns;
    }

    public Collection<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(ArrayList<Campaign> campaigns) {

        this.campaigns = campaigns;
    }

    public Ignore getIgnore() {

        return ignore;
    }

    public void setIgnore(Ignore ignore) {

        this.ignore = ignore;
    }
}
