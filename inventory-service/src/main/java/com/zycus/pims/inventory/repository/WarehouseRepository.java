package com.zycus.pims.inventory.repository;

import com.zycus.pims.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByName(String name);
    boolean existsByName(String name);
    List<Warehouse> findByActiveTrue();
}
