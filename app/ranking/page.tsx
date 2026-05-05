import Link from "next/link";
import { Bot, SlidersHorizontal, Upload } from "lucide-react";

import { AppShell } from "@/components/app/app-shell";
import { GlassPanel } from "@/components/app/glass-panel";
import { HiringDecisionActions } from "@/components/app/hiring-decision-actions";
import { MotionPanel } from "@/components/app/motion-panel";
import { PageHeader } from "@/components/app/page-header";
import { ProgressBar } from "@/components/app/progress-bar";
import { Button } from "@/components/ui/button";
import { fetchCandidateRanking, fetchJobRoles } from "@/lib/backend-api";

export default async function CandidateRankingPage() {
  const [rankingResult, rolesResult] = await Promise.all([
    fetchCandidateRanking(),
    fetchJobRoles(),
  ]);
  const candidates = rankingResult.data;
  const role = rolesResult.data[0];
  const weights = role
    ? [
        ["Skills", role.skillWeightage ?? 40],
        ["Experience", role.experienceWeightage ?? 25],
        ["Projects", role.projectWeightage ?? 15],
        ["Education", role.educationWeightage ?? 10],
        ["Keywords", role.keywordWeightage ?? 10],
      ]
    : [];

  return (
    <AppShell active="Candidate Ranking">
      <div className="space-y-8">
        <PageHeader
          eyebrow="Ranking"
          title="Candidate ranking"
          description="Compare real screening reports sorted by score. Shortlisted candidates are produced by the backend screening threshold."
          action={
            <Button asChild>
              <Link href="/interview">
                <Bot className="size-4" />
                Start interview
              </Link>
            </Button>
          }
        />

        {!rankingResult.ok ? (
          <GlassPanel className="p-4 text-sm leading-6 text-[#A3A3A3]">
            Backend connection issue. Start the Spring Boot API to load real
            ranking data.
          </GlassPanel>
        ) : null}

        <section className="grid gap-4 xl:grid-cols-[1fr_320px]">
          <MotionPanel>
            <GlassPanel className="overflow-hidden">
              <div className="grid grid-cols-[48px_1fr_92px] gap-4 border-b border-white/10 px-5 py-4 text-sm text-[#A3A3A3] sm:grid-cols-[64px_1.2fr_0.8fr_140px]">
                <span>Rank</span>
                <span>Candidate</span>
                <span className="hidden sm:block">Decision</span>
                <span className="text-right">Final score</span>
              </div>

              <div className="divide-y divide-white/10">
                {candidates.map((candidate, index) => (
                  <div
                    key={candidate.reportId}
                    className="grid grid-cols-[48px_1fr_92px] gap-4 px-5 py-5 transition-colors hover:bg-white/5 sm:grid-cols-[64px_1.2fr_0.8fr_140px] sm:items-center"
                  >
                    <span className="flex size-9 items-center justify-center rounded-md bg-white/10 text-sm font-semibold text-[#E85D04]">
                      {index + 1}
                    </span>
                    <div className="min-w-0">
                      <p className="truncate font-medium text-[#F5F5F5]">
                        {candidate.candidateName}
                      </p>
                      <p className="mt-1 truncate text-sm text-[#A3A3A3]">
                        {candidate.roleName} - {candidate.email}
                      </p>
                      <div className="mt-3 flex flex-wrap gap-2">
                        {candidate.skills.map((skill) => (
                          <span
                            key={skill}
                            className="rounded-md bg-white/10 px-2.5 py-1 text-xs text-[#A3A3A3]"
                          >
                            {skill}
                          </span>
                        ))}
                      </div>
                      <HiringDecisionActions
                        resumeId={candidate.resumeId}
                        reportId={candidate.reportId}
                        initialDecision={candidate.hiringDecision}
                      />
                    </div>
                    <span className="hidden text-sm text-[#A3A3A3] sm:block">
                      {candidate.hiringDecision}
                      <span className="mt-1 block text-xs">
                        ATS {candidate.atsScore}
                        {candidate.interviewScore === null
                          ? " - interview pending"
                          : ` - interview ${candidate.interviewScore}`}
                      </span>
                    </span>
                    <div className="text-right">
                      <p className="text-lg font-semibold text-[#F5F5F5]">
                        {candidate.finalScore}
                      </p>
                      <ProgressBar value={candidate.finalScore} className="mt-2" />
                    </div>
                  </div>
                ))}

                {candidates.length === 0 ? (
                  <div className="p-5 text-sm leading-6 text-[#A3A3A3]">
                    No ranked candidates yet. Upload resumes and run screening
                    to populate this list.
                    <div className="mt-4">
                      <Button asChild variant="outline">
                        <Link href="/resumes/upload">
                          <Upload className="size-4" />
                          Upload resumes
                        </Link>
                      </Button>
                    </div>
                  </div>
                ) : null}
              </div>
            </GlassPanel>
          </MotionPanel>

          <MotionPanel delay={0.08}>
            <GlassPanel className="p-5">
              <div className="flex items-center gap-3">
                <SlidersHorizontal className="size-5 text-[#E85D04]" />
                <h2 className="text-lg font-semibold text-[#F5F5F5]">
                  Role scorecard
                </h2>
              </div>
              {role ? (
                <>
                  <p className="mt-2 text-sm text-[#A3A3A3]">
                    {role.roleName}
                  </p>
                  <div className="mt-6 space-y-5">
                    {weights.map(([label, value]) => (
                      <div key={label.toString()} className="space-y-2">
                        <div className="flex items-center justify-between text-sm">
                          <span className="text-[#F5F5F5]">{label}</span>
                          <span className="text-[#A3A3A3]">{value}%</span>
                        </div>
                        <ProgressBar value={Number(value)} />
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <p className="mt-6 text-sm leading-6 text-[#A3A3A3]">
                  No job role found. Create a role to define real scoring
                  weights.
                </p>
              )}
            </GlassPanel>
          </MotionPanel>
        </section>
      </div>
    </AppShell>
  );
}
