package ca.bc.gov.open.jag.ceis;

import ca.bc.gov.open.jag.efilingceisapiclient.api.DefaultApi;
import ca.bc.gov.open.jag.efilingceisapiclient.api.handler.ApiException;
import ca.bc.gov.open.jag.efilingcommons.court.EfilingCourtLocationService;
import ca.bc.gov.open.jag.efilingcommons.model.InternalCourtLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CeisCourtLocationServiceImpl implements EfilingCourtLocationService {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final DefaultApi defaultApi;
    private final CeisCourtLocationMapper ceisCourtLocationMapper;

    public CeisCourtLocationServiceImpl(
            DefaultApi defaultApi,
            CeisCourtLocationMapper courtLocationMapper) {
        this.defaultApi = defaultApi;
        this.ceisCourtLocationMapper = courtLocationMapper;
    }

    @Override
    public List<InternalCourtLocation> getCourtLocations(String courtType) {

        try {
            List<InternalCourtLocation> courtLocationList = defaultApi.courtLocationsGet().getCourtlocations().stream()
                    .filter(courtLocation -> isSearchedType(courtLocation.getIssupremecourt(), courtLocation.getIsprovincialcourt(), courtType))
                    .map(ceisCourtLocationMapper::toCourtLocation)
                    .collect(Collectors.toList());

            return courtLocationList;
        } catch (ApiException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

    }

    private boolean isSearchedType(String isSupreme, String isProvincial, String courtType) {

        if (StringUtils.isBlank(courtType)) return true;
        if (courtType.equals("S") && StringUtils.equals(isSupreme, "Y")) return true;
        if (courtType.equals("P") && StringUtils.equals(isProvincial, "Y")) return true;

        return false;
    }
}
