package org.example.journal_app.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.journal_app.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    private String fullName;
    private String email;
    @JsonIgnore
    private String password;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    @JsonBackReference
    private List<Post> posts;

    public UserDto toDto() {
        return new UserDto(
                this.username,
                this.fullName,
                this.email,
                this.createdAt
        );
    }
}
