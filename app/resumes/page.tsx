import Link from "next/link";
import {
  Database,
  Download,
  Eye,
  FileText,
  HardDrive,
  Upload,
} from "lucide-react";

import { AppShell } from "@/components/app/app-shell";
import { GlassPanel } from "@/components/app/glass-panel";
import { MotionPanel } from "@/components/app/motion-panel";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { apiBaseUrl, fetchResumes } from "@/lib/backend-api";

export default async function UploadedResumesPage() {
  const resumesResult = await fetchResumes();
  const resumes = resumesResult.data;
  const resumesWithFiles = resumes.filter((resume) => resume.fileName);
  const totalStoredBytes = resumes.reduce(
    (total, resume) => total + (resume.fileSize ?? 0),
    0,
  );
  const metrics = [
    { label: "Saved resumes", value: resumes.length, icon: Database },
    { label: "Stored files", value: resumesWithFiles.length, icon: FileText },
    {
      label: "Database storage",
      value: formatBytes(totalStoredBytes),
      icon: HardDrive,
    },
  ];

  return (
    <AppShell active="Uploaded Resumes">
      <div className="space-y-8">
        <PageHeader
          eyebrow="Resume database"
          title="Uploaded resumes"
          description="View every resume saved in the database, including the original PDF, Word, or text file uploaded by the candidate."
          action={
            <Button asChild>
              <Link href="/resumes/upload">
                <Upload className="size-4" />
                Upload resume
              </Link>
            </Button>
          }
        />

        {!resumesResult.ok ? (
          <GlassPanel className="p-4 text-sm leading-6 text-[#A3A3A3]">
            Backend connection issue. Start the Spring Boot API to load uploaded
            resumes from the database.
          </GlassPanel>
        ) : null}

        <section className="grid gap-4 md:grid-cols-3">
          {metrics.map((metric, index) => {
            const Icon = metric.icon;

            return (
              <MotionPanel key={metric.label} delay={index * 0.05}>
                <GlassPanel className="p-5">
                  <div className="flex items-center justify-between gap-4">
                    <p className="text-sm text-[#A3A3A3]">{metric.label}</p>
                    <Icon className="size-4 text-[#E85D04]" />
                  </div>
                  <p className="mt-3 text-3xl font-semibold text-[#F5F5F5]">
                    {metric.value}
                  </p>
                </GlassPanel>
              </MotionPanel>
            );
          })}
        </section>

        <MotionPanel delay={0.08}>
          <GlassPanel className="overflow-hidden">
            <div className="grid grid-cols-[1fr_150px_150px] gap-4 border-b border-white/10 px-5 py-4 text-sm text-[#A3A3A3] max-lg:hidden">
              <span>Candidate</span>
              <span>Stored file</span>
              <span className="text-right">Actions</span>
            </div>

            <div className="divide-y divide-white/10">
              {resumes.map((resume) => {
                const fileUrl = `${apiBaseUrl()}/api/resumes/${resume.id}/file`;
                const downloadUrl = `${fileUrl}?download=true`;

                return (
                  <div
                    key={resume.id}
                    className="grid gap-4 px-5 py-5 lg:grid-cols-[1fr_150px_150px] lg:items-center"
                  >
                    <div className="min-w-0">
                      <div className="flex items-center gap-3">
                        <span className="flex size-10 shrink-0 items-center justify-center rounded-md bg-white/10 text-[#E85D04]">
                          <FileText className="size-4" />
                        </span>
                        <span className="min-w-0">
                          <p className="truncate font-medium text-[#F5F5F5]">
                            {resume.candidateName || "Unnamed candidate"}
                          </p>
                          <p className="mt-1 truncate text-sm text-[#A3A3A3]">
                            {resume.email || "No email"} -{" "}
                            {resume.appliedRole || "No role selected"}
                          </p>
                        </span>
                      </div>
                      <div className="mt-3 flex flex-wrap gap-2">
                        {resume.skills.slice(0, 5).map((skill) => (
                          <span
                            key={skill}
                            className="rounded-md bg-white/10 px-2.5 py-1 text-xs text-[#A3A3A3]"
                          >
                            {skill}
                          </span>
                        ))}
                      </div>
                    </div>

                    <div className="text-sm text-[#A3A3A3]">
                      <p className="truncate text-[#F5F5F5]">
                        {resume.fileName || "Form entry"}
                      </p>
                      <p className="mt-1">{formatBytes(resume.fileSize)}</p>
                      <p className="mt-1">{formatDate(resume.createdAt)}</p>
                    </div>

                    <div className="flex gap-2 lg:justify-end">
                      {resume.fileName ? (
                        <>
                          <Button asChild variant="outline" size="sm">
                            <a href={fileUrl} target="_blank" rel="noreferrer">
                              <Eye className="size-4" />
                              View
                            </a>
                          </Button>
                          <Button asChild variant="ghost" size="sm">
                            <a href={downloadUrl}>
                              <Download className="size-4" />
                              Download
                            </a>
                          </Button>
                        </>
                      ) : (
                        <span className="text-sm text-[#A3A3A3]">
                          No file saved
                        </span>
                      )}
                    </div>
                  </div>
                );
              })}

              {resumes.length === 0 ? (
                <div className="p-6 text-sm leading-6 text-[#A3A3A3]">
                  No resumes found in the database yet. Upload PDF, Word, or
                  text resumes to see them here.
                </div>
              ) : null}
            </div>
          </GlassPanel>
        </MotionPanel>
      </div>
    </AppShell>
  );
}

function formatBytes(value?: number | null) {
  if (!value || value <= 0) {
    return "0 KB";
  }
  const units = ["B", "KB", "MB", "GB"];
  let size = value;
  let unitIndex = 0;
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024;
    unitIndex += 1;
  }
  return `${size.toFixed(size >= 10 || unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
}

function formatDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "Stored";
  }
  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(date);
}
