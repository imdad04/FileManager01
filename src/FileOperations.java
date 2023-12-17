import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;

public class FileOperations {

    private static List<OperationRecord> operationHistory = new ArrayList<>();

    public static void copyFile(String sourceFilePath, String destinationDirPath) {
        Path sourcePath = Paths.get(sourceFilePath);
        Path destinationPath = Paths.get(destinationDirPath);

        try {
            Path destinationFilePath = destinationPath.resolve(sourcePath.getFileName());
            Files.copy(sourcePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Файл успешно скопирован в: " + destinationFilePath);

            // Добавление записи в историю
            addOperationRecord(sourceFilePath, destinationDirPath);

        } catch (IOException e) {
            System.err.println("Ошибка при копировании файла: " + e.getMessage());
        }
    }

    public static void displayFilesSize(String directoryPath, JTextArea textArea) {
        Path path = Paths.get(directoryPath);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    textArea.append(file.getFileName() + ": " + attrs.size() + " байт\n");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Ошибка при чтении директории: " + e.getMessage());
        }
    }

    private static void addOperationRecord(String source, String destination) {
        operationHistory.add(new OperationRecord(source, destination, System.currentTimeMillis()));
    }

    public static List<OperationRecord> getOperationHistory() {
        return operationHistory;
    }

    public static class OperationRecord {
        private String source;
        private String destination;
        private long timestamp;

        public OperationRecord(String source, String destination, long timestamp) {
            this.source = source;
            this.destination = destination;
            this.timestamp = timestamp;
        }

        // Геттеры
        public String getSource() {
            return source;
        }

        public String getDestination() {
            return destination;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "Копирование из: " + source + " в: " + destination + " во время: " + timestamp;
        }
    }
}
