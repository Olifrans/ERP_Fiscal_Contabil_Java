package com.senai.escola.dto;



public record AuthResponse(
    String token,
    String nome,
    String perfil,
    Long empresaId,
    Long usuarioId
) {}