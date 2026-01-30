package namoo.zinex.tradelog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import namoo.zinex.fill.domain.FillEntity;
import namoo.zinex.order.domain.OrderEntity;
import namoo.zinex.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "trade_logs")
public class TradeLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
  private User user;

  @Column(name = "order_id")
  private Long orderId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", referencedColumnName = "id", insertable = false, updatable = false)
  private OrderEntity order;

  @Column(name = "fill_id")
  private Long fillId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fill_id", referencedColumnName = "id", insertable = false, updatable = false)
  private FillEntity fill;

  @Column(name = "event_type", nullable = false, length = 64)
  private String eventType;

  @Column(name = "payload_json", columnDefinition = "json")
  private String payloadJson;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

