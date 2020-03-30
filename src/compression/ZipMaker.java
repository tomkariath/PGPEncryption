package compression;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipMaker {
	
	private static List<String> filesList = new ArrayList<String>();

	public static void zipFiles (String... filePaths) {
		
		String firstFileName = filePaths[0];
		String zipName = firstFileName.substring(0, firstFileName.lastIndexOf(".")).concat(".zip");
		
		try {
			//creating zip File
			FileOutputStream fileOutputStream = new FileOutputStream(zipName);
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
			
			//reading Files one-by-one
			for (String filePath : filePaths) {
				zipOutputStream.putNextEntry(new ZipEntry(new File(filePath).getName()));
				byte[] bytes = Files.readAllBytes(Path.of(filePath));
				
				zipOutputStream.write(bytes, 0, bytes.length);
				zipOutputStream.closeEntry();
			}
			
			zipOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void zipDirectory (String directoryPath) {

		File directory = new File(directoryPath);
		String zipName = directory.getAbsolutePath().concat(".zip");
		generateFilesList(directory);

		//creating zip File
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(zipName);
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

			for (String filePath : filesList) {
				new File(filePath);
				ZipEntry zipEntry = new ZipEntry(filePath.substring(directory.getAbsolutePath().length()+1));
				zipOutputStream.putNextEntry(zipEntry);
				byte[] bytes = Files.readAllBytes(Path.of(filePath));

				zipOutputStream.write(bytes, 0, bytes.length);
				zipOutputStream.closeEntry();
			}

			zipOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void generateFilesList (File directory) {
		
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				filesList.add(file.getAbsolutePath());
			}
			else {
				generateFilesList(file);
			}
		}
	}
	
	public static void main(String[] args) {
		zipFiles("D:\\Downloads\\Home Page.png", "D:\\Downloads\\Form Page.png");
		zipDirectory("D:\\Downloads\\WorkDocs\\testFolder");
	}
}
