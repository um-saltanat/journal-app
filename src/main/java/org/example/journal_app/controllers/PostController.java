package org.example.journal_app.controllers;


import org.example.journal_app.dto.PostDto;
import org.example.journal_app.entities.Post;
import org.example.journal_app.entities.User;
import org.example.journal_app.exceptions.NotFoundException;
import org.example.journal_app.services.PostService;
import org.example.journal_app.services.StorageService;
import org.example.journal_app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> createPost(@ModelAttribute PostDto postDto,
                                           @RequestParam("image") MultipartFile file) throws IOException {
        String imagePath = storageService.uploadImageToFileSystem(file);
        System.out.println("Uploaded Image Path: " + imagePath); // Debugging
        postDto.setImagePath(Collections.singletonList(imagePath));
        System.out.println("PostDto Image Path: " + postDto.getImagePath()); // Debugging

        User user = userService.getUserByUsername(postDto.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found with username: " + postDto.getUsername()));

        Post post = postDto.toEntity(user);
        Post savedPost = postService.createPost(post);
        System.out.println("Saved Post: " + savedPost);

        return ResponseEntity
                .created(URI.create("/posts/" + savedPost.getId()))
                .body(savedPost);
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<Post>> getPostsById() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/get/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
        return postService.getPostById(postId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{postId}")
    public ResponseEntity<Post> updatePost(@Validated @RequestBody PostDto postDto,
                                           @PathVariable Long postId) {
        Post existingPost = postService.getPostById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with ID: " + postId));

        User user = userService.getUserByUsername(postDto.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found with username: " + postDto.getUsername()));

        Post postToUpdate = postDto.toEntity(user);
        postToUpdate.setId(existingPost.getId());
        postToUpdate.setCreatedAt(existingPost.getCreatedAt());
        Post updatedPost = postService.updatePost(postId, postToUpdate);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        postService.getPostById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with ID: " + postId));
        postService.deletePost(postId);
        return ResponseEntity.ok("Post deleted successfully");
    }

    @GetMapping("/image/{postId}/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long postId, @PathVariable String imageName) throws IOException {
        Post post = postService.getPostById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with ID: " + postId));

        // Check if the image exists in the post
        if (!post.getImagePath().contains(imageName)) {
            throw new NotFoundException("Image not associated with the post: " + imageName);
        }

        // Load the image file from the filesystem
        String imagePath = "C:/Users/user/Desktop/Files/" + imageName;
        File imageFile = new File(imagePath);

        // Check if the file exists
        if (!imageFile.exists()) {
            throw new IOException("Image file not found: " + imagePath);
        }

        // Read the file as bytes
        byte[] imageData = Files.readAllBytes(imageFile.toPath());

        // Return the image data as a response
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)  // or .IMAGE_JPEG depending on the image type
                .body(imageData);
    }


    @DeleteMapping("/image/{postId}")
    public ResponseEntity<String> removeImage(@PathVariable Long postId,
                                              @RequestParam("imageName") String imageName) {
        postService.removeImageFromPost(postId, imageName);
        return ResponseEntity.ok("Image deleted successfully from post with ID: " + postId);
    }

    @PutMapping(value = "/image/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> addImagesToPost(
            @PathVariable Long postId,
            @RequestParam("image") List<MultipartFile> files) throws IOException {

        Post existingPost = postService.getPostById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with ID: " + postId));

        // Upload the images and add their paths to the existing post
        for (MultipartFile file : files) {
            String imagePath = storageService.uploadImageToFileSystem(file);
            existingPost.getImagePath().add(imagePath);
        }

        Post updatedPost = postService.updatePost(postId, existingPost);

        return ResponseEntity.ok(updatedPost);
    }
}
