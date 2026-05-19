package com.digitalcow.feeding.record.event;

/** Evento publicado tras crear/actualizar/eliminar FeedingRecord. */
public record FeedingRecordChangedEvent(Long accountId) { }
