package com.skishop.web.handler;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_returnsErrorView() {
        Model model = new ExtendedModelMap();
        String view = handler.handleException(new RuntimeException("test error"), model);
        assertEquals("error/general", view);
        assertEquals("test error", model.getAttribute("errorMessage"));
    }

    @Test
    void handleIllegalArgumentException_returnsErrorView() {
        Model model = new ExtendedModelMap();
        String view = handler.handleException(new IllegalArgumentException("bad argument"), model);
        assertEquals("error/general", view);
        assertEquals("bad argument", model.getAttribute("errorMessage"));
    }
}
