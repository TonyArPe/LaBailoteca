package com.bailoteca.repository.chat;

import com.bailoteca.models.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ChatRepo extends JpaRepository<Chat, Long> {

    // Buscar un chat privado entre dos usuarios
    Optional<Chat> findByUsuario1IdAndUsuario2IdAndEsGrupalFalse(Long u1, Long u2);

    Optional<Chat> findByUsuario2IdAndUsuario1IdAndEsGrupalFalse(Long u2, Long u1);

    // Buscar chat grupal por clase
    Optional<Chat> findByClaseIdAndEsGrupalTrue(Long claseId);

    // Listar todos los chats de un usuario
    List<Chat> findByUsuario1IdOrUsuario2Id(Long id1, Long id2);
}