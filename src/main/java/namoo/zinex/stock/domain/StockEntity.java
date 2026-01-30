package namoo.zinex.stock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "stocks",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_stocks_symbol", columnNames = {"symbol"})
    }
)
public class StockEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "symbol", nullable = false, length = 32)
  private String symbol;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "market", nullable = false, length = 32)
  private String market;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private Status status = Status.ACTIVE;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public enum Status {
    ACTIVE,
    INACTIVE
  }
}

