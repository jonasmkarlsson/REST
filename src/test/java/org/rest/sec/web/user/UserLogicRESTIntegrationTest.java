package org.rest.sec.web.user;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.rest.client.template.impl.RoleRESTTemplateImpl;
import org.rest.client.template.impl.UserRESTTemplateImpl;
import org.rest.sec.dto.User;
import org.rest.sec.model.Role;
import org.rest.sec.test.SecLogicRESTIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

public class UserLogicRESTIntegrationTest extends SecLogicRESTIntegrationTest<User> {

    @Autowired
    private UserRESTTemplateImpl userRestTemplate;

    @Autowired
    private RoleRESTTemplateImpl associationRestTemplate;

    public UserLogicRESTIntegrationTest() {
	super(User.class);
    }

    // tests

    // find - one

    @Test
    public final void whenResourceIsRetrieved_thenAssociationsAreAlsoRetrieved() {
	final User existingResource = getTemplate().create(getTemplate().createNewEntity());
	assertThat(existingResource.getRoles(), not(Matchers.<Role> empty()));
    }

    // create

    /**
     * - note: this test ensures that a new User cannot automatically create new Privileges <br>
     * - note: the standard way to do this is: first create the Privilege resource(s), then associate them with the new User resource and then create the User resource
     */
    @Test
    public final void whenResourceIsCreatedWithNewAssociation_then409IsReceived() {
	final User newResource = getTemplate().createNewEntity();
	newResource.getRoles().add(associationRestTemplate.createNewEntity());

	// When
	final Response response = getTemplate().createAsResponse(newResource);

	// Then
	assertThat(response.getStatusCode(), is(409));
    }

    @Test
    @Ignore("intermitent failures - temporarily ignored")
    public final void whenResourceIsCreatedWithInvalidAssociation_then409IsReceived() {
	final Role invalidAssociation = associationRestTemplate.createNewEntity();
	invalidAssociation.setId(1001l);
	final User newResource = getTemplate().createNewEntity();
	newResource.getRoles().add(invalidAssociation);

	// When
	final Response response = getTemplate().createAsResponse(newResource);

	// Then
	assertThat(response.getStatusCode(), is(409));
    }

    @Test
    public final void whenUserIsCreatedWithExistingRole_then201IsReceived() {
	final Role existingAssociation = associationRestTemplate.create(associationRestTemplate.createNewEntity());
	final User newResource = getTemplate().createNewEntity();
	newResource.getRoles().add(existingAssociation);

	// When
	final Response response = getTemplate().createAsResponse(newResource);

	// Then
	assertThat(response.getStatusCode(), is(201));
    }

    // TODO: sort

    @Test
    public final void whenScenario_getResource_getAssociationsById() {
	final Role existingAssociation = associationRestTemplate.create(associationRestTemplate.createNewEntity());
	final User resourceToCreate = getTemplate().createNewEntity();
	resourceToCreate.getRoles().add(existingAssociation);

	// When
	final User existingResource = getTemplate().create(resourceToCreate);
	for (final Role associationOfResourcePotential : existingResource.getRoles()) {
	    final Role existingAssociationOfResource = associationRestTemplate.findOne(associationOfResourcePotential.getId());
	    assertThat(existingAssociationOfResource, notNullValue());
	}
    }

    // scenarios

    @Test
    public final void whenScenarioOfWorkingWithAssociations_thenTheChangesAreCorrectlyPersisted() {
	final Role existingAssociation = associationRestTemplate.create(associationRestTemplate.createNewEntity());
	final User resource1 = new User(randomAlphabetic(6), randomAlphabetic(6), Sets.newHashSet(existingAssociation));

	final User resource1ViewOfServerBefore = getTemplate().create(resource1);
	assertThat(resource1ViewOfServerBefore.getRoles(), hasItem(existingAssociation));

	final User resource2 = new User(randomAlphabetic(6), randomAlphabetic(6), Sets.newHashSet(existingAssociation));
	getTemplate().createAsResponse(resource2);

	final User resource1ViewOfServerAfter = getTemplate().findOne(resource1ViewOfServerBefore.getId());
	assertThat(resource1ViewOfServerAfter.getRoles(), hasItem(existingAssociation));
    }

    // template method

    @Override
    protected final User createNewEntity() {
	return getTemplate().createNewEntity();
    }

    @Override
    protected final String getURI() {
	return getTemplate().getURI() + "/";
    }

    @Override
    protected final void change(final User resource) {
	resource.setName(randomAlphabetic(6));
    }

    @Override
    protected final void invalidate(final User resource) {
	getTemplate().invalidate(resource);
    }

    @Override
    protected final RequestSpecification givenAuthenticated() {
	return getTemplate().givenAuthenticated();
    }

    @Override
    protected final UserRESTTemplateImpl getTemplate() {
	return userRestTemplate;
    }

}
