package com.exam.demo.service.event;

import com.exam.demo.endpoint.event.model.ConvertImageToBlackAndWhiteRequested;
import com.exam.demo.file.bucket.BucketComponent;
import com.exam.demo.mail.Email;
import com.exam.demo.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ConvertImageToBlackAndWhiteRequestedService
    implements Consumer<ConvertImageToBlackAndWhiteRequested> {

  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(ConvertImageToBlackAndWhiteRequested event) {
    File originalFile = bucketComponent.download(event.getBucketKey());

    var extension = event.getBucketKey().endsWith(".png") ? "png" : "jpg";
    File bwFile = File.createTempFile("bw-", "." + extension);
    convertToBlackAndWhite(originalFile, bwFile, extension);

    var recipient = new InternetAddress(event.getEmail());
    var email =
        new Email(
            recipient,
            List.of(),
            List.of(),
            "Votre image en noir et blanc",
            "Bonjour,\n\nVoici la version noir et blanc de l'image que vous avez envoyée.",
            List.of(bwFile));

    mailer.accept(email);

    bwFile.delete();
  }

  private void convertToBlackAndWhite(File input, File output, String extension) throws Exception {
    BufferedImage originalImage = ImageIO.read(input);
    BufferedImage grayImage =
        new BufferedImage(
            originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

    Graphics graphics = grayImage.getGraphics();
    graphics.drawImage(originalImage, 0, 0, null);
    graphics.dispose();

    ImageIO.write(grayImage, extension, output);
  }
}
