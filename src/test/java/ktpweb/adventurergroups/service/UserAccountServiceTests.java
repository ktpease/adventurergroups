package ktpweb.adventurergroups.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ktpweb.adventurergroups.exception.UserAccountServiceException;
import ktpweb.adventurergroups.model.CharacterDto;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.model.MaintainerDto;
import ktpweb.adventurergroups.model.OwnerDto;
import ktpweb.adventurergroups.repository.CharacterRepository;
import ktpweb.adventurergroups.repository.InstanceRepository;
import ktpweb.adventurergroups.repository.UserAccountRepository;

@SpringBootTest
@ActiveProfiles("test")
class UserAccountServiceTests
{
	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private InstanceService instanceService;

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
	void createOwnerTests() throws Exception
	{
		UserAccountServiceException exception;

		// Create a new owner.
		OwnerDto testOwner = userAccountService.createOwner("testowner",
			"testpassword", "testemail", null);

		assertNotNull(testOwner.getId(),
			"Cannot create Test Owner in database");
		assertEquals(testOwner.getUsername(), "testowner",
			"Test Owner does not have correct username");

		assertDoesNotThrow(() -> userAccountService.createOwner("testowner2",
			"testpassword", "testemail2", null),
			"Cannot create separate owner");

		// Fail to create an owner with an invalid username.
		exception = assertThrows(
			UserAccountServiceException.class, () -> userAccountService
				.createOwner("", "testpassword", null, null),
			"Should not create second Test Owner with no username.");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.INVALID_USERNAME);

		// Fail to create an owner with an invalid password.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createOwner("testowner2", "", null, null),
			"Should not create second Test Owner with bad password.");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.INVALID_PASSWORD);

		// Fail to create a new owner with the same username as another owner.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createOwner("testowner", "testpassword",
				null, null),
			"Should not create Owner with same username as Test Owner");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);

		// Fail to create a new owner with the same email as another owner.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createOwner("testowner2", "testpassword",
				"testemail", null),
			"Should not create Owner with same non-null email as Test Owner");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
	}

	@Test
	void createUnregisteredMaintainerTests() throws Exception
	{
		UserAccountServiceException exception;

		// Create a new owner, instance, and character.
		OwnerDto testOwner = userAccountService.createOwner("testowner",
			"testpassword", "testemail", null);

		InstanceDto testInstance = instanceService.createInstance(testOwner,
			"test");

		// TODO: Create maintainer for Character.

		// Create maintainer for Instance.
		MaintainerDto testMaintainerForInstance = userAccountService
			.createUnregisteredMaintainer(testInstance);

		assertNotNull(testMaintainerForInstance.getId(),
			"Cannot create Test Maintainer (instance) in database");
		assertEquals(testMaintainerForInstance.getParentInstanceId(),
			testInstance.getId(),
			"Test Maintainer (instance) does not have correct instance");

		assertDoesNotThrow(
			() -> userAccountService.createUnregisteredMaintainer(testInstance),
			"Cannot create separate maintainer (instance)");

		// Fail to create a maintainer with an invalid instance.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService
				.createUnregisteredMaintainer((InstanceDto) null),
			"Should not create Maintainer (instance) with no instance");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT);

		// Fail to create a maintainer with an invalid character.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService
				.createUnregisteredMaintainer((CharacterDto) null),
			"Should not create Maintainer (character) with no character");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.INVALID_CHARACTER_OBJECT);
	}

	@Test
	void registerMaintainerTests() throws Exception
	{
		UserAccountServiceException exception;

		// Create a new owner, instance.
		OwnerDto testOwner = userAccountService.createOwner("testowner",
			"testpassword", "testowneremail", null);

		InstanceDto testInstance = instanceService.createInstance(testOwner,
			"test");

		// Create and Register maintainer
		MaintainerDto testunregisteredMaintainer = userAccountService
			.createUnregisteredMaintainer(testInstance);

		MaintainerDto testRegisteredMaintainer = userAccountService
			.registerMaintainer(testunregisteredMaintainer, "testmaintainer",
				"testpassword", "testemail", null);

		assertNotNull(testRegisteredMaintainer.getId(),
			"Cannot register maintainer in database");
		assertEquals(testRegisteredMaintainer.getParentInstanceId(),
			testInstance.getId(),
			"Test Maintainer does not have correct instance");

		// Fail to re-register maintainer
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.registerMaintainer(
				testRegisteredMaintainer, "testmaintainer2", "testpassword",
				null, null),
			"Should not register Maintainer that is already registered");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.INVALID_ROLE);

		// Create second unregistered maintainer for fail conditions.
		MaintainerDto testunregisteredMaintainer2 = userAccountService
			.createUnregisteredMaintainer(testInstance);

		// Fail to register maintainer with an invalid username.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.registerMaintainer(
				testunregisteredMaintainer2, "", "testpassword", null, null),
			"Should not register Maintainer with no username.");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.INVALID_USERNAME);

		// Fail to register maintainer with an invalid password.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.registerMaintainer(
				testunregisteredMaintainer2, "testmaintainer2", "", null, null),
			"Should not register Maintainer with bad password.");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.INVALID_PASSWORD);

		// Fail to register maintainer with the same username as another
		// maintainer.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.registerMaintainer(
				testunregisteredMaintainer2, "testmaintainer", "testpassword",
				null, null),
			"Should not register Maintainer with same username as Test Maintainer");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);

		// Fail to register maintainer with the same email as another
		// maintainer.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.registerMaintainer(
				testunregisteredMaintainer2, "testmaintainer2", "testpassword",
				"testemail", null),
			"Should not register Maintainer with same non-null email as Test Maintainer");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);

		// Fail to register maintainer with the same username as instance
		// owner.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.registerMaintainer(
				testunregisteredMaintainer2, "testowner", "testpassword", null,
				null),
			"Should not register Maintainer with same username as Test Owner");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);

		// Fail to register maintainer with the same email as instance owner.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.registerMaintainer(
				testunregisteredMaintainer2, "testmaintainer2", "testpassword",
				"testowneremail", null),
			"Should not register Maintainer with same non-null email as Test Owner");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
	}
}
