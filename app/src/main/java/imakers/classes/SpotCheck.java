package imakers.classes;

public class SpotCheck {

    Long deleteTime;
    int hash;

    public SpotCheck(Long deleteTime, int hash) {
        this.deleteTime = deleteTime;
        this.hash = hash;
    }

    public Long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }
}
