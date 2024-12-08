package org.example.journal_app.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


@Service
public class StorageService {

    private final String FOLDER_PATH = "C:\\Users\\user\\Desktop\\Files\\";


    public String uploadImageToFileSystem(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        File destinationFile = new File(FOLDER_PATH, fileName);
        String normalizedFilePath = destinationFile.getCanonicalPath();
        System.out.println("Saving file: " + normalizedFilePath);
        file.transferTo(destinationFile);
        return fileName;
    }

    public byte[] downloadImageFromFileSystem(String imageName) throws IOException {
        File file = new File(FOLDER_PATH, imageName);
        String filePath = file.getCanonicalPath();

        if (!file.exists()) {
            throw new IOException("File does not exist on the file system: " + filePath);
        }
        System.out.println("Reading file: " + filePath);
        return Files.readAllBytes(file.toPath());
    }
}
