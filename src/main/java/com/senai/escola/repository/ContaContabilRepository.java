package com.senai.escola.repository;



package com.erp.fiscal.repository;

import com.erp.fiscal.entity.*;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDate;
import java.util.List;

public interface ContaContabilRepository extends JpaRepository<ContaContabil, Long> {
    List<ContaContabil> findByGrupo(ContaContabil.Grupo grupo);
}

