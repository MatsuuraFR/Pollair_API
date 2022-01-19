package controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import Tools.FileInfo;
import Tools.JsonResponse;
import Tools.SQLRequests;
import Tools.TrajetsFileTemplate;
import model.Personne;
import model.PersonneRowMapper;
import service.FilesStorageService;
import service.FilesStorageServiceImpl;

//import com.bezkoder.spring.files.upload.model.FileInfo;
//import com.bezkoder.spring.files.upload.model.ResponseMessage;
//import com.bezkoder.spring.files.upload.service.FilesStorageService;

@RestController
@RequestMapping(path = "/fileshandler")
//@CrossOrigin("http://localhost:8080")
public class UploadFileHandler {

	@Autowired
	FilesStorageService storageService;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@PostMapping("/uploadone")
	public ResponseEntity<Object> uploadOneFile(@RequestParam("file") MultipartFile file,@RequestParam("id") String idLogin) {
		//String message = "";
		
		//1- vérification de l'extension du fichier
		String[] fileNameSplit = file.getOriginalFilename().split("\\.");
		if(fileNameSplit.length >= 2) {
			if(fileNameSplit[fileNameSplit.length - 1].equals("timeline")) {
				//return JsonResponse.generateResponse("Extension de fichiers valide", 200, null);
				
				//2 - on vérifie que la personne existe 
				try {
					Personne personne = jdbcTemplate.queryForObject(SQLRequests.SelectPersonById, new PersonneRowMapper(), idLogin);
					
					//3 - ajout du fichier
					try {
						storageService.save(file,idLogin,FilesStorageServiceImpl.TimelineFolderName);
						
						// 4 - lancement du traitement (gérer en async)
						
						return JsonResponse.generateResponse("Extension de fichiers valide", 200, null);
					}catch(Exception e){
						return JsonResponse.generateResponse("Erreur lors de l'ajout du fichier", -1, null);
					}
					
				}catch(EmptyResultDataAccessException e) {
					
					return JsonResponse.generateResponse("ID inconnu", 0, null);
				}
			}else {
				return JsonResponse.generateResponse("Extension de fichiers invalide: ", 1, null);
			}
		}else {
			return JsonResponse.generateResponse("Erreur extension fichier: ", -1, null);
		}
	}
	
