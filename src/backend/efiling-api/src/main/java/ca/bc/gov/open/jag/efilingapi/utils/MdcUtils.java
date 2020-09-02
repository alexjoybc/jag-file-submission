package ca.bc.gov.open.jag.efilingapi.utils;

import ca.bc.gov.open.jag.efilingapi.Keys;
import ca.bc.gov.open.jag.efilingapi.submission.models.SubmissionKey;
import org.slf4j.MDC;

public class MdcUtils {

    private MdcUtils() {}

    public static void setClientMDC(SubmissionKey submissionKey, String clientId) {
        MDC.put(Keys.MDC_EFILING_CLIENT_ID, clientId);
        setUserMDC(submissionKey);
    }

    public static void clearClientMDC() {
        MDC.remove(Keys.MDC_EFILING_CLIENT_ID);
        clearUserMDC();
    }

    public static void setUserMDC(SubmissionKey submissionKey) {
        MDC.put(Keys.MDC_EFILING_UNIVERSAL_ID, submissionKey.getUniversalId().toString());
        MDC.put(Keys.MDC_EFILING_SUBMISSION_ID, submissionKey.getSubmissionId() == null ? null : submissionKey.getSubmissionId().toString());
        MDC.put(Keys.MDC_EFILING_TRANSACTION_ID, submissionKey.getTransactionId().toString());
    }

    public static void clearUserMDC() {
        MDC.remove(Keys.MDC_EFILING_UNIVERSAL_ID);
        MDC.remove(Keys.MDC_EFILING_SUBMISSION_ID);
        MDC.remove(Keys.MDC_EFILING_TRANSACTION_ID);
    }

}
