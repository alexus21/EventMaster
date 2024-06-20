package ues.pdm24.eventmaster.events;

public class Events {
    public String id;
    public String eventTitle;
    public String location;
    public String category;
    public String date;
    public String description;
    public String imageUrl;
    public String assistants;
    public String creatorId;

    public Events(String id, String eventTitle, String location, String category,
                  String date, String description, String imageUrl, String assistants, String creatorId) {
        this.id = id;
        this.eventTitle = eventTitle;
        this.location = location;
        this.category = category;
        this.date = date;
        this.description = description;
        this.imageUrl = imageUrl;
        this.assistants = assistants;
        this.creatorId = creatorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAssistants() {
        return assistants;
    }

    public void setAssistants(String assistants) {
        this.assistants = assistants;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}
