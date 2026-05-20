package adapter.servletElementConverter5;

import java.io.IOException;

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
 * TDD red-phase defect tests for FR-FIX-001 (StreamConverter.read incorrectly
 * delegates to origin.readLine instead of origin.read; and the readLine
 * signature itself is not delegated to origin.readLine).
 *
 * Each test exercises FR-FIX-001 under a distinct acceptance-criteria scenario
 * (AC-1 ~ AC-4). All four MUST fail against the currently-shipped (defective)
 * code at servlet5/.../StreamConverter.java L42-44 and L94-96, and turn green
 * only after FR-FIX-001 lands (task T-PH003-04).
 *
 * Expected failure signature:
 *   AC-1/AC-2/AC-4 : Mockito verify failure
 *                    ("Wanted but not invoked: origin.read(...);
 *                      However, origin.readLine(...) was called instead")
 *   AC-3           : Mockito verify failure
 *                    ("Wanted but not invoked: origin.readLine(...);
 *                      adapter.readLine fell through to ServletInputStream
 *                      default which calls origin.read byte-by-byte")
 *
 * Note on assertEquals ordering: in every test the Mockito verify(...) calls
 * precede assertEquals(...) so that the failure surfaces as a verification
 * mismatch (i.e. wrong method called on the origin), not as a misleading
 * stub-return assertion (Mockito returns 0 by default for an unstubbed int
 * call). After FR-FIX-001 fix lands, both verify and assertEquals will pass.
 */
class StreamConverterDefectTest {

    // ----------------------------------------------------------------------
    // AC-1 write_to_byte_array_no_line_truncation
    // jakarta -> javax adapter: read(byte[], int, int) must NOT truncate at
    // line boundaries; it must delegate to origin.read, not origin.readLine.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-001 AC-1: jakarta->javax read(byte[],off,len) must delegate to origin.read (no line truncation)")
    void shouldDelegateReadToReadNotReadLine_AC1() throws IOException {
        jakarta.servlet.ServletInputStream origin = mock(jakarta.servlet.ServletInputStream.class);
        byte[] buffer = new byte[16];
        when(origin.read(buffer, 0, buffer.length)).thenReturn(8);

        javax.servlet.ServletInputStream converted = StreamConverter.convert(origin);

        int returned = converted.read(buffer, 0, buffer.length);

        // verify first so red-phase failure surfaces as a Mockito verification
        // mismatch (origin.read wanted but origin.readLine called instead).
        verify(origin).read(buffer, 0, buffer.length);
        verify(origin, never()).readLine(any(byte[].class), anyInt(), anyInt());
        assertEquals(8, returned, "adapter must propagate origin.read return value, not a line-bounded count");
    }

    // ----------------------------------------------------------------------
    // AC-2 read_binary_safety
    // jakarta -> javax adapter: binary payloads (bytes possibly including LF
    // 0x0A) must not be reinterpreted by readLine semantics. The adapter
    // must invoke origin.read with the exact (off, len) requested.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-001 AC-2: jakarta->javax read must be binary-safe (no readLine reinterpretation)")
    void shouldDelegateReadToReadNotReadLine_AC2() throws IOException {
        jakarta.servlet.ServletInputStream origin = mock(jakarta.servlet.ServletInputStream.class);
        byte[] buffer = new byte[4];
        when(origin.read(buffer, 0, 4)).thenReturn(4);

        javax.servlet.ServletInputStream converted = StreamConverter.convert(origin);

        int returned = converted.read(buffer, 0, 4);

        // verify first so red-phase failure surfaces as a Mockito verification
        // mismatch (origin.read wanted but origin.readLine called instead).
        verify(origin).read(buffer, 0, 4);
        verify(origin, never()).readLine(any(byte[].class), anyInt(), anyInt());
        assertEquals(4, returned, "binary read must return full requested length from origin.read");
    }

    // ----------------------------------------------------------------------
    // AC-3 readLine signature retained (SRS FR-FIX-001 AC-3)
    //
    // SRS meaning: "the readLine(byte[], int, int) signature is preserved
    // separately and must delegate to the origin's readLine."
    //
    // Current defect surface: the jakarta->javax adapter's anonymous
    // javax.servlet.ServletInputStream subclass at StreamConverter.java L17-69
    // does NOT explicitly override readLine(byte[], int, int). Therefore
    // adapter.readLine(buf, off, len) falls through to the javax.servlet
    // .ServletInputStream default implementation (byte-by-byte read until
    // newline). It does NOT call origin.readLine. This violates AC-3.
    //
    // Expected red: verify(origin).readLine(...) fails with "Wanted but not
    // invoked". The green path requires T-PH003-04 to add an explicit
    // readLine(byte[], int, int) override that delegates to inputstream
    // .readLine(b, off, len).
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-001 AC-3: readLine signature retained — adapter.readLine must delegate to origin.readLine")
    void shouldDelegateReadToReadNotReadLine_AC3() throws IOException {
        jakarta.servlet.ServletInputStream origin = mock(jakarta.servlet.ServletInputStream.class);
        byte[] buffer = new byte[32];
        when(origin.readLine(buffer, 4, 20)).thenReturn(20);

        javax.servlet.ServletInputStream converted = StreamConverter.convert(origin);

        int returned = converted.readLine(buffer, 4, 20);

        // verify first so red-phase failure surfaces as a Mockito verification
        // mismatch (origin.readLine wanted but adapter.readLine fell through
        // to ServletInputStream default which never calls origin.readLine).
        verify(origin).readLine(buffer, 4, 20);
        verify(origin, never()).read(any(byte[].class), anyInt(), anyInt());
        assertEquals(20, returned, "adapter.readLine must propagate origin.readLine return value verbatim");
    }

    // ----------------------------------------------------------------------
    // AC-4 cross_module_servlet6_61_consistency
    // servlet5 module cannot reference servlet6/servlet61 directly (CON-MOD-001).
    // Instead, verify both directional adapters within servlet5 share the
    // identical correct delegation contract — jakarta->javax AND javax->jakarta
    // — as a servlet5-local proxy for cross-module consistency.
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-FIX-001 AC-4: both directional adapters must delegate read->read consistently (cross-module proxy)")
    void shouldDelegateReadToReadNotReadLine_AC4() throws IOException {
        // direction A: jakarta -> javax
        jakarta.servlet.ServletInputStream jakartaOrigin = mock(jakarta.servlet.ServletInputStream.class);
        byte[] bufferA = new byte[12];
        when(jakartaOrigin.read(bufferA, 0, 12)).thenReturn(12);

        javax.servlet.ServletInputStream convertedToJavax = StreamConverter.convert(jakartaOrigin);
        int returnedA = convertedToJavax.read(bufferA, 0, 12);

        // verify first so red-phase failure surfaces as a Mockito verification
        // mismatch (origin.read wanted but origin.readLine called instead).
        verify(jakartaOrigin).read(bufferA, 0, 12);
        verify(jakartaOrigin, never()).readLine(any(byte[].class), anyInt(), anyInt());
        assertEquals(12, returnedA, "jakarta->javax adapter must propagate origin.read return value");

        // direction B: javax -> jakarta
        javax.servlet.ServletInputStream javaxOrigin = mock(javax.servlet.ServletInputStream.class);
        byte[] bufferB = new byte[24];
        when(javaxOrigin.read(bufferB, 2, 10)).thenReturn(10);

        jakarta.servlet.ServletInputStream convertedToJakarta = StreamConverter.convert(javaxOrigin);
        int returnedB = convertedToJakarta.read(bufferB, 2, 10);

        // verify first so red-phase failure surfaces as a Mockito verification
        // mismatch (origin.read wanted but origin.readLine called instead).
        verify(javaxOrigin).read(bufferB, 2, 10);
        verify(javaxOrigin, never()).readLine(any(byte[].class), anyInt(), anyInt());
        assertEquals(10, returnedB, "javax->jakarta adapter must propagate origin.read return value");
    }
}
