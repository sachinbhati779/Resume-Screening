"use client";

import * as React from "react";
import Link from "next/link";
import { ArrowRight, FileText, UploadCloud } from "lucide-react";

import { GlassPanel } from "@/components/app/glass-panel";
import { ProgressBar } from "@/components/app/progress-bar";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  type JobRole,
  type ResumeRecord,
  type ScreeningReport,
  screenResumes,
  uploadResumeFiles,
} from "@/lib/backend-api";

type ResumeUploadPanelProps = {
  roles: JobRole[];
  backendMessage?: string;
};

export function ResumeUploadPanel({
  roles,
  backendMessage,
}: ResumeUploadPanelProps) {
  const [files, setFiles] = React.useState<File[]>([]);
  const [screenImmediately, setScreenImmediately] = React.useState(true);
  const [selectedRoleId, setSelectedRoleId] = React.useState(
    roles[0]?.id.toString() ?? "",
  );
  const [uploadedResumes, setUploadedResumes] = React.useState<ResumeRecord[]>(
    [],
  );
  const [screeningReports, setScreeningReports] = React.useState<
    ScreeningReport[]
  >([]);
  const [warnings, setWarnings] = React.useState<string[]>([]);
  const [status, setStatus] = React.useState<string | null>(
    backendMessage ?? null,
  );
  const [isUploading, setIsUploading] = React.useState(false);

  const selectedRole = roles.find(
    (role) => role.id.toString() === selectedRoleId,
  );
  const shortlistedCount = screeningReports.filter(
    (report) => report.status === "SHORTLISTED",
  ).length;
  const invalidSelectedFiles = files.filter((file) => !isAllowedResumeFile(file));

  async function uploadFiles(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setStatus(null);
    setWarnings([]);
    setUploadedResumes([]);
    setScreeningReports([]);

    if (roles.length === 0) {
      setStatus("Create a job role before uploading resumes.");
      return;
    }

    if (!selectedRole) {
      setStatus("Choose the role these resumes should be screened against.");
      return;
    }

    if (files.length === 0) {
      setStatus("Select at least one resume file.");
      return;
    }

    setIsUploading(true);
    try {
      const upload = await uploadResumeFiles(files, selectedRole.roleName);
      setUploadedResumes(upload.resumes);
      setWarnings(upload.warnings);

      if (upload.resumes.length === 0) {
        setStatus("No resumes were accepted. Check the warnings below.");
        return;
      }

      if (screenImmediately) {
        const reports = await screenResumes(
          selectedRole.id,
          upload.resumes.map((resume) => resume.id),
        );
        setScreeningReports(reports);
        setStatus(
          `Processed ${upload.resumes.length} resume${upload.resumes.length === 1 ? "" : "s"} and shortlisted ${reports.filter((report) => report.status === "SHORTLISTED").length}.`,
        );
        return;
      }

      setStatus(`Uploaded ${upload.resumes.length} resume record${upload.resumes.length === 1 ? "" : "s"}.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Unable to upload resumes");
    } finally {
      setIsUploading(false);
    }
  }

  return (
    <div className="grid gap-4 xl:grid-cols-[1fr_0.85fr]">
      <GlassPanel className="p-5 sm:p-6">
        <form onSubmit={uploadFiles}>
          <label
            htmlFor="resume-upload"
            className="flex min-h-72 cursor-pointer flex-col items-center justify-center rounded-lg border border-dashed border-white/15 bg-white/5 px-5 py-10 text-center transition-all duration-200 hover:border-[#E85D04]/55 hover:bg-white/10"
          >
            <span className="flex size-14 items-center justify-center rounded-md bg-[#E85D04] text-[#0A0A0A] shadow-[0_0_28px_rgba(232,93,4,0.24)]">
              <UploadCloud className="size-6" />
            </span>
            <span className="mt-5 block text-lg font-semibold text-[#F5F5F5]">
              Upload resumes
            </span>
            <span className="mt-2 block max-w-sm text-sm leading-6 text-[#A3A3A3]">
              Upload PDF, Word, or text resumes with name, email, skills,
              education, summary, projects, experience, and role fields.
            </span>
            <input
              id="resume-upload"
              type="file"
              multiple
              accept=".pdf,.doc,.docx,.txt,.text"
              className="sr-only"
              onChange={(event) => {
                setFiles(Array.from(event.target.files ?? []));
              }}
            />
          </label>

          <div className="mt-5 space-y-2">
            <label
              htmlFor="role"
              className="text-sm font-medium text-[#F5F5F5]"
            >
              Screen against role
            </label>
            <select
              id="role"
              value={selectedRoleId}
              onChange={(event) => setSelectedRoleId(event.target.value)}
              className="h-10 w-full rounded-md border border-white/10 bg-white/5 px-3 text-sm text-[#F5F5F5] outline-none transition-all duration-200 focus:border-[#E85D04]/60 focus:bg-white/10 focus:ring-2 focus:ring-[#E85D04]/20"
            >
              {roles.length === 0 ? (
                <option value="">No job roles found</option>
              ) : null}
              {roles.map((role) => (
                <option key={role.id} value={role.id} className="bg-[#111111]">
                  {role.roleName}
                </option>
              ))}
            </select>
          </div>

          <label className="mt-5 flex items-center gap-3 rounded-md border border-white/10 bg-white/5 p-3 text-sm text-[#A3A3A3]">
            <Checkbox
              checked={screenImmediately}
              onCheckedChange={(value) => setScreenImmediately(value === true)}
            />
            Screen and shortlist after upload
          </label>

          {files.length > 0 ? (
            <div className="mt-5 rounded-lg border border-white/10 bg-white/5 p-4">
              <p className="text-sm font-medium text-[#F5F5F5]">
                Selected files
              </p>
              <div className="mt-3 space-y-2">
                {files.map((file) => (
                  <div
                    key={`${file.name}-${file.size}`}
                    className="flex items-center justify-between gap-3 text-sm text-[#A3A3A3]"
                  >
                    <span className="flex min-w-0 items-center gap-3">
                      <FileText className="size-4 shrink-0 text-[#E85D04]" />
                      <span className="min-w-0 truncate">{file.name}</span>
                    </span>
                    <span className="shrink-0 text-xs">
                      {isAllowedResumeFile(file) ? "Allowed" : "Will warn"}
                    </span>
                  </div>
                ))}
              </div>
              {invalidSelectedFiles.length > 0 ? (
                <p className="mt-3 text-xs leading-5 text-[#A3A3A3]">
                  Invalid files will be skipped by the backend and returned as
                  warnings; valid PDF, Word, and text resumes will still be
                  saved.
                </p>
              ) : null}
            </div>
          ) : null}

          <div className="mt-6 flex flex-col items-start justify-between gap-3 sm:flex-row sm:items-center">
            {status ? (
              <p className="text-sm text-[#A3A3A3]">{status}</p>
            ) : (
              <span />
            )}
            <Button type="submit" disabled={isUploading || roles.length === 0}>
              {isUploading ? "Processing" : "Upload and process"}
            </Button>
          </div>
        </form>
      </GlassPanel>

      <GlassPanel className="p-5 sm:p-6">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="text-lg font-semibold text-[#F5F5F5]">
              Processing result
            </h2>
            <p className="mt-2 text-sm leading-6 text-[#A3A3A3]">
              Shortlisted candidates are stored only when backend screening
              returns a score of 80 or higher.
            </p>
          </div>
          <div className="flex gap-2">
            <Button asChild variant="ghost" size="sm">
              <Link href="/resumes">Uploaded</Link>
            </Button>
            <Button asChild variant="ghost" size="sm">
              <Link href="/shortlist">
                Shortlist
                <ArrowRight className="size-4" />
              </Link>
            </Button>
          </div>
        </div>

        {screeningReports.length > 0 ? (
          <div className="mt-6 rounded-lg border border-[#E85D04]/30 bg-[#E85D04]/10 p-4">
            <p className="text-sm text-[#E85D04]">
              {shortlistedCount} of {screeningReports.length} candidates met the
              shortlist threshold.
            </p>
          </div>
        ) : null}

        <div className="mt-6 space-y-5">
          {screeningReports.map((report) => (
            <div key={report.reportId} className="space-y-2">
              <div className="flex items-center justify-between gap-3 text-sm">
                <span className="min-w-0 truncate text-[#F5F5F5]">
                  {report.candidateName}
                </span>
                <span className="text-[#A3A3A3]">{report.score}</span>
              </div>
              <ProgressBar value={report.score} />
              <p className="text-xs text-[#A3A3A3]">{report.status}</p>
              {report.missingKeywords.length > 0 ? (
                <p className="text-xs leading-5 text-[#A3A3A3]">
                  Missing: {report.missingKeywords.slice(0, 4).join(", ")}
                </p>
              ) : null}
            </div>
          ))}

          {screeningReports.length === 0 && uploadedResumes.length > 0 ? (
            <div className="space-y-3">
              {uploadedResumes.map((resume) => (
                <div
                  key={resume.id}
                  className="rounded-md border border-white/10 bg-white/5 p-3"
                >
                  <p className="text-sm font-medium text-[#F5F5F5]">
                    {resume.candidateName}
                  </p>
                  <p className="mt-1 text-xs text-[#A3A3A3]">{resume.email}</p>
                  {resume.fileName ? (
                    <p className="mt-1 text-xs text-[#A3A3A3]">
                      Saved file: {resume.fileName} ({formatBytes(resume.fileSize)})
                    </p>
                  ) : null}
                </div>
              ))}
              <Button asChild variant="outline" size="sm">
                <Link href="/resumes">
                  View uploaded resumes
                  <ArrowRight className="size-4" />
                </Link>
              </Button>
            </div>
          ) : null}

          {screeningReports.length === 0 && uploadedResumes.length === 0 ? (
            <div className="rounded-lg border border-white/10 bg-white/5 p-4 text-sm leading-6 text-[#A3A3A3]">
              No resumes processed yet.
            </div>
          ) : null}
        </div>

        {warnings.length > 0 ? (
          <div className="mt-6 rounded-lg border border-white/10 bg-white/5 p-4">
            <p className="text-sm font-medium text-[#F5F5F5]">Warnings</p>
            <ul className="mt-3 space-y-2 text-sm text-[#A3A3A3]">
              {warnings.map((warning) => (
                <li key={warning}>{warning}</li>
              ))}
            </ul>
          </div>
        ) : null}
      </GlassPanel>
    </div>
  );
}

function formatBytes(value?: number | null) {
  if (!value || value <= 0) {
    return "0 KB";
  }
  const units = ["B", "KB", "MB"];
  let size = value;
  let unitIndex = 0;
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024;
    unitIndex += 1;
  }
  return `${size.toFixed(size >= 10 || unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
}

function isAllowedResumeFile(file: File) {
  const extension = file.name.split(".").pop()?.toLowerCase() ?? "";
  return ["pdf", "doc", "docx", "txt", "text"].includes(extension);
}
