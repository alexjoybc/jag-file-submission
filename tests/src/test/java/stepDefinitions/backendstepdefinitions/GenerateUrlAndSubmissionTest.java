package stepDefinitions.backendstepdefinitions;

import ca.bc.gov.open.jagefilingapi.qa.backend.generateurlpayload.GenerateUrlPayload;
import ca.bc.gov.open.jagefilingapi.qa.backendutils.APIResources;
import ca.bc.gov.open.jagefilingapi.qa.backendutils.TestUtil;
import ca.bc.gov.open.jagefilingapi.qa.frontendutils.DriverClass;
import ca.bc.gov.open.jagefilingapi.qa.frontendutils.JsonDataReader;
import ca.bc.gov.open.jagefilingapi.qa.requestbuilders.GenerateUrlRequestBuilders;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

public class GenerateUrlAndSubmissionTest extends DriverClass {

    private Response response;
    private GenerateUrlRequestBuilders generateUrlRequestBuilders;
    private String submissionId;
    private JsonPath jsonPath;
    private String validExistingCSOGuid;
    private static final String CONTENT_TYPE = "application/json";
    private static final String X_TRANSACTION_ID = "X-Transaction-Id";
    private static final String X_USER_ID = "X-User-Id";
    private static final String SUBMISSION_ID = "submissionId";
    private static final String TRANSACTION_ID = "transactionId";
    private String userToken;

    public Logger log = LogManager.getLogger(GenerateUrlAndSubmissionTest.class);

    @Given("POST http request is made to {string} with valid existing CSO account guid and a single pdf file")
    public void postHttpRequestIsMadeToWithValidExistingCsoAccountGuidAndASinglePdfFile(String resource) throws IOException {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        response = generateUrlRequestBuilders.validRequestWithSingleDocument(resource);
    }

    @When("status code is {int} and content type is verified")
    public void statusCodeIsAndContentTypeIsVerified(Integer statusCode) {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        assertEquals(200, response.getStatusCode());
        assertEquals(CONTENT_TYPE, response.getContentType());
    }

    @Then("verify submission id and document count is received")
    public void verifySubmissionIdAndDocumentCountIsReceived() {
        jsonPath = new JsonPath(response.asString());

        submissionId = TestUtil.getJsonPath(response, SUBMISSION_ID);
        int receivedCount = jsonPath.get("received");

        switch (receivedCount) {
            case 1:
                assertEquals(1, receivedCount);
                break;
            case 2:
                assertEquals(2, receivedCount);
                break;
            default:
                log.info("Document count did not match.");
        }
        assertNotNull(submissionId);
    }

    @Given("POST http request is made to {string} with client application, court details and navigation urls")
    public void POSTHttpRequestIsMadeToWithClientApplicationCourtDetailsAndNavigationUrls(String resource) throws IOException {
        GenerateUrlPayload payloadData = new GenerateUrlPayload();
        APIResources resourceGet = APIResources.valueOf(resource);

        validExistingCSOGuid = JsonDataReader.getCsoAccountGuid().getValidExistingCSOGuid();
        String validUserid = JsonDataReader.getCsoAccountGuid().getValidUserId();

        response = generateUrlRequestBuilders.getBearerToken();
        jsonPath = new JsonPath(response.asString());

        String accessToken = jsonPath.get("access_token");

        RequestSpecification request = given().auth().preemptive().oauth2(accessToken)
                .spec(TestUtil.requestSpecification())
                .header(X_TRANSACTION_ID, validExistingCSOGuid)
                .header(X_USER_ID,validUserid )
                .body(payloadData.validGenerateUrlPayload());
        response = request.when().post(resourceGet.getResource() + submissionId + "/generateUrl")
                .then()
                .spec(TestUtil.responseSpecification())
                .extract().response();
    }

    @Then("verify expiry date and eFiling url are returned with the CSO account guid and submission id")
    public void verifyExpiryDateAndEfilingUrlAreReturnedWithTheCsoAccountGuidAndSubmissionId() throws URISyntaxException, IOException {
        jsonPath = new JsonPath(response.asString());
        validExistingCSOGuid = JsonDataReader.getCsoAccountGuid().getValidExistingCSOGuid();

        String respUrl = jsonPath.get("efilingUrl");
        Long expiryDate = jsonPath.get("expiryDate");

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(respUrl), StandardCharsets.UTF_8);
        String respSubId = null;
        String respTransId = null;

        for (NameValuePair param : params) {
            if (param.getName().equals(SUBMISSION_ID)) {
                respSubId = param.getValue();
            } else if (param.getName().equals(TRANSACTION_ID)) {
                respTransId = param.getValue();
            }
        }

