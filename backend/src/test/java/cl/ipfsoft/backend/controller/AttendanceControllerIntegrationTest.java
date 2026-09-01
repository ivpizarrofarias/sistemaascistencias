package cl.ipfsoft.backend.controller;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración del flujo de asistencia:
 * 1) El admin crea un empleado (solo el admin puede).
 * 2) El empleado creado NO tiene cuenta de acceso (solo su código de marcaje).
 * 3) El kiosco/móvil marca la entrada del trabajador mediante su código generado.
 * 4) Un código inexistente devuelve 404.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:att_it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AttendanceControllerIntegrationTest {

    @Autowired MockMvc mockMvc;

    private static final String RUT = "25327905-2";

    // Código de marcaje devuelto al crear al empleado (se comparte entre los tests ordenados).
    private static String CODIGO = null;

    private String loginAdmin() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String body = res.getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(body, "$.token");
    }

    @Test
    @Order(1)
    void adminPuedeCrearEmpleado() throws Exception {
        String token = loginAdmin();
        String payload = """
                {
                  "rut": "%s", "firstName": "Maria", "lastName": "Gonzalez",
                  "hireDate": "2024-03-01",
                  "baseSalary": 1200000, "dependents": 0,
                  "pensionSystem": "AFP", "afpFund": "APROVISIONA", "healthSystem": "ISAPRE"
                }
                """.formatted(RUT);
        MvcResult res = mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rut").value("25.327.905-2"))
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andReturn();
        CODIGO = com.jayway.jsonpath.JsonPath.read(res.getResponse().getContentAsString(), "$.code");
    }

    @Test
    @Order(2)
    void empleadoNoTieneUsuario_loginFalla() throws Exception {
        // El empleado ya no tiene credenciales: su identificador es solo el código de marcaje.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"maria\",\"password\":\"1234\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    void kioscoMarcaEntradaConCodigo() throws Exception {
        // El marcaje es público: solo se envía el código de marcaje del empleado.
        org.junit.jupiter.api.Assertions.assertNotNull(CODIGO, "El código debería haberse generado");
        mockMvc.perform(post("/api/attendance/mark")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"%s\"}".formatted(CODIGO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryTime").exists());
    }

    @Test
    @Order(4)
    void statusConCodigoDesconocido_devuelve404() throws Exception {
        mockMvc.perform(post("/api/attendance/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ZZZZ9999\"}"))
                .andExpect(status().isNotFound());
    }
}