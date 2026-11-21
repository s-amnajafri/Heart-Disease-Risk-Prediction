package com.heartrisk.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.heartrisk.TestRecords;
import org.junit.jupiter.api.Test;

/** Inheritance (User), composition (Patient-HealthRecord) and aggregation (Doctor-Patient). */
class UserHierarchyTest {

    private static Patient patient(String id) {
        return new Patient(id, "Test Patient", "patient@example.com", TestRecords.validBuilder());
    }

    @Test
    void subclassesAreUsersWithDifferentRoles() {
        User asUser = patient("P-0001");
        User doctor = new Doctor("D-001", "Dr Test", "doctor@example.com", "Cardiology");
        assertEquals("PATIENT", asUser.getRole());
        assertEquals("DOCTOR", doctor.getRole());
    }

    @Test
    void userDataIsValidated() {
        assertThrows(IllegalArgumentException.class,
                () -> new Patient("P-1", " ", "a@b.com", TestRecords.validBuilder()));
        assertThrows(IllegalArgumentException.class,
                () -> new Patient("P-1", "Ann", "not-an-email", TestRecords.validBuilder()));
    }

    @Test
    void patientBuildsAndOwnsItsHealthRecord() {
        Patient p = patient("P-0001");
        assertEquals(50, p.getHealthRecord().getAge());
        assertThrows(IllegalArgumentException.class,
                () -> new Patient("P-2", "Ann", "a@b.com", TestRecords.validBuilder().age(0)));
    }

    @Test
    void doctorAggregatesPatientsWithoutOwningThem() {
        Doctor doctor = new Doctor("D-001", "Dr Test", "doctor@example.com", "Cardiology");
        Patient p = patient("P-0001");
        doctor.addPatient(p);
        doctor.addPatient(p);
        assertEquals(1, doctor.getPatients().size());
        assertTrue(doctor.removePatient(p));
        assertFalse(doctor.getPatients().contains(p));
        assertEquals(50, p.getHealthRecord().getAge());
    }

    @Test
    void doctorListIsReadOnly() {
        Doctor doctor = new Doctor("D-001", "Dr Test", "doctor@example.com", "Cardiology");
        assertThrows(UnsupportedOperationException.class, () -> doctor.getPatients().add(patient("P-0001")));
    }
}
