package pl.klejczyk.tpm.workorder.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pl.klejczyk.tpm.workorder.TestcontainersConfiguration;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class WorkOrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static RequestPostProcessor actor(String id, String role) {
        return jwt().jwt(token -> token.subject(id).claim("role", role));
    }

    private String reportWorkOrder() throws Exception {
        String body = mockMvc.perform(post("/work-orders").with(actor("op-1", "OPERATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"machineId\":\"m-1\",\"reason\":\"BREAKDOWN\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asText();
    }

    @Test
    void takesTheReporterFromTheTokenRatherThanTheRequest() throws Exception {
        mockMvc.perform(post("/work-orders").with(actor("op-1", "OPERATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"machineId\":\"m-1\",\"reason\":\"BREAKDOWN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportedBy").value("op-1"));
    }

    @Test
    void ignoresAnAttemptToSpoofTheReporter() throws Exception {
        mockMvc.perform(post("/work-orders").with(actor("op-1", "OPERATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"machineId\":\"m-1\",\"reason\":\"BREAKDOWN\",\"reportedBy\":\"mgr-1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportedBy").value("op-1"));
    }

    @Test
    void technicianCannotStartWorkOrderThatIsStillReported() throws Exception {
        String id = reportWorkOrder();

        mockMvc.perform(post("/work-orders/" + id + "/start").with(actor("tech-1", "TECHNICIAN")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void operatorIsNotAllowedToAssign() throws Exception {
        String id = reportWorkOrder();

        mockMvc.perform(post("/work-orders/" + id + "/assign").with(actor("op-1", "OPERATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"technicianId\":\"tech-1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/work-orders/any-id/start"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void walksFullLifecycleThroughApi() throws Exception {
        String id = reportWorkOrder();

        mockMvc.perform(post("/work-orders/" + id + "/assign").with(actor("mgr-1", "MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"technicianId\":\"tech-1\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/work-orders/" + id + "/start").with(actor("tech-1", "TECHNICIAN")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/work-orders/" + id + "/resolve").with(actor("tech-1", "TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"Bearing replaced\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/work-orders/" + id + "/close").with(actor("mgr-1", "MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
}
