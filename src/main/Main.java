public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Create tasks
        Task task1 = manager.createTask(new Task("Задача 1", "Описание задачи 1"));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание задачи 2"));

        // Create epics and subtasks
        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Большой семейный праздник"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Купить продукты", epic1));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Пригласить гостей", epic1));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Украсить дом", epic1));

        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Переезд"));

        // Initial history
        System.out.println("История после создания:");
        printAllTasks(manager);

        // Request tasks in different orders
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        System.out.println("\nИстория после первого просмотра:");
        printAllTasks(manager);

        // Request some tasks again to check duplicates
        manager.getTask(task1.getId());
        manager.getSubtask(subtask2.getId());
        System.out.println("\nИстория после повторного просмотра (проверка дубликатов):");
        printAllTasks(manager);

        // Delete a task and check history
        manager.deleteTask(task1.getId());
        System.out.println("\nИстория после удаления задачи 1:");
        printAllTasks(manager);

        // Delete epic1 and check history
        manager.deleteEpic(epic1.getId());
        System.out.println("\nИстория после удаления эпика 1 (и его подзадач):");
        printAllTasks(manager);
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
    }
}