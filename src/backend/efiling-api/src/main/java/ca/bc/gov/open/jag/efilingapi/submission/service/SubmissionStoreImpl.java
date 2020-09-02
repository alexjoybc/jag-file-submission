package ca.bc.gov.open.jag.efilingapi.submission.service;

import ca.bc.gov.open.jag.efilingapi.submission.models.SubmissionKey;
import ca.bc.gov.open.jag.efilingapi.submission.models.Submission;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

/**
 * Implementation of SubmissionStore using Submission StorageService
 */
public class SubmissionStoreImpl implements SubmissionStore {


    public static final String STORE_KEY = "{ #submissionKey.universalId, #submissionKey.submissionId, #submissionKey.transactionId }";
    public static final String SUBMISSION = "submission";
    public static final String SUBMISSION_CACHE_MANAGER = "submissionCacheManager";

    @Override
    @CachePut(cacheNames = SUBMISSION, key = STORE_KEY, cacheManager = SUBMISSION_CACHE_MANAGER)
    public Optional<Submission> put(SubmissionKey submissionKey, Submission submission) {
        return Optional.of(submission);
    }

    @Override
    @Cacheable(cacheNames = SUBMISSION, key = STORE_KEY, cacheManager = SUBMISSION_CACHE_MANAGER, unless="#result == null")
    public Optional<Submission> get(SubmissionKey submissionKey) {
        return Optional.empty();
    }

    @Override
    @CacheEvict(cacheNames = SUBMISSION, key = STORE_KEY, cacheManager = SUBMISSION_CACHE_MANAGER)
    public void evict(SubmissionKey submissionKey) {
        //This implements Redis delete no code required
    }

}
