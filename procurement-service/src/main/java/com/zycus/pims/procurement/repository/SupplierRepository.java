package com.zycus.pims.procurement.repository;

import com.zycus.pims.procurement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByTaxId(String taxId);
    boolean existsByTaxId(String taxId);
}
