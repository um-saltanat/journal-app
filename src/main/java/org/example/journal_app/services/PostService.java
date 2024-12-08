package org.example.journal_app.services;

import org.example.journal_app.dto.auth.CustomUserDetails;
import org.example.journal_app.dto.UserDto;
import org.example.journal_app.entities.Post;
import org.example.journal_app.entities.User;
import org.example.journal_app.exceptions.NotFoundException;
import org.example.journal_app.repositories.PostRepository;
import org.example.journal_app.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Post createPost(Post newPost) {
        if (newPost.getUser() == null || newPost.getUser().getUsername() == null) {
            throw new IllegalArgumentException("User ID or username is missing");
        }

        Optional<User> user = userRepository.findByUsername(newPost.getUser().getUsername());
        if (user.isPresent()) {
            newPost.setCreatedAt(LocalDateTime.now());
            newPost.setUser(user.get()); // Associate the User
            return postRepository.save(newPost); // Save with all fields, including imagePath
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    public Post updatePost(Long postId, Post post) {
        Post newPost = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (post.getTitle() != null) {
            newPost.setTitle(post.getTitle());
        }
        if (post.getContent() != null) {
            newPost.setContent(post.getContent());
        }
        if (post.getCreatedAt() != null) {
            newPost.setCreatedAt(post.getCreatedAt());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        if (customUserDetails != null && customUserDetails.user() != null) {
            String username = customUserDetails.getUsername();
            Optional<User> user = userRepository.findByUsername(username);

            UserDto userDto = user.map(User::toDto).orElseThrow(() -> new NotFoundException("User not found with username: " + username));
            newPost.setUser(user.orElseThrow(() -> new NotFoundException("User not found with username: " + username)));  // You can still set the User entity for DB
        }
        if (post.getImagePath() != null) {
            newPost.setImagePath(post.getImagePath());
        }

        newPost.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(newPost);
    }

    public void deletePost(Long postId) {
        if (postRepository.existsById(postId)) {
            postRepository.deleteById(postId);
        } else {
            throw new IllegalArgumentException("Post not found");
        }
    }

    public Optional<Post> getPostById(Long postId) {
        return postRepository.findById(postId);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public void removeImageFromPost(Long postId, String imageName) {
        // Retrieve the post by ID
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with ID: " + postId));

        // Check if the image name exists in the list of images
        if (!post.getImagePath().contains(imageName)) {
            throw new NotFoundException("Image not associated with the post: " + imageName);
        }
        post.getImagePath().remove(imageName);
        postRepository.save(post);
        String imagePath = "C:/Users/user/Desktop/Files/" + imageName;  // Full file path
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            imageFile.delete();  // Delete the image file
        }
    }
}