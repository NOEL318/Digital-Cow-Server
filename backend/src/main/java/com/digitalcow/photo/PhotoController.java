package com.digitalcow.photo;

import com.digitalcow.photo.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints de gestion de fotos asociadas a un animal. */
@RestController
@RequestMapping("/api/v1/animals/{animalId}")
public class PhotoController {

    private final PhotoService svc;

    public PhotoController(PhotoService svc) { this.svc = svc; }

    /** Este metodo lista las fotos. */
    @GetMapping("/photos")
    public List<PhotoDto> list(@PathVariable Long animalId) { return svc.list(animalId); }

    /** Este metodo devuelve una firma para que el cliente suba la foto a Cloudinary. */
    @PostMapping("/photos/sign-upload")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public SignUploadResponse sign(@PathVariable Long animalId) { return svc.signUpload(animalId); }

    /** Este metodo confirma que la foto fue subida y la asocia al animal. */
    @PostMapping("/photos/confirm")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public PhotoDto confirm(@PathVariable Long animalId, @Valid @RequestBody ConfirmPhotoRequest req) {
        return svc.confirm(animalId, req);
    }

    /** Este metodo elimina una foto del animal. */
    @DeleteMapping("/photos/{photoId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public ResponseEntity<Void> delete(@PathVariable Long animalId, @PathVariable Long photoId) {
        svc.delete(animalId, photoId);
        return ResponseEntity.noContent().build();
    }

    /** Este metodo marca una foto del animal como portada. */
    @PatchMapping("/cover-photo/{photoId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public ResponseEntity<Void> setCover(@PathVariable Long animalId, @PathVariable Long photoId) {
        svc.setCover(animalId, photoId);
        return ResponseEntity.noContent().build();
    }
}
