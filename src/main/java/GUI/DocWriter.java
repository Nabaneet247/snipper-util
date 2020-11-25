package GUI;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class DocWriter {

    private static double maxWidthInInches = 6.2;
    private static String docFileExtension = ".docx";

    void insertText(XWPFDocument doc, String text) {
        XWPFRun run = doc.createParagraph().createRun();
        run.setText(text);
    }

    void insertPic(XWPFDocument doc, BufferedImage bufferedImage) throws IOException, InvalidFormatException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        double width = bufferedImage.getWidth();
        double height = bufferedImage.getHeight();
        double scalingFactor = 1.0;
        if (width > 72 * maxWidthInInches) {
            scalingFactor = (72 * maxWidthInInches)/width; // scale width not to be greater than 6 inches
        }

        XWPFRun run = doc.createParagraph().createRun();
        run.addPicture(is, XWPFDocument.PICTURE_TYPE_JPEG, "image", Units.toEMU(width * scalingFactor), Units.toEMU(height * scalingFactor));
    }

    String saveDoc(XWPFDocument doc, String fileName, String directory) throws IOException {
        String outputPath;
        try {
            outputPath = getCreatableFileNameWithPath(fileName, directory);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            outputPath = getCreatableFileNameWithPath(Constants.DEFAULT_FILE_NAME, System.getProperty("user.dir"));
        }
        File outputFile = new File(outputPath);
        FileOutputStream out = new FileOutputStream(outputFile);
        doc.write(out);
        out.close();
        doc.close();
        return outputPath;
    }

    private String getCreatableFileNameWithPath(String fileName, String directory) throws IOException {
        String finalFileName = fileName;
        String outputPath = generateFilePath(fileName, directory);
        File outputFile = new File(outputPath);

        if (!outputFile.createNewFile()) {
            File folder = new File(directory);
            File[] listOfFiles = folder.listFiles();
            List<String> fileSuffixes = new ArrayList<>();

            for (int i = 0; listOfFiles != null && i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(docFileExtension) && listOfFiles[i].getName().startsWith(fileName)) {
                    String currentFileName = listOfFiles[i].getName();
                    String suffix = currentFileName.substring(currentFileName.indexOf(fileName) + fileName.length(), currentFileName.lastIndexOf(docFileExtension));
                    fileSuffixes.add(suffix.replaceAll("\\D", StringUtils.EMPTY));
                }
            }
            if (fileSuffixes.size() == 0) {
                finalFileName = fileName + StringUtils.SPACE + "1";
            } else {
                fileSuffixes.sort(String::compareToIgnoreCase);
                String lastSuffix = fileSuffixes.get(fileSuffixes.size() - 1);
                finalFileName = fileName + StringUtils.SPACE  + (Integer.parseInt(StringUtils.isNotBlank(lastSuffix) ? lastSuffix : "0") + 1);
            }
        }

        return generateFilePath(finalFileName, directory);
    }

    private String generateFilePath(String fileName, String directory) {
        return directory + "\\" + fileName + docFileExtension;
    }
}
