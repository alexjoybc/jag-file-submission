package ca.bc.gov.open.jag.efilingapi.submission.models;

import ca.bc.gov.open.jag.efilingapi.api.model.Navigation;
import ca.bc.gov.open.jag.efilingcommons.model.FilingPackage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the submission details for a transaction
 */
public class Submission {

    private SubmissionKey submissionKey;

    private long expiryDate;

    private Navigation navigation;

    private String clientAppName;

    private FilingPackage filingPackage;

    protected Submission(Submission.Builder builder) {
        this.submissionKey = builder.submissionKey;
        this.filingPackage = builder.filingPackage;
        this.navigation = builder.navigation;
        this.clientAppName = builder.clientAppName;
        this.expiryDate = builder.expiryDate;
    }

    public static Submission.Builder builder() {
        return new Submission.Builder();
    }

    @JsonCreator
    public Submission(
            @JsonProperty("submissionKey") SubmissionKey submissionKey,
            @JsonProperty("package") FilingPackage filingPackage,
            @JsonProperty("navigation") Navigation navigation,
            @JsonProperty("clientAppName") String clientAppName,
            @JsonProperty("expiryDate") long expiryDate) {
        this.submissionKey = submissionKey;
        this.filingPackage = filingPackage;
        this.navigation = navigation;
        this.clientAppName = clientAppName;
        this.expiryDate = expiryDate;
    }

    public SubmissionKey getSubmissionKey() { return submissionKey; }

    public FilingPackage getFilingPackage() {
        return filingPackage;
    }

    public Navigation getNavigation() {
        return navigation;
    }

    public String getClientAppName() { return clientAppName; }

    public long getExpiryDate() {
        return expiryDate;
    }

    public static class Builder {

        private SubmissionKey submissionKey;
        private FilingPackage filingPackage;
        private Navigation navigation;
        private String clientAppName;
        private long expiryDate;

        public Builder submissionKey(SubmissionKey submissionKey) {
            this.submissionKey = submissionKey;
            return this;
        }

        public Builder filingPackage(FilingPackage filingPackage) {
            this.filingPackage =  filingPackage;
            return this;
        }

        public Builder navigation(Navigation navigation) {
            this.navigation = navigation;
            return this;
        }

        public Builder clientAppName(String clientAppName) {
            this.clientAppName = clientAppName;
            return this;
        }

        public Builder expiryDate(long expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public Submission create() {
            return new Submission(this);
        }
    }

}
