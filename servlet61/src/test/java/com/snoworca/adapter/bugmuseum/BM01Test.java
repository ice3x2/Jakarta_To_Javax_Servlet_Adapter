package com.snoworca.adapter.bugmuseum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BM01 — servlet61 mirror of servlet5 BugMuseum BM01 (FR-FIX-001).
 *
 * StreamConverter.convert(jakarta.servlet.ServletInputStream).read must delegate to the origin
 * inputstream.read, not to inputstream.readLine. Regression for jakarta 6.1
 * (adapter.servletElementConverter61.StreamConverter).
 *
 * @req FR-TEST-004
 */
class BM01Test {

    @Test
    @DisplayName("BM01: StreamConverter.read must delegate to inputstream.read, not readLine (FR-FIX-001)")
    void shouldReproduceBug01() throws Exception {
        jakarta.servlet.ServletInputStream origin = mock(jakarta.servlet.ServletInputStream.class);
        byte[] buffer = new byte[16];
        when(origin.read(buffer, 0, buffer.length)).thenReturn(7);

        javax.servlet.ServletInputStream converted =
            adapter.servletElementConverter61.StreamConverter.convert(origin);

        int returned = converted.read(buffer, 0, buffer.length);

        assertEquals(7, returned, "adapter must propagate the byte count returned by inputstream.read");
        verify(origin).read(buffer, 0, buffer.length);
        verify(origin, never()).readLine(any(byte[].class), anyInt(), anyInt());
    }
}
