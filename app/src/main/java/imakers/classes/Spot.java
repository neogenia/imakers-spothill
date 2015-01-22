package imakers.classes;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Spots")
public class Spot extends Model{
    @Key
    @Column("major")
    Integer major;
    @Column("minor")
    Integer minor;
    @Column("time")
    Long time;
    @Column("hash")
    String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Spot(Integer major, Integer minor, Long time) {
        this.major = major;
        this.minor = minor;
        this.time = time;
    }

    public Spot(Integer major, Integer minor, Long time, String hash) {
        this.major = major;
        this.minor = minor;
        this.time = time;
        this.hash = hash;
    }

    public Spot() {
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
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
}
