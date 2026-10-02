package com.senai.escola.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;


package com.erp.fiscal.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record LancamentoRequest(
    @NotNull Long empresaId,
    @NotNull LocalDate data,
    @NotBlank String historico,
    String documento,
    @NotEmpty List<ItemDto> itens
) {
    public record ItemDto(
        @NotNull Long contaId,
        @NotNull String tipo,      // "DEBITO" ou "CREDITO"
        @NotNull @Positive BigDecimal valor
    ) {}
}