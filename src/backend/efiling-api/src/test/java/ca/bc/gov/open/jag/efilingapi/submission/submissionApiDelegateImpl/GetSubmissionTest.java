package ca.bc.gov.open.jag.efilingapi.submission.submissionApiDelegateImpl;

import ca.bc.gov.open.clamav.starter.ClamAvService;
import ca.bc.gov.open.jag.efilingapi.Keys;
import ca.bc.gov.open.jag.efilingapi.TestHelpers;
import ca.bc.gov.open.jag.efilingapi.account.service.AccountService;
import ca.bc.gov.open.jag.efilingapi.api.model.Account;
import ca.bc.gov.open.jag.efilingapi.api.model.EfilingError;
import ca.bc.gov.open.jag.efilingapi.api.model.GetSubmissionResponse;
import ca.bc.gov.open.jag.efilingapi.config.NavigationProperties;
import ca.bc.gov.open.jag.efilingapi.document.DocumentStore;
import ca.bc.gov.open.jag.efilingapi.submission.SubmissionApiDelegateImpl;
import ca.bc.gov.open.jag.efilingapi.submission.mappers.GenerateUrlResponseMapper;
import ca.bc.gov.open.jag.efilingapi.submission.models.Submission;
import ca.bc.gov.open.jag.efilingapi.submission.service.SubmissionService;
import ca.bc.gov.open.jag.efilingapi.submission.service.SubmissionStore;
import ca.bc.gov.open.jag.efilingcommons.model.AccountDetails;
import ca.bc.gov.open.jag.efilingcommons.model.ServiceFees;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("SubmissionApiDelegateImpl test suite")
public class GetSubmissionTest {

    private static final String EMAIL = "email";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String MIDDLE_NAME = "middleName";
    private static final String SERVICE_TYPE_CD = "DCFL";
    private static final String SERVICE_TYPE_CD1 = "NOTDCFL";
    private static final String INTERNAL_CLIENT_NUMBER = "123";

    private SubmissionApiDelegateImpl sut;

    @Mock
    private SubmissionService submissionServiceMock;

    @Mock
    private GenerateUrlResponseMapper generateUrlResponseMapperMock;

    @Mock
    private SubmissionStore submissionStoreMock;

    @Mock
    private DocumentStore documentStoreMock;

    @Mock
    private SecurityContext securityContextMock;

    @Mock
    private Authentication authenticationMock;

    @Mock
    private KeycloakPrincipal keycloakPrincipalMock;

    @Mock
    private KeycloakSecurityContext keycloakSecurityContextMock;

    @Mock
    private AccessToken tokenMock;

    @Mock
    private AccountService accountServiceMock;

    @Mock
    private ClamAvService clamAvServiceMock;

    @BeforeAll
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(keycloakPrincipalMock);
        Mockito.when(keycloakPrincipalMock.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContextMock);
        Mockito.when(keycloakSecurityContextMock.getToken()).thenReturn(tokenMock);

        SecurityContextHolder.setContext(securityContextMock);

