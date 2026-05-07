package com.resumescreening.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResumeTextSimilarityTest {

    @Test
    void normalizesPunctuationCaseAndStopWordsBeforeScoringSimilarity() {
        double similar = ResumeTextSimilarity.cosineSimilarity(
                "Frontend AI Engineer with Java, React, REST APIs and SQL",
                "Built java REST api dashboards using react and sql for AI hiring workflows.");
        double unrelated = ResumeTextSimilarity.cosineSimilarity(
                "Frontend AI Engineer with Java, React, REST APIs and SQL",
                "Managed payroll operations and office travel reimbursements.");

        assertThat(similar).isGreaterThan(unrelated);
        assertThat(similar).isGreaterThan(0);
    }

    @Test
    void returnsZeroForBlankOrUnrelatedText() {
        assertThat(ResumeTextSimilarity.cosineSimilarity("", "Java SQL")).isZero();
        assertThat(ResumeTextSimilarity.cosineSimilarity("Java SQL", "payroll finance")).isZero();
    }
}
