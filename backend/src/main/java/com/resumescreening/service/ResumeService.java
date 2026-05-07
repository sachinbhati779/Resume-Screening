package com.resumescreening.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.resumescreening.dto.ResumeFileDTO;
import com.resumescreening.dto.ResumeRequestDTO;
import com.resumescreening.dto.ResumeResponseDTO;
import com.resumescreening.dto.ResumeUploadResponseDTO;
import com.resumescreening.exception.CandidateNotFoundException;
import com.resumescreening.exception.IncompleteResumeException;
import com.resumescreening.model.Resume;
import com.resumescreening.repository.ResumeRepository;

@Service
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "txt", "text");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.ms-word",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );
    private static final List<String> SECTION_HEADERS = List.of(
            "name",
            "email",
            "phone",
            "skills",
            "experience",
            "education",
            "projects",
            "summary",
            "role"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\+?\\d[\\d\\s().-]{7,}\\d"
    );
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(?:years?|yrs?)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Tika TIKA = new Tika();

    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public ResumeResponseDTO create(ResumeRequestDTO request) {
        Resume saved = resumeRepository.save(toModel(request));
        log.info("Created resume id={} candidate={}", saved.getId(), saved.getCandidateName());
        return toResponse(saved);
    }

    public ResumeUploadResponseDTO upload(MultipartFile[] files, String appliedRole) {
        if (files == null || files.length == 0) {
            throw new IncompleteResumeException("At least one file is required");
        }

        List<ResumeResponseDTO> savedResumes = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                validateFile(file);
                Resume parsed = parseResume(file, appliedRole);
                Resume saved = resumeRepository.save(parsed);
                savedResumes.add(toResponse(saved));
            } catch (RuntimeException | IOException exception) {
                warnings.add(file.getOriginalFilename() + ": " + exception.getMessage());
            }
        }
        return new ResumeUploadResponseDTO(savedResumes.size(), savedResumes, warnings);
    }

    public List<ResumeResponseDTO> findAll() {
        return resumeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Resume findModelById(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException("Resume not found: " + id));
    }

    public ResumeResponseDTO findById(Long id) {
        return toResponse(findModelById(id));
    }

    public ResumeFileDTO findFileById(Long id) {
        Resume resume = findModelById(id);
        byte[] data = resume.getFileData();
        if (data == null || data.length == 0) {
            throw new CandidateNotFoundException("Resume file not found: " + id);
        }
        return new ResumeFileDTO(
                data,
                resume.getFileName(),
                resume.getFileType(),
                resume.getFileSize()
        );
    }

    public ResumeResponseDTO toResponse(Resume resume) {
        return new ResumeResponseDTO(
                resume.getId(),
                resume.getCandidateName(),
                resume.getEmail(),
                resume.getPhone(),
                resume.getSkills(),
                resume.getExperienceYears(),
                resume.getEducation(),
                resume.getProjects(),
                resume.getSummary(),
                resume.getAppliedRole(),
                resume.getFileName(),
                resume.getFileType(),
                resume.getFileSize(),
                resume.getCreatedAt()
        );
    }

    private Resume toModel(ResumeRequestDTO request) {
        Resume resume = new Resume();
        resume.setCandidateName(request.candidateName());
        resume.setEmail(request.email());
        resume.setPhone(request.phone());
        resume.setSkills(request.skills());
        resume.setExperienceYears(request.experienceYears());
        resume.setEducation(request.education());
        resume.setProjects(request.projects());
        resume.setSummary(request.summary());
        resume.setAppliedRole(request.appliedRole());
        return resume;
    }

    private Resume parseResume(MultipartFile file, String appliedRole) throws IOException {
        String content = extractResumeText(file);
        Resume resume = parseResumeText(content, appliedRole);
        resume.setFileName(file.getOriginalFilename());
        resume.setFileType(resolveFileType(file));
        resume.setFileSize(file.getSize());
        resume.setFileData(file.getBytes());
        resume.setExtractedText(content);
        resume.setCandidateName(resolveCandidateName(
                resume.getCandidateName(),
                resume.getEmail(),
                file.getOriginalFilename()));
        return resume;
    }

    private Resume parseResumeText(String content, String appliedRole) {
        String safeContent = content == null ? "" : content;
        Resume resume = new Resume();
        String email = extractEmail(safeContent);
        resume.setCandidateName(extractCandidateName(safeContent, email));
        resume.setEmail(email);
        resume.setPhone(extractPhone(safeContent));
        resume.setSkills(splitValues(extractSectionValue(safeContent, "skills")));
        String experienceSection = extractSectionValue(safeContent, "experience");
        resume.setExperienceYears(parseExperience(experienceSection.isBlank() ? safeContent : experienceSection));
        resume.setEducation(extractSectionValue(safeContent, "education"));
        resume.setProjects(splitValues(extractSectionValue(safeContent, "projects")));
        String summary = extractSectionValue(safeContent, "summary");
        resume.setSummary(summary.isBlank() ? summarize(safeContent) : summary);
        String resolvedRole = appliedRole == null || appliedRole.isBlank()
                ? extractSectionValue(safeContent, "role")
                : appliedRole;
        resume.setAppliedRole(resolvedRole == null ? "" : resolvedRole);
        return resume;
    }

    private String extractCandidateName(String content, String email) {
        String name = extractSectionValue(content, "name");
        if (!name.isBlank()) {
            return name;
        }
        String firstLine = firstNonEmptyLine(content);
        if (!firstLine.isBlank() && !firstLine.toLowerCase(Locale.ROOT).contains("resume")) {
            return firstLine;
        }
        if (email != null && !email.isBlank()) {
            String localPart = email.split("@")[0];
            String normalized = localPart.replaceAll("[._-]+", " ").trim();
            if (!normalized.isBlank()) {
                return titleCase(normalized);
            }
        }
        return "";
    }

    private String firstNonEmptyLine(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        return Arrays.stream(content.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .findFirst()
                .orElse("");
    }

    private String titleCase(String value) {
        String[] parts = value.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.toString();
    }

    private String extractSectionValue(String content, String key) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        String[] lines = content.split("\\R");
        boolean inSection = false;
        StringBuilder value = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                if (inSection && value.length() > 0) {
                    break;
                }
                continue;
            }
            String lower = trimmed.toLowerCase(Locale.ROOT);
            if (lower.startsWith(normalizedKey + ":")) {
                return trimmed.substring(trimmed.indexOf(':') + 1).trim();
            }
            if (isSectionHeader(lower, normalizedKey)) {
                inSection = true;
                continue;
            }
            if (inSection) {
                if (isAnySectionHeader(lower)) {
                    break;
                }
                if (value.length() > 0) {
                    value.append(' ');
                }
                value.append(trimmed);
            }
        }
        return value.toString().trim();
    }

    private boolean isSectionHeader(String line, String normalizedKey) {
        return line.equals(normalizedKey)
                || line.startsWith(normalizedKey + " ")
                || line.startsWith(normalizedKey + "-");
    }

    private boolean isAnySectionHeader(String line) {
        return SECTION_HEADERS.stream().anyMatch(header -> isSectionHeader(line, header));
    }

    private String extractEmail(String content) {
        Matcher matcher = EMAIL_PATTERN.matcher(content == null ? "" : content);
        return matcher.find() ? matcher.group() : "";
    }

    private String extractPhone(String content) {
        Matcher matcher = PHONE_PATTERN.matcher(content == null ? "" : content);
        return matcher.find() ? matcher.group().trim() : "";
    }

    private List<String> splitValues(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private double parseExperience(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        Matcher matcher = EXPERIENCE_PATTERN.matcher(value);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        String numeric = value.replaceAll("[^0-9.]", "");
        if (numeric.isBlank()) {
            return 0;
        }
        return Double.parseDouble(numeric);
    }

    private String summarize(String content) {
        if (content == null) {
            return "";
        }
        String trimmed = content.trim();
        if (trimmed.length() <= 400) {
            return trimmed;
        }
        return trimmed.substring(0, 400).trim();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IncompleteResumeException("Resume file is empty.");
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IncompleteResumeException("Only PDF, DOC, DOCX, or TXT files are allowed.");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!contentType.isBlank()
                && !"application/octet-stream".equals(contentType)
                && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IncompleteResumeException("Unsupported resume content type: " + contentType);
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String resolveFileType(MultipartFile file) {
        String normalized = normalizeContentType(file.getContentType());
        if (!normalized.isBlank()) {
            return normalized;
        }
        return switch (extractExtension(file.getOriginalFilename())) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "text/plain";
        };
    }

    private String extractResumeText(MultipartFile file) throws IOException {
        String extension = extractExtension(file.getOriginalFilename());
        if ("txt".equals(extension) || "text".equals(extension)) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        try (InputStream inputStream = file.getInputStream()) {
            return TIKA.parseToString(inputStream);
        } catch (Exception exception) {
            throw new IOException("Unable to read resume text", exception);
        }
    }

    private String fallbackCandidateName(String filename) {
        if (filename == null || filename.isBlank()) {
            return "Unknown Candidate";
        }
        String name = filename.replaceFirst("\\.[^.]+$", "");
        return name.isBlank() ? "Unknown Candidate" : name.trim();
    }

    private String resolveCandidateName(String candidateName, String email, String filename) {
        if (candidateName != null && !candidateName.isBlank()) {
            return candidateName;
        }
        if (email != null && !email.isBlank()) {
            String localPart = email.split("@")[0];
            String normalized = localPart.replaceAll("[._-]+", " ").trim();
            if (!normalized.isBlank()) {
                return titleCase(normalized);
            }
        }
        return fallbackCandidateName(filename);
    }
}
