/*
 * **************************************************************************
 *
 * Copyright (c) 2018, EPAM SYSTEMS INC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ***************************************************************************
 */

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.resources.dto.UpdateRoleGroupDto;
import com.epam.dlab.backendapi.resources.dto.UpdateRoleUserDto;
import com.epam.dlab.backendapi.resources.dto.UserGroupDto;
import com.epam.dlab.backendapi.resources.dto.UserRoleDto;
import com.epam.dlab.backendapi.service.UserRolesService;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class UserRolesResourceTest extends TestBase {

	private static final String USER = "user";
	private static final String ROLE_ID = "id";
	private static final String GROUP = "group";
	private UserRolesService rolesService = mock(UserRolesService.class);

	@Before
	public void setup() throws AuthenticationException {
		authSetup();
	}

	@Rule
	public final ResourceTestRule resources =
			getResourceTestRuleInstance(new UserRolesResource(rolesService));

	@Test
	public void getRoles() {
		when(rolesService.getUserRoles()).thenReturn(Collections.singletonList(getUserRole()));

		final Response response = resources.getJerseyTest()
				.target("/role")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		final List<UserRoleDto> actualRoles = response.readEntity(new GenericType<List<UserRoleDto>>() {
		});

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertEquals(ROLE_ID, actualRoles.get(0).getId());
		assertEquals(singleton(USER), actualRoles.get(0).getUsers());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(rolesService).getUserRoles();
		verifyNoMoreInteractions(rolesService);
	}

	@Test
	public void getAggregatedRoles() {
		when(rolesService.getAggregatedRolesByGroup()).thenReturn(Collections.singletonList(getUserGroup()));

		final Response response = resources.getJerseyTest()
				.target("/role/group")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		final List<UserGroupDto> actualRoles = response.readEntity(new GenericType<List<UserGroupDto>>() {
		});

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertEquals(GROUP, actualRoles.get(0).getGroup());
		assertTrue(actualRoles.get(0).getRoles().isEmpty());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(rolesService).getAggregatedRolesByGroup();
		verifyNoMoreInteractions(rolesService);
	}

	@Test
	public void createRole() {

		final Response response = resources.getJerseyTest()
				.target("/role")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.post(Entity.json(getUserRole()));

		assertEquals(HttpStatus.SC_OK, response.getStatus());

		verify(rolesService).createRole(refEq(getUserRole()));
		verifyNoMoreInteractions(rolesService);
	}

	@Test
	public void addGroupToRole() {

		final Response response = resources.getJerseyTest()
				.target("/role/group")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.put(Entity.json(new UpdateRoleGroupDto(singleton(ROLE_ID), singleton(GROUP))));

		assertEquals(HttpStatus.SC_OK, response.getStatus());

		verify(rolesService).addGroupToRole(singleton(GROUP), singleton(ROLE_ID));
		verifyNoMoreInteractions(rolesService);
	}

	@Test
	public void addGroupToRoleWithValidationException() {

		final Response response = resources.getJerseyTest()
				.target("/role/group")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.put(Entity.json(new UpdateRoleGroupDto(singleton(ROLE_ID), Collections.emptySet())));

		assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, response.getStatus());

		verifyZeroInteractions(rolesService);
	}

	@Test
	public void deleteGroupFromRole() {
		final Response response = resources.getJerseyTest()
				.target("/role/group")
				.queryParam("group", GROUP)
				.queryParam("roleId", ROLE_ID)
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.delete();

		assertEquals(HttpStatus.SC_OK, response.getStatus());


		verify(rolesService).removeGroupFromRole(singleton(GROUP), singleton(ROLE_ID));
		verifyNoMoreInteractions(rolesService);
	}

	@Test
	public void deleteGroupFromRoleWithValidationException() {
		final Response response = resources.getJerseyTest()
				.target("/role/group")
				.queryParam("group", GROUP)
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.delete();

		assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());

		verifyZeroInteractions(rolesService);
	}

	@Test
	public void addUserToRole() {
		final Response response = resources.getJerseyTest()
				.target("/role/user")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.put(Entity.json(new UpdateRoleUserDto(singleton(ROLE_ID), singleton(USER))));

		assertEquals(HttpStatus.SC_OK, response.getStatus());

		verify(rolesService).addUserToRole(singleton(USER), singleton(ROLE_ID));
		verifyNoMoreInteractions(rolesService);
	}

	@Test
	public void addUserToRoleWithValidationException() {
		final Response response = resources.getJerseyTest()
				.target("/role/user")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.put(Entity.json(new UpdateRoleUserDto(emptySet(), singleton(USER))));

		assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, response.getStatus());

		verifyZeroInteractions(rolesService);
	}

	@Test
	public void deleteUserFromRole() {
		final Response response = resources.getJerseyTest()
				.target("/role/user")
				.queryParam("user", USER)
				.queryParam("roleId", ROLE_ID)
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.delete();

		assertEquals(HttpStatus.SC_OK, response.getStatus());


		verify(rolesService).removeUserFromRole(singleton(USER), singleton(ROLE_ID));
		verifyNoMoreInteractions(rolesService);
	}

	@Test
	public void deleteUserFromRoleWithValidationException() {
		final Response response = resources.getJerseyTest()
				.target("/role/user")
				.queryParam("roleId", ROLE_ID)
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.delete();

		assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());

		verifyZeroInteractions(rolesService);
	}

	private UserRoleDto getUserRole() {
		final UserRoleDto userRoleDto = new UserRoleDto();
		userRoleDto.setId(ROLE_ID);
		userRoleDto.setUsers(singleton(USER));
		return userRoleDto;
	}

	private UserGroupDto getUserGroup() {
		return new UserGroupDto(GROUP, Collections.emptyList());
	}


}