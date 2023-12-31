package org.acme.hibernate.orm;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestQuery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
@Path("/")
public class FruitResource {

    private static final Logger LOGGER = Logger.getLogger(FruitResource.class.getName());

    @Inject
    EntityManager entityManager;

    @GET
    @Path("fruits")
    public Fruit[] getDefault() {
        return get();
    }

    @GET
    @Path("{tenant}/fruits")
    public Fruit[] getTenant() {
        return get();
    }

    private Fruit[] get() {
        return entityManager.createNamedQuery("Fruits.findAll", Fruit.class)
                .getResultList().toArray(new Fruit[0]);
    }

    @GET
    @Path("fruits/{id}")
    public Fruit getSingleDefault(Integer id) {
        return findById(id);
    }

    @GET
    @Path("{tenant}/fruits/{id}")
    public Fruit getSingleTenant(Integer id) {
        return findById(id);
    }

    private Fruit findById(Integer id) {
        Fruit entity = entityManager.find(Fruit.class, id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        return entity;
    }

    @POST
    @Transactional
    @Path("fruits")
    public Response createDefault(Fruit fruit) {
        return create(fruit);
    }

    @POST
    @Transactional
    @Path("{tenant}/fruits")
    public Response createTenant(Fruit fruit) {
        return create(fruit);
    }

    private Response create(Fruit fruit) {
        if (fruit.getId() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }
        LOGGER.debugv("Create {0}", fruit.getName());
        entityManager.persist(fruit);
        return Response.ok(fruit).status(201).build();
    }

    @PUT
    @Path("fruits/{id}")
    @Transactional
    public Fruit updateDefault(Integer id, Fruit fruit) {
        return update(id, fruit);
    }

    @PUT
    @Path("{tenant}/fruits/{id}")
    @Transactional
    public Fruit updateTenant(Integer id, Fruit fruit) {
        return update(id, fruit);
    }

    public Fruit update(Integer id, Fruit fruit) {
        if (fruit.getName() == null) {
            throw new WebApplicationException("Fruit Name was not set on request.", 422);
        }

        Fruit entity = entityManager.find(Fruit.class, id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.setName(fruit.getName());

        LOGGER.debugv("Update #{0} {1}", fruit.getId(), fruit.getName());

        return entity;
    }

    @DELETE
    @Path("fruits/{id}")
    @Transactional
    public Response deleteDefault(Integer id) {
        return delete(id);
    }

    @DELETE
    @Path("{tenant}/fruits/{id}")
    @Transactional
    public Response deleteTenant(Integer id) {
        return delete(id);
    }

    public Response delete(Integer id) {
        Fruit fruit = entityManager.getReference(Fruit.class, id);
        if (fruit == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        LOGGER.debugv("Delete #{0} {1}", fruit.getId(), fruit.getName());
        entityManager.remove(fruit);
        return Response.status(204).build();
    }

    @GET
    @Path("fruitsFindBy")
    public Response findByDefault(@RestQuery String type, @RestQuery String value) {
        return findBy(type, value);
    }

    @GET
    @Path("{tenant}/fruitsFindBy")
    public Response findByTenant(@RestQuery String type, @RestQuery String value) {
        return findBy(type, value);
    }

    private Response findBy(String type, String value) {
        if (!"name".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Currently only 'fruitsFindBy?type=name' is supported");
        }
        List<Fruit> list = entityManager.createNamedQuery("Fruits.findByName", Fruit.class).setParameter("name", value).getResultList();
        if (list.size() == 0) {
            return Response.status(404).build();
        }
        Fruit fruit = list.get(0);
        return Response.status(200).entity(fruit).build();
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Response toResponse(Exception exception) {
            LOGGER.error("Failed to handle request", exception);

            int code = 500;
            if (exception instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }

            ObjectNode exceptionJson = objectMapper.createObjectNode();
            exceptionJson.put("exceptionType", exception.getClass().getName());
            exceptionJson.put("code", code);

            if (exception.getMessage() != null) {
                exceptionJson.put("error", exception.getMessage());
            }

            return Response.status(code)
                    .entity(exceptionJson)
                    .build();
        }

    }
}
