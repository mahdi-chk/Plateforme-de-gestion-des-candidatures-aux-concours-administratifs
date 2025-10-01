package com.concours.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/debug")
@Slf4j
public class DiagnosticController {

    @PostMapping("/multipart-info")
    @ResponseBody
    public Map<String, Object> diagnosticMultipart(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();

        try {
            info.put("requestClass", request.getClass().getName());
            info.put("contentType", request.getContentType());
            info.put("contentLength", request.getContentLength());
            info.put("characterEncoding", request.getCharacterEncoding());

            // Compter les paramètres
            info.put("parameterCount", request.getParameterMap().size());

            // Compter les parties si multipart
            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                info.put("isMultipartRequest", true);
                info.put("multipartFileCount", multipartRequest.getMultiFileMap().size());

                // Détailler les fichiers
                Map<String, String> files = new HashMap<>();
                for (Iterator<String> it = multipartRequest.getFileNames(); it.hasNext(); ) {
                    String name = it.next();
                    MultipartFile file = multipartRequest.getFile(name);
                    if (file != null) {
                        files.put(name, file.getOriginalFilename() + " (" + file.getSize() + " bytes)");
                    }
                }
                info.put("files", files);
            } else {
                info.put("isMultipartRequest", false);
            }

            // Compter les parties avec l'API Servlet
            try {
                Collection<Part> parts = request.getParts();
                info.put("totalPartsCount", parts.size());

                Map<String, String> partDetails = new HashMap<>();
                for (Part part : parts) {
                    partDetails.put(part.getName(),
                            "Size: " + part.getSize() + ", Type: " + part.getContentType());
                }
                info.put("partDetails", partDetails);

            } catch (Exception e) {
                info.put("partsError", e.getMessage());
                log.error("Erreur lors de l'accès aux parties", e);
            }

        } catch (Exception e) {
            info.put("generalError", e.getMessage());
            log.error("Erreur générale dans le diagnostic", e);
        }

        return info;
    }
}