        assertEquals(submissionId, respSubId);
        assertEquals(validExistingCSOGuid, respTransId);
        assertNotNull(expiryDate);
    }

    @Given("{string} id is submitted with GET http request")
    public void idIsSubmittedWithGetHttpRequest(String resource) throws IOException {
        APIResources resourceGet = APIResources.valueOf(resource);

        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();
        userToken = generateUrlRequestBuilders.getUserJwtToken();

        RequestSpecification request = given().auth().preemptive().oauth2(userToken)
                .spec(TestUtil.requestSpecification())
                .header(X_TRANSACTION_ID, validExistingCSOGuid);

        response = request.when().get(resourceGet.getResource() + submissionId)
                .then()
                .spec(TestUtil.responseSpecification())
                .extract().response();
    }

    @Then("verify universal id, user details, account type and identifier values are returned and not empty")
    public void verifyUniversalIdUserDetailsAccountTypeAndIdentifierValuesAreReturnedAndNotEmpty() {
        jsonPath = new JsonPath(response.asString());

        String universalId = jsonPath.get("userDetails.universalId");
        String displayName = jsonPath.get("clientApplication.displayName");
        String clientAppType = jsonPath.get("clientApplication.type");

        List<String> type = jsonPath.get("userDetails.accounts.type");
        List<String> identifier = jsonPath.get("userDetails.accounts.identifier");

        assertThat(universalId, is(not(emptyString())));
        assertThat(displayName, is(not(emptyString())));
        assertThat(clientAppType, is(not(emptyString())));
        log.info("Names and email objects from the valid CSO account submission response have valid values");

        assertFalse(type.isEmpty());
        assertFalse(identifier.isEmpty());
        log.info("Account type and identifier objects from the valid CSO account submission response have valid values");
    }

    @Given("{string} id with filing package path is submitted with GET http request")
    public void idWithFilingPackagePathIsSubmittedWithGETHttpRequest(String resource) throws IOException {
        APIResources resourceGet = APIResources.valueOf(resource);

        RequestSpecification request = given().auth().preemptive().oauth2(userToken).spec(TestUtil.requestSpecification())
                .header(X_TRANSACTION_ID, validExistingCSOGuid);

        response = request.when().get(resourceGet.getResource() + submissionId + "/filing-package")
                .then()
                .spec(TestUtil.responseSpecification())
                .extract().response();
    }

    @Then("verify court details and document details are returned and not empty")
    public void verifyCourtDetailsAndDocumentDetailsAreReturnedAndNotEmpty() {
        jsonPath = new JsonPath(response.asString());

        String location = jsonPath.get("court.location");
        String level = jsonPath.get("court.level");
        String courtClass = jsonPath.get("court.class");
        String division = jsonPath.get("court.division");
        String fileNumber = jsonPath.get("court.fileNumber");
        String participatingClass = jsonPath.get("court.participatingClass");
        String locationDescription = jsonPath.get("court.locationDescription");
        String levelDescription = jsonPath.get("court.levelDescription");
        float submissionFeeAmount = jsonPath.get("submissionFeeAmount");

        List<String> name = jsonPath.get("documents.name");
        List<String> type = jsonPath.get("documents.type");
        List<String> subType = jsonPath.get("documents.subType");
        List<String> isAmendment = jsonPath.get("documents.isAmendment");
        List<String> isSupremeCourtScheduling = jsonPath.get("documents.isSupremeCourtScheduling");
        List<String> description = jsonPath.get("documents.description");
        List<String> statutoryFeeAmount = jsonPath.get("documents.statutoryFeeAmount");
        List<String> mimeType = jsonPath.get("documents.mimeType");

        assertThat(location, is(not(emptyString())));
        assertThat(level, is(not(emptyString())));
        assertThat(courtClass, is(not(emptyString())));
        assertThat(division, is(not(emptyString())));
        assertThat(fileNumber, is(not(emptyString())));
        assertThat(participatingClass, is(not(emptyString())));
        assertThat(locationDescription, is(not(emptyString())));
        assertThat(levelDescription, is(not(emptyString())));
        assertEquals(7.00, submissionFeeAmount, 0);
        log.info("Court fee and document details response have valid values");

        assertFalse(name.isEmpty());
        assertFalse(type.isEmpty());
        assertFalse(subType.isEmpty());
        assertFalse(isAmendment.isEmpty());
        assertFalse(isSupremeCourtScheduling.isEmpty());
        assertFalse(description.isEmpty());
        assertFalse(mimeType.isEmpty());
        assertNotNull(statutoryFeeAmount);
        log.info("Account type, description and name objects from the valid CSO account submission response have valid values");
    }

    @And("verify success, error and cancel navigation urls are returned")
    public void verifySuccessErrorAndCancelNavigationUrlsAreReturned() {
        jsonPath = new JsonPath(response.asString());

        String successUrl = jsonPath.get("navigation.success.url");
        String errorUrl = jsonPath.get("navigation.error.url");
        String cancelUrl = jsonPath.get("navigation.cancel.url");

        assertNotNull(successUrl);
        assertNotNull(errorUrl);
        assertNotNull(cancelUrl);
    }

    @Given("{string} id with filename path is submitted with GET http request")
    public void idWithFilenamePathIsSubmittedWithGETHttpRequest(String resource) throws IOException {
        APIResources resourceGet = APIResources.valueOf(resource);

        RequestSpecification request = given().auth().preemptive().oauth2(userToken).spec(TestUtil.requestSpecification())
                .header(X_TRANSACTION_ID, validExistingCSOGuid);

        response = request.when().get(resourceGet.getResource() + submissionId + "/document" + "/test-document.pdf")
                .then()
               // .spec(TestUtil.documentValidResponseSpecification())
                .extract().response();
    }

    @Then("Verify status code is {int} and content type is not json")
    public void verifyStatusCodeIsAndContentTypeIsNotJson(Integer int1) {
        assertEquals(200, response.getStatusCode());
        assertEquals("application/octet-stream", response.getContentType());
    }

    @Given("POST http request is made to {string} with valid existing CSO account guid and multiple file")
    public void POSTHttpRequestIsMadeToWithValidExistingCSOAccountGuidAndMultipleFile(String resource) throws IOException {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        response = generateUrlRequestBuilders.validRequestWithMultipleDocuments(resource);
    }
}
