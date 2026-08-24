package com.pilo.cases;

import java.util.UUID;

public record CaseRequirementStatusResponse(
		UUID id, String code, String name, String description, boolean mandatory, String fulfillmentStatus) {}
