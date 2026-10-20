package com.lokendra.springecom.model.dto;

import com.lokendra.springecom.model.Product;
import java.util.List;

public record AgentResponse(
        String reply,
        String thoughtProcess,
        List<ToolCallRecord> toolCalls,
        List<AgentAction> actions,
        List<Product> products,
        List<String> citations
) {}
