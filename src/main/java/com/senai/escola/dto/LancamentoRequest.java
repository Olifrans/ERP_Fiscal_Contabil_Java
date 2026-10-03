package com.senai.escola.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record LancamentoRequest(
    @NotNull LocalDate data,
    @NotNull String historico,
    String documento,
    String tipo,
    @NotEmpty List<ItemDto> itens
) {
    public record ItemDto(
        @NotNull Long contaId,
        @NotNull String tipo,
        @NotNull @Positive BigDecimal valor
    ) {}
}