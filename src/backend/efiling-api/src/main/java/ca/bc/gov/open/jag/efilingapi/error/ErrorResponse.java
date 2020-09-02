package ca.bc.gov.open.jag.efilingapi.error;

public enum ErrorResponse {

    INVALID_ROLE("User does not have a valid role for this request."),
    MISSING_UNIVERSAL_ID("universal-id claim missing in jwt token."),
    ACCOUNT_EXCEPTION("Client has multiple CSO profiles."),
    DOCUMENT_TYPE_ERROR("Error while retrieving documents"),
    DOCUMENT_REQUIRED("At least one document is required."),
    DOCUMENT_STORAGE_FAILURE("An unknown error happened while storing documents."),
    FILE_TYPE_ERROR("File is not a PDF"),
    CREATE_ACCOUNT_EXCEPTION("Error Creating CSO account."),
    UPDATE_CLIENT_EXCEPTION("Error Updating CSO client account."),
    CACHE_ERROR("Cache related error."),
    URL_GENERATION_FAILURE("failed to generate bambora card update url.");

    private final String errorMessage;

    ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return this.toString();
    }

    public String getErrorMessage() { return errorMessage; }
}
