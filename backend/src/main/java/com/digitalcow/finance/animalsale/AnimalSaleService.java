package com.digitalcow.finance.animalsale;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.animal.AnimalStatus;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.finance.animalsale.dto.AnimalSaleCreateDto;
import com.digitalcow.finance.animalsale.dto.AnimalSaleResponseDto;
import com.digitalcow.finance.animalsale.dto.AnimalSaleUpdateDto;
import com.digitalcow.finance.animalsale.event.AnimalSaleChangedEvent;
import com.digitalcow.finance.animalsale.mapper.AnimalSaleMapper;
import com.digitalcow.finance.category.IncomeCategory;
import com.digitalcow.finance.category.IncomeCategoryRepository;
import com.digitalcow.finance.category.IncomeKind;
import com.digitalcow.finance.income.Income;
import com.digitalcow.finance.income.IncomeRepository;
import com.digitalcow.finance.income.IncomeSourceType;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de AnimalSale. La creacion cambia animal.status=SOLD y crea income automatico.
 * El borrado revierte ambas operaciones.
 */
@Service
@Transactional
public class AnimalSaleService {

    private final AnimalSaleRepository repository;
    private final AnimalSaleMapper mapper;
    private final AnimalRepository animalRepository;
    private final IncomeRepository incomeRepository;
    private final IncomeCategoryRepository incomeCategoryRepository;
    private final ApplicationEventPublisher events;

    public AnimalSaleService(AnimalSaleRepository repository,
                             AnimalSaleMapper mapper,
                             AnimalRepository animalRepository,
                             IncomeRepository incomeRepository,
                             IncomeCategoryRepository incomeCategoryRepository,
                             ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.animalRepository = animalRepository;
        this.incomeRepository = incomeRepository;
        this.incomeCategoryRepository = incomeCategoryRepository;
        this.events = events;
    }

    /** Lista paginada por defecto ordenada por sold_at desc. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public Page<AnimalSaleResponseDto> list(Pageable pageable) {
        Pageable sorted = pageable.getSort().isSorted()
            ? pageable
            : org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "soldAt"));
        return repository.findAll(sorted).map(mapper::toDto);
    }

    /** Devuelve una venta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public AnimalSaleResponseDto get(Long id) {
        AnimalSale entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal sale not found"));
        return mapper.toDto(entity);
    }

    /**
     * Crea una venta de animal.
     * Valida que el animal este activo, lo marca como SOLD y crea income automatico.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public AnimalSaleResponseDto create(AnimalSaleCreateDto dto) {
        Animal animal = animalRepository.findById(dto.animalId())
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal not found"));
        if (animal.getStatus() != AnimalStatus.ACTIVE) {
            throw BusinessException.conflict(ErrorCode.CONFLICT, "Animal is not active");
        }
        animal.setStatus(AnimalStatus.SOLD);

        AnimalSale entity = mapper.fromCreate(dto);
        entity.setCreatedByUserId(CurrentUser.require().userId());
        if (entity.getCurrency() == null) entity.setCurrency("MXN");
        AnimalSale saved;
        try {
            saved = repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException ex) {
            throw BusinessException.conflict(ErrorCode.CONFLICT,
                "Animal already sold");
        }

        IncomeCategory category = resolveAnimalSaleCategory();
        Income income = new Income();
        income.setIncomeCategoryId(category.getId());
        income.setReceivedAt(saved.getSoldAt());
        income.setAmount(saved.getTotalPrice());
        income.setCurrency(saved.getCurrency());
        income.setAnimalId(saved.getAnimalId());
        income.setRanchId(animal.getRanchId());
        income.setLotId(animal.getLotId());
        income.setPayer(saved.getBuyer());
        income.setSourceType(IncomeSourceType.ANIMAL_SALE);
        income.setSourceId(saved.getId());
        income.setCreatedByUserId(saved.getCreatedByUserId());
        incomeRepository.save(income);

        events.publishEvent(new AnimalSaleChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /**
     * Actualiza una venta. Si cambia totalPrice/soldAt/buyer/currency, sincroniza el income.
     * No permite cambiar animal_id.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public AnimalSaleResponseDto update(Long id, AnimalSaleUpdateDto dto) {
        AnimalSale entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal sale not found"));
        mapper.applyUpdate(dto, entity);

        Income income = incomeRepository
            .findFirstBySourceTypeAndSourceId(IncomeSourceType.ANIMAL_SALE, entity.getId())
            .orElse(null);
        if (income != null) {
            income.setReceivedAt(entity.getSoldAt());
            income.setAmount(entity.getTotalPrice());
            if (entity.getCurrency() != null) income.setCurrency(entity.getCurrency());
            income.setPayer(entity.getBuyer());
        }

        events.publishEvent(new AnimalSaleChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /**
     * Borra una venta. Revierte animal.status=ACTIVE y borra income automatico.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        AnimalSale entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal sale not found"));

        incomeRepository.findFirstBySourceTypeAndSourceId(IncomeSourceType.ANIMAL_SALE, entity.getId())
            .ifPresent(incomeRepository::delete);

        Animal animal = animalRepository.findById(entity.getAnimalId()).orElse(null);
        if (animal != null && animal.getStatus() == AnimalStatus.SOLD) {
            animal.setStatus(AnimalStatus.ACTIVE);
        }

        repository.delete(entity);
        events.publishEvent(new AnimalSaleChangedEvent(TenantContext.requireAccountId()));
    }

    /** Resuelve la categoria ANIMAL_SALE: preferir cuenta propia, fallback a global. */
    private IncomeCategory resolveAnimalSaleCategory() {
        Long accountId = TenantContext.requireAccountId();
        return incomeCategoryRepository
            .findFirstByKindAndAccountId(IncomeKind.ANIMAL_SALE, accountId)
            .or(() -> incomeCategoryRepository.findFirstByKindAndAccountIdIsNull(IncomeKind.ANIMAL_SALE))
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND,
                "No income category for ANIMAL_SALE"));
    }
}
