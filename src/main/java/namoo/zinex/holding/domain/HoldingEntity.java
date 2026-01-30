package namoo.zinex.holding.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import namoo.zinex.stock.domain.StockEntity;
import namoo.zinex.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "holdings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_holdings_user_stock", columnNames = {"user_id", "stock_id"})
    }
)
public class HoldingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
  private User user;

  @Column(name = "stock_id", nullable = false)
  private Long stockId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stock_id", referencedColumnName = "id", insertable = false, updatable = false)
  private StockEntity stock;

  @Column(name = "quantity", nullable = false)
  private Long quantity = 0L;

  @Column(name = "avg_price_krw", nullable = false)
  private Long avgPriceKrw = 0L;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}

