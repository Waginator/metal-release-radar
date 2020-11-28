package rocks.metaldetector.service.spotify;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rocks.metaldetector.service.artist.ArtistService;
import rocks.metaldetector.service.artist.FollowArtistService;
import rocks.metaldetector.spotify.facade.SpotifyService;
import rocks.metaldetector.testutil.DtoFactory.ArtistDtoFactory;
import rocks.metaldetector.testutil.DtoFactory.SpotifyAlbumDtoFactory;
import rocks.metaldetector.testutil.DtoFactory.SpotifyArtistDtoFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static rocks.metaldetector.persistence.domain.artist.ArtistSource.SPOTIFY;
import static rocks.metaldetector.service.spotify.SpotifyFetchType.ALBUMS;

@ExtendWith(MockitoExtension.class)
class SpotifyFollowedArtistsServiceImplTest implements WithAssertions {

  @Mock
  private SpotifyService spotifyService;

  @Mock
  private FollowArtistService followArtistService;

  @Mock
  private ArtistService artistService;

  @Mock
  private SpotifyUserAuthorizationService userAuthorizationService;

  @InjectMocks
  private SpotifyFollowedArtistsServiceImpl underTest;

  @AfterEach
  void tearDown() {
    reset(spotifyService, followArtistService, artistService, userAuthorizationService);
  }

  @Nested
  @DisplayName("Tests for artist import")
  class ArtistImportTest {

    @Test
    @DisplayName("userAuthorizationService is called")
    void test_user_authorization_service_called() {
      // when
      underTest.importArtistsFromLikedReleases();

      // then
      verify(userAuthorizationService).getOrRefreshToken();
    }

    @Test
    @DisplayName("spotifyService is called with access token")
    void test_spotify_service_called() {
      // given
      var accessToken = "accessToken";
      doReturn(accessToken).when(userAuthorizationService).getOrRefreshToken();

      // when
      underTest.importArtistsFromLikedReleases();

      // then
      verify(spotifyService).fetchLikedAlbums(accessToken);
    }

    @Test
    @DisplayName("artistService is called to get new artist ids")
    void test_artist_service_called() {
      // given
      var albumDto = SpotifyAlbumDtoFactory.createDefault();
      doReturn(List.of(albumDto)).when(spotifyService).fetchLikedAlbums(any());

      // when
      underTest.importArtistsFromLikedReleases();

      // then
      verify(artistService).findNewArtistIds(List.of(albumDto.getArtists().get(0).getId()));
    }

    @Test
    @DisplayName("artistService is called with all artists of an album")
    void test_artist_service_called_with_all_artists() {
      // given
      var album = SpotifyAlbumDtoFactory.withTwoArtist();
      var id1 = "id1";
      var id2 = "id2";
      album.getArtists().get(0).setId(id1);
      album.getArtists().get(1).setId(id2);
      doReturn(List.of(album)).when(spotifyService).fetchLikedAlbums(any());

      // when
      underTest.importArtistsFromLikedReleases();

      // then
      verify(artistService).findNewArtistIds(List.of(id1, id2));
    }

    @Test
    @DisplayName("duplicates are eliminated when calling artistService")
    void test_no_duplicates_when_calling_artist_service() {
      // given
      var firstAlbum = SpotifyAlbumDtoFactory.createDefault();
      var secondAlbum = SpotifyAlbumDtoFactory.createDefault();
      var id = "id";
      firstAlbum.getArtists().get(0).setId(id);
      secondAlbum.getArtists().get(0).setId(id);
      var albumDtos = List.of(firstAlbum, secondAlbum);
      doReturn(albumDtos).when(spotifyService).fetchLikedAlbums(any());

      // when
      underTest.importArtistsFromLikedReleases();

      // then
      verify(artistService).findNewArtistIds(List.of(id));
    }

    @Test
    @DisplayName("spotifyService is called to fetch artists")
    void test_spotify_service_called_with_new_artists_list() {
      // given
      var artists = List.of("id1", "id2");
      doReturn(artists).when(artistService).findNewArtistIds(any());

      // when
      underTest.importArtistsFromLikedReleases();

      // then
      verify(spotifyService).searchArtistsByIds(artists);
    }

    @Test
    @DisplayName("artistService is called to persists all fetched artists")
    void test_artist_service_called_to_persist_new_artists() {
      // given
      var spotifyDtos = List.of(SpotifyArtistDtoFactory.withArtistName("a"));
      doReturn(spotifyDtos).when(spotifyService).searchArtistsByIds(any());

      // when
      underTest.importArtistsFromLikedReleases();

      // then
      verify(artistService).persistArtists(spotifyDtos);
    }

