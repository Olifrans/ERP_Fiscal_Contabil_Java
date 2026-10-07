//package com.senai.escola.audit;
//
//
//
//import com.senai.escola.entity.AuditLog;
//import com.senai.escola.repository.AuditLogRepository;
//import com.senai.escola.tenant.TenantContext;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.persistence.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//import java.time.LocalDateTime;
//
//@Component
//@RequiredArgsConstructor
//public class AuditListener {
//    private final AuditLogRepository repo;
//    private final ObjectMapper mapper;
//
//    @PrePersist
//    public void prePersist(Object e) { log("CREATE", e); }
//
//    @PreUpdate
//    public void preUpdate(Object e) { log("UPDATE", e); }
//
//    @PreRemove
//    public void preRemove(Object e) { log("DELETE", e); }
//
//    private void log(String acao, Object e) {
//        try {
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            String user = (auth != null && auth.getName() != null) ? auth.getName() : "system";
//            Long entityId = extrairId(e);
//            String payload = mapper.writeValueAsString(e);
//            if (payload.length() > 60000) payload = payload.substring(0, 60000) + "...(truncado)";
//
//            AuditLog log = AuditLog.builder()
//                    .instante(LocalDateTime.now())
//                    .usuario(user)
//                    .empresaId(TenantContext.get())
//                    .acao(acao)
//                    .entidade(e.getClass().getSimpleName())
//                    .entityId(entityId)
//                    .payload(payload)
//                    .build();
//            repo.save(log);
//        } catch (Exception ignore) {
//            // auditoria nunca deve quebrar o fluxo
//        }
//    }
//
//    private Long extrairId(Object e) {
//        try {
//            Method m = e.getClass().getMethod("getId");
//            Object v = m.invoke(e);
//            return v instanceof Number n ? n.longValue() : null;
//        } catch (Exception ex) {
//            return null;
//        }
//    }
//}





package com.senai.escola.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.escola.entity.AuditLog;
import com.senai.escola.repository.AuditLogRepository;
import com.senai.escola.tenant.TenantContext;
import jakarta.persistence.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
public class AuditListener {

    private final AuditLogRepository auditLogRepo;
    private final ObjectMapper objectMapper;

    // ✅ Construtor manual com @Lazy para quebrar o ciclo
    public AuditListener(@Lazy AuditLogRepository auditLogRepo, ObjectMapper objectMapper) {
        this.auditLogRepo = auditLogRepo;
        this.objectMapper = objectMapper;
    }

    @PrePersist
    public void prePersist(Object entity) {
        logAction("CREATE", entity);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        logAction("UPDATE", entity);
    }

    @PreRemove
    public void preRemove(Object entity) {
        logAction("DELETE", entity);
    }

    private void logAction(String action, Object entity) {
        try {
            // 1. Descobrir o usuário logado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String usuario = (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser"))
                    ? auth.getName()
                    : "system";

            // 2. Extrair o ID da entidade via Reflection
            Long entityId = extrairId(entity);

            // 3. Converter a entidade em JSON para o payload (limitado a 50.000 caracteres)
            String payload = objectMapper.writeValueAsString(entity);
            if (payload.length() > 50000) {
                payload = payload.substring(0, 50000) + "... [TRUNCADO]";
            }

            // 4. Salvar o log no banco
            AuditLog log = AuditLog.builder()
                    .instante(LocalDateTime.now())
                    .usuario(usuario)
                    .empresaId(TenantContext.get())
                    .acao(action)
                    .entidade(entity.getClass().getSimpleName())
                    .entityId(entityId)
                    .payload(payload)
                    .build();

            auditLogRepo.save(log);

        } catch (Exception e) {
            // A auditoria NUNCA deve quebrar o fluxo principal
            System.err.println("Erro ao registrar auditoria: " + e.getMessage());
        }
    }

    private Long extrairId(Object entity) {
        try {
            Method getIdMethod = entity.getClass().getMethod("getId");
            Object idValue = getIdMethod.invoke(entity);
            return idValue instanceof Number ? ((Number) idValue).longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }
}