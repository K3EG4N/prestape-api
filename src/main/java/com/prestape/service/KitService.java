package com.prestape.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.prestape.config.ReportProperties;
import com.prestape.constants.KitConstants;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.request.kit.create.CreateKitRequest;
import com.prestape.dto.request.kit.list.KitFilterRequest;
import com.prestape.dto.request.kit.update.UpdateKitRequest;
import com.prestape.dto.response.KitListResponse;
import com.prestape.entity.Equipment;
import com.prestape.entity.Kit;
import com.prestape.exception.BusinessException;
import com.prestape.exception.ConflictException;
import com.prestape.exception.ResourceNotFoundException;
import com.prestape.repository.EquipmentRepository;
import com.prestape.repository.KitRepository;
import com.prestape.specifications.KitSpecifications;
import com.prestape.util.RandomCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class KitService {

    private static final Logger log = LoggerFactory.getLogger(KitService.class);

    private final KitRepository kitRepository;
    private final EquipmentRepository equipmentRepository;
    private final ReportProperties reportProperties;

    public KitService(
            KitRepository kitRepository,
            EquipmentRepository equipmentRepository,
            ReportProperties reportProperties
    ) {
        this.kitRepository = kitRepository;
        this.equipmentRepository = equipmentRepository;
        this.reportProperties = reportProperties;
    }

    @Transactional(readOnly = true)
    public PageResponse<KitListResponse> list(PaginationRequest pagination, KitFilterRequest filter) {
        return PageResponse.from(kitRepository
                .findAll(buildSpecification(pagination.search(), filter), pagination.toPageable(KitConstants.DEFAULT_SORT))
                .map(KitListResponse::from));
    }

    @Transactional(readOnly = true)
    public List<KitListResponse> listForExport(String search, KitFilterRequest filter) {
        Specification<Kit> spec = buildSpecification(search, filter);

        int maxRows = reportProperties.maxRows();
        if (kitRepository.count(spec) > maxRows) {
            throw new BusinessException(
                    "La exportación supera las " + maxRows + " filas. Aplica más filtros");
        }

        return kitRepository.findAll(spec, KitConstants.DEFAULT_SORT).stream()
                .map(KitListResponse::from)
                .toList();
    }

    @Transactional
    public void create(CreateKitRequest request) {
        Kit kit = new Kit();
        kit.setCode(RandomCodeGenerator.generateUnique(
                KitConstants.CODE_PREFIX, KitConstants.CODE_LENGTH, kitRepository::existsByCode));
        kit.setName(request.name().trim());
        kit.setDescription(trimToNull(request.description()));
        kit.setEquipments(findAssignableEquipments(request.equipmentIds(), null));

        Kit saved = kitRepository.save(kit);
        log.info("Kit creado: kitId={}, code={}", saved.getId(), saved.getCode());
    }

    @Transactional
    public void update(Long id, UpdateKitRequest request) {
        Kit kit = findKit(id);

        kit.setName(request.name().trim());
        kit.setDescription(trimToNull(request.description()));
        kit.getEquipments().clear();
        kit.getEquipments().addAll(findAssignableEquipments(request.equipmentIds(), id));
        log.info("Kit actualizado: kitId={}", id);
    }

    @Transactional
    public void delete(Long id) {
        Kit kit = findKit(id);

        kit.getEquipments().clear();
        kitRepository.delete(kit);
        log.info("Kit eliminado: kitId={}", id);
    }

    private Set<Equipment> findAssignableEquipments(List<Long> equipmentIds, Long kitId) {
        Set<Long> ids = new HashSet<>(equipmentIds);
        List<Equipment> equipments = equipmentRepository.findAllById(ids);

        if (equipments.size() != ids.size()) {
            throw new ResourceNotFoundException("Uno o más equipos seleccionados no existen");
        }

        List<KitRepository.AssignedEquipment> assigned =
                kitRepository.findAssignedToOtherKits(ids, kitId == null ? 0L : kitId);
        if (!assigned.isEmpty()) {
            KitRepository.AssignedEquipment first = assigned.getFirst();
            throw new ConflictException(
                    "El equipo " + first.getEquipmentName() + " ya pertenece al kit " + first.getKitName());
        }

        return new HashSet<>(equipments);
    }

    private Specification<Kit> buildSpecification(String search, KitFilterRequest filter) {
        Specification<Kit> spec = Specification.unrestricted();

        if (StringUtils.hasText(search)) {
            spec = spec.and(KitSpecifications.matchesSearch(search.trim()));
        }
        if (!filter.availabilities().isEmpty()) {
            spec = spec.and(KitSpecifications.hasAnyAvailability(filter.availabilities()));
        }

        return spec;
    }

    private Kit findKit(Long id) {
        return kitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kit no encontrado"));
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
