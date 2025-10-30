package com.example.personalmemory.model;

public class NotificationMessage {
    private String routineId;
    private String question;
    private String timeOfDay;

    public NotificationMessage() {}

    public NotificationMessage(String routineId, String question, String timeOfDay) {
        this.routineId = routineId;
        this.question = question;
        this.timeOfDay = timeOfDay;
    }

    // getters & setters
    public String getRoutineId(){return routineId;}
    public void setRoutineId(String routineId){this.routineId = routineId;}
    public String getQuestion(){return question;}
    public void setQuestion(String question){this.question = question;}
    public String getTimeOfDay(){return timeOfDay;}
    public void setTimeOfDay(String timeOfDay){this.timeOfDay = timeOfDay;}
}
