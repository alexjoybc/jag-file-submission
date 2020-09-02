package ca.bc.gov.open.jag.efilingapi.account;

import ca.bc.gov.open.bceid.starter.account.BCeIDAccountService;
import ca.bc.gov.open.bceid.starter.account.GetAccountRequest;
import ca.bc.gov.open.bceid.starter.account.models.IndividualIdentity;
import ca.bc.gov.open.jag.efilingapi.account.mappers.BceidAccountMapper;
import ca.bc.gov.open.jag.efilingapi.api.BceidAccountApiDelegate;
import ca.bc.gov.open.jag.efilingapi.api.model.BceidAccount;
import ca.bc.gov.open.jag.efilingapi.error.EfilingErrorBuilder;
import ca.bc.gov.open.jag.efilingapi.error.ErrorResponse;
import ca.bc.gov.open.jag.efilingapi.submission.models.SubmissionKey;
import ca.bc.gov.open.jag.efilingapi.utils.MdcUtils;
import ca.bc.gov.open.jag.efilingapi.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import java.util.Optional;
import java.util.UUID;

@Service
public class BceidAccountApiDelagateImpl implements BceidAccountApiDelegate {

    Logger logger = LoggerFactory.getLogger(BceidAccountApiDelagateImpl.class);

    private final BCeIDAccountService bCeIDAccountService;
    private final BceidAccountMapper bceidAccountMapper;

    public BceidAccountApiDelagateImpl(BCeIDAccountService bCeIDAccountService, BceidAccountMapper bceidAccountMapper) {
        this.bCeIDAccountService = bCeIDAccountService;
        this.bceidAccountMapper = bceidAccountMapper;
    }


    @Override
    @RolesAllowed("efiling-user")
    public ResponseEntity<BceidAccount> getBceidAccount(UUID xTransactionId) {

        logger.debug("Attempting to retrieved bceid information");

        Optional<SubmissionKey> submissionKey = SecurityUtils.getSubmissionKey(xTransactionId, null);

        if(!submissionKey.isPresent())
            return new ResponseEntity(
                    EfilingErrorBuilder.builder().errorResponse(ErrorResponse.MISSING_UNIVERSAL_ID).create(),
                    HttpStatus.FORBIDDEN);

        MdcUtils.setUserMDC(submissionKey.get());

        Optional<IndividualIdentity> individualIdentity = bCeIDAccountService.getIndividualIdentity(
                GetAccountRequest.IndividualSelfRequest(submissionKey.get().getUniversalId().toString().replace("-", "").toUpperCase()));

        if(!individualIdentity.isPresent()) {
            logger.warn("BCEID user not found");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        logger.info("Successfully found user info");

        MdcUtils.clearUserMDC();

        return ResponseEntity.ok(bceidAccountMapper.toBceidAccount(individualIdentity.get()));

    }
}
