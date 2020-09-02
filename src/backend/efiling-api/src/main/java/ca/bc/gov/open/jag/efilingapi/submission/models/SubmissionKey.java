package ca.bc.gov.open.jag.efilingapi.submission.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class SubmissionKey {

    private UUID universalId;
    private UUID transactionId;
    private UUID submissionId;

    @JsonCreator
    public SubmissionKey(
            @JsonProperty("universalId") UUID universalId,
            @JsonProperty("transactionId") UUID transactionId,
            @JsonProperty("submissionId") UUID submissionId) {
        this.universalId = universalId;
        this.transactionId = transactionId;
        this.submissionId = submissionId;
    }

    public UUID getUniversalId() {
        return universalId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }
}
