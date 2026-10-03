package com.senai.escola.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.escola.entity.Empresa;
import com.senai.escola.entity.Usuario;

import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);
}