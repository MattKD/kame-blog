package blog;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepo extends CrudRepository<TagModel, Integer> {

  @Query("select t from TagModel t where t.name in :names")
  List<TagModel> findByNames(@Param("names") List<String> names);
}

