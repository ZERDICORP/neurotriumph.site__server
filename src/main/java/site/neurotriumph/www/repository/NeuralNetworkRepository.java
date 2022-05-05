package site.neurotriumph.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.neurotriumph.www.entity.NeuralNetwork;

import java.util.Optional;

@Repository
public interface NeuralNetworkRepository extends JpaRepository<NeuralNetwork, Long> {
  @Query("SELECT nn FROM neural_network nn WHERE nn.name = :name AND nn.owner_id = :owner_id")
  Optional<NeuralNetwork> findByNameAndOwnerId(String name, Long owner_id);

  @Query("SELECT nn FROM neural_network nn WHERE nn.api_root = :api_root AND nn.owner_id = :owner_id")
  Optional<NeuralNetwork> findByApiRootAndOwnerId(String api_root, Long owner_id);

  @Query("SELECT nn FROM neural_network nn WHERE nn.id = :id AND nn.owner_id = :owner_id")
  Optional<NeuralNetwork> findByIdAndOwnerId(Long id, Long owner_id);

  @Modifying
  @Query("DELETE FROM neural_network nn WHERE nn.owner_id = :owner_id")
  void deleteAllByOwnerId(Long owner_id);
}
