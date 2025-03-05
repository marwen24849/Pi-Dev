package esprit.tn.pidevrh.leave;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import esprit.tn.pidevrh.connection.DatabaseConnection;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PDFGenerator {

    public static File generateLeavePDF(Leave leave) {
        try {
            // Define the PDF file name
            File pdfFile = new File("Leave_Approval_" + leave.getId() + ".pdf");

            // Initialize the PDF document
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Date Formatter
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Get current date (Approval Date)
            String approvalDate = LocalDate.now().format(dateFormatter);

            // Fetch User Full Name
            String userName = getUserName(leave.getUserId());

            // 📌 1. Company Header
            document.add(new Paragraph("SGRH - Gestion des Congés")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLUE)
                    .setMarginBottom(20));

            // 📌 2. Approval Date (Top Right)
            document.add(new Paragraph("Date : " + approvalDate)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(10));

            // 📌 3. Subject Line
            document.add(new Paragraph("Objet : Acceptation de la Demande de Congé")
                    .setBold()
                    .setUnderline()
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(15));

            // 📌 4. Formal Greeting
            document.add(new Paragraph("Monsieur/Madame " + userName + ",")
                    .setMarginBottom(10));

            // 📌 5. Approval Message
            document.add(new Paragraph("Nous avons le plaisir de vous informer que votre demande de congé "
                    + "pour la période du " + leave.getDateDebut().format(dateFormatter) + " au "
                    + leave.getDateFin().format(dateFormatter) + " a été approuvée."));

            document.add(new Paragraph("Vous êtes donc autorisé(e) à bénéficier de votre congé conformément "
                    + "aux règles en vigueur au sein de notre entreprise.")
                    .setMarginBottom(10));

            // 📌 6. Additional Information
            document.add(new Paragraph("Merci de vous assurer que toutes vos responsabilités sont bien "
                    + "prises en charge avant votre départ afin de garantir la continuité du service."));

            document.add(new Paragraph("Nous vous souhaitons un agréable congé.")
                    .setMarginBottom(40));

            // 📌 7. Signature Section
            document.add(new Paragraph("Cordialement,")
                    .setBold()
                    .setMarginBottom(5));
            document.add(new Paragraph("Responsable RH")
                    .setBold()
                    .setMarginBottom(10));

            // 📌 8. Overlay Signature on Approved Stamp
            try {
                InputStream signatureStream = PDFGenerator.class.getResourceAsStream("/signature.png");
                InputStream approvedStream = PDFGenerator.class.getResourceAsStream("/approved.png");

                if (signatureStream != null && approvedStream != null) {
                    ImageData signatureData = ImageDataFactory.create(signatureStream.readAllBytes());
                    ImageData approvedData = ImageDataFactory.create(approvedStream.readAllBytes());

                    // Create images
                    Image approvedImage = new Image(approvedData).scaleToFit(150, 150);  // Resize stamp
                    Image signatureImage = new Image(signatureData).scaleToFit(100, 50); // Resize signature

                    // Set positions (absolute positioning)
                    float xPosition = 350; // X coordinate
                    float yPosition = 150; // Y coordinate (lower part of the page)

                    approvedImage.setFixedPosition(xPosition, yPosition);
                    signatureImage.setFixedPosition(xPosition + 20, yPosition + 40); // Overlay signature on top

                    // Add images to document
                    document.add(approvedImage);
                    document.add(signatureImage);

                } else {
                    document.add(new Paragraph("⚠ Signature ou cachet APPROUVÉ non disponible").setFontColor(ColorConstants.RED));
                }
            } catch (Exception e) {
                document.add(new Paragraph("⚠ Erreur lors du chargement de la signature ou du cachet APPROUVÉ").setFontColor(ColorConstants.RED));
                e.printStackTrace();
            }

            // Close the document
            document.close();
            return pdfFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the user's full name from the database.
     */
    private static String getUserName(int userId) {
        String userName = "Utilisateur " + userId;  // Default in case of error
        String query = "SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM user WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                userName = rs.getString("full_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la récupération du nom de l'utilisateur: " + e.getMessage());
        }

        return userName;
    }
}
