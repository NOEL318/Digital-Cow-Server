package com.digitalcow.account;

import com.digitalcow.finance.category.ExpenseCategory;
import com.digitalcow.finance.category.ExpenseCategoryRepository;
import com.digitalcow.finance.category.ExpenseKind;
import com.digitalcow.finance.category.IncomeCategory;
import com.digitalcow.finance.category.IncomeCategoryRepository;
import com.digitalcow.finance.category.IncomeKind;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Crea las categorias por defecto para una cuenta recien creada.
 * Antes eran globales (account_id NULL) y el usuario no podia
 * editarlas; ahora cada cuenta arranca con su propio set de
 * categorias preconfiguradas y puede modificarlas o eliminarlas.
 */
@Service
public class AccountSeeding {

    private final ExpenseCategoryRepository expenseRepo;
    private final IncomeCategoryRepository incomeRepo;

    public AccountSeeding(ExpenseCategoryRepository expenseRepo,
                           IncomeCategoryRepository incomeRepo) {
        this.expenseRepo = expenseRepo;
        this.incomeRepo = incomeRepo;
    }

    /** Este metodo crea las categorias de gasto e ingreso por defecto para la cuenta. */
    @Transactional
    public void seedDefaultCategories(Long accountId) {
        for (Map.Entry<String, Object[]> e : DEFAULT_EXPENSE.entrySet()) {
            ExpenseCategory c = new ExpenseCategory();
            c.setAccountId(accountId);
            c.setCode(e.getKey());
            c.setNameEs((String) e.getValue()[0]);
            c.setNameEn((String) e.getValue()[1]);
            c.setKind((ExpenseKind) e.getValue()[2]);
            expenseRepo.save(c);
        }
        for (Map.Entry<String, Object[]> e : DEFAULT_INCOME.entrySet()) {
            IncomeCategory c = new IncomeCategory();
            c.setAccountId(accountId);
            c.setCode(e.getKey());
            c.setNameEs((String) e.getValue()[0]);
            c.setNameEn((String) e.getValue()[1]);
            c.setKind((IncomeKind) e.getValue()[2]);
            incomeRepo.save(c);
        }
    }

    /** code -> [nombre ES, nombre EN, tipo]. */
    private static final Map<String, Object[]> DEFAULT_EXPENSE = new java.util.LinkedHashMap<>(List.of(
        Map.entry("ALIMENTACION",   new Object[]{"Alimentacion",      "Feed",            ExpenseKind.FEED}),
        Map.entry("SALUD_ANIMAL",   new Object[]{"Salud animal",      "Animal health",   ExpenseKind.HEALTH}),
        Map.entry("MANO_DE_OBRA",   new Object[]{"Mano de obra",      "Labor",           ExpenseKind.LABOR}),
        Map.entry("INFRAESTRUCTURA",new Object[]{"Infraestructura",   "Infrastructure",  ExpenseKind.INFRASTRUCTURE}),
        Map.entry("TRANSPORTE",     new Object[]{"Transporte",        "Transport",       ExpenseKind.TRANSPORT}),
        Map.entry("REPRODUCCION",   new Object[]{"Reproduccion",      "Reproduction",    ExpenseKind.REPRODUCTION}),
        Map.entry("COMPRA_ANIMALES",new Object[]{"Compra de animales","Animal purchase", ExpenseKind.OTHER}),
        Map.entry("OTROS",          new Object[]{"Otros",             "Other",           ExpenseKind.OTHER})
    ).stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
        (a, b) -> a, java.util.LinkedHashMap::new)));

    private static final Map<String, Object[]> DEFAULT_INCOME = new java.util.LinkedHashMap<>(List.of(
        Map.entry("VENTA_ANIMAL",   new Object[]{"Venta de animal",   "Animal sale",     IncomeKind.ANIMAL_SALE}),
        Map.entry("VENTA_LECHE",    new Object[]{"Venta de leche",    "Milk sale",       IncomeKind.MILK_SALE}),
        Map.entry("SUBPRODUCTO",    new Object[]{"Subproducto",       "Byproduct",       IncomeKind.BYPRODUCT}),
        Map.entry("SERVICIOS",      new Object[]{"Servicios",         "Services",        IncomeKind.SERVICE}),
        Map.entry("OTROS_INGRESOS", new Object[]{"Otros ingresos",    "Other income",    IncomeKind.OTHER})
    ).stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
        (a, b) -> a, java.util.LinkedHashMap::new)));
}
