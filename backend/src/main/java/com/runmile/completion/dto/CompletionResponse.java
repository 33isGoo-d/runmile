package com.runmile.completion.dto;

import com.runmile.completion.domain.Completion;
import com.runmile.global.type.Course;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record CompletionResponse(
        boolean completed,
        Course course,
        Integer finishTimeSeconds,
        LocalDateTime completedAt
) {
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    public static CompletionResponse from(Completion completion) {
        LocalDateTime completedAt = completion.getCompletedAt() == null
                ? null
                : LocalDateTime.ofInstant(completion.getCompletedAt(), SEOUL_ZONE);

        return new CompletionResponse(
                completion.isCompleted(),
                completion.getRunner().getCourse(),
                completion.getFinishTimeSeconds(),
                completedAt
        );
    }
}
