package ca.bc.gov.open.jag.efilingapi.submission.service;

import ca.bc.gov.open.jag.efilingapi.submission.models.SubmissionKey;
import ca.bc.gov.open.jag.efilingapi.submission.models.Submission;

import java.util.Optional;

/**
 * A service to manage persistence of submissions objects
 */
public interface SubmissionStore {

    Optional<Submission> put(SubmissionKey submissionKey, Submission submission);

    Optional<Submission> get(SubmissionKey submissionKey);

    void evict(SubmissionKey submissionKey);

}
