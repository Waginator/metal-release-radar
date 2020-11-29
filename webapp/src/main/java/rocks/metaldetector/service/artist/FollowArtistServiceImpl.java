package rocks.metaldetector.service.artist;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rocks.metaldetector.discogs.facade.DiscogsService;
import rocks.metaldetector.discogs.facade.dto.DiscogsArtistDto;
import rocks.metaldetector.persistence.domain.artist.ArtistEntity;
import rocks.metaldetector.persistence.domain.artist.ArtistRepository;
import rocks.metaldetector.persistence.domain.artist.ArtistSource;
import rocks.metaldetector.persistence.domain.artist.FollowActionEntity;
import rocks.metaldetector.persistence.domain.artist.FollowActionRepository;
import rocks.metaldetector.persistence.domain.user.UserEntity;
import rocks.metaldetector.persistence.domain.user.UserRepository;
import rocks.metaldetector.security.CurrentUserSupplier;
import rocks.metaldetector.spotify.facade.SpotifyService;
import rocks.metaldetector.spotify.facade.dto.SpotifyArtistDto;
import rocks.metaldetector.support.exceptions.ResourceNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class FollowArtistServiceImpl implements FollowArtistService {

  private final UserRepository userRepository;
  private final ArtistRepository artistRepository;
  private final FollowActionRepository followActionRepository;
  private final SpotifyService spotifyService;
  private final DiscogsService discogsService;
  private final ArtistTransformer artistTransformer;
  private final CurrentUserSupplier currentUserSupplier;
  private final ArtistService artistService;

  @Override
  @Transactional
  public void follow(String externalArtistId, ArtistSource source) {
    ArtistEntity artist = saveAndFetchArtist(externalArtistId, source);
    FollowActionEntity followAction = FollowActionEntity.builder()
        .user(currentUserSupplier.get())
        .artist(artist)
        .build();

    followActionRepository.save(followAction);
  }

  @Override
  @Transactional
  public int followSpotifyArtists(List<String> spotifyArtistIds) {
    saveSpotifyArtists(spotifyArtistIds);
    List<ArtistEntity> artistEntitiesToFollow = artistRepository.findAllByExternalIdIn(spotifyArtistIds);
    UserEntity currentUser = currentUserSupplier.get();
    List<FollowActionEntity> followActionEntities = artistEntitiesToFollow.stream()
        .map(artistEntity -> FollowActionEntity.builder()
            .user(currentUser)
            .artist(artistEntity)
            .build())
        .collect(Collectors.toList());

    return followActionRepository.saveAll(followActionEntities).size();
  }

  @Override
  @Transactional
  public void unfollow(String externalArtistId, ArtistSource source) {
    ArtistEntity artistEntity = fetchArtistEntity(externalArtistId, source);
    followActionRepository.deleteByUserAndArtist(currentUserSupplier.get(), artistEntity);
  }

  @Override
  public boolean isCurrentUserFollowing(String externalArtistId, ArtistSource source) {
    Optional<ArtistEntity> artistOptional = artistRepository.findByExternalIdAndSource(externalArtistId, source);

    if (artistOptional.isEmpty()) {
      return false;
    }

    UserEntity currentUser = currentUserSupplier.get();
    return followActionRepository.existsByUserIdAndArtistId(currentUser.getId(), artistOptional.get().getId());
  }

  @Override
  @Transactional
  public List<ArtistDto> getFollowedArtistsOfCurrentUser() {
    return getFollowedArtists(currentUserSupplier.get());
  }

  @Override
  @Transactional
  public List<ArtistDto> getFollowedArtistsOfUser(String publicUserId) {
    UserEntity user = fetchUserEntity(publicUserId);
    return getFollowedArtists(user);
  }

  private List<ArtistDto> getFollowedArtists(UserEntity user) {
    return followActionRepository.findAllByUser(user).stream()
        .map(artistTransformer::transform)
        .sorted(Comparator.comparing(ArtistDto::getArtistName))
        .collect(Collectors.toUnmodifiableList());
  }

  private ArtistEntity saveAndFetchArtist(String externalId, ArtistSource source) {
    if (artistRepository.existsByExternalIdAndSource(externalId, source)) {
      return fetchArtistEntity(externalId, source);
    }

    ArtistEntity artistEntity = switch (source) {
      case DISCOGS -> {
        DiscogsArtistDto artist = discogsService.searchArtistById(externalId);
        yield new ArtistEntity(artist.getId(), artist.getName(), artist.getImageUrl(), source);
      }
      case SPOTIFY -> {
        SpotifyArtistDto artist = spotifyService.searchArtistById(externalId);
        yield new ArtistEntity(artist.getId(), artist.getName(), artist.getImageUrl(), source);
      }
    };

    return artistRepository.save(artistEntity);
  }

  private void saveSpotifyArtists(List<String> spotifyArtistIds) {
    List<String> newArtistsIds = artistService.findNewArtistIds(spotifyArtistIds);
    List<SpotifyArtistDto> newSpotifyArtistDtos = spotifyService.searchArtistsByIds(newArtistsIds);
    artistService.persistSpotifyArtists(newSpotifyArtistDtos);
  }

  private UserEntity fetchUserEntity(String publicUserId) {
    return userRepository
        .findByPublicId(publicUserId)
        .orElseThrow(() -> new ResourceNotFoundException("User with public id '" + publicUserId + "' not found!"));
  }

  private ArtistEntity fetchArtistEntity(String externalArtistId, ArtistSource source) {
    return artistRepository
        .findByExternalIdAndSource(externalArtistId, source)
        .orElseThrow(() -> new ResourceNotFoundException("Artist with id '" + externalArtistId + "' (" + source + ") not found!"));
  }
}
