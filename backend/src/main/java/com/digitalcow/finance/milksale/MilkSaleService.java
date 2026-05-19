package com.digitalcow.finance.milksale;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.finance.category.IncomeCategory;
import com.digitalcow.finance.category.IncomeCategoryRepository;
import com.digitalcow.finance.category.IncomeKind;
import com.digitalcow.finance.income.Income;
import com.digitalcow.finance.income.IncomeRepository;
import com.digitalcow.finance.income.IncomeSourceType;
import com.digitalcow.finance.milksale.dto.MilkSaleCreateDto;
import com.digitalcow.finance.milksale.dto.MilkSaleResponseDto;
import com.digitalcow.finance.milksale.dto.MilkSaleUpdateDto;
import com.digitalcow.finance.milksale.event.MilkSaleChangedEvent;
import com.digitalcow.finance.milksale.mapper.MilkSaleMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de MilkSale. La creacion crea income automatico (source_type=MILK_SALE).
 * El borrado revierte el income asociado.
 */
@Service
@Transactional
public class MilkSaleService {

    private final MilkSaleRepository repository;
    private final MilkSaleMapper mapper;
    private final IncomeRepository incomeRepository;
    private final IncomeCategoryRepository incomeCategoryRepository;
    private final ApplicationEventPublisher events;

    public MilkSaleService(MilkSaleRepository repository,
                           MilkSaleMapper mapper,
                           IncomeRepository incomeRepository,
                           IncomeCategoryRepository incomeCategoryRepository,
                           ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.incomeRepository = incomeRepository;
        this.incomeCategoryRepository = incomeCategoryRepository;
        this.events = events;
    }

    /** Lista paginada por defecto ordenada por sale_date desc. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public Page<MilkSaleResponseDto> list(Pageable pageable) {
        Pageable sorted = pageable.getSort().isSorted()
            ? pageable
            : org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "saleDate"));
        return repository.findAll(sorted).map(mapper::toDto);
    }

    /** Devuelve una venta. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public MilkSaleResponseDto get(Long id) {
        MilkSale entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Milk sale not found"));
        return mapper.toDto(entity);
    }

    /** Crea una venta de leche y un income automatico asociado. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public MilkSaleResponseDto create(MilkSaleCreateDto dto) {
        MilkSale entity = mapper.fromCreate(dto);
        entity.setCreatedByUserId(CurrentUser.require().userId());
        if (entity.getCurrency() == null) entity.setCurrency("MXN");
        MilkSale saved = repository.save(entity);

        IncomeCategory category = resolveMilkSaleCategory();
        Income income = new Income();
        income.setIncomeCategoryId(category.getId());
        income.setReceivedAt(saved.getSaleDate());
        income.setAmount(saved.getTotalPrice());
        income.setCurrency(saved.getCurrency());
        income.setRanchId(saved.getRanchId());
        income.setPayer(saved.getBuyer());
        income.setSourceType(IncomeSourceType.MILK_SALE);
        income.setSourceId(saved.getId());
        income.setCreatedByUserId(saved.getCreatedByUserId());
        incomeRepository.save(income);

        events.publishEvent(new MilkSaleChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Actualiza una venta y sincroniza el income asociado. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public MilkSaleResponseDto update(Long id, MilkSaleUpdateDto dto) {
        MilkSale entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Milk sale not found"));
        mapper.applyUpdate(dto, entity);

        Income income = incomeRepository
            .findFirstBySourceTypeAndSourceId(IncomeSourceType.MILK_SALE, entity.getId())
            .orElse(null);
        if (income != null) {
            income.setReceivedAt(entity.getSaleDate());
            income.setAmount(entity.getTotalPrice());
            if (entity.getCurrency() != null) income.setCurrency(entity.getCurrency());
            income.setPayer(entity.getBuyer());
            income.setRanchId(entity.getRanchId());
        }

        events.publishEvent(new MilkSaleChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Borra la venta y su income automatico. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        MilkSale entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Milk sale not found"));

        incomeRepository.findFirstBySourceTypeAndSourceId(IncomeSourceType.MILK_SALE, entity.getId())
            .ifPresent(incomeRepository::delete);

        repository.delete(entity);
        events.publishEvent(new MilkSaleChangedEvent(TenantContext.requireAccountId()));
    }

    /** Resuelve la categoria MILK_SALE: preferir cuenta propia, fallback a global. */
    private IncomeCategory resolveMilkSaleCategory() {
        Long accountId = TenantContext.requireAccountId();
        return incomeCategoryRepository
            .findFirstByKindAndAccountId(IncomeKind.MILK_SALE, accountId)
            .or(() -> incomeCategoryRepository.findFirstByKindAndAccountIdIsNull(IncomeKind.MILK_SALE))
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND,
                "No income category for MILK_SALE"));
    }
}
