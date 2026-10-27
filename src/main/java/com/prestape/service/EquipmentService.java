package com.prestape.service;

import java.util.List;

import com.prestape.config.ReportProperties;
import com.prestape.constants.EquipmentConstants;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.common.SelectOption;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EquipmentService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentService.class);

    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository categoryRepository;
    private final ReportProperties reportProperties;

    public EquipmentService(
            EquipmentRepository equipmentRepository,
            EquipmentCategoryRepository categoryRepository,
            ReportProperties reportProperties
    ) {
        this.equipmentRepository = equipmentRepository;
        this.categoryRepository = categoryRepository;
        this.reportProperties = reportProperties;
    }

    @Transactional(readOnly = true)
    public PageResponse<EquipmentListResponse> list(PaginationRequest pagination, EquipmentFilterRequest filter) {
        return PageResponse.from(equipmentRepository
                .findAll(buildSpecification(pagination.search(), filter), pagination.toPageable(EquipmentConstants.DEFAULT_SORT))
                .map(EquipmentListResponse::from));
    }

    @Transactional(readOnly = true)
    public List<EquipmentListResponse> listForExport(String search, EquipmentFilterRequest filter) {
        Specification<Equipment> spec = buildSpecification(search, filter);

        int maxRows = reportProperties.maxRows();
        if (equipmentRepository.count(spec) > maxRows) {
            throw new BusinessException(
                    "La exportación supera las " + maxRows + " filas. Aplica más filtros");
        }

        return equipmentRepository.findAll(spec, EquipmentConstants.DEFAULT_SORT).stream()
                .map(EquipmentListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SelectOption> options(Long kitId) {
        return equipmentRepository.findAssignableToKit(kitId == null ? 0L : kitId, EquipmentConstants.OPTIONS_SORT)
                .stream()
                .map(equipment -> new SelectOption(equipment.getId(), equipment.getName(), equipment.getCode()))
                .toList();
    }

    @Transactional
    public void create(CreateEquipmentRequest request) {
        String serialNumber = request.serialNumber().trim();
        if (equipmentRepository.existsBySerialNumberIgnoreCase(serialNumber)) {
            throw new ConflictException("La serie ya está registrada");
        }

        Equipment equipment = new Equipment();
        equipment.setCode(RandomCodeGenerator.generateUnique(
                EquipmentConstants.CODE_PREFIX, EquipmentConstants.CODE_LENGTH, equipmentRepository::existsByCode));
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

        equipmentRepository.removeFromKits(id);
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

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
