package com.pilo.cases;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCaseRequest(@NotNull UUID procedureTypeId) {}
