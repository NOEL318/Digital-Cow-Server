package com.digitalcow.photo;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.photo.dto.*;
import com.digitalcow.tenancy.TenantContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/** Firma uploads, confirma fotos, listado, eliminacion y marcado de cover. */
@Service
public class PhotoService {

    private final CloudinarySignatureService sigSvc;
    private final AnimalPhotoRepository photos;
    private final AnimalRepository animals;

    public PhotoService(CloudinarySignatureService sigSvc,
                        AnimalPhotoRepository photos,
                        AnimalRepository animals) {
        this.sigSvc = sigSvc;
        this.photos = photos;
        this.animals = animals;
    }

    /** Este metodo firma la subida. */
    public SignUploadResponse signUpload(Long animalId) {
        Animal a = requireAnimal(animalId);
        var s = sigSvc.sign(a.getAccountId(), a.getId());
        return new SignUploadResponse(s.cloudName(), s.apiKey(), s.timestamp(),
            s.folder(), s.tags(), s.signature());
    }

    /** Este metodo confirma la foto. */
    @Transactional
    public PhotoDto confirm(Long animalId, ConfirmPhotoRequest req) {
        Animal a = requireAnimal(animalId);
        String expected = "accounts/" + a.getAccountId() + "/animals/" + a.getId();
        validatePublicId(req.publicId(), expected);
        AnimalPhoto p = new AnimalPhoto();
        p.setAnimalId(a.getId());
        p.setCloudinaryPublicId(req.publicId());
        p.setCloudinaryUrl(req.url());
        p.setWidth(req.width());
        p.setHeight(req.height());
        p.setBytes(req.bytes());
        p.setTakenAt(req.takenAt());
        p.setUploadedByUserId(CurrentUser.require().userId());
        photos.save(p);
        return toDto(p);
    }

    /** Este metodo lista las fotos. */
    public List<PhotoDto> list(Long animalId) {
        requireAnimal(animalId);
        return photos.findAllByAnimalIdOrderByCreatedAtDesc(animalId).stream()
            .map(this::toDto).toList();
    }

    /** Este metodo elimina la foto. */
    @Transactional
    public void delete(Long animalId, Long photoId) {
        AnimalPhoto p = photos.findById(photoId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.PHOTO_NOT_FOUND, "Photo not found"));
        if (!p.getAnimalId().equals(animalId)) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cross-animal");
        }
        photos.delete(p);
    }

    /** Este metodo marca una foto existente como portada del animal. */
    @Transactional
    public void setCover(Long animalId, Long photoId) {
        Animal a = requireAnimal(animalId);
        AnimalPhoto p = photos.findById(photoId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.PHOTO_NOT_FOUND, "Photo not found"));
        if (!p.getAnimalId().equals(animalId)) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cross-animal");
        }
        a.setCoverPhotoId(p.getId());
    }

    /** Verifica que public_id pertenezca a la carpeta esperada (defensa contra IDs ajenos). */
    public static boolean validatePublicId(String publicId, String expectedPrefix) {
        if (publicId == null || !publicId.startsWith(expectedPrefix + "/")) {
            throw BusinessException.badRequest(ErrorCode.PHOTO_PUBLIC_ID_INVALID, "Bad public_id");
        }
        return true;
    }

    private Animal requireAnimal(Long animalId) {
        Animal a = animals.findById(animalId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal not found"));
        if (!a.getAccountId().equals(TenantContext.get())) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cross-tenant");
        }
        return a;
    }

    private PhotoDto toDto(AnimalPhoto p) {
        return new PhotoDto(p.getId(), p.getCloudinaryPublicId(), p.getCloudinaryUrl(),
            p.getWidth(), p.getHeight(), p.getBytes(), p.getTakenAt(), p.getCreatedAt());
    }
}
