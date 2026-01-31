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
import namoo.zinex.core.entity.BaseEntity;
import namoo.zinex.stock.domain.Stocks;
import namoo.zinex.user.domain.User;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Orders extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stock_id")
  private Stocks stock;

  @Enumerated(EnumType.STRING)
  @Column(name = "side", nullable = false)
  private Side side;

  @Enumerated(EnumType.STRING)
  @Column(name = "order_type", nullable = false)
  private OrderType orderType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

  @Column(name = "quantity", nullable = false)
  private Long quantity;

  @Column(name = "limit_price_krw")
  private Long limitPriceKrw;

  @Column(name = "filled_quantity", nullable = false)
  private Long filledQuantity;

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

