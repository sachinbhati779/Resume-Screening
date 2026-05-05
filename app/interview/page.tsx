import { AppShell } from "@/components/app/app-shell";
import { InterviewConsole } from "@/components/app/interview-console";
import { LiveInterviewPanel } from "@/components/app/live-interview-panel";
import { MotionPanel } from "@/components/app/motion-panel";
import { PageHeader } from "@/components/app/page-header";
import {
  fetchJobRoles,
  fetchShortlistedCandidates,
} from "@/lib/backend-api";

export default async function AIInterviewPage() {
  const [shortlistResult, rolesResult] = await Promise.all([
    fetchShortlistedCandidates(),
    fetchJobRoles(),
  ]);
  const candidate = shortlistResult.data[0];
  const role =
    rolesResult.data.find((item) => item.roleName === candidate?.roleName) ??
    rolesResult.data[0];

  return (
    <AppShell active="AI Interview">
      <div className="space-y-8">
        <PageHeader
          eyebrow="Interview"
          title="AI interview chat"
          description="Run structured AI interviews with progress tracking, transcript capture, and final scoring."
        />
        <MotionPanel>
          <InterviewConsole
            candidateId={candidate?.resumeId}
            roleId={role?.id}
            roleName={role?.roleName}
            candidateName={candidate?.candidateName}
          />
        </MotionPanel>
        <MotionPanel delay={0.08}>
          <LiveInterviewPanel
            candidateId={candidate?.resumeId}
            roleId={role?.id}
            candidateName={candidate?.candidateName}
            roleName={role?.roleName}
          />
        </MotionPanel>
      </div>
    </AppShell>
  );
}
