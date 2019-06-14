package blog;

import java.util.List;
import java.util.Set;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepo extends CrudRepository<UserModel, Integer> {

  Optional<UserModel> findByName(String name);

}

