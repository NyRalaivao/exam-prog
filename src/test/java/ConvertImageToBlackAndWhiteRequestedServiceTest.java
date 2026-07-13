import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.exam.demo.endpoint.event.model.ConvertImageToBlackAndWhiteRequested;
import com.exam.demo.file.bucket.BucketComponent;
import com.exam.demo.mail.Email;
import com.exam.demo.mail.Mailer;
import com.exam.demo.service.event.ConvertImageToBlackAndWhiteRequestedService;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConvertImageToBlackAndWhiteRequestedServiceTest {

  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;

  @InjectMocks private ConvertImageToBlackAndWhiteRequestedService service;

  @Test
  void accept_shouldDownloadConvertAndSendEmail() throws Exception {
    var sourceFile = File.createTempFile("source-", ".jpg");
    var image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    ImageIO.write(image, "jpg", sourceFile);

    when(bucketComponent.download(eq("abc.jpg"))).thenReturn(sourceFile);

    var event =
        ConvertImageToBlackAndWhiteRequested.builder()
            .bucketKey("abc.jpg")
            .email("test@test.com")
            .build();

    service.accept(event);

    verify(mailer).accept(any(Email.class));

    sourceFile.delete();
  }
}
