package com.personal.storagegate.dto.llm;

import java.util.List;

public record ModelListResponse(
        String object,
        List<ModelData> data
) {
    public record ModelData(
            String id,
            String object,
            String owned_by
    ) {}
}