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

import ktpweb.adventurergroups.exception.AdminAccountServiceException;
import ktpweb.adventurergroups.model.AdminAccountDto;
import ktpweb.adventurergroups.repository.AdminAccountRepository;

@SpringBootTest
@ActiveProfiles("test")
class AdminAccountServiceTests
{
	@Autowired
	private AdminAccountService adminAccountService;

	@Autowired
	private AdminAccountRepository adminAccountRepository;

	@BeforeEach
	void beforeEach()
	{
		adminAccountRepository.deleteAll();
	}

	@Test
	void createAdminTests() throws Exception
	{
		AdminAccountServiceException exception;

		// Create a new admin.
		AdminAccountDto testAdmin = adminAccountService.createAdmin("testadmin",
			"testpassword", "testemail", null);

		assertNotNull(testAdmin.getId(),
			"Cannot create Test Admin in database");
		assertEquals(testAdmin.getUsername(), "testadmin",
			"Test Admin does not have correct username");

		assertDoesNotThrow(() -> adminAccountService.createAdmin("testadmin2",
			"testpassword", "testemail2", null),
			"Cannot create separate admin");

		// Fail to create an admin with an invalid username.
		exception = assertThrows(
			AdminAccountServiceException.class, () -> adminAccountService
				.createAdmin("", "testpassword", null, null),
			"Should not create second Test Admin with no username.");
		assertEquals(exception.getCode(),
			AdminAccountServiceException.Codes.INVALID_USERNAME);

		// Fail to create an admin with an invalid password.
		exception = assertThrows(AdminAccountServiceException.class,
			() -> adminAccountService.createAdmin("testadmin3", "", null, null),
			"Should not create second Test Admin with bad password.");
		assertEquals(exception.getCode(),
			AdminAccountServiceException.Codes.INVALID_PASSWORD);

		// Fail to create a new admin with the same username as another admin.
		exception = assertThrows(AdminAccountServiceException.class,
			() -> adminAccountService.createAdmin("testadmin", "testpassword",
				null, null),
			"Should not create Admin with same username as Test Admin");
		assertEquals(exception.getCode(),
			AdminAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);

		// Fail to create a new admin with the same email as another admin.
		exception = assertThrows(AdminAccountServiceException.class,
			() -> adminAccountService.createAdmin("testadmin3", "testpassword",
				"testemail", null),
			"Should not create Admin with same non-null email as Test Admin");
		assertEquals(exception.getCode(),
			AdminAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
	}
}
