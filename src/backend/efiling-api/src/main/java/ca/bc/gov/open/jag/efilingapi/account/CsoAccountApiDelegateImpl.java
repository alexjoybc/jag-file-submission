package ca.bc.gov.open.jag.efilingapi.account;

import ca.bc.gov.open.jag.efilingapi.account.mappers.CsoAccountMapper;
import ca.bc.gov.open.jag.efilingapi.account.service.AccountService;
import ca.bc.gov.open.jag.efilingapi.api.CsoAccountApiDelegate;
import ca.bc.gov.open.jag.efilingapi.api.model.CreateCsoAccountRequest;
import ca.bc.gov.open.jag.efilingapi.api.model.CsoAccount;
import ca.bc.gov.open.jag.efilingapi.api.model.CsoAccountUpdateRequest;
import ca.bc.gov.open.jag.efilingapi.api.model.EfilingError;
import ca.bc.gov.open.jag.efilingapi.error.EfilingErrorBuilder;
import ca.bc.gov.open.jag.efilingapi.error.ErrorResponse;
import ca.bc.gov.open.jag.efilingapi.submission.SubmissionApiDelegateImpl;
import ca.bc.gov.open.jag.efilingapi.submission.models.SubmissionKey;
import ca.bc.gov.open.jag.efilingapi.utils.MdcUtils;
import ca.bc.gov.open.jag.efilingapi.utils.SecurityUtils;
import ca.bc.gov.open.jag.efilingcommons.exceptions.EfilingAccountServiceException;
import ca.bc.gov.open.jag.efilingcommons.model.AccountDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import java.util.Optional;
import java.util.UUID;

/**
 * Suite of services to manage cso account
 */
@Service
public class CsoAccountApiDelegateImpl implements CsoAccountApiDelegate {

    Logger logger = LoggerFactory.getLogger(SubmissionApiDelegateImpl.class);

    private final AccountService accountService;
    private final CsoAccountMapper csoAccountMapper;


    public CsoAccountApiDelegateImpl(AccountService accountService, CsoAccountMapper csoAccountMapper) {
        this.accountService = accountService;
        this.csoAccountMapper = csoAccountMapper;
    }


    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<CsoAccount> createAccount(UUID xTransactionId, CreateCsoAccountRequest createAccountRequest) {

        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, null);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());

        try {

            logger.debug("attempting to create a cso account");
            
            AccountDetails accountDetails = accountService.createAccount(submissionKey.get().getUniversalId(), createAccountRequest);

            logger.info("Account successfully created");

            return new ResponseEntity<>(csoAccountMapper.toCsoAccount(accountDetails), HttpStatus.CREATED);

        } catch (EfilingAccountServiceException e) {

            logger.error("Exception while trying to create a CSO Account", e);

            EfilingError response = new EfilingError();
            response.setError(ErrorResponse.CREATE_ACCOUNT_EXCEPTION.getErrorCode());
            response.setMessage(ErrorResponse.CREATE_ACCOUNT_EXCEPTION.getErrorMessage());
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);

        } finally {
            MdcUtils.clearUserMDC();
        }

    }

    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<CsoAccount> getCsoAccount(UUID xTransactionId) {

        logger.debug("Attempting to get CSO account details");

        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, null);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());


        AccountDetails accountDetails = accountService.getCsoAccountDetails(submissionKey.get().getUniversalId());

        if(accountDetails == null) {
            logger.debug("User account not found");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        logger.debug("Successfully retrieved account");

        MdcUtils.clearUserMDC();

        return ResponseEntity.ok(csoAccountMapper.toCsoAccount(accountDetails));

    }


    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<CsoAccount> updateCsoAccount(UUID xTransactionId, CsoAccountUpdateRequest clientUpdateRequest) {

        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, null);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());

        ResponseEntity response;

        try {

            AccountDetails accountDetails = accountService.getCsoAccountDetails(submissionKey.get().getUniversalId());

            if(accountDetails == null) {
                return new ResponseEntity(
                        EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                        HttpStatus.FORBIDDEN);
            }

            accountDetails.updateInternalClientNumber(clientUpdateRequest.getInternalClientNumber());

            accountService.updateClient(accountDetails);

            response = ResponseEntity.ok(csoAccountMapper.toCsoAccount(accountDetails));
        } catch (EfilingAccountServiceException e) {
            logger.warn(e.getMessage(), e);
            response = new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.UPDATE_CLIENT_EXCEPTION).create(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        MdcUtils.clearUserMDC();

        return response;
    }

}
