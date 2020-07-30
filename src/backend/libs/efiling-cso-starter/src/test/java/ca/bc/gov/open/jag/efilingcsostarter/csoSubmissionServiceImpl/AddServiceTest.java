package ca.bc.gov.open.jag.efilingcsostarter.csoSubmissionServiceImpl;

import ca.bc.gov.ag.csows.filing.FilingFacadeBean;
import ca.bc.gov.ag.csows.services.NestedEjbException_Exception;
import ca.bc.gov.ag.csows.services.Service;
import ca.bc.gov.ag.csows.services.ServiceFacadeBean;
import ca.bc.gov.open.jag.efilingcommons.exceptions.EfilingSubmissionServiceException;
import ca.bc.gov.open.jag.efilingcommons.model.EfilingService;
import ca.bc.gov.open.jag.efilingcsostarter.CsoSubmissionServiceImpl;
import ca.bc.gov.open.jag.efilingcsostarter.TestHelpers;
import ca.bc.gov.open.jag.efilingcsostarter.mappers.ServiceMapperImpl;
import org.joda.time.DateTime;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Efiling Submission Test")
public class AddServiceTest {

    CsoSubmissionServiceImpl sut;

    @Mock
    FilingFacadeBean filingFacadeBeanMock;

    @Mock
    ServiceFacadeBean serviceFacadeBean;

    @BeforeEach
    public void init() {

        MockitoAnnotations.initMocks(this);

        sut = new CsoSubmissionServiceImpl(filingFacadeBeanMock, serviceFacadeBean, new ServiceMapperImpl());

    }

    @DisplayName("Exception: with null service should throw IllegalArgumentException")
    @Test
    public void testWithEmptyServiceId() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.addService(null));
    }

    @DisplayName("OK: addService called with any non-empty service")
    @Test
    public void testWithPopulatedSubmissionId() throws NestedEjbException_Exception, DatatypeConfigurationException {
        Mockito.when(serviceFacadeBean.addService(any())).thenReturn(createService());
        EfilingService actual = sut.addService(TestHelpers.createBaseEfilingService());
        Assertions.assertEquals(BigDecimal.TEN, actual.getAccountId());
        Assertions.assertEquals(BigDecimal.TEN, actual.getClientId());
        Assertions.assertEquals(TestHelpers.CLIENT_REFERENCE_TXT, actual.getClientReferenceTxt());
        Assertions.assertEquals(TestHelpers.COURT_FILE_NUMBER, actual.getCourtFileNumber());
        Assertions.assertEquals(TestHelpers.DOCUMENTS_PROCESSED, actual.getDocumentsProcessed());
        Assertions.assertEquals(2018, actual.getEntryDateTime().getYear());
        Assertions.assertEquals("10", actual.getEntryUserId());
        Assertions.assertEquals(BigDecimal.TEN, actual.getServiceId());
        Assertions.assertEquals(2018, actual.getServiceReceivedDateTime().getYear());
        Assertions.assertEquals(TestHelpers.SERVICE_RECEIVED_DTM_TEXT, actual.getServiceReceivedDtmText());
        Assertions.assertEquals(BigDecimal.TEN, actual.getServiceSessionId());
        Assertions.assertEquals(TestHelpers.SERVICE_SUBTYPE_CD, actual.getServiceSubtypeCd());
        Assertions.assertEquals(TestHelpers.SERVICE_TYPE_CD, actual.getServiceTypeCd());
        Assertions.assertEquals(TestHelpers.SERVICE_TYPE_DESC, actual.getServiceTypeDesc());
    }
    @DisplayName("Exception: with NestedEjbException_Exception should throw EfilingLookupServiceException")
    @Test
    public void whenNestedEjbException_ExceptionShouldThrowEfilingSubmissionServiceException() throws ca.bc.gov.ag.csows.filing.NestedEjbException_Exception, NestedEjbException_Exception {
        Mockito.when(serviceFacadeBean.addService(any())).thenThrow(new ca.bc.gov.ag.csows.services.NestedEjbException_Exception());
        Assertions.assertThrows(EfilingSubmissionServiceException.class, () -> sut.addService(TestHelpers.createBaseEfilingService()));
    }

    private Service createService() throws DatatypeConfigurationException {
        Service service = new Service();
        service.setAccountId(BigDecimal.TEN);
        service.setClientId(BigDecimal.TEN);
        service.setClientReferenceTxt(TestHelpers.CLIENT_REFERENCE_TXT);
        service.setCourtFileNo(TestHelpers.COURT_FILE_NUMBER);
        service.setDocumentsProcessed(TestHelpers.DOCUMENTS_PROCESSED);
        service.setEntDtm(TestHelpers.getXmlDate(TestHelpers.DATE));
        service.setEntUserId("10");
        service.setServiceId(BigDecimal.TEN);
        service.setServiceReceivedDtm(TestHelpers.getXmlDate(TestHelpers.DATE));
        service.setServiceReceivedDtmText(TestHelpers.SERVICE_RECEIVED_DTM_TEXT);
        service.setServiceSessionId(BigDecimal.TEN);
        service.setServiceSubtypeCd(TestHelpers.SERVICE_SUBTYPE_CD);
        service.setServiceTypeCd(TestHelpers.SERVICE_TYPE_CD);
        service.setServiceTypeDesc(TestHelpers.SERVICE_TYPE_DESC);
        return service;
    }
}
