"use client";

import * as React from "react";
import { Copy, Link2 } from "lucide-react";

import { GlassPanel } from "@/components/app/glass-panel";
import { Button } from "@/components/ui/button";
import {
  type LiveInterviewLink,
  createLiveInterview,
} from "@/lib/backend-api";

type LiveInterviewPanelProps = {
  candidateId?: number;
  roleId?: number;
  candidateName?: string;
  roleName?: string;
};

export function LiveInterviewPanel({
  candidateId,
  roleId,
  candidateName,
  roleName,
}: LiveInterviewPanelProps) {
  const [link, setLink] = React.useState<LiveInterviewLink | null>(null);
  const [status, setStatus] = React.useState<string | null>(null);
  const [loading, setLoading] = React.useState(false);

  async function handleCreate() {
    if (!candidateId || !roleId) {
      setStatus("Shortlist a candidate and pick a role first.");
      return;
    }
    setLoading(true);
    setStatus(null);
    try {
      const created = await createLiveInterview(candidateId, roleId);
      setLink(created);
      setStatus("Live interview link created.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Unable to create live interview link");
    } finally {
      setLoading(false);
    }
  }

  async function copy(value: string) {
    try {
      await navigator.clipboard.writeText(value);
      setStatus("Copied link to clipboard.");
    } catch {
      setStatus("Copy failed. Select the link and copy manually.");
    }
  }

  return (
    <GlassPanel className="p-5 sm:p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h3 className="text-lg font-semibold text-[#F5F5F5]">
            Voice interview link
          </h3>
          <p className="mt-2 text-sm text-[#A3A3A3]">
            Host a live, voice-based interview with recording on your own Jitsi
            server.
          </p>
          <p className="mt-2 text-xs text-[#A3A3A3]">
            Candidate: {candidateName ?? "Select a shortlisted candidate"} · Role: {roleName ?? "Select a role"}
          </p>
        </div>
        <Button type="button" onClick={handleCreate} disabled={loading}>
          {loading ? "Creating" : "Create live link"}
        </Button>
      </div>

      {status ? (
        <p className="mt-4 text-sm text-[#A3A3A3]">{status}</p>
      ) : null}

      {link ? (
        <div className="mt-5 space-y-3">
          {(
            [
              ["Host link", link.hostLink, link.hostToken, true],
              ["Candidate link", link.candidateLink, link.candidateToken, false],
            ] as const
          ).map(([label, value, token, showToken], index) => (
            <div
              key={`${label}-${index}`}
              className="flex flex-col gap-3 rounded-md border border-white/10 bg-white/5 p-4 sm:flex-row sm:items-center sm:justify-between"
            >
              <div className="min-w-0">
                <p className="text-sm font-medium text-[#F5F5F5]">{label}</p>
                <p className="mt-1 break-all text-xs text-[#A3A3A3]">{value}</p>
                {showToken ? (
                  <p className="mt-2 text-[11px] text-[#A3A3A3]">
                    Token: {token}
                  </p>
                ) : null}
              </div>
              <div className="flex shrink-0 gap-2">
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={() => copy(value)}
                >
                  <Copy className="size-4" />
                  Copy
                </Button>
                <Button asChild size="sm" variant="outline">
                  <a href={value} target="_blank" rel="noreferrer">
                    <Link2 className="size-4" />
                    Open
                  </a>
                </Button>
              </div>
            </div>
          ))}
        </div>
      ) : null}
    </GlassPanel>
  );
}