        NavigationProperties navigationProperties = new NavigationProperties();
        navigationProperties.setBaseUrl("http://localhost");
        Submission submissionWithCsoAccount = Submission
                .builder()
                .navigation(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL))
                .clientApplication(TestHelpers.createClientApplication(TestHelpers.DESCRIPTION, TestHelpers.TYPE))
                .create();

        Mockito.when(submissionStoreMock.get(Mockito.eq(TestHelpers.CASE_2), Mockito.any())).thenReturn(Optional.of(submissionWithCsoAccount));

        Submission submissionWithoutCsoAccount = Submission
                .builder()
                .accountDetails(AccountDetails.builder()
                        .accountId(null)
                        .clientId(null)
                        .create()
                )
                .navigation(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL))
                .create();

        Mockito.when(submissionStoreMock.get(Mockito.eq(TestHelpers.CASE_3), Mockito.any())).thenReturn(Optional.of(submissionWithoutCsoAccount));

        Submission submissionWithoutAccountDetails = Submission
                .builder()
                .navigation(TestHelpers.createNavigation(TestHelpers.SUCCESS_URL, TestHelpers.CANCEL_URL, TestHelpers.ERROR_URL))
                .create();

        Mockito.when(submissionStoreMock.get(Mockito.eq(TestHelpers.CASE_4), Mockito.any())).thenReturn(Optional.of(submissionWithoutCsoAccount));


        Mockito.when(accountServiceMock.getCsoAccountDetails(Mockito.eq(TestHelpers.CASE_2)))
                .thenReturn(AccountDetails
                        .builder()
                        .clientId(BigDecimal.TEN)
                        .internalClientNumber(INTERNAL_CLIENT_NUMBER)
                        .universalId(TestHelpers.CASE_2)
                        .email(EMAIL + TestHelpers.CASE_2)
                        .firstName(FIRST_NAME + TestHelpers.CASE_2)
                        .accountId(BigDecimal.TEN)
                        .fileRolePresent(true)
                        .cardRegistered(true)
                        .create());

        sut = new SubmissionApiDelegateImpl(submissionServiceMock, accountServiceMock, generateUrlResponseMapperMock, navigationProperties, submissionStoreMock, documentStoreMock, clamAvServiceMock, filingPackageMapper);

    }

    @Test
    @DisplayName("404: With null redis storage response return NotFound")
    public void withNullRedisStorageResponseReturnNotFound() {

        Map<String, Object> otherClaims = new HashMap<>();
        otherClaims.put(Keys.UNIVERSAL_ID_CLAIM_KEY, UUID.randomUUID());
        Mockito.when(tokenMock.getOtherClaims()).thenReturn(otherClaims);

        ResponseEntity<GetSubmissionResponse> actual = sut.getSubmission(UUID.randomUUID(), TestHelpers.CASE_1);
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    @DisplayName("200: With user having cso account and efiling role return submission details")
    public void withUserHavingCsoAccountShouldReturnUserDetailsAndAccount() {

        Map<String, Object> otherClaims = new HashMap<>();
        otherClaims.put(Keys.UNIVERSAL_ID_CLAIM_KEY, TestHelpers.CASE_2);
        Mockito.when(tokenMock.getOtherClaims()).thenReturn(otherClaims);

        ResponseEntity<GetSubmissionResponse> actual = sut.getSubmission( TestHelpers.CASE_2, TestHelpers.CASE_2);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(TestHelpers.CASE_2, actual.getBody().getUserDetails().getUniversalId());
        assertEquals(INTERNAL_CLIENT_NUMBER, actual.getBody().getUserDetails().getInternalClientNumber());
        assertEquals(1, actual.getBody().getUserDetails().getAccounts().size());
        assertEquals(Account.TypeEnum.CSO, actual.getBody().getUserDetails().getAccounts().stream().findFirst().get().getType());
        assertEquals("10", actual.getBody().getUserDetails().getAccounts().stream().findFirst().get().getIdentifier());
        assertEquals(TestHelpers.SUCCESS_URL, actual.getBody().getNavigation().getSuccess().getUrl());
        assertEquals(TestHelpers.CANCEL_URL, actual.getBody().getNavigation().getCancel().getUrl());
        assertEquals(TestHelpers.ERROR_URL, actual.getBody().getNavigation().getError().getUrl());
        assertEquals(TestHelpers.TYPE, actual.getBody().getClientApplication().getType());
        assertEquals(TestHelpers.DESCRIPTION, actual.getBody().getClientApplication().getDisplayName());

    }

    @Test
    @DisplayName("200: With user not having cso account")
    public void withUserHavingNoCsoAccountShouldReturnUserDetailsButNoAccount() {


        Map<String, Object> otherClaims = new HashMap<>();
        otherClaims.put(Keys.UNIVERSAL_ID_CLAIM_KEY, UUID.randomUUID());
        Mockito.when(tokenMock.getOtherClaims()).thenReturn(otherClaims);

        ResponseEntity<GetSubmissionResponse> actual = sut.getSubmission(TestHelpers.CASE_3, UUID.randomUUID());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertNull(actual.getBody().getUserDetails().getInternalClientNumber());
        assertNull(actual.getBody().getUserDetails().getAccounts());
        assertEquals(TestHelpers.SUCCESS_URL, actual.getBody().getNavigation().getSuccess().getUrl());
        assertEquals(TestHelpers.CANCEL_URL, actual.getBody().getNavigation().getCancel().getUrl());
        assertEquals(TestHelpers.ERROR_URL, actual.getBody().getNavigation().getError().getUrl());
    }

    @Test
    @DisplayName("200: With user not having account details present")
    public void withUserHavingNoAccountDetailsShouldReturnUserDetailsButNoAccount() {


        Map<String, Object> otherClaims = new HashMap<>();
        otherClaims.put(Keys.UNIVERSAL_ID_CLAIM_KEY, UUID.randomUUID());
        Mockito.when(tokenMock.getOtherClaims()).thenReturn(otherClaims);

        ResponseEntity<GetSubmissionResponse> actual = sut.getSubmission(TestHelpers.CASE_4, UUID.randomUUID());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertNull(actual.getBody().getUserDetails().getInternalClientNumber());
        assertNull(actual.getBody().getUserDetails().getAccounts());
        assertEquals(TestHelpers.SUCCESS_URL, actual.getBody().getNavigation().getSuccess().getUrl());
        assertEquals(TestHelpers.CANCEL_URL, actual.getBody().getNavigation().getCancel().getUrl());
        assertEquals(TestHelpers.ERROR_URL, actual.getBody().getNavigation().getError().getUrl());
    }

    @Test
    @DisplayName("403: With user not having universal id claim")
    public void withUserNotHavingUniversalIdShouldReturn403() {

        Map<String, Object> otherClaims = new HashMap<>();
        Mockito.when(tokenMock.getOtherClaims()).thenReturn(otherClaims);

        ResponseEntity actual = sut.getSubmission(UUID.randomUUID(), TestHelpers.CASE_5);
        assertEquals(HttpStatus.FORBIDDEN, actual.getStatusCode());
        assertEquals("MISSING_UNIVERSAL_ID", ((EfilingError)actual.getBody()).getError());
        assertEquals("universal-id claim missing in jwt token.", ((EfilingError)actual.getBody()).getMessage());


    }

    private List<ServiceFees> createFees() {
        ServiceFees fee1 = new ServiceFees(BigDecimal.TEN, SERVICE_TYPE_CD);
        ServiceFees fee2 = new ServiceFees(BigDecimal.ONE, SERVICE_TYPE_CD1);
        return Arrays.asList(fee1, fee2);
    }

}
