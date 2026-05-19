package com.digitalcow.report;

import com.digitalcow.report.dto.AnimalReportDto;
import com.digitalcow.report.dto.HealthSummaryDto;
import com.digitalcow.report.dto.InventoryReportDto;
import com.digitalcow.report.dto.SalesHistoryDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Endpoints REST de reportes (datos para vistas imprimibles y CSV cliente-side). */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    /** Este metodo devuelve el reporte completo de un animal. */
    @GetMapping("/animal/{animalId}")
    public AnimalReportDto animal(@PathVariable Long animalId) {
        return service.animalReport(animalId);
    }

    /** Este metodo devuelve el reporte de inventario actual del rancho. */
    @GetMapping("/inventory")
    public InventoryReportDto inventory() {
        return service.inventoryReport();
    }

    /** Este metodo devuelve el historial de ventas en el rango de fechas indicado. */
    @GetMapping("/sales-history")
    public SalesHistoryDto salesHistory(
        @RequestParam LocalDate from,
        @RequestParam LocalDate to
    ) {
        return service.salesHistory(from, to);
    }

    /** Este metodo devuelve el resumen de salud del rebano en el rango indicado. */
    @GetMapping("/health-summary")
    public HealthSummaryDto healthSummary(
        @RequestParam LocalDate from,
        @RequestParam LocalDate to
    ) {
        return service.healthSummary(from, to);
    }
}
