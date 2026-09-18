package com.runmile.infrastructure.blockchain;

import com.runmile.completion.domain.Completion;
import com.runmile.completion.domain.NftRecord;
import com.runmile.runner.domain.Runner;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;

public record CompletionAnchorPayload(
        Long runnerId,
        String runnerCode,
        Long completionId,
        String course,
        int finishTimeSeconds,
        Instant completedAt
) {
    private static final String VERSION = "RUNMILE_COMPLETION_V1";

    public CompletionAnchorPayload {
        Objects.requireNonNull(runnerId, "runnerId는 필수입니다.");
        Objects.requireNonNull(runnerCode, "runnerCode는 필수입니다.");
        Objects.requireNonNull(completionId, "completionId는 필수입니다.");
        Objects.requireNonNull(course, "course는 필수입니다.");
        Objects.requireNonNull(completedAt, "completedAt은 필수입니다.");
        if (runnerId <= 0 || completionId <= 0 || runnerCode.isBlank() || finishTimeSeconds <= 0) {
            throw new IllegalArgumentException("완주 앵커 데이터가 올바르지 않습니다.");
        }
    }

    public static CompletionAnchorPayload from(NftRecord record) {
        Runner runner = record.getRunner();
        Completion completion = record.getCompletion();
        if (!completion.isCompleted()) {
            throw new IllegalArgumentException("완료되지 않은 기록은 체인에 앵커링할 수 없습니다.");
        }
        return new CompletionAnchorPayload(
                runner.getId(),
                runner.getRunnerCode(),
                completion.getId(),
                runner.getCourse().name(),
                Objects.requireNonNull(completion.getFinishTimeSeconds(), "finishTimeSeconds는 필수입니다."),
                completion.getCompletedAt()
        );
    }

    public String toHexData() {
        String canonical = String.join("|",
                VERSION,
                runnerId.toString(),
                runnerCode,
                completionId.toString(),
                course,
                Integer.toString(finishTimeSeconds),
                completedAt.toString()
        );
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            return "0x" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", exception);
        }
    }
}
