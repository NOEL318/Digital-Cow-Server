package com.digitalcow.ranch.condition;

import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.ranch.condition.dto.LotConditionCreateRequest;
import com.digitalcow.ranch.condition.dto.LotConditionDto;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * CRUD ligero de condiciones del corral. Cualquier rol del tenant
 * puede leer; OWNER, ADMIN, MANAGER y WORKER pueden registrar
 * (es captura de campo, ampliamos el rol de escritura).
 */
@Service
public class LotConditionService {

    private final LotConditionRepository repository;

    public LotConditionService(LotConditionRepository repository) {
        this.repository = repository;
    }

    /** Este metodo lista las condiciones recientes de un lote. */
    @Transactional(readOnly = true)
    public List<LotConditionDto> listByLot(Long lotId) {
        return repository.findTop100ByLotIdOrderByObservedAtDescIdDesc(lotId)
            .stream().map(LotConditionService::toDto).toList();
    }

    /** Este metodo crea la condicion del lote. */
    @Transactional
    public LotConditionDto create(LotConditionCreateRequest req) {
        LotCondition c = new LotCondition();
        c.setAccountId(TenantContext.requireAccountId());
        c.setLotId(req.lotId());
        c.setObservedAt(req.observedAt());
        c.setKind(req.kind());
        c.setSeverity(req.severity());
        c.setCustomLabel(req.customLabel());
        c.setNotes(req.notes());
        c.setRecordedByUserId(CurrentUser.require().userId());
        return toDto(repository.save(c));
    }

    /** Este metodo elimina la condicion del lote. */
    @Transactional
    public void delete(Long id) {
        LotCondition c = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Condicion no encontrada"));
        repository.delete(c);
    }

    private static LotConditionDto toDto(LotCondition c) {
        return new LotConditionDto(
            c.getId(),
            c.getLotId(),
            c.getObservedAt(),
            c.getKind(),
            c.getSeverity(),
            c.getCustomLabel(),
            c.getNotes()
        );
    }
}
