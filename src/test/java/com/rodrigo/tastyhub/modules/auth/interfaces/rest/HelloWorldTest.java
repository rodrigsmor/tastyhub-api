package com.rodrigo.tastyhub.modules.auth.interfaces.rest;

import com.rodrigo.tastyhub.modules.auth.interfaces.rest.HelloWorld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloWorld.class)
@WithMockUser
class HelloWorldTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return Hello World message and status 200")
    void shouldReturnHelloWorld() throws Exception {
        mockMvc.perform(get("/hello-world"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello World!")); // Check response body
    }
}