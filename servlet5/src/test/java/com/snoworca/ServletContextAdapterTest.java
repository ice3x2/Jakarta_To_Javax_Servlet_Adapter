package com.snoworca;

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServletContextAdapterTest {

    @Test
    void testConvertServletContext() {
        ServletContext jakartaContext = mock(ServletContext.class);
        when(jakartaContext.getContextPath()).thenReturn("/mock");

        javax.servlet.ServletContext converted = ServletAdapter.adaptToJavax(jakartaContext);
        assertNotNull(converted);
        assertEquals("/mock", converted.getContextPath());
    }
}
