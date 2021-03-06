package ca.bc.gov.open.jag.efilingcsoclient;


import ca.bc.gov.ag.csows.filing.status.FilingStatus;
import ca.bc.gov.ag.csows.filing.status.FilingStatusFacadeBean;
import ca.bc.gov.ag.csows.filing.status.NestedEjbException_Exception;
import ca.bc.gov.open.jag.efilingcommons.exceptions.EfilingStatusServiceException;
import ca.bc.gov.open.jag.efilingcommons.submission.EfilingStatusService;
import ca.bc.gov.open.jag.efilingcommons.submission.models.FilingPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

public class CsoStatusServiceImpl implements EfilingStatusService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FilingStatusFacadeBean filingStatusFacadeBean;

    public CsoStatusServiceImpl(FilingStatusFacadeBean filingStatusFacadeBean) {

        this.filingStatusFacadeBean = filingStatusFacadeBean;

    }

    @Override
    public Optional<FilingPackage> findStatusByPackage(BigDecimal clientId, BigDecimal packageNo) {

        try {

            logger.info("Calling soap service");

            FilingStatus filingStatus = filingStatusFacadeBean
                    .findStatusBySearchCriteria(null,null,null,null, null,null,packageNo, clientId,null,null,null,null,BigDecimal.ONE,null);

            if (filingStatus.getFilePackages().isEmpty()) return Optional.empty();

            FilingPackage result = FilingPackage.builder().applicationCode("test").create();

            return  Optional.of(result);

        } catch (NestedEjbException_Exception e) {

            throw new EfilingStatusServiceException("Exception while finding status", e.getCause());

        }

    }
}
