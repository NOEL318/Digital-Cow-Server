package com.digitalcow.reproduction.alerts.dto;

import java.util.List;

/** Conjunto de alertas reproductivas del tenant. */
public record ReproductionAlertsDto(
    List<AlertItemDto> upcomingCalvings21d,
    List<AlertItemDto> dryOffDue,
    List<AlertItemDto> servedWithoutCheck,
    List<AlertItemDto> openTooLong
) { }
