package test;

import model.Epic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic", "Description", null, null);
        epic1.setId(1);
        Epic epic2 = new Epic("Epic", "Description", null, null);
        epic2.setId(1);

        assertEquals(epic1, epic2, "Epics with the same ID should be equal");
        assertEquals(epic1.hashCode(), epic2.hashCode(), "Hash codes should match for equal epics");
    }
}