package com.aerobook.domain.dto.response;


import java.time.LocalDateTime;
import java.util.List;

public record BulkOperationResponse(
        int             totalRequested,
        int             successCount,
        int             failureCount,
        List<Long>      successIds,
        List<FailedOperation> failures,
        LocalDateTime   processedAt
) {
    public record FailedOperation(
            Long   flightId,
            String reason
    ) {}
}
