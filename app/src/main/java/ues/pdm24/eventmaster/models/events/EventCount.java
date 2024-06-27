package ues.pdm24.eventmaster.models.events;

public class EventCount {
    private int total;
    private int libre;
    private int agendado;

    private EventCount() {
    }

    public EventCount(int total, int libre, int agendado) {
        this.total = total;
        this.libre = libre;
        this.agendado = agendado;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getLibre() {
        return libre;
    }

    public void setLibre(int libre) {
        this.libre = libre;
    }

    public int getAgendado() {
        return agendado;
    }

    public void setAgendado(int agendado) {
        this.agendado = agendado;
    }
}
