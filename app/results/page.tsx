import Link from "next/link";
import { ArrowRight, FileSearch, Upload } from "lucide-react";

import { AppShell } from "@/components/app/app-shell";
import { GlassPanel } from "@/components/app/glass-panel";
import { MotionPanel } from "@/components/app/motion-panel";
import { PageHeader } from "@/components/app/page-header";
import { ProgressBar } from "@/components/app/progress-bar";
import { ScreeningWorkbench } from "@/components/app/screening-workbench";
import { Button } from "@/components/ui/button";
import {
  fetchJobRoles,
  fetchResumes,
  fetchScreeningReports,
} from "@/lib/backend-api";

export default async function ScreeningResultsPage() {
  const [rolesResult, resumesResult, reportsResult] = await Promise.all([
    fetchJobRoles(),
    fetchResumes(),
    fetchScreeningReports(),
  ]);
  const reports = reportsResult.data;
  const shortlistedCount = reports.filter(
    (report) => report.status === "SHORTLISTED",
  ).length;

  return (
    <AppShell active="Screening Results">
      <div className="space-y-8">
        <PageHeader
          eyebrow="Screening"
          title="Screening results"
          description="Run the backend scorer against real resume records. Candidates are shortlisted only after scoring 80 or higher."
          action={
            <Button asChild>
              <Link href="/resumes/upload">
                <Upload className="size-4" />
                Upload resumes
              </Link>
            </Button>
          }
        />

        {!rolesResult.ok || !resumesResult.ok || !reportsResult.ok ? (
          <GlassPanel className="p-4 text-sm leading-6 text-[#A3A3A3]">
            Backend connection issue. Start the Spring Boot API on
            `http://localhost:8080` to run the real shortlist process.
          </GlassPanel>
        ) : null}

        <MotionPanel>
          <ScreeningWorkbench
            roles={rolesResult.data}
            resumes={resumesResult.data}
          />
        </MotionPanel>

        <section className="grid gap-4 md:grid-cols-3">
          {[
            ["Screened reports", reports.length],
            ["Shortlisted", shortlistedCount],
            ["Rejected", reports.filter((report) => report.status === "REJECTED").length],
          ].map(([label, value]) => (
            <MotionPanel key={label.toString()}>
              <GlassPanel className="p-5">
                <p className="text-sm text-[#A3A3A3]">{label}</p>
                <p className="mt-2 text-3xl font-semibold text-[#F5F5F5]">
                  {value}
                </p>
              </GlassPanel>
            </MotionPanel>
          ))}
        </section>

        <MotionPanel delay={0.08}>
          <GlassPanel className="overflow-hidden">
            <div className="flex items-center justify-between gap-4 border-b border-white/10 p-5">
              <div className="flex items-center gap-3">
                <FileSearch className="size-5 text-[#E85D04]" />
                <h2 className="text-lg font-semibold text-[#F5F5F5]">
                  Stored screening reports
                </h2>
              </div>
              <Button asChild variant="ghost">
                <Link href="/ranking">
                  Ranking
                  <ArrowRight className="size-4" />
                </Link>
              </Button>
            </div>

            <div className="divide-y divide-white/10">
              {reports.map((report) => (
                <div
                  key={report.reportId}
                  className="grid gap-4 p-5 md:grid-cols-[1fr_120px_120px] md:items-center"
                >
                <div>
                  <p className="font-medium text-[#F5F5F5]">
                    {report.candidateName}
                  </p>
                  <p className="mt-1 text-sm text-[#A3A3A3]">
                    {report.roleName} - {report.remarks}
                  </p>
                  <div className="mt-3 flex flex-wrap gap-2">
                    {report.matchedKeywords.slice(0, 6).map((keyword) => (
                      <span
                        key={keyword}
                        className="rounded-md bg-[#E85D04]/12 px-2.5 py-1 text-xs text-[#E85D04]"
                      >
                        {keyword}
                      </span>
                    ))}
                    {report.missingKeywords.slice(0, 4).map((keyword) => (
                      <span
                        key={keyword}
                        className="rounded-md bg-white/10 px-2.5 py-1 text-xs text-[#A3A3A3]"
                      >
                        Missing {keyword}
                      </span>
                    ))}
                  </div>
                  <p className="mt-3 text-xs leading-5 text-[#A3A3A3]">
                    {report.explanation}
                  </p>
                </div>
                  <span className="text-sm text-[#A3A3A3]">
                    {report.status}
                  </span>
                  <div className="md:text-right">
                    <p className="font-semibold text-[#E85D04]">
                      {report.score}
                    </p>
                    <ProgressBar value={report.score} className="mt-2" />
                  </div>
                </div>
              ))}

              {reports.length === 0 ? (
                <div className="p-5 text-sm leading-6 text-[#A3A3A3]">
                  No screening reports yet. Upload resumes, select a role, and
                  run screening to create shortlist decisions.
                </div>
              ) : null}
            </div>
          </GlassPanel>
        </MotionPanel>
      </div>
    </AppShell>
  );
}
