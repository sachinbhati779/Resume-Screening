import { AppShell } from "@/components/app/app-shell";
import { MotionPanel } from "@/components/app/motion-panel";
import { PageHeader } from "@/components/app/page-header";
import { ResumeUploadPanel } from "@/components/app/resume-upload-panel";
import { fetchJobRoles } from "@/lib/backend-api";

export default async function UploadResumePage() {
  const rolesResult = await fetchJobRoles();

  return (
    <AppShell active="Upload Resume">
      <div className="space-y-8">
        <PageHeader
          eyebrow="Resume intake"
          title="Upload resume"
          description="Parse resumes into structured profiles, normalize skills, and send clean data into screening."
        />
        <MotionPanel>
          <ResumeUploadPanel
            roles={rolesResult.data}
            backendMessage={rolesResult.ok ? undefined : rolesResult.message}
          />
        </MotionPanel>
      </div>
    </AppShell>
  );
}
