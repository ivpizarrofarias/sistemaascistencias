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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración del módulo de remuneraciones:
 * se crea un empleado, se registra un anticipo y una licencia, y se genera la
 * liquidación del mes verificando los totales y los detalles.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pay_it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PayrollControllerIntegrationTest {

    private static final String RUT = "12345678-5";

    @Autowired MockMvc mockMvc;

    private String token(String username, String password) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(res.getResponse().getContentAsString(), "$.token");
    }

    private String loginAdmin() throws Exception {
        return token("admin", "admin123");
    }

    @Test
    @Order(1)
    void adminCreaEmpleado() throws Exception {
        String token = loginAdmin();
        String payload = """
                { "rut": "%s", "firstName": "Ana", "lastName": "Rojas",
                  "hireDate": "2022-05-01", "baseSalary": 1500000,
                  "dependents": 1, "pensionSystem": "AFP", "afpFund": "HABITAT",
                  "healthSystem": "ISAPRE" }""".formatted(RUT);
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void adminRegistraAnticipo() throws Exception {
        String token = loginAdmin();
        mockMvc.perform(post("/api/payroll/employee/1/advances")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-08-10\",\"amount\":100000,\"description\":\"anticipo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(100000.0));
    }

    @Test
    @Order(3)
    void adminRegistraLicencia() throws Exception {
        String token = loginAdmin();
        mockMvc.perform(post("/api/payroll/employee/1/licencias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "startDate": "2026-08-17", "endDate": "2026-08-19",
                                  "motivo": "Enfermedad común", "employerPays": true }"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalDays").value(3));
    }

    @Test
    @Order(4)
    void adminGeneraLiquidacion_conDetalles() throws Exception {
        String token = loginAdmin();
        mockMvc.perform(post("/api/payroll/liquidaciones/employee/1")
                        .header("Authorization", "Bearer " + token)
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.totalHaberes").exists())
                .andExpect(jsonPath("$.totalDescuentos").exists())
                .andExpect(jsonPath("$.liquidoAPagar").exists())
                .andExpect(jsonPath("$.detalles").isArray())
                .andExpect(jsonPath("$.detalles").isNotEmpty());
    }

    @Test
    @Order(5)
    void sinAutenticacion_noPuedeGenerarLiquidaciones() throws Exception {
        // Sin token (los empleados ya no tienen cuenta de acceso) -> 401.
        mockMvc.perform(post("/api/payroll/liquidaciones/todos")
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isUnauthorized());
    }
}
