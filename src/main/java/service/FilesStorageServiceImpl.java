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
	public static final Path rootPersonne = Paths.get("data/personne");
	public static final Path rootTemplate = Paths.get("data/template");
	public static final Path rootWfs = Paths.get("data/wfs");
	
	//TODO enum
	public static final String TimelineFolderName = "timeline";
	public static final String TrajetFolderName = "trajet";
	public static final String TemplateFolderName = "template";
	public static final String WfsFolderName = "wfs";


	@Override
	public void init() {
		try {
			Files.createDirectories(rootPersonne);
		} catch (IOException e) {
			System.err.println(e);
			// throw new RuntimeException("Could not initialize folder for upload!");
		}
	}

	@Override
	public void save(MultipartFile file, String idLogin, String type) {
		try {
			//Path folderSession = root.resolve(idLogin + "/" + TimelineFolderName);
			Path folderSession = null;
			switch(type) {
				case TimelineFolderName : {
					folderSession = rootPersonne.resolve(idLogin + "/" + TimelineFolderName);
				}
				break;
				case TrajetFolderName : {
					folderSession = rootPersonne.resolve(idLogin + "/" + TrajetFolderName);
				}
				break;
				/*
				case TemplateFolderName : {
					folderSession = root.resolve(idLogin + "/" + TemplateFolderName);
				}
				*/
				//break;
				/*
				case WfsFolderName : {
					folderSession = rootWfs;
				}
				
				break;
				*/
				default : {
					throw new RuntimeException("Invalid type");
				}
			}
			
			if(Files.exists(folderSession)) {
				//System.out.println("folder existe");//debug
				
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
	public Resource load(String filename, String idLogin, String type) {
		try {
			
			Path folderSession = null;
			switch(type) {
				case TimelineFolderName : {
					folderSession = rootPersonne.resolve(idLogin + "/" + TimelineFolderName);
				}
				break;
				case TrajetFolderName : {
					folderSession = rootPersonne.resolve(idLogin + "/" + TrajetFolderName);
				}
				break;
				case TemplateFolderName : {
					//folderSession = rootPersonne.resolve(idLogin + "/" + TemplateFolderName);
					folderSession = rootTemplate;
				}
				break;
				case WfsFolderName : {
					folderSession = rootWfs;
				}
				break;
				default : {
					throw new RuntimeException("Invalid type");
				}
			}
			
			
			
			Path file = folderSession.resolve(filename);
			//System.out.println(file.getFileName());//debug
			Resource resource = new UrlResource(file.toUri());

			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new RuntimeException("Could not read the file");
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}
	
	@Override
	public Resource loadTrajetJson(String idLogin) {
		return load("trajets.json", idLogin, TrajetFolderName);
	}
	
	/*
	@Override
	public void saveTrajetsJson(String data,String idLogin) {
		
	}
	*/
	
	/*
	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(root.toFile());
	}
	*/
	
	@Override
	public int delete(String filename, String idLogin){
		Path folderSession = rootPersonne.resolve(idLogin + "/" + TimelineFolderName);
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
			Path folderSession = rootPersonne.resolve(idLogin + "/" + TimelineFolderName);
			return Files.walk(folderSession, 1).filter(path -> !path.equals(this.rootPersonne)).map(this.rootPersonne::relativize);
		} catch (IOException e) {
			throw new RuntimeException("Could not load the files!");
		}
	}
}
