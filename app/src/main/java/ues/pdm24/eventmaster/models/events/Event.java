package ues.pdm24.eventmaster.models.events;

public class Event {
    private String image;
    private String title;
    private String location;
    private String category;
    private String date;
    private String description;
    private int assistants;

    public Event(String image, String title, String location, String category, String date, String description, int assistants) {
        this.image = image;
        this.title = title;
        this.location = location;
        this.category = category;
        this.date = date;
        this.description = description;
        this.assistants = assistants;
    }

    public Event() {
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

    public int getAssistants() {
        return assistants;
    }

    public void setAssistants(int assistants) {
        this.assistants = assistants;
    }

    @Override
    public String toString() {
        return "Event{" + title + "}";
    }
}
