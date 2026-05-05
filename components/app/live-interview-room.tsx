"use client";

import * as React from "react";
import Script from "next/script";
import { Mic, MicOff } from "lucide-react";

import { GlassPanel } from "@/components/app/glass-panel";
import { Button } from "@/components/ui/button";
import { type LiveInterviewAccess, getLiveInterviewAccess } from "@/lib/backend-api";

type LiveInterviewRoomProps = {
  token: string;
};

type JitsiApi = {
  executeCommand: (command: string, value?: unknown) => void;
  dispose: () => void;
};

declare global {
  interface Window {
    JitsiMeetExternalAPI?: new (
      domain: string,
      options: {
        roomName: string;
        parentNode: HTMLElement;
        userInfo?: { displayName?: string };
        configOverwrite?: Record<string, unknown>;
        interfaceConfigOverwrite?: Record<string, unknown>;
      },
    ) => JitsiApi;
  }
}

export function LiveInterviewRoom({ token }: LiveInterviewRoomProps) {
  const [access, setAccess] = React.useState<LiveInterviewAccess | null>(null);
  const [status, setStatus] = React.useState<string | null>(null);
  const [loading, setLoading] = React.useState(true);
  const [scriptReady, setScriptReady] = React.useState(false);
  const containerRef = React.useRef<HTMLDivElement>(null);
  const apiRef = React.useRef<JitsiApi | null>(null);

  React.useEffect(() => {
    let active = true;
    setLoading(true);
    getLiveInterviewAccess(token)
      .then((data) => {
        if (!active) return;
        setAccess(data);
        setStatus(null);
      })
      .catch((error) => {
        if (!active) return;
        setStatus(error instanceof Error ? error.message : "Unable to load live interview");
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [token]);

  React.useEffect(() => {
    if (!scriptReady || !access || !containerRef.current || !window.JitsiMeetExternalAPI) {
      return;
    }

    const jitsiUrl = new URL(access.jitsiUrl);
    const domain = jitsiUrl.host;

    apiRef.current?.dispose();
    apiRef.current = new window.JitsiMeetExternalAPI(domain, {
      roomName: access.roomName,
      parentNode: containerRef.current,
      userInfo: {
        displayName: access.host ? "Interviewer" : access.candidateName,
      },
      configOverwrite: {
        startWithAudioMuted: false,
        startWithVideoMuted: true,
        disableVideo: true,
        prejoinPageEnabled: false,
      },
      interfaceConfigOverwrite: {
        TOOLBAR_BUTTONS: [
          "microphone",
          "hangup",
          "tileview",
          "settings",
        ],
        VIDEO_LAYOUT_FIT: "contain",
      },
    });

    return () => {
      apiRef.current?.dispose();
      apiRef.current = null;
    };
  }, [access, scriptReady]);

  function startRecording() {
    apiRef.current?.executeCommand("startRecording", { mode: "file" });
    setStatus("Recording started.");
  }

  function stopRecording() {
    apiRef.current?.executeCommand("stopRecording", "file");
    setStatus("Recording stopped.");
  }

  return (
    <div className="space-y-4">
      <Script
        src={`${access ? new URL(access.jitsiUrl).origin : "http://localhost:8000"}/external_api.js`}
        onLoad={() => setScriptReady(true)}
      />

      <GlassPanel className="p-4 sm:p-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold text-[#F5F5F5]">
              Live voice interview
            </h2>
            <p className="mt-1 text-sm text-[#A3A3A3]">
              {access ? `${access.candidateName} · ${access.roleName}` : "Loading interview"}
            </p>
          </div>
          {access?.host ? (
            <div className="flex gap-2">
              <Button type="button" size="sm" onClick={startRecording}>
                <Mic className="size-4" />
                Start recording
              </Button>
              <Button type="button" size="sm" variant="ghost" onClick={stopRecording}>
                <MicOff className="size-4" />
                Stop recording
              </Button>
            </div>
          ) : null}
        </div>

        {status ? (
          <p className="mt-3 text-xs text-[#A3A3A3]">{status}</p>
        ) : null}
      </GlassPanel>

      <GlassPanel className="overflow-hidden">
        {loading ? (
          <div className="p-6 text-sm text-[#A3A3A3]">Loading interview room...</div>
        ) : null}
        {!loading && !access ? (
          <div className="p-6 text-sm text-[#A3A3A3]">Unable to load interview room.</div>
        ) : null}
        <div ref={containerRef} className="min-h-[520px]" />
      </GlassPanel>
    </div>
  );
}
