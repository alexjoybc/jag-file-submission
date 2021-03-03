package ca.bc.gov.open.jag.efilingreviewerapi.queue;

import ca.bc.gov.open.jag.efilingreviewerapi.api.DocumentsApiDelegate;
import ca.bc.gov.open.jag.efilingreviewerapi.api.model.DocumentEvent;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Receiver test suite")
public class ReceiverTest {

    Receiver sut;

    @Mock
    private DocumentsApiDelegate documentsApiDelegateMock;

    @BeforeAll
    public void beforeAll() {

        MockitoAnnotations.openMocks(this);

        Mockito.when(documentsApiDelegateMock.documentEvent(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        sut = new Receiver(documentsApiDelegateMock);

    }

    @Test
    @DisplayName("Ok: Message added")
    public void withMessageAddedToRedis() {

        sut.receiveMessage("2");

        Assertions.assertEquals(1, sut.getCount());

    }

}
