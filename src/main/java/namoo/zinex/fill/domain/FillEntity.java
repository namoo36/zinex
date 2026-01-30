package namoo.zinex.fill.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import namoo.zinex.order.domain.OrderEntity;
import namoo.zinex.stock.domain.StockEntity;
import namoo.zinex.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "fills")
public class FillEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", referencedColumnName = "id", insertable = false, updatable = false)
  private OrderEntity order;

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

  @Enumerated(EnumType.STRING)
  @Column(name = "side", nullable = false, length = 8)
  private Side side;

  @Column(name = "quantity", nullable = false)
  private Long quantity;

  @Column(name = "price_krw", nullable = false)
  private Long priceKrw;

  @Column(name = "fee_krw", nullable = false)
  private Long feeKrw = 0L;

  @CreationTimestamp
  @Column(name = "executed_at", nullable = false, updatable = false)
  private Instant executedAt;

  public enum Side {
    BUY,
    SELL
  }
}

