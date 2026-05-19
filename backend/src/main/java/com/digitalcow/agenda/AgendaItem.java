package com.digitalcow.agenda;

import java.time.LocalDate;

/**
 * Item de la agenda diaria. Cada item tiene un tipo, un animal o
 * lote relacionado, una fecha esperada y una nota corta. El tipo
 * permite a la UI elegir icono y color.
 */
public record AgendaItem(
    String type,
    Long animalId,
    String animalTag,
    Long lotId,
    String lotName,
    LocalDate dueDate,
    String message,
    String severity
) { }
