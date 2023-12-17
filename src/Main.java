import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите путь к исходному файлу для копирования:");
        String sourceFilePath = scanner.nextLine();

        System.out.println("Введите путь к директории назначения:");
        String destinationDirPath = scanner.nextLine();

        // Копирование файла
        copyFile(Paths.get(sourceFilePath), Paths.get(destinationDirPath));

        // Вывод размера файлов в директории
        displayFilesSize(Paths.get(destinationDirPath));
    }

    private static void copyFile(Path sourcePath, Path destinationPath) {
        try {
            Path destinationFilePath = destinationPath.resolve(sourcePath.getFileName());
            Files.copy(sourcePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Файл успешно скопирован в: " + destinationFilePath);
        } catch (IOException e) {
            System.err.println("Ошибка при копировании файла: " + e.getMessage());
        }
    }

    private static void displayFilesSize(Path directoryPath) {
        try {
            Files.walk(directoryPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            long size = Files.size(file);
                            System.out.println(file.getFileName() + ": " + size + " байт");
                        } catch (IOException e) {
                            System.err.println("Ошибка при получении размера файла: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Ошибка при чтении директории: " + e.getMessage());
        }
    }
}
