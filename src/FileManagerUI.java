import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.Preferences;

public class FileManagerUI {
    private static final Preferences prefs = Preferences.userRoot().node(FileManagerUI.class.getName());
    private static boolean isDarkTheme;
    private static JProgressBar progressBar;
    private static FileCopyWorker fileCopyWorker;

    public static void main(String[] args) {
        // Загрузка настройки темы
        isDarkTheme = prefs.getBoolean("darkTheme", true);

        SwingUtilities.invokeLater(FileManagerUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Менеджер Файлов");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        frame.add(panel);

        placeComponents(panel, frame);
        setTheme(isDarkTheme);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel, JFrame frame) {
        JLabel sourceLabel = new JLabel("Исходный файл:");
        sourceLabel.setBounds(10, 20, 100, 25);
        panel.add(sourceLabel);

        JTextField sourceText = new JTextField(20);
        sourceText.setBounds(120, 20, 165, 25);
        panel.add(sourceText);
        sourceText.setText(prefs.get("sourcePath", ""));

        JButton sourceBrowseButton = new JButton("Обзор...");
        sourceBrowseButton.setBounds(290, 20, 90, 25);
        panel.add(sourceBrowseButton);

        JLabel destinationLabel = new JLabel("Папка назначения:");
        destinationLabel.setBounds(10, 60, 100, 25);
        panel.add(destinationLabel);

        JTextField destinationText = new JTextField(20);
        destinationText.setBounds(120, 60, 165, 25);
        panel.add(destinationText);
        destinationText.setText(prefs.get("destinationPath", ""));

        JButton destinationBrowseButton = new JButton("Обзор...");
        destinationBrowseButton.setBounds(290, 60, 90, 25);
        panel.add(destinationBrowseButton);

        JButton copyButton = new JButton("Копировать файл");
        copyButton.setBounds(10, 100, 150, 25);
        panel.add(copyButton);

        JLabel statusLabel = new JLabel("");
        statusLabel.setBounds(10, 140, 400, 25);
        panel.add(statusLabel);

        progressBar = new JProgressBar();
        progressBar.setBounds(10, 170, 470, 25);
        progressBar.setStringPainted(true);
        panel.add(progressBar);

        sourceBrowseButton.addActionListener(e -> chooseFile(panel, sourceText));
        destinationBrowseButton.addActionListener(e -> chooseDirectory(panel, destinationText));

        copyButton.addActionListener(e -> {
            String sourceFile = sourceText.getText();
            String destinationDir = destinationText.getText();

            prefs.put("sourcePath", sourceFile);
            prefs.put("destinationPath", destinationDir);

            if (fileCopyWorker != null && !fileCopyWorker.isDone()) {
                fileCopyWorker.cancel(true);
            }

            fileCopyWorker = new FileCopyWorker(sourceFile, destinationDir, statusLabel, progressBar);
            fileCopyWorker.execute();
        });

        // Кнопка для открытия диалога настроек
        JButton settingsButton = new JButton("Настройки");
        settingsButton.setBounds(350, 200, 120, 25);
        panel.add(settingsButton);

        settingsButton.addActionListener(e -> openSettingsDialog(frame));
    }

    private static void openSettingsDialog(JFrame frame) {
        JDialog settingsDialog = new JDialog(frame, "Настройки", true);
        settingsDialog.setSize(300, 200);
        settingsDialog.setLayout(new FlowLayout());

        JCheckBox darkThemeCheckBox = new JCheckBox("Темная тема", isDarkTheme);
        settingsDialog.add(darkThemeCheckBox);

        JButton saveButton = new JButton("Сохранить");
        settingsDialog.add(saveButton);

        saveButton.addActionListener(e -> {
            isDarkTheme = darkThemeCheckBox.isSelected();
            prefs.putBoolean("darkTheme", isDarkTheme);
            setTheme(isDarkTheme);
            settingsDialog.dispose();
            SwingUtilities.updateComponentTreeUI(frame);
        });

        settingsDialog.setVisible(true);
    }

    private static void setTheme(boolean darkTheme) {
        if (darkTheme) {
            setDarkTheme();
        } else {
            setLightTheme();
        }
    }

    private static void setDarkTheme() {
        // Установка цветов для темной темы
        UIManager.put("control", new ColorUIResource(128, 128, 128));
        UIManager.put("nimbusBase", new ColorUIResource(18, 30, 49));
        UIManager.put("nimbusLightBackground", new ColorUIResource(18, 30, 49));
        UIManager.put("text", new ColorUIResource(230, 230, 230));
    }

    private static void setLightTheme() {
        // Установка цветов для светлой темы
        UIManager.put("control", new ColorUIResource(214, 217, 223));
        UIManager.put("nimbusBase", new ColorUIResource(115, 164, 209));
        UIManager.put("nimbusLightBackground", new ColorUIResource(214, 217, 223));
        UIManager.put("text", new ColorUIResource(0, 0, 0));
    }

    private static void chooseFile(JPanel panel, JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(panel);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textField.setText(selectedFile.getAbsolutePath());
        }
    }

    private static void chooseDirectory(JPanel panel, JTextField textField) {
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = directoryChooser.showOpenDialog(panel);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = directoryChooser.getSelectedFile();
            textField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private static class FileCopyWorker extends SwingWorker<Void, Integer> {
        private final String sourceFile;
        private final String destinationDir;
        private final JLabel statusLabel;
        private final JProgressBar progressBar;

        public FileCopyWorker(String sourceFile, String destinationDir, JLabel statusLabel, JProgressBar progressBar) {
            this.sourceFile = sourceFile;
            this.destinationDir = destinationDir;
            this.statusLabel = statusLabel;
            this.progressBar = progressBar;
        }

        @Override
        protected Void doInBackground() throws Exception {
            FileInputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                File source = new File(sourceFile);
                File destination = new File(destinationDir, source.getName());

                long fileSize = source.length();
                long bytesCopied = 0;

                inputStream = new FileInputStream(source);
                outputStream = new FileOutputStream(destination);

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1 && !isCancelled()) {
                    outputStream.write(buffer, 0, bytesRead);
                    bytesCopied += bytesRead;

                    int percentComplete = (int) ((bytesCopied * 100) / fileSize);
                    publish(percentComplete);
                }

                if (!isCancelled()) {
                    statusLabel.setText("Файл успешно скопирован!");
                } else {
                    statusLabel.setText("Операция копирования прервана.");
                    destination.delete(); // Удаляем файл, если операция была прервана
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                statusLabel.setText("Ошибка: " + ex.getMessage());
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            return null;
        }

        @Override
        protected void process(java.util.List<Integer> chunks) {
            int percentComplete = chunks.get(chunks.size() - 1);
            progressBar.setValue(percentComplete);
        }
    }
}
