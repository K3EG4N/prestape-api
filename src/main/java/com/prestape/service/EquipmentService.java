package com.prestape.service;

import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.request.equipment.create.CreateEquipmentRequest;
import com.prestape.dto.request.equipment.list.EquipmentFilterRequest;
import com.prestape.dto.request.equipment.update.UpdateEquipmentRequest;
import com.prestape.dto.response.EquipmentListResponse;
import com.prestape.entity.Equipment;
import com.prestape.entity.EquipmentCategory;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.exception.BusinessException;
import com.prestape.exception.ConflictException;
import com.prestape.exception.ResourceNotFoundException;
import com.prestape.repository.EquipmentCategoryRepository;
import com.prestape.repository.EquipmentRepository;
import com.prestape.specifications.EquipmentSpecifications;
import com.prestape.util.RandomCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EquipmentService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentService.class);

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private static final String CODE_PREFIX = "EQ-";
    private static final int CODE_LENGTH = 4;
    private static final int MAX_CODE_ATTEMPTS = 10;

    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository categoryRepository;

    public EquipmentService(
            EquipmentRepository equipmentRepository,
            EquipmentCategoryRepository categoryRepository
    ) {
        this.equipmentRepository = equipmentRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<EquipmentListResponse> list(PaginationRequest pagination, EquipmentFilterRequest filter) {
        return PageResponse.from(equipmentRepository
                .findAll(buildSpecification(pagination.search(), filter), pagination.toPageable(DEFAULT_SORT))
                .map(EquipmentListResponse::from));
    }

    @Transactional
    public void create(CreateEquipmentRequest request) {
        String serialNumber = request.serialNumber().trim();
        if (equipmentRepository.existsBySerialNumberIgnoreCase(serialNumber)) {
            throw new ConflictException("La serie ya está registrada");
        }

        Equipment equipment = new Equipment();
        equipment.setCode(generateUniqueCode());
        equipment.setName(request.name().trim());
        equipment.setSerialNumber(serialNumber);
        equipment.setDescription(trimToNull(request.description()));
        equipment.setCategory(findCategory(request.categoryId()));
        equipment.setCondition(request.condition());
        equipment.setConditionNotes(trimToNull(request.conditionNotes()));

        Equipment saved = equipmentRepository.save(equipment);
        log.info("Equipo creado: equipmentId={}, code={}", saved.getId(), saved.getCode());
    }

    @Transactional
    public void update(Long id, UpdateEquipmentRequest request) {
        Equipment equipment = findEquipment(id);
        String serialNumber = request.serialNumber().trim();

        if (equipmentRepository.existsBySerialNumberIgnoreCaseAndIdNot(serialNumber, id)) {
            throw new ConflictException("La serie ya está registrada");
        }

        validateAvailabilityChange(equipment.getAvailability(), request.availability());

        equipment.setName(request.name().trim());
        equipment.setSerialNumber(serialNumber);
        equipment.setDescription(trimToNull(request.description()));
        equipment.setCategory(findCategory(request.categoryId()));
        equipment.setAvailability(request.availability());
        equipment.setCondition(request.condition());
        equipment.setConditionNotes(trimToNull(request.conditionNotes()));
        log.info("Equipo actualizado: equipmentId={}", id);
    }

    @Transactional
    public void delete(Long id) {
        Equipment equipment = findEquipment(id);

        if (equipment.getAvailability() == EquipmentAvailability.LOANED) {
            throw new BusinessException("No se puede eliminar un equipo que está prestado");
        }

        equipmentRepository.delete(equipment);
        log.info("Equipo eliminado: equipmentId={}", id);
    }

    private void validateAvailabilityChange(EquipmentAvailability current, EquipmentAvailability requested) {
        if (current == requested) {
            return;
        }
        if (current == EquipmentAvailability.LOANED) {
            throw new BusinessException("No puedes cambiar la disponibilidad de un equipo prestado");
        }
        if (requested == EquipmentAvailability.LOANED) {
            throw new BusinessException("La disponibilidad Prestado se asigna al registrar un préstamo");
        }
    }

    private Specification<Equipment> buildSpecification(String search, EquipmentFilterRequest filter) {
        Specification<Equipment> spec = Specification.unrestricted();

        if (StringUtils.hasText(search)) {
            spec = spec.and(EquipmentSpecifications.matchesSearch(search.trim()));
        }
        if (!filter.categoryIds().isEmpty()) {
            spec = spec.and(EquipmentSpecifications.hasAnyCategory(filter.categoryIds()));
        }
        if (!filter.availabilities().isEmpty()) {
            spec = spec.and(EquipmentSpecifications.hasAnyAvailability(filter.availabilities()));
        }
        if (!filter.conditions().isEmpty()) {
            spec = spec.and(EquipmentSpecifications.hasAnyCondition(filter.conditions()));
        }

        return spec;
    }

    private Equipment findEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
    }

    private EquipmentCategory findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            String code = CODE_PREFIX + RandomCodeGenerator.generate(CODE_LENGTH);
            if (!equipmentRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("No se pudo generar un código único para el equipo");
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
