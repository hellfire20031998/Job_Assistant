package com.hellFire.JobAssistant.service.resume;

import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ResumePdfTextExtractor {

	public String extractText(byte[] pdfBytes, String originalFilename) {
		Resource resource = new NamedByteArrayResource(pdfBytes, originalFilename != null ? originalFilename : "resume.pdf");
		var reader = new PagePdfDocumentReader(resource);
		return reader.get().stream().map(Document::getText).collect(Collectors.joining("\n\n"));
	}

	private static final class NamedByteArrayResource extends ByteArrayResource {
		private final String filename;

		NamedByteArrayResource(byte[] bytes, String filename) {
			super(bytes);
			this.filename = filename;
		}

		@Override
		public String getFilename() {
			return filename;
		}
	}
}
