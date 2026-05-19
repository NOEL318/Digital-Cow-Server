package com.digitalcow.ranch.condition;

/**
 * Tipos de condicion que se pueden registrar en un lote. Cubre
 * factores ambientales (lodo, lluvia, calor), sanitarios (plagas) y
 * de infraestructura. CUSTOM permite al usuario describir cualquier
 * cosa que no entre en los predefinidos.
 */
public enum LotConditionKind {
    MUD_LOW, MUD_MEDIUM, MUD_HIGH,
    RAIN_LIGHT, RAIN_HEAVY, DROUGHT,
    HEAT_WAVE, COLD,
    FLIES, TICKS, OTHER_PEST,
    WATER_OK, WATER_LOW, WATER_OUT,
    PASTURE_GOOD, PASTURE_LOW,
    INFRA_DAMAGE,
    CUSTOM
}
