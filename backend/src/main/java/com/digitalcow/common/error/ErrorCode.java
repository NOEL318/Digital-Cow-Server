package com.digitalcow.common.error;

/**
 * Codigos de error custom mapeados a messageKey i18n del frontend.
 * Convencion: SCOPE_REASON. Cada nuevo error debe agregarse aqui.
 */
public enum ErrorCode {
    INTERNAL_ERROR("errors.internal"),
    VALIDATION_ERROR("errors.validation"),
    UNAUTHENTICATED("errors.unauthenticated"),
    FORBIDDEN("errors.forbidden"),
    NOT_FOUND("errors.notFound"),
    CONFLICT("errors.conflict"),

    AUTH_INVALID_CREDENTIALS("errors.auth.invalidCredentials"),
    AUTH_EMAIL_NOT_VERIFIED("errors.auth.emailNotVerified"),
    AUTH_USER_DISABLED("errors.auth.userDisabled"),
    AUTH_TOKEN_INVALID("errors.auth.tokenInvalid"),
    AUTH_TOKEN_EXPIRED("errors.auth.tokenExpired"),
    AUTH_REFRESH_INVALID("errors.auth.refreshInvalid"),
    AUTH_EMAIL_ALREADY_USED("errors.auth.emailAlreadyUsed"),

    INVITATION_INVALID("errors.invitation.invalid"),
    INVITATION_EXPIRED("errors.invitation.expired"),
    INVITATION_ALREADY_ACCEPTED("errors.invitation.alreadyAccepted"),

    RANCH_HAS_ANIMALS("errors.ranch.hasAnimals"),
    LOT_HAS_ANIMALS("errors.lot.hasAnimals"),

    ANIMAL_TAG_DUPLICATE("errors.animal.tagDuplicate"),
    ANIMAL_OFFICIAL_TAG_DUPLICATE("errors.animal.officialTagDuplicate"),
    ANIMAL_NOT_DELETABLE("errors.animal.notDeletable"),

    PHOTO_PUBLIC_ID_INVALID("errors.photo.publicIdInvalid"),
    PHOTO_NOT_FOUND("errors.photo.notFound"),
    PHOTO_SERVICE_UNAVAILABLE("errors.photo.serviceUnavailable");

    private final String messageKey;
    ErrorCode(String messageKey) { this.messageKey = messageKey; }
    /** Este metodo devuelve la clave i18n asociada al codigo de error. */
    public String messageKey() { return messageKey; }
}
