package com.pilo.workflows;

import java.util.List;
import java.util.UUID;

public record WorkflowResponse(UUID caseId, List<WorkflowTaskResponse> tasks) {}
