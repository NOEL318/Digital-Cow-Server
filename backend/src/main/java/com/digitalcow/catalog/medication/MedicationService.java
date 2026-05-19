package com.digitalcow.catalog.medication;

import com.digitalcow.catalog.medication.dto.MedicationDto;
import com.digitalcow.catalog.medication.dto.MedicationUpsertRequest;
import com.digitalcow.catalog.medication.mapper.MedicationMapper;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

/**
 * Logica de catalogo de medicamentos. Cada tenant ve unicamente sus
 * propias entradas. El codigo interno se autogenera a partir del
 * nombre cuando el usuario no lo provee, y el nombre en ingles se
 * copia del español por defecto.
 */
@Service
public class MedicationService {

    private final MedicationRepository repository;
    private final MedicationMapper mapper;

    public MedicationService(MedicationRepository repository, MedicationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Este metodo lista los medicamentos. */
    @Transactional(readOnly = true)
    public List<MedicationDto> list() {
        Long accountId = TenantContext.requireAccountId();
        return repository.findVisibleForAccount(accountId)
            .stream().map(mapper::toDto).toList();
    }

    /** Este metodo devuelve el medicamento. */
    @Transactional(readOnly = true)
    public MedicationDto get(Long id) {
        Long accountId = TenantContext.requireAccountId();
        Medication m = repository.findVisibleByIdForAccount(id, accountId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento no encontrado"));
        return mapper.toDto(m);
    }

    /** Este metodo busca el medicamento barcode. */
    @Transactional(readOnly = true)
    public Optional<MedicationDto> findByBarcode(String barcode) {
        Long accountId = TenantContext.requireAccountId();
        return repository.findByBarcodeForAccount(barcode, accountId).map(mapper::toDto);
    }

    /** Este metodo crea el medicamento. */
    @CacheEvict(value = "catalog-medications", allEntries = true)
    @Transactional
    public MedicationDto create(MedicationUpsertRequest req) {
        Long accountId = TenantContext.requireAccountId();
        Medication m = new Medication();
        m.setAccountId(accountId);
        applyRequest(m, req);
        return mapper.toDto(repository.save(m));
    }

    /** Este metodo actualiza el medicamento. */
    @CacheEvict(value = "catalog-medications", allEntries = true)
    @Transactional
    public MedicationDto update(Long id, MedicationUpsertRequest req) {
        Long accountId = TenantContext.requireAccountId();
        Medication m = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento no encontrado"));
        if (m.getAccountId() == null || !accountId.equals(m.getAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes editar este medicamento");
        }
        applyRequest(m, req);
        return mapper.toDto(m);
    }

    /** Este metodo elimina el medicamento. */
    @CacheEvict(value = "catalog-medications", allEntries = true)
    @Transactional
    public void delete(Long id) {
        Long accountId = TenantContext.requireAccountId();
        Medication m = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento no encontrado"));
        if (m.getAccountId() == null || !accountId.equals(m.getAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar este medicamento");
        }
        repository.delete(m);
    }

    private void applyRequest(Medication m, MedicationUpsertRequest req) {
        String name = req.nameEs() == null ? "" : req.nameEs().trim();
        m.setNameEs(name);
        // Si el usuario no manda nombre en ingles, usamos el mismo (no es campo visible).
        String nameEn = (req.nameEn() == null || req.nameEn().isBlank()) ? name : req.nameEn().trim();
        m.setNameEn(nameEn);
        // Codigo se autogenera del nombre si no viene; siempre normalizado.
        String code = (req.code() == null || req.code().isBlank()) ? slug(name) : req.code();
        m.setCode(normalizeCode(code));
        m.setActiveIngredient(req.activeIngredient());
        m.setManufacturer(req.manufacturer());
        m.setPresentation(req.presentation());
        m.setBarcode(blankToNull(req.barcode()));
        m.setExpiresAt(req.expiresAt());
        m.setCategory(mapper.orDefault(req.category()));
        m.setDefaultDose(req.defaultDose());
        m.setDefaultRoute(req.defaultRoute());
        if (req.withdrawalMilkDays() != null) m.setWithdrawalMilkDays(req.withdrawalMilkDays());
        if (req.withdrawalMeatDays() != null) m.setWithdrawalMeatDays(req.withdrawalMeatDays());
        m.setNotes(req.notes());
    }

    private static String normalizeCode(String code) {
        if (code == null || code.isBlank()) return null;
        return code.trim().toUpperCase().replaceAll("\\s+", "_");
    }

    /** Convierte el nombre en un slug ASCII para usar como code. */
    private static String slug(String name) {
        if (name == null) return null;
        String n = Normalizer.normalize(name, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .replaceAll("[^A-Za-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
        if (n.isEmpty()) return "MED_" + System.currentTimeMillis();
        return n.length() > 60 ? n.substring(0, 60) : n;
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
