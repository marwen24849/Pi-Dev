package esprit.tn.pidevrh.leave;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import java.io.File;

public class PDFGenerator {
    public static File generateLeavePDF(Leave leave) {
        try {
            File pdfFile = new File("Leave_Request_" + leave.getId() + ".pdf");
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Détails de la Demande de Congé"));
            document.add(new Paragraph("Utilisateur: " + leave.getUserId()));
            document.add(new Paragraph("Type de Congé: " + leave.getTypeConge()));
            document.add(new Paragraph("Date de Début: " + leave.getDateDebut()));
            document.add(new Paragraph("Date de Fin: " + leave.getDateFin()));
            document.add(new Paragraph("Durée: " + leave.getDateDebut().until(leave.getDateFin()).getDays() + " jours"));
            document.add(new Paragraph("Statut: APPROVED"));

            document.close();
            return pdfFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}