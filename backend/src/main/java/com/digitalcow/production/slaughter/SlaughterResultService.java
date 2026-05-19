package com.digitalcow.production.slaughter;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.production.slaughter.dto.SlaughterResultCreateDto;
import com.digitalcow.production.slaughter.dto.SlaughterResultResponseDto;
import com.digitalcow.production.slaughter.dto.SlaughterResultUpdateDto;
import com.digitalcow.production.slaughter.event.SlaughterResultChangedEvent;
import com.digitalcow.production.slaughter.mapper.SlaughterResultMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Servicio CRUD de SlaughterResult. Calcula yield_pct = carcass/live*100 si no viene
 * y ambos pesos estan presentes. Redondeo a 2 decimales.
 */
@Service
@Transactional
public class SlaughterResultService {

    private final SlaughterResultRepository repository;
    private final SlaughterResultMapper mapper;
    private final ApplicationEventPublisher events;

    public SlaughterResultService(SlaughterResultRepository repository,
                                  SlaughterResultMapper mapper,
                                  ApplicationEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    /** Este metodo lista los resultados de faena. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER','VIEWER')")
    public List<SlaughterResultResponseDto> list() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Este metodo crea el resultado de faena. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public SlaughterResultResponseDto create(SlaughterResultCreateDto dto) {
        SlaughterResult entity = mapper.fromCreate(dto);
        applyYieldIfMissing(entity);
        SlaughterResult saved = repository.save(entity);
        events.publishEvent(new SlaughterResultChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(saved);
    }

    /** Este metodo actualiza el resultado de faena. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public SlaughterResultResponseDto update(Long id, SlaughterResultUpdateDto dto) {
        SlaughterResult entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Slaughter result not found"));
        mapper.applyUpdate(dto, entity);
        applyYieldIfMissing(entity);
        events.publishEvent(new SlaughterResultChangedEvent(TenantContext.requireAccountId()));
        return mapper.toDto(entity);
    }

    /** Este metodo elimina el resultado de faena. */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public void delete(Long id) {
        SlaughterResult entity = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Slaughter result not found"));
        repository.delete(entity);
        events.publishEvent(new SlaughterResultChangedEvent(TenantContext.requireAccountId()));
    }

    /**
     * Calcula yield_pct = carcass/live*100 si no viene en el dto y ambos pesos estan
     * presentes. Redondeo a 2 decimales.
     */
    private void applyYieldIfMissing(SlaughterResult entity) {
        if (entity.getYieldPct() != null) return;
        BigDecimal live = entity.getLiveWeightKg();
        BigDecimal carc = entity.getCarcassWeightKg();
        if (live == null || carc == null) return;
        if (live.signum() <= 0) return;
        BigDecimal pct = carc.multiply(BigDecimal.valueOf(100))
            .divide(live, 2, RoundingMode.HALF_UP);
        entity.setYieldPct(pct);
    }
}
