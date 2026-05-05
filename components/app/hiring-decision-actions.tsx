"use client";

import * as React from "react";

import { Button } from "@/components/ui/button";
import { createHiringDecision } from "@/lib/backend-api";

type HiringDecisionActionsProps = {
  resumeId: number;
  reportId: number;
  initialDecision: "HIRE" | "HOLD" | "REJECT";
};

const decisions = ["HIRE", "HOLD", "REJECT"] as const;

export function HiringDecisionActions({
  resumeId,
  reportId,
  initialDecision,
}: HiringDecisionActionsProps) {
  const [decision, setDecision] = React.useState(initialDecision);
  const [saving, setSaving] = React.useState<string | null>(null);

  async function saveDecision(nextDecision: (typeof decisions)[number]) {
    setSaving(nextDecision);
    try {
      await createHiringDecision({
        resumeId,
        reportId,
        decision: nextDecision,
        notes: "Recruiter decision from ranking dashboard.",
      });
      setDecision(nextDecision);
    } finally {
      setSaving(null);
    }
  }

  return (
    <div className="mt-4 flex flex-wrap gap-2">
      {decisions.map((item) => (
        <Button
          key={item}
          type="button"
          size="sm"
          variant={decision === item ? "secondary" : "ghost"}
          disabled={saving !== null}
          onClick={() => saveDecision(item)}
        >
          {saving === item ? "Saving" : item}
        </Button>
      ))}
    </div>
  );
}
