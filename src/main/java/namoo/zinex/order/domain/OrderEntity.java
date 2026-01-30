package namoo.zinex.order.domain;

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
import namoo.zinex.stock.domain.StockEntity;
import namoo.zinex.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class OrderEntity {

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

  @Enumerated(EnumType.STRING)
  @Column(name = "side", nullable = false, length = 8)
  private Side side;

  @Enumerated(EnumType.STRING)
  @Column(name = "order_type", nullable = false, length = 8)
  private OrderType orderType = OrderType.LIMIT;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private Status status = Status.NEW;

  @Column(name = "quantity", nullable = false)
  private Long quantity;

  @Column(name = "limit_price_krw")
  private Long limitPriceKrw;

  @Column(name = "filled_quantity", nullable = false)
  private Long filledQuantity = 0L;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "cancelled_at")
  private Instant cancelledAt;

  public enum Side {
    BUY,
    SELL
  }

  public enum OrderType {
    LIMIT,
    MARKET
  }

  public enum Status {
    NEW,
    OPEN,
    CANCELLED,
    FILLED,
    FAILED,
    EXPIRED
  }
}

