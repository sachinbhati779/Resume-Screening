import { AppShell } from "@/components/app/app-shell";
import { LiveInterviewRoom } from "@/components/app/live-interview-room";
import { MotionPanel } from "@/components/app/motion-panel";
import { PageHeader } from "@/components/app/page-header";

type LiveInterviewPageProps = {
  params: { token: string };
};

export default function LiveInterviewPage({ params }: LiveInterviewPageProps) {
  return (
    <AppShell active="AI Interview">
      <div className="space-y-8">
        <PageHeader
          eyebrow="Live interview"
          title="Voice interview session"
          description="Join the authenticated voice interview room and record the live session."
        />
        <MotionPanel>
          <LiveInterviewRoom token={params.token} />
        </MotionPanel>
      </div>
    </AppShell>
  );
}
