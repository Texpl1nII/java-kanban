package main;

import manager.FileBackedTaskManager;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.File;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = manager.createTask(new Task("Задача 1", "Описание задачи 1", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 27, 10, 0)));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание задачи 2", Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 27, 11, 0)));

        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Большой семейный праздник", null, null));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Купить продукты", Duration.ofMinutes(45), LocalDateTime.of(2025, 5, 27, 12, 0), epic1));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Пригласить гостей", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 27, 13, 0), epic1));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Украсить дом", Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 27, 14, 0), epic1));

        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Переезд", null, null));

        System.out.println("История после создания:");
        printAllTasks(manager);

        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        System.out.println("\nИстория после первого просмотра:");
        printAllTasks(manager);

        manager.getTask(task1.getId());
        manager.getSubtask(subtask2.getId());
        System.out.println("\nИстория после повторного просмотра (проверка дубликатов):");
        printAllTasks(manager);

        manager.deleteTask(task1.getId());
        System.out.println("\nИстория после удаления задачи 1:");
        printAllTasks(manager);

        manager.deleteEpic(epic1.getId());
        System.out.println("\nИстория после удаления эпика 1 (и его подзадач):");
        printAllTasks(manager);

        if (manager instanceof FileBackedTaskManager) {
            try {
                Field fileField = FileBackedTaskManager.class.getDeclaredField("file");
                fileField.setAccessible(true);
                File file = (File) fileField.get(manager);

                FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(file);

                System.out.println("\nTasks in new manager (loaded from file):");
                newManager.getAllTasks().forEach(System.out::println);
                System.out.println("\nEpics in new manager:");
                newManager.getAllEpics().forEach(epic -> {
                    System.out.println(epic);
                    newManager.getEpicSubtasks(epic.getId()).forEach(subtask -> System.out.println("--> " + subtask));
                });
                System.out.println("\nSubtasks in new manager:");
                newManager.getAllSubtasks().forEach(System.out::println);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Error accessing file field: " + e.getMessage());
            }
        }
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
            for (Subtask task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
        System.out.println("Приоритетные задачи:");
        for (Task task : manager.getPrioritizedTasks()) {
            System.out.println(task);
        }
    }
}