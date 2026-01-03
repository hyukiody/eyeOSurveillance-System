package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.models.ImageRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageRecord, Long> {

    // O Spring implementa estes métodos automaticamente baseado no nome.
    
    // Encontrar registros pelo nome do arquivo
    List<ImageRecord> findByFileName(String fileName);

    // Encontrar o registro mais recente (útil para mostrar o último processamento)
    Optional<ImageRecord> findTopByOrderByProcessedAtDesc();
}