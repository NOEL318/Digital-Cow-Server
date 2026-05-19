package com.digitalcow.animal.purchase;

import com.digitalcow.animal.AnimalService;
import com.digitalcow.animal.dto.AnimalResponse;
import com.digitalcow.finance.category.ExpenseCategory;
import com.digitalcow.finance.category.ExpenseCategoryRepository;
import com.digitalcow.finance.category.ExpenseKind;
import com.digitalcow.finance.expense.ExpenseService;
import com.digitalcow.finance.expense.dto.ExpenseCreateDto;
import com.digitalcow.finance.expense.dto.ExpenseResponseDto;
import com.digitalcow.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Coordina la creacion atomica de animal + gasto de compra. Una sola
 * transaccion: si la creacion del gasto falla, el animal tambien hace
 * rollback. Si no hay precio de compra, solo se crea el animal.
 */
@Service
public class AnimalPurchaseService {

    private static final String DEFAULT_PURCHASE_CATEGORY_CODE = "COMPRA_ANIMALES";

    private final AnimalService animalService;
    private final ExpenseService expenseService;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    public AnimalPurchaseService(AnimalService animalService,
                                  ExpenseService expenseService,
                                  ExpenseCategoryRepository expenseCategoryRepository) {
        this.animalService = animalService;
        this.expenseService = expenseService;
        this.expenseCategoryRepository = expenseCategoryRepository;
    }

    /** Este metodo registra la compra de un animal y crea el gasto asociado. */
    @Transactional
    public AnimalPurchaseResponse purchase(AnimalPurchaseRequest req) {
        AnimalResponse animal = animalService.create(req.animal());
        ExpenseResponseDto expense = null;
        if (req.purchasePrice() != null) {
            Long categoryId = req.expenseCategoryId() != null
                ? req.expenseCategoryId()
                : resolveDefaultCategoryId();
            ExpenseCreateDto expenseDto = new ExpenseCreateDto(
                categoryId,
                req.purchasedAt() != null ? req.purchasedAt() : LocalDate.now(),
                req.purchasePrice(),
                req.purchaseCurrency(),
                req.animal().ranchId(),
                req.animal().lotId(),
                animal.id(),
                buildDescription(req, animal),
                req.seller(),
                null
            );
            expense = expenseService.create(expenseDto);
        }
        return new AnimalPurchaseResponse(animal, expense);
    }

    private Long resolveDefaultCategoryId() {
        Long accountId = TenantContext.requireAccountId();
        return expenseCategoryRepository
            .findFirstByAccountIdAndCode(accountId, DEFAULT_PURCHASE_CATEGORY_CODE)
            .map(ExpenseCategory::getId)
            .orElseGet(() -> {
                ExpenseCategory created = new ExpenseCategory();
                created.setAccountId(accountId);
                created.setCode(DEFAULT_PURCHASE_CATEGORY_CODE);
                created.setNameEs("Compra de animales");
                created.setNameEn("Animal purchase");
                created.setKind(ExpenseKind.OTHER);
                return expenseCategoryRepository.save(created).getId();
            });
    }

    private static String buildDescription(AnimalPurchaseRequest req, AnimalResponse animal) {
        StringBuilder sb = new StringBuilder("Compra de animal ").append(animal.internalTag());
        if (req.seller() != null && !req.seller().isBlank()) {
            sb.append(" a ").append(req.seller());
        }
        if (req.notes() != null && !req.notes().isBlank()) {
            sb.append(". ").append(req.notes());
        }
        return sb.length() > 400 ? sb.substring(0, 400) : sb.toString();
    }
}
