"use client";

import * as React from "react";
import { Plus, Save } from "lucide-react";

import { GlassPanel } from "@/components/app/glass-panel";
import { ProgressBar } from "@/components/app/progress-bar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { createJobRole } from "@/lib/backend-api";

const scoreWeights = [
  ["Skills match", 40],
  ["Experience match", 25],
  ["Projects relevance", 15],
  ["Education match", 10],
  ["Summary keywords", 10],
];

const weightFields = [
  ["Skills match", "skillWeightage", 40],
  ["Experience", "experienceWeightage", 25],
  ["Projects", "projectWeightage", 15],
  ["Education", "educationWeightage", 10],
  ["Keywords", "keywordWeightage", 10],
];

export function JobRoleBuilder() {
  const [skills, setSkills] = React.useState<string[]>([]);
  const [skill, setSkill] = React.useState("");
  const [status, setStatus] = React.useState<string | null>(null);
  const [isSaving, setIsSaving] = React.useState(false);

  function addSkill() {
    const nextSkill = skill.trim();

    if (!nextSkill || skills.includes(nextSkill)) {
      return;
    }

    setSkills((current) => [...current, nextSkill]);
    setSkill("");
  }

  async function saveRole(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    setIsSaving(true);
    setStatus(null);

    const formData = new FormData(event.currentTarget);
    const roleName = String(formData.get("roleName") || "").trim();
    const requiredEducation = String(formData.get("requiredEducation") || "").trim();
    const summary = String(formData.get("summary") || "");
    const pendingSkill = skill.trim();
    const nextSkills = [
      ...skills,
      ...(pendingSkill ? [pendingSkill] : []),
    ].filter((item, index, list) =>
      list.findIndex(
        (candidate) => candidate.toLowerCase() === item.toLowerCase(),
      ) === index,
    );
    const minExperience = parseExperience(
      String(formData.get("experience") || ""),
    );
    const weights = Object.fromEntries(
      weightFields.map(([, name, fallback]) => [
        name,
        parseWeight(String(formData.get(String(name)) || ""), Number(fallback)),
      ]),
    );
    const keywords = summary
      .split(/\s|,|\./)
      .map((item) => item.trim().toLowerCase())
      .filter((item) => item.length > 3)
      .slice(0, 12);

    if (!roleName || !requiredEducation || nextSkills.length === 0) {
      setStatus("Role name, education, and at least one skill are required.");
      setIsSaving(false);
      return;
    }

    try {
      const role = await createJobRole({
        roleName,
        requiredSkills: nextSkills,
        minExperience,
        requiredEducation,
        keywords,
        skillWeightage: weights.skillWeightage,
        experienceWeightage: weights.experienceWeightage,
        projectWeightage: weights.projectWeightage,
        educationWeightage: weights.educationWeightage,
        keywordWeightage: weights.keywordWeightage,
      });
      setStatus(`Saved role: ${role.roleName}`);
      form.reset();
      setSkills([]);
      setSkill("");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Unable to save role");
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <div className="grid gap-4 xl:grid-cols-[1.25fr_0.75fr]">
      <GlassPanel className="p-5 sm:p-6">
        <form onSubmit={saveRole}>
          <div className="grid gap-5 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="role-title">Job title</Label>
              <Input
                id="role-title"
                name="roleName"
                placeholder="Backend Engineer"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="education">Education</Label>
              <Input
                id="education"
                name="requiredEducation"
                placeholder="Computer Science"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="experience">Minimum experience</Label>
              <Input id="experience" name="experience" placeholder="3 years" />
            </div>
          </div>

          <div className="mt-6 space-y-2">
            <Label htmlFor="summary">Role summary</Label>
            <textarea
              id="summary"
              name="summary"
              className="min-h-32 w-full resize-none rounded-md border border-white/10 bg-white/5 px-3 py-3 text-sm leading-6 text-[#F5F5F5] outline-none transition-all duration-200 placeholder:text-[#A3A3A3]/70 focus:border-[#E85D04]/60 focus:bg-white/10 focus:ring-2 focus:ring-[#E85D04]/20"
              placeholder="Describe the work, stack, and domain keywords used by the backend scorer."
            />
          </div>

          <div className="mt-6">
            <Label>Custom scoring weights</Label>
            <div className="mt-3 grid gap-3 sm:grid-cols-5">
              {weightFields.map(([label, name, value]) => (
                <div key={String(name)} className="space-y-2">
                  <Label htmlFor={String(name)}>{label}</Label>
                  <Input
                    id={String(name)}
                    name={String(name)}
                    type="number"
                    min="0"
                    max="100"
                    defaultValue={Number(value)}
                  />
                </div>
              ))}
            </div>
          </div>

          <div className="mt-6 space-y-3">
            <Label htmlFor="skill">Required skills</Label>
            <div className="flex flex-col gap-3 sm:flex-row">
              <Input
                id="skill"
                value={skill}
                onChange={(event) => setSkill(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === "Enter") {
                    event.preventDefault();
                    addSkill();
                  }
                }}
                placeholder="Add skill"
              />
              <Button type="button" variant="secondary" onClick={addSkill}>
                <Plus className="size-4" />
                Add
              </Button>
            </div>
            <div className="flex flex-wrap gap-2">
              {skills.map((item) => (
                <button
                  key={item}
                  type="button"
                  onClick={() =>
                    setSkills((current) =>
                      current.filter((currentSkill) => currentSkill !== item),
                    )
                  }
                  className="rounded-md border border-white/10 bg-white/10 px-3 py-1.5 text-sm text-[#F5F5F5] transition-colors hover:border-[#E85D04]/50 hover:text-[#E85D04]"
                >
                  {item}
                </button>
              ))}
            </div>
          </div>

          <div className="mt-6 flex flex-col items-start justify-between gap-3 sm:flex-row sm:items-center">
            {status ? (
              <p className="text-sm text-[#A3A3A3]">{status}</p>
            ) : (
              <span />
            )}
            <Button type="submit" disabled={isSaving}>
              <Save className="size-4" />
              {isSaving ? "Saving" : "Save role"}
            </Button>
          </div>
        </form>
      </GlassPanel>

      <GlassPanel className="p-5 sm:p-6">
        <h2 className="text-lg font-semibold text-[#F5F5F5]">Backend scorer</h2>
        <p className="mt-2 text-sm leading-6 text-[#A3A3A3]">
          The shortlist decision is generated by the Spring Boot scoring engine.
        </p>

        <div className="mt-6 space-y-5">
          {scoreWeights.map(([label, value]) => (
            <div key={label.toString()} className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="text-[#F5F5F5]">{label}</span>
                <span className="text-[#A3A3A3]">{value}%</span>
              </div>
              <ProgressBar value={Number(value)} />
            </div>
          ))}
        </div>
      </GlassPanel>
    </div>
  );
}

function parseExperience(value: string) {
  const match = value.match(/\d+(\.\d+)?/);
  return match ? Number(match[0]) : 0;
}

function parseWeight(value: string, fallback: number) {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) {
    return fallback;
  }
  return Math.max(0, Math.min(100, Math.round(parsed)));
}
