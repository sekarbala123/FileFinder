package org.bn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ArchiveFinderGUI {
    private static final String CONFIG_FILE = "config.properties";
    
    private final JTextField pathField;
    private final JTextField searchStringField;
    private final JTextField archiveTypesField;
    private final JTextArea logArea;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ArchiveFinderGUI::new);
    }

    public ArchiveFinderGUI() {
        JFrame frame = new JFrame("Archive Finder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 3, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Search"));

        JLabel pathLabel = new JLabel("Search Path:");
        pathField = new JTextField();
        JButton browseButton = new JButton("Browse");

        JLabel searchStringLabel = new JLabel("Search String:");
        searchStringField = new JTextField("*.txt");
        JLabel exampleSearchString = new JLabel("Example: *eclipse* ");

        JLabel archiveTypesLabel = new JLabel("Archive Types:");
        archiveTypesField = new JTextField("zip,jar,war,ear,rar");
        JLabel exampleArchiveString = new JLabel("Example: zip,ear ");

        JButton searchButton = new JButton("Search");

        inputPanel.add(pathLabel);
        inputPanel.add(pathField);
        inputPanel.add(browseButton);

        inputPanel.add(searchStringLabel);
        inputPanel.add(searchStringField);
        inputPanel.add(exampleSearchString);

        inputPanel.add(archiveTypesLabel);
        inputPanel.add(archiveTypesField);
        inputPanel.add(exampleArchiveString);

        inputPanel.add(searchButton);

        logArea = new JTextArea();
        RedirectSystemOutToJTextArea.redirectSystemStreams(logArea);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log"));

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(logScrollPane, BorderLayout.CENTER);

        browseButton.addActionListener(new BrowseButtonListener());
        searchButton.addActionListener(new SearchButtonListener());

        loadConfig();

        frame.setVisible(true);
    }

    private void loadConfig() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                Properties properties = new Properties();
                properties.load(Files.newInputStream(configFile.toPath()));
                pathField.setText(properties.getProperty("path", ""));
                searchStringField.setText(properties.getProperty("searchString", "*.txt"));
                archiveTypesField.setText(properties.getProperty("archiveTypes", "zip,jar,war,ear,rar"));
            }
        } catch (IOException e) {
            log("Error loading configuration: " + e.getMessage());
        }
    }

    private void saveConfig() {
        try {
            Properties properties = new Properties();
            properties.setProperty("path", pathField.getText());
            properties.setProperty("searchString", searchStringField.getText());
            properties.setProperty("archiveTypes", archiveTypesField.getText());
            properties.store(Files.newOutputStream(Paths.get(CONFIG_FILE)), null);
        } catch (IOException e) {
            log("Error saving configuration: " + e.getMessage());
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private class BrowseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    private class SearchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveConfig();
            String path = pathField.getText();
            String searchString = searchStringField.getText();
            String[] archiveTypes = archiveTypesField.getText().split(",");
            long startTimeMilliSecs = System.currentTimeMillis();
            log("Starting search in: " + path);
            log("Search String: " + searchString);
            log("Archive Types: " + String.join(", ", archiveTypes));
            RecursiveArchiveFinder.main(new String[]{path, searchString,String.join(",", archiveTypes)});
           long EndTimeMilliSecs = System.currentTimeMillis();
           log("Search Completed in :"+((EndTimeMilliSecs-startTimeMilliSecs)/1000.00)+" seconds");
        }
    }
}
