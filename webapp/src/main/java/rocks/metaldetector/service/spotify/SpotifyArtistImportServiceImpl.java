package rocks.metaldetector.service.spotify;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rocks.metaldetector.persistence.domain.user.UserEntity;
import rocks.metaldetector.persistence.domain.user.UserRepository;
import rocks.metaldetector.security.CurrentPublicUserIdSupplier;
import rocks.metaldetector.service.artist.ArtistDto;
import rocks.metaldetector.service.artist.ArtistService;
import rocks.metaldetector.service.artist.FollowArtistService;
import rocks.metaldetector.spotify.facade.SpotifyService;
import rocks.metaldetector.spotify.facade.dto.SpotifyAlbumDto;
import rocks.metaldetector.spotify.facade.dto.SpotifyArtistDto;
import rocks.metaldetector.support.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static rocks.metaldetector.persistence.domain.artist.ArtistSource.SPOTIFY;

@Service
@AllArgsConstructor
public class SpotifyArtistImportServiceImpl implements SpotifyArtistImportService {

  private final SpotifyService spotifyService;
  private final CurrentPublicUserIdSupplier currentPublicUserIdSupplier;
  private final UserRepository userRepository;
  private final FollowArtistService followArtistService;
  private final ArtistService artistService;

  @Override
  public List<ArtistDto> importArtistsFromLikedReleases() {
    String publicUserId = currentPublicUserIdSupplier.get();
    UserEntity currentUser = userRepository.findByPublicId(publicUserId).orElseThrow(
        () -> new ResourceNotFoundException("User with public id '" + publicUserId + "' not found!")
    );

    List<SpotifyAlbumDto> importedAlbums = spotifyService.fetchLikedAlbums(currentUser.getSpotifyAuthorization().getAccessToken());
    List<String> artistIds = importedAlbums.stream()
        .flatMap(album -> album.getArtists().stream())
        .map(SpotifyArtistDto::getId)
        .distinct()
        .collect(Collectors.toList());

    List<String> newArtistsIds = artistService.findNewArtistIds(artistIds);
    persistNewArtists(newArtistsIds);

    return artistService.findAllArtistsByExternalIds(artistIds).stream()
        .filter(artist -> !followArtistService.isCurrentUserFollowing(artist.getExternalId(), SPOTIFY))
        .peek(artist -> followArtistService.follow(artist.getExternalId(), SPOTIFY))
        .collect(Collectors.toList());
  }

  private void persistNewArtists(List<String> newArtistsIds) {
    List<SpotifyArtistDto> spotifyArtistDtos = spotifyService.searchArtistsByIds(newArtistsIds);
    artistService.persistArtists(spotifyArtistDtos);
  }
}
