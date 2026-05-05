"use client";

import * as React from "react";
import Link from "next/link";
import { ArrowRight, Play } from "lucide-react";

import { GlassPanel } from "@/components/app/glass-panel";
import { ProgressBar } from "@/components/app/progress-bar";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  type JobRole,
  type ResumeRecord,
  type ScreeningReport,
  screenResumes,
} from "@/lib/backend-api";

type ScreeningWorkbenchProps = {
  roles: JobRole[];
  resumes: ResumeRecord[];
};

export function ScreeningWorkbench({ roles, resumes }: ScreeningWorkbenchProps) {
  const [selectedRoleId, setSelectedRoleId] = React.useState(
    roles[0]?.id.toString() ?? "",
  );
  const [selectedResumeIds, setSelectedResumeIds] = React.useState<number[]>(
    resumes.map((resume) => resume.id),
  );
  const [reports, setReports] = React.useState<ScreeningReport[]>([]);
  const [status, setStatus] = React.useState<string | null>(null);
  const [isScreening, setIsScreening] = React.useState(false);

  const selectedRole = roles.find(
    (role) => role.id.toString() === selectedRoleId,
  );
  const shortlistedCount = reports.filter(
    (report) => report.status === "SHORTLISTED",
  ).length;

  function toggleResume(resumeId: number, checked: boolean) {
    setSelectedResumeIds((current) =>
      checked
        ? [...new Set([...current, resumeId])]
        : current.filter((id) => id !== resumeId),
    );
  }

  async function runScreening(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setStatus(null);
    setReports([]);

    if (!selectedRole) {
      setStatus("Create and select a job role first.");
      return;
    }

    if (selectedResumeIds.length === 0) {
      setStatus("Select at least one resume to screen.");
      return;
    }

    setIsScreening(true);
    try {
      const nextReports = await screenResumes(selectedRole.id, selectedResumeIds);
      setReports(nextReports);
      setStatus(
        `Screened ${nextReports.length} candidate${nextReports.length === 1 ? "" : "s"} and shortlisted ${nextReports.filter((report) => report.status === "SHORTLISTED").length}.`,
      );
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Screening failed");
    } finally {
      setIsScreening(false);
    }
  }

  return (
    <GlassPanel className="p-5 sm:p-6">
      <form onSubmit={runScreening}>
        <div className="grid gap-5 lg:grid-cols-[320px_1fr]">
          <div className="space-y-4">
            <div className="space-y-2">
              <label
                htmlFor="screen-role"
                className="text-sm font-medium text-[#F5F5F5]"
              >
                Job role
              </label>
              <select
                id="screen-role"
                value={selectedRoleId}
                onChange={(event) => setSelectedRoleId(event.target.value)}
                className="h-10 w-full rounded-md border border-white/10 bg-white/5 px-3 text-sm text-[#F5F5F5] outline-none transition-all duration-200 focus:border-[#E85D04]/60 focus:bg-white/10 focus:ring-2 focus:ring-[#E85D04]/20"
              >
                {roles.length === 0 ? (
                  <option value="">No roles available</option>
                ) : null}
                {roles.map((role) => (
                  <option
                    key={role.id}
                    value={role.id}
                    className="bg-[#111111]"
                  >
                    {role.roleName}
                  </option>
                ))}
              </select>
            </div>

            <div className="rounded-lg border border-white/10 bg-white/5 p-4 text-sm leading-6 text-[#A3A3A3]">
              A backend score of 80 or higher automatically writes the candidate
              to the shortlisted table.
            </div>

            <div className="flex flex-col gap-3">
              <Button
                type="submit"
                disabled={
                  isScreening || roles.length === 0 || resumes.length === 0
                }
              >
                <Play className="size-4" />
                {isScreening ? "Screening" : "Run screening"}
              </Button>
              <Button asChild variant="outline">
                <Link href="/shortlist">
                  View shortlist
                  <ArrowRight className="size-4" />
                </Link>
              </Button>
            </div>

            {status ? (
              <p className="text-sm leading-6 text-[#A3A3A3]">{status}</p>
            ) : null}
          </div>

          <div>
            <div className="flex items-center justify-between gap-4">
              <h2 className="text-lg font-semibold text-[#F5F5F5]">
                Candidate resumes
              </h2>
              <span className="text-sm text-[#A3A3A3]">
                {selectedResumeIds.length} selected
              </span>
            </div>

            <div className="mt-4 divide-y divide-white/10 overflow-hidden rounded-lg border border-white/10">
              {resumes.map((resume) => (
                <label
                  key={resume.id}
                  className="grid cursor-pointer gap-3 bg-white/5 p-4 transition-colors hover:bg-white/10 sm:grid-cols-[auto_1fr_auto] sm:items-center"
                >
                  <Checkbox
                    checked={selectedResumeIds.includes(resume.id)}
                    onCheckedChange={(value) =>
                      toggleResume(resume.id, value === true)
                    }
                  />
                  <span className="min-w-0">
                    <span className="block truncate font-medium text-[#F5F5F5]">
                      {resume.candidateName}
                    </span>
                    <span className="mt-1 block truncate text-sm text-[#A3A3A3]">
                      {resume.email} - {resume.appliedRole}
                    </span>
                  </span>
                  <span className="text-sm text-[#A3A3A3]">
                    {resume.experienceYears} yrs
                  </span>
                </label>
              ))}

              {resumes.length === 0 ? (
                <div className="bg-white/5 p-4 text-sm leading-6 text-[#A3A3A3]">
                  No resumes found. Upload resumes before running screening.
                </div>
              ) : null}
            </div>
          </div>
        </div>
      </form>

      {reports.length > 0 ? (
        <div className="mt-6 border-t border-white/10 pt-6">
          <div className="flex items-center justify-between gap-4">
            <h2 className="text-lg font-semibold text-[#F5F5F5]">
              Latest screening run
            </h2>
            <span className="rounded-md border border-[#E85D04]/30 bg-[#E85D04]/10 px-3 py-1 text-sm text-[#E85D04]">
              {shortlistedCount} shortlisted
            </span>
          </div>
          <div className="mt-4 grid gap-4 md:grid-cols-2">
            {reports.map((report) => (
              <div
                key={report.reportId}
                className="rounded-lg border border-white/10 bg-white/5 p-4"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="font-medium text-[#F5F5F5]">
                      {report.candidateName}
                    </p>
                    <p className="mt-1 text-sm text-[#A3A3A3]">
                      {report.roleName}
                    </p>
                  </div>
                  <span className="text-sm font-semibold text-[#E85D04]">
                    {report.score}
                  </span>
                </div>
                <ProgressBar value={report.score} className="mt-4" />
                <p className="mt-3 text-sm text-[#A3A3A3]">{report.status}</p>
              </div>
            ))}
          </div>
        </div>
      ) : null}
    </GlassPanel>
  );
}
