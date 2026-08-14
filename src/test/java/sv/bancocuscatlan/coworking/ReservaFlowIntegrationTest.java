package sv.bancocuscatlan.coworking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private Long espacioId;
    private Instant inicio;
    private Instant fin;

    @BeforeEach
    void setUp() throws Exception {
        String username = "res_" + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s@test.com",
                                  "password": "secret123"
                                }
                                """.formatted(username, username)))
                .andExpect(status().isCreated());

        userToken = login(username, "secret123");
        String adminToken = login("admin", "admin123");

        MvcResult espacioResult = mockMvc.perform(post("/api/espacios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Sala %s",
                                  "tipo": "DESK",
                                  "capacidad": 2,
                                  "ubicacion": "Piso 1",
                                  "tarifaHora": 10.00,
                                  "activo": true
                                }
                                """.formatted(username)))
                .andExpect(status().isCreated())
                .andReturn();

        espacioId = objectMapper.readTree(espacioResult.getResponse().getContentAsString()).get("id").asLong();
        inicio = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        fin = inicio.plus(2, ChronoUnit.HOURS);
    }

    @Test
    void createConfirmAndRejectOverlap() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/reservas")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaPayload(inicio, fin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDING"))
                .andReturn();

        Long reservaId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/reservas/{id}/confirm", reservaId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "paymentMethod": "CARD" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMED"));

        mockMvc.perform(post("/api/reservas")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaPayload(
                                inicio.plus(30, ChronoUnit.MINUTES),
                                fin.plus(30, ChronoUnit.MINUTES))))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/reportes/ocupacion")
                        .header("Authorization", "Bearer " + userToken)
                        .param("desde", inicio.minus(1, ChronoUnit.HOURS).toString())
                        .param("hasta", fin.plus(1, ChronoUnit.HOURS).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.espacioId==" + espacioId + ")]").exists());
    }

    @Test
    void cancelOwnReservation() throws Exception {
        Instant otherInicio = inicio.plus(5, ChronoUnit.DAYS);
        Instant otherFin = otherInicio.plus(1, ChronoUnit.HOURS);

        MvcResult createResult = mockMvc.perform(post("/api/reservas")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaPayload(otherInicio, otherFin)))
                .andExpect(status().isCreated())
                .andReturn();

        Long reservaId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/reservas/{id}/cancel", reservaId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELLED"));
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }

    private String reservaPayload(Instant from, Instant to) {
        return """
                {
                  "espacioId": %d,
                  "inicio": "%s",
                  "fin": "%s"
                }
                """.formatted(espacioId, from, to);
    }
}
