const validationFailureLabels: Record<string, string> = {
  CONFIDENCE: "La confianza de la IA está por debajo del umbral mínimo.",
  EXPECTED_DOCUMENT_TYPE: "El tipo de documento detectado no coincide con el requisito.",
  FUTURE_EXPIRATION: "La fecha de vencimiento no es válida o ya ha expirado.",
};

const extractionFieldLabels: Record<string, string> = {
  personName: "Nombre del titular",
  idNumber: "Número de identificación",
  issueDate: "Fecha de expedición",
  expirationDate: "Fecha de vencimiento",
  address: "Dirección",
  documentType: "Tipo de documento",
};

export function validationFailureLabel(code: string) {
  return validationFailureLabels[code] ?? code;
}

export function extractionFieldLabel(key: string) {
  return extractionFieldLabels[key] ?? key;
}

export function orderedExtractionEntries(fields: Record<string, string>) {
  const preferredOrder = [
    "personName",
    "idNumber",
    "issueDate",
    "expirationDate",
    "address",
    "documentType",
  ];
  const entries: Array<[string, string]> = [];
  for (const key of preferredOrder) {
    const value = fields[key];
    if (value) {
      entries.push([key, value]);
    }
  }
  for (const [key, value] of Object.entries(fields)) {
    if (!preferredOrder.includes(key)) {
      entries.push([key, value]);
    }
  }
  return entries;
}
