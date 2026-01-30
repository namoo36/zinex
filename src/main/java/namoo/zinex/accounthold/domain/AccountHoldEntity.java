package namoo.zinex.accounthold.domain;

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
import namoo.zinex.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "accounts_hold")
public class AccountHoldEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
  private User user;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", referencedColumnName = "id", insertable = false, updatable = false)
  private OrderEntity order;

  @Column(name = "hold_krw", nullable = false)
  private Long holdKrw;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private Status status = Status.ACTIVE;

  @CreationTimestamp
  @Column(name = "reserved_at", nullable = false, updatable = false)
  private Instant reservedAt;

  @Column(name = "released_at")
  private Instant releasedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "release_reason", length = 16)
  private ReleaseReason releaseReason;

  public enum Status {
    ACTIVE,
    RELEASED
  }

  public enum ReleaseReason {
    CANCELLED,
    FILLED,
    FAILED,
    EXPIRED,
    ADJUSTED
  }
}

