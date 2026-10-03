package com.senai.escola.dto;


import java.math.BigDecimal;
import java.util.Map;

public record DreResponse(
    BigDecimal receitaBruta,
    BigDecimal deducoes,
    BigDecimal receitaLiquida,
    BigDecimal custoMercadoria,
    BigDecimal lucroBruto,
    BigDecimal despesasOperacionais,
    BigDecimal lucroOperacional,
    BigDecimal irpj,
    BigDecimal csll,
    BigDecimal lucroLiquido,
    Map<String, BigDecimal> detalheReceitas,
    Map<String, BigDecimal> detalheDespesas
) {}