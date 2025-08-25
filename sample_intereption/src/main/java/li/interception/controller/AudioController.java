package li.interception.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/audio")
@CrossOrigin(origins = "*") // Allow Vue app to call API
public class AudioController {

    @GetMapping("/play/{id}")
    public ResponseEntity<byte[]> playAudio(@PathVariable("id") int id) throws IOException {
        //Play the same sample audio for any ID
        ClassPathResource audioFile = new ClassPathResource("audio/sample.wav");

        byte[] audioBytes = Files.readAllBytes(audioFile.getFile().toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        headers.setContentLength(audioBytes.length);
        headers.set("Content-Disposition", "inline; filename=\"sample.wav\"");

        return ResponseEntity.ok().headers(headers).body(audioBytes);
    }
}