package rikser123.bundle.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rikser123.bundle.feign.SecurityClient;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicKeyLoaderService {
  private final SecurityClient securityClient;

  private PublicKey publicKey;

  @PostConstruct
  void loadPublicKey() {
    try {
      var response = securityClient.getPublicKey();

      if (response == null || response.getData() == null) {
        throw new RuntimeException("Failed to get public key: empty response");
      }

      var publicKeyPem = response.getData().getPublicKey();
      this.publicKey = parsePublicKey(publicKeyPem);
      log.info("Public key successfully loaded and parsed");

    } catch (Exception e) {
      log.error("Failed to load public key at startup", e);
      throw new RuntimeException("Could not initialize JWT decoder", e);
    }
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  private PublicKey parsePublicKey(String pem) throws Exception {
    var base64 = pem
      .replace("-----BEGIN PUBLIC KEY-----", "")
      .replace("-----END PUBLIC KEY-----", "")
      .replaceAll("\\s", "");

    var decoded = Base64.getDecoder().decode(base64);

    // Создаём PublicKey
    var keySpec = new X509EncodedKeySpec(decoded);
    var keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(keySpec);
  }
}
