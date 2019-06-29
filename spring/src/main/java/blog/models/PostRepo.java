package blog;

import java.util.List;
import java.util.Set;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

public interface PostRepo 
    extends PagingAndSortingRepository<PostModel, Integer> {

  @Modifying
  @Query("delete from PostModel p where p.user.id = :id")
  void deleteUserPosts(@Param("id") int id);

  @Query("select p from PostModel p join fetch p.user where p.id = :id")
  Optional<PostModel> findByIdWithUser(@Param("id") int id);

  @Query("select p from PostModel p join fetch p.user")
  List<PostModel> findPosts(Pageable page);

  @Query("select p from PostModel p join fetch p.user u where u.name = :name")
  List<PostModel> findByUserName(@Param("name") String name, Pageable page);

  @Query("select distinct p from PostModel p join fetch p.user u join fetch " +
         "p.tags t where p.id in (select p.id from PostModel p join p.tags t " +
         " where t.name in :names)")
  List<PostModel> findByTags(@Param("names") List<String> names, Pageable page);
}

