import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    @Test
    void getDefaultShouldReturnInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "TaskManager should not be null");
        assertInstanceOf(InMemoryTaskManager.class, manager, "Should return InMemoryTaskManager");
    }

    @Test
    void getDefaultHistoryShouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "HistoryManager should not be null");
        assertInstanceOf(InMemoryHistoryManager.class, historyManager, "Should return InMemoryHistoryManager");
    }
}