INSERT INTO job_roles (
    role_name,
    required_skills,
    min_experience,
    required_education,
    keywords,
    skill_weightage,
    experience_weightage,
    project_weightage,
    education_weightage,
    keyword_weightage
) VALUES (
    'Frontend AI Engineer',
    'React,TypeScript,REST,Java,SQL',
    4,
    'Computer Science',
    'ai,dashboard,rest,screening,sql',
    40,
    25,
    15,
    10,
    10
);

INSERT INTO resumes (
    candidate_name,
    email,
    phone,
    skills,
    experience_years,
    education,
    projects,
    summary,
    applied_role
) VALUES (
    'Aarav Menon',
    'aarav@example.com',
    '+91 90000 00000',
    'Java,SQL,React,REST',
    5,
    'B.Tech Computer Science',
    'AI hiring dashboard,SQL ranking engine',
    'Built Java REST APIs, SQL scoring workflows, and React dashboards for AI products.',
    'Frontend AI Engineer'
);
