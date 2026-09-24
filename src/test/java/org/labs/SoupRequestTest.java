package org.labs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoupRequestTest {

    @Test
    void storesProgrammerId() {
        SoupRequest request = new SoupRequest(7);
        assertEquals(7, request.getProgrammerId());
    }

    @Test
    void resultIsNotDoneUntilCompleted() {
        SoupRequest request = new SoupRequest(1);
        assertFalse(request.getResult().isDone());
    }

    @Test
    void completeFinishesTheFuture() {
        SoupRequest request = new SoupRequest(1);

        request.complete(SoupResult.SOUP_RECEIVED);

        assertTrue(request.getResult().isDone());
        assertEquals(SoupResult.SOUP_RECEIVED, request.getResult().join());
    }
}