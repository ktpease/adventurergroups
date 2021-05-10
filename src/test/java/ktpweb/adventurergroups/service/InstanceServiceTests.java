package ktpweb.adventurergroups.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ktpweb.adventurergroups.exception.InstanceServiceException;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.model.OwnerDto;
import ktpweb.adventurergroups.repository.CharacterRepository;
import ktpweb.adventurergroups.repository.InstanceRepository;
import ktpweb.adventurergroups.repository.UserAccountRepository;

@SpringBootTest
@ActiveProfiles("test")
class InstanceServiceTests
{
    @Autowired
    private InstanceService instanceService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @BeforeEach
    void beforeEach()
    {
        userAccountRepository.deleteAll();
        instanceRepository.deleteAll();
        characterRepository.deleteAll();
    }

    @Test
    void createInstanceTests() throws Exception
    {
        InstanceServiceException exception;

        // Create a new owner for the test.
        OwnerDto testOwner = userAccountService.createOwner("testowner",
            "testpassword", "testemail", null);

        // Create a new instance.
        InstanceDto testInstance = instanceService.createInstance(testOwner,
            "test");

        assertNotNull(testInstance.getId(),
            "Cannot create Test Instance in database");
        assertEquals(testInstance.getOwnerId(), testOwner.getId(),
            "Test Instance does not have correct Owner");

        // Fail to create an instance with an invalid subdomain name.
        exception = assertThrows(InstanceServiceException.class,
            () -> instanceService.createInstance(testOwner, ""),
            "Should not create second Test Instance with no subdomain name.");
        assertEquals(exception.getCode(),
            InstanceServiceException.Codes.INVALID_SUBDOMAINNAME);

        // Fail to create an instance with matching subdomain name.
        exception = assertThrows(InstanceServiceException.class,
            () -> instanceService.createInstance(testOwner, "test"),
            "Should not create second Test Instance with same subdomain name.");
        assertEquals(exception.getCode(),
            InstanceServiceException.Codes.INSTANCE_ALREADY_EXISTS);

        // Fail to create an instance with no owner.
        exception = assertThrows(InstanceServiceException.class,
            () -> instanceService.createInstance(null, "test"),
            "Should not create second Test Instance with no owner.");
        assertEquals(exception.getCode(),
            InstanceServiceException.Codes.INVALID_OWNER_OBJECT);

        // Can create instance with same owner.
        assertDoesNotThrow(
            () -> instanceService.createInstance(testOwner, "test2"),
            "Cannot create separate instance for same owner");
    }

    @Test
    void activateAndDeactivateInstanceTests() throws Exception
    {
        InstanceServiceException exception;

        // Create a new owner and instancefor the test.
        OwnerDto testOwner = userAccountService.createOwner("testowner",
            "testpassword", "testemail", null);
        InstanceDto testInstance = instanceService.createInstance(testOwner,
            "test");

        assertFalse(testInstance.getActive(),
            "Instance should not start as active");

        // Activate instance
        testInstance = instanceService.activateInstance(testInstance);

        assertTrue(testInstance.getActive(), "Instance did not activate");

        // Activate an already active instance
        LocalDateTime oldActivateDate = testInstance.getLastActivateDate();
        testInstance = instanceService.activateInstance(testInstance);

        assertTrue(testInstance.getActive(), "Instance should still be active");
        assertNotEquals(testInstance.getLastActivateDate(), oldActivateDate,
            "Instance last activate date did not change");

        // Deactivate instance
        testInstance = instanceService.deactivateInstance(testInstance);

        assertFalse(testInstance.getActive(), "Instance did not deactivate");

        // Deactivate an already active instance
        LocalDateTime oldDeactivateDate = testInstance.getLastDeactivateDate();
        testInstance = instanceService.deactivateInstance(testInstance);

        assertFalse(testInstance.getActive(),
            "Instance should still be inactive");
        assertNotEquals(testInstance.getLastActivateDate(), oldDeactivateDate,
            "Instance last deactivate date did not change");

        // Fail to activate and deactivate instance that does not exist
        exception = assertThrows(InstanceServiceException.class,
            () -> instanceService.activateInstance(null),
            "Should not activate a null instance");
        assertEquals(exception.getCode(),
            InstanceServiceException.Codes.NULL_INSTANCE_OBJECT);

        exception = assertThrows(InstanceServiceException.class,
            () -> instanceService.deactivateInstance(null),
            "Should not deactivate a null instance");
        assertEquals(exception.getCode(),
            InstanceServiceException.Codes.NULL_INSTANCE_OBJECT);
    }
}