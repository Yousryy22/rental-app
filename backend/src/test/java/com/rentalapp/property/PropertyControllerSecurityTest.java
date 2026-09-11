package com.rentalapp.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalapp.auth.JwtService;
import com.rentalapp.auth.SecurityConfig;
import com.rentalapp.auth.UserDetailsServiceImpl;
import com.rentalapp.property.dto.PropertyRequest;
import com.rentalapp.user.Role;
import com.rentalapp.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test: verifies method-level @PreAuthorize actually blocks the wrong role.
 *
 * @Import(SecurityConfig.class) is required — without it, @WebMvcTest falls back to
 * Spring Boot's *default* auto-configured security, not the app's real filter chain.
 *
 * Note: JwtAuthFilter itself is NOT mocked here. @WebMvcTest auto-detects @Component
 * Filter beans, so the REAL JwtAuthFilter runs — its dependencies (JwtService,
 * UserDetailsServiceImpl) are mocked instead. Mocking JwtAuthFilter directly makes
 * Mockito's do-nothing doFilter() swallow every request without calling
 * filterChain.doFilter(...), short-circuiting the whole chain to an empty 200
 * regardless of auth state.
 *
 * For the "allowed" case, plain @WithMockUser is NOT enough: it populates the security
 * context with Spring Security's own generic User, not our domain com.rentalapp.user.User
 * — so @AuthenticationPrincipal User in the controller resolves to null and NPEs. Use
 * SecurityMockMvcRequestPostProcessors.user(UserDetails) with our real domain object instead.
 */
@WebMvcTest(PropertyController.class)
@Import(SecurityConfig.class)
class PropertyControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PropertyService propertyService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private final PropertyRequest validRequest =
            new PropertyRequest("Loft", "nice place", "1 St", "Cairo", new BigDecimal("500"), 2, 1);

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotCreateProperty() throws Exception {
        // @PreAuthorize denies before the method body (and @AuthenticationPrincipal
        // resolution) ever runs, so the generic @WithMockUser principal is fine here.
        mockMvc.perform(post("/api/properties")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanCreateProperty() throws Exception {
        User owner = User.builder()
                .id(UUID.randomUUID())
                .email("owner@example.com")
                .passwordHash("irrelevant-for-this-test")
                .fullName("Test Owner")
                .role(Role.OWNER)
                .build();

        mockMvc.perform(post("/api/properties")
                        .with(user(owner))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/properties")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }
}
