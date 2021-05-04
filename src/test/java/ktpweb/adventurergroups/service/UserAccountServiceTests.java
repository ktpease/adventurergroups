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
import ktpweb.adventurergroups.model.AdminDto;
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
	void createAdminTest() throws Exception
	{
		// Create a new admin.
		AdminDto testAdmin = userAccountService.createAdmin("testadmin",
			"testpassword", "testemail", null);

		assertNotNull(testAdmin.getId(),
			"Cannot create Test Admin in database");
		assertEquals(testAdmin.getUsername(), "testadmin",
			"Test Admin does not have correct username");

		assertDoesNotThrow(() -> userAccountService.createAdmin("testadmin2",
			"testpassword", "testemail2", null),
			"Cannot create separate admin");

		// Fail to create an admin with an invalid username.
		UserAccountServiceException exception = assertThrows(
			UserAccountServiceException.class, () -> userAccountService
				.createAdmin("", "testpassword", null, null),
			"Should not create second Test Admin with no username.");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.INVALID_USERNAME);

		// Fail to create an admin with an invalid password.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createAdmin("testadmin3", "", null, null),
			"Should not create second Test Admin with bad password.");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.INVALID_PASSWORD);

		// Fail to create a new admin with the same username as another admin.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createAdmin("testadmin", "testpassword",
				null, null),
			"Should not create Admin with same username as Test Admin");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_EXISTS);

		// Fail to create a new admin with the same email as another admin.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createAdmin("testadmin3", "testpassword",
				"testemail", null),
			"Should not create Admin with same non-null email as Test Admin");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_EXISTS);

		userAccountService.createOwner("testowner", "testpassword",
			"testemail3", null);

		// Fail to create a new admin with the same username as another owner.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createAdmin("testowner", "testpassword",
				null, null),
			"Should not create Admin with same username as Test Owner");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_EXISTS);

		// Fail to create a new admin with the same email as another owner.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createAdmin("testadmin3", "testpassword",
				"testemail3", null),
			"Should not create Admin with same non-null email as Test Owner");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_EXISTS);
	}

	@Test
	void createOwnerTest() throws Exception
	{
		// Create a new owner.
		OwnerDto testOwner = userAccountService.createOwner("testowner",
			"testpassword", "testemail", null);

		assertNotNull(testOwner.getId(),
			"Cannot create Test Owner in database");
		assertEquals(testOwner.getUsername(), "testowner",
			"Test Owner does not have correct username");

		assertDoesNotThrow(() -> userAccountService.createOwner("testowner2",
			"testpassword", "testemail2", null),
			"Cannot create separate admin");

		// Fail to create an owner with an invalid username.
		UserAccountServiceException exception = assertThrows(
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
			UserAccountServiceException.Codes.ACCOUNT_EXISTS);

		// Fail to create a new owner with the same email as another owner.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createOwner("testowner2", "testpassword",
				"testemail", null),
			"Should not create Owner with same non-null email as Test Owner");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_EXISTS);

		userAccountService.createAdmin("testadmin", "testpassword",
			"testemail3", null);

		// Fail to create a new owner with the same username as another
		// admin.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createOwner("testadmin", "testpassword",
				null, null),
			"Should not create Owner with same username as Test Owner");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_EXISTS);

		// Fail to create a new owner with the same email as another admin.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createOwner("testowner2", "testpassword",
				"testemail3", null),
			"Should not create Owner with same non-null email as Test Owner");
		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_EXISTS);
	}
}
