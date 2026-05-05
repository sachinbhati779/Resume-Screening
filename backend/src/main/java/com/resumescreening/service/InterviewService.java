package com.resumescreening.service;

import com.resumescreening.dto.AnswerEvaluationResultDTO;
import com.resumescreening.dto.InterviewAnswerDTO;
import com.resumescreening.dto.InterviewQuestionDTO;
import com.resumescreening.dto.InterviewResultDTO;
import com.resumescreening.dto.InterviewStartRequestDTO;
import com.resumescreening.dto.InterviewStartResponseDTO;
import com.resumescreening.exception.CandidateNotFoundException;
import com.resumescreening.exception.InterviewSessionNotFoundException;
import com.resumescreening.exception.InvalidJobRoleException;
import com.resumescreening.model.InterviewAnswer;
import com.resumescreening.model.InterviewQuestion;
import com.resumescreening.model.InterviewReport;
import com.resumescreening.model.InterviewSession;
import com.resumescreening.model.JobRole;
import com.resumescreening.model.Resume;
import com.resumescreening.repository.InterviewRepository;
import com.resumescreening.repository.JobRoleRepository;
import com.resumescreening.repository.ResumeRepository;
import com.resumescreening.repository.ScreeningReportRepository;
import com.resumescreening.util.ScreeningConstants;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewRepository interviewRepository;
    private final ResumeRepository resumeRepository;
    private final JobRoleRepository jobRoleRepository;
    private final ScreeningReportRepository screeningReportRepository;
    private final QuestionGeneratorService questionGeneratorService;
    private final AnswerEvaluatorService answerEvaluatorService;

    public InterviewService(
            InterviewRepository interviewRepository,
            ResumeRepository resumeRepository,
            JobRoleRepository jobRoleRepository,
            ScreeningReportRepository screeningReportRepository,
            QuestionGeneratorService questionGeneratorService,
            AnswerEvaluatorService answerEvaluatorService) {
        this.interviewRepository = interviewRepository;
        this.resumeRepository = resumeRepository;
        this.jobRoleRepository = jobRoleRepository;
        this.screeningReportRepository = screeningReportRepository;
        this.questionGeneratorService = questionGeneratorService;
        this.answerEvaluatorService = answerEvaluatorService;
    }

    public InterviewStartResponseDTO start(InterviewStartRequestDTO request) {
        Resume resume = resumeRepository.findById(request.candidateId())
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found: " + request.candidateId()));
        JobRole role = jobRoleRepository.findById(request.roleId())
                .orElseThrow(() -> new InvalidJobRoleException("Job role not found: " + request.roleId()));
        if (!screeningReportRepository.existsShortlistedByResumeId(resume.getId())) {
            throw new CandidateNotFoundException("Candidate must be shortlisted before interview");
        }

        InterviewSession session = new InterviewSession();
        session.setCandidateId(resume.getId());
        session.setRoleName(role.getRoleName());
        session.setCurrentQuestionIndex(0);
        session.setTotalScore(0);
        session.setStatus(ScreeningConstants.INTERVIEW_IN_PROGRESS);
        InterviewSession savedSession = interviewRepository.saveSession(session);

        List<InterviewQuestion> generatedQuestions = questionGeneratorService.generateQuestions(
                savedSession.getId(),
                resume,
                role);
        generatedQuestions.forEach(interviewRepository::saveQuestion);
        log.info("Started interview session id={} candidateId={}", savedSession.getId(), resume.getId());

        return new InterviewStartResponseDTO(
                savedSession.getId(),
                resume.getId(),
                role.getRoleName(),
                generatedQuestions.size(),
                savedSession.getStatus()
        );
    }

    public InterviewQuestionDTO getCurrentQuestion(Long sessionId) {
        InterviewSession session = findSession(sessionId);
        List<InterviewQuestion> questions = questions(sessionId);
        if (session.getCurrentQuestionIndex() >= questions.size()) {
            throw new IllegalArgumentException("Interview is already complete");
        }
        return toQuestionDto(questions.get(session.getCurrentQuestionIndex()), session.getCurrentQuestionIndex(), questions.size());
    }

    public InterviewAnswerDTO submitAnswer(Long sessionId, InterviewAnswerDTO request) {
        InterviewSession session = findSession(sessionId);
        if (ScreeningConstants.INTERVIEW_COMPLETED.equals(session.getStatus())) {
            return new InterviewAnswerDTO(
                    request.questionId(),
                    request.answerText(),
                    null,
                    "Interview already completed",
                    true,
                    null,
                    getResult(sessionId)
            );
        }

        List<InterviewQuestion> questions = questions(sessionId);
        InterviewQuestion question = resolveQuestion(session, questions, request.questionId());
        AnswerEvaluationResultDTO evaluation = answerEvaluatorService.evaluate(question, request.answerText());

        InterviewAnswer answer = new InterviewAnswer();
        answer.setSessionId(sessionId);
        answer.setQuestionId(question.getId());
        answer.setAnswerText(request.answerText());
        answer.setScore(evaluation.score());
        answer.setFeedback(evaluation.feedback());
        interviewRepository.saveAnswer(answer);

        int nextIndex = session.getCurrentQuestionIndex() + 1;
        double totalScore = round(session.getTotalScore() + evaluation.score());
        boolean completed = nextIndex >= questions.size();
        interviewRepository.updateSession(
                sessionId,
                nextIndex,
                totalScore,
                completed ? ScreeningConstants.INTERVIEW_COMPLETED : ScreeningConstants.INTERVIEW_IN_PROGRESS);

        if (completed) {
            InterviewResultDTO result = createFinalReport(sessionId, totalScore);
            return new InterviewAnswerDTO(question.getId(), request.answerText(), evaluation.score(), evaluation.feedback(), true, null, result);
        }

        return new InterviewAnswerDTO(
                question.getId(),
                request.answerText(),
                evaluation.score(),
                evaluation.feedback(),
                false,
                toQuestionDto(questions.get(nextIndex), nextIndex, questions.size()),
                null
        );
    }

    public InterviewResultDTO getResult(Long sessionId) {
        InterviewReport existing = interviewRepository.findReportBySessionId(sessionId)
                .orElse(null);
        if (existing != null) {
            return toResultDto(existing);
        }
        InterviewSession session = findSession(sessionId);
        if (!ScreeningConstants.INTERVIEW_COMPLETED.equals(session.getStatus())) {
            throw new IllegalArgumentException("Interview is still in progress");
        }
        return createFinalReport(sessionId, session.getTotalScore());
    }

    private InterviewSession findSession(Long sessionId) {
        return interviewRepository.findSessionById(sessionId)
                .orElseThrow(() -> new InterviewSessionNotFoundException("Interview session not found: " + sessionId));
    }

    private List<InterviewQuestion> questions(Long sessionId) {
        List<InterviewQuestion> questions = interviewRepository.findQuestionsBySessionId(sessionId);
        if (questions.isEmpty()) {
            throw new InterviewSessionNotFoundException("No questions found for interview session: " + sessionId);
        }
        return questions;
    }

    private InterviewQuestion resolveQuestion(InterviewSession session, List<InterviewQuestion> questions, Long requestedQuestionId) {
        if (requestedQuestionId != null) {
            return questions.stream()
                    .filter(question -> question.getId().equals(requestedQuestionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Question does not belong to this session"));
        }
        if (session.getCurrentQuestionIndex() >= questions.size()) {
            throw new IllegalArgumentException("No active question available");
        }
        return questions.get(session.getCurrentQuestionIndex());
    }

    private InterviewResultDTO createFinalReport(Long sessionId, double finalScore) {
        InterviewReport report = new InterviewReport();
        report.setSessionId(sessionId);
        report.setFinalScore(round(finalScore));
        report.setRecommendation(recommendation(finalScore));
        report.setStrengths("Relevant technical coverage, practical project discussion, and role-oriented reasoning.");
        report.setWeaknesses(finalScore >= 70 ? "Needs deeper examples in follow-up rounds." : "Needs stronger concept clarity and more specific implementation detail.");
        InterviewReport saved = interviewRepository.saveReport(report);
        return toResultDto(saved);
    }

    private String recommendation(double finalScore) {
        if (finalScore >= 80) {
            return "HIRE";
        }
        if (finalScore >= 60) {
            return "HOLD";
        }
        return "REJECT";
    }

    private InterviewQuestionDTO toQuestionDto(InterviewQuestion question, int index, int totalQuestions) {
        return new InterviewQuestionDTO(
                question.getId(),
                question.getSessionId(),
                question.getQuestionText(),
                index + 1,
                totalQuestions,
                question.getMarks()
        );
    }

    private InterviewResultDTO toResultDto(InterviewReport report) {
        LocalDateTime createdAt = report.getCreatedAt() == null ? LocalDateTime.now() : report.getCreatedAt();
        return new InterviewResultDTO(
                report.getSessionId(),
                report.getFinalScore(),
                report.getRecommendation(),
                report.getStrengths(),
                report.getWeaknesses(),
                createdAt
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
