package ca.bc.gov.open.jag.efilingapi.fakes;

import ca.bc.gov.open.jag.efilingapi.court.models.GetCourtDetailsRequest;
import ca.bc.gov.open.jag.efilingapi.court.models.IsValidCourtFileNumberRequest;
import ca.bc.gov.open.jag.efilingapi.court.models.IsValidCourtRequest;
import ca.bc.gov.open.jag.efilingapi.court.services.CourtService;
import ca.bc.gov.open.jag.efilingcommons.model.CourtDetails;

public class CourtServiceFake implements CourtService {
    @Override
    public boolean isValidCourt(IsValidCourtRequest isValidCourtRequest) {
        return false;
    }

    @Override
    public CourtDetails getCourtDetails(GetCourtDetailsRequest getCourtDetailsRequest) {
        return null;
    }

    @Override
    public boolean isValidCourtFileNumber(IsValidCourtFileNumberRequest isValidCourtFileNumberRequest) {
        return false;
    }
}
