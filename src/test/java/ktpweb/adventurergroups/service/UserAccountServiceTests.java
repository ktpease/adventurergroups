package ktpweb.adventurergroups.service;

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
		AdminDto testAdmin = userAccountService.createAdmin("testusername",
			"testpassword", "testemail", null);

		assertNotNull(testAdmin.getId(),
			"Cannot create Test Admin in database");
		assertEquals(testAdmin.getUsername(), "testusername",
			"Test Admin does not have correct username");

		// Fail to create a new admin with the same username.

		UserAccountServiceException exception = assertThrows(
			UserAccountServiceException.class,
			() -> userAccountService.createAdmin("testusername", "testpassword",
				null, null),
			"Should not create Admin with same username as Test Admin");

		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_EXISTS,
			"Should not create Admin with same username as Test Admin");

		// Fail to create a new admin with the same email.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createAdmin("testusername_2",
				"testpassword", "testemail", null),
			"Should not create Admin with same non-null email as Test Admin");

		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.ACCOUNT_EXISTS,
			"Should not create Admin with same non-null email as Test Admin");

		// Fail to create an admin with an invalid username.
		exception = assertThrows(
			UserAccountServiceException.class, () -> userAccountService
				.createAdmin("", "testpassword", null, null),
			"Should not create second Test Admin with no username.");

		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.BAD_USERNAME,
			"Should not create second Test Admin with no username.");

		// Fail to create an admin with an invalid password.
		exception = assertThrows(UserAccountServiceException.class,
			() -> userAccountService.createAdmin("testusername_2", "", null,
				null),
			"Should not create second Test Admin with bad password.");

		assertEquals(exception.getCode(),
			UserAccountServiceException.Codes.BAD_PASSWORD,
			"Should not create second Test Admin with bad password.");
	}
}
