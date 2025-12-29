package com.postech.dto;

import com.postech.domain.Feedback;

import java.time.LocalDateTime;
import java.util.List;

public record WeeklyReportDTO(
        List<Feedback> feedbackList,
        WeeklyAggregationsDTO aggregations,
        LocalDateTime periodStart,
        LocalDateTime periodEnd
) {}
