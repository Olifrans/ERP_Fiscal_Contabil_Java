package com.senai.escola.repository;



import com.senai.escola.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("SELECT a FROM AuditLog a WHERE a.empresaId = :eid AND a.instante BETWEEN :i AND :f ORDER BY a.instante DESC")
    List<AuditLog> findByPeriodo(@Param("eid") Long eid, @Param("i") LocalDateTime i, @Param("f") LocalDateTime f);
}