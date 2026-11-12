package com.zycus.pims.procurement.service;

import com.zycus.pims.common.exception.DuplicateResourceException;
import com.zycus.pims.common.exception.ResourceNotFoundException;
import com.zycus.pims.procurement.dto.SupplierDto;
import com.zycus.pims.procurement.dto.SupplierRequest;
import com.zycus.pims.procurement.entity.Supplier;
import com.zycus.pims.procurement.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public List<SupplierDto> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierDto getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        return mapToDto(supplier);
    }

    @Transactional
    public SupplierDto createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByTaxId(request.getTaxId())) {
            throw new DuplicateResourceException("Supplier with Tax ID '" + request.getTaxId() + "' already exists");
        }
        Supplier supplier = new Supplier(
                request.getName(),
                request.getContactPerson(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress(),
                request.getTaxId(),
                request.getStatus()
        );
        return mapToDto(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierDto updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        if (!supplier.getTaxId().equalsIgnoreCase(request.getTaxId()) && supplierRepository.existsByTaxId(request.getTaxId())) {
            throw new DuplicateResourceException("Supplier with Tax ID '" + request.getTaxId() + "' already exists");
        }

        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setTaxId(request.getTaxId());
        if (request.getStatus() != null) {
            supplier.setStatus(request.getStatus());
        }

        return mapToDto(supplierRepository.save(supplier));
    }

    private SupplierDto mapToDto(Supplier s) {
        return new SupplierDto(
                s.getId(),
                s.getName(),
                s.getContactPerson(),
                s.getEmail(),
                s.getPhone(),
                s.getAddress(),
                s.getTaxId(),
                s.getStatus(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
