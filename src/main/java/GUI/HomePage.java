package GUI;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HomePage extends JPanel implements ActionListener {
    static private final String START_RECORDING = "Start Recording";
    static private final String STOP_RECORDING = "Stop Recording";
    static private final String FILENAME_FIELD_LABEL = "File Name: ";
    private JButton chooseDirectoryButton, recordingButton;
    private JTextField directoryTextField, fileNameTextField;
    private JTextArea log;
    private JFileChooser fc;
    private JFrame frame;
    private String title;
    private TrackerPage trackerPage;
    private boolean isRecording;

    public HomePage(String title) {
        this();
        this.title = title;
    }

    private HomePage() {
        super(new BorderLayout());

        // Default Title
        this.title = "Screenshot Recorder Tool";
        this.trackerPage = createNewTrackerInstance();

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5,20);
        log.setFont(FontGenerator.getDefaultFont(Constants.TRACKER_LOG_FONT_SIZE));
        log.setMargin(new Insets(5,5,5,5));
        ((DefaultCaret)log.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        log.setEditable(false);
        log.append("Today's font is " + FontGenerator.getRandomFont().getName() + "\n");
        JScrollPane logScrollPane = new JScrollPane(log);

        JPanel directoryPanel = new JPanel();
        //Create a file chooser
        fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        directoryTextField = new JTextField();
        directoryTextField.setFont(FontGenerator.getRandomFont());
        directoryTextField.setText(System.getProperty("user.dir"));
        directoryTextField.setColumns(25);
        directoryTextField.setEditable(false);

        chooseDirectoryButton = new JButton("Choose directory...");
        chooseDirectoryButton.addActionListener(this);

        recordingButton = new JButton(START_RECORDING);
        recordingButton.addActionListener(this);

        directoryPanel.add(directoryTextField);
        directoryPanel.add(chooseDirectoryButton);
        directoryPanel.add(recordingButton);
        directoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));


        JPanel fileNamePanel = new JPanel();
        JLabel fileNameFieldLabel = new JLabel();
        fileNameFieldLabel.setFont(FontGenerator.getRandomFont());
        fileNameFieldLabel.setText(FILENAME_FIELD_LABEL);
        fileNamePanel.add(fileNameFieldLabel);

        fileNameTextField = new JTextField();
        fileNameTextField.setFont(FontGenerator.getRandomFont());
        fileNameTextField.setText(Constants.DEFAULT_FILE_NAME);
        fileNameTextField.setColumns(20);
        fileNameTextField.setEditable(true);
        fileNamePanel.add(fileNameTextField);
        fileNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));


        //Add the buttons and the log to this panel.
        add(fileNamePanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
        add(directoryPanel, BorderLayout.PAGE_END);
    }

    private TrackerPage createNewTrackerInstance() {
        return new TrackerPage("Recording In Progress", this);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle choose directory button action.
        if (e.getSource() == chooseDirectoryButton) {
            int returnVal = fc.showOpenDialog(HomePage.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File folder = fc.getSelectedFile();
                //This is where a real application would open the file.
                directoryTextField.setText(folder.getAbsolutePath());
            }
        //Handle start recording button action.
        } else if (e.getSource() == recordingButton) {
            if (!trackerPage.isPageVisible()) {
                showTrackerPage();
            } else {
                hideTrackerPage();
            }
        }
    }

    String getOutputFolderPath() {
        return StringUtils.isNotBlank(directoryTextField.getText()) ? directoryTextField.getText() : System.getProperty("user.dir");
    }

    String getOutputFileName() {
        return StringUtils.isNotBlank(fileNameTextField.getText()) ? fileNameTextField.getText() : Constants.DEFAULT_FILE_NAME;
    }

    private void showTrackerPage() {
        trackerPage.showFrame();
        frame.setVisible(false);
        isRecording = true;
        recordingButton.setText(STOP_RECORDING);
    }

    void hideTrackerPage() {
        trackerPage.hideFrame();
        this.trackerPage = createNewTrackerInstance();
        frame.setVisible(true);
        isRecording = false;
        recordingButton.setText(START_RECORDING);
    }


    private void createAndShowGUI() {
        //Create and set up the window.
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(this);

        //Display the window.
        frame.setPreferredSize(new Dimension(800, 300));
        frame.setLocation(20, 20);
        frame.pack();
        frame.setVisible(true);
    }

    void insertLog(String message) {
        log.append("[" + getFormattedLogTime() + "]\n" + message + "\n");
    }

    private String getFormattedLogTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(Calendar.getInstance().getTime());
    }

    public void createFrame() {
        this.createAndShowGUI();
    }

    public void showFrame() {
        frame.setVisible(true);
    }

    public void hideFrame() {
        frame.setVisible(false);
    }

    public boolean isPageVisible() {
        return frame != null && frame.isVisible();
    }

}
