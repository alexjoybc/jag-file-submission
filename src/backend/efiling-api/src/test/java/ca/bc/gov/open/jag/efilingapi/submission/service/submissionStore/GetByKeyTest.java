package ca.bc.gov.open.jag.efilingapi.submission.service.submissionStore;


import ca.bc.gov.open.jag.efilingapi.TestHelpers;
import ca.bc.gov.open.jag.efilingapi.api.model.DocumentProperties;
import ca.bc.gov.open.jag.efilingapi.submission.models.Submission;
import ca.bc.gov.open.jag.efilingapi.submission.service.SubmissionStoreImpl;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetByKeyTest {

    private static final String TYPE = "type";

    @InjectMocks
    private SubmissionStoreImpl sut;

    @BeforeAll
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        DocumentProperties documentMetadata = new DocumentProperties();
        documentMetadata.setType(TYPE);
    }

    @Test
    @DisplayName("CASE 1: without cache always return empty")
    public void withExistingIdShouldReturnSubmission() {

        Optional<Submission> actual = sut.get(TestHelpers.CASE_1, UUID.randomUUID());
        Assertions.assertEquals(Optional.empty(), actual);

    }


}
