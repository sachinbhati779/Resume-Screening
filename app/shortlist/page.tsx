import Link from "next/link";
import { CalendarClock, Download, Mail, Upload, UserCheck } from "lucide-react";

import { AppShell } from "@/components/app/app-shell";
import { GlassPanel } from "@/components/app/glass-panel";
import { MotionPanel } from "@/components/app/motion-panel";
import { PageHeader } from "@/components/app/page-header";
import { ProgressBar } from "@/components/app/progress-bar";
import { Button } from "@/components/ui/button";
import { apiBaseUrl, fetchShortlistedCandidates } from "@/lib/backend-api";

export default async function ShortlistedCandidatesPage() {
  const shortlistResult = await fetchShortlistedCandidates();
  const shortlist = shortlistResult.data;

  return (
    <AppShell active="Shortlisted">
      <div className="space-y-8">
        <PageHeader
          eyebrow="Shortlist"
          title="Shortlisted candidates"
          description="Candidates appear here only after real backend screening stores a SHORTLISTED decision."
          action={
            <Button asChild variant="outline">
              <a href={`${apiBaseUrl()}/api/shortlisted/export`}>
                <Download className="size-4" />
                Export
              </a>
            </Button>
          }
        />

        {!shortlistResult.ok ? (
          <GlassPanel className="p-4 text-sm leading-6 text-[#A3A3A3]">
            Backend connection issue. Start the Spring Boot API to load the real
            shortlist.
          </GlassPanel>
        ) : null}

        {shortlist.length === 0 ? (
          <GlassPanel className="p-6 text-center">
            <div className="mx-auto flex size-12 items-center justify-center rounded-md bg-white/10 text-[#E85D04]">
              <UserCheck className="size-5" />
            </div>
            <h2 className="mt-5 text-xl font-semibold text-[#F5F5F5]">
              No shortlisted candidates yet
            </h2>
            <p className="mx-auto mt-2 max-w-xl text-sm leading-6 text-[#A3A3A3]">
              Upload resumes and run screening. Any candidate scoring 80 or
              higher will be saved here automatically.
            </p>
            <div className="mt-6">
              <Button asChild>
                <Link href="/resumes/upload">
                  <Upload className="size-4" />
                  Upload resumes
                </Link>
              </Button>
            </div>
          </GlassPanel>
        ) : null}

        <section className="grid gap-4 lg:grid-cols-3">
          {shortlist.map((candidate, index) => (
            <MotionPanel key={candidate.id} delay={index * 0.06}>
              <GlassPanel className="p-5 transition-all duration-200 hover:-translate-y-1 hover:border-[#E85D04]/35">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-lg font-semibold text-[#F5F5F5]">
                      {candidate.candidateName}
                    </p>
                    <p className="mt-1 text-sm text-[#A3A3A3]">
                      {candidate.roleName}
                    </p>
                  </div>
                  <span className="flex size-10 items-center justify-center rounded-md bg-[#E85D04] text-[#0A0A0A]">
                    <UserCheck className="size-5" />
                  </span>
                </div>

                <div className="mt-6 space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-[#A3A3A3]">Screening score</span>
                    <span className="font-semibold text-[#E85D04]">
                      {candidate.score}
                    </span>
                  </div>
                  <ProgressBar value={candidate.score} />
                </div>

                <div className="mt-6 grid gap-3 text-sm">
                  <div className="flex items-center justify-between gap-3">
                    <span className="text-[#A3A3A3]">Email</span>
                    <span className="truncate text-right text-[#F5F5F5]">
                      {candidate.email}
                    </span>
                  </div>
                  <div className="flex items-center justify-between gap-3">
                    <span className="text-[#A3A3A3]">Shortlisted</span>
                    <span className="text-right text-[#F5F5F5]">
                      {formatDate(candidate.createdAt)}
                    </span>
                  </div>
                </div>

                <div className="mt-6 flex gap-3">
                  <Button asChild variant="outline" size="sm" className="flex-1">
                    <a href={`mailto:${candidate.email}`}>
                      <Mail className="size-4" />
                      Email
                    </a>
                  </Button>
                  <Button asChild size="sm" className="flex-1">
                    <Link href="/interview">
                      <CalendarClock className="size-4" />
                      Interview
                    </Link>
                  </Button>
                </div>
              </GlassPanel>
            </MotionPanel>
          ))}
        </section>

        {shortlist.length > 0 ? (
          <MotionPanel delay={0.14}>
            <GlassPanel className="overflow-hidden">
              <div className="grid grid-cols-[1fr_140px_140px] gap-4 border-b border-white/10 px-5 py-4 text-sm text-[#A3A3A3] max-md:hidden">
                <span>Candidate</span>
                <span>Score</span>
                <span className="text-right">Next action</span>
              </div>
              <div className="divide-y divide-white/10">
                {shortlist.map((candidate) => (
                  <div
                    key={`${candidate.id}-row`}
                    className="grid gap-3 px-5 py-4 md:grid-cols-[1fr_140px_140px] md:items-center"
                  >
                    <div>
                      <p className="font-medium text-[#F5F5F5]">
                        {candidate.candidateName}
                      </p>
                      <p className="mt-1 text-sm text-[#A3A3A3]">
                        {candidate.roleName} - {candidate.email}
                      </p>
                    </div>
                    <span className="text-sm text-[#A3A3A3]">
                      {candidate.score}
                    </span>
                    <Link
                      href="/interview"
                      className="text-sm font-medium text-[#E85D04] md:text-right"
                    >
                      Start interview
                    </Link>
                  </div>
                ))}
              </div>
            </GlassPanel>
          </MotionPanel>
        ) : null}
      </div>
    </AppShell>
  );
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
