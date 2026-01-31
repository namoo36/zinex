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
import namoo.zinex.order.domain.Orders;
import namoo.zinex.stock.domain.Stocks;
import namoo.zinex.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "fills")
public class Fills {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Orders order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stock_id")
  private Stocks stock;

  @Enumerated(EnumType.STRING)
  @Column(name = "side", nullable = false)
  private Side side;

  @Column(name = "quantity", nullable = false)
  private Long quantity;

  @Column(name = "price_krw", nullable = false)
  private Long priceKrw;

  @Column(name = "fee_krw", nullable = false)
  private Long feeKrw;

  @CreationTimestamp
  @Column(name = "executed_at", nullable = false, updatable = false)
  private Instant executedAt;

  public enum Side {
    BUY,
    SELL
  }
}

