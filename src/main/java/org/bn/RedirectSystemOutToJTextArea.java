package org.bn;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class RedirectSystemOutToJTextArea {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Redirect System Output to JTextArea");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            frame.add(scrollPane, BorderLayout.CENTER);

            // Redirect System.out and System.err to JTextArea
            redirectSystemStreams(textArea);

            // Test output and error
            System.out.println("This is a standard output message.");
            System.err.println("This is an error message.");

            frame.setVisible(true);
        });
    }

    public static void redirectSystemStreams(JTextArea textArea) {
        // Create a custom OutputStream that writes to the JTextArea
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateTextArea(textArea, String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateTextArea(textArea, new String(b, off, len));
            }

            @Override
            public void write(byte[] b) {
                write(b, 0, b.length);
            }
        };

        // Redirect standard output stream and standard error stream to the custom output stream
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private static void updateTextArea(JTextArea textArea, String text) {
        // Ensure updates to JTextArea are done on the EDT
        SwingUtilities.invokeLater(() -> {
            textArea.append(text);
            // Auto-scroll to the latest log
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
