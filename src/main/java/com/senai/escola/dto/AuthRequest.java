package com.senai.escola.dto;



import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
    @NotBlank String login,
    @NotBlank String senha
) {}