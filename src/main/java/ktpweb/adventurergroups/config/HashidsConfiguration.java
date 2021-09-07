package ktpweb.adventurergroups.config;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HashidsConfiguration {
    @Value("${adventurergroups.invitetoken.salt:}")
    private String hashidsSalt;

    @Bean
    public Hashids inviteTokenHashids()
    {
        return new Hashids(hashidsSalt, 8);
    }
}
