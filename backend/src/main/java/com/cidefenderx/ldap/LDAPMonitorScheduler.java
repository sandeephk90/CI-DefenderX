package com.cidefenderx.ldap;

import com.cidefenderx.model.Endpoint;
import com.cidefenderx.model.Threat;
import com.cidefenderx.repository.EndpointRepository;
import com.cidefenderx.repository.ThreatRepository;
import com.cidefenderx.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.ldap.enabled", havingValue = "true")
public class LDAPMonitorScheduler {

    private final LdapTemplate ldapTemplate;
    private final ThreatRepository threatRepository;
    private final EndpointRepository endpointRepository;
    private final AlertService alertService;

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
                ctx -> (String) ctx.getAttributes().get("sAMAccountName").get()
        );

        log.debug("Domain Admins count: {}", privilegedUsers.size());

        // In production, compare against a snapshot to detect additions/removals
        // This scaffold logs the current state
        if (privilegedUsers.size() > 5) {
            log.warn("Unusual number of Domain Admin accounts detected: {}", privilegedUsers.size());
        }
    }
}
