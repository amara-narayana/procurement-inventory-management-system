package com.zycus.pims.inventory.service;

import com.zycus.pims.common.exception.DuplicateResourceException;
import com.zycus.pims.common.exception.ResourceNotFoundException;
import com.zycus.pims.inventory.dto.WarehouseDto;
import com.zycus.pims.inventory.dto.WarehouseRequest;
import com.zycus.pims.inventory.entity.Warehouse;
import com.zycus.pims.inventory.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<WarehouseDto> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WarehouseDto getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        return mapToDto(warehouse);
    }

    @Transactional
    public WarehouseDto createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Warehouse with name '" + request.getName() + "' already exists");
        }
        Warehouse warehouse = new Warehouse(request.getName(), request.getLocation(), request.getDescription());
        return mapToDto(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseDto updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));

        if (!warehouse.getName().equalsIgnoreCase(request.getName()) && warehouseRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Warehouse with name '" + request.getName() + "' already exists");
        }

        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setDescription(request.getDescription());
        return mapToDto(warehouseRepository.save(warehouse));
    }

    private WarehouseDto mapToDto(Warehouse warehouse) {
        return new WarehouseDto(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getDescription(),
                warehouse.isActive(),
                warehouse.getCreatedAt()
        );
    }
}
