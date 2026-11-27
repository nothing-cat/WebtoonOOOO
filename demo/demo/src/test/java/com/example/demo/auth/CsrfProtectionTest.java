package com.example.demo.auth;

import com.example.demo.config.SecurityConfig;
import com.example.demo.config.WebConfig;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(CsrfProtectionTest.TestConfig.class)
@ActiveProfiles("csrf-test")
class CsrfProtectionTest {
    @Autowired private WebApplicationContext context;
    @Autowired private FilterChainProxy securityFilter;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilter).build();
    }

    @Test
    void writeRequiresTokenFromTheSameSession() throws Exception {
        mvc.perform(post("/api/test-write")).andExpect(status().isForbidden());

        var tokenResponse = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) tokenResponse.getRequest().getSession(false);
        String token = JsonPath.read(tokenResponse.getResponse().getContentAsString(), "$.token");

        mvc.perform(post("/api/test-write").header("X-CSRF-TOKEN", token))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/test-write").session(session).header("X-CSRF-TOKEN", token))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, WebConfig.class, CsrfController.class, WriteController.class})
    static class TestConfig {}

    @RestController
    @Profile("csrf-test")
    static class WriteController {
        @PostMapping("/api/test-write")
        void write() {}
    }
}
