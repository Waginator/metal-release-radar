package com.metalr2.model.artist;

import com.metalr2.testutil.WithIntegrationTestProfile;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@DataJpaTest
class FollowedArtistsRepositoryIT implements WithAssertions, WithIntegrationTestProfile {

  private static final String USER_ID = "1";
  private static final String FALSE_USER_ID = "0";
  private static final long DISCOGS_ID = 1L;
  private static final long FALSE_DISCOGS_ID = 0L;

  @Autowired
  private FollowedArtistsRepository followedArtistsRepository;

  @BeforeEach
  void setup() {
    followedArtistsRepository.saveAll(IntStream.range(1, 7).mapToObj(entity -> new FollowedArtistEntity(USER_ID, entity)).collect(Collectors.toList()));
  }

  @AfterEach
  void tearDown() {
    followedArtistsRepository.deleteAll();
  }

  @Test
  @DisplayName("findAllByPublicUserId() finds the correct entities for a given user id if it exists")
  void find_all_by_user_id_should_return_correct_entities() {
    List<FollowedArtistEntity> entities = followedArtistsRepository.findByPublicUserId(USER_ID);

    assertThat(entities).hasSize(6);

    for (int i = 0; i < entities.size(); i++) {
      FollowedArtistEntity entity = entities.get(i);
      assertThat(entity.getPublicUserId()).isEqualTo(USER_ID);
      assertThat(entity.getDiscogsId()).isEqualTo(i+1);
    }
  }

  @Test
  @DisplayName("findAllByPublicUserId() returns empty list for a given user id if it does not exist")
  void find_all_by_user_id_should_return_empty_list() {
    List<FollowedArtistEntity> notFollowedArtistEntitiesPerUser = followedArtistsRepository.findByPublicUserId("0");

    assertThat(notFollowedArtistEntitiesPerUser).isEmpty();
  }

  @Test
  @DisplayName("Should return true for existing combination of user id and artist discogs id")
  void exists_by_user_id_and_artist_discogs_id() {
    boolean result = followedArtistsRepository.existsByPublicUserIdAndDiscogsId(USER_ID, DISCOGS_ID);

    assertThat(result).isTrue();
  }


  @ParameterizedTest(name = "[{index}] => UserId <{0}> | ArtistDiscogsId <{1}>")
  @MethodSource("inputProviderExistsByFalse")
  @DisplayName("Should return false for not existing combinations of user id and artist discogs id")
  void exists_by_user_id_and_artist_discogs_id(String userId, long artistDiscogsId) {
    boolean result = followedArtistsRepository.existsByPublicUserIdAndDiscogsId(userId, artistDiscogsId);

    assertThat(result).isFalse();
  }

  private static Stream<Arguments> inputProviderExistsByFalse() {
    return Stream.of(
            Arguments.of(FALSE_USER_ID, DISCOGS_ID),
            Arguments.of(USER_ID, FALSE_DISCOGS_ID)
    );
  }

  @Test
  @DisplayName("Should return optional containing the correct entity for existing combinations of user id and artist discogs id")
  void find_by_user_id_and_artist_discogs_id_should_return_valid_optional() {
    Optional<FollowedArtistEntity> optionalFollowedArtistEntity = followedArtistsRepository.findByPublicUserIdAndDiscogsId(USER_ID, DISCOGS_ID);

    assertThat(optionalFollowedArtistEntity.isPresent()).isTrue();
    assertThat(optionalFollowedArtistEntity.get().getDiscogsId()).isEqualTo(DISCOGS_ID);
    assertThat(optionalFollowedArtistEntity.get().getPublicUserId()).isEqualTo(USER_ID);
  }

  @ParameterizedTest(name = "[{index}] => UserId <{0}> | ArtistDiscogsId <{1}>")
  @MethodSource("inputProviderFalseArguments")
  @DisplayName("Should return an empty optional for not existing or faulty combinations of user id and artist discogs id")
  void find_by_user_id_and_artist_discogs_id_should_return_empty_optional(String userId, long artistDiscogsId) {
    Optional<FollowedArtistEntity> optionalFollowedArtistEntity = followedArtistsRepository.findByPublicUserIdAndDiscogsId(userId, artistDiscogsId);

    assertThat(optionalFollowedArtistEntity).isEmpty();
  }

  private static Stream<Arguments> inputProviderFalseArguments() {
    return Stream.of(
            Arguments.of(FALSE_USER_ID, DISCOGS_ID),
            Arguments.of(null, DISCOGS_ID)
    );
  }

  @Test
  @DisplayName("findAllByPublicUserId(id, pageable) should return correct paginated items")
  void find_all_by_discogs_id_paginated() {
    List<FollowedArtistEntity> entities = followedArtistsRepository.findByPublicUserId(USER_ID, PageRequest.of(1, 2));

    assertThat(entities).hasSize(2);

    assertThat(entities.get(0).getDiscogsId()).isEqualTo(3L);
    assertThat(entities.get(0).getPublicUserId()).isEqualTo(USER_ID);

    assertThat(entities.get(1).getDiscogsId()).isEqualTo(4L);
    assertThat(entities.get(1).getPublicUserId()).isEqualTo(USER_ID);
  }

  @ParameterizedTest(name = "[{index}] => UserID <{0}> | Expected Number <{1}>")
  @MethodSource("userIdProvider")
  @DisplayName("countByPublicUserId() should return correct number of items")
  void count_by_public_user_id(String userId, int expected) {
    long numberOfEntities = followedArtistsRepository.countByPublicUserId(userId);

    assertThat(numberOfEntities).isEqualTo(expected);
  }

  private static Stream<Arguments> userIdProvider() {
    return Stream.of(
        Arguments.of(FALSE_USER_ID, 0),
        Arguments.of(USER_ID, 6)
    );
  }
}
