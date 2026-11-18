package com.helpdeskpro.helpdeskpro_backend.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ResultadoImpactoFinanciero(
        boolean estimable,
        String mensaje,
        Long clienteId,
        String ruc,
        String razonSocial,
        String moneda,
        String nivelRiesgo,
        BigDecimal exposicionDirecta,
        BigDecimal montoSemanal,
        Integer diasOperativos,
        BigDecimal montoDiario,
        Integer diasInterrupcion,
        BigDecimal volumenAfectado,
        BigDecimal comision,
        BigDecimal perdidaIngresos,
        String categoria,
        List<String> advertencias,
        Map<String, String> fuentes) {
}
