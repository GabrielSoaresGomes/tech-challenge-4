package br.com.tc4.notify_critical_feedback.domain;

import java.time.LocalDateTime;

public class Feedback {
    public long id;
    public String description;
    public int note;
    public boolean urgency;
    public LocalDateTime sendDate;
}
