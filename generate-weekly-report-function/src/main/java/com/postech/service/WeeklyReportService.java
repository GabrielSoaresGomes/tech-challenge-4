package com.postech.service;

import com.postech.domain.Feedback;
import com.postech.dto.WeeklyAggregationsDTO;
import com.postech.dto.WeeklyReportDTO;
import com.postech.repository.FeedbackRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class WeeklyReportService {

    private final FeedbackRepository feedbackRepository;

    public WeeklyReportService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public WeeklyReportDTO generateWeeklyReport(LocalDateTime now) {
        LocalDateTime end = now;
        LocalDateTime start = end.minusDays(7);

        List<Feedback> feedbacks = feedbackRepository.findBetween(start, end);

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Map<String, Long> countPerDay = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        f -> f.sendDate.toLocalDate().format(dayFmt),
                        TreeMap::new,
                        Collectors.counting()
                ));

        Map<String, Long> countPerUrgency = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        f -> f.urgency ? "URGENTE" : "NÃO URGENTE",
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        WeeklyAggregationsDTO aggregations = new WeeklyAggregationsDTO(countPerDay, countPerUrgency);

        return new WeeklyReportDTO(feedbacks, aggregations, start, end);
    }
}
