package com.senai.escola.config;



import com.senai.escola.entity.Empresa;
import com.senai.escola.entity.Usuario;
import com.senai.escola.repository.EmpresaRepository;
import com.senai.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final EmpresaRepository empresaRepo;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // 1. Garante que existe pelo menos uma empresa (ID 1)
            if (empresaRepo.count() == 0) {
                Empresa emp = new Empresa();
                emp.setRazaoSocial("Empresa Demo LTDA");
                emp.setCnpj("12.345.678/0001-90");
                emp.setInscricaoEstadual("123.456.789.000");
                emp.setMunicipio("São Paulo");
                emp.setUf("SP");
                emp.setRegimeTributario("LUCRO_REAL");
                empresaRepo.save(emp);
                System.out.println("✅ Empresa Demo criada com ID: " + emp.getId());
            }

            // 2. Garante que o usuário admin existe com a senha correta
            if (usuarioRepo.findByLogin("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setLogin("admin");
                // O Spring gera o hash BCrypt válido para "admin123" neste exato momento!
                admin.setSenhaHash(passwordEncoder.encode("admin123")); 
                admin.setNome("Administrador");
                admin.setEmail("admin@erp.com");
                admin.setPerfil("ADMIN");
                admin.setEmpresaId(1L); 
                admin.setAtivo(true);
                usuarioRepo.save(admin);
                System.out.println("✅ Usuário 'admin' criado com sucesso! Senha: admin123");
            } else {
                System.out.println("ℹ️ Usuário 'admin' já existe no banco.");
            }
        };
    }
}