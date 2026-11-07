import { describe, expect, it } from "vitest";
import { describeDeadline } from "@/lib/cases/dates";

const now = new Date("2026-03-10T10:00:00Z");

describe("describeDeadline", () => {
  it("counts remaining days and flags the last three as urgent", () => {
    expect(describeDeadline("2026-03-20T10:00:00Z", "IN_PROGRESS", now)).toMatchObject({
      label: "10 días restantes",
      urgent: false,
      overdue: false,
    });
    expect(describeDeadline("2026-03-11T10:00:00Z", "IN_PROGRESS", now)).toMatchObject({
      label: "1 día restante",
      urgent: true,
    });
  });

  it("reports overdue deadlines in days", () => {
    expect(describeDeadline("2026-03-07T10:00:00Z", "PENDING", now)).toMatchObject({
      label: "Vencido hace 3 días",
      overdue: true,
      urgent: false,
    });
  });

  it("ignores the deadline once the case is closed", () => {
    expect(describeDeadline("2026-03-01T10:00:00Z", "APPROVED", now)).toMatchObject({
      label: "Expediente cerrado",
      overdue: false,
      urgent: false,
      closed: true,
    });
  });
});
