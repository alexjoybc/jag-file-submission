package ca.bc.gov.open.jag.efilingcommons.submission.models;

import ca.bc.gov.open.jag.efilingcommons.model.Court;
import ca.bc.gov.open.jag.efilingcommons.model.Document;
import ca.bc.gov.open.jag.efilingcommons.model.Party;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FilingPackage {

    private BigDecimal submissionFeeAmount;
    private Court court;
    private List<Document> documents = new ArrayList<>();
    private List<Party> parties = new ArrayList<>();
    private boolean rushedSubmission = false;
    private String applicationCode;

    public FilingPackage(
            @JsonProperty("submissionFeeAmount") BigDecimal submissionFeeAmount,
            @JsonProperty("court") Court court,
            @JsonProperty("documents") List<Document> documents,
            @JsonProperty("parties") List<Party> parties,
            @JsonProperty("rushedSubmission") boolean rushedSubmission,
            @JsonProperty("applicationCode") String applicationCode
    ) {

        this.submissionFeeAmount = submissionFeeAmount;
        this.court = court;
        this.documents.addAll(documents);
        this.parties.addAll(parties);
        this.applicationCode = applicationCode;
        this.rushedSubmission = rushedSubmission;
    }

    public FilingPackage(Builder builder) {
        this.submissionFeeAmount = builder.submissionFeeAmount;
        this.court = builder.court;
        this.documents.addAll(builder.documents);
        this.parties.addAll(builder.parties);
        this.applicationCode = builder.applicationCode;
        this.rushedSubmission = builder.rushedSubmission;
    }

    public BigDecimal getSubmissionFeeAmount() {
        return submissionFeeAmount;
    }

    public Court getCourt() {
        return court;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public List<Party> getParties() {
        return parties;
    }

    public boolean isRushedSubmission() { return rushedSubmission; }

    public String getApplicationCode() { return applicationCode; }

    public static Builder builder() {
        return new Builder();
    }

    public void addDocument(Document document) {
        if(this.documents == null) this.documents = new ArrayList<>();
        this.documents.add(document);
    }

    public static class Builder {

        private BigDecimal submissionFeeAmount;
        private Court court;
        private List<Document> documents = new ArrayList<>();
        private List<Party> parties = new ArrayList<>();
        private String applicationCode;
        private boolean rushedSubmission;

        public Builder rushedSubmission(boolean rushedSubmission) {
            this.rushedSubmission = rushedSubmission;
            return this;
        }

        public Builder applicationCode(String applicationCode) {
            this.applicationCode = applicationCode;
            return this;
        }

        public Builder submissionFeeAmount(BigDecimal submissionFeeAmount) {
            this.submissionFeeAmount = submissionFeeAmount;
            return this;
        }

        public Builder court(Court court) {
            this.court = court;
            return this;
        }

        public Builder documents(List<Document> documents) {
            this.documents = documents;
            return this;
        }

        public Builder parties(List<Party> parties) {
            this.parties = parties;
            return this;
        }

        public FilingPackage create() {
            return new FilingPackage(this);
        }
    }
}


