import Link from "next/link";
import { ArrowRight, Bot, FileCheck2, Trophy, Upload, UsersRound } from "lucide-react";

import { AppShell } from "@/components/app/app-shell";
import { GlassPanel } from "@/components/app/glass-panel";
import { MetricCard } from "@/components/app/metric-card";
import { MotionPanel } from "@/components/app/motion-panel";
import { PageHeader } from "@/components/app/page-header";
import { ProgressBar } from "@/components/app/progress-bar";
import { Button } from "@/components/ui/button";
import {
  fetchCandidateRanking,
  fetchDashboardSummary,
} from "@/lib/backend-api";

export default async function DashboardPage() {
  const [summaryResult, rankingResult] = await Promise.all([
    fetchDashboardSummary(),
    fetchCandidateRanking(),
  ]);
  const summary = summaryResult.data;
  const ranking = rankingResult.data;
  const liveMetrics = [
    {
      label: "Total resumes",
      value: summary.totalResumes.toLocaleString(),
      delta: summaryResult.ok ? "Live" : "Offline",
      icon: FileCheck2,
    },
    {
      label: "Shortlisted",
      value: summary.shortlistedCandidates.toLocaleString(),
      delta: summaryResult.ok ? "Live" : "Offline",
      icon: UsersRound,
    },
    {
      label: "Rejected",
      value: summary.rejectedCandidates.toLocaleString(),
      delta: summaryResult.ok ? "Live" : "Offline",
      icon: Trophy,
    },
    {
      label: "Average score",
      value: summary.averageScore.toString(),
      delta: summaryResult.ok ? "API" : "Offline",
      icon: Bot,
    },
  ];
  const livePipeline = [
    { label: "Parsed", value: summary.totalResumes },
    { label: "Ranked", value: ranking.length },
    { label: "Shortlisted", value: summary.shortlistedCandidates },
    { label: "Rejected", value: summary.rejectedCandidates },
  ];
  const visiblePipeline = livePipeline;
  const maxPipeline = Math.max(
    ...visiblePipeline.map((item) => item.value),
    1,
  );

  return (
    <AppShell active="Dashboard">
      <div className="space-y-8">
        <PageHeader
          eyebrow="Dashboard"
          title="AI-based resume screening and interviewer system"
          description="A focused workspace for job roles, resume parsing, candidate ranking, AI interviews, and final shortlists."
          action={
            <div className="flex flex-col gap-3 sm:flex-row">
              <Button asChild variant="outline">
                <Link href="/resumes/upload">
                  <Upload className="size-4" />
                  Upload
                </Link>
              </Button>
              <Button asChild>
                <Link href="/jobs/new">
                  Add role
                  <ArrowRight className="size-4" />
                </Link>
              </Button>
            </div>
          }
        />

        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {liveMetrics.map((metric, index) => (
            <MotionPanel key={metric.label} delay={index * 0.05}>
              <MetricCard {...metric} />
            </MotionPanel>
          ))}
        </section>

        <section className="grid gap-4 xl:grid-cols-[1fr_1.25fr]">
          <MotionPanel delay={0.1}>
            <GlassPanel className="p-5">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <h2 className="text-lg font-semibold text-[#F5F5F5]">
                    Hiring funnel
                  </h2>
                  <p className="mt-1 text-sm text-[#A3A3A3]">
                    Current role pipeline
                  </p>
                </div>
                <span className="rounded-md border border-[#E85D04]/30 bg-[#E85D04]/10 px-3 py-1 text-sm text-[#E85D04]">
                  Live
                </span>
              </div>

              <div className="mt-6 space-y-5">
                {visiblePipeline.map((item) => (
                  <div key={item.label} className="space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-[#F5F5F5]">{item.label}</span>
                      <span className="text-[#A3A3A3]">{item.value}</span>
                    </div>
                    <ProgressBar value={(item.value / maxPipeline) * 100} />
                  </div>
                ))}
              </div>
            </GlassPanel>
          </MotionPanel>

          <MotionPanel delay={0.15}>
            <GlassPanel className="overflow-hidden">
              <div className="flex items-center justify-between gap-4 border-b border-white/10 p-5">
                <div>
                  <h2 className="text-lg font-semibold text-[#F5F5F5]">
                    Top candidate signals
                  </h2>
                  <p className="mt-1 text-sm text-[#A3A3A3]">
                    Sorted by model confidence
                  </p>
                </div>
                <Button asChild variant="ghost">
                  <Link href="/ranking">View ranking</Link>
                </Button>
              </div>
              <div className="divide-y divide-white/10">
                {ranking.slice(0, 3).map((candidate) => (
                  <div
                    key={candidate.reportId}
                    className="grid gap-4 p-5 sm:grid-cols-[1fr_auto] sm:items-center"
                  >
                    <div>
                      <p className="font-medium text-[#F5F5F5]">
                        {candidate.candidateName}
                      </p>
                      <p className="mt-1 text-sm text-[#A3A3A3]">
                        {candidate.roleName} - {candidate.experienceYears} yrs
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      <ProgressBar value={candidate.finalScore} className="w-28" />
                      <span className="w-10 text-right text-sm font-semibold text-[#E85D04]">
                        {candidate.finalScore}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </GlassPanel>
          </MotionPanel>
        </section>
      </div>
    </AppShell>
  );
}
