package com.postech.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeedbackInputDTO {
    
    @JsonProperty("description")
    public String description;
    
    @JsonProperty("note")
    public int note;
    
    @JsonProperty("urgency")
    public boolean urgency;
    
    public FeedbackInputDTO() {
    }
    
    public FeedbackInputDTO(String description, int note, boolean urgency) {
        this.description = description;
        this.note = note;
        this.urgency = urgency;
    }
}