	/*
	 * TODO appel en async
	 */
	private void processFileToJson(String file, String idLogin) {
		/*
		 * 1 - création d'une task
		 * 2 - chargement du trajets.json de la personne
		 * 3 - ajout des elements dans le json si ils n'existent pas
		 * 4 - 
		 * x - task en complet
		 */
		
		//oject qui permettra la sauvegarde du fichier trajets.json
		JSONObject trajetsJson = null;
		
		//Resource jsonFile = storageService.load(filename,idLogin);
		
		//1 - création d'une task
		//TODO
		
		//2 - chargement du trajets.json de la personne
		try {
			Resource jsonFile = storageService.loadTrajetJson(idLogin);
			
			try (Reader reader = new InputStreamReader(jsonFile.getInputStream())){
				String jsonString = FileCopyUtils.copyToString(reader);
				
				trajetsJson = new JSONObject(jsonString);
				
				//System.out.println(existingtrajetsJson.toString(1));
				
			}catch (IOException e) {
				System.err.println("IOException1: "+e);
			}
		}catch(Exception e) {
			//Le fichier n'existe pas
			//TODO charger le template json
		}
		
		//3 - 
		//TrajetsFileTemplate trajetsFileTemplate = new TrajetsFileTemplate();
		//chargement du template si aucun fichier dans la session n'existe
		if(trajetsJson == null) {
			//trajetsFileTemplate.setCleaned_place(existingtrajetsJson.getJSONArray("cleaned_place"));
			try {
				Resource jsonFileTemplate = storageService.load("trajets_file_template.json", idLogin, "template");
				try (Reader reader = new InputStreamReader(jsonFileTemplate.getInputStream())){
					String jsonString = FileCopyUtils.copyToString(reader);
					
					trajetsJson = new JSONObject(jsonString);
					
				}catch (IOException e) {
					//le fichier n'existe pas
					System.out.println("file not found: "+e.getMessage());
				}
				
			}catch(Exception e) {
				System.err.println(e);
			}
		}
		
		//4 - 
		//TODO ajout (avec vérif si existe déjà) des trajets
		
		//5 - sauvegarde du fichier
		Path filePath = FilesStorageServiceImpl.rootPersonne;
		filePath = filePath.resolve(idLogin + "/" + FilesStorageServiceImpl.TrajetFolderName);
		try {
			//si le dossier trajet n'existe pas, on le crée
			if(!Files.exists(filePath)) {
				Files.createDirectories(filePath);
			}
			if(trajetsJson != null) {
				try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toString()+"/trajets.json"))){
					writer.write(trajetsJson.toString(1));
					
					//Files.copy(file.getInputStream(), folderSession.resolve(file.getOriginalFilename()),  StandardCopyOption.REPLACE_EXISTING);
				}catch (IOException e) {
					System.err.println("IOException2: " + e);
				}
			}
		
		}catch (IOException e) {
			System.err.println(e);
		}
		
		
		//System.out.println(trajetsFileTemplate.toString(1));
	}
	
	@GetMapping("/testprocesstimeline/{typeOS}/{idLogin}/{filename}")
	@ResponseBody
	public ResponseEntity<Object> readJson(@PathVariable String filename,@PathVariable("idLogin") String idLogin, @PathVariable("typeOS") String typeOS) {
		
		processFileToJson(filename,idLogin);
		
		return null; //debug
	}
	
	@GetMapping("/files/{idLogin}")
	public /*ResponseEntity<List<FileInfo>>*/ ResponseEntity<Object> getListFiles(@PathVariable("idLogin") String idLogin) {
		
		// on vérifie que la session existe
		try {
			Personne personne = jdbcTemplate.queryForObject(SQLRequests.SelectPersonById, new PersonneRowMapper(), idLogin);
			
			List<FileInfo> fileInfos = storageService.loadAll(idLogin).map(path -> {
				String filename = path.getFileName().toString();
				String url = MvcUriComponentsBuilder
						.fromMethodName(UploadFileHandler.class, "getFile", path.getFileName().toString(),idLogin).build()
						.toString();

				return new FileInfo(filename, url);
			}).collect(Collectors.toList());

			//return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
			return JsonResponse.generateResponse("ok", 200, fileInfos);
		}catch(EmptyResultDataAccessException e) {
			
			return JsonResponse.generateResponse("ID inconnu", 0, null);
		}
		
		/*
		List<FileInfo> fileInfos = storageService.loadAll(idLogin).map(path -> {
			String filename = path.getFileName().toString();
			String url = MvcUriComponentsBuilder
					.fromMethodName(UploadFileHandler.class, "getFile", path.getFileName().toString(),idLogin).build()
					.toString();

			return new FileInfo(filename, url);
		}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
		*/
		
	}
	
	@GetMapping("/files/{idLogin}/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> getFile(@PathVariable String filename,@PathVariable("idLogin") String idLogin) {
		Resource file = storageService.load(filename,idLogin,"timeline");
		//System.out.println("file.getFilename():"+file.getFilename());//debug
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}
	
	
	@GetMapping("/delete/{idLogin}/{filename}")
	@ResponseBody
	public ResponseEntity<Object> deleteFile(@PathVariable String filename,@PathVariable("idLogin") String idLogin){
		
		try {
			int success = storageService.delete(filename, idLogin);
			
			if(success == 200) {
				return JsonResponse.generateResponse("ok", 200, null);
			}else {
				return JsonResponse.generateResponse("Le fichier n'existe pas", 0, null);
			}
			
		}catch(Exception e){
			return JsonResponse.generateResponse("Erreur ", -1, null);
		}
	}
	
	
	
	
	/*
	@PostMapping("/upload")
	public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
		String message = "";
		try {
			storageService.save(file);

			message = "Uploaded the file successfully: " + file.getOriginalFilename();
			return ResponseEntity.status(HttpStatus.OK).body(message);
		} catch (Exception e) {
			message = "Could not upload the file: " + file.getOriginalFilename() + "!";
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
		}
	}

	@GetMapping("/files")
	public ResponseEntity<List<FileInfo>> getListFiles() {
		List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
			String filename = path.getFileName().toString();
			String url = MvcUriComponentsBuilder
					.fromMethodName(UploadFileHandler.class, "getFile", path.getFileName().toString()).build()
					.toString();

			return new FileInfo(filename, url);
		}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> getFile(@PathVariable String filename) {
		Resource file = storageService.load(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}
	*/

}
