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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración de la gestión de empleados (solo administrador).
 * Verifica la validación del RUT en el backend, el alta y la deshabilitación.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:emp_it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeControllerIntegrationTest {

    @Autowired MockMvc mockMvc;

    private String loginAdmin() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(res.getResponse().getContentAsString(), "$.token");
    }

    @Test
    @Order(1)
    void accesoASinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void crearEmpleadoConRutInvalido_devuelve400() throws Exception {
        String token = loginAdmin();
        String payload = """
                { "rut": "11111111-9", "firstName": "X", "lastName": "Y",
                  "hireDate": "2024-01-01", "shift": "MANANA",
                  "baseSalary": 800000, "dependents": 0 }""";
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void crearEmpleadoValido_devuelve201_conCodigo() throws Exception {
        String token = loginAdmin();
        String payload = """
                { "rut": "11111111-1", "firstName": "Luis", "lastName": "Torres",
                  "hireDate": "2024-01-01",
                  "baseSalary": 800000, "dependents": 0,
                  "pensionSystem": "AFP", "afpFund": "HABITAT", "healthSystem": "FONASA" }""";
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rut").value("11.111.111-1"))
                // El empleado no tiene usuario: la API devuelve su código de marcaje.
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    @Order(4)
    void deshabilitarEmpleado() throws Exception {
        String token = loginAdmin();
        mockMvc.perform(put("/api/employees/1/enabled")
                        .header("Authorization", "Bearer " + token)
                        .param("enabled", "false"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void eliminarEmpleado() throws Exception {
        String token = loginAdmin();
        mockMvc.perform(delete("/api/employees/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
