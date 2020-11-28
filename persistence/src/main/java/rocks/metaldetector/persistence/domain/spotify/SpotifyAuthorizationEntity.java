package rocks.metaldetector.persistence.domain.spotify;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import rocks.metaldetector.persistence.domain.BaseEntity;
import rocks.metaldetector.persistence.domain.user.UserEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for hibernate and model mapper
@AllArgsConstructor(access = AccessLevel.PRIVATE) // for lombok builder
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity(name = "spotifyAuthorizations")
public class SpotifyAuthorizationEntity extends BaseEntity {

  @Column(name = "state", nullable = false, updatable = false)
  private String state;

  @Column(name = "access_token")
  private String accessToken;

  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "scope")
  private String scope;

  @Column(name = "token_type")
  private String tokenType;

  @Column(name = "expires_in")
  private Integer expiresIn;

  @OneToOne(targetEntity = UserEntity.class)
  @JoinColumn(nullable = false, name = "users_id")
  @NonNull
  private UserEntity user;

  public SpotifyAuthorizationEntity(UserEntity user, String state) {
    this.state = state;
    this.user = user;
  }
}
