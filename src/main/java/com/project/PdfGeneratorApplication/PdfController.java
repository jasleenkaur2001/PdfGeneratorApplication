package com.project.PdfGeneratorApplication;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.google.gson.Gson;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
class PdfController {

    private final TemplateEngine templateEngine;
    public PdfController() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");

        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        
    }
    
  
    
    @PostMapping(value = "/generatePdf", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> generatePdf(@RequestBody PdfDetails pdfDetails) throws IOException {
    	
    	DBManager db = new DBManager();
        if(db.checkInMap(pdfDetails)) {
        	String fileName = db.getFileName(pdfDetails);
     		HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String filePath = "C:\\PDFs\\" + fileName;
            Path pdfPath = Paths.get(filePath);
            byte[] pdf = Files.readAllBytes(pdfPath);
            return new ResponseEntity<>(pdf,headers , HttpStatus.OK);
        }
    	try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            String htmlContent = generateHtml(pdfDetails);
            HtmlConverter.convertToPdf(htmlContent , outputStream);
            HttpHeaders headers = new HttpHeaders();
            String fileName = "invoice_" + db.getCounter() + ".pdf";
            headers.setContentType(MediaType.APPLICATION_PDF);
            db.addToMap(pdfDetails, fileName);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);       
            db.updateCounter();            
            String filePath = "C:\\PDFs\\" + fileName;
            File myObj = new File(filePath);
            myObj.createNewFile();          
            FileOutputStream out = new FileOutputStream(filePath);
            out.write(outputStream.toByteArray());
            out.close();
            return new ResponseEntity<>(outputStream.toByteArray(),headers , HttpStatus.OK);
        } catch (DocumentException e) {
            e.printStackTrace();
           
            return new ResponseEntity<>(null , null , HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String generateHtml(PdfDetails pdfDetails) {
        Context context = new Context();
        context.setVariable("pdfDetails", pdfDetails);
        return templateEngine.process("pdf-template", context);
    }
}