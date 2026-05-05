package com.resumescreening.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.resumescreening.util.ScreeningStatusResolver;
import org.junit.jupiter.api.Test;

class ScreeningStatusResolverTest {

    @Test
    void resolvesShortlistConsiderAndRejectBands() {
        assertThat(ScreeningStatusResolver.resolve(91)).isEqualTo("SHORTLISTED");
        assertThat(ScreeningStatusResolver.resolve(72)).isEqualTo("CONSIDER");
        assertThat(ScreeningStatusResolver.resolve(41)).isEqualTo("REJECTED");
    }
}
