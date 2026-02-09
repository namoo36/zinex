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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import namoo.zinex.core.entity.BaseEntity;
import namoo.zinex.fill.domain.Fills;
import namoo.zinex.order.domain.Orders;
import namoo.zinex.user.domain.Users;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "trade_logs")
public class TradeLogs extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private Users user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Orders order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fill_id")
  private Fills fill;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "payload_json", columnDefinition = "json")
  private String payloadJson;

}

