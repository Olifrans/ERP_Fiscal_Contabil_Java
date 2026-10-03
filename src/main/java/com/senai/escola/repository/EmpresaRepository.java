package com.senai.escola.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.escola.entity.Empresa;
import java.util.Optional;


public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByCnpj(String cnpj);
}