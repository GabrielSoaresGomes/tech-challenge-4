package com.postech.dto;

import java.util.Map;

public record WeeklyAggregationsDTO(
        Map<String, Long> countPerDay,
        Map<String, Long> countPerUrgency
) {}
