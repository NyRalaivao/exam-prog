package com.exam.demo.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ImageResponse {
  private UUID id;
  private String nomFichier;
  private String email;
  private LocalDateTime dateCreation;
  private String url;
}
