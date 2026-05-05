import Link from "next/link";
import {
  ArrowRight,
  Bot,
  BrainCircuit,
  FileCheck2,
  ListChecks,
  Trophy,
  Upload,
  UsersRound,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import GlassTunnel3D from "@/components/ui/liquid-glass-boxes";
import { fetchDashboardSummary } from "@/lib/backend-api";

export default async function LandingPage() {
  const summaryResult = await fetchDashboardSummary();
  const summary = summaryResult.data;
  const stats = [
    {
      label: "Total resumes",
      value: summary.totalResumes.toLocaleString(),
      icon: FileCheck2,
    },
    {
      label: "Shortlisted",
      value: summary.shortlistedCandidates.toLocaleString(),
      icon: UsersRound,
    },
    {
      label: "Rejected",
      value: summary.rejectedCandidates.toLocaleString(),
      icon: Trophy,
    },
    {
      label: "Average score",
      value: summary.averageScore.toString(),
      icon: Bot,
    },
  ];
  const workflow = [
    {
      label: "Define roles",
      description: "Set skills, experience, education, and scoring weights.",
      href: "/jobs/new",
      icon: ListChecks,
    },
    {
      label: "Screen resumes",
      description: "Upload profiles and rank candidates against the role.",
      href: "/resumes/upload",
      icon: Upload,
    },
    {
      label: "Run interviews",
      description: "Move shortlisted candidates through structured AI chat.",
      href: "/interview",
      icon: BrainCircuit,
    },
  ];

  return (
    <main className="min-h-screen bg-[#0A0A0A] text-[#F5F5F5]">
      <section className="relative isolate min-h-[88svh] overflow-hidden border-b border-white/10">
        <GlassTunnel3D
          className="pointer-events-none absolute inset-0 h-full min-h-full opacity-90"
          boxCount={4}
          circleCount={5}
          animationDuration={5.8}
          boxWidth={430}
          boxHeight={330}
          circleSize={140}
        />
        <div
          className="absolute inset-0 z-0"
          style={{
            background:
              "linear-gradient(90deg, #0A0A0A 0%, rgba(10,10,10,0.92) 34%, rgba(10,10,10,0.48) 66%, rgba(10,10,10,0.22) 100%)",
          }}
        />

        <header className="relative z-10 mx-auto flex w-full max-w-7xl items-center justify-between gap-4 px-4 py-5 sm:px-6 lg:px-8">
          <Link href="/" className="flex min-w-0 items-center gap-3">
            <span className="flex size-10 shrink-0 items-center justify-center rounded-md bg-[#E85D04] text-[#0A0A0A] shadow-[0_0_28px_rgba(232,93,4,0.28)]">
              <BrainCircuit className="size-5" />
            </span>
            <span className="min-w-0">
              <span className="block truncate text-sm font-semibold">
                AI Hiring OS
              </span>
              <span className="block truncate text-xs text-[#A3A3A3]">
                Screening + Interviewer
              </span>
            </span>
          </Link>

          <nav className="hidden items-center gap-6 text-sm text-[#A3A3A3] md:flex">
            <Link className="transition hover:text-[#F5F5F5]" href="/dashboard">
              Dashboard
            </Link>
            <Link className="transition hover:text-[#F5F5F5]" href="/ranking">
              Ranking
            </Link>
            <Link className="transition hover:text-[#F5F5F5]" href="/interview">
              Interview
            </Link>
          </nav>

          <Button asChild size="sm" variant="outline">
            <Link href="/dashboard">Open app</Link>
          </Button>
        </header>

        <div className="relative z-10 mx-auto grid w-full max-w-7xl items-center gap-10 px-4 pb-14 pt-10 sm:px-6 sm:pb-20 sm:pt-16 lg:grid-cols-[minmax(0,0.95fr)_minmax(280px,0.55fr)] lg:px-8">
          <div className="max-w-3xl">
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-[#E85D04]">
              Resume Screening + AI Interviewer
            </p>
            <h1 className="mt-5 max-w-3xl text-5xl font-semibold leading-[0.96] text-[#F5F5F5] sm:text-6xl lg:text-7xl">
              AI Hiring OS
            </h1>
            <p className="mt-6 max-w-2xl text-base leading-7 text-[#A3A3A3] sm:text-lg">
              Screen resumes, rank candidates, and run structured AI interviews
              from one focused workflow built for modern recruiting teams.
            </p>
            <div className="mt-8 flex flex-col gap-3 sm:flex-row">
              <Button asChild size="lg">
                <Link href="/dashboard">
                  Open dashboard
                  <ArrowRight className="size-4" />
                </Link>
              </Button>
              <Button asChild size="lg" variant="outline">
                <Link href="/resumes/upload">
                  <Upload className="size-4" />
                  Upload resume
                </Link>
              </Button>
            </div>
          </div>

          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
            {stats.map((stat) => {
              const Icon = stat.icon;

              return (
                <div
                  key={stat.label}
                  className="rounded-lg border border-white/10 bg-white/5 p-4 backdrop-blur-xl"
                >
                  <div className="flex items-center justify-between gap-4">
                    <span className="text-sm text-[#A3A3A3]">{stat.label}</span>
                    <Icon className="size-4 text-[#E85D04]" />
                  </div>
                  <p className="mt-3 text-2xl font-semibold text-[#F5F5F5]">
                    {stat.value}
                  </p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      <section className="mx-auto grid w-full max-w-7xl gap-4 px-4 py-8 sm:px-6 lg:grid-cols-3 lg:px-8">
        {workflow.map((item) => {
          const Icon = item.icon;

          return (
            <Link
              key={item.label}
              href={item.href}
              className="group rounded-lg border border-white/10 bg-[#111111] p-5 transition-all duration-200 hover:-translate-y-0.5 hover:border-[#E85D04]/45 hover:bg-white/5"
            >
              <div className="flex items-center justify-between gap-4">
                <Icon className="size-5 text-[#E85D04]" />
                <ArrowRight className="size-4 text-[#A3A3A3] transition group-hover:text-[#E85D04]" />
              </div>
              <h2 className="mt-5 text-lg font-semibold text-[#F5F5F5]">
                {item.label}
              </h2>
              <p className="mt-2 text-sm leading-6 text-[#A3A3A3]">
                {item.description}
              </p>
            </Link>
          );
        })}
      </section>
    </main>
  );
}
