package com.runmile.completion.service;

import com.runmile.completion.domain.Completion;
import com.runmile.completion.dto.CompletionResponse;
import com.runmile.completion.repository.CompletionRepository;
import com.runmile.global.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompletionService {
    private final CompletionRepository completionRepository;

    public CompletionService(CompletionRepository completionRepository) {
        this.completionRepository = completionRepository;
    }

    @Transactional(readOnly = true)
    public CompletionResponse getCompletion(Long runnerId) {
        Completion completion = completionRepository.findByRunnerId(runnerId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "COMPLETION_NOT_FOUND",
                        "완주 기록을 찾을 수 없습니다."
                ));

        return CompletionResponse.from(completion);
    }
}
