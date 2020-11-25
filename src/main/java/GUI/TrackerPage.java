package GUI;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrackerPage extends JPanel implements ActionListener, NativeKeyListener {
    private JFrame frame;
    private String title;
    private HomePage homePage;
    private JTextArea log;
    private JTextArea textInputArea;
    private JButton insertTextButton, takeScreenshotButton, stopRecordingButton;
    private int screenshotsCaptured = 0;
    private boolean isShiftPressed;
    private XWPFDocument docx;
    private DocWriter docWriter;
    private JTextField delayDuration;

    static private final String TEXT_FIELD_LABEL = "File Name: ";
    static private final String DEFAULT_DELAY_DURATION = "300";
    static private final String TEXT_FIELD_PLACEHOLDER = "Enter text here";

    public TrackerPage() {}

    TrackerPage(String title, HomePage homePage) {
        this.title = title;
        this.frame = new JFrame(title);
        this.homePage = homePage;

        log = new JTextArea(5,50);
        log.setFont(FontGenerator.getDefaultFont(Constants.TRACKER_LOG_FONT_SIZE));
        log.setMargin(new Insets(5,5,5,5));
        ((DefaultCaret)log.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        log.setEditable(false);
        log.append("Logs:-\n");
        JScrollPane logScrollPane = new JScrollPane(log);

        textInputArea = new JTextArea(3,32);
        textInputArea.setFont(FontGenerator.getRandomFont(Constants.TRACKER_LOG_FONT_SIZE));
        textInputArea.setMargin(new Insets(5,5,5,5));
        textInputArea.setEditable(true);
        textInputArea.setText(TEXT_FIELD_PLACEHOLDER);
        JScrollPane textArea = new JScrollPane(textInputArea);

        JPanel buttonPanel = new JPanel();
        JLabel delayLabel = new JLabel();
        delayLabel.setFont(FontGenerator.getRandomFont(Constants.TRACKER_LOG_FONT_SIZE));
        delayLabel.setText("Delay");
        delayDuration = new JTextField();
        delayDuration.setText(DEFAULT_DELAY_DURATION);
        delayDuration.setColumns(3);
        delayDuration.setEditable(true);

        insertTextButton = new JButton("Insert above text");
        insertTextButton.addActionListener(this);

        takeScreenshotButton = new JButton("Insert Screenshot (Shift + A)");
        takeScreenshotButton.addActionListener(this);

        stopRecordingButton = new JButton("Stop");
        stopRecordingButton.addActionListener(this);

        buttonPanel.add(delayLabel);
        buttonPanel.add(delayDuration);
        buttonPanel.add(insertTextButton);
        buttonPanel.add(takeScreenshotButton);
        buttonPanel.add(stopRecordingButton);

        add(logScrollPane, BorderLayout.PAGE_START);
        add(textArea, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.PAGE_END);

        docx = new XWPFDocument();
        docWriter = new DocWriter();
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == insertTextButton) {
            insertTextData(textInputArea.getText());
        } else if (e.getSource() == takeScreenshotButton) {
            insertScreenshot();
        } else if (e.getSource() == stopRecordingButton) {
            if (screenshotsCaptured > 0) {
                saveFile();
            } else {
                homePage.insertLog("No screenshots were captured");
            }
            homePage.hideTrackerPage();
        }
    }

    private void createAndShowGUI() {
        //Create and set up the window.
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Add content to the window.
        frame.add(this);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                homePage.hideTrackerPage();
            }
        });

        //Display the window.-
        frame.setPreferredSize(new Dimension(550, 300));
        frame.setLocation(10, 10);
        frame.pack();
        frame.setVisible(true);
        this.resetScreenshotsCaptured();

        try {
            Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            ex.printStackTrace();
            homePage.hideTrackerPage();
        }
        GlobalScreen.addNativeKeyListener(this);
    }

    void showFrame() {
        this.createAndShowGUI();
        frame.setVisible(true);
    }

    void hideFrame() {
        frame.setVisible(false);
        frame.dispose();
    }

    boolean isPageVisible() {
        return frame != null && frame.isVisible();
    }

    private void resetScreenshotsCaptured() {
        screenshotsCaptured = 0;
    }

    private void incrementScreenshotsCaptured() {
        screenshotsCaptured += 1;
    }

    private void saveFile() {
        try {
            String outputPath = docWriter.saveDoc(docx, this.homePage.getOutputFileName(), this.homePage.getOutputFolderPath());
            homePage.insertLog("File has been saved at " + outputPath);
        } catch (Exception ex) {
            System.err.println(ex);
            homePage.insertLog("Encountered error while saving document");
        }
    }

    private void captureScreenshot() {
        try {
            if (StringUtils.isBlank(delayDuration.getText().replaceAll("\\D", StringUtils.EMPTY))) {
                delayDuration.setText(DEFAULT_DELAY_DURATION);
            }
            TimeUnit.MILLISECONDS.sleep(Integer.parseInt(delayDuration.getText()));
            Robot robot = new Robot();
            String format = "jpg";
            String fileName = "FullScreenshot." + format;

            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
            docWriter.insertPic(docx, screenFullImage);
        }  catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    private void insertScreenshot() {
        frame.setVisible(false);
        captureScreenshot();
        frame.setVisible(true);
        incrementScreenshotsCaptured();
        insertLog("Screenshot inserted. Total = " + screenshotsCaptured);
    }

    private void insertTextData(String data) {
        if (StringUtils.isNotBlank(data) && !TEXT_FIELD_PLACEHOLDER.equals(data)) {
            try {
                docWriter.insertText(docx, data);
                textInputArea.setText(StringUtils.EMPTY);
                insertLog("Text Inserted");
            } catch (Exception ex) {
                insertLog("Encountered error while inserting text. Try again");
                System.err.println(ex);
            }
        } else {
            insertLog("Can't insert blank data");
        }
    }

    private void insertLog(String message) {
        log.append("[" + getFormattedLogTime() + "]\n" + message + "\n");
    }

    private String getFormattedLogTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(Calendar.getInstance().getTime());
    }

    public void nativeKeyTyped(NativeKeyEvent var1) {
    }

    public void nativeKeyPressed(NativeKeyEvent var1) {
        if (var1.getKeyCode() == NativeKeyEvent.VC_SHIFT || var1.getKeyCode() == 3638) {
            isShiftPressed = true;
        }
    }

    public void nativeKeyReleased(NativeKeyEvent var1) {
        if (var1.getKeyCode() == NativeKeyEvent.VC_SHIFT || var1.getKeyCode() == 3638) {
            isShiftPressed = false;
        }
        if (var1.getKeyCode() == NativeKeyEvent.VC_A && isShiftPressed) {
            insertScreenshot();
        }
    }
}
