package ca.bc.gov.open.jag.efilingcsoclient.csoStatusServiceImpl;

import ca.bc.gov.ag.csows.filing.status.FilePackage;
import ca.bc.gov.ag.csows.filing.status.FilingStatus;
import ca.bc.gov.ag.csows.filing.status.FilingStatusFacadeBean;
import ca.bc.gov.ag.csows.filing.status.NestedEjbException_Exception;
import ca.bc.gov.open.jag.efilingcommons.exceptions.EfilingStatusServiceException;
import ca.bc.gov.open.jag.efilingcommons.submission.models.FilingPackage;
import ca.bc.gov.open.jag.efilingcommons.utils.DateUtils;
import ca.bc.gov.open.jag.efilingcsoclient.CsoStatusServiceImpl;
import org.joda.time.DateTime;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.xml.datatype.DatatypeConfigurationException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Find Package Test Suite")
public class FindPackageByIdTest {
    public static final String CLIENT_FILE_NO = "CLIENTFILENO";
    public static final String COURT_CLASS_CD = "CLASSCD";
    public static final String COURT_FILE_NO = "FILENO";
    public static final String COURT_LEVEL_CD = "LEVELCD";
    public static final String COURT_LOCATION_CD = "LOCATIONCD";
    public static final String COURT_LOCATION_NAME = "LOCATIONAME";
    public static final String FILING_COMMENTS_TXT = "COMMENTSTXT";
    public static final String FIRST_NAME = "FIRSTNAME";
    public static final String LAST_NAME = "LASTNAME";
    public static final String PACKAGE_NO = "PACKAGENO";
    public static final DateTime SUBMITED_DATE = new DateTime(2020, 12, 12, 1, 1);
    @Mock
    FilingStatusFacadeBean filingStatusFacadeBean;

    private final BigDecimal SUCCESS_CLIENT = BigDecimal.ONE;
    private final BigDecimal SUCCESS_PACKAGE = BigDecimal.ONE;

    private final BigDecimal EXCEPTION_CLIENT = BigDecimal.TEN;
    private final BigDecimal EXCEPTION_PACKAGE = BigDecimal.TEN;

    private final BigDecimal NOTFOUND_CLIENT = BigDecimal.ZERO;
    private final BigDecimal NOTFOUND_PACKAGE = BigDecimal.ZERO;

    private static CsoStatusServiceImpl sut;

    @BeforeAll
    public void beforeAll() throws NestedEjbException_Exception, DatatypeConfigurationException {

        MockitoAnnotations.openMocks(this);

        FilingStatus filingStatus =  createFilingStatus();
        filingStatus.getFilePackages().add(createFilePackage());

        Mockito.when(filingStatusFacadeBean.findStatusBySearchCriteria(any(), any(), any(), any(), any(), any(), ArgumentMatchers.eq(SUCCESS_PACKAGE), ArgumentMatchers.eq(SUCCESS_CLIENT), any(), any(), any(), any(), any(), any())).thenReturn(filingStatus);

        Mockito.when(filingStatusFacadeBean.findStatusBySearchCriteria(any(), any(), any(), any(), any(), any(), ArgumentMatchers.eq(NOTFOUND_PACKAGE), ArgumentMatchers.eq(NOTFOUND_CLIENT), any(), any(), any(), any(), any(), any())).thenReturn(createFilingStatus());

        Mockito.when(filingStatusFacadeBean.findStatusBySearchCriteria(any(), any(), any(), any(), any(), any(), ArgumentMatchers.eq(EXCEPTION_PACKAGE), ArgumentMatchers.eq(EXCEPTION_CLIENT), any(), any(), any(), any(), any(), any())).thenThrow(new NestedEjbException_Exception());

        sut = new CsoStatusServiceImpl(filingStatusFacadeBean);
    }

    @DisplayName("OK: package found")
    @Test
    public void testWithFoundResult() throws DatatypeConfigurationException {
        Optional<FilingPackage> result = sut.findStatusByPackage(SUCCESS_CLIENT, SUCCESS_PACKAGE);

        Assertions.assertEquals("test", result.get().getApplicationCode());

    }

    @DisplayName("Ok: no packages found")
    @Test
    public void testWithNoResult() {
        Optional<FilingPackage> result = sut.findStatusByPackage(NOTFOUND_CLIENT, NOTFOUND_PACKAGE);

        Assertions.assertFalse(result.isPresent());

    }

    @DisplayName("Exception: filing status facade throws an exception")
    @Test
    public void testWithException() {
        Assertions.assertThrows(EfilingStatusServiceException.class, () -> sut.findStatusByPackage(EXCEPTION_CLIENT, EXCEPTION_PACKAGE));
    }

    private FilingStatus createFilingStatus() {
        FilingStatus filingStatus = new FilingStatus();
        filingStatus.setRecordsFrom(BigDecimal.ONE);
        filingStatus.setRecordsTo(BigDecimal.ONE);
        filingStatus.setTotalRecords(BigDecimal.ONE);
        return filingStatus;
    }

    private FilePackage createFilePackage() throws DatatypeConfigurationException {
        FilePackage filePackage = new FilePackage();
        filePackage.setClientFileNo(CLIENT_FILE_NO);
        filePackage.setCourtClassCd(COURT_CLASS_CD);
        filePackage.setCourtFileNo(COURT_FILE_NO);
        filePackage.setCourtLevelCd(COURT_LEVEL_CD);
        filePackage.setCourtLocationCd(COURT_LOCATION_CD);
        filePackage.setCourtLocationId(BigDecimal.ONE);
        filePackage.setCourtLocationName(COURT_LOCATION_NAME);
        filePackage.setExistingCourtFileYN(true);
        filePackage.setFilingCommentsTxt(FILING_COMMENTS_TXT);
        filePackage.setFirstName(FIRST_NAME);
        filePackage.setHasChecklist(true);
        filePackage.setHasRegistryNotice(true);
        filePackage.setLastName(LAST_NAME);
        filePackage.setPackageNo(PACKAGE_NO);
        filePackage.setSubmittedDate(DateUtils.getXmlDate(SUBMITED_DATE));
        return filePackage;
    }
}
