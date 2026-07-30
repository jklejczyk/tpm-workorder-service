package pl.klejczyk.tpm.workorder.api;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.klejczyk.tpm.workorder.TestcontainersConfiguration;

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

    private String reportWorkOrder() throws Exception {
        String body = mockMvc.perform(post("/work-orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"machineId\":\"m-1\",\"reason\":\"BREAKDOWN\",\"reportedBy\":\"op-1\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asString();
    }

    @Test
    void technicianCannotStartWorkOrderThatIsStillReported() throws Exception {
        String id = reportWorkOrder();

        mockMvc.perform(post("/work-orders/" + id + "/start").header("X-Actor-Id", "tech-1").header("X-Actor-Role", "TECHNICIAN"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void operatorIsNotAllowedToAssign() throws Exception {
        String id = reportWorkOrder();

        mockMvc.perform(post("/work-orders/" + id + "/assign").header("X-Actor-Id", "op-1").header("X-Actor-Role", "OPERATOR")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"technicianId\":\"tech-1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requiresActorHeaders() throws Exception {
        String id = reportWorkOrder();

        mockMvc.perform(post("/work-orders/" + id + "/start")).andExpect(status().isBadRequest());
    }

    @Test
    void walksFullLifecycleThroughApi() throws Exception {
        String id = reportWorkOrder();

        mockMvc.perform(post("/work-orders/" + id + "/assign")
                        .header("X-Actor-Id", "mgr-1")
                        .header("X-Actor-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"technicianId\":\"tech-1\"}"))
                .andExpect(status().isOk());


        mockMvc.perform(post("/work-orders/" + id + "/start")
                        .header("X-Actor-Id", "tech-1")
                        .header("X-Actor-Role", "TECHNICIAN"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/work-orders/" + id + "/resolve")
                        .header("X-Actor-Id", "tech-1").header("X-Actor-Role", "TECHNICIAN").
                        contentType(MediaType.APPLICATION_JSON).content("{\"resolution\":\"Bearing replaced\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/work-orders/" + id + "/close")
                        .header("X-Actor-Id", "mgr-1")
                        .header("X-Actor-Role", "MANAGER"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CLOSED"));
    }
}
