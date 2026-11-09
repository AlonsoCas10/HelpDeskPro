package com.helpdeskpro.helpdeskpro_backend.hu03;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.helpdeskpro.helpdeskpro_backend.model.Cliente;
import com.helpdeskpro.helpdeskpro_backend.model.ConfiguracionEmpresa;
import com.helpdeskpro.helpdeskpro_backend.model.Deudor;
import com.helpdeskpro.helpdeskpro_backend.repository.ClienteRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.ConfiguracionEmpresaRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.DeudorRepository;
import com.helpdeskpro.helpdeskpro_backend.service.ExcelClienteVipService;
import com.helpdeskpro.helpdeskpro_backend.service.ExcelDeudorService;

/**
 * Pruebas unitarias de QA para HU03: validación de las filas en la carga de clientes y deudores.
 * Usan repositorios simulados (no tocan la base) y arman el Excel en memoria.
 */
@ExtendWith(MockitoExtension.class)
class HU03ExcelCargaTest {

    private static final String[] CABECERA_CLIENTES = {
        "RUC", "Razón social", "Dominio autorizado", "Correo autorizado",
        "Monto promedio habitual", "Monto máximo habitual", "VIP", "Estado"
    };
    private static final String[] CABECERA_DEUDORES = {
        "RUC", "Razón social", "Dominio autorizado", "Correo autorizado", "Estado"
    };

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private DeudorRepository deudorRepository;

    @Mock
    private ConfiguracionEmpresaRepository configuracionRepository;

    private final List<Cliente> clientesGuardados = new ArrayList<>();
    private final List<Deudor> deudoresGuardados = new ArrayList<>();

