package com.lokendra.springecom.model.dto;

import java.util.Map;

public record ToolCallRecord(
        String toolName,
        Map<String, Object> arguments,
        String resultSummary,
        String status 
) {}
