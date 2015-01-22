package imakers.classes;

import java.lang.reflect.Array;

public class Occurence {

    private int campaign;
    private int period;
    private int provider;
    private int[] minors;

    public int getCampaign() {
        return campaign;
    }

    public void setCampaign(int campaign) {
        this.campaign = campaign;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getProvider() {
        return provider;
    }

    public void setProvider(int provider) {
        this.provider = provider;
    }

    public int[] getMinors() {
        return minors;
    }

    public void setMinors(int[] minors) {
        this.minors = minors;
    }
}
