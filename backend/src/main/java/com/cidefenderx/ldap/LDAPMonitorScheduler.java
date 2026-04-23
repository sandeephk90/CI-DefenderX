package com.cidefenderx.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "spring.ldap.enabled", havingValue = "true")
public class LDAPMonitorScheduler {

    private static final Logger log = LoggerFactory.getLogger(LDAPMonitorScheduler.class);

    private final LdapTemplate ldapTemplate;

    public LDAPMonitorScheduler(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Scheduled(fixedDelayString = "${cidefenderx.ldap.poll-interval-ms:60000}")
    public void pollActiveDirectory() {
        log.debug("Polling Active Directory for threats...");
        try {
            checkPrivilegedGroupChanges();
        } catch (Exception e) {
            log.warn("LDAP poll error: {}", e.getMessage());
        }
    }

    private void checkPrivilegedGroupChanges() {
        List<String> privilegedUsers = ldapTemplate.search(
                LdapQueryBuilder.query()
                        .where("objectClass").is("user")
                        .and("memberOf").is("CN=Domain Admins,CN=Users,DC=corp,DC=local"),
                (AttributesMapper<String>) attrs -> (String) attrs.get("sAMAccountName").get()
        );

        log.debug("Domain Admins count: {}", privilegedUsers.size());

        if (privilegedUsers.size() > 5) {
            log.warn("Unusual number of Domain Admin accounts detected: {}", privilegedUsers.size());
        }
    }
}