    @Test
    @DisplayName("artistService is called to get all imported artists after persisting")
    void test_artist_service_gets_all_artists() {
      // given
      var albumDto = SpotifyAlbumDtoFactory.createDefault();
      doReturn(List.of(albumDto)).when(spotifyService).fetchLikedAlbums(any());

      // when
      underTest.importArtistsFromLikedReleases();

      // then
      verify(artistService).findAllArtistsByExternalIds(List.of(albumDto.getArtists().get(0).getId()));
    }

    @Test
    @DisplayName("followArtistService is called to check for already followed artistDtos")
    void test_follow_artist_service_called_to_check_followed() {
      // given
      var artistDtos = List.of(ArtistDtoFactory.withName("A"), ArtistDtoFactory.withName("B"));
      doReturn(artistDtos).when(artistService).findAllArtistsByExternalIds(any());

      // when
      underTest.importArtistsFromLikedReleases();

      // then
      verify(followArtistService).isCurrentUserFollowing(artistDtos.get(0).getExternalId(), SPOTIFY);
      verify(followArtistService).isCurrentUserFollowing(artistDtos.get(1).getExternalId(), SPOTIFY);
    }

    @Test
    @DisplayName("artistDtos are returned")
    void test_artist_dtos_are_returned() {
      // given
      var artistDtos = List.of(ArtistDtoFactory.withName("A"), ArtistDtoFactory.withName("B"));
      doReturn(artistDtos).when(artistService).findAllArtistsByExternalIds(any());

      // when
      var result = underTest.importArtistsFromLikedReleases();

      // then
      assertThat(result).isEqualTo(artistDtos);
    }
  }

  @Nested
  @DisplayName("Tests for getting followed artists")
  class GettingFollowedArtistsTest {

    @Test
    @DisplayName("userAuthorizationService is called")
    void test_user_authorization_service_called() {
      // when
      underTest.getNewFollowedArtists(List.of(ALBUMS));

      // then
      verify(userAuthorizationService).getOrRefreshToken();
    }

    @Test
    @DisplayName("spotifyService is called with access token for import")
    void test_spotify_service_called() {
      // given
      var accessToken = "accessToken";
      doReturn(accessToken).when(userAuthorizationService).getOrRefreshToken();

      // when
      underTest.getNewFollowedArtists(List.of(ALBUMS));

      // then
      verify(spotifyService).fetchLikedAlbums(accessToken);
    }

    @Test
    @DisplayName("duplicates are eliminated from liked albums before the spotify service is called")
    void test_no_duplicates_when_calling_artist_service() {
      // given
      var firstAlbum = SpotifyAlbumDtoFactory.createDefault();
      var secondAlbum = SpotifyAlbumDtoFactory.createDefault();
      var id = "id";

      firstAlbum.getArtists().get(0).setId(id);
      secondAlbum.getArtists().get(0).setId(id);
      var albumDtos = List.of(firstAlbum, secondAlbum);
      doReturn(albumDtos).when(spotifyService).fetchLikedAlbums(any());

      // when
      underTest.getNewFollowedArtists(List.of(ALBUMS));

      // then
      verify(spotifyService).searchArtistsByIds(List.of(firstAlbum.getArtists().get(0).getId()));
    }

    @Test
    @DisplayName("followArtistService is called for each artist")
    void test_follow_artist_service_called_to_check_followed() {
      // given
      var spotifyArtist1 = SpotifyArtistDtoFactory.withArtistName("Slayer");
      var spotifyArtist2 = SpotifyArtistDtoFactory.withArtistName("Metallica");
      doReturn(List.of(SpotifyAlbumDtoFactory.createDefault())).when(spotifyService).fetchLikedAlbums(any());
      doReturn(List.of(spotifyArtist1, spotifyArtist2)).when(spotifyService).searchArtistsByIds(anyList());

      // when
      underTest.getNewFollowedArtists(List.of(ALBUMS));

      // then
      verify(followArtistService).isCurrentUserFollowing(spotifyArtist1.getId(), SPOTIFY);
      verify(followArtistService).isCurrentUserFollowing(spotifyArtist2.getId(), SPOTIFY);
    }

    @Test
    @DisplayName("Spotify artists are returned in alphabetically order")
    void test_spotify_artist_are_returned() {
      // given
      var spotifyArtist1 = SpotifyArtistDtoFactory.withArtistName("B");
      var spotifyArtist2 = SpotifyArtistDtoFactory.withArtistName("C");
      var spotifyArtist3 = SpotifyArtistDtoFactory.withArtistName("A");
      doReturn(List.of(SpotifyAlbumDtoFactory.createDefault())).when(spotifyService).fetchLikedAlbums(any());
      doReturn(List.of(spotifyArtist1, spotifyArtist2, spotifyArtist3)).when(spotifyService).searchArtistsByIds(anyList());

      // when
      var result = underTest.getNewFollowedArtists(List.of(ALBUMS));

      // then
      assertThat(result).isEqualTo(List.of(spotifyArtist3, spotifyArtist1, spotifyArtist2));
    }
  }
}
