package redact;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.licensekey.LicenseKey;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.pdfcleanup.PdfCleanUpTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import redact.storage.StorageFileNotFoundException;
import redact.storage.StorageService;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public ResponseEntity<Resource> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                              RedirectAttributes redirectAttributes) throws IOException {

        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        //File convFile = new File( file.getOriginalFilename());
        //file.transferTo(convFile);

        Resource fileResource = storageService.loadAsResource(file.getOriginalFilename());

        File redactFile = redactPdf(fileResource.getFile());
        //return "redirect:/";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Disposition", "attachment; filename = fileRedact.pdf");

        InputStreamResource resource = new InputStreamResource(new FileInputStream(redactFile));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(redactFile.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    public File redactPdf(File file) throws IOException { 

        String current = new java.io.File( "." ).getCanonicalPath();
        System.out.println("Current dir:"+current);

        //Load the license file to use cleanup features
        LicenseKey.loadLicenseFile("itextkey1519348717848_0.xml");

        File outFile = new File(file.getPath()+"-redact.pdf");
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(file), new PdfWriter(outFile));
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
        com.itextpdf.kernel.colors.Color myColor = new DeviceRgb(0, 0, 0);

        //cleanUpLocations.add(new PdfCleanUpLocation(1, new Rectangle(97, 405, 383, 40), myColor));

        // Business Address: No. Street City / Town Province - page 2
        cleanUpLocations.add(new PdfCleanUpLocation(2, new Rectangle(65, 647, 465, 16), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(2, new Rectangle(65, 630, 465, 16), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(2, new Rectangle(65, 610, 465, 16), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(2, new Rectangle(65, 592, 465, 16), myColor));

        // COMPLETE PRINCIPAL OFFICE ADDRESS - page 3
        cleanUpLocations.add(new PdfCleanUpLocation(3, new Rectangle(102, 381, 372, 21), myColor));

        // COMPLETE BUSINESS ADDRESS - page 3
        cleanUpLocations.add(new PdfCleanUpLocation(3, new Rectangle(102, 350, 372, 23), myColor));

        // COMPLETE BUSINESS ADDRESS - page 3
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 648, 184, 20), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 619, 184, 20), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 590, 184, 20), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 558, 184, 23), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 525, 184, 23), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 492, 184, 23), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 455, 184, 25), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 422, 184, 25), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 389, 184, 23), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 358, 184, 23), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 330, 184, 18), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 302, 184, 18), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 267, 184, 25), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 240, 184, 19), myColor));
        cleanUpLocations.add(new PdfCleanUpLocation(12, new Rectangle(70, 210, 184, 20), myColor));

        PdfCleanUpTool cleaner = new PdfCleanUpTool(pdfDoc, cleanUpLocations);
        cleaner.cleanUp();

        pdfDoc.close();

        return outFile;
    }

}
