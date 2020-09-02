package ca.bc.gov.open.jag.efilingapi.submission;

import ca.bc.gov.open.clamav.starter.ClamAvService;
import ca.bc.gov.open.clamav.starter.VirusDetectedException;
import ca.bc.gov.open.jag.efilingapi.Keys;
import ca.bc.gov.open.jag.efilingapi.account.service.AccountService;
import ca.bc.gov.open.jag.efilingapi.api.SubmissionApiDelegate;
import ca.bc.gov.open.jag.efilingapi.api.model.*;
import ca.bc.gov.open.jag.efilingapi.config.NavigationProperties;
import ca.bc.gov.open.jag.efilingapi.document.DocumentStore;
import ca.bc.gov.open.jag.efilingapi.error.EfilingErrorBuilder;
import ca.bc.gov.open.jag.efilingapi.error.ErrorResponse;
import ca.bc.gov.open.jag.efilingapi.submission.mappers.FilingPackageMapper;
import ca.bc.gov.open.jag.efilingapi.submission.mappers.GenerateUrlResponseMapper;
import ca.bc.gov.open.jag.efilingapi.submission.mappers.SubmissionMapper;
import ca.bc.gov.open.jag.efilingapi.submission.models.Submission;
import ca.bc.gov.open.jag.efilingapi.submission.models.SubmissionKey;
import ca.bc.gov.open.jag.efilingapi.submission.service.SubmissionService;
import ca.bc.gov.open.jag.efilingapi.submission.service.SubmissionStore;
import ca.bc.gov.open.jag.efilingapi.utils.MdcUtils;
import ca.bc.gov.open.jag.efilingapi.utils.SecurityUtils;
import ca.bc.gov.open.jag.efilingapi.utils.TikaAnalysis;
import ca.bc.gov.open.jag.efilingcommons.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@EnableConfigurationProperties(NavigationProperties.class)
public class SubmissionApiDelegateImpl implements SubmissionApiDelegate {

    Logger logger = LoggerFactory.getLogger(SubmissionApiDelegateImpl.class);

    private final SubmissionService submissionService;
    private final SubmissionStore submissionStore;
    private final AccountService accountService;
    private final GenerateUrlResponseMapper generateUrlResponseMapper;
    private final SubmissionMapper submissionMapper;
    private final NavigationProperties navigationProperties;
    private final DocumentStore documentStore;
    private final ClamAvService clamAvService;
    private final FilingPackageMapper filingPackageMapper;

    public SubmissionApiDelegateImpl(
            SubmissionService submissionService,
            AccountService accountService,
            GenerateUrlResponseMapper generateUrlResponseMapper,
            NavigationProperties navigationProperties,
            SubmissionStore submissionStore, SubmissionMapper submissionMapper, DocumentStore documentStore, ClamAvService clamAvService, FilingPackageMapper filingPackageMapper) {
        this.submissionService = submissionService;
        this.accountService = accountService;
        this.generateUrlResponseMapper = generateUrlResponseMapper;
        this.navigationProperties = navigationProperties;
        this.submissionStore = submissionStore;
        this.submissionMapper = submissionMapper;
        this.documentStore = documentStore;
        this.clamAvService = clamAvService;
        this.filingPackageMapper = filingPackageMapper;
    }

    @Override
    @RolesAllowed("efiling-client")
    public ResponseEntity<UploadSubmissionDocumentsResponse> uploadSubmissionDocuments(UUID xTransactionId, String xUserId, List<MultipartFile> files) {

        Optional<UUID> universalId = SecurityUtils.stringToUUID(xUserId);

        if(!universalId.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.BAD_REQUEST);

        SubmissionKey submissionKey = new SubmissionKey(
                universalId.get(),
                xTransactionId,
                UUID.randomUUID());

        MdcUtils.setClientMDC(submissionKey, SecurityUtils.getClientId());

        logger.info("attempting to upload original document [{}]", submissionKey.getSubmissionId());

        if (!universalId.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        ResponseEntity response = storeDocuments(submissionKey, files);

        logger.info("successfully uploaded original document [{}]", submissionKey.getSubmissionId());

        MdcUtils.clearClientMDC();

        return response;

    }

    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<UploadSubmissionDocumentsResponse> uploadAdditionalSubmissionDocuments(UUID submissionId, UUID xTransactionId, List<MultipartFile> files) {


        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, submissionId);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());

        Optional<Submission> fromCacheSubmission = this.submissionStore.get(submissionKey.get());

        if (!fromCacheSubmission.isPresent())
            return ResponseEntity.notFound().build();

