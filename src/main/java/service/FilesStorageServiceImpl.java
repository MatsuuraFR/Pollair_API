package service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FilesStorageServiceImpl implements FilesStorageService {
	private final Path root = Paths.get("data");

	@Override
	public void init() {
		try {
			Files.createDirectories(root);
		} catch (IOException e) {
			System.err.println(e);
			// throw new RuntimeException("Could not initialize folder for upload!");
		}
	}

	@Override
	public void save(MultipartFile file, String idLogin) {
		try {
			Path folderSession = root.resolve(idLogin);
			if(Files.exists(folderSession)) {
				//System.out.println("fichier existe");//debug
				
			}else {
				//System.out.println("fichier n'existe pas");//debug
				Files.createDirectories(folderSession);
			}
			Files.copy(file.getInputStream(), folderSession.resolve(file.getOriginalFilename()),  StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			//System.out.println(e);//debug
			throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
		}
	}

	@Override
	public Resource load(String filename, String idLogin) {
		try {
			Path folderSession = root.resolve(idLogin);
			
			Path file = folderSession.resolve(filename);
			//System.out.println(file.getFileName());//debug
			Resource resource = new UrlResource(file.toUri());

			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new RuntimeException("Could not read the file!");
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}
	
	/*
	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(root.toFile());
	}
	*/
	
	@Override
	public int delete(String filename, String idLogin){
		Path folderSession = root.resolve(idLogin);
		Path file = folderSession.resolve(filename);
		
		try {
			if(Files.exists(file)) {
				Files.delete(file);
				return 200;
			}else {
				return 0;
			}
		}catch(Exception e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}

	@Override
	public Stream<Path> loadAll(String idLogin) {
		try {
			Path folderSession = root.resolve(idLogin);
			return Files.walk(folderSession, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
		} catch (IOException e) {
			throw new RuntimeException("Could not load the files!");
		}
	}
}
