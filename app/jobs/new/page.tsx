import { AppShell } from "@/components/app/app-shell";
import { JobRoleBuilder } from "@/components/app/job-role-builder";
import { MotionPanel } from "@/components/app/motion-panel";
import { PageHeader } from "@/components/app/page-header";

export default function AddJobRolePage() {
  return (
    <AppShell active="Add Job Role">
      <div className="space-y-8">
        <PageHeader
          eyebrow="Role setup"
          title="Add job role"
          description="Define the hiring brief, scorecard weights, and interview defaults before resume screening begins."
        />
        <MotionPanel>
          <JobRoleBuilder />
        </MotionPanel>
      </div>
    </AppShell>
  );
}