        logger.info("attempting to upload new document for transaction [{}]", submissionId);

        ResponseEntity responseEntity = storeDocuments(submissionKey.get(), files);

        logger.info("successfully uploaded new document for transaction [{}]", submissionId);

        MdcUtils.clearUserMDC();

        return responseEntity;
    }

    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<UpdateDocumentResponse> updateDocumentProperties(UUID submissionId, UUID
            xTransactionId, UpdateDocumentRequest updateDocumentRequest) {

        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, submissionId);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());

        logger.info("attempting to add new document for transaction [{}]", submissionId);

        if (updateDocumentRequest == null || updateDocumentRequest.getDocuments().isEmpty())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.DOCUMENT_REQUIRED).create(),
                    HttpStatus.BAD_REQUEST);

        Optional<Submission> fromCacheSubmission = this.submissionStore.get(submissionKey.get());

        if (!fromCacheSubmission.isPresent())
            return ResponseEntity.notFound().build();

        try {
            Submission submission = submissionService.updateDocuments(fromCacheSubmission.get(), updateDocumentRequest, submissionKey.get());
            UpdateDocumentResponse updateDocumentResponse = new UpdateDocumentResponse();
            FilingPackage filingPackage = filingPackageMapper.toApiFilingPackage(submission.getFilingPackage());
            updateDocumentResponse.setDocuments(filingPackage.getDocuments());
            logger.info("successfully added new document for transaction [{}]", submissionId);
            return ResponseEntity.ok(updateDocumentResponse);

        } catch (EfilingDocumentServiceException e) {
            logger.warn(e.getMessage(), e);
            return new ResponseEntity(buildEfilingError(ErrorResponse.DOCUMENT_TYPE_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);

        } finally {
            MdcUtils.clearUserMDC();
        }
    }

    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<Resource> getSubmissionDocument(UUID xTransactionId,
                                                          UUID submissionId,
                                                          String filename) {

        logger.info("getSubmission document for transaction [{}]", xTransactionId);

        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, submissionId);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());

        byte[] bytes = documentStore.get(submissionKey.get(), filename);

        if (bytes == null) return ResponseEntity.notFound().build();

        logger.info("successfully retrieved document for transaction [{}]", xTransactionId);

        MdcUtils.clearUserMDC();

        return ResponseEntity.ok(new ByteArrayResource(bytes));

    }

    @Override
    @RolesAllowed("efiling-client")
    public ResponseEntity<GenerateUrlResponse> generateUrl(UUID xTransactionId, String xUserId, UUID submissionId, GenerateUrlRequest generateUrlRequest) {

        logger.info("Attempting to generate Url Request Received");

        Optional<UUID> universalId = SecurityUtils.stringToUUID(xUserId);

        if (!universalId.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        SubmissionKey submissionKey = new SubmissionKey(universalId.get(), xTransactionId, submissionId);

        MdcUtils.setClientMDC(submissionKey, SecurityUtils.getClientId());

        ResponseEntity response;

        try {
            response = ResponseEntity.ok(
                    generateUrlResponseMapper.toGenerateUrlResponse(
                            submissionService.generateFromRequest(submissionKey, generateUrlRequest),
                            navigationProperties.getBaseUrl()));
            logger.info("successfully generated return url.");
        } catch (CSOHasMultipleAccountException e) {
            logger.warn(e.getMessage(), e);
            response = new ResponseEntity(buildEfilingError(ErrorResponse.ACCOUNT_EXCEPTION), HttpStatus.BAD_REQUEST);
        } catch (InvalidAccountStateException e) {
            logger.warn(e.getMessage(), e);
            response = new ResponseEntity(buildEfilingError(ErrorResponse.INVALID_ROLE), HttpStatus.FORBIDDEN);
        } catch (EfilingDocumentServiceException e) {
            logger.warn(e.getMessage(), e);
            response = new ResponseEntity(buildEfilingError(ErrorResponse.DOCUMENT_TYPE_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (StoreException e) {
            logger.warn(e.getMessage(), e);
            response = new ResponseEntity(buildEfilingError(ErrorResponse.CACHE_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        MdcUtils.clearClientMDC();

        return response;

    }

    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<GetSubmissionConfigResponse> getSubmissionConfig(UUID submissionId, UUID xTransactionId) {

        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, submissionId);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());

        logger.info("attempting to get Submission for transactionId [{}]", xTransactionId);

        Optional<Submission> fromCacheSubmission = this.submissionStore.get(submissionKey.get());

        if (!fromCacheSubmission.isPresent())
            return ResponseEntity.notFound().build();

        logger.info("Successfully retrieved submission for transactionId [{}]", xTransactionId);

        MdcUtils.clearUserMDC();

        return ResponseEntity.ok(submissionMapper.toGetSubmissionConfigResponse(fromCacheSubmission.get(), navigationProperties.getCsoBaseUrl()));

    }

    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<FilingPackage> getSubmissionFilingPackage(UUID xTransactionId, UUID submissionId) {

        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, submissionId);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());

        logger.info("attempting to get Submission Filing Package for transactionId [{}]", xTransactionId);

        Optional<Submission> fromCacheSubmission = this.submissionStore.get(submissionKey.get());

        if (!fromCacheSubmission.isPresent())
            return ResponseEntity.notFound().build();

        logger.info("successfully retrieved submission filing package for transactionId [{}]", xTransactionId);

        MdcUtils.clearUserMDC();

        return ResponseEntity.ok(filingPackageMapper.toApiFilingPackage(fromCacheSubmission.get().getFilingPackage()));
    }


    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<Void> deleteSubmission(UUID submissionId, UUID xTransactionId) {

        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, submissionId);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());

        Optional<Submission> fromCacheSubmission = this.submissionStore.get(submissionKey.get());
        if(!fromCacheSubmission.isPresent())
            return ResponseEntity.notFound().build();
        //Remove documents from cache
        if (fromCacheSubmission.get().getFilingPackage() != null && fromCacheSubmission.get().getFilingPackage().getDocuments() != null)
            fromCacheSubmission.get().getFilingPackage().getDocuments().forEach(
                    document -> documentStore.evict(submissionKey.get(), document.getName()));

        //Remove submission from cache
        submissionStore.evict(submissionKey.get());
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<SubmitResponse> submit(UUID xTransactionId, UUID submissionId, Object body) {

        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, submissionId);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());

        logger.info("attempting to submit efiling package for transaction [{}]", xTransactionId);

        Optional<Submission> fromCacheSubmission = this.submissionStore.get(submissionKey.get());

        if (!fromCacheSubmission.isPresent())
            return ResponseEntity.notFound().build();
        ResponseEntity response;
        MDC.put(Keys.MDC_EFILING_SUBMISSION_ID, submissionId.toString());
        try {
            SubmitResponse result = submissionService.createSubmission(fromCacheSubmission.get(), accountService.getCsoAccountDetails(submissionKey.get().getUniversalId()));
            response = new ResponseEntity(result, HttpStatus.CREATED);
            logger.info("successfully submitted efiling package for transaction [{}], cso id {}", xTransactionId);

        } catch (EfilingSubmissionServiceException e) {
            logger.error("failed package submission {}", xTransactionId);
            response = new ResponseEntity(buildEfilingError(ErrorResponse.DOCUMENT_TYPE_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);

        }

        MdcUtils.clearUserMDC();

        return response;
    }

    public EfilingError buildEfilingError(ErrorResponse errorResponse) {

        EfilingError response = new EfilingError();
        response.setError(errorResponse.getErrorCode());
        response.setMessage(errorResponse.getErrorMessage());
        return response;

    }



    private ResponseEntity storeDocuments(SubmissionKey submissionKey, List<MultipartFile> files) {

        if (files == null || files.isEmpty())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.DOCUMENT_REQUIRED).create(),
                    HttpStatus.BAD_REQUEST);

        try {

            for (MultipartFile file : files) {
                try {
                    clamAvService.scan(new ByteArrayInputStream(file.getBytes()));
                } catch (VirusDetectedException e) {
                    return new ResponseEntity(EfilingErrorBuilder.builder().errorResponse(ErrorResponse.DOCUMENT_STORAGE_FAILURE).create(),
                            HttpStatus.BAD_GATEWAY);
                }

                if (!TikaAnalysis.isPdf(file))
                    return new ResponseEntity(EfilingErrorBuilder.builder().errorResponse(ErrorResponse.FILE_TYPE_ERROR).create(),
                            HttpStatus.BAD_REQUEST);
            }
            for (MultipartFile file : files) {
                documentStore.put(submissionKey, file.getResource().getFilename(), file.getBytes());
            }

        } catch (IOException e) {
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.DOCUMENT_STORAGE_FAILURE).create(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.info("{} stored in cache", files.size());

        return ResponseEntity.ok(new UploadSubmissionDocumentsResponse().submissionId(submissionKey.getSubmissionId()).received(new BigDecimal(files.size())));
    }

}
