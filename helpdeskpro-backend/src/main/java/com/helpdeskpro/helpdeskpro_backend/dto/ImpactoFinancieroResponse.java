package com.helpdeskpro.helpdeskpro_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ImpactoFinancieroResponse(
        String incidenciaId,
        Long eventoId,
        String eventoCodigo,
        Long clienteId,
        String rucCliente,
        String razonSocialCliente,
        String moneda,
        String categoria,
        String confiabilidadExposicion,
        BigDecimal exposicionDirectaPotencial,
        BigDecimal volumenPotencialAfectado,
        BigDecimal perdidaEstimadaIngresos,
        BigDecimal montoSemanal,
        BigDecimal montoDiario,
        Integer diasOperativos,
        Integer diasInterrupcion,
        BigDecimal comision,
        String fuentesCalculo,
        String explicacionCalculo,
        LocalDateTime fechaCalculo,
        String mensajePotencial) {
}
