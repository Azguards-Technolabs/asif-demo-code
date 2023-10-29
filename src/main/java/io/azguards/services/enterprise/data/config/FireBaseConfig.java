package io.azguards.services.enterprise.data.config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FireBaseConfig {
	
	@Value("${fire-base.creds-file-path}")
	private String serviceAccountCredsFilePath;	

	@Bean
	FirebaseApp createFireBaseApp() throws IOException {
		FileInputStream serviceAccount = new FileInputStream(serviceAccountCredsFilePath);

		FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

		return FirebaseApp.initializeApp(options);
	}
}
