package com.exam.demo.service;

import com.exam.demo.dto.ImageResponse;
import com.exam.demo.endpoint.event.EventProducer;
import com.exam.demo.endpoint.event.model.ConvertImageToBlackAndWhiteRequested;
import com.exam.demo.entity.ImageEntity;
import com.exam.demo.file.bucket.BucketComponent;
import com.exam.demo.repository.ImageRepository;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class ImageService {

  private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png");

  private final BucketComponent bucketComponent;
  private final ImageRepository imageRepository;
  private final EventProducer<ConvertImageToBlackAndWhiteRequested> eventProducer;

  @SneakyThrows
  public ImageResponse uploadImage(MultipartFile file, String email) {
    validateFile(file);

    var id = UUID.randomUUID();
    var extension = "image/png".equals(file.getContentType()) ? ".png" : ".jpg";
    var bucketKey = id + extension;

    var tempFile = File.createTempFile("upload-", extension);
    file.transferTo(tempFile);
    bucketComponent.upload(tempFile, bucketKey);
    tempFile.delete();

    var entity =
        ImageEntity.builder()
            .id(id)
            .nomFichier(bucketKey)
            .email(email)
            .dateCreation(LocalDateTime.now())
            .build();
    imageRepository.save(entity);

    // --- 3. DÉCLENCHEMENT ASYNCHRONE ---
    // On ne fait QUE produire l'événement ici : pas de conversion d'image,
    // pas d'envoi de mail. Le Worker s'en chargera en arrière-plan.
    var event =
        ConvertImageToBlackAndWhiteRequested.builder().bucketKey(bucketKey).email(email).build();
    eventProducer.accept(List.of(event));

    var presignedUrl = bucketComponent.presign(bucketKey, Duration.ofMinutes(10)).toString();
    return toResponse(entity, presignedUrl);
  }

  public List<ImageResponse> getAllImages() {
    return imageRepository.findAllOrderByDateCreationDesc().stream()
        .map(
            entity ->
                toResponse(
                    entity,
                    bucketComponent
                        .presign(entity.getNomFichier(), Duration.ofMinutes(10))
                        .toString()))
        .toList();
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Le fichier est vide ou manquant");
    }
    var contentType = file.getContentType();
    if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
      throw new IllegalArgumentException(
          "Format non supporté : seuls JPEG et PNG sont acceptés (reçu: " + contentType + ")");
    }
  }

  private ImageResponse toResponse(ImageEntity entity, String url) {
    return ImageResponse.builder()
        .id(entity.getId())
        .nomFichier(entity.getNomFichier())
        .email(entity.getEmail())
        .dateCreation(entity.getDateCreation())
        .url(url)
        .build();
  }
}
