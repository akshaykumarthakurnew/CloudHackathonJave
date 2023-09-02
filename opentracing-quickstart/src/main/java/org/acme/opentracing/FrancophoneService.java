package org.acme.opentracing;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.opentracing.Traced;

@Traced
@ApplicationScoped
public class FrancophoneService {

    public String bonjour() {
        return "bonjour";
    }
}
