"use client";

import * as React from "react";
import Link from "next/link";
import { motion } from "framer-motion";
import { Bot, RotateCcw, Send, UserRound } from "lucide-react";

import { GlassPanel } from "@/components/app/glass-panel";
import { ProgressBar } from "@/components/app/progress-bar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  getInterviewQuestion,
  startInterview,
  submitInterviewAnswer,
} from "@/lib/backend-api";
import { cn } from "@/lib/utils";

type ChatMessage = {
  id: number;
  role: "ai" | "candidate";
  content: string;
};

type InterviewConsoleProps = {
  candidateId?: number;
  roleId?: number;
  roleName?: string;
  candidateName?: string;
};

export function InterviewConsole({
  candidateId,
  roleId,
  roleName = "Selected role",
  candidateName = "Selected candidate",
}: InterviewConsoleProps) {
  const [messages, setMessages] = React.useState<ChatMessage[]>([]);
  const [answer, setAnswer] = React.useState("");
  const [questionIndex, setQuestionIndex] = React.useState(0);
  const [totalQuestions, setTotalQuestions] = React.useState(1);
  const [complete, setComplete] = React.useState(false);
  const [sessionId, setSessionId] = React.useState<number | null>(null);
  const [questionId, setQuestionId] = React.useState<number | null>(null);
  const [finalScore, setFinalScore] = React.useState(0);
  const [recommendation, setRecommendation] = React.useState("");
  const [resultText, setResultText] = React.useState("");
  const [status, setStatus] = React.useState<string | null>(null);
  const [isStarting, setIsStarting] = React.useState(false);
  const scrollRef = React.useRef<HTMLDivElement>(null);

  const startBackendFlow = React.useCallback(async () => {
    setMessages([]);
    setComplete(false);
    setAnswer("");
    setFinalScore(0);
    setRecommendation("");
    setResultText("");

    if (!candidateId || !roleId) {
      setSessionId(null);
      setQuestionId(null);
      setStatus("Shortlist a candidate and create a matching role first.");
      return;
    }

    setIsStarting(true);
    try {
      setStatus("Starting interview");
      const session = await startInterview(candidateId, roleId);
      const question = await getInterviewQuestion(session.sessionId);
      setSessionId(session.sessionId);
      setQuestionId(question.questionId);
      setTotalQuestions(question.totalQuestions);
      setQuestionIndex(question.questionNumber - 1);
      setMessages([
        {
          id: question.questionId,
          role: "ai",
          content: question.questionText,
        },
      ]);
      setStatus("Interview active");
    } catch (error) {
      setSessionId(null);
      setQuestionId(null);
      setStatus(error instanceof Error ? error.message : "Unable to start interview");
    } finally {
      setIsStarting(false);
    }
  }, [candidateId, roleId]);

  React.useEffect(() => {
    const timeout = window.setTimeout(() => {
      void startBackendFlow();
    }, 0);

    return () => window.clearTimeout(timeout);
  }, [startBackendFlow]);

  React.useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages, complete]);

  const progress = complete
    ? 100
    : ((questionIndex + 1) / Math.max(totalQuestions, 1)) * 100;

  async function submitAnswer(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const trimmed = answer.trim();
    if (!trimmed || complete || !sessionId || !questionId) {
      return;
    }

    const userMessage: ChatMessage = {
      id: Date.now(),
      role: "candidate",
      content: trimmed,
    };
    const nextMessages = [...messages, userMessage];

    try {
      const response = await submitInterviewAnswer(
        sessionId,
        questionId,
        trimmed,
      );
      if (response.completed && response.result) {
        setMessages(nextMessages);
        setQuestionIndex(totalQuestions);
        setFinalScore(response.result.finalScore);
        setRecommendation(response.result.recommendation);
        setResultText(response.result.strengths);
        setAnswer("");
        setComplete(true);
        return;
      }
      if (response.nextQuestion) {
        setMessages([
          ...nextMessages,
          {
            id: response.nextQuestion.questionId,
            role: "ai",
            content: response.nextQuestion.questionText,
          },
        ]);
        setQuestionId(response.nextQuestion.questionId);
        setQuestionIndex(response.nextQuestion.questionNumber - 1);
        setTotalQuestions(response.nextQuestion.totalQuestions);
        setAnswer("");
      }
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Unable to submit answer");
    }
  }

  return (
    <GlassPanel className="overflow-hidden">
      <div className="grid min-h-[720px] lg:grid-cols-[300px_1fr]">
        <aside className="border-b border-white/10 p-5 lg:border-b-0 lg:border-r">
          <div className="flex items-center gap-3">
            <span className="flex size-11 items-center justify-center rounded-md bg-[#E85D04] text-[#0A0A0A]">
              <Bot className="size-5" />
            </span>
            <div>
              <h2 className="font-semibold text-[#F5F5F5]">AI Interviewer</h2>
              <p className="mt-1 text-sm text-[#A3A3A3]">{roleName}</p>
            </div>
          </div>

          <div className="mt-4 rounded-md border border-white/10 bg-white/5 px-3 py-2">
            <p className="text-xs text-[#A3A3A3]">Candidate</p>
            <p className="mt-1 text-sm text-[#F5F5F5]">{candidateName}</p>
          </div>

          {status ? (
            <p className="mt-4 rounded-md border border-white/10 bg-white/5 px-3 py-2 text-sm text-[#A3A3A3]">
              {status}
            </p>
          ) : null}

          <div className="mt-8 space-y-3">
            <div className="flex items-center justify-between text-sm">
              <span className="text-[#A3A3A3]">Progress</span>
              <span className="font-medium text-[#E85D04]">
                {Math.round(progress)}%
              </span>
            </div>
            <ProgressBar value={progress} />
          </div>

          <div className="mt-8 space-y-3 text-sm">
            {[
              "Technical depth",
              "Concept clarity",
              "Keyword coverage",
              "Communication",
            ].map((item, index) => (
              <div
                key={item}
                className={cn(
                  "flex items-center justify-between rounded-md border border-white/10 px-3 py-2",
                  index <= questionIndex || complete
                    ? "bg-white/10 text-[#F5F5F5]"
                    : "bg-white/5 text-[#A3A3A3]",
                )}
              >
                <span>{item}</span>
                <span>{index < questionIndex || complete ? "Done" : "Open"}</span>
              </div>
            ))}
          </div>
        </aside>

        <section className="flex min-h-[720px] flex-col">
          {complete ? (
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              className="flex flex-1 items-center justify-center p-6"
            >
              <div className="w-full max-w-lg text-center">
                <div className="mx-auto flex size-16 items-center justify-center rounded-md bg-[#E85D04] text-[#0A0A0A] shadow-[0_0_34px_rgba(232,93,4,0.28)]">
                  <Bot className="size-7" />
                </div>
                <h3 className="mt-6 text-3xl font-semibold text-[#F5F5F5]">
                  Final interview score
                </h3>
                <p className="mt-3 text-sm leading-6 text-[#A3A3A3]">
                  {resultText}
                </p>
                <div className="mt-8">
                  <p className="text-6xl font-semibold text-[#F5F5F5]">
                    {Math.round(finalScore)}
                  </p>
                  <ProgressBar value={finalScore} className="mt-5" />
                </div>
                {recommendation ? (
                  <p className="mt-4 text-sm font-semibold text-[#E85D04]">
                    {recommendation}
                  </p>
                ) : null}
                <div className="mt-8 flex justify-center gap-3">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={startBackendFlow}
                  >
                    <RotateCcw className="size-4" />
                    Restart
                  </Button>
                  <Button asChild type="button">
                    <Link href="/shortlist">View shortlist</Link>
                  </Button>
                </div>
              </div>
            </motion.div>
          ) : (
            <>
              <div className="flex-1 space-y-5 overflow-y-auto p-5 sm:p-6">
                {messages.map((message) => {
                  const isAi = message.role === "ai";

                  return (
                    <motion.div
                      key={message.id}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      className={cn(
                        "flex gap-3",
                        isAi ? "justify-start" : "justify-end",
                      )}
                    >
                      {isAi ? (
                        <span className="mt-1 flex size-9 shrink-0 items-center justify-center rounded-md bg-white/10 text-[#E85D04]">
                          <Bot className="size-4" />
                        </span>
                      ) : null}
                      <div
                        className={cn(
                          "max-w-[82%] rounded-lg px-4 py-3 text-sm leading-6 sm:max-w-[68%]",
                          isAi
                            ? "border border-white/10 bg-white/10 text-[#F5F5F5]"
                            : "bg-[#E85D04] text-[#0A0A0A] shadow-[0_0_24px_rgba(232,93,4,0.18)]",
                        )}
                      >
                        {message.content}
                      </div>
                      {!isAi ? (
                        <span className="mt-1 flex size-9 shrink-0 items-center justify-center rounded-md bg-[#E85D04] text-[#0A0A0A]">
                          <UserRound className="size-4" />
                        </span>
                      ) : null}
                    </motion.div>
                  );
                })}

                {messages.length === 0 ? (
                  <div className="flex h-full min-h-80 items-center justify-center text-center">
                    <div className="max-w-md">
                      <Bot className="mx-auto size-8 text-[#E85D04]" />
                      <h3 className="mt-4 text-xl font-semibold text-[#F5F5F5]">
                        No active interview
                      </h3>
                      <p className="mt-2 text-sm leading-6 text-[#A3A3A3]">
                        Run screening first. A candidate must be shortlisted
                        before the backend allows an interview session.
                      </p>
                      <div className="mt-6 flex justify-center gap-3">
                        <Button asChild variant="outline">
                          <Link href="/results">Run screening</Link>
                        </Button>
                        <Button
                          type="button"
                          onClick={startBackendFlow}
                          disabled={isStarting}
                        >
                          Retry
                        </Button>
                      </div>
                    </div>
                  </div>
                ) : null}
                <div ref={scrollRef} />
              </div>

              <form
                onSubmit={submitAnswer}
                className="border-t border-white/10 p-4 sm:p-5"
              >
                <div className="flex gap-3">
                  <Input
                    value={answer}
                    onChange={(event) => setAnswer(event.target.value)}
                    placeholder="Type candidate answer"
                    className="h-12"
                    disabled={!sessionId || !questionId}
                  />
                  <Button
                    type="submit"
                    size="icon"
                    className="size-12"
                    disabled={!sessionId || !questionId}
                  >
                    <Send className="size-4" />
                    <span className="sr-only">Send</span>
                  </Button>
                </div>
              </form>
            </>
          )}
        </section>
      </div>
    </GlassPanel>
  );
}
