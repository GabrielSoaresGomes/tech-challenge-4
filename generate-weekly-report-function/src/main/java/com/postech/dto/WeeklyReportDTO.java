package com.postech.dto;

import com.postech.domain.Feedback;

import java.util.List;

public record WeeklyReportDTO(
        List<Feedback> feedbackList,
        WeeklyAggregationsDTO aggregations
) {}
