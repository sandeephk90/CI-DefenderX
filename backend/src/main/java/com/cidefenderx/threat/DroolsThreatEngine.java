package com.cidefenderx.threat;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DroolsThreatEngine {

    private KieContainer kieContainer;

    @PostConstruct
    public void init() {
        try {
            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] rules = resolver.getResources("classpath:rules/*.drl");

            for (Resource rule : rules) {
                String path = "src/main/resources/rules/" + rule.getFilename();
                kfs.write(path, ks.getResources().newInputStreamResource(rule.getInputStream()));
                log.info("Loaded Drools rule: {}", rule.getFilename());
            }

            KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                throw new RuntimeException("Drools build errors: " + kieBuilder.getResults().toString());
            }

            kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
            log.info("Drools threat engine initialized with {} rule files", rules.length);

        } catch (IOException e) {
            log.error("Failed to load Drools rules", e);
            throw new RuntimeException("Drools init failed", e);
        }
    }

    public List<ThreatResult> evaluate(ThreatFact fact) {
        KieSession session = kieContainer.newKieSession();
        List<ThreatResult> results = new ArrayList<>();

        try {
            session.setGlobal("results", results);
            session.insert(fact);
            session.fireAllRules();

            // Collect ThreatResult objects inserted by rules
            session.getObjects(obj -> obj instanceof ThreatResult)
                   .forEach(obj -> results.add((ThreatResult) obj));

        } finally {
            session.dispose();
        }

        return results;
    }

    public List<ThreatResult> evaluateBatch(List<ThreatFact> facts) {
        KieSession session = kieContainer.newKieSession();
        List<ThreatResult> results = new ArrayList<>();

        try {
            facts.forEach(session::insert);
            session.fireAllRules();
            session.getObjects(obj -> obj instanceof ThreatResult)
                   .forEach(obj -> results.add((ThreatResult) obj));
        } finally {
            session.dispose();
        }

        return results;
    }
}
