# API Examples

## Create Job Role

```http
POST /api/job-roles
Content-Type: application/json
```

```json
{
  "roleName": "Frontend AI Engineer",
  "requiredSkills": ["React", "TypeScript", "REST", "Java"],
  "minExperience": 4,
  "requiredEducation": "Computer Science",
  "keywords": ["ai", "dashboard", "rest", "screening"],
  "skillWeightage": 40,
  "experienceWeightage": 25,
  "projectWeightage": 15,
  "educationWeightage": 10,
  "keywordWeightage": 10
}
```

## Submit Resume Manually

```http
POST /api/resumes
Content-Type: application/json
```

```json
{
  "candidateName": "Aarav Menon",
  "email": "aarav@example.com",
  "phone": "+91 90000 00000",
  "skills": ["Java", "SQL", "React", "REST"],
  "experienceYears": 5,
  "education": "B.Tech Computer Science",
  "projects": ["AI hiring dashboard", "SQL ranking engine"],
  "summary": "Built Java REST APIs, SQL scoring workflows, and React dashboards for AI products.",
  "appliedRole": "Frontend AI Engineer"
}
```

## Upload Resume File

```http
POST /api/resumes/upload
Content-Type: multipart/form-data
```

Form fields:

- `files`: one or more `.pdf`, `.doc`, `.docx`, `.txt`, or `.text` resume files
- `appliedRole`: optional fallback role

Valid files are saved in the `resumes` table with `file_name`, `file_type`, `file_size`, and `file_data`. In a batch upload, valid files are accepted even if another file is rejected; rejected files are returned in the `warnings` array.

Text resumes parse best with lines like:

```text
name: Aarav Menon
email: aarav@example.com
phone: +91 90000 00000
skills: Java, SQL, React, REST
experience: 5
education: B.Tech Computer Science
projects: AI dashboard, SQL ranking engine
summary: Built Java REST APIs and React hiring dashboards.
role: Frontend AI Engineer
```

## View Or Download Uploaded Resume File

```http
GET /api/resumes/1/file
```

```http
GET /api/resumes/1/file?download=true
```

## Screen Resumes

```http
POST /api/screen
Content-Type: application/json
```

```json
{
  "roleId": 1,
  "resumeIds": [1, 2, 3]
}
```

If `resumeIds` is empty or omitted, all resumes are processed.

## Candidate Ranking

```http
GET /api/candidates/ranking
```

Returns candidates sorted by score descending.

## Start Interview

```http
POST /api/interview/start
Content-Type: application/json
```

```json
{
  "candidateId": 1,
  "roleId": 1
}
```

## Get Current Question

```http
GET /api/interview/1/question
```

## Submit Answer

```http
POST /api/interview/1/answer
Content-Type: application/json
```

```json
{
  "questionId": 1,
  "answerText": "I would expose REST endpoints over HTTP, use JSON payloads, validate status codes, and persist interview events."
}
```

## Get Final Interview Result

```http
GET /api/interview/1/result
```

Recommendations:

- `HIRE`: final score >= 80
- `HOLD`: final score >= 60
- `REJECT`: final score < 60
