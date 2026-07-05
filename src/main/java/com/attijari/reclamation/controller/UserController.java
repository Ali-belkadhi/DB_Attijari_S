package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.CreateUserDto;
import com.attijari.reclamation.dto.UpdateUserDto;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "users", description = "Users management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public User create(@Valid @RequestBody CreateUserDto createUserDto) {
        return userService.create(createUserDto);
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID")
    public User findOne(@PathVariable String id) {
        return userService.findOne(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user by ID")
    public User update(@PathVariable String id, @Valid @RequestBody UpdateUserDto updateUserDto) {
        return userService.update(id, updateUserDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user by ID")
    public Map<String, String> remove(@PathVariable String id) {
        userService.remove(id);
        return Map.of("message", "User deleted successfully");
    }

    @PostMapping(value = "/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile image for a user")
    public User uploadImage(
            @PathVariable String id,
            @RequestParam("image") MultipartFile file) {
        
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No image file provided");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/jpg") && !contentType.equals("image/png"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files (jpg, jpeg, png) are allowed!");
        }

        try {
            // Ensure uploads directory exists
            Path uploadDir = Paths.get("uploads/profile-images");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Get original file extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                // Default to png if no extension is found
                extension = ".png";
            }

            // Construct unique file name matching NestJS logic
            String uniqueSuffix = System.currentTimeMillis() + "-" + Math.round(Math.random() * 1e9);
            String filename = "user-" + id + "-" + uniqueSuffix + extension;

            // Target path
            Path targetPath = uploadDir.resolve(filename);

            // Copy file to target path
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Relative path to store in database
            String relativeImagePath = "/uploads/profile-images/" + filename;

            return userService.updateImage(id, relativeImagePath);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image: " + e.getMessage());
        }
    }
}
