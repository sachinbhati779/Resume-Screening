import Link from "next/link";
import { BrainCircuit } from "lucide-react";

import { navItems } from "@/lib/hiring-data";
import { cn } from "@/lib/utils";

type AppShellProps = {
  active: string;
  children: React.ReactNode;
};

export function AppShell({ active, children }: AppShellProps) {
  return (
    <main className="min-h-screen bg-[#0A0A0A] text-[#F5F5F5]">
      <div className="mx-auto flex min-h-screen w-full max-w-[1480px] flex-col lg:flex-row">
        <aside className="sticky top-0 z-20 border-b border-white/10 bg-[#0A0A0A]/90 px-4 py-4 backdrop-blur-xl lg:h-screen lg:w-72 lg:border-b-0 lg:border-r lg:px-5 lg:py-6">
          <div className="flex items-center gap-3">
            <Link href="/" className="flex min-w-0 items-center gap-3">
              <span className="flex size-10 shrink-0 items-center justify-center rounded-md bg-[#E85D04] text-[#0A0A0A] shadow-[0_0_24px_rgba(232,93,4,0.26)]">
                <BrainCircuit className="size-5" />
              </span>
              <span className="min-w-0">
                <span className="block truncate text-sm font-semibold text-[#F5F5F5]">
                  AI Hiring OS
                </span>
                <span className="block truncate text-xs text-[#A3A3A3]">
                  Screening + Interviewer
                </span>
              </span>
            </Link>
          </div>

          <nav className="mt-5 flex gap-2 overflow-x-auto pb-1 lg:mt-8 lg:block lg:space-y-1 lg:overflow-visible lg:pb-0">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = active === item.label;

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    "flex h-10 shrink-0 items-center gap-3 rounded-md px-3 text-sm transition-all duration-200 lg:w-full",
                    isActive
                      ? "bg-[#E85D04] text-[#0A0A0A] shadow-[0_0_24px_rgba(232,93,4,0.22)]"
                      : "text-[#A3A3A3] hover:bg-white/5 hover:text-[#F5F5F5]",
                  )}
                >
                  <Icon className="size-4" />
                  <span className="whitespace-nowrap">{item.label}</span>
                </Link>
              );
            })}
          </nav>

          <div className="mt-8 hidden rounded-lg border border-white/10 bg-white/5 p-4 backdrop-blur-xl lg:block">
            <p className="text-xs font-medium uppercase text-[#E85D04]">
              Live model
            </p>
            <p className="mt-3 text-sm text-[#F5F5F5]">Hiring Signal v4</p>
            <p className="mt-1 text-xs leading-5 text-[#A3A3A3]">
              Resume parsing, role matching, rank scoring, and structured AI interview scoring.
            </p>
          </div>
        </aside>

        <section className="flex-1 px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
          {children}
        </section>
      </div>
    </main>
  );
}
