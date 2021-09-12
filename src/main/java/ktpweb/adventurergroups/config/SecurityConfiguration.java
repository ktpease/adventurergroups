package ktpweb.adventurergroups.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import ktpweb.adventurergroups.security.UserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration<S extends Session>
    extends WebSecurityConfigurerAdapter
{
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private FindByIndexNameSessionRepository<S> sessionRepository;

    @Bean
    public SpringSessionBackedSessionRegistry<S> sessionRegistry()
    {
        return new SpringSessionBackedSessionRegistry<>(this.sessionRepository);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        // Disable CSRF for now.
        http.csrf().disable();

        // H2 database console support.
        http.headers().frameOptions().sameOrigin();
        http.authorizeRequests().antMatchers("/h2-console/**").permitAll();

        // @formatter:off
        http.authorizeRequests() //
            .antMatchers(HttpMethod.GET, "/api/v1/**").permitAll() //
            .antMatchers("/api/auth").permitAll() //
            .anyRequest().authenticated(); //
        // @formatter:on

        http.formLogin().disable();
        http.httpBasic().disable();
        http.logout().disable();

        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .maximumSessions(1).sessionRegistry(sessionRegistry());
    }

    // Use custom authentication provider with bcrypt-encoded passwords and
    // users
    // with ID along with username.

    @Bean(name = "customAuthenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception
    {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        auth.authenticationProvider(authProvider());
    }

    @Bean
    public DaoAuthenticationProvider authProvider()
    {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}