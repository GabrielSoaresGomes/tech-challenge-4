package com.postech.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeedbackInputDTO {
    
    @JsonProperty("description")
    public String description;
    
    @JsonProperty("note")
    public int note;
    
    public FeedbackInputDTO() {
    }
    
    public FeedbackInputDTO(String description, int note) {
        this.description = description;
        this.note = note;
    }
}
