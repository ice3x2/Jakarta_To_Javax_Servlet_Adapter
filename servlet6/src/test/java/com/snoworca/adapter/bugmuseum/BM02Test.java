package com.snoworca.adapter.bugmuseum;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BM02 — servlet6 mirror of servlet5 BugMuseum BM02 (FR-FIX-002).
 *
 * adapter.jakarta.servlet6.http.Part.getSubmittedFileName must delegate to the wrapped origin
 * Part.getSubmittedFileName, not self-recurse. Regression for jakarta 6.0.
 *
 * @req FR-TEST-004
 */
class BM02Test {

    @Test
    @DisplayName("BM02: Part.getSubmittedFileName must delegate to origin Part, not recurse on self (FR-FIX-002)")
    void shouldReproduceBug02() throws Exception {
        javax.servlet.http.Part originPart = mock(javax.servlet.http.Part.class);
        when(originPart.getSubmittedFileName()).thenReturn("upload.txt");

        // adapter.jakarta.servlet6.http.Part has a package-default constructor; access via reflection.
        Class<?> adapterClass = Class.forName("adapter.jakarta.servlet6.http.Part");
        Constructor<?> ctor = adapterClass.getDeclaredConstructor(javax.servlet.http.Part.class);
        ctor.setAccessible(true);
        jakarta.servlet.http.Part adapterPart = (jakarta.servlet.http.Part) ctor.newInstance(originPart);

        String fileName = assertDoesNotThrow(adapterPart::getSubmittedFileName,
            "adapter must not throw StackOverflowError or other recursion-induced exception");

        assertEquals("upload.txt", fileName, "adapter must return origin.getSubmittedFileName()");
        verify(originPart).getSubmittedFileName();
    }
}
