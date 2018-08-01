package com.webservice;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.entity.Coffee;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qualifier.Resource;

@Path("/coffeecab")
public class CoffeeService {
	
	/**
	* GET - http://localhost:8080/coffee-cab/rest/coffeecab/coffee/
	* POST - http://localhost:8080/coffee-cab/rest/coffeecab/coffee/
	* PUT - http://localhost:8080/coffee-cab/rest/coffeecab/coffee/{name} 
	* like http://localhost:8080/coffee-cab/rest/coffeecab/coffee/arabica
	* DELETE - the same like PUT
	* http://localhost:8080/coffee-cab/rest/coffeecab/coffee/{name}
	*/

	@Inject
	private EntityManager em;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("status")
	public Response getStatus() {
		return Response.ok(
				"{\"status\":\"Service Coffee Cab is running...\"}").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("coffee")
	public Response getCoffee() {
		String response = null;
		try {

			em = Resource.getEntityManager();
			Query query = em.createQuery("FROM com.entity.Coffee");
			List<Coffee> list = query.getResultList();
			em.close();
			response = toJSONString(list);
		} catch (Exception err) {
			response = "{\"status\":\"401\","
					+ "\"message\":\"No content found \""
					+ "\"developerMessage\":\"" + err.getMessage() + "\"" + "}";
			return Response.status(401).entity(response).build();
		}
		return Response.ok(response).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("coffee/{name}")
	public Response getCoffeeByName(@PathParam("name") String coffeeName){
		String response = null;
		try {
			em = Resource.getEntityManager();
			Coffee existingCoffee = em.find(Coffee.class, coffeeName);
			if(null == existingCoffee){
				response = "{\"status\":\"401\","
						+ "\"message\":\"No content found \""
						+ "\"developerMessage\":\"Book - "+coffeeName+" Not Found in Library" + "}";
				return Response.status(401).entity(response).build();
			}
			em.close();
			response = toJSONString(existingCoffee);
		} catch (Exception err) {
			response = "{\"status\":\"401\","
					+ "\"message\":\"No content found \""
					+ "\"developerMessage\":\"" + err.getMessage() + "\"" + "}";
			return Response.status(401).entity(response).build();
		}
		return Response.ok(response, MediaType.APPLICATION_JSON).build();
		
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("coffee")
	public Response createNewCoffee(String payload){
		System.out.println("payload - " + payload);

		// Create a new Gson object that could parse all passed in elements
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Gson gson = gsonBuilder.create();

		// Get book Object parsed from JSON string
		Coffee coffee = gson.fromJson(payload, Coffee.class);
		String returnCode = "200";
		em = Resource.getEntityManager();

		// Insert Coffee using JTA persistance with Hibernate
		try {
			em.getTransaction().begin();
			em.persist(coffee);
			em.flush();
			em.refresh(coffee);
			em.getTransaction().commit();
			em.close();
			returnCode = "{"
					+ "\"href\":\"http://localhost:8080/coffee-cab/rest/bookcab/coffee/"+coffee.getName()+"\","
					+ "\"message\":\"New coffee successfully created.\""
					+ "}";
		} catch (Exception err) {
			err.printStackTrace();
			returnCode = "{\"status\":\"500\","+
					"\"message\":\"Resource not created.\""+
					"\"developerMessage\":\""+err.getMessage()+"\""+
					"}";
			return  Response.status(500).entity(returnCode).build(); 

		}
		return  Response.status(201).entity(returnCode).build(); 
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("coffee/{name}")
	public Response updateCoffee(@PathParam("name") String coffeeName,
			String payload) {

		System.out.println("payload - " + payload);

		// Create a new Gson object that could parse all passed in elements
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
		Gson gson = gsonBuilder.create();

		
		// Get book Object parsed from JSON string
		Coffee coffee = gson.fromJson(payload, Coffee.class);
		String returnCode = "200";

		System.out.println("Coffee Name - " + coffee.getName());
		System.out.println("Coffee Plantation - " + coffee.getPlantation());
		System.out.println("Coffee Price - " + coffee.getPrice());
		System.out.println("Coffee publishedDate - " + coffee.getPublishedDate());

		em = Resource.getEntityManager();

		// Update using JTA persistance with Hibernate
		try {
			em.getTransaction().begin();
			Coffee existingCoffee = em.find(Coffee.class, coffeeName);
			System.out
					.println("Existing Coffee Name - " + existingCoffee.getName());
			existingCoffee.setPlantation(coffee.getPlantation());
			existingCoffee.setPrice(coffee.getPrice());
			existingCoffee.setPublishedDate(coffee.getPublishedDate());
			em.merge(existingCoffee);
			em.getTransaction().commit();
			em.close();
			returnCode = "{"
					+ "\"href\":\"http://localhost:8080/coffee-cab/rest/coffeecab/coffee/"+coffee.getName()+"\","
					+ "\"message\":\""+coffeeName+" was successfully updated.\""
					+ "}";
		} catch (Exception err) {
			err.printStackTrace();
			returnCode = "{\"status\":\"304\","+
					"\"message\":\"Resource not modified.\""+
					"\"developerMessage\":\""+err.getMessage()+"\""+
					"}";
			return  Response.status(304).entity(returnCode).build(); 
		}
		return  Response.status(200).entity(returnCode).build(); 
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("coffee/{name}")
	public Response deleteCoffee(@PathParam("name") String coffeeName) {
		em = Resource.getEntityManager();
		String returnCode = "";
		// Remove a book using JTA persistance with Hibernate
		try {
			em.getTransaction().begin();
			Coffee existingCoffee = em.find(Coffee.class, coffeeName);
			em.remove(coffeeName);
			em.getTransaction().commit();
			em.close();
			returnCode = "{"
					+ "\"message\":\"Coffee succesfully deleted\""
					+ "}";
		} catch (Exception err) {
			err.printStackTrace();
			returnCode = "{\"status\":\"500\","+
					"\"message\":\"Resource not deleted.\""+
					"\"developerMessage\":\""+err.getMessage()+"\""+
					"}";
			return  Response.status(500).entity(returnCode).build(); 
		}
		return Response.ok(returnCode,MediaType.APPLICATION_JSON).build();
	}

	public String toJSONString(Object object) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // ISO8601 /
																	// UTC
		Gson gson = gsonBuilder.create();
		return gson.toJson(object);
	}
}
