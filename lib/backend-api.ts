const DEFAULT_API_BASE_URL = "http://localhost:8080";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data?: T;
  errorCode?: string;
};

export type BackendResult<T> = {
  data: T;
  ok: boolean;
  message?: string;
};

export type DashboardSummary = {
  totalResumes: number;
  shortlistedCandidates: number;
  rejectedCandidates: number;
  averageScore: number;
};

export type CandidateRanking = {
  reportId: number;
  resumeId: number;
  candidateName: string;
  email: string;
  roleName: string;
  skills: string[];
  experienceYears: number;
  score: number;
  atsScore: number;
  interviewScore: number | null;
  finalScore: number;
  status: string;
  remarks: string;
  hiringDecision: "HIRE" | "HOLD" | "REJECT";
};

export type ScreeningReport = {
  reportId: number;
  resumeId: number;
  roleId: number;
  candidateName: string;
  roleName: string;
  score: number;
  status: string;
  remarks: string;
  matchedKeywords: string[];
  missingKeywords: string[];
  scoreBreakdown: {
    skillsScore: number;
    experienceScore: number;
    projectScore: number;
    educationScore: number;
    keywordScore: number;
  };
  atsChecks: {
    complete: boolean;
    readable: boolean;
    simpleFormatting: boolean;
    hasRequiredSections: boolean;
    issues: string[];
  };
  explanation: string;
  createdAt: string;
};

export type ShortlistedCandidate = {
  id: number;
  resumeId: number;
  reportId: number;
  candidateName: string;
  email: string;
  roleName: string;
  score: number;
  createdAt: string;
};

export type ResumeRecord = {
  id: number;
  candidateName: string;
  email: string;
  phone: string | null;
  skills: string[];
  experienceYears: number;
  education: string;
  projects: string[];
  summary: string;
  appliedRole: string;
  fileName?: string;
  fileType?: string;
  fileSize?: number;
  createdAt: string;
};

export type ResumeUploadResponse = {
  uploadedCount: number;
  resumes: ResumeRecord[];
  warnings: string[];
};

export type JobRolePayload = {
  roleName: string;
  requiredSkills: string[];
  minExperience: number;
  requiredEducation: string;
  keywords: string[];
  skillWeightage?: number;
  experienceWeightage?: number;
  projectWeightage?: number;
  educationWeightage?: number;
  keywordWeightage?: number;
};

export type JobRole = JobRolePayload & {
  id: number;
  createdAt: string;
};

export type InterviewStart = {
  sessionId: number;
  candidateId: number;
  roleName: string;
  totalQuestions: number;
  status: string;
};

export type InterviewQuestion = {
  questionId: number;
  sessionId: number;
  questionText: string;
  questionNumber: number;
  totalQuestions: number;
  marks: number;
};

export type InterviewAnswerResponse = {
  questionId: number;
  answerText: string;
  score: number | null;
  feedback: string | null;
  completed: boolean;
  nextQuestion: InterviewQuestion | null;
  result: InterviewResult | null;
};

export type InterviewResult = {
  sessionId: number;
  finalScore: number;
  recommendation: string;
  strengths: string;
  weaknesses: string;
  createdAt: string;
};

export type LiveInterviewLink = {
  id: number;
  candidateId: number;
  roleId: number;
  roomName: string;
  hostToken: string;
  candidateToken: string;
  hostLink: string;
  candidateLink: string;
  status: string;
  createdAt: string;
};

export type LiveInterviewAccess = {
  id: number;
  roomName: string;
  jitsiUrl: string;
  candidateName: string;
  roleName: string;
  host: boolean;
  status: string;
};

export type HiringDecisionPayload = {
  candidateId?: number;
  resumeId: number;
  reportId: number;
  decision: "HIRE" | "HOLD" | "REJECT";
  notes?: string;
};

const emptySummary: DashboardSummary = {
  totalResumes: 0,
  shortlistedCandidates: 0,
  rejectedCandidates: 0,
  averageScore: 0,
};

export function apiBaseUrl() {
  return (
    process.env.NEXT_PUBLIC_API_BASE_URL ||
    process.env.API_BASE_URL ||
    DEFAULT_API_BASE_URL
  );
}

export async function fetchDashboardSummary(): Promise<
  BackendResult<DashboardSummary>
> {
  return fetchBackend("/api/dashboard/summary", emptySummary);
}

