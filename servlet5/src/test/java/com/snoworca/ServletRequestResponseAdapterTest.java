package com.snoworca;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class ServletRequestResponseAdapterTest {

    @Test
    void testConvertServletRequestResponse() {
        javax.servlet.ServletRequest javaxRequest = mock(HttpServletRequest.class);
        javax.servlet.ServletResponse javaxResponse = mock(HttpServletResponse.class);

        ServletRequest jakartaRequest = ServletAdapter.adaptToJakarta(javaxRequest);
        ServletResponse jakartaResponse = ServletAdapter.adaptToJakarta(javaxResponse);

        assertNotNull(jakartaRequest);
        assertNotNull(jakartaResponse);

        javax.servlet.ServletRequest convertedRequest = ServletAdapter.adaptToJavax(jakartaRequest);
        javax.servlet.ServletResponse convertedResponse = ServletAdapter.adaptToJavax(jakartaResponse);

        assertNotNull(convertedRequest);
        assertNotNull(convertedResponse);
    }
}
