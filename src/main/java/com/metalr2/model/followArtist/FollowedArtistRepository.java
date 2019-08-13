package com.metalr2.model.followArtist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowedArtistRepository extends JpaRepository<FollowedArtistEntity, Long> {

  List<FollowedArtistEntity> findFollowedArtistEntitiesByUserId(long userId);

  boolean existsFollowedArtistEntityByUserId(long userId);

  boolean existsFollowedArtistEntityByArtistDiscogsId(long artistDiscogsId);

  boolean existsFollowedArtistEntityByUserIdAndArtistDiscogsId(long userId, long artistDiscogsId);

}
