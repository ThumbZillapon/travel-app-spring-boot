package com.techup.spring_project.service;

import com.techup.spring_project.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class SupabaseStorageService {

  @Value("${supabase.url}")
  private String supabaseUrl;

  @Value("${supabase.bucket}")
  private String bucket;

  @Value("${supabase.apiKey}")
  private String apiKey;

  private final WebClient webClient = WebClient.builder().build();

  /** อัปโหลดไฟล์ขึ้น Supabase แล้วคืน public URL */
  public String uploadFile(MultipartFile file) {
    // Validate file exists
    if (file == null || file.isEmpty()) {
      throw new InvalidFileException("Please select a file to upload");
    }

    // Validate file type (only images allowed)
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new InvalidFileException("Only image files are supported");
    }

    String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file.bin";
    String fileName = System.currentTimeMillis() + "_" + original;
    String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, fileName);

    byte[] bytes;
    try {
      bytes = file.getBytes();
    } catch (IOException e) {
      throw new InvalidFileException("Cannot read file bytes");
    }

    try {
      webClient.put()
          .uri(uploadUrl)
          .header("Authorization", "Bearer " + apiKey)     // Service Role Key
          .header("Content-Type", contentType)
          .bodyValue(bytes)
          .retrieve()
          .onStatus(HttpStatusCode::isError, res ->
              res.bodyToMono(String.class).defaultIfEmpty("Upload failed").flatMap(msg ->
                  Mono.error(new InvalidFileException("Supabase upload failed: " + msg))
              )
          )
          .toBodilessEntity()
          .block();

      // public URL สำหรับ access ไฟล์ได้ทันที
      return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, fileName);

    } catch (InvalidFileException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new InvalidFileException("Unexpected error while uploading to Supabase");
    }
  }
}

