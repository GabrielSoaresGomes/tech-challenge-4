package com.postech.service;

import com.postech.domain.Feedback;
import com.postech.dto.WeeklyAggregationsDTO;
import com.postech.dto.WeeklyReportDTO;
import com.postech.repository.FeedbackRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class WeeklyReportService {

    @Inject
    FeedbackRepository repository;

    public WeeklyReportDTO generateWeeklyReport() {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);

        List<Feedback> feedbacks = repository.findByDateRange(start, end);

        Map<String, Long> perDay = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        f -> f.sendDate.toLocalDate().toString(),
                        Collectors.counting()
                ));

        Map<String, Long> perUrgency = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        f -> f.urgency ? "Urgente" : "Não urgente",
                        Collectors.counting()
                ));

        WeeklyAggregationsDTO agg = new WeeklyAggregationsDTO(perDay, perUrgency);

        return new WeeklyReportDTO(feedbacks, agg);
    }
}
