package com.shoppingcart.admin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class FileUploadUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadUtil.class);

	public static void saveFile(String uploadDir, String fileName, MultipartFile multipartFile) throws IOException {
		Path uploadPath = Paths.get(uploadDir);//uploadDir là đường dẫn lưu file hình

		if (!Files.exists(uploadPath)) {//nếu folder lưu hình chưa tồn tại thì tạo folder tương ứng với đường dẫn
			Files.createDirectories(uploadPath);
		}

		try (InputStream inputStream = multipartFile.getInputStream()) {
			Path filePath = uploadPath.resolve(fileName);
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);//copy file hình vào folder
		} catch (IOException ex) {
			throw new IOException("Could not save file: " + fileName, ex);
		}
	}

	public static void cleanDir(String dir) {
		Path dirPath = Paths.get(dir);

		try {
			Files.list(dirPath).forEach(file -> {
				if (!Files.isDirectory(file)) {
					try {
						Files.delete(file);//xóa tất cả file hình nằm bên trong folder vì chỉ tồn tại 1 hình trong 1 folder
					} catch (IOException ex) {
						LOGGER.error("Could not delete file: " + file);
					}
				}
			});
		} catch (IOException ex) {
			LOGGER.error("Could not list directory: " + dirPath);
		}
	}

	public static void removeDir(String dir) {
		cleanDir(dir);//xóa tất cả file hình nằm bên trong folder vì chỉ tồn tại 1 hình trong 1 folder
		try {
			Files.delete(Paths.get(dir));//xóa folder
		} catch (IOException e) {
			LOGGER.error("Could not remove directory: " + dir);
		}

	}
}
