CREATE TABLE IF NOT EXISTS job_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(160) NOT NULL,
    required_skills TEXT NOT NULL,
    min_experience DECIMAL(5,2) NOT NULL DEFAULT 0,
    required_education VARCHAR(255) NOT NULL,
    keywords TEXT,
    skill_weightage INT NOT NULL DEFAULT 40,
    experience_weightage INT NOT NULL DEFAULT 25,
    project_weightage INT NOT NULL DEFAULT 15,
    education_weightage INT NOT NULL DEFAULT 10,
    keyword_weightage INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    candidate_name VARCHAR(160) NOT NULL,
    email VARCHAR(180) NOT NULL,
    phone VARCHAR(40),
    skills TEXT NOT NULL,
    experience_years DECIMAL(5,2) NOT NULL DEFAULT 0,
    education VARCHAR(255) NOT NULL,
    projects TEXT,
    summary TEXT NOT NULL,
    applied_role VARCHAR(160) NOT NULL,
    file_name VARCHAR(255),
    file_type VARCHAR(120),
    file_size BIGINT,
    file_data BLOB,
    extracted_text CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resumes_email ON resumes(email);
CREATE INDEX IF NOT EXISTS idx_resumes_applied_role ON resumes(applied_role);

CREATE TABLE IF NOT EXISTS screening_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    candidate_name VARCHAR(160) NOT NULL,
    role_name VARCHAR(160) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    status VARCHAR(40) NOT NULL,
    remarks TEXT,
    matched_keywords TEXT,
    missing_keywords TEXT,
    skills_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    experience_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    project_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    education_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    keyword_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    ats_complete BOOLEAN NOT NULL DEFAULT FALSE,
    ats_readable BOOLEAN NOT NULL DEFAULT FALSE,
    ats_simple_formatting BOOLEAN NOT NULL DEFAULT FALSE,
    ats_required_sections BOOLEAN NOT NULL DEFAULT FALSE,
    ats_issues TEXT,
    explanation TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_screening_resume FOREIGN KEY (resume_id) REFERENCES resumes(id),
    CONSTRAINT fk_screening_role FOREIGN KEY (role_id) REFERENCES job_roles(id)
);

CREATE INDEX IF NOT EXISTS idx_screening_score ON screening_reports(score);
CREATE INDEX IF NOT EXISTS idx_screening_status ON screening_reports(status);

CREATE TABLE IF NOT EXISTS shortlisted_candidates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id BIGINT NOT NULL,
    report_id BIGINT NOT NULL,
    candidate_name VARCHAR(160) NOT NULL,
    email VARCHAR(180) NOT NULL,
    role_name VARCHAR(160) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shortlist_resume FOREIGN KEY (resume_id) REFERENCES resumes(id),
    CONSTRAINT fk_shortlist_report FOREIGN KEY (report_id) REFERENCES screening_reports(id),
    CONSTRAINT uk_shortlist_resume_role UNIQUE (resume_id, role_name)
);

CREATE TABLE IF NOT EXISTS interview_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    candidate_id BIGINT NOT NULL,
    role_name VARCHAR(160) NOT NULL,
    current_question_index INT NOT NULL DEFAULT 0,
    total_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interview_candidate FOREIGN KEY (candidate_id) REFERENCES resumes(id)
);

CREATE TABLE IF NOT EXISTS interview_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    expected_keywords TEXT,
    marks INT NOT NULL DEFAULT 20,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_question_session FOREIGN KEY (session_id) REFERENCES interview_sessions(id)
);

CREATE TABLE IF NOT EXISTS interview_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    feedback TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_answer_session FOREIGN KEY (session_id) REFERENCES interview_sessions(id),
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES interview_questions(id)
);

CREATE TABLE IF NOT EXISTS interview_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    final_score DECIMAL(5,2) NOT NULL,
    recommendation VARCHAR(40) NOT NULL,
    strengths TEXT,
    weaknesses TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_session FOREIGN KEY (session_id) REFERENCES interview_sessions(id),
    CONSTRAINT uk_interview_report_session UNIQUE (session_id)
);

CREATE TABLE IF NOT EXISTS live_interviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    candidate_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    room_name VARCHAR(200) NOT NULL,
    host_token VARCHAR(120) NOT NULL,
    candidate_token VARCHAR(120) NOT NULL,
    status VARCHAR(40) NOT NULL,
    recording_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_live_interview_candidate FOREIGN KEY (candidate_id) REFERENCES resumes(id),
    CONSTRAINT fk_live_interview_role FOREIGN KEY (role_id) REFERENCES job_roles(id),
    CONSTRAINT uk_live_interview_host_token UNIQUE (host_token),
    CONSTRAINT uk_live_interview_candidate_token UNIQUE (candidate_token)
);

CREATE TABLE IF NOT EXISTS hiring_decisions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    candidate_id BIGINT,
    resume_id BIGINT NOT NULL,
    report_id BIGINT NOT NULL,
    candidate_name VARCHAR(160) NOT NULL,
    role_name VARCHAR(160) NOT NULL,
    decision VARCHAR(40) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hiring_resume FOREIGN KEY (resume_id) REFERENCES resumes(id),
    CONSTRAINT fk_hiring_report FOREIGN KEY (report_id) REFERENCES screening_reports(id),
    CONSTRAINT uk_hiring_report UNIQUE (report_id)
);
