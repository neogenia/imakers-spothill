package imakers.classes;

import com.google.gson.annotations.SerializedName;

import java.util.Timer;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

public class CopyCampaign {


    private Spot spot;
    private int type;
    private int allowDetail;
    private String image;
    private String title;
    private String description;
    @SerializedName("id")
    private Long idCamp;
    private Providers provider;
    private Category category;
    private int showDistance;
    private int remaining;
    private String dateTo;
    private String favourite;
    private String reminder;
    private int delay;
    private int event;
    private int checkoutMinor;
    private String notificationTitle;
    private Boolean isGroupCampaign;
    private Boolean isSeparator;
    private Boolean isSeparatorEnable;
    private Boolean isStarted;
    private String ignored;
    Timer delete;

    public CopyCampaign() {
    }

    public Campaign toCampaign() {

        Campaign c = new Campaign();

        c.setType(type);
        c.setId(idCamp);
        c.setImage(image);
        c.setTitle(title);
        c.setDescription(description);
        c.setProvider(provider);
        c.setCategory(category);
        c.setReminder(reminder);
        c.setRemaining(remaining);
        c.setDateTo(dateTo);
        c.setFavourite(favourite);


        return c;

    }

    public Boolean getIsStarted() {

        if(isStarted == null) {
            isStarted = false;
        }

        return isStarted;
    }

    public void setIsStarted(Boolean isStarted) {
        this.isStarted = isStarted;
    }

    public Timer getDelete() {
        return delete;
    }

    public void setDelete(Timer delete) {
        this.delete = delete;
    }

    public String getIgnored() {
        return ignored;
    }

    public void setIgnored(String ignored) {
        this.ignored = ignored;
    }

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public Boolean getIsSeparatorEnable() {

        if(isSeparatorEnable == null)
            return false;

        return isSeparatorEnable;
    }

    public void setIsSeparatorEnable(Boolean isSeparatorEnable) {
        this.isSeparatorEnable = isSeparatorEnable;
    }

    public Boolean getIsGroupCampaign() {
        return isGroupCampaign;
    }

    public void setIsGroupCampaign(Boolean isGroupCampaign) {
        this.isGroupCampaign = isGroupCampaign;
    }

    public Boolean getIsSeparator() {
        return isSeparator;
    }

    public void setIsSeparator(Boolean isSeparator) {
        this.isSeparator = isSeparator;
    }

    public int getShowDistance() {
        return showDistance;
    }

    public void setShowDistance(int showDistance) {
        this.showDistance = showDistance;
    }

    public int getAllowDetail() {
        return allowDetail;
    }

    public void setAllowDetail(int allowDetail) {
        this.allowDetail = allowDetail;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return this.idCamp;
    }

    public void setId(Long id) {
        this.idCamp = id;
    }

    public Providers getProvider() {
        return provider;
    }

    public void setProvider(Providers provider) {
        this.provider = provider;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getFavourite() {
        return favourite;
    }

    public void setFavourite(String favourite) {
        this.favourite = favourite;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public int getCheckoutMinor() {
        return checkoutMinor;
    }

    public void setCheckoutMinor(int checkoutMinor) {
        this.checkoutMinor = checkoutMinor;
    }

    public String getNotificationTitle() {

        if(notificationTitle == null) {
            return "";
        }

        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
