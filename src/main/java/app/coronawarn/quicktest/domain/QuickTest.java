package app.coronawarn.quicktest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quicktest")
public class QuickTest {

  static final long SERIAL_VERSION_UID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "guid")
  private String guid;

  @Column(name = "personal_data_hash")
  private String personalDataHash;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "created_at")
  private LocalDateTime updatedAt;

  @Version
  @Column(name = "version")
  private long version;


}