export async function fetchCandidateRanking(): Promise<
  BackendResult<CandidateRanking[]>
> {
  return fetchBackend("/api/candidates/ranking", []);
}

export async function fetchScreeningReports(): Promise<
  BackendResult<ScreeningReport[]>
> {
  return fetchBackend("/api/screening-reports", []);
}

export async function fetchShortlistedCandidates(): Promise<
  BackendResult<ShortlistedCandidate[]>
> {
  return fetchBackend("/api/shortlisted", []);
}

export async function fetchJobRoles(): Promise<BackendResult<JobRole[]>> {
  return fetchBackend("/api/job-roles", []);
}

export async function fetchResumes(): Promise<BackendResult<ResumeRecord[]>> {
  return fetchBackend("/api/resumes", []);
}

export async function createJobRole(payload: JobRolePayload) {
  const response = await sendBackend<JobRole>("/api/job-roles", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  return requireData(response);
}

export async function uploadResumeFiles(files: File[], appliedRole: string) {
  const formData = new FormData();
  files.forEach((file) => formData.append("files", file));
  if (appliedRole.trim()) {
    formData.append("appliedRole", appliedRole.trim());
  }
  const response = await sendBackend<ResumeUploadResponse>(
    "/api/resumes/upload",
    {
      method: "POST",
      body: formData,
    },
  );
  return requireData(response);
}

export async function screenResumes(roleId: number, resumeIds: number[]) {
  const response = await sendBackend<ScreeningReport[]>("/api/screen", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ roleId, resumeIds }),
  });
  return requireData(response);
}

export async function startInterview(candidateId: number, roleId: number) {
  const response = await sendBackend<InterviewStart>("/api/interview/start", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ candidateId, roleId }),
  });
  return requireData(response);
}

export async function getInterviewQuestion(sessionId: number) {
  const response = await sendBackend<InterviewQuestion>(
    `/api/interview/${sessionId}/question`,
    { method: "GET" },
  );
  return requireData(response);
}

export async function submitInterviewAnswer(
  sessionId: number,
  questionId: number,
  answerText: string,
) {
  const response = await sendBackend<InterviewAnswerResponse>(
    `/api/interview/${sessionId}/answer`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ questionId, answerText }),
    },
  );
  return requireData(response);
}

export async function createLiveInterview(candidateId: number, roleId: number) {
  const response = await sendBackend<LiveInterviewLink>("/api/live-interviews", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ candidateId, roleId }),
  });
  return requireData(response);
}

export async function getLiveInterviewAccess(token: string) {
  const response = await sendBackend<LiveInterviewAccess>(
    `/api/live-interviews/${token}`,
    { method: "GET" },
  );
  return requireData(response);
}

export async function createHiringDecision(payload: HiringDecisionPayload) {
  const response = await sendBackend("/api/hiring-decisions", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  return requireData(response);
}

async function fetchBackend<T>(
  path: string,
  emptyData: T,
): Promise<BackendResult<T>> {
  try {
    const response = await fetch(`${apiBaseUrl()}${path}`, {
      cache: "no-store",
      next: { revalidate: 0 },
    });
    const body = (await parseJson<T>(response)) as ApiResponse<T> | null;

    if (!response.ok || body?.success === false || body?.data === undefined) {
      return {
        data: emptyData,
        ok: false,
        message: body?.message || `Backend request failed: ${path}`,
      };
    }

    return { data: body.data, ok: true };
  } catch (error) {
    return {
      data: emptyData,
      ok: false,
      message:
        error instanceof Error
          ? error.message
          : `Backend request failed: ${path}`,
    };
  }
}

async function sendBackend<T>(path: string, init: RequestInit) {
  const response = await fetch(`${apiBaseUrl()}${path}`, init);
  const body = (await parseJson<T>(response)) as ApiResponse<T> | null;

  if (!response.ok || body?.success === false) {
    throw new Error(body?.message || "Backend request failed");
  }

  return body;
}

async function parseJson<T>(response: Response) {
  const contentType = response.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) {
    return null as ApiResponse<T> | null;
  }

  return (await response.json()) as ApiResponse<T>;
}

function requireData<T>(response: ApiResponse<T> | null) {
  if (!response?.data) {
    throw new Error(response?.message || "Backend response did not include data");
  }
  return response.data;
}