    @BeforeEach
    void simularRepositorios() {
        ConfiguracionEmpresa configuracion = new ConfiguracionEmpresa();
        configuracion.setId(1L);
        lenient().when(configuracionRepository.findById(anyLong())).thenReturn(Optional.of(configuracion));
        lenient().when(clienteRepository.findByRuc(anyString())).thenReturn(Optional.empty());
        lenient().when(deudorRepository.findByRuc(anyString())).thenReturn(Optional.empty());
        lenient().when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> {
            clientesGuardados.add(i.getArgument(0));
            return i.getArgument(0);
        });
        lenient().when(deudorRepository.save(any(Deudor.class))).thenAnswer(i -> {
            deudoresGuardados.add(i.getArgument(0));
            return i.getArgument(0);
        });
    }

    private static MockMultipartFile excel(String[] cabecera, Object[]... filas) throws Exception {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Hoja1");
            Row primera = hoja.createRow(0);
            for (int c = 0; c < cabecera.length; c++) {
                primera.createCell(c).setCellValue(cabecera[c]);
            }
            for (int r = 0; r < filas.length; r++) {
                Row fila = hoja.createRow(r + 1);
                for (int c = 0; c < filas[r].length; c++) {
                    Object valor = filas[r][c];
                    if (valor instanceof Number numero) {
                        fila.createCell(c).setCellValue(numero.doubleValue());
                    } else if (valor != null) {
                        fila.createCell(c).setCellValue(valor.toString());
                    }
                }
            }
            libro.write(salida);
            return new MockMultipartFile("archivo", "qa-hu03.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", salida.toByteArray());
        }
    }

    private ExcelClienteVipService clientes() {
        return new ExcelClienteVipService(clienteRepository, configuracionRepository);
    }

    private ExcelDeudorService deudores() {
        return new ExcelDeudorService(deudorRepository, configuracionRepository);
    }

    @Test
    @DisplayName("HU03-CP-U-09 No guardar una fila de deudor incompleta")
    void HU03_CP_U_09_no_guardar_deudor_incompleto() throws Exception {
        int procesados = deudores().importarDeudores(
                excel(CABECERA_DEUDORES, new Object[] {"20999000009", "", "qa.invalid", "pagos@qa.invalid", "Activo"}), 1L);

        // El servicio solo devuelve un conteo: que la fila se informe como rechazada se comprueba por API (HU03-CP-A-08).
        assertAll(
                () -> assertEquals(0, procesados, "la fila incompleta no debe contarse como procesada"),
                () -> assertTrue(deudoresGuardados.isEmpty(), "la fila sin razón social no debe guardarse"));
    }

    @Test
    @DisplayName("HU03-CP-U-11 Rechazar VIP y estado desconocidos")
    void HU03_CP_U_11_rechazar_vip_y_estado_desconocidos() throws Exception {
        clientes().importarClientesVip(excel(CABECERA_CLIENTES,
                new Object[] {"20999000011", "QA VIP Yes", "qa.invalid", "pagos@qa.invalid", 1000, 5000, "Yes", "Activo"},
                new Object[] {"20999000012", "QA Estado Talvez", "qa.invalid", "pagos@qa.invalid", 1000, 5000, "No", "Talvez"}), 1L);

        assertTrue(clientesGuardados.isEmpty(),
                "las filas con VIP «Yes» y estado «Talvez» deben rechazarse, pero se guardaron "
                        + clientesGuardados.size() + " como no VIP o inactivas");
    }

    @Test
    @DisplayName("HU03-CP-U-12 Rechazar un monto que no es numérico")
    void HU03_CP_U_12_rechazar_monto_no_numerico() throws Exception {
        clientes().importarClientesVip(excel(CABECERA_CLIENTES,
                new Object[] {"20999000013", "QA Monto abc", "qa.invalid", "pagos@qa.invalid", "abc", 5000, "Sí", "Activo"}), 1L);

        assertTrue(clientesGuardados.isEmpty(),
                "la fila con monto «abc» debe rechazarse, pero se guardó con monto promedio "
                        + (clientesGuardados.isEmpty() ? "" : clientesGuardados.get(0).getMontoPromedioHabitual()));
    }

    @Test
    @DisplayName("HU03-CP-U-13 Conservar ausente un monto vacío")
    void HU03_CP_U_13_conservar_ausente_monto_vacio() throws Exception {
        clientes().importarClientesVip(excel(CABECERA_CLIENTES,
                new Object[] {"20999000014", "QA Montos vacíos", "qa.invalid", "pagos@qa.invalid", null, null, "Sí", "Activo"}), 1L);

        Cliente guardado = clientesGuardados.get(0);
        assertAll(
                () -> assertNull(guardado.getMontoPromedioHabitual(), "un monto promedio vacío debe quedar ausente, no en 0"),
                () -> assertNull(guardado.getMontoMaximoHabitual(), "un monto máximo vacío debe quedar ausente, no en 0"));
    }

    @Test
    @DisplayName("HU03-CP-U-14 Rechazar montos negativos o incoherentes")
    void HU03_CP_U_14_rechazar_montos_negativos_o_incoherentes() throws Exception {
        clientes().importarClientesVip(excel(CABECERA_CLIENTES,
                new Object[] {"20999000015", "QA Monto negativo", "qa.invalid", "pagos@qa.invalid", -500, 5000, "Sí", "Activo"},
                new Object[] {"20999000016", "QA Máximo menor", "qa.invalid", "pagos@qa.invalid", 5000, 1000, "Sí", "Activo"}), 1L);

        assertTrue(clientesGuardados.isEmpty(),
                "la fila con monto negativo y la de máximo menor que el promedio deben rechazarse, pero se guardaron "
                        + clientesGuardados.size());
    }

    @Test
    @DisplayName("HU03-CP-U-15 Conservar ausentes dominio y correo")
    void HU03_CP_U_15_conservar_ausentes_dominio_y_correo() throws Exception {
        clientes().importarClientesVip(excel(CABECERA_CLIENTES,
                new Object[] {"20999000017", "QA Sin dominio", null, null, 1000, 5000, "Sí", "Activo"}), 1L);

        Cliente guardado = clientesGuardados.get(0);
        assertAll(
                () -> assertNull(guardado.getDominioAutorizado(), "un dominio ausente debe quedar ausente, no como texto vacío"),
                () -> assertNull(guardado.getCorreoAutorizado(), "un correo ausente debe quedar ausente, no como texto vacío"));
    }
}
