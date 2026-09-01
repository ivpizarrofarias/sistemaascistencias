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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración del flujo de permisos laborales:
 * 1) El admin crea un empleado.
 * 2) El admin registra una solicitud de permiso a nombre del empleado.
 * 3) El admin aprueba el permiso.
 * 4) Solo el admin puede ver todos los permisos (sin token -> 401).
 * Nota: los empleados no inician sesión (solo tienen su código de marcaje).
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:permiso_it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PermisoLaboralControllerIntegrationTest {

    private static final String RUT = "20286349-3";

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
    void adminCreaEmpleado_paraSolicitudDePermiso() throws Exception {
        String token = loginAdmin();
        String payload = """
                { "rut": "%s", "firstName": "Pedro", "lastName": "Lopez",
                  "hireDate": "2023-02-01", "baseSalary": 900000,
                  "dependents": 0, "pensionSystem": "AFP", "afpFund": "HABITAT",
                  "healthSystem": "FONASA" }""".formatted(RUT);
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rut").value("20.286.349-3"));
    }

    @Test
    @Order(2)
    void adminRegistraPermisoDelEmpleado() throws Exception {
        String token = loginAdmin();
        mockMvc.perform(post("/api/permisos/employee/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "date": "2026-08-14", "tipo": "POR_HORA", "horas": 2,
                                  "motivo": "Trámite personal", "formaCompensacion": "DESCUENTO" }"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @Order(3)
    void adminApruebaPermiso() throws Exception {
        String token = loginAdmin();
        mockMvc.perform(put("/api/permisos/1/validar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"APROBADO\",\"observacion\":\"aprobado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"));
    }

    @Test
    @Order(4)
    void soloAdminVeTodosLosPermisos() throws Exception {
        // Sin token (los empleados no tienen cuenta) -> 401.
        mockMvc.perform(get("/api/permisos"))
                .andExpect(status().isUnauthorized());

        // Admin sí puede.
        String adminToken = loginAdmin();
        mockMvc.perform(get("/api/permisos")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void adminCreaPermisoQuedaAprobado() throws Exception {
        String token = loginAdmin();
        mockMvc.perform(post("/api/permisos/admin/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "date": "2026-08-20", "tipo": "POR_DIA", "dias": 1,
                                  "motivo": "Permiso administrativo", "formaCompensacion": "DEVOLVER" }"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.formaCompensacion").value("DEVOLVER"));
    }
}
