package org.example.journal_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.journal_app.entities.Post;
import org.example.journal_app.entities.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private String title;
    private String content;
    private List<String> imagePath;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Post toEntity(User user) {
        return Post.builder()
                .title(this.title)
                .content(this.content)
                .user(user)
                .imagePath(this.imagePath != null ? new ArrayList<>(this.imagePath) : new ArrayList<>())
                .build();
    }

}
