package ca.bc.gov.open.jag.efilingapi.submission.mappers;

import ca.bc.gov.open.jag.efilingapi.api.model.GenerateUrlRequest;
import ca.bc.gov.open.jag.efilingapi.api.model.GetSubmissionConfigResponse;
import ca.bc.gov.open.jag.efilingapi.submission.models.Submission;
import ca.bc.gov.open.jag.efilingapi.submission.models.SubmissionKey;
import ca.bc.gov.open.jag.efilingcommons.model.FilingPackage;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
uses = { FilingPackageMapper.class })
public interface SubmissionMapper {

    @Mapping(source = "submissionKey", target = "submissionKey")
    @Mapping(source = "generateUrlRequest.navigation", target = "navigation")
    @Mapping(source = "generateUrlRequest.clientAppName", target = "clientAppName")
    @Mapping(source = "filingPackage", target = "filingPackage")
    @Mapping(source = "expiryDate", target = "expiryDate")
    Submission toSubmission(
            SubmissionKey submissionKey,
            GenerateUrlRequest generateUrlRequest,
            FilingPackage filingPackage,
            long expiryDate,
            boolean rushedSubmission);


    GetSubmissionConfigResponse toGetSubmissionConfigResponse(Submission submission, String csoBaseUrl);

}
