package esthesis.edge.crypto.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * SSL socket factory construction details encapsulation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SSLSocketFactoryDTO {

  // The list of trusted certificates (usually, a CA or a remote peer).
  @Singular
  private List<SSLSocketFactoryCertificateDTO> trustedCertificates;

  // The client certificate to present during client authenticate.
  @NotNull
  private SSLSocketFactoryCertificateDTO clientCertificate;

  // The client private key to sign during client authenticate.
  @NotNull
  private SSLSocketFactoryPrivateKeyDTO clientPrivateKey;

  // TLS version to use.
  @Builder.Default
  private String tlsVersion = "TLSv1.2";
}
