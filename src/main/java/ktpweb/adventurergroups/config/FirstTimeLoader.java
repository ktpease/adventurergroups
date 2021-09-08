package ktpweb.adventurergroups.config;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import ktpweb.adventurergroups.model.CharacterDto;
import ktpweb.adventurergroups.model.CharacterGroupDto;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.model.MaintainerDto;
import ktpweb.adventurergroups.model.OwnerDto;
import ktpweb.adventurergroups.service.AdminAccountService;
import ktpweb.adventurergroups.service.CharacterService;
import ktpweb.adventurergroups.service.InstanceService;
import ktpweb.adventurergroups.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FirstTimeLoader
    implements ApplicationListener<ContextRefreshedEvent>
{
    private boolean firstBoot = true;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AdminAccountService adminAccountService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private CharacterService characterService;

    @Value("${adventurergroups.useFirstBootProcess:false}")
    private Boolean useFirstBootProcess;

    @Value("${adventurergroups.createDemo:false}")
    private Boolean createDemo;

    @Value("${adventurergroups.demoPassword:demo}")
    private String demoPassword;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event)
    {
        if (!firstBoot || !useFirstBootProcess)
            return;

        log.warn("First time boot sequence activated");

        //
        // Create first-time superadmin to configure the administration section.
        // As the password itself is shown in plain-text on the logs, it's
        // advisable to change this password through the admin interface
        // IMMEDIATELY.
        //
        try
        {
            if (!adminAccountService.checkIfAdminExists())
            {
                String defaultPass = RandomStringUtils.randomAlphanumeric(10);
                adminAccountService.createAdmin("admin", defaultPass, null,
                    null);
                log.warn("----------------------------");
                log.warn("First-time superadmin created with password {}",
                    defaultPass);
                log.warn("Change the password ASAP.");
                log.warn("----------------------------");
            }
        }
        catch (Exception e)
        {
            log.error(
                "Cannot check existance of, or create, first-time admin! Shutting down");
            SpringApplication.exit(applicationContext, () -> 1);
        }

        //
        // Create a demo Owner and Instance.
        //
        if (createDemo)
        {
            try
            {
                // Create a sample owner.
                OwnerDto demoOwner = userAccountService.createOwner("demo",
                    demoPassword, null);

                // Create a sample instance.
                InstanceDto demoInstance = instanceService
                    .createInstance(demoOwner, "demo");

                log.info("Demo instance created with subdomain name: {}",
                    demoInstance.getSubdomainName());

                demoInstance.setDisplayName("Demo Instance");
                demoInstance.setDescription(
                    "Sample instance with some characters from the hit 1977 film Star Wars.");
                demoInstance = instanceService
                    .updateInstance(demoInstance.getId(), demoInstance);

                // Create sample character groups.
                CharacterGroupDto demoGroupHeroes = characterService
                    .createCharacterGroup(demoInstance);
                demoGroupHeroes.setName("Heroes");
                demoGroupHeroes
                    .setDescription("The heroes of the film Star Wars.");
                demoGroupHeroes.setColorPrimary(0x0000FF);
                demoGroupHeroes = characterService.updateCharacterGroup(
                    demoGroupHeroes.getId(), demoGroupHeroes);

                CharacterGroupDto demoGroupVillains = characterService
                    .createCharacterGroup(demoInstance);
                demoGroupVillains.setName("Villains");
                demoGroupVillains
                    .setDescription("The villains of the film Star Wars.");
                demoGroupVillains.setColorPrimary(0xFF0000);
                demoGroupVillains = characterService.updateCharacterGroup(
                    demoGroupVillains.getId(), demoGroupVillains);

                // Create sample characters.
                CharacterDto demoCharacterLuke = characterService
                    .createCharacter(demoInstance);
                demoCharacterLuke.setName("Luke Skywalker");
                demoCharacterLuke
                    .setDescription("A young farmer from Tatooine.");
                demoCharacterLuke.setCharacterGroup(demoGroupHeroes);
                demoCharacterLuke = characterService.updateCharacter(
                    demoCharacterLuke.getId(), demoCharacterLuke);

                CharacterDto demoCharacterVader = characterService
                    .createCharacter(demoInstance);
                demoCharacterVader.setName("Darth Vader");
                demoCharacterVader.setDescription("The main villain.");
                demoCharacterVader.setCharacterGroup(demoGroupVillains);
                demoCharacterVader = characterService.updateCharacter(
                    demoCharacterVader.getId(), demoCharacterVader);

                CharacterDto demoCharacterHan = characterService
                    .createCharacter(demoInstance);
                demoCharacterHan.setName("Han Solo");
                demoCharacterHan.setDescription("A smuggler.");
                demoCharacterHan.setCharacterGroup(demoGroupHeroes);
                demoCharacterHan = characterService.updateCharacter(
                    demoCharacterHan.getId(), demoCharacterHan);

                // Create a sample unregistered maintainer.
                MaintainerDto demoMaintainer = userAccountService
                    .createUnregisteredMaintainer(demoCharacterLuke);
                log.info(
                    "Unregistered demo maintainer created with invite token: {}",
                    demoMaintainer.getInviteToken());

            }
            catch (Exception e)
            {
                log.error("Issue creating demo Instance:", e);
            }
        }

        firstBoot = false;

        log.warn("First time boot sequence completed");
    }
}
