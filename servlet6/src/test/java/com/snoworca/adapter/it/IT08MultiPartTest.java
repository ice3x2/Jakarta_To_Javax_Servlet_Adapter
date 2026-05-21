package com.snoworca.adapter.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import jakarta.servlet.MultipartConfigElement;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import adapter.servletElementConverter6.ServletConverter;

/**
 * IT08 — multipart/form-data integration test (servlet6 module).
 *
 * <p>Boots Tomcat 10.1.x via {@link TomcatRunner}, registers a {@code javax.servlet}
 * {@link HttpServlet} bridged through {@link ServletConverter#convert(javax.servlet.Servlet)}.
 * The servlet is configured with a {@link MultipartConfigElement} pointing to a dedicated
 * temp directory and a small {@code fileSizeThreshold} to force Tomcat to spool the binary
 * part to disk. The client posts a hand-crafted multipart body containing a UTF-8 text part
 * and a binary part. The test asserts the servlet receives both parts intact through the
 * adapter (F2 boundary — {@link HttpServletRequest#getPart}/{@link HttpServletRequest#getParts}),
 * and that the multipart spool directory is cleaned up by Tomcat's request lifecycle so no
 * temp files are left behind.
 *
 * @req FR-TEST-003
 */
@Tag("integration")
class IT08MultiPartTest {

    private static final String BOUNDARY = "----IT08BoundaryXyZ123";
    private static final String TEXT_PART_NAME = "metadata";
    private static final String TEXT_PART_VALUE = "스노우오르카-멀티파트-텍스트";
    private static final String BINARY_PART_NAME = "payload";
    private static final String BINARY_FILENAME = "payload.bin";

    /**
     * @req FR-TEST-003
     */
    @Test
    void shouldHandleMultiPart() throws Exception {
        Path multipartTmp = Files.createTempDirectory("it08-multipart-spool-");
        try (TomcatRunner runner = new TomcatRunner()) {
            javax.servlet.Servlet javaxServlet = new MultiPartEchoServlet();
            jakarta.servlet.Servlet jakartaServlet = ServletConverter.convert(javaxServlet);
            MultipartConfigElement config = new MultipartConfigElement(
                    multipartTmp.toAbsolutePath().toString(), 10_000_000L, 10_000_000L, 128);
            runner.addServlet("multipart", "/multipart", jakartaServlet, config);
            runner.start();

            byte[] binaryPayload = buildBinaryPayload(16_384);
            byte[] body = buildMultipartBody(binaryPayload);

            HttpURLConnection conn = (HttpURLConnection) new URL(
                    runner.baseUrl() + "/multipart").openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setFixedLengthStreamingMode(body.length);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(body);
            }

            assertEquals(200, conn.getResponseCode(), "status");
            String textEcho = conn.getHeaderField("X-Echo-Text");
            String binaryLen = conn.getHeaderField("X-Echo-Binary-Length");
            String binaryFilename = conn.getHeaderField("X-Echo-Binary-Filename");
            String partsCount = conn.getHeaderField("X-Echo-Parts-Count");
            byte[] echoedBinary = readAll(conn.getInputStream());

            assertNotNull(textEcho, "X-Echo-Text header present");
            String decodedText = new String(Base64.getDecoder().decode(textEcho), StandardCharsets.UTF_8);
            assertEquals(TEXT_PART_VALUE, decodedText, "text part value");
            assertEquals("2", partsCount, "getParts() returned both parts");
            assertEquals(BINARY_FILENAME, binaryFilename, "binary part filename");
            assertEquals(String.valueOf(binaryPayload.length), binaryLen, "binary part length");
            assertTrue(equalBytes(binaryPayload, echoedBinary), "binary part bytes match");

            List<Path> leftover = listLeftoverSpoolFiles(multipartTmp);
            assertTrue(leftover.isEmpty(),
                    "multipart spool directory should be cleaned, but found: " + leftover);
        } finally {
            deleteRecursively(multipartTmp);
        }
    }

    private static byte[] buildMultipartBody(byte[] binaryPayload) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        String crlf = "\r\n";

        buf.write(("--" + BOUNDARY + crlf).getBytes(StandardCharsets.UTF_8));
        buf.write(("Content-Disposition: form-data; name=\"" + TEXT_PART_NAME + "\"" + crlf)
                .getBytes(StandardCharsets.UTF_8));
        buf.write(("Content-Type: text/plain; charset=UTF-8" + crlf).getBytes(StandardCharsets.UTF_8));
        buf.write(crlf.getBytes(StandardCharsets.UTF_8));
        buf.write(TEXT_PART_VALUE.getBytes(StandardCharsets.UTF_8));
        buf.write(crlf.getBytes(StandardCharsets.UTF_8));

        buf.write(("--" + BOUNDARY + crlf).getBytes(StandardCharsets.UTF_8));
        buf.write(("Content-Disposition: form-data; name=\"" + BINARY_PART_NAME + "\"; filename=\""
                + BINARY_FILENAME + "\"" + crlf).getBytes(StandardCharsets.UTF_8));
        buf.write(("Content-Type: application/octet-stream" + crlf).getBytes(StandardCharsets.UTF_8));
        buf.write(crlf.getBytes(StandardCharsets.UTF_8));
        buf.write(binaryPayload);
        buf.write(crlf.getBytes(StandardCharsets.UTF_8));

        buf.write(("--" + BOUNDARY + "--" + crlf).getBytes(StandardCharsets.UTF_8));
        return buf.toByteArray();
    }

    private static byte[] buildBinaryPayload(int size) {
        byte[] out = new byte[size];
        for (int i = 0; i < size; i++) {
            out[i] = (byte) ((i * 31) % 256);
        }
        return out;
    }

    private static boolean equalBytes(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static List<Path> listLeftoverSpoolFiles(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile).collect(Collectors.toList());
        }
    }

    private static void deleteRecursively(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // best-effort
                }
            });
        } catch (IOException ignored) {
            // best-effort
        }
    }

    /**
     * @req FR-TEST-003
     */
    static final class MultiPartEchoServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            Part text = req.getPart(TEXT_PART_NAME);
            Part binary = req.getPart(BINARY_PART_NAME);
            int totalParts = req.getParts() == null ? 0 : req.getParts().size();

            String textValue = readPartAsString(text);
            byte[] binaryBytes = readPartAsBytes(binary);

            resp.setStatus(200);
            resp.setContentType("application/octet-stream");
            // Encode text value in base64 because HTTP headers cannot reliably carry UTF-8 bytes.
            resp.setHeader("X-Echo-Text", Base64.getEncoder().encodeToString(
                    textValue.getBytes(StandardCharsets.UTF_8)));
            resp.setHeader("X-Echo-Binary-Length", String.valueOf(binaryBytes.length));
            resp.setHeader("X-Echo-Binary-Filename",
                    binary == null ? "" : binary.getSubmittedFileName());
            resp.setHeader("X-Echo-Parts-Count", String.valueOf(totalParts));
            resp.setContentLength(binaryBytes.length);
            resp.getOutputStream().write(binaryBytes);
        }

        private static String readPartAsString(Part part) throws IOException {
            if (part == null) {
                return "";
            }
            try (InputStream in = part.getInputStream()) {
                return new String(readAll(in), StandardCharsets.UTF_8);
            }
        }

        private static byte[] readPartAsBytes(Part part) throws IOException {
            if (part == null) {
                return new byte[0];
            }
            try (InputStream in = part.getInputStream()) {
                return readAll(in);
            }
        }
    }
}
