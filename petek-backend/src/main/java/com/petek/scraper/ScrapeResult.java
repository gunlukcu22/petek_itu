package com.petek.scraper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScrapeResult {

    private final int totalRows;
    private final int savedCount;
    private final int skippedCount;
    private final List<String> warnings;

    public ScrapeResult(int totalRows, int savedCount, int skippedCount, List<String> warnings) {
        this.totalRows = totalRows;
        this.savedCount = savedCount;
        this.skippedCount = skippedCount;
        this.warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getSavedCount() {
        return savedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int totalRows;
        private int savedCount;
        private int skippedCount;
        private final List<String> warnings = new ArrayList<>();

        public Builder totalRows(int totalRows) {
            this.totalRows = totalRows;
            return this;
        }

        public Builder savedCount(int savedCount) {
            this.savedCount = savedCount;
            return this;
        }

        public Builder skippedCount(int skippedCount) {
            this.skippedCount = skippedCount;
            return this;
        }

        public Builder addWarning(String warning) {
            if (warning != null && !warning.isBlank()) {
                warnings.add(warning);
            }
            return this;
        }

        public ScrapeResult build() {
            return new ScrapeResult(totalRows, savedCount, skippedCount, Collections.unmodifiableList(warnings));
        }
    }
}
