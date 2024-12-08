package org.example.journal_app.repositories;

import org.example.journal_app.entities.Post;
import org.example.journal_app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, CrudRepository<Post, Long> {
}